<?php
session_start();

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $email = trim($_POST['email'] ?? '');
    $passwd = $_POST['passwd'] ?? '';

    if (!function_exists('mysqli_connect')) {
        $error = 'Brak rozszerzenia mysqli w PHP. Włącz PHP MySQL (mysqli) na serwerze.';
    } else {
        $database = mysqli_connect('127.0.0.1', 'root', '', '5td_panel');
    }

    if (isset($database) && !$database) {
        $error = 'Błąd połączenia z bazą danych.';
    } elseif (isset($database)) {
        $check = mysqli_prepare($database, "SELECT Id, Email, Hasło, Rola FROM users WHERE Email = ?");

        mysqli_stmt_bind_param($check, "s", $email);
        mysqli_stmt_execute($check);
        $result = mysqli_stmt_get_result($check);
        $konto = mysqli_fetch_assoc($result);

        if (!$konto) {
            $error = 'Nieprawidłowy e-mail lub hasło.';
        } else {
            $hashed = sha1($passwd);
            if ($hashed !== $konto['Hasło']) {
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
<!DOCTYPE html>
<html lang="pl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Logowanie | Panel RB</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>
<main class="page-content">
    <?php if (!empty($error)): ?>
        <p class="notice error"><?= htmlspecialchars($error) ?></p>
        <p class="footer-link"><a href="index.php">Wróć do logowania</a></p>
    <?php endif; ?>
</main>
</body>
</html>