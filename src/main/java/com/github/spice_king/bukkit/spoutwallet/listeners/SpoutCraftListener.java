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
package com.github.spice_king.bukkit.spoutwallet.listeners;

import com.github.spice_king.bukkit.spoutwallet.PlayerUpdateTask;
import com.github.spice_king.bukkit.spoutwallet.SpoutWallet;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.getspout.spoutapi.event.spout.SpoutCraftEnableEvent;

/**
 *
 * @author Kyle
 */
public class SpoutCraftListener implements Listener {

    SpoutWallet plugin;

    public SpoutCraftListener(SpoutWallet plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void spoutEnable(SpoutCraftEnableEvent event) {
        PlayerUpdateTask task = new PlayerUpdateTask(plugin, event.getPlayer());
        plugin.addPlayerUpdateTask(event.getPlayer().getName(), task);
    }
}
