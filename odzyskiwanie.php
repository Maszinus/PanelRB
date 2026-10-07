<?php
session_start();

$error = '';
$success = '';

$database = mysqli_connect('127.0.0.1', 'root', '', '5td_panel');

if (!$database) {
    $error = 'Błąd połączenia z bazą danych.';
}

// ETAP 1 - sprawdzanie kodu odzyskiwania
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['sprawdz_kod'])) {

    $kod = $_POST['kod'] ?? '';

    if (empty($kod)) {
        $error = 'Podaj kod odzyskiwania.';
    } else {

        $hashed = sha1($kod);

        $check = mysqli_prepare(
            $database,
            "SELECT Id, Email, Rola FROM users WHERE Kod_odzyskiwania = ?"
        );

        mysqli_stmt_bind_param($check, "s", $hashed);
        mysqli_stmt_execute($check);

        $result = mysqli_stmt_get_result($check);
        $konto = mysqli_fetch_assoc($result);

        if (!$konto) {
            $error = 'Błędny kod odzyskiwania.';
        } else {

            // Zapamiętujemy ID konta w sesji
            $_SESSION['odzyskiwanie_id'] = $konto['Id'];

            // Kod został poprawnie zweryfikowany
            $success = 'Kod jest poprawny. Ustaw nowe hasło.';
        }

        mysqli_stmt_close($check);
    }
}


// ETAP 2 - zmiana hasła
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['zmien_haslo'])) {

    // Sprawdzamy, czy wcześniej poprawnie podano kod
    if (!isset($_SESSION['odzyskiwanie_id'])) {

        $error = 'Najpierw podaj poprawny kod odzyskiwania.';

    } else {

        $nowe_haslo = $_POST['nowe_haslo'] ?? '';
        $powtorz_haslo = $_POST['powtorz_haslo'] ?? '';

        if (empty($nowe_haslo) || empty($powtorz_haslo)) {

            $error = 'Wypełnij oba pola hasła.';

        } elseif ($nowe_haslo !== $powtorz_haslo) {

            $error = 'Hasła nie są takie same.';

        } else {

            $id = $_SESSION['odzyskiwanie_id'];

            // Haszujemy nowe hasło
            $hashed_password = sha1($nowe_haslo);
            $update = mysqli_prepare($database,"UPDATE users SET Haslo = ? WHERE Id = ?");
            mysqli_stmt_bind_param($update, "si", $hashed_password, $id);

            if (mysqli_stmt_execute($update)) {
                $success = 'Hasło zostało zmienione. Możesz się teraz zalogować.';
                $kod_odz = random_int(100000, 999999);
                $kod_safe = sha1($kod_odz);
                $mysql_kod = mysqli_prepare($database, "UPDATE users SET Kod_Odzyskiwania = `$kod_safe` WHERE Id = `$id`");
                mysqli_stmt_execute($mysql_kod);
                $_SESSION['kod'] = $kod_odz;
                unset($_SESSION['odzyskiwanie_id']);
            } else {

                $error = 'Wystąpił błąd podczas zmiany hasła.';
            }

            mysqli_stmt_close($update);
        }
    }
}

mysqli_close($database);
?>


<!DOCTYPE html>
<html lang="pl">
<head>
    <meta charset="UTF-8">
    <title>Odzyskiwanie konta</title>
</head>

<body>

<?php if (!empty($error)): ?>
    <p style="color: red;">
        <?= htmlspecialchars($error) ?>
    </p>
<?php endif; ?>


<?php if (!empty($success)): ?>
    <p style="color: green;">
        <?= htmlspecialchars($success) ?>
    </p>
<?php endif; ?>


<?php if (!isset($_SESSION['odzyskiwanie_id'])): ?>

    <!-- FORMULARZ KODU ODZYSKIWANIA -->

    <form name="Odzyskiwanie" method="POST" action="odzyskiwanie.php">

        <h2>Odzyskaj konto</h2>

        <input
            type="text"
            name="kod"
            placeholder="Podaj kod odzyskiwania"
            required
        >

        <br>

        <button type="submit" name="sprawdz_kod">
            Sprawdź
        </button>

    </form>


<?php else: ?>

    <!-- FORMULARZ NOWEGO HASŁA -->

    <form name="NoweHaslo" method="POST" action="odzyskiwanie.php">

        <h2>Ustaw nowe hasło</h2>

        <input
            type="password"
            name="nowe_haslo"
            placeholder="Nowe hasło"
            required
        >

        <br>

        <input
            type="password"
            name="powtorz_haslo"
            placeholder="Powtórz nowe hasło"
            required
        >

        <br>

        <button type="submit" name="zmien_haslo">
            Zmień hasło
        </button>

    </form>

<?php endif; ?>

</body>
</html>
