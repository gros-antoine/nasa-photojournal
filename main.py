from selenium import webdriver
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.by import By
from bs4 import BeautifulSoup, NavigableString
from requests import get
from PIL import Image
from io import BytesIO
from datetime import datetime
import urllib.request
import re
import time
import pyautogui
import mysql.connector
import os

# Opening the log file
log = open('/opt/bitnami/apache2/htdocs/log.txt', 'a')

now = datetime.now()
log.write('[' + now.strftime("%d/%m/%Y %H:%M:%S") + ']\n')

#
# Deletion of unwanted images
#
delete = open('/opt/bitnami/apache2/htdocs/delete.txt', 'r')
lines = delete.readlines()

for line in lines:

    line = line[:-1]

    # Getting the 5-digit number
    while len(line) < 5:
        line = "0" + line
    # Log
    log.write('Delete image id ' + line + '\n')
    
    os.remove('/opt/bitnami/apache2/htdocs/images/' + line + '.jpg')
    
delete.close()

delete = open('/opt/bitnami/apache2/htdocs/delete.txt', 'w')
delete.close()

# Connection to the database
mydb = mysql.connector.connect(

    host = "",
    user = "",
    passwd = "",
    port = "",
    db=""
)

cursor = mydb.cursor()

#
# Data recovery from 20 potential new images
#

# Retrieving the last tested id
lastid = open('/opt/bitnami/apache2/htdocs/lastid.txt', 'r')
start = int(lastid.readlines()[0]) + 1
lastid.close()

# Writing the last tested id
lastid = open('/opt/bitnami/apache2/htdocs/lastid.txt', 'w')
lastid.write(str(start+19))
lastid.close()

# Log
log.write('Check images de ' + str(start) + ' à ' + str(start+19) + "\n")

