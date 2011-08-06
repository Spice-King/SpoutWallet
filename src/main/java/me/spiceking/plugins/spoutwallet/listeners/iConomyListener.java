package me.spiceking.plugins.spoutwallet.listeners;

import com.iConomy.iConomy;
import me.spiceking.plugins.spoutwallet.SpoutWallet;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;

public class iConomyListener extends ServerListener {
    private SpoutWallet plugin;

    public iConomyListener(SpoutWallet plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginDisable(PluginDisableEvent event) {
        if (plugin.iConomy != null) {
            if (event.getPlugin().getDescription().getName().equals("iConomy")) {
                plugin.iConomy = null;
                plugin.RemoveScheduledTasks();
                System.out.println("[SpoutWallet] un-hooked from iConomy.");
            }
        }
    }

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        if (plugin.iConomy == null) {
            Plugin iConomy = plugin.getServer().getPluginManager().getPlugin("iConomy");

            if (iConomy != null) {
                if (iConomy.isEnabled() && iConomy.getClass().getName().equals("com.iConomy.iConomy")) {
                    plugin.iConomy = (iConomy)iConomy;
                    plugin.SetupScheduledTasks();
                    System.out.println("[SpoutWallet] hooked into iConomy.");
                }
            }
        }
    }
}
