package fr.black_eyes.guildObjectives.listeners;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import fr.black_eyes.guildObjectives.Main;
import fr.black_eyes.guildObjectives.Objective;
import fr.black_eyes.guildObjectives.Utils;
import fr.black_eyes.guildObjectives.commands.contract;
import me.glaremasters.guilds.guild.Guild;

public class InventoryListeners implements Listener {
	

	@EventHandler
    public void onClose(final InventoryCloseEvent e) {
		if (e.getInventory() == null) {
            return;
        }

        final Player player =(Player)e.getPlayer();

        if(contract.menuName.containsKey(player)) {
        	contract.menuName.remove(player);
        }
	}
	
	//gère le menu de création du coffre
    @SuppressWarnings("unchecked")
	@EventHandler
    public void onClick(final InventoryClickEvent e) {
        if (e.getInventory() == null) {
            return;
        }
        if (e.getWhoClicked() == null) {
            return;
        }
        if (e.getCurrentItem() == null) {
            return;
        }
        if (!(e.getWhoClicked() instanceof Player)) {
        	return;
        }

        final Player player =(Player)e.getWhoClicked();

        if(!contract.menuName.containsKey(player)) {
        	return;
        }
        if (contract.menuName.get(player).equals("ContratsDuJour")) {
        	e.setCancelled(true);
    		for(int i = 0, j=-1; i<54 ; i++) {
    			if(Main.objectifs[i] != null) {
    				j++;
    				if(j == e.getSlot()) {
    					Objective ob = Main.objectifs[j];
    					Guild g = Main.api.getGuild(player);
    					String id = g.getId().toString();
    					if(ob.joursFin != -1) {
	    					if(g.getBalance() >= ob.cout) {
		    					Utils.setObjective(ob, "takenObjectives."+id+".objective");
		    					Objective ob2 = new Objective();
		    					ob2.cout = ob.cout;
		    					ob2.recompense = ob.recompense;
		    					ob2.commandes = ob.commandes;
		    					ob2.joursFin = ob.joursFin;
		    					ob2.joursPenalite = ob.joursPenalite;
		    					ob2.materiaux = ob.materiaux;
		    					ob2.penalite = ob.penalite;
		    					Main.takenObjectives.put(ob2, id);
		    					player.closeInventory();
		    					Utils.msg(player, "tookObjective", " ", " ");
		    					g.setBalance(g.getBalance()-ob.cout);
		    					ob.joursFin = -1;
		    					Main.getInstance().getData().set("dailyObjectives." + j + ".joursFin", -1);
		    					Main.getInstance().getData().set("takenObjectives."+id+".dejaFait", true);
	    					}else {
		    					Utils.msg(player, "guildNeedsMoney", "[Money]", ob.cout-g.getBalance()+"");	
		    				}
	    					player.closeInventory();
	    					contract.menuName.remove(player);
    					}
    				}
    			}
    		}
    		try {
    			Main.getInstance().getData().save(Main.getInstance().getDataF());
    			Main.getInstance().getData().load(Main.getInstance().getDataF());
    		} catch (IOException | InvalidConfigurationException e1) {
    			e1.printStackTrace();
    		}
    		
        }
        
        //copy menu
        else if (contract.menuName.get(player).equals("Objectif")) {
        	e.setCancelled(true);
        	if(e.getSlot() != 4) {
        		return;
        	}
        	Guild g = Main.api.getGuild(player);
        	Objective ob = (Objective)Utils.getKey(Main.takenObjectives, g.getId().toString());
        	HashMap<Material, Integer> materiaux = new HashMap<Material, Integer>();
        	materiaux = (HashMap<Material, Integer>) ob.materiaux.clone();
    		for (Entry<Material, Integer> entry : materiaux.entrySet()) {
    			entry.setValue(entry.getValue()*64);
    		}
			int vaults = g.getTier().getVaultAmount();
			if(vaults <= 0) {
				player.sendMessage("§cVeuillez ouvrir votre coffre de guilde au moins une fois pour faire cela (/guild vault)");
				player.closeInventory();
				contract.menuName.remove(player);
				return;
			}

			//for all guild vaults
        	for(int i=1; i<vaults+1; i++) {
        		
        		ItemStack[] inv = null;
        		try{
        			inv = Main.api.getGuildVault(g, i).getContents();
        		}catch(IndexOutOfBoundsException e1) {
        			player.sendMessage("§cVeuillez ouvrir votre coffre de guilde au moins une fois pour faire cela (/guild vault)");
        			player.closeInventory();
    				contract.menuName.remove(player);
    				return;
        		}
        		//check all required items and count them
        		for (Entry<Material, Integer> entry : materiaux.entrySet()) {
        			Material mat = entry.getKey();
        	        int number = Utils.itemAmount(inv, mat);
        	        entry.setValue(entry.getValue()-number);
        		}
        	}
        		
    		boolean hasenoughitems = true;
    		String itemsStillRequired = "";
    		for (Entry<Material, Integer> entry : materiaux.entrySet()) {
    			if(entry.getValue()>0) {
    				hasenoughitems = false;
    				itemsStillRequired += "; "+entry.getValue() + " " + entry.getKey().toString();
    			}
    		}
    		if(hasenoughitems) {
    			materiaux = (HashMap<Material, Integer>) ob.materiaux.clone();
    			for (Entry<Material, Integer> entry : materiaux.entrySet()) {
        			entry.setValue(entry.getValue()*64);
        		}
    			for(int i=1; i<vaults+1; i++) {
            		Inventory inv = Main.api.getGuildVault(g, i);
            		//check all required items and count them
            		for (Entry<Material, Integer> entry : materiaux.entrySet()) {
            			Material mat = entry.getKey();
            	        int number = Utils.itemAmount(inv.getContents(), mat);
            	        if(entry.getValue()>0) {
	            	        if(entry.getValue() >= number) {
	            	        	inv.remove(mat);
	            	        }
	            	        else {
	            	        	inv.removeItem(new ItemStack(mat, entry.getValue()));
	            	        }
	            	        entry.setValue(entry.getValue()-number);
            	        }
            		}
            	}
    			g.setBalance(g.getBalance()+ob.recompense);
    			for(String command : ob.commandes) {
    				command = command.replace("[Guild]", g.getName());
    				command = command.replace("[Player]", player.getName());
    				Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
    			}
    			Utils.msg(player, "objectiveCompleted", "[Recompense]", ob.recompense.toString());
    			Main.takenObjectives.remove(ob);
   				Main.getInstance().getData().set("takenObjectives."+g.getId().toString()+".objective", null);

        		try {
        			Main.getInstance().getData().save(Main.getInstance().getDataF());
        			Main.getInstance().getData().load(Main.getInstance().getDataF());
        		} catch (IOException | InvalidConfigurationException e1) {
        			e1.printStackTrace();
        		}
    		}
    		else {
    			Utils.msg(player, "youLackSomeItems", "[Items]", itemsStillRequired);
        	}
    		
    		player.closeInventory();
    		contract.menuName.remove(player);

        }
        //main menu
       

    }
	
	
	
	
	
}