for c in range(start, start+20):

    num = str(c)

    # Getting the 5-digit number
    while len(num) != 5:
        num = "0" + num

    # Retrieving the html of the page associated with the number
    url = 'https://photojournal.jpl.nasa.gov/catalog/PIA' + num
    response = get(url)

    html = BeautifulSoup(response.text, 'lxml')

    texts = ""

    # Retrieving text related to the photo
    for t in html.find_all('td'):
        
        if t.get('bgcolor') == '#ffffff':
            
            a = t.findChildren('p')

            # Obtaining only texts
            for p in a:
                if p.findChildren('font') == []:
                    text = p.get_text()
                    if (text != '' and text != '\n'):
                        texts += text + "\n"

            texts = texts.replace("\n\n", "\n")

    # Second text formatting
    if texts == "":

        # The text is in a 'dd' element
        dd = html.find('dd')

        # Obtaining only texts
        if dd != None:        
            for p in dd.findChildren('p'):
                text = p.get_text()
                if (text != '' and text != '\n'):
                    texts += text + "\n"

            # Shitty cases are removed
            texts = texts.replace("\n\n", "\n")

    # Third text formatting
    if texts == "":
        
        # The text is in a NavigableString, retrieving them all
        try:
            texts = [e for e in dd if isinstance(e, NavigableString)]

        # If there are no NavigableString, i.e. there is no text
        except:
            pass

    # Layout of the recovered text
    # The line breaks at the beginning and end of the text are removed
    if isinstance(texts, str):

        while(texts[:1] == '\n' or texts[:1] == '\r'):
            texts = texts[1:]

        while(texts[-1:] == '\n' or texts[-1:] == '\r'):
            texts = texts[:-1]

    else:
        
        for i in range(len(texts)):
            
            while(texts[i][:1] == '\n' or texts[i][:1] == '\r'):
                texts[i] = texts[i][1:]

            while(texts[i][-1:] == '\n' or texts[i][-1:] == '\r'):
                texts[i] = texts[i][:-1]

        cleared_texts = ""

        for sub in texts:
            if sub != '':
                cleared_texts += (sub + '\r\n')

        texts = cleared_texts[:-4]

    if texts != "":

        # Log
        log.write('Image trouvée pour id ' + num + '\n')
        
        b = html.find('b')
        
        # Retriving the title
        cursor.execute("INSERT INTO images (id, titre) VALUES(%s, %s)", (c, b.get_text().replace('PIA' + str(num) + ':  ', '')))
        mydb.commit()

        # Search for image information
        for t in html.find_all('tr'):
            
            if t.get('bgcolor') == '#eeeeee':

                a = t.findChildren('td')

                # Getting the name of the info
                cat = a[0].get_text().replace('\n', '')[1:-1]
                
                # Getting the info
                info = a[1].get_text().replace('\n', '').replace('\r', '').replace('    ', '')

                # Texts layout
                re.sub('\W+','', info)

                # Retrieving only certain information
                if cat == "Target Name:":
                    cursor.execute("UPDATE images SET target = %s WHERE id = %s", (info, c))
                    mydb.commit()
                elif cat == "Is a satellite of:":
                    cursor.execute("UPDATE images SET satelliteOf = %s WHERE id = %s", (info, c))
                    mydb.commit()
                elif cat == "Mission:":
                    cursor.execute("UPDATE images SET mission = %s WHERE id = %s", (info[:-1], c))
                    mydb.commit()
                elif cat == "Spacecraft:":
                    cursor.execute("UPDATE images SET spacecraft = %s WHERE id = %s", (info, c))
                    mydb.commit()
                elif cat == "Instrument:":
                    cursor.execute("UPDATE images SET instrument = %s WHERE id = %s", (info[:-1], c))
                    mydb.commit()

        # Credits retrieving
        dd = html.find_all('dd')
        cr = dd[-2].text.split()

        credit = ""

        for text in cr:

            # Retrieving the correct credit
            if text[:4] == "NASA":
                credit = text
                break

        # Sending the credit to the database
        cursor.execute("UPDATE images SET credit = %s WHERE id = %s", (credit, c))
        
        # Sending the text to the database
        cursor.execute("UPDATE images SET text = %s WHERE id = %s", (texts, c))
        mydb.commit()

        # Retrieving the image
        image_res = get('https://photojournal.jpl.nasa.gov/jpeg/PIA' + num +'.jpg')
        img = Image.open(BytesIO(image_res.content))
        
        width, height = img.size

        coeff = 0

        # Resizing the image
        if width > height:
            coeff = 500/width
        else:
            coeff = 500/height

        resized = img.resize((int(width*coeff), int(height*coeff)))
        resized.save('/opt/bitnami/apache2/htdocs/images/' + num + '.jpg')

        os.system('chgrp bitnami /opt/bitnami/apache2/htdocs/images/' + num + '.jpg')
        os.system('chown bitnami /opt/bitnami/apache2/htdocs/images/' + num + '.jpg')
        os.system('chmod g+w /opt/bitnami/apache2/htdocs/images/' + num + '.jpg')

    else:
        # Log
        log.write('Rien trouvé pour id ' + num + '\n')

#
# Retrieving and formatting the information
# of the image to be posted
#

title_em = u'\ud83d\udcab'
mission_em = u'\ud83d\udcd6'
spacecraft_em = u'\ud83d\udef0\ufe0f'
default_em = u'\ud83c\udf20'
sun_em = u'\ud83c\udf1e'
earth_em = u'\ud83c\udf0d'
moon_em = u'\ud83c\udf1d'
mars_em = u'\ud83d\udd34'
jupiter_em = u'\ud83d\udfe0'
saturn_em = u'\ud83e\ude90'
uranus_em = u'\ud83d\udd35'
neptune_em = u'\ud83d\udd35'
credit_em = u'\u00a9'

def getEmoji(body):

    if body == "Sol (our sun)":
        return sun_em
    elif body == "Earth":
        return earth_em
    elif body == "Moon":
        return moon_em
    elif body == "Mars":
        return mars_em
    elif body == "Jupiter":
        return jupiter_em
    elif body == "Saturn":
        return saturn_em
    elif body == "Uranus":
        return uranus_em
    elif body == "Neptune":
        return neptune_em
    else:
        return default_em

