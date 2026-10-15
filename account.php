<?php
// Upewnij się, że sesja jest uruchomiona na początku pliku, jeśli jeszcze jej nie ma
// session_start();

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
                echo "<p style='color:green; text-align:center;'>Adres email został zmieniony pomyślnie.</p>";
            } else {
                echo "<p style='color:red; text-align:center;'>Błąd zmiany emaila: " . mysqli_error($database) . "</p>";
            }
        } else {
            echo "<p style='color:red; text-align:center;'>Podane adresy email muszą być identyczne.</p>";
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
                echo "<p style='color:green; text-align:center;'>Hasło zostało zmienione pomyślnie.</p>";
            } else {
                echo "<p style='color:red; text-align:center;'>Błąd zmiany hasła: " . mysqli_error($database) . "</p>";
            }
        } else {
            echo "<p style='color:red; text-align:center;'>Hasła muszą być takie same i nie mogą być puste.</p>";
        }
    }

    // Wyświetlanie formularzy
    echo '
    <center>
        <h2>Zarządzaj kontem</h2>
        
        <!-- Formularz zmiany emaila -->
        <form name="EmailForm" method="POST" action="account.php">
            <b>Zmień email</b><br>
            <input type="text" name="em1" required value="'.$email.'"><br>
            <input type="text" name="em2" required value="'.$email.'"><br>
            <button type="submit" name="change_em">Zmień email</button>
        </form>
        
        <br><hr style="width: 15%;"><br>

        <!-- Formularz zmiany hasła -->
        <form name="PassForm" method="POST" action="account.php">
            <b>Zmień hasło</b><br>
            <input type="password" name="pass1" required placeholder="Nowe hasło"><br>
            <input type="password" name="pass2" required placeholder="Powtórz nowe hasło"><br>
            <button type="submit" name="change_pass">Zmień hasło</button>
        </form>
    </center>
    ';
    
    mysqli_close($database);
} else {
    echo "Brak dostępu";
}
?>