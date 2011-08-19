package me.spiceking.plugins.spoutwallet;

import com.iConomy.*;
import com.iConomy.system.Account;

import java.util.logging.Logger;
import java.util.HashMap;
import java.util.HashSet;
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

public class SpoutWallet extends JavaPlugin {
    
    public Configuration config;
    
    private Set<SpoutPlayer> wallets = new HashSet<SpoutPlayer>();
    
    public Boolean showRank;
    public String fundsString;
    public String rankString;
    public Integer updateSpeed;
    public Integer ySetting;
    public Boolean ignoreEssentials;
    
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
        ySetting = config.getInt("Height", 3);
        ignoreEssentials = config.getBoolean("ignoreEssentials", false);
        
        if (ySetting < 0){
            ySetting = 3;
            config.setProperty("Height", ySetting);
        }
        
        if (updateSpeed < 20){
            updateSpeed = 20;
            config.setProperty("UpdateSpeed", updateSpeed);
        }
        config.save(); //Save the config!
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
                    if (sPlayer.isSpoutCraftEnabled()){
                        setWallet(sPlayer, !walletOn(sPlayer));
                    } else {
                        cs.sendMessage(ChatColor.RED + "You don't have SpoutCraft, so you can't use this command!");
                        cs.sendMessage(ChatColor.RED + "Install SpoutCraft from http://goo.gl/UbjS1");
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
        
        if (!walletOn(sPlayer)){
            //wallet is off
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
            fundsLabel.setDirty(true);
            
            if (showRank && ("iConomy".equals(Method.getName())) && ("5".equals(Method.getVersion()))){
                iConomy = (iConomy) Method.getPlugin();
                Account account = iConomy.getAccount(player.getName());
                rankText =  String.format(rankString, account.getRank());
                rankLabel.setText(rankText);
                rankLabel.setDirty(true);
            }
            return;
        } else {
            
            fundsLabel.setText("Looks like a supported economy system is not installed or not working");
            fundsLabel.setDirty(true);
            if (showRank){
                rankLabel.setText("");
                rankLabel.setDirty(true);
            }
            return;
        }
    }
}
