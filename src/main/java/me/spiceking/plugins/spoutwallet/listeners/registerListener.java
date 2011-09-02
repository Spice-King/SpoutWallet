/*
 * This file is part of SpoutWallet.
 * 
 * SpoutWallet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 
 * SpoutWallet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with SpoutWallet.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.spiceking.plugins.spoutwallet.listeners;

/**
 *
 * @author Kyle
 */

// Imports for SpoutWallet
import me.spiceking.plugins.spoutwallet.payment.Methods;
import me.spiceking.plugins.spoutwallet.SpoutWallet;

// Bukkit Imports
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;

public class registerListener extends ServerListener {
    
    private SpoutWallet plugin;
    private Methods Methods = null;

    public registerListener(SpoutWallet plugin) {
        this.plugin = plugin;
        this.Methods = new Methods();
    }

    @Override
    public void onPluginDisable(PluginDisableEvent event) {
        // Check to see if the plugin thats being disabled is the one we are using
        if (this.Methods != null && this.Methods.hasMethod()) {
            Boolean check = this.Methods.checkDisabled(event.getPlugin());

            if(check) {
                this.plugin.Method = null;
                this.plugin.iConomy = null;
                System.out.println("[" + plugin.getDescription().getName() + "] Payment method was disabled. No longer showing balance.");
            }
        }
    }

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        // Ignore Essentials if we want to!
        if ("Essentials".equals(event.getPlugin().getDescription().getName()) && plugin.ignoreEssentials){
            System.out.print("[" + plugin.getDescription().getName() + "] Almost loaded Essentials, but stopped because you want me to ignore Essentials!");
            return;
        }
        // Check to see if we need a payment method
        if (!this.Methods.hasMethod()) {
            if(this.Methods.setMethod(event.getPlugin())) {
                this.plugin.Method = this.Methods.getMethod();
                System.out.println("[" + plugin.getDescription().getName() + "] Payment method found (" + this.plugin.Method.getName() + " version: " + this.plugin.Method.getVersion() + ")");
            }
        }
    }
}
