<?php
session_start();
unset($_SESSION['access']);
unset($_SESSION['login']);
unset($_SESSION['type']);
session_destroy();
header('location: index.php');
exit();
?>