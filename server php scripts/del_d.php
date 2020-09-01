<?php

# 
# Script deleting an entry in the database
# according to an id
# 

include 'dbconnection.php';

# Connection to the databse
$conn = connection();

# Retrievial of the id
$id = $_GET['id'];

# Deletion of the entry in the database
$sth = mysqli_query($conn, "DELETE FROM images WHERE id = ".$id);

# Saving of the id of the deleted image to delete later the .jpg
$fp = fopen('/opt/bitnami/apache2/htdocs/delete.txt', 'a');
fwrite($fp, ''.$id."\n");
fclose($fp);

?>