cursor.execute("SELECT id, titre, target, satelliteOf, mission, spacecraft, instrument, text, credit FROM images WHERE isWorth = 1 and published = 0 ORDER BY date LIMIT 1")
data = cursor.fetchall()

rows = data[0]
desc = ""

# Retrieving all the information from the image
ide = rows[0]
titre = rows[1]
target = rows[2]
satelliteOf = rows[3]
mission = rows[4]
spacecraft = rows[5]
instrument = rows[6]
text = rows[7]
credit = rows[8]

compt_emoji = 1 # Takes into account only title and not credit because length 1

hashtag = "\n\n"

desc += (titre + " " + title_em + "\n\n")

# Log
log.write('Upload image id ' + str(ide) + '\n')

# Layout of the description
if target != "":

    if target == "Sol (our sun)":

        desc += ("Target: Our sun " + getEmoji(target) + "\n")
        hashtag += "#Sun "
        compt_emoji += 1
        
    else:

        desc += ("Target: " + target + " " + getEmoji(target) + "\n")
        hashtag += ("#" + target + " ")
        compt_emoji += 1
    
if satelliteOf != "":

    if satelliteOf == "Sol (our sun)":
        
        desc += ("Satellite of: Our sun " + getEmoji(satelliteOf) + "\n")
        compt_emoji += 1
        
    else:

        desc += ("Satellite of: " + satelliteOf + " " + getEmoji(satelliteOf)+ "\n")
        compt_emoji += 1
        
        if satelliteOf != "Earth" and satelliteOf != "Sol (our sun)":
            hashtag += ("#" + satelliteOf + " ") 
        
if mission != "":
    
    desc += ("Mission " + mission_em + ": " + mission + "\n")
    compt_emoji += 1

if spacecraft != "":
    
    desc += ("Spacecraft " + spacecraft_em + ": " + spacecraft + "\n")
    compt_emoji += 1

hashtag += "#Space #Nasa #JPL"

desc += "\n"

# Removal of excess characters
total = len(desc) + len(text) + 72 + len(credit) + len(hashtag) - compt_emoji

sub = 2200 - total

if sub < 0:

    sub = sub - 5
    text = text[:sub]
    text = text + "[...]"

desc += (text + "\n\n")

c_id = str(ide)

while len(c_id) < 5:
    c_id = "0" + c_id

# Adding credits
desc += ("More at: https://photojournal.jpl.nasa.gov/catalog/PIA" + c_id + "\n")
desc += ("Credit " + credit_em + ": " + credit)
desc += hashtag

# Log
log.write('Mise en page faite\n')

# Uploading, resizing and saving the image    
image_res = get('https://photojournal.jpl.nasa.gov/jpeg/PIA' + c_id +'.jpg')
img = Image.open(BytesIO(image_res.content))

width, height = img.size

coeff = 1

if (width >= height) and (width >= 1920):
    coeff = 1920/width
elif height >= 1920:
    coeff = 1920/height

resized = img.resize((int(width*coeff), int(height*coeff)))
resized.save('/opt/bitnami/apache2/htdocs/temp/' + c_id + '.jpg')

# Log
log.write('Dl et resize image faits\n')

#
# Image upload
#

# Start-up of Selenium and Chromium
mobile_emulation = { "deviceName": "Galaxy S5" }
chrome_options = webdriver.ChromeOptions()
chrome_options.add_experimental_option("mobileEmulation", mobile_emulation)
chrome_options.add_argument('--user-agent=Mozilla/5.0 (iPhone; CPU iPhone OS 10_3 like Mac OS X) AppleWebKit/602.1.50 (KHTML, like Gecko) CriOS/56.0.2924.75 Mobile/14E5239e Safari/602.1')
chrome_options.add_argument("--window-size=540,960")
chrome_options.add_argument('--no-sandbox')
driver = webdriver.Chrome(options=chrome_options)

# Access to Instagram
driver.get('https://www.instagram.com')
delay = 10

# Log
log.write('Instagram chargé\n')

# Login to Instagram
try:
    
    connect_button = WebDriverWait(driver, delay).until(EC.presence_of_element_located((By.XPATH, '/html/body/div[1]/section/main/article/div/div/div/div[2]/button')))
    connect_button.click()
    
    login = driver.find_element_by_xpath('/html/body/div[1]/section/main/article/div/div/div/form/div[1]/div[3]/div/label/input')
    login.send_keys('')

    password = driver.find_element_by_xpath('/html/body/div[1]/section/main/article/div/div/div/form/div[1]/div[4]/div/label/input')
    password.send_keys('')
    password.send_keys(webdriver.common.keys.Keys.ENTER)
except:

    # Log
    log.write('Problème connexion\n')

# Bypass of the first 'popup'
try:
    
    cancel_button1 = WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.XPATH, '/html/body/div[1]/section/main/div/div/div/button')))
    cancel_button1.click()
except:
    
    # Log
    log.write('Problème premier bypass\n')

time.sleep(3)

# Bypass of the second 'popup'
try:
    
    cancel_button2 = WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.XPATH, '/html/body/div[4]/div/div/div')))
    buttons = driver.find_elements_by_tag_name('button')

    for b in buttons:
        try:
            if b.text == "Cancel":
                b.click()
        except:
            pass
except:
    
    # Log
    log.write('Problème deuxième bypass\n')

# Image upload
upload_button = WebDriverWait(driver, 5).until(EC.presence_of_element_located((By.XPATH, '/html/body/div[1]/section/nav[2]/div/div/div[2]/div/div/div[3]')))
upload_button.click()

pyautogui.hotkey('ctrl', 'l')
pyautogui.write('/opt/bitnami/apache2/htdocs/temp/' + c_id)
pyautogui.press('enter')

time.sleep(3)

# Expansion of the image if possible
try:

    section = driver.find_element_by_xpath('/html/body/div[1]/section')

    button = section.find_elements_by_tag_name('button')
    for b in button:
        if b.text == 'Expand':
            b.click()
except:

    # Log
    log.write('Problème expansion image\n')

# Finishing the upload
next_button = WebDriverWait(driver, delay).until(EC.presence_of_element_located((By.XPATH, '/html/body/div[1]/section/div[1]/header/div/div[2]/button')))
next_button.click()

text_area = WebDriverWait(driver, delay).until(EC.presence_of_element_located((By.XPATH, '/html/body/div[1]/section/div[2]/section[1]/div[1]/textarea')))
text_area.click()

JS_ADD_TEXT_TO_INPUT = """
  var elm = arguments[0], txt = arguments[1];
  elm.value += txt;
  elm.dispatchEvent(new Event('change'));
  """

# Sending the description
text_area.send_keys(desc[0])
driver.execute_script(JS_ADD_TEXT_TO_INPUT, text_area, desc[1:-1])
text_area.send_keys(desc[-1])

share_button = WebDriverWait(driver, delay).until(EC.presence_of_element_located((By.XPATH, '/html/body/div[1]/section/div[1]/header/div/div[2]/button')))
share_button.click()

insta = WebDriverWait(driver, 60).until(EC.presence_of_element_located((By.XPATH, '/html/body/div[1]/section/nav[1]/div/div/header/div/h1/div/a/img')))

# Log
log.write('Image id ' + c_id + ' upload\n')

time.sleep(2)

cursor.execute("UPDATE images SET published = %s WHERE id = %s", (1, int(c_id)))
mydb.commit()

# Disconnection from the database
mydb.close()

# Chrome closure
driver.quit()

# Deleting the temporary file
os.remove('/opt/bitnami/apache2/htdocs/temp/' + c_id + '.jpg')

# Log
log.write('Fichier ' + c_id + '.jpg supprimé\n\n')
log.close()
