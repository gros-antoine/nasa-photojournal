<?php

#
# Script retrieving the id and title of 
# 10 unprocessed images
#

header("Content-type: application/json");

include 'dbconnection.php';

# Connection to the database
$conn = connection();

# Retrieval of 10 unprocessed images
$sth = mysqli_query($conn, "SELECT id, titre FROM images WHERE isWorth = 0 AND id > ".$_GET['id']." LIMIT 10");
$rows = array();

while($row = $sth->fetch_array(MYSQLI_ASSOC)) {
	$rows[] = $row;
}

# Displaying information in json format
echo json_encode($rows);

?>
