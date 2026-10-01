<?php
session_start();

    echo "<h1>Witaj w panelu!</h1>";
    echo "Twój kod odzyskiwania to: <b>". $_SESSION['kod'] . "</b><br></b>UWAGA: To jedyna szansa aby go zapisać</b>";
    unset($_SESSION['kod']);

?>