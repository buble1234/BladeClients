package win.blade.common.gui.impl.screen.account;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class Account {
    private String username;
    private long creationDate;
    private boolean favourite = false;

    public Account(String username, long creationDate) {
        this.username = username;
        this.creationDate = creationDate;
    }

    public Account(String username, LocalDateTime time){
        this(username, time.toEpochSecond(ZoneOffset.UTC));
    }


    public Account favourite(boolean pFavourite){
        favourite = pFavourite;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public LocalDateTime getCreationDateTime(){
        return LocalDateTime.ofEpochSecond(creationDate, 0, ZoneOffset.UTC);
    }

}