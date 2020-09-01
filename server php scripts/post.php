<?php

#
# Script to add an entry in the database
#

include 'dbconnection.php';

# Connection to the databse
$conn = connection();

# Retrieval of the most recent date from the database
$date = mysqli_query($conn, "SELECT date FROM images ORDER BY date DESC LIMIT 1");

mysqli_close($conn);
$rows = array();

while($row = $date->fetch_array(MYSQLI_ASSOC)) {
	$rows[] = $row;
}

# Addition of 1 day to the previous date
$date_str = str_replace('-', '/', $rows[0][date]);
$tomorrow = date("Y-m-d", strtotime($date_str . "+1 days"));

# Retrieval of all the information for the entry
$id = $_GET['id'];
$titre = $_GET['titre'];
$target = $_GET['target'];
$satellite = $_GET['satelliteOf'];
$mission = $_GET['mission'];
$spacecraft = $_GET['spacecraft'];
$instrument = $_GET['instrument'];
$text = $_GET['text'];
$worth = $_GET['isWorth'];
$treated = $_GET['treated'];

# Addition to the database
$sql = "UPDATE images SET titre = '$titre', target = '$target', satelliteOf = '$satellite', mission = '$mission', spacecraft = '$spacecraft', instrument = '$instrument', text = '$text', isWorth = $worth, treated = $treated, date = '$tomorrow' WHERE id = $id";

$conn2 = connectionPDO();
$query = mysqli_query($conn2, $sql);

?>
