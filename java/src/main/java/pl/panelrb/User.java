package pl.panelrb;

public record User(int id, String email, int role) {
    public String roleName() {
        return role == 2 ? "Administrator" : "Użytkownik";
    }
}
