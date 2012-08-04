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
package com.github.spice_king.bukkit.spoutwallet;

import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.Screen;
import org.getspout.spoutapi.player.SpoutPlayer;

public class PlayerUpdateTask implements Runnable {

    private SpoutWallet plugin;
    private SpoutPlayer player;
    private GenericLabel label;

    public PlayerUpdateTask(SpoutWallet plugin, SpoutPlayer player) {
        this.plugin = plugin;
        this.player = player;
        drawGUI(player, player.getMainScreen());
    }

    public void run() {
        updateGUI(player);
    }

    private void updateGUI(SpoutPlayer player) {
        
        String fundsText;

        if (!plugin.walletOn(player) || !player.hasPermission("spoutwallet.use")) {
            //wallet is off or not allowed
            label.setText("");
            label.setDirty(true);
            return;
        }

        if (SpoutWallet.economy != null) {
            fundsText = String.format(plugin.fundsString, SpoutWallet.economy.format(SpoutWallet.economy.getBalance(player.getName())));
            label.setText(fundsText);
            label.setWidth(label.getMinWidth()).setHeight(label.getMinHeight());
            label.setDirty(true);
        } else {
            label.setText("Looks like a supported economy system is not installed or not working");
            label.setDirty(true);
        }
    }

    private void drawGUI(SpoutPlayer sp, Screen screen) {
        //Perms, yay!
        if (sp.hasPermission("SpoutWallet.Use")) {
            plugin.setWallet(sp, true);
        } else {
            plugin.setWallet(sp, false);
        }
        //This is the code to start the funds lable
        label = new GenericLabel("");
        // Todo: fundsLable: config the location and colour
        label.setTextColor(plugin.colorFunds).setAnchor(plugin.location);
        label.setAlign(plugin.location);
        label.setX(plugin.xSetting).setY(plugin.ySetting);
        label.setHeight(0).setWidth(0);
        screen.attachWidget(plugin, label);

        plugin.setWallet(sp, true);
    }
}
