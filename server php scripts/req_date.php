<?php

#
# Script allowing the recovery of all the ids and dates of 
# the processed images
#

header("Content-type: application/json");

include 'dbconnection.php';

# Connection to the databse
$conn = connection();

# Retrieval of the dates to be modified in the app
$sth = mysqli_query($conn, "SELECT id, date FROM images WHERE isWorth = 1 and published = 0");
$rows = array();

while($row = $sth->fetch_array(MYSQLI_ASSOC)) {
	$rows[] = $row;
}

# Displaying information in json format
echo json_encode($rows);

?>
