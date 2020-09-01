<?php

#
# Script to update an entry in the database
#

include 'dbconnection.php';

# Connection to the databse
$conn = connection();

# Retrieval of all the information to be modified
$id = $_GET['id'];
$titre = $_GET['titre'];
$target = $_GET['target'];
$satellite = $_GET['satelliteOf'];
$mission = $_GET['mission'];
$spacecraft = $_GET['spacecraft'];
$instrument = $_GET['instrument'];
$text = $_GET['text'];

# Updating the entry
$sql = "UPDATE images SET titre = '$titre', target = '$target', satelliteOf = '$satellite', mission = '$mission', spacecraft = '$spacecraft', instrument = '$instrument', text = '$text' WHERE id = $id";
$query = mysqli_query($conn, $sql);

?>
