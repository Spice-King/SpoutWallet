package me.spiceking.plugins.spoutwallet;

import com.iConomy.*;
import com.iConomy.system.Account;

import java.util.logging.Logger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import me.spiceking.plugins.spoutwallet.listeners.registerListener;
import me.spiceking.plugins.spoutwallet.listeners.SpoutCraftListener;
import me.spiceking.plugins.spoutwallet.payment.Method;
import me.spiceking.plugins.spoutwallet.payment.Method.MethodAccount;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.config.Configuration;

import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.player.SpoutPlayer;
import org.getspout.spoutapi.gui.WidgetAnchor;
import org.getspout.spoutapi.gui.Color;

public class SpoutWallet extends JavaPlugin {
    
    public Configuration config;
    
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
    
    public iConomy iConomy = null;

    // This is public so we can
    public Method Method = null;
    
    HashMap fundsLabels = new HashMap();
    HashMap rankLabels = new HashMap();
    
    private final registerListener registerListener = new registerListener(this);
    
    public void onDisable() {
        // Clear economy stuff
        Method = null;
        pluginManager = null;
        iConomy = null;
        RemoveScheduledTasks();
        System.out.println(this + " is now disabled!");
    }

    public void onEnable() {
        config = getConfiguration();
        showRank = config.getBoolean("ShowRank", true);
        fundsString = config.getString("Funds", "You have %s with you."); //String test = String.format("test goes here %s more text", "Testing");
        rankString = config.getString("Rank", "Your rank is: #%s");
        updateSpeed = config.getInt("UpdateSpeed", 20);
        ySetting = config.getInt("yOffset", 3);
        xSetting = config.getInt("xOffset", 3);
        ignoreEssentials = config.getBoolean("ignoreEssentials", false);
        
        if (updateSpeed < 20){
            updateSpeed = 20;
            config.setProperty("UpdateSpeed", updateSpeed);
        }
        //Colors
        colorFundsRed = config.getInt("color.funds.red", 255);
        colorFundsBlue = config.getInt("color.funds.blue", 255);
        colorFundsGreen = config.getInt("color.funds.green", 255);
        
        colorRankRed = config.getInt("color.rank.red", 255);
        colorRankBlue = config.getInt("color.rank.blue", 255);
        colorRankGreen = config.getInt("color.rank.green", 255);
        
        if ((colorFundsRed > 255) || (colorFundsRed <= -1)){
            colorFundsRed = 255;
            config.setProperty("color.funds.red", colorFundsRed);
        }
        if ((colorFundsBlue > 255) || (colorFundsBlue <= -1)){
            colorFundsBlue = 255;
            config.setProperty("color.funds.blue", colorFundsBlue);
        }
        if ((colorFundsGreen > 255) || (colorFundsGreen <= -1)){
            colorFundsGreen = 255;
            config.setProperty("color.funds.green", colorFundsGreen);
        }
        if ((colorRankRed > 255) || (colorRankRed <= -1)){
            colorRankRed = 255;
            config.setProperty("color.rank.red", colorRankRed);
        }
        if ((colorRankBlue > 255) || (colorRankBlue <= -1)){
            colorRankBlue = 255;
            config.setProperty("color.rank.blue", colorRankBlue);
        }
        if ((colorRankGreen > 255) || (colorRankGreen <= -1)){
            colorRankGreen = 255;
            config.setProperty("color.rank.green", colorRankGreen);
        }
        try {
            location = Enum.valueOf(WidgetAnchor.class, config.getString("location", "TOP_LEFT").toUpperCase(Locale.ENGLISH));
        }
        catch (java.lang.IllegalArgumentException e){
            System.out.print("[SpoutWallet] Oops, the location you want to start from is not a location Spout knows about.");
            System.out.print("[SpoutWallet] I'm going to change it back to TOP_LEFT");
            config.setProperty("location", "TOP_LEFT");
        }
        config.save(); //Save the config!
        // make the colors
        colorFunds = new Color(new Float(colorFundsRed)/255, new Float(colorFundsGreen)/255, new Float(colorFundsBlue)/255);
        colorRank = new Color(new Float(colorRankRed)/255, new Float(colorRankGreen)/255, new Float(colorRankBlue)/255);
        
        Logger log = getServer().getLogger();
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Type.PLUGIN_ENABLE, registerListener, Priority.Monitor, this);
        pm.registerEvent(Type.PLUGIN_DISABLE, registerListener, Priority.Monitor, this);
        pm.registerEvent(Type.CUSTOM_EVENT, new SpoutCraftListener(this), Priority.Low, this);
        SetupScheduledTasks();
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
        System.out.println(this + " is now enabled!");
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
    
    public HashMap getRankLabels(){
        return rankLabels;
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
        
        GenericLabel rankLabel = null;
        UUID rankLabelId = (UUID) getRankLabels().get(player.getName());
        rankLabel = (GenericLabel) sPlayer.getMainScreen().getWidget(rankLabelId);
        
        String fundsText = null;
        String rankText = null;
        
        if (!walletOn(sPlayer) || !sPlayer.hasPermission("spoutwallet.use")){
            //wallet is off or not allowed
            fundsLabel.setText("");
            fundsLabel.setDirty(true);
            rankLabel.setText("");
            rankLabel.setDirty(true);
            return;
        }
        
        if (Method != null){
            if (!Method.hasAccount(player.getName()))
                return;
            MethodAccount balance = Method.getAccount(player.getName());
            fundsText = String.format(fundsString, Method.format(balance.balance()));
            fundsLabel.setText(fundsText);
            if (location==WidgetAnchor.CENTER_CENTER || location==WidgetAnchor.BOTTOM_CENTER || location==WidgetAnchor.TOP_CENTER){
                fundsLabel.setX(xSetting - (fundsLabel.getWidth()/2));
            } else if (location==WidgetAnchor.BOTTOM_RIGHT || location==WidgetAnchor.CENTER_RIGHT || location==WidgetAnchor.TOP_RIGHT){
                fundsLabel.setX(xSetting - fundsLabel.getWidth());
            }
            fundsLabel.setDirty(true);
            
            if (showRank && ("iConomy".equals(Method.getName())) && ("5".equals(Method.getVersion()))){
                iConomy = (iConomy) Method.getPlugin();
                Account account = iConomy.getAccount(player.getName());
                rankText =  String.format(rankString, account.getRank());
                rankLabel.setText(rankText);
                if (location==WidgetAnchor.CENTER_CENTER || location==WidgetAnchor.BOTTOM_CENTER || location==WidgetAnchor.TOP_CENTER){

                } else if (location==WidgetAnchor.BOTTOM_RIGHT || location==WidgetAnchor.CENTER_RIGHT || location==WidgetAnchor.TOP_RIGHT){
                    
                }
                rankLabel.setDirty(true);
            }
            return;
        } else {
            
            fundsLabel.setText("Looks like a supported economy system is not installed or not working");
            if (location==WidgetAnchor.CENTER_CENTER || location==WidgetAnchor.BOTTOM_CENTER || location==WidgetAnchor.TOP_CENTER){
                fundsLabel.setX(xSetting - (fundsLabel.getWidth()/2));
            } else if (location==WidgetAnchor.BOTTOM_RIGHT || location==WidgetAnchor.CENTER_RIGHT || location==WidgetAnchor.TOP_RIGHT){
                fundsLabel.setX(xSetting - fundsLabel.getWidth());
            }
            fundsLabel.setDirty(true);
            if (showRank){
                rankLabel.setText("");
                rankLabel.setDirty(true);
            }
            return;
        }
    }
}
