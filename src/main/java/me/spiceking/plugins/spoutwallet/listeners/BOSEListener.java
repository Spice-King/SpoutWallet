/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.spiceking.plugins.spoutwallet.listeners;

import cosine.boseconomy.BOSEconomy;
import me.spiceking.plugins.spoutwallet.SpoutWallet;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Kyle
 */

public class BOSEListener extends ServerListener {
    private SpoutWallet plugin;

    public BOSEListener(SpoutWallet plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginDisable(PluginDisableEvent event) {
        if (plugin.BOSE != null) {
            if (event.getPlugin().getDescription().getName().equals("BOSEconomy")) {
                plugin.BOSE = null;
                System.out.println("[SpoutWallet] un-hooked from BOSEconomy.");
            }
        }
    }

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        if (plugin.BOSE == null) {
            Plugin temp = plugin.getServer().getPluginManager().getPlugin("BOSEconomy");

            if (temp != null) {
                plugin.BOSE = (BOSEconomy)temp;
                plugin.SetupScheduledTasks();
                System.out.println("[SpoutWallet] hooked into BOSEconomy.");
            }
        }
    }
}
