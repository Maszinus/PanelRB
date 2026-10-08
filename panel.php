<?php
session_start();
$role = "";
if($_SESSION['type'] == 1){
    $role = "użytkownik";
} else if($_SESSION['type'] == 2){
    $role = "administrator";
}
    if($_SESSION["access"] != "1") {
        echo "<center><b>Brak dostępu<br><a href='index.php'>Zaloguj się</a></b></center>";
        } else {
            echo "<center><b>Witaj w panelu ".$_SESSION['login']."!</b><br>Poziom dostępu: ".$role."</center>";
            if(isset($_SESSION["kod"]) ) {
            echo "<center>Twój kod odzyskiwania to: <b>". $_SESSION['kod'] . "</b><br></b>UWAGA: To jedyna szansa aby go zapisać</b></center>";
            unset($_SESSION['kod']);
            }
            echo "<center><a href='wyloguj.php'>Wyloguj się</a></center>";
        }
?>