<?php
session_start();

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $email1 = trim($_POST['email1'] ?? '');
    $email2 = trim($_POST['email2'] ?? '');
    $passwd1 = $_POST['passwd1'] ?? '';
    $passwd2 = $_POST['passwd2'] ?? '';

    if ($email1 !== $email2) {
        $error = 'Emaile nie pasują do siebie!';
    }

    elseif ($passwd1 !== $passwd2 || strlen($passwd1) < 4) {
        $error = 'Hasła nie pasują do siebie lub są za krótkie!';
    } else {
        $database = mysqli_connect('127.0.0.1', 'root', '', '5td_panel');
        if (!$database) {
            $error = 'Błąd połączenia z bazą danych.';
        } else {
            $hashed = sha1($passwd1);
            $check = mysqli_prepare($database, "SELECT Id FROM konta WHERE Email = '$email1'");
            mysqli_stmt_execute($check);
            $result = mysqli_stmt_get_result($check);
            if (mysqli_num_rows($result) > 0) {
                $error = 'Ten email jest już zarejestrowany!';
                } else {
                    $kod_odz = random_int(100000, 999999);
                    $kod_safe = sha1($kod_odz);
                    $register = mysqli_prepare($database, "INSERT INTO konta (Email, Haslo, Kod_odzyskiwania, Rola) VALUES ('$email1', '$hashed', '$kod_safe', '1')");
                if (mysqli_stmt_execute($register)) {
                    $_SESSION['access'] = 1;
                    $_SESSION['login'] = $email1;
                    $_SESSION['type'] = 1;
                    $_SESSION['kod'] = $kod_odz;
                    mysqli_close($database);
                    header('Location: panel.php');
                    exit();
                } else {
                    $error = 'Błąd podczas rejestracji.';
                }
                mysqli_close($database);
            }
        }
    }
}
?>