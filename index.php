<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Panel RB</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>
    <?php
        session_start();
        $_SESSION['access'] = 0;
    ?>
<main>
    <h1 class="page-title">Panel RB</h1>
    <div class="auth-layout">
<form name="Tworzenie konta" method="POST" action="form.php">
    <h2>Utwórz Konto</h2>
    <input type="email" name="email1" placeholder="Podaj adres email"><br>
    <input type="email" name="email2" placeholder="Powtórz adres email"><br>
    <input type="password" name="passwd1" placeholder="Podaj hasło"><br>
    <input type="password" name="passwd2" placeholder="Powtórz hasło"><br><br>
    <button type="submit" name="create">Utwórz Konto</button>
</form>
<form name="Logowanie" method="POST" action="login.php">
    <h2>Zaloguj</h2>
    <input type="email" name="email" placeholder="Podaj adres email"><br>
    <input type="password" name="passwd" placeholder="Podaj hasło"><br><br>
    <button type="submit" name="create">Zaloguj</button>
</form><br>
    </div>
    <p class="footer-link"><a href="odzyskiwanie.php">Odzyskaj konto</a></p>
</main>
</body>
</html>