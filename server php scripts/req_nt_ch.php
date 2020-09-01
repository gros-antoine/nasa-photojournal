<?php

#
# Script counting the number of images
# not yet processed
#

include 'dbconnection.php';

# Connection to the database
$conn = connection();

# Retrieval of the number of images
$query = mysqli_query($conn, "SELECT COUNT(*) AS nb FROM images WHERE isWorth = 0");

$count = mysqli_fetch_assoc($query);

echo $count['nb'];

?>
