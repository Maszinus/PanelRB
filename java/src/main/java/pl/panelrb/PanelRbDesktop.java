package pl.panelrb;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public final class PanelRbDesktop {
    private static final Color BACKGROUND = new Color(11, 17, 32);
    private static final Color SURFACE = new Color(17, 24, 39);
    private static final Color INPUT = new Color(24, 34, 53);
    private static final Color TEXT = new Color(229, 237, 248);
    private static final Color MUTED = new Color(154, 171, 196);
    private static final Color ACCENT = new Color(97, 168, 255);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final String LOGIN = "login";
    private static final String REGISTER = "register";
    private static final String RECOVERY = "recovery";
    private static final String RECOVERY_PASSWORD = "recovery-password";
    private static final String DASHBOARD = "dashboard";

    private final UserRepository repository;
    private final JFrame frame = new JFrame("Panel RB");
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private User currentUser;
    private User recoveryUser;
    private DefaultTableModel userTableModel;
    private JTable userTable;

    private PanelRbDesktop(UserRepository repository) {
        this.repository = repository;
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(760, 600));
        frame.setSize(900, 700);
        frame.setLocationRelativeTo(null);
        cards.setBackground(BACKGROUND);
        cards.add(createLoginPanel(), LOGIN);
        cards.add(createRegisterPanel(), REGISTER);
        cards.add(createRecoveryPanel(), RECOVERY);
        cards.add(createRecoveryPasswordPanel(), RECOVERY_PASSWORD);
        frame.setContentPane(cards);
        showCard(LOGIN);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Database database = new Database();
            try {
                database.initialize();
                new PanelRbDesktop(new UserRepository(database)).frame.setVisible(true);
            } catch (SQLException exception) {
                JOptionPane.showMessageDialog(
                        null,
                        "Nie można połączyć się z bazą Panel RB:\n" + exception.getMessage()
                                + "\n\nSprawdź, czy MySQL działa i czy baza 5td_panel jest dostępna.",
                        "Błąd połączenia", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private JPanel createLoginPanel() {
        JPanel page = pagePanel();
        JPanel form = formPanel();
        addTitle(form, "Witaj w Panel RB", "Zaloguj się do swojego konta");
        JTextField email = inputField("Adres e-mail");
        JPasswordField password = passwordField("Hasło");
        addField(form, "E-mail", email);
        addField(form, "Hasło", password);
        JButton login = primaryButton("Zaloguj");
        login.addActionListener(event -> {
            String address = email.getText().trim();
            char[] secret = password.getPassword();
            if (address.isBlank() || secret.length == 0) {
                showMessage("Wpisz adres e-mail i hasło.", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                Optional<User> user = repository.authenticate(address, secret);
                if (user.isEmpty()) {
                    showMessage("Nieprawidłowy e-mail lub hasło.", JOptionPane.ERROR_MESSAGE);
                } else {
                    currentUser = user.get();
                    showDashboard(null);
                }
            } catch (SQLException | GeneralSecurityException exception) {
                showError(exception);
            } finally {
                java.util.Arrays.fill(secret, '\0');
                password.setText("");
            }
        });
        form.add(login, fullWidth());
        JPanel links = new JPanel();
        links.setOpaque(false);
        JButton register = linkButton("Utwórz konto");
        register.addActionListener(event -> showCard(REGISTER));
        JButton recover = linkButton("Odzyskaj konto");
        recover.addActionListener(event -> showCard(RECOVERY));
        links.add(register);
        links.add(recover);
        form.add(links, fullWidth());
        page.add(form);
        return page;
    }

    private JPanel createRegisterPanel() {
        JPanel page = pagePanel();
        JPanel form = formPanel();
        addTitle(form, "Utwórz konto", "Nowe konto otrzyma kod odzyskiwania");
        JTextField email1 = inputField("adres@example.com");
        JTextField email2 = inputField("Powtórz adres e-mail");
        JPasswordField password1 = passwordField("Hasło");
        JPasswordField password2 = passwordField("Powtórz hasło");
        addField(form, "E-mail", email1);
        addField(form, "Powtórz e-mail", email2);
        addField(form, "Hasło (co najmniej 4 znaki)", password1);
        addField(form, "Powtórz hasło", password2);
        JButton create = primaryButton("Utwórz konto");
        create.addActionListener(event -> {
            String address1 = email1.getText().trim();
            String address2 = email2.getText().trim();
            char[] secret1 = password1.getPassword();
            char[] secret2 = password2.getPassword();
            try {
                if (!validEmail(address1) || !validEmail(address2)) {
                    showMessage("Podaj prawidłowy adres e-mail (maksymalnie 45 znaków).",
                            JOptionPane.WARNING_MESSAGE);
                } else if (!address1.equals(address2)) {
                    showMessage("Adresy e-mail muszą być identyczne.", JOptionPane.WARNING_MESSAGE);
                } else if (!java.util.Arrays.equals(secret1, secret2) || secret1.length < 4) {
                    showMessage("Hasła muszą być identyczne i mieć co najmniej 4 znaki.",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    Registration registration = repository.register(address1, secret1);
                    currentUser = registration.user();
                    showDashboard(registration.recoveryCode());
                }
            } catch (IllegalArgumentException exception) {
                showMessage(exception.getMessage(), JOptionPane.ERROR_MESSAGE);
            } catch (SQLException | GeneralSecurityException exception) {
                showError(exception);
            } finally {
                java.util.Arrays.fill(secret1, '\0');
                java.util.Arrays.fill(secret2, '\0');
                password1.setText("");
                password2.setText("");
            }
        });
        form.add(create, fullWidth());
        JButton back = linkButton("Wróć do logowania");
        back.addActionListener(event -> showCard(LOGIN));
        form.add(back, fullWidth());
        page.add(form);
        return page;
    }

    private JPanel createRecoveryPanel() {
        JPanel page = pagePanel();
        JPanel form = formPanel();
        addTitle(form, "Odzyskaj konto", "Wprowadź sześciocyfrowy kod odzyskiwania");
        JTextField code = inputField("Kod odzyskiwania");
        addField(form, "Kod", code);
        JButton check = primaryButton("Sprawdź kod");
        check.addActionListener(event -> {
            String recoveryCode = code.getText().trim();
            if (recoveryCode.isBlank()) {
                showMessage("Podaj kod odzyskiwania.", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                Optional<User> found = repository.findByRecoveryCode(recoveryCode);
                if (found.isEmpty()) {
                    showMessage("Błędny kod odzyskiwania.", JOptionPane.ERROR_MESSAGE);
                } else {
                    recoveryUser = found.get();
                    showCard(RECOVERY_PASSWORD);
                }
            } catch (SQLException | GeneralSecurityException exception) {
                showError(exception);
            }
        });
        form.add(check, fullWidth());
        JButton back = linkButton("Wróć do logowania");
        back.addActionListener(event -> showCard(LOGIN));
        form.add(back, fullWidth());
        page.add(form);
        return page;
    }

    private JPanel createRecoveryPasswordPanel() {
        JPanel page = pagePanel();
        JPanel form = formPanel();
        addTitle(form, "Ustaw nowe hasło", "Po zmianie otrzymasz nowy kod odzyskiwania");
        JPasswordField password1 = passwordField("Nowe hasło");
        JPasswordField password2 = passwordField("Powtórz nowe hasło");
        addField(form, "Nowe hasło", password1);
        addField(form, "Powtórz hasło", password2);
        JButton save = primaryButton("Zmień hasło");
        save.addActionListener(event -> {
            char[] secret1 = password1.getPassword();
            char[] secret2 = password2.getPassword();
            try {
                if (secret1.length == 0 || !java.util.Arrays.equals(secret1, secret2)) {
                    showMessage("Hasła muszą być identyczne i nie mogą być puste.",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String recoveryCode = repository.resetPassword(recoveryUser.id(), secret1);
                recoveryUser = null;
                showMessage("Hasło zostało zmienione. Zapisz nowy kod odzyskiwania: "
                                + recoveryCode,
                        JOptionPane.INFORMATION_MESSAGE);
                showCard(LOGIN);
            } catch (SQLException | GeneralSecurityException exception) {
                showError(exception);
            } finally {
                java.util.Arrays.fill(secret1, '\0');
                java.util.Arrays.fill(secret2, '\0');
                password1.setText("");
                password2.setText("");
            }
        });
        form.add(save, fullWidth());
        JButton back = linkButton("Wróć do logowania");
        back.addActionListener(event -> {
            recoveryUser = null;
            showCard(LOGIN);
        });
        form.add(back, fullWidth());
        page.add(form);
        return page;
    }

    private void showDashboard(String recoveryCode) {
        JPanel page = pagePanel();
        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(18, 30, 18, 30));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel greeting = new JLabel(
                "<html><div style='text-align:center'><h1>Panel RB</h1>Witaj, "
                        + escapeHtml(currentUser.email()) + "<br>Poziom dostępu: "
                        + currentUser.roleName() + "</div></html>", SwingConstants.CENTER);
        greeting.setForeground(TEXT);
        greeting.setFont(greeting.getFont().deriveFont(16f));
        header.add(greeting, BorderLayout.CENTER);
        JButton logout = linkButton("Wyloguj");
        logout.addActionListener(event -> {
            currentUser = null;
            showCard(LOGIN);
        });
        header.add(logout, BorderLayout.EAST);
        content.add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Zarządzaj kontem", createAccountPanel());
        if (currentUser.role() == 2) {
            tabs.addTab("Użytkownicy", createAdminPanel());
        }
        content.add(tabs, BorderLayout.CENTER);

        if (recoveryCode != null) {
            JLabel codeNotice = new JLabel(
                    "<html><b>Twój kod odzyskiwania: " + escapeHtml(recoveryCode)
                            + "</b><br>Zapisz go teraz — później nie będzie można go ponownie wyświetlić.</html>");
            codeNotice.setOpaque(true);
            codeNotice.setForeground(new Color(110, 231, 183));
            codeNotice.setBackground(SURFACE);
            codeNotice.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(110, 231, 183)),
                    BorderFactory.createEmptyBorder(12, 14, 12, 14)));
            content.add(codeNotice, BorderLayout.SOUTH);
        }
        page.add(content);
        Component existingDashboard = null;
        for (Component component : cards.getComponents()) {
            if (component.getName() != null && component.getName().equals(DASHBOARD)) {
                existingDashboard = component;
                break;
            }
        }
        if (existingDashboard != null) {
            cards.remove(existingDashboard);
        }
        page.setName(DASHBOARD);
        cards.add(page, DASHBOARD);
        showCard(DASHBOARD);
    }

    private JPanel createAccountPanel() {
        JPanel panel = sectionPanel();
        JTextField email1 = inputField("Nowy adres e-mail");
        JTextField email2 = inputField("Powtórz nowy adres e-mail");
        email1.setText(currentUser.email());
        email2.setText(currentUser.email());
        addField(panel, "Nowy e-mail", email1);
        addField(panel, "Powtórz e-mail", email2);
        JButton changeEmail = primaryButton("Zmień e-mail");
        changeEmail.addActionListener(event -> {
            String address1 = email1.getText().trim();
            String address2 = email2.getText().trim();
            if (!validEmail(address1) || !validEmail(address2)) {
                showMessage("Podaj prawidłowy adres e-mail (maksymalnie 45 znaków).",
                        JOptionPane.WARNING_MESSAGE);
            } else if (!address1.equals(address2)) {
                showMessage("Adresy e-mail muszą być identyczne.", JOptionPane.WARNING_MESSAGE);
            } else {
                try {
                    currentUser = repository.changeEmail(currentUser, address1);
                    showMessage("Adres e-mail został zmieniony.", JOptionPane.INFORMATION_MESSAGE);
                    showDashboard(null);
                } catch (SQLException exception) {
                    showError(exception);
                }
            }
        });
        panel.add(changeEmail, fullWidth());

        JLabel separator = new JLabel("Zmień hasło");
        separator.setForeground(TEXT);
        separator.setFont(separator.getFont().deriveFont(Font.BOLD, 16f));
        panel.add(separator, fullWidth());
        JPasswordField password1 = passwordField("Nowe hasło");
        JPasswordField password2 = passwordField("Powtórz nowe hasło");
        addField(panel, "Nowe hasło", password1);
        addField(panel, "Powtórz hasło", password2);
        JButton changePassword = primaryButton("Zmień hasło");
        changePassword.addActionListener(event -> {
            char[] secret1 = password1.getPassword();
            char[] secret2 = password2.getPassword();
            try {
                if (secret1.length == 0 || !java.util.Arrays.equals(secret1, secret2)) {
                    showMessage("Hasła muszą być identyczne i nie mogą być puste.",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    repository.changePassword(currentUser.id(), secret1);
                    showMessage("Hasło zostało zmienione.", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (SQLException | GeneralSecurityException exception) {
                showError(exception);
            } finally {
                java.util.Arrays.fill(secret1, '\0');
                java.util.Arrays.fill(secret2, '\0');
                password1.setText("");
                password2.setText("");
            }
        });
        panel.add(changePassword, fullWidth());
        return panel;
    }

    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        userTableModel = new DefaultTableModel(new Object[]{"ID", "E-mail", "Rola"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1 || column == 2;
            }
        };
        userTable = new JTable(userTableModel);
        userTable.setRowHeight(30);
        userTable.setBackground(SURFACE);
        userTable.setForeground(TEXT);
        userTable.setGridColor(new Color(43, 58, 85));
        userTable.getTableHeader().setBackground(INPUT);
        userTable.getTableHeader().setForeground(TEXT);
        userTable.getColumnModel().getColumn(2).setCellEditor(
                new javax.swing.DefaultCellEditor(new JComboBox<>(new String[]{"1 - Użytkownik", "2 - Administrator"})));
        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        JButton refresh = primaryButton("Odśwież");
        refresh.addActionListener(event -> refreshUsers());
        JButton save = primaryButton("Zapisz zmiany");
        save.addActionListener(event -> saveSelectedUser());
        JButton delete = primaryButton("Usuń użytkownika");
        delete.addActionListener(event -> deleteSelectedUser());
        actions.add(refresh);
        actions.add(save);
        actions.add(delete);
        panel.add(actions, BorderLayout.SOUTH);
        refreshUsers();
        return panel;
    }

    private void refreshUsers() {
        try {
            List<User> users = repository.findAll();
            userTableModel.setRowCount(0);
            for (User user : users) {
                userTableModel.addRow(new Object[]{
                        user.id(), user.email(),
                        user.role() == 2 ? "2 - Administrator" : "1 - Użytkownik"
                });
            }
        } catch (SQLException exception) {
            showError(exception);
        }
    }

    private void saveSelectedUser() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            showMessage("Wybierz użytkownika z tabeli.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (userTable.isEditing() && !userTable.getCellEditor().stopCellEditing()) {
            return;
        }
        int modelRow = userTable.convertRowIndexToModel(row);
        int id = (int) userTableModel.getValueAt(modelRow, 0);
        String email = String.valueOf(userTableModel.getValueAt(modelRow, 1)).trim();
        String roleValue = String.valueOf(userTableModel.getValueAt(modelRow, 2));
        int role = roleValue.startsWith("2") ? 2 : 1;
        if (!validEmail(email)) {
            showMessage("Podaj prawidłowy adres e-mail (maksymalnie 45 znaków).",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            repository.updateUser(id, email, role);
            refreshUsers();
            showMessage("Dane użytkownika zostały zapisane.", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException exception) {
            showError(exception);
        }
    }

    private void deleteSelectedUser() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            showMessage("Wybierz użytkownika z tabeli.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = userTable.convertRowIndexToModel(row);
        int id = (int) userTableModel.getValueAt(modelRow, 0);
        if (id == currentUser.id()) {
            showMessage("Nie można usunąć konta, na które jesteś zalogowany.",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int answer = JOptionPane.showConfirmDialog(
                frame, "Czy na pewno chcesz usunąć tego użytkownika?",
                "Potwierdź usunięcie", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer == JOptionPane.YES_OPTION) {
            try {
                repository.deleteUser(id);
                refreshUsers();
            } catch (SQLException exception) {
                showError(exception);
            }
        }
    }

    private JPanel pagePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(28, 24, 28, 24));
        return panel;
    }

    private JPanel formPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(43, 58, 85)),
                BorderFactory.createEmptyBorder(28, 34, 28, 34)));
        panel.setPreferredSize(new Dimension(420, 500));
        return panel;
    }

    private JPanel sectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 32, 20, 32));
        return panel;
    }

    private void addTitle(JPanel panel, String title, String subtitle) {
        GridBagConstraints constraints = fullWidth();
        constraints.insets = new Insets(8, 0, 4, 0);
        JLabel heading = new JLabel(title, SwingConstants.CENTER);
        heading.setForeground(TEXT);
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 25f));
        panel.add(heading, constraints);
        JLabel description = new JLabel(subtitle, SwingConstants.CENTER);
        description.setForeground(MUTED);
        constraints = fullWidth();
        constraints.insets = new Insets(0, 0, 18, 0);
        panel.add(description, constraints);
    }

    private void addField(JPanel panel, String label, Component field) {
        GridBagConstraints constraints = fullWidth();
        constraints.insets = new Insets(6, 0, 5, 0);
        JLabel caption = new JLabel(label);
        caption.setForeground(TEXT);
        caption.setFont(caption.getFont().deriveFont(Font.BOLD));
        panel.add(caption, constraints);
        constraints = fullWidth();
        constraints.insets = new Insets(0, 0, 8, 0);
        panel.add(field, constraints);
    }

    private JTextField inputField(String placeholder) {
        JTextField field = new JTextField();
        styleInput(field);
        field.putClientProperty("JTextField.placeholderText", placeholder);
        return field;
    }

    private JPasswordField passwordField(String placeholder) {
        JPasswordField field = new JPasswordField();
        styleInput(field);
        field.putClientProperty("JTextField.placeholderText", placeholder);
        return field;
    }

    private void styleInput(JTextField field) {
        field.setForeground(TEXT);
        field.setCaretColor(TEXT);
        field.setBackground(INPUT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(43, 58, 85)),
                BorderFactory.createEmptyBorder(9, 10, 9, 10)));
        field.setPreferredSize(new Dimension(320, 42));
    }

    private JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(new Color(8, 17, 31));
        button.setBackground(ACCENT);
        button.setFont(button.getFont().deriveFont(Font.BOLD));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        return button;
    }

    private JButton linkButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(ACCENT);
        button.setBackground(SURFACE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        return button;
    }

    private GridBagConstraints fullWidth() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.insets = new Insets(4, 0, 4, 0);
        return constraints;
    }

    private void showCard(String name) {
        cardLayout.show(cards, name);
        cards.revalidate();
        cards.repaint();
    }

    private void showMessage(String message, int type) {
        JOptionPane.showMessageDialog(frame, message, "Panel RB", type);
    }

    private void showError(Exception exception) {
        showMessage("Operacja nie powiodła się:\n" + exception.getMessage(),
                JOptionPane.ERROR_MESSAGE);
    }

    private static boolean validEmail(String email) {
        return email.length() <= 45 && EMAIL_PATTERN.matcher(email).matches();
    }

    private static String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}
