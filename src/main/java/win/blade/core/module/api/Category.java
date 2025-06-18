package win.blade.core.module.api;

public enum Category {
    COMBAT("Combat", "O"),
    MOVE("Movement", "I"),
    PLAYER("Player", "J"),
    RENDER("Render", "L"),
    MISC("Misc", "C");


    private final String name;
    private final String i;

    Category(String name,String iconChar) {
        this.name = name;
        this.i = iconChar;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return i;
    }

}