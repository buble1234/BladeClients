package win.blade.core.module.api;

public enum Category {
    COMBAT("Combat"),
    MOVE("Move"),
    PLAYER("Player"),
    RENDER("Render"),
    MISC("Misc");

    private final String name;

    Category(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}