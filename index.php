<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Panel RB</title>
</head>
<body>
    <?php
        session_start();
        $_SESSION['access'] = 0;   
    ?>
<form name="Tworzenie konta" method="POST" action="form.php">
    <h2>Utwórz Konto</h2>
    <input type="email" name="email1" placeholder="Podaj adres email"><br>
    <input type="email" name="email2" placeholder="Powtórz adres email"><br>
    <input type="password" name="passwd1" placeholder="Podaj hasło"><br>
    <input type="password" name="passwd2" placeholder="Powtórz hasło"><br>
    <button type="submit" name="create">Utwórz Konto</button>
</form>
<form name="Logowanie" method="POST" action="login.php">
    <h2>Zaloguj</h2>
    <input type="email" name="email1" placeholder="Podaj adres email"><br>
    <input type="password" name="passwd1" placeholder="Podaj hasło"><br>
    <button type="submit" name="create">Zaloguj</button>
</form><br>
<a href="odzyskiwanie.php">Odzyskaj konto</a>
</body>
</html>