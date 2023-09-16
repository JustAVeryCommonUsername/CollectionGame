package org.tenmillionapples.collectiongame;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.bukkit.ChatColor.RED;

public final class Util {
    private Util(){}

    private static final Set<Material> allValidItems = new HashSet<>();

    /**
     * Converts the material set into a list of the materials names, if the input is contained in the
     * material name or the input is empty (empty inputs are contained in everything)
     * @param input The input that is contained in the material names
     * @return A list of material names
     */
    public static List<String> getValidMatNames(Set<Material> mats, String input) {
        return mats.stream().filter(m ->
                        input.isEmpty() || m.name().contains(input.toUpperCase()))
                .map(m -> m.name().toLowerCase()).collect(Collectors.toList());
    }

    /**
     * @param item The item stack
     * @return the display name of the item, or its material name if it has none
     */
    public static String getItemName(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        String name = item.getType().name().toLowerCase();
        if (meta == null || meta.getDisplayName().isEmpty()) {
            return name;
        }
        return meta.getDisplayName();
    }

    /**
     * Creates a new item stack with the specified material and display name
     * @param mat The material to use
     * @param name The new display name
     * @return A new item stack
     */
    public static ItemStack createItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Gets a list of every material that is valid (doesn't include the illegal mats)
     * @return A new hash set with all the valid mats
     */
    public static Set<Material> allValidMats() {
        if (allValidItems.isEmpty()) {
            allValidItems.addAll(Arrays.stream(Material.values()).collect(Collectors.toSet()));
            allValidItems.removeAll(CollectionGame.illegalMats);
        }
        return new HashSet<>(allValidItems);
    }
}
