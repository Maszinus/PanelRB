package pl.panelrb;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class UserRepository {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final Database database;

    public UserRepository(Database database) {
        this.database = database;
    }

    public Optional<User> authenticate(String email, char[] password)
            throws SQLException, GeneralSecurityException {
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT Id, Email, Hasło, Rola FROM users WHERE Email = ?")) {
            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }

                String storedPassword = result.getString("Hasło");
                if (!PasswordHasher.verifyPassword(password, storedPassword)) {
                    return Optional.empty();
                }

                int id = result.getInt("Id");
                String currentEmail = result.getString("Email");
                int role = result.getInt("Rola");
                if (!storedPassword.startsWith("pbkdf2$")) {
                    upgradePasswordHash(connection, id, password);
                }
                return Optional.of(new User(id, currentEmail, role));
            }
        }
    }

    public Registration register(String email, char[] password)
            throws SQLException, GeneralSecurityException {
        String recoveryCode = newRecoveryCode();
        int userId;
        try (Connection connection = database.connect()) {
            if (emailExists(connection, email)) {
                throw new IllegalArgumentException("Ten adres e-mail jest już zarejestrowany.");
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO users (Email, Hasło, Kod_Odzyskiwania, Rola) VALUES (?, ?, ?, 1)",
                    PreparedStatement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, email);
                statement.setString(2, PasswordHasher.hashPassword(password));
                statement.setString(3, PasswordHasher.hashRecoveryCode(recoveryCode));
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Nie udało się odczytać identyfikatora nowego konta.");
                    }
                    userId = keys.getInt(1);
                }
            }
        }
        return new Registration(new User(userId, email, 1), recoveryCode);
    }

    public User changeEmail(User user, String email) throws SQLException {
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE users SET Email = ? WHERE Id = ?")) {
            statement.setString(1, email);
            statement.setInt(2, user.id());
            statement.executeUpdate();
        }
        return new User(user.id(), email, user.role());
    }

    public void changePassword(int userId, char[] password)
            throws SQLException, GeneralSecurityException {
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE users SET Hasło = ? WHERE Id = ?")) {
            statement.setString(1, PasswordHasher.hashPassword(password));
            statement.setInt(2, userId);
            statement.executeUpdate();
        }
    }

    public Optional<User> findByRecoveryCode(String code) throws SQLException, GeneralSecurityException {
        String hashedCode = PasswordHasher.hashRecoveryCode(code);
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT Id, Email, Rola FROM users WHERE Kod_Odzyskiwania = ?")) {
            statement.setString(1, hashedCode);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }
                return Optional.of(new User(
                        result.getInt("Id"), result.getString("Email"), result.getInt("Rola")));
            }
        }
    }

    public String resetPassword(int userId, char[] password)
            throws SQLException, GeneralSecurityException {
        String recoveryCode = newRecoveryCode();
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE users SET Hasło = ?, Kod_Odzyskiwania = ? WHERE Id = ?")) {
            statement.setString(1, PasswordHasher.hashPassword(password));
            statement.setString(2, PasswordHasher.hashRecoveryCode(recoveryCode));
            statement.setInt(3, userId);
            statement.executeUpdate();
        }
        return recoveryCode;
    }

    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT Id, Email, Rola FROM users ORDER BY Id");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                users.add(new User(
                        result.getInt("Id"), result.getString("Email"), result.getInt("Rola")));
            }
        }
        return users;
    }

    public void updateUser(int id, String email, int role) throws SQLException {
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE users SET Email = ?, Rola = ? WHERE Id = ?")) {
            statement.setString(1, email);
            statement.setInt(2, role);
            statement.setInt(3, id);
            statement.executeUpdate();
        }
    }

    public void deleteUser(int id) throws SQLException {
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE Id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    private void upgradePasswordHash(Connection connection, int userId, char[] password)
            throws SQLException, GeneralSecurityException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE users SET Hasło = ? WHERE Id = ?")) {
            statement.setString(1, PasswordHasher.hashPassword(password));
            statement.setInt(2, userId);
            statement.executeUpdate();
        }
    }

    private boolean emailExists(Connection connection, String email) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM users WHERE Email = ?")) {
            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private static String newRecoveryCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}
