<?php

#
# Script counting the number of
# interesting images
#

include 'dbconnection.php';

# Connection to the database
$conn = connection();

# Retrieval of the number of images
$query = mysqli_query($conn, "SELECT COUNT(*) AS nb FROM images WHERE isWorth = 1 AND published = 0");

$count = mysqli_fetch_assoc($query);

echo $count['nb'];

?>
