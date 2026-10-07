<?php
session_start();
    if($_SESSION["access"] == "1") {
        echo "<h1>Witaj w panelu!</h1>";
        if(isset($_SESSION["kod"]) ) {
        echo "Twój kod odzyskiwania to: <b>". $_SESSION['kod'] . "</b><br></b>UWAGA: To jedyna szansa aby go zapisać</b>";
        unset($_SESSION['kod']);
    }
     
    } else {
        echo "Brak dostępu";
    }

?>