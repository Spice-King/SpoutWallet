package com.github.spice_king.bukkit.spoutwallet;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.Screen;
import org.getspout.spoutapi.player.SpoutPlayer;

public class PlayerUpdateTask implements Runnable {

    private SpoutWallet plugin;

    public PlayerUpdateTask(SpoutWallet plugin) {
        this.plugin = plugin;
    }

    public void run() {
        Player[] players = Bukkit.getServer().getOnlinePlayers();
        for (Player player : players) {
            updateGUI(player);
        }
    }

    private void updateGUI(Player player) {

        SpoutPlayer sPlayer = (SpoutPlayer) player;

        if (!sPlayer.isSpoutCraftEnabled()) {
            return; //Don't crash me bro!
        }

        UUID fundsLabelId = (UUID) plugin.getFundsLabels().get(player.getName());
        GenericLabel fundsLabel = (GenericLabel) sPlayer.getMainScreen().getWidget(fundsLabelId);

        // Make sure the player has his/her own label
        if (fundsLabelId == null || fundsLabel == null) {
            drawGUI(sPlayer, sPlayer.getMainScreen());
            return;  //Don't crash me bro! v.2.0
        }

        String fundsText;

        if (!plugin.walletOn(sPlayer) || !sPlayer.hasPermission("spoutwallet.use")) {
            //wallet is off or not allowed
            fundsLabel.setText("");
            fundsLabel.setDirty(true);
            return;
        }

        if (SpoutWallet.economy != null) {
            /*if (!economy.hasAccount(player.getName()))
             return;*/
            /*System.out.print(economy.getBalance(player.getName()));
             System.out.print(economy.format(economy.getBalance(player.getName())));*/
            fundsText = String.format(plugin.fundsString, SpoutWallet.economy.format(SpoutWallet.economy.getBalance(player.getName())));
            fundsLabel.setText(fundsText);
            fundsLabel.setWidth(fundsLabel.getMinWidth()).setHeight(fundsLabel.getMinHeight());
            fundsLabel.setDirty(true);
        } else {
            fundsLabel.setText("Looks like a supported economy system is not installed or not working");
            fundsLabel.setDirty(true);
        }
    }

    public void drawGUI(SpoutPlayer sp, Screen screen) {
        //Perms, yay!
        if (sp.hasPermission("SpoutWallet.Use")) {
            plugin.setWallet(sp, true);
        } else {
            plugin.setWallet(sp, false);
        }
        //This is the code to start the funds lable
        GenericLabel fundsLabel = new GenericLabel("");
        // Todo: fundsLable: config the location and colour
        fundsLabel.setTextColor(plugin.colorFunds).setAnchor(plugin.location);
        fundsLabel.setAlign(plugin.location);
        fundsLabel.setX(plugin.xSetting).setY(plugin.ySetting);
        fundsLabel.setHeight(0).setWidth(0);
        plugin.fundsLabels.put(sp.getName(), fundsLabel.getId());
        screen.attachWidget(plugin, fundsLabel);

        plugin.setWallet(sp, true);
    }
}
