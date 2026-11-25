package pl.panelrb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {
    private static final String DEFAULT_URL =
            "jdbc:mysql://127.0.0.1:3306/5td_panel?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC";

    private final String url;
    private final String username;
    private final String password;

    public Database() {
        url = setting("PANEL_DB_URL", DEFAULT_URL);
        username = setting("PANEL_DB_USER", "root");
        password = setting("PANEL_DB_PASSWORD", "");
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public void initialize() throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        Id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        Email VARCHAR(45) NOT NULL,
                        Hasło VARCHAR(255) NOT NULL,
                        Kod_Odzyskiwania VARCHAR(255) NOT NULL,
                        Rola INT NOT NULL DEFAULT 1
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                    """);
        }
    }

    private static String setting(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
