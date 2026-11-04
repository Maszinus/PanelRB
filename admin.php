<?php

if($_SESSION['type'] == 2){
    $database = mysqli_connect('127.0.0.1', 'root', '', '5td_panel');

    if (!$database) {
        die("Błąd połączenia z bazą danych: " . mysqli_connect_error());
    }

    mysqli_set_charset($database, "utf8mb4");

    if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['save'])) {
        $id = (int)($_POST['id'] ?? 0);
        $email = trim($_POST['email'] ?? '');
        $rola = trim($_POST['rola'] ?? '');

        if ($id > 0 && $email !== '' && $rola !== '') {
            $stmt = mysqli_prepare(
                $database,
                "UPDATE users SET Email = ?, Rola = ? WHERE Id = ?"
            );
            mysqli_stmt_bind_param($stmt, "ssi", $email, $rola, $id);
            mysqli_stmt_execute($stmt);
            mysqli_stmt_close($stmt);
        }
        header("Location: panel.php");
        exit;
    }

    if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['delete'])) {
        $id = (int)($_POST['id'] ?? 0);
        if ($id > 0) {
            $stmt = mysqli_prepare(
                $database,
                "DELETE FROM users WHERE Id = ?"
            );
            mysqli_stmt_bind_param($stmt, "i", $id);
            mysqli_stmt_execute($stmt);
            mysqli_stmt_close($stmt);
        }
        header("Location: panel.php");
        exit;
    }
    $result = mysqli_query(
        $database,
        "SELECT Id, Email, Rola FROM users ORDER BY Id"
    );

    if (!$result) {
        die("Błąd zapytania: " . mysqli_error($database));
    }
    ?>

    <section class="panel-section">
    <h2>Zarządzaj Użytkownikami</h2>
    <div class="table-wrap">
    <table>
        <tr>
            <th>ID</th>
            <th>Email</th>
            <th>Rola</th>
            <th>Akcje</th>
        </tr>

        <?php while ($user = mysqli_fetch_assoc($result)): ?>
            <tr>
                <form method="POST" action="panel.php">
                    <td><?= htmlspecialchars($user['Id']) ?></td>
                    <td><input type="email" name="email" value="<?= htmlspecialchars($user['Email']) ?>" required></td>
                    <td><select name="rola" required>
                        <option value="1" <?= $user['Rola'] == 1 ? 'selected' : '' ?>>1 - Użytkownik</option>
                    <option value="2" <?= $user['Rola'] == 2 ? 'selected' : '' ?>>2 - Administrator</option>
                    </select></td>
                    <td><input type="hidden" name="id" value="<?= htmlspecialchars($user['Id']) ?>">
                    <button type="submit" name="save">Zapisz</button>
                </form>
                        <form method="POST" action="panel.php" style="display:inline;" onsubmit="return confirm('Czy na pewno chcesz usunąć tego użytkownika?');">
                        <input type="hidden" name="id" value="<?= htmlspecialchars($user['Id']) ?>">
                        <button type="submit" name="delete">Usuń</button>
                    </form>
                </td>
            </tr>
        <?php endwhile; ?>
    </table>
    </div>
    </section>
    <?php
    mysqli_close($database);
} else {
    header('location: panel.php');
}
?>
