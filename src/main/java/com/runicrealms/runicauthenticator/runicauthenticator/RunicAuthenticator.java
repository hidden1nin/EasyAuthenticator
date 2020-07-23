package com.runicrealms.runicauthenticator.runicauthenticator;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public final class RunicAuthenticator extends JavaPlugin implements Listener {

        private HashMap<UUID,GoogleAuthenticatorKey> authlockednew= new HashMap<UUID,GoogleAuthenticatorKey>();
    private ArrayList<UUID> authlocked= new ArrayList<UUID>();
        @Override
        public void onEnable() {
            this.getServer().getPluginManager().registerEvents(this, this);

            this.getConfig().options().copyDefaults(true);
            this.saveConfig();
        }


        @EventHandler
        public void onJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();

            if (!this.getConfig().contains("authcodes." + player.getUniqueId())&&player.hasPermission("RunicAuthenticator.required")) {
                GoogleAuthenticator gAuth = new GoogleAuthenticator();
                GoogleAuthenticatorKey key = gAuth.createCredentials();

                player.sendMessage("§7Your §bGoogle Auth Code §7is §a" + key.getKey());
                player.sendMessage(ChatColor.GREEN+"Since You have so much power we need to enable 2 Factor Authentication");
                //player.sendMessage(url);
                //Bukkit.broadcastMessage(url);
                player.sendMessage("§7You must enter this code in the Google Authenticator App before leaving the server.");
                String url =KeyHandler.getQRBarcodeURL(player.getName(),"Runic Realms",key.getKey());
                ComponentBuilder message = new ComponentBuilder(ChatColor.BLUE+""+ChatColor.BOLD+"Click to Copy Link to QR code");
                BaseComponent[] msg = message.event(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, url)).create();
                player.spigot().sendMessage(msg);
                player.sendMessage("§7Then use the code you get in chat to finish this process and log in");
                authlocked.add(player.getUniqueId());
                authlockednew.put(player.getUniqueId(),key);
            } else {
                authlocked.add(player.getUniqueId());
                player.sendMessage("§cPlease open the Google Authenticator App and provide the six digit code in chat");
            }
        }
    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
    }
        private boolean playerInputCode(Player player, int code) {
            String secretkey = this.getConfig().getString("authcodes." + player.getUniqueId());

            GoogleAuthenticator gAuth = new GoogleAuthenticator();
            boolean codeisvalid = gAuth.authorize(secretkey, code);


            if (codeisvalid) {
                authlocked.remove(player.getUniqueId());
                return codeisvalid;
            }

            return codeisvalid;
        }
    private boolean playerInputCodeNew(Player player, int code,String secretkey) {
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        boolean codeisvalid = gAuth.authorize(secretkey, code);


        if (codeisvalid) {
            authlocked.remove(player.getUniqueId());
            return codeisvalid;
        }

        return codeisvalid;
    }

        @EventHandler
        public void chat(AsyncPlayerChatEvent event) {
            Player player = event.getPlayer();
            String message = event.getMessage();
            if(authlockednew.containsKey(player.getUniqueId())){
                try {
                    Integer code = Integer.parseInt(message);
                    if (playerInputCodeNew(player, code,authlockednew.get(player.getUniqueId()).getKey())) {
                        authlocked.remove(player.getUniqueId());
                        player.sendMessage(ChatColor.DARK_GREEN +"*Access Granted* §bWelcome to the server!");
                        if(!this.getConfig().contains("authcodes." + player.getUniqueId())){
                            player.sendMessage(ChatColor.GREEN+"Sorry for the hassle!");
                            this.getConfig().set("authcodes." + player.getUniqueId(), authlockednew.get(player.getUniqueId()).getKey());
                            this.saveConfig();
                            authlockednew.remove(player.getUniqueId());
                            event.setCancelled(true);
                            return;
                        }
                    } else {
                        player.sendMessage(ChatColor.RED+"Incorrect or expired code ** A code will only contain numbers **");
                    }
            }catch (Exception e) {
                    player.sendMessage(ChatColor.RED+"Incorrect or expired code ** A code will only contain numbers **");
                }
                event.setCancelled(true);
                return;
            }
            if (authlocked.contains(player.getUniqueId())) {
                try {
                    Integer code = Integer.parseInt(message);
                    if (playerInputCode(player, code)) {
                        authlocked.remove(player.getUniqueId());
                        player.sendMessage(ChatColor.DARK_GREEN +"*Access Granted* §bWelcome to the server!");
                    } else {
                        player.sendMessage(ChatColor.RED+"Incorrect or expired code ** A code will only contain numbers **");
                    }
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED+"Incorrect or expired code ** A code will only contain numbers **");
                }
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void command(PlayerCommandPreprocessEvent event) {
            Player player = event.getPlayer();
            if (authlocked.contains(player.getUniqueId())) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED+"Ah Ah Ah, you didnt say the magic word!");
            }
        }

        @EventHandler
        public void blockbreak(BlockBreakEvent event) {
            Player player = event.getPlayer();
            if (authlocked.contains(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void blockplace(BlockPlaceEvent event) {
            Player player = event.getPlayer();
            if (authlocked.contains(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
}
