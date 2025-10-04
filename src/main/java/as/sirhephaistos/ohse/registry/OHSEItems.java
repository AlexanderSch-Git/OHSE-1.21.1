package as.sirhephaistos.ohse.registry;

import as.sirhephaistos.ohse.world.item.ZoneWandItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Central registry holder for the mod's items.
 *
 * <p>This final utility class is responsible for creating and registering all item
 * instances belonging to the {@code ohse} mod and for placing them into the
 * appropriate creative item groups. Registration occurs when {@link #register()} is invoked,
 * typically during the mod initialization phase.</p>
 *
 * @since 1.0
 */
public final class OHSEItems {
    /**
     * The unique identifier for this mod used in registrations.
     */
    public static final String MOD_ID = "ohse";

    /**
     * The runtime reference to the zone wand item.
     *
     * <p>Assigned during {@link #register()}. This field remains {@code null}
     * until registration has occurred.</p>
     */
    public static Item ZONE_WAND;

    /**
     * Private constructor to prevent instantiation.
     *
     * <p>This class exposes only static members and must not be instantiated.</p>
     */
    private OHSEItems() {
    }

    /**
     * Register all items for this mod and perform any additional setup such as
     * adding the items to creative item groups.
     *
     * <p>Call this method from the mod initialization entry point so that items
     * are registered with the game registry and become available at runtime.</p>
     */
    public static void register() {
        ZONE_WAND = registerWand();
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
                .register(entries -> entries.add(ZONE_WAND));
    }

    /**
     * Create and register the zone wand item.
     *
     * <p>Constructs a new {@link ZoneWandItem} with the desired settings and
     * registers it in {@link Registries#ITEM} under the identifier
     * {@code ohse:zone_wand}.</p>
     *
     * @return the registered {@link Item} instance
     */
    private static Item registerWand() {
        Item item = new ZoneWandItem(new Item.Settings().maxCount(1));
        return Registry.register(
                Registries.ITEM,
                Identifier.of(MOD_ID, "zone_wand"),
                item
        );
    }
}