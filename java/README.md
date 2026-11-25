# Panel RB Desktop

Desktopowa aplikacja Java Swing korzystająca z tej samej bazy MySQL `5td_panel` co wersja PHP.

## Wymagania

- Java 17 lub nowsza
- Maven 3.8 lub nowszy do budowania
- Uruchomiony serwer MySQL/MariaDB i baza `5td_panel`

Sterownik MySQL jest pobierany przez Maven i dołączany do gotowego JAR-a.

## Baza danych

Utwórz bazę, jeśli jeszcze nie istnieje, i zaimportuj `../users.sql`:

```sql
CREATE DATABASE 5td_panel CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

Import SQL można wykonać narzędziem `mysql`:

```bash
mysql -u root -p 5td_panel < ../users.sql
```

Aplikacja tworzy tabelę `users`, jeśli nie istnieje. Domyślne połączenie to `127.0.0.1:3306`, użytkownik `root`, puste hasło. Ustawienia można zmienić zmiennymi środowiskowymi:

| Zmienna | Domyślna wartość |
| --- | --- |
| `PANEL_DB_URL` | `jdbc:mysql://127.0.0.1:3306/5td_panel?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC` |
| `PANEL_DB_USER` | `root` |
| `PANEL_DB_PASSWORD` | puste |

## Budowanie i uruchamianie

W katalogu `java/` wykonaj:

```bash
mvn package
java -jar target/panel-rb-desktop-1.0.0.jar
```

Gotowy JAR zawiera sterownik JDBC. Serwer MySQL nadal musi być uruchomiony podczas korzystania z aplikacji.

## Funkcje

- Logowanie istniejącymi kontami, rejestracja i odzyskiwanie hasła kodem odzyskiwania
- Zmiana adresu e-mail i hasła
- Panel administratora do edycji ról i adresów e-mail oraz usuwania kont

Istniejące hasła SHA-1 są akceptowane i aktualizowane do PBKDF2 przy pierwszym udanym logowaniu. Nowe konta i zmieniane hasła są zapisywane jako PBKDF2, więc po użyciu tych funkcji do logowania do starej wersji PHP potrzebna byłaby jej aktualizacja.
