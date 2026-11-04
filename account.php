<?php
// Upewnij się, że sesja jest uruchomiona na początku pliku, jeśli jeszcze jej nie ma
$standalone = basename($_SERVER['SCRIPT_FILENAME'] ?? '') === basename(__FILE__);
if ($standalone && session_status() !== PHP_SESSION_ACTIVE) {
    session_start();
}

if ($standalone) {
    echo '<!DOCTYPE html>
<html lang="pl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Zarządzaj kontem | Panel RB</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>
<main class="page-content">';
}

if(isset($_SESSION['type']) && $_SESSION['type'] >= 1){
    $database = mysqli_connect('127.0.0.1', 'root', '', '5td_panel');

    if (!$database) {
        die("Błąd połączenia z bazą danych: " . mysqli_connect_error());
    }

    $email = $_SESSION['login'];

    // Obsługa zmiany emaila
    if(isset($_POST['change_em'])) {
        $em1 = mysqli_real_escape_string($database, $_POST['em1']);
        $em2 = mysqli_real_escape_string($database, $_POST['em2']);

        if($em1 === $em2) {
            $query = "UPDATE users SET Email = '$em1' WHERE Email = '$email'";
            if(mysqli_query($database, $query)) {
                $_SESSION['login'] = $em1; // Aktualizacja sesji
                $email = $em1;
                echo "<p class='notice success'>Adres email został zmieniony pomyślnie.</p>";
            } else {
                echo "<p class='notice error'>Błąd zmiany emaila: " . htmlspecialchars(mysqli_error($database)) . "</p>";
            }
        } else {
            echo "<p class='notice error'>Podane adresy email muszą być identyczne.</p>";
        }
    }

    // Obsługa zmiany hasła
    if(isset($_POST['change_pass'])) {
        $pass1 = $_POST['pass1'];
        $pass2 = $_POST['pass2'];

        if(!empty($pass1) && $pass1 === $pass2) {
            $hashed_pass = sha1($pass1); // Szyfrowanie hasła funkcją sha1()
            $query = "UPDATE users SET Hasło = '$hashed_pass' WHERE Email = '$email'";
            if(mysqli_query($database, $query)) {
                echo "<p class='notice success'>Hasło zostało zmienione pomyślnie.</p>";
            } else {
                echo "<p class='notice error'>Błąd zmiany hasła: " . htmlspecialchars(mysqli_error($database)) . "</p>";
            }
        } else {
            echo "<p class='notice error'>Hasła muszą być takie same i nie mogą być puste.</p>";
        }
    }

    // Wyświetlanie formularzy
    echo '
    <section class="panel-section">
        <h2>Zarządzaj kontem</h2>

        <!-- Formularz zmiany emaila -->
        <form name="EmailForm" method="POST" action="panel.php">
            <b>Zmień email</b><br>
            <input type="email" name="em1" required value="'.htmlspecialchars($email, ENT_QUOTES, 'UTF-8').'"><br>
            <input type="email" name="em2" required value="'.htmlspecialchars($email, ENT_QUOTES, 'UTF-8').'"><br>
            <button type="submit" name="change_em">Zmień email</button>
        </form>

        <br><hr style="width: 15%;"><br>

        <!-- Formularz zmiany hasła -->
        <form name="PassForm" method="POST" action="panel.php">
            <b>Zmień hasło</b><br>
            <input type="password" name="pass1" required placeholder="Nowe hasło"><br>
            <input type="password" name="pass2" required placeholder="Powtórz nowe hasło"><br>
            <button type="submit" name="change_pass">Zmień hasło</button>
        </form>
    </section>
    ';

    mysqli_close($database);
} else {
    echo "<p class='notice error'>Brak dostępu</p>";
}

if ($standalone) {
    echo '</main>
</body>
</html>';
}
?>