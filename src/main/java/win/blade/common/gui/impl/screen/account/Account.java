package win.blade.common.gui.impl.screen.account;

import java.time.LocalDateTime;

public class Account {
    private final String username;
    private final LocalDateTime creationDate;

    public Account(String username, LocalDateTime creationDate, boolean favorite) {
        this.username = username;
        this.creationDate = creationDate;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

}