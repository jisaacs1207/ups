package io.github.jisaacs1207.ups;

import java.util.ArrayList;
import java.util.List;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public final class ups extends JavaPlugin{
	public static Economy econ = null;
	
	@Override
	public void onEnable() {
		getLogger().info("UPS has been hooked!");
		saveDefaultConfig();
		setupEconomy();
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
            	for(Player p : Bukkit.getOnlinePlayers()){
            		Object test = getConfig().get(p.getName().toLowerCase());
            		if (test != null) {
            			List<String> itemList = getConfig().getStringList(p.getName().toLowerCase());
        				int listLength = itemList.size();
        				if (listLength==1){
        					p.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " +ChatColor.YELLOW + "You've got " + listLength + " package waiting for pickup.");
                			p.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " + ChatColor.YELLOW + "Collect it by typing '/receive' now.");
        				}
        				else{
        					p.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " +ChatColor.YELLOW + "You've got " + listLength + " packages waiting for pickup.");
                			p.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " + ChatColor.YELLOW + "Collect one of them by typing '/receive' now.");
        				}
        				p.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " + ChatColor.RED + "Unclaimed items vaporize every hour, on the hour!");
            		} 
            	}
            }
        }, 0L, 3000L);
	}
 
	@Override
	public void onDisable() {
		getLogger().info("UPS has been unhooked!");
	}
	
	private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            econ = economyProvider.getProvider();
        }

        return (econ != null);
    }
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		this.reloadConfig();
		if (args.length == 0) {
			if (commandLabel.equalsIgnoreCase("ups")){
				Player player = (Player)sender;
				player.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " +ChatColor.YELLOW + "Try /send or /receive!");
				return true;
			}
			else if (commandLabel.equalsIgnoreCase("send")){
				Player player = (Player)sender;
				int passthrough = 0;
				if (player.getInventory().getItemInHand().getTypeId() == 0){
					player.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " + ChatColor.YELLOW + "Air is too difficult to package! Hold something.");
					return true;
				}
				if(!player.hasPermission("ups.free")){
					for(String key : this.getConfig().getConfigurationSection("costPerSpecific").getKeys(true)) {
						if(player.getInventory().getItemInHand().getTypeId()==Integer.valueOf(key)){
							if(!econ.has(player.getName(), this.getConfig().getDouble("costPerSpecific." + key))){
								player.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " +ChatColor.YELLOW + "You don't have enough money to send that.");
								return true;
							}
							econ.withdrawPlayer(player.getName(), this.getConfig().getDouble("costPerSpecific." + key));
							passthrough=1;
						}
					}
					if(passthrough==0){
						if(!econ.has(player.getName(), this.getConfig().getDouble("costPerPackage"))){
							player.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " +ChatColor.YELLOW + "You don't have enough money to send that.");
							return true;
						}
						econ.withdrawPlayer(player.getName(), this.getConfig().getDouble("costPerPackage"));
					}
				}
				String name = player.getName().toLowerCase();
				String itemString = ItemStackUtils.deserialize(player.getInventory().getItemInHand());
				ArrayList<String> list = (ArrayList<String>) this.getConfig().getStringList(name);
				list.add(itemString);
				this.getConfig().set(name, list);
				saveConfig();
				player.getInventory().setItemInHand(null);
				return true;
			}
			else if (commandLabel.equalsIgnoreCase("receive")){
				Player player = (Player)sender;
				String name = player.getName().toLowerCase();				
				if(!getConfig().contains(name)){
					player.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " +ChatColor.YELLOW + "You haven't anything to receive!");
					return true;
				}
				int count = 0;
				for (int i = 0; i < player.getInventory().getContents().length; i++) {
				    if (player.getInventory().getContents()[i] == null)
				        count++;
				    
				}
				if(count==0){
					player.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " +ChatColor.YELLOW + "You haven't enough space in your inventory!");
					return true;
				}
				List<String> itemList = getConfig().getStringList(name);
				String itemString = itemList.get(0);
				itemList.remove(0);
				if(itemList.isEmpty()){
					getConfig().set(name, null);
				}
				else{
					getConfig().set(name, itemList);
				}	
				saveConfig();
				ItemStack rItem = ItemStackUtils.deserial(itemString);
				player.getInventory().addItem(new ItemStack(rItem));
				return true;
			}
		}
		if (args.length == 1) {
			if (commandLabel.equalsIgnoreCase("ups")){
				if(args[0].equalsIgnoreCase("check")){
					Player player = (Player)sender;
					int passthrough=0;
					for(String key : this.getConfig().getConfigurationSection("costPerSpecific").getKeys(true)) {
						if(player.getInventory().getItemInHand().getTypeId()==Integer.valueOf(key)){
							int price = this.getConfig().getInt("costPerSpecific." + key);
							sender.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " +ChatColor.YELLOW + "The price to send that item is $" + price +".");
							passthrough=1;
						}
					}
					if(passthrough==0){
						int price = this.getConfig().getInt("costPerPackage");
						sender.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " +ChatColor.YELLOW + "The price to send that item is $" + price +".");
					}
				}
				if(args[0].equalsIgnoreCase("reset")){
					if(sender.isOp()){
						saveConfig();
						for(String key : getConfig().getKeys(false)){
							if((!key.equalsIgnoreCase("costPerPackage")&&!key.equalsIgnoreCase("costForUPSPrime")&&!key.equalsIgnoreCase("playerPackages")&&!key.equalsIgnoreCase("costPerSpecific"))){
								getConfig().set(key,null);
							}
		            	}
		            	saveConfig();
		            	return true;
					}
				}	
			}
			if (commandLabel.equalsIgnoreCase("send")){
				int pOnline = 0;
				for(Player p : Bukkit.getOnlinePlayers()){
					String receiver = p.getName();
					if(args[0].equalsIgnoreCase(receiver)){
						pOnline = 1;
					}
				}
				if(pOnline==1){
					Player player = (Player)sender;
					player.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " +ChatColor.YELLOW + "You are about to send your held item(s) to " + args[0] + ".");
					player.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " +ChatColor.YELLOW + "If you are sure you've got the name correct, type:");
					player.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " +ChatColor.LIGHT_PURPLE + "/send " + args[0] + " confirm");
					return true;
				}
				else if(pOnline==0){
					Player player = (Player)sender;
					player.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " +ChatColor.YELLOW + args[0] + " is offline or doesn't exist.");
					player.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " +ChatColor.YELLOW + "We can deliver to an offline player but if");
					player.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " +ChatColor.YELLOW + "you've misspelled the name, the item is lost!");
					player.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " +ChatColor.LIGHT_PURPLE + "To continue, type '/send " + args[0] + " offline'.");
					return true;
				}
			}
			else if(commandLabel.equalsIgnoreCase("receive")){
				Player player = (Player)sender;
				String name = player.getName().toLowerCase();
				List<String> itemList = getConfig().getStringList(name);
				int listLength = itemList.size();
				if(!isInteger(args[0])){
					player.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " +ChatColor.YELLOW + "Correct Syntax:");
					player.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " +ChatColor.YELLOW + "/receive [number]");
					return true;
				}
				int argInt = Integer.valueOf(args[0]);
				if(listLength < argInt){
					player.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " +ChatColor.YELLOW + "You only have " + listLength + " items available!");
					return true;
				}
				int count = 0;
				for (int i = 0; i < player.getInventory().getContents().length; i++) {
				    if (player.getInventory().getContents()[i] == null)
				        count++;
				}
				if(count<argInt){
					player.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " +ChatColor.YELLOW + "You haven't enough space in your inventory!");
					return true;
				}
				for(int i=1; i<argInt+1; i++){
					itemList = getConfig().getStringList(name);
					String itemString = itemList.get(0);
					itemList.remove(0);
					if(itemList.isEmpty()){
						getConfig().set(name, null);
					}
					else{
						getConfig().set(name, itemList);
					}	
					saveConfig();
					ItemStack rItem = ItemStackUtils.deserial(itemString);
					player.getInventory().addItem(new ItemStack(rItem));
					
		         }
				 return true;
			}
		}
		if (args.length == 2) {
			if (commandLabel.equalsIgnoreCase("send")){
				if(args[1].equalsIgnoreCase("confirm")){
					Player player = (Player)sender;
					if (player.getInventory().getItemInHand().getTypeId() == 0){
						player.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " + ChatColor.YELLOW + "Air is too difficult to package! Hold something.");
						return true;
					}
					String name = player.getName();
					String rName = args[0].toLowerCase();
					int pOnline = 0; //This is a boolean. It didn't work, and I am too lazy to figure out why... hence the int.
					for(Player p : Bukkit.getOnlinePlayers()){
						String receiver = p.getName();
						if(args[0].equalsIgnoreCase(receiver)){
							p.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " + ChatColor.YELLOW + name + " has sent you something via " + ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "]!");
							p.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " + ChatColor.YELLOW + "Collect it with by typing '/receive' now.");
							p.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " + ChatColor.RED + "Unclaimed items vaporize every hour, on the hour!");
							pOnline = 1;
						}
					}
					if(pOnline == 0){ 
						player.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " +ChatColor.YELLOW + args[0] + " is offline or doesn't exist.");
						player.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " +ChatColor.YELLOW + "We can deliver to an offline player but if");
						player.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " +ChatColor.YELLOW + "you've misspelled the name, the item is lost!");
						player.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " +ChatColor.LIGHT_PURPLE + "To continue, type '/send " + args[0] + " offline'.");
						return true;
					}
					String itemString = ItemStackUtils.deserialize(player.getInventory().getItemInHand());
					ArrayList<String> list = (ArrayList<String>) this.getConfig().getStringList(rName);
					list.add(itemString);
					this.getConfig().set(rName, list);
					saveConfig();
					player.getInventory().setItemInHand(null);
					return true;
				}
				if(args[1].equalsIgnoreCase("offline")){
					String rName = args[0].toLowerCase();
					Player player = (Player)sender;
					int passthrough=0;
					if(!player.hasPermission("ups.free")){
						for(String key : this.getConfig().getConfigurationSection("costPerSpecific").getKeys(true)) {
							if(player.getInventory().getItemInHand().getTypeId()==Integer.valueOf(key)){
								if(!econ.has(player.getName(), this.getConfig().getDouble("costPerSpecific." + key))){
									player.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " +ChatColor.YELLOW + "You don't have enough money to send that.");
									return true;
								}
								econ.withdrawPlayer(player.getName(), this.getConfig().getDouble("costPerSpecific." + key));
								passthrough=1;
							}
						}
						if(passthrough==0){
							if(!econ.has(player.getName(), this.getConfig().getDouble("costPerPackage"))){
								player.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "UPS" + ChatColor.GOLD + "] " +ChatColor.YELLOW + "You don't have enough money to send that.");
								return true;
							}
							econ.withdrawPlayer(player.getName(), this.getConfig().getDouble("costPerPackage"));
						}
					}
					String itemString = ItemStackUtils.deserialize(player.getInventory().getItemInHand());
					ArrayList<String> list = (ArrayList<String>) this.getConfig().getStringList(rName);
					list.add(itemString);
					this.getConfig().set(rName, list);
					saveConfig();
					player.getInventory().setItemInHand(null);
				}
			}
		}
		return false;
	}
	public static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    return true;
	}
}

