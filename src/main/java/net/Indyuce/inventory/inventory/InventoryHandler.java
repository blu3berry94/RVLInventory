package net.Indyuce.inventory.inventory;

import net.Indyuce.inventory.slot.CustomSlot;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class InventoryHandler {
    private final UUID uuid;

    /**
     * Player instance is not final because it needs to be updated every time
     * the player joins; this instance is used to equip vanilla items
     *
     * @deprecated Change implementation and add timeout before the player
     *         data is saved into the database.
     */
    @Deprecated
    protected final Player player;

    public InventoryHandler(Player player) {
        this.uuid = player.getUniqueId();
        this.player = player;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    @NotNull
    public Player getPlayer() {
        return Objects.requireNonNull(player, "Player is offline");
    }

    public void setItem(@NotNull CustomSlot slot, @Nullable ItemStack item) {
        if (slot.getType().isCustom())
            insertItemAtSlot(slot, item);
        else
            slot.getType().getVanillaSlotHandler().equip(player, item);
    }

    /**
     * @param lookupMode The way MMOInv collects and filters
     *                   items in the returned collection
     * @return The extra items from the player's custom inventory
     */
    public Collection<InventoryItem> getItems(InventoryLookupMode lookupMode) {
        Set<InventoryItem> items = new HashSet<>();
        for (InventoryItem invItem : retrieveItems())
            if (lookupMode == InventoryLookupMode.IGNORE_RESTRICTIONS || invItem.getSlot().checkSlotRestrictions(this, invItem.getItemStack()))
                items.add(invItem);
        return items;
    }

    /**
     * Puts an item in a specific slot index. This works for both
     * slots in the custom inventory GUI, and slot indexes from
     * the player's inventory as long as they are nicely configured.
     *
     * @param slot Target slot
     * @param item  Item to place
     */
    protected abstract void insertItemAtSlot(@NotNull CustomSlot slot, @Nullable ItemStack item);

    /**
     * @return A collection of all the extra items (vanilla slots put aside) ie
     *         accessories placed in custom RPG slots. This should include all items even
     *         the ones not usable by the player.
     */
    protected abstract Collection<InventoryItem> retrieveItems();
}
