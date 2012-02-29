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

package me.spiceking.plugins.spoutwallet;

import java.util.logging.Logger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import me.spiceking.plugins.spoutwallet.listeners.SpoutCraftListener;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.player.SpoutPlayer;
import org.getspout.spoutapi.gui.WidgetAnchor;
import org.getspout.spoutapi.gui.Color;

public class SpoutWallet extends JavaPlugin {
    
    public static Economy economy = null;
    
    public FileConfiguration config;
    
    private Set<SpoutPlayer> wallets = new HashSet<SpoutPlayer>();
    
    public Boolean showRank;
    public String fundsString;
    public String rankString;
    public Integer updateSpeed;
    public Integer ySetting;
    public Integer xSetting;
    public Boolean ignoreEssentials;
    
    public Integer colorFundsRed;
    public Integer colorFundsBlue;
    public Integer colorFundsGreen;
        
    public Integer colorRankRed;
    public Integer colorRankBlue;
    public Integer colorRankGreen;
    
    public Color colorFunds;
    public Color colorRank;

    public WidgetAnchor location;
    
    public PluginManager pluginManager = null;
    
    HashMap fundsLabels = new HashMap();
    HashMap rankLabels = new HashMap();
    
    
    @Override
    public void onDisable() {
        pluginManager = null;
        // Clean up Spout widgets
        RemoveScheduledTasks();
        for (Player player : getServer().getOnlinePlayers()) {
            SpoutPlayer sp = (SpoutPlayer) player;
            if (sp.isSpoutCraftEnabled())
                sp.getMainScreen().removeWidgets(this);
        }
        System.out.println(this + " is now disabled!");
    }

    @Override
    public void onEnable() {
        config = getConfig();
        fundsString = config.getString("Funds", "You have %s with you."); //String test = String.format("test goes here %s more text", "Testing");
        updateSpeed = config.getInt("UpdateSpeed", 20);
        ySetting = config.getInt("yOffset", 3);
        xSetting = config.getInt("xOffset", 3);
        ignoreEssentials = config.getBoolean("ignoreEssentials", false);
        
        if (updateSpeed < 20){
            updateSpeed = 20;
            config.set("UpdateSpeed", updateSpeed);
        }
        //Colors
        colorFundsRed = config.getInt("color.funds.red", 255);
        colorFundsBlue = config.getInt("color.funds.blue", 255);
        colorFundsGreen = config.getInt("color.funds.green", 255);
        
        if ((colorFundsRed > 255) || (colorFundsRed <= -1)){
            colorFundsRed = 255;
            config.set("color.funds.red", colorFundsRed);
        }
        if ((colorFundsBlue > 255) || (colorFundsBlue <= -1)){
            colorFundsBlue = 255;
            config.set("color.funds.blue", colorFundsBlue);
        }
        if ((colorFundsGreen > 255) || (colorFundsGreen <= -1)){
            colorFundsGreen = 255;
            config.set("color.funds.green", colorFundsGreen);
        }
        try {
            location = Enum.valueOf(WidgetAnchor.class, config.getString("location", "TOP_LEFT").toUpperCase(Locale.ENGLISH));
        }
        catch (java.lang.IllegalArgumentException e){
            System.out.print("[SpoutWallet] Oops, the location you want to start from is not a location Spout knows about.");
            System.out.print("[SpoutWallet] I'm going to change it back to TOP_LEFT");
            config.set("location", "TOP_LEFT");
            try {
                    location = WidgetAnchor.TOP_LEFT;
            } catch (java.lang.IllegalArgumentException a){
                    System.err.print("[SpoutWallet] Uh oh, Spout broke something! Tell Spice_King that WidgetAnchor enum has changed!");
            }
        }
        this.saveConfig(); //Save the config!
        // make the colors
        colorFunds = new Color(new Float(colorFundsRed)/255, new Float(colorFundsGreen)/255, new Float(colorFundsBlue)/255);
        
        Logger log = getServer().getLogger();
        getServer().getPluginManager().registerEvents(new SpoutCraftListener(this), this);
        SetupScheduledTasks();
        if (setupEconomy()){
            System.out.print("[SpoutWallet] Hooked Vault!");
        } else {
            System.out.print("[SpoutWallet] Oh this is bad. Vault has no economy for me!");
        }
        getCommand("wallet").setExecutor(new CommandExecutor() {
            public boolean onCommand(CommandSender cs, Command cmnd, String alias, String[] args) {
                if (args.length > 0) {
                    return false;
                }
                
                if (cs instanceof Player) {
                    SpoutPlayer sPlayer = (SpoutPlayer)cs;
                    if (!cs.hasPermission("spoutwallet.toggle")){
                        cs.sendMessage(ChatColor.RED + "You can't use this!");
                    } else {
                        if (sPlayer.isSpoutCraftEnabled()){
                            setWallet(sPlayer, !walletOn(sPlayer));
                        } else {
                            cs.sendMessage(ChatColor.RED + "You don't have SpoutCraft, so you can't use this command!");
                            cs.sendMessage(ChatColor.RED + "Install SpoutCraft from http://goo.gl/UbjS1");
                        }
                    }
                } else {
                    cs.sendMessage(ChatColor.RED + "You seem to be lacking a body to hold a wallet, so I can't tell you how full it is.");
                }
                
                return true;
            }
        });
        System.out.println("[SpoutWallet] is now enabled!");
    }
    
    public boolean walletOn(SpoutPlayer sPlayer) {
        return wallets.contains(sPlayer);
    }
    
    public void setWallet(SpoutPlayer sPlayer, boolean enabled) {
        if (enabled) {
            wallets.add(sPlayer);
        } else {
            wallets.remove(sPlayer);
        }
    }
    
    public HashMap getFundsLabels(){
        return fundsLabels;
    }
    
    public void SetupScheduledTasks() {
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                onSecond();
            }
        }, 50, 20);
    }
    
    public void RemoveScheduledTasks(){
        getServer().getScheduler().cancelTasks(this);
    }
    
    private void onSecond() {
        Player[] players = getServer().getOnlinePlayers();
        for (Player player : players){
            updateGUI(player);
            }
    }
    
    private void updateGUI(Player player) {
        
        SpoutPlayer sPlayer = (SpoutPlayer) player;
        
        if (!sPlayer.isSpoutCraftEnabled())
            return; //Don't crash me bro!
        
        UUID fundsLabelId = (UUID) getFundsLabels().get(player.getName());
        GenericLabel fundsLabel = (GenericLabel) sPlayer.getMainScreen().getWidget(fundsLabelId);
        
        String fundsText = null;
        
        if (!walletOn(sPlayer) || !sPlayer.hasPermission("spoutwallet.use")){
            //wallet is off or not allowed
            fundsLabel.setText("");
            fundsLabel.setDirty(true);
            return;
        }
        
        if (economy != null){
            /*if (!economy.hasAccount(player.getName()))
                return;*/
            /*System.out.print(economy.getBalance(player.getName()));
            System.out.print(economy.format(economy.getBalance(player.getName())));*/
            fundsText = String.format(fundsString, economy.format(economy.getBalance(player.getName())));
            fundsLabel.setText(fundsText);
            fundsLabel.setWidth(fundsLabel.getMinWidth()).setHeight(fundsLabel.getMinHeight());
            fundsLabel.setDirty(true);
            return;
        } else {
            
            fundsLabel.setText("Looks like a supported economy system is not installed or not working");
            fundsLabel.setDirty(true);
            return;
        }
    }
    
    private Boolean setupEconomy(){
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        
        return (economy != null);
    }
}
