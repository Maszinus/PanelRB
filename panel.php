<?php
session_start();
$role = "";
if($_SESSION['type'] == 1){
    $role = "użytkownik";
} else if($_SESSION['type'] == 2){
    $role = "administrator";
}
    if($_SESSION["access"] != "1") {
        echo "<!DOCTYPE html><html lang='pl'><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'><title>Brak dostępu | Panel RB</title><link rel='stylesheet' href='styles.css'></head><body><main class='page-content'><p class='notice error'>Brak dostępu</p><p class='footer-link'><a href='index.php'>Zaloguj się</a></p></main></body></html>";
        } else {
            echo "<!DOCTYPE html><html lang='pl'><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'><title>Panel | Panel RB</title><link rel='stylesheet' href='styles.css'></head><body><main class='page-content'><header class='panel-header'><h1>Panel RB</h1><p><b>Witaj w panelu ".htmlspecialchars($_SESSION['login'])."!</b><br>Poziom dostępu: ".$role."</p></header>";
            if(isset($_SESSION["kod"]) ) {
                echo "<p class='notice success'>Twój kod odzyskiwania to: <b>". htmlspecialchars((string)$_SESSION['kod']) . "</b><br><b>UWAGA: To jedyna szansa aby go zapisać</b></p>";
                unset($_SESSION['kod']);
            }
            // Zarządzaj kontem
            require('account.php');
            // Panel Admina
            if($_SESSION['type'] == 2){
                require('admin.php');
            }
            echo "<p class='footer-link'><a href='wyloguj.php'>Wyloguj się</a></p></main></body></html>";
        }
?>