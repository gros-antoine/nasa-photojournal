<?php

# 
# Script deleting an already processed
# image from the database according to
# and id by correctiob the publishing dates
# 

include 'dbconnection.php';

# Connection to the database
$conn = connection();

# Retrievial of the id and the publishing date of the associated image
$id = $_GET['id'];
$date = mysqli_query($conn, "SELECT date FROM images WHERE id = ".$id);
mysqli_close($conn);

$rows = array();

while($row = $date->fetch_array(MYSQLI_ASSOC)) {
	$rows[] = $row;
}

$conn = connectionPDO();

# Retrievial of all ids and dates with a date superior to the previous date
$sth = mysqli_query($conn, "SELECT id, date FROM images WHERE date > '".$rows[0]["date"]."' and published = 0");

$rows2 = array();

while($row2 = $sth->fetch_array(MYSQLI_ASSOC)) {
	$rows2[] = $row2;
}

mysqli_close($conn);

$i = 0;
while($i < count($rows2)) {
	
	# Subtract 1 day from the dates
	$date_str = str_replace('-', '/', $rows2[$i]["date"]);
	$yesterday = date("Y-m-d", strtotime($date_str . "-1 days"));
	
	# Update of the new dates in the database
	$conn = connectionPDO();
	$id2 = $rows2[$i]["id"];
	$sql = "UPDATE images SET date = '$yesterday' WHERE id = $id2";
	$oui = mysqli_query($conn, $sql);
	mysqli_close($conn);
	$i++;
}

# Deletion of the image with the initial id
$conn = connectionPDO();
$sth = mysqli_query($conn, "DELETE FROM images WHERE id = ".$id);
mysqli_close($conn);

# Saving of the id of the deleted image to delete later the .jpg
$fp = fopen('/opt/bitnami/apache2/htdocs/delete.txt', 'a'); 
fwrite($fp, ''.$id."\n");
fclose($fp);

?>
