package net.Indyuce.inventory.manager;

import net.Indyuce.inventory.inventory.CustomInventoryHandler;
import net.Indyuce.inventory.inventory.InventoryHandler;
import org.apache.commons.lang3.Validate;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public abstract class DataManager {
    private final Map<UUID, InventoryHandler> inventories = new HashMap<>();

    /**
     * Function used to generate an inventoryProvider instance given a player.
     * There are two types of inventoryProviders: Complex, which are used when
     * the custom inventory is used, and Simple when the 'no-custom-inventory'
     * is toggled on
     */
    private Function<Player, InventoryHandler> load = (player -> new CustomInventoryHandler(player));

    /**
     * Used by the 'no-custom-inventory' config option
     *
     * @param load A function which takes a player as input and returns a new
     *             inventoryProvider corresponding to the player
     */
    public void setInventoryProvider(Function<Player, InventoryHandler> load) {
        Validate.notNull(load, "Function cannot be null");

        this.load = load;
    }

    public InventoryHandler getInventory(OfflinePlayer player) {
        return inventories.get(player.getUniqueId());
    }

    public InventoryHandler getInventory(UUID uuid) {
        return inventories.get(uuid);
    }

    /**
     * Loads/updates player data on login.
     */
    public void setupData(Player player) {
        inventories.computeIfAbsent(player.getUniqueId(), uuid -> load.apply(player));
    }

    /**
     * Saves and unloads player data on logout.
     */
    public void unloadData(OfflinePlayer player) {
        final InventoryHandler handler = getInventory(player);
        if (handler instanceof CustomInventoryHandler)
            save((CustomInventoryHandler) handler, false);

        inventories.remove(player.getUniqueId());
    }

    @Deprecated
    public void unloadData(UUID uuid) {
        inventories.remove(uuid);
    }

    public Collection<InventoryHandler> getLoaded() {
        return inventories.values();
    }

    /**
     * Called when the server stops/on autosave. This
     * saved all the data of online players.
     *
     * @param autosave If it's autosaving
     */
    public void save(boolean autosave) {
        for (InventoryHandler handler : getLoaded())
            if (handler instanceof CustomInventoryHandler)
                save((CustomInventoryHandler) handler, autosave);
    }

    /**
     * This only needs to do something when the custom inventory handler
     * is enabled. If items are stored in the player vanilla item, nothing
     * will be called.
     *
     * @param player Player data to save
     */
    @Deprecated
    public void save(OfflinePlayer player) {
        final InventoryHandler handler = getInventory(player);
        if (handler instanceof CustomInventoryHandler)
            save((CustomInventoryHandler) handler, false);
    }

    /**
     * Called either when the server stops, or when the 'save-on-log-off'
     * option is toggled on and the player leaves the server. This has
     * the effect of saving player data
     */
    public abstract void save(CustomInventoryHandler data, boolean autosave);

    /**
     * Called when a player logs on the server and his data has to be loaded.
     */
    public abstract void load(CustomInventoryHandler data);
}
