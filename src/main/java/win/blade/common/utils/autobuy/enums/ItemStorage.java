package win.blade.common.utils.autobuy.enums;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

/**
 * Автор: NoCap
 * Дата создания: 13.09.2025
 */
public enum ItemStorage {
    DIAMOND_SWORD(Items.DIAMOND_SWORD, 400),
    GOLDEN_APPLE(Items.GOLDEN_APPLE, 200),
    IRON_PICKAXE(Items.IRON_PICKAXE, 75),
    ENDER_PEARL(Items.ENDER_PEARL, 150),
    TOTEM_OF_UNDYING(Items.TOTEM_OF_UNDYING, 10000);

    private final Item item;
    private final int price;

    ItemStorage(Item item, int price) {
        this.item = item;
        this.price = price;
    }

    public Item getItem() {
        return item;
    }

    public int getPrice() {
        return price;
    }

    public static ItemStorage fromItem(Item item) {
        for (ItemStorage value : values()) {
            if (value.getItem() == item) {
                return value;
            }
        }
        return null;
    }
}