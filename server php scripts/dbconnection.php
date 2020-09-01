<?php

#
# Script returning a connection to the database
#

function connection() {
		
	$login = "";
	$mdp = "";
	$bd = "";
	$serveur = "";
	$port = "";
		
	$conn = mysqli_connect($serveur, $login, $mdp, $bd, $port);
	return $conn;
}

?>
