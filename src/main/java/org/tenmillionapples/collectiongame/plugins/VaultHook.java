package org.tenmillionapples.collectiongame;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {
    private Economy economy;
    private final CollectionGame game;

    public VaultHook(CollectionGame game) {
        this.game = game;
    }

    public boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = game.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }

    public void addMoney(OfflinePlayer player, double amount) {
        EconomyResponse response = economy.depositPlayer(player, amount);
        if (player.isOnline()) {
            Player online = player.getPlayer();
            if (response.transactionSuccess()) {
                online.sendMessage(String.format(ChatColor.GREEN + "%s has been added to your account.", economy.format(response.amount)));
            } else {
                online.sendMessage(String.format(ChatColor.RED + "An error occurred: %s", response.errorMessage));
            }
        }
    }
}
