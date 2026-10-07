<?php
session_start();

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $email = trim($_POST['email'] ?? '');
    $passwd = $_POST['passwd'] ?? '';

    $database = mysqli_connect('127.0.0.1', 'root', '', '5td_panel');

    if (!$database) {
        $error = 'Błąd połączenia z bazą danych.';
    } else {
        $check = mysqli_prepare($database, "SELECT Id, Email, Haslo, Rola FROM konta WHERE Email = ?");

        mysqli_stmt_bind_param($check, "s", $email);
        mysqli_stmt_execute($check);
        $result = mysqli_stmt_get_result($check);
        $konto = mysqli_fetch_assoc($result);

        if (!$konto) {
            $error = 'Nieprawidłowy e-mail lub hasło.';
        } else {
            $hashed = sha1($passwd);
            if ($hashed !== $konto['Haslo']) {
                $error = 'Nieprawidłowy e-mail lub hasło.';
            } else {
                $_SESSION['access'] = 1;
                $_SESSION['login'] = $konto['Email'];
                $_SESSION['type'] = $konto['Rola'];
                mysqli_stmt_close($check);
                mysqli_close($database);
                header('Location: panel.php');
                exit();
            }
        }
        mysqli_stmt_close($check);
        mysqli_close($database);
    }
}
?>