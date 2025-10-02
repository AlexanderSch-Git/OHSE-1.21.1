package as.sirhephaistos.ohse.registry;


import as.sirhephaistos.ohse.world.item.ZoneWandItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Item registry for OHSE.
 * Keeps static references to mod items.
 */
public final class OHSEItems {
    public static final String MOD_ID = "ohse";

    public static Item ZONE_WAND;

    private OHSEItems() {
    }

    public static void register() {
        ZONE_WAND = registerWand();

        // Creative tab (dev)
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
                .register(entries -> entries.add(ZONE_WAND));
    }

    private static Item registerWand() {
        // 1. Create the item normally
        Item item = new ZoneWandItem(new Item.Settings().maxCount(1));
        // 2. Register with an Identifier (NOT RegistryKey)
        return Registry.register(
                Registries.ITEM,
                Identifier.of(MOD_ID, "zone_wand"),
                item
        );
    }
}