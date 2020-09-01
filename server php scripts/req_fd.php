<?php

#
# Script retrieving the id, title and date
# of 10 images processed from a certain
# date onward
#

header("Content-type: application/json");

include 'dbconnection.php';

# Connection to the databse
$conn = connection();

# Retrieval of the date and the 10 images
$date = $_GET['date'];
$sth = mysqli_query($conn, "SELECT id, titre, date FROM images WHERE isWorth = 1 AND published = 0 AND date > '$date' ORDER BY date LIMIT 10");
$rows = array();

while($row = $sth->fetch_array(MYSQLI_ASSOC)) {
	$rows[] = $row;
}

# Displaying information in json format
echo json_encode($rows);

?>
