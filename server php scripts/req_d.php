<?php

#
# Script to retrieve information from an id
#

header("Content-type: application/json");

include 'dbconnection.php';

# Connection to the databse
$conn = connection();

# Retrieval of the information
$sth = mysqli_query($conn, "SELECT titre, target, satelliteOf, mission, spacecraft, instrument, text FROM images WHERE id = ".$_GET['id']);
$rows = array();

while($row = $sth->fetch_array(MYSQLI_ASSOC)) {
	$rows[] = $row;
}

# Displaying information in json format
echo json_encode($rows);

?>
