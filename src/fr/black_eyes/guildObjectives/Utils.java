package fr.black_eyes.guildObjectives;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import fr.black_eyes.guildObjectives.commands.contract;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;

public class Utils implements Listener {

	//message functions that automaticly get a message from language file
	public static void msg(CommandSender p, String path, String replacer, String replacement) {
		p.sendMessage(getMsg(path, replacer, replacement));
	}
	
	public static String getMsg(String path, String replacer, String replacement) {
		return Main.getInstance().getLang().getString(path).replace(replacer, replacement).replace("&", "§");
	}
	
	
	//function to copy a chest
	//fonction pour copier un coffre
	public static void loadObjectives(String path){
		FileConfiguration data = Main.getInstance().getData();
		for(int i=0; data.isSet(path+i+".cout"); i++) {
			Main.objectifs[i] = new Objective();
			Main.objectifs[i].joursFin = data.getInt(path+i+".joursFin");
			Main.objectifs[i].cout = data.getInt(path+i+".cout");
			Main.objectifs[i].recompense = data.getInt(path+i+".recompense");
			Main.objectifs[i].joursPenalite = data.getInt(path+i+".joursPenalite");
			Main.objectifs[i].penalite = data.getInt(path+i+".penalite");
			for(String materiaux : data.getConfigurationSection(path+i+".materiaux").getKeys(false)) {
				Main.objectifs[i].materiaux.put(Material.valueOf(materiaux),data.getInt(path+i+".materiaux."+materiaux));
			}
			for(String commandes : data.getStringList(path+i+".commandes")) {
				Main.objectifs[i].commandes.add(commandes);
			}
			
		}
	}
	
	public static Objective getTakenObjective(String path){
		FileConfiguration data = Main.getInstance().getData();
		Objective objectif = new Objective();
		objectif.joursFin = data.getInt(path+".joursFin");
		objectif.cout = data.getInt(path+".cout");
		objectif.recompense = data.getInt(path+".recompense");
		objectif.joursPenalite = data.getInt(path+".joursPenalite");
		objectif.penalite = data.getInt(path+".penalite");
		for(String materiaux : data.getConfigurationSection(path+".materiaux").getKeys(false)) {
			objectif.materiaux.put(Material.valueOf(materiaux),data.getInt(path+".materiaux."+materiaux));
		}
		for(String commandes : data.getStringList(path+".commandes")) {
			objectif.commandes.add(commandes);
		}
		return objectif;
			
	}
	
	public static Objective generateObjective(){
		FileConfiguration config = Main.getInstance().getConfig();
		Objective objectif = new Objective();
		objectif.cout = ThreadLocalRandom.current().nextInt(config.getInt("contract.Cost.min"), config.getInt("contract.Cost.max"));
		objectif.recompense = ThreadLocalRandom.current().nextInt(config.getInt("contract.Reward.min"), config.getInt("contract.Reward.max"));
		objectif.joursFin = ThreadLocalRandom.current().nextInt(config.getInt("contract.Time.min"), config.getInt("contract.Time.max"));
		objectif.joursPenalite = ThreadLocalRandom.current().nextInt(config.getInt("contract.Penalty.Time.min"), config.getInt("contract.Penalty.Time.max"));
		objectif.penalite = ThreadLocalRandom.current().nextInt(config.getInt("contract.Penalty.Money.min"), config.getInt("contract.Penalty.Money.max"));

		Integer nbmateriaux = 0;
		for(@SuppressWarnings("unused") String materiaux : config.getStringList("contract.Materials")) {
			nbmateriaux++;
		}
		Material[] tabMateriaux = new Material[nbmateriaux];
		int i=0;
		for(String materiaux : config.getStringList("contract.Materials")) {
			tabMateriaux[i] = Material.valueOf(materiaux);
			i++;
		}
		nbmateriaux = ThreadLocalRandom.current().nextInt(config.getInt("contract.NumberMaterial.min"), config.getInt("contract.NumberMaterial.max"));
		for(i=0; i<nbmateriaux; i++) {
			int quantity = ThreadLocalRandom.current().nextInt(config.getInt("contract.Quantity.min"), config.getInt("contract.Quantity.max")+1);
			int random = ThreadLocalRandom.current().nextInt(0,tabMateriaux.length);

			objectif.materiaux.put(tabMateriaux[random], quantity);	
		}
		for(String commandes : config.getConfigurationSection("contract.Reward.Command").getKeys(false)) {
			int random = ThreadLocalRandom.current().nextInt(0, 101);
			if(random<config.getInt("contract.Reward.Command."+commandes)) {
				objectif.commandes.add(commandes);
			}
		}
		return objectif;
	}
	
	public static void reset() {
		Utils.resetDailyContracts();
		Main instance = Main.getInstance();
		//baisse des jours de pénalité
		for(String keys : instance.getData().getConfigurationSection("takenObjectives").getKeys(false)) {
			if(instance.getData().isSet("takenObjectives."+keys+".joursPenalite")) {
    			int jourspenalite = instance.getData().getInt("takenObjectives."+keys+".joursPenalite");
    			if(jourspenalite>0) {
    				if(jourspenalite -1 == 0) instance.getData().set("takenObjectives."+keys+".joursPenalite", null);
    				else instance.getData().set("takenObjectives."+keys+".joursPenalite", jourspenalite -1);
    				
    			}
			}
			if(Main.getInstance().getData().isSet("takenObjectives."+keys+".dejaFait")){
				Main.getInstance().getData().set("takenObjectives."+keys+".dejaFait", false);
			}
		}
		
		//gestion du temps de fin et de la pénalité d'un objectif
		for(Entry<Objective, String> entry : Main.takenObjectives.entrySet()) {
			Objective ob = entry.getKey();
			String keys = entry.getValue();
			
			instance.getData().set("takenObjectives."+keys+".objective.joursFin", ob.joursFin-1);
			if(ob.joursFin-1 == 0) {
				Guild guilde = Guilds.getApi().getGuild(UUID.fromString(keys));
				double monnaie = guilde.getBalance();
				double dette = ob.penalite - monnaie;
				if(dette > 0) {
					instance.getData().set("takenObjectives."+keys+".dette", dette);
					guilde.setBalance(0);
				}
				else {
					guilde.setBalance(monnaie-ob.penalite);
				}
				instance.getData().set("takenObjectives."+keys+".joursPenalite", ob.joursPenalite);
				instance.getData().set("takenObjectives."+keys+".objective", null);
			}	
		}
	}
	
	public static void setObjective(Objective ob, String path) {
		FileConfiguration data = Main.getInstance().getData();
		data.set(path+".cout", ob.cout);
		data.set(path+".joursFin", ob.joursFin);
		data.set(path+".joursPenalite", ob.joursPenalite);
		data.set(path+".recompense", ob.recompense);
		data.set(path+".penalite", ob.penalite);
		for (String entry : ob.commandes) {
		    List<String> list = data.getStringList(path+".commandes");
		    list.add(entry);
		    data.set(path+".commandes", list);
		}
		for (Entry<Material, Integer> entry : ob.materiaux.entrySet()) {
		    data.set(path+".materiaux."+entry.getKey().toString(),entry.getValue());
		}
		try {
			Main.getInstance().getData().save(Main.getInstance().getDataF());
			Main.getInstance().getData().load(Main.getInstance().getDataF());
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void resetDailyContracts() {
		FileConfiguration data = Main.getInstance().getData();
		FileConfiguration config = Main.getInstance().getConfig();
		//effacement des anciens objectifs
		for(int i =0; i<54; i++) Main.objectifs[i] = null;
		for(String keys : data.getConfigurationSection("dailyObjectives").getKeys(false)) {
			data.set("dailyObjectives." + keys, null);
		}
		for(int i = 0; i<config.getInt("contract.Number") && i<54; i++) {
			Objective ob = generateObjective();
			setObjective(ob, "dailyObjectives."+i);
			Main.objectifs[i] = ob;
		}
	}
	
	
	//Inventaires
	public static void showContracts(Player p) {
		int taille=0;
		for(int i=0; i<54;i++) {
			if(Main.objectifs[i] != null) taille++;
		}
		if(taille/9>0) {
			taille = taille/9;
		}else{
			taille = (taille/9)+1;
		}
		taille *=9;
		final Inventory inv = Bukkit.createInventory((InventoryHolder)null, taille, Utils.getMsg("Menu.dailyContracts.name", " ",  " "));
		for(int i=0, j=0; i<54; i++) {
			if(Main.objectifs[i] != null) {
				Objective ob = Main.objectifs[i];
				String items = "||Items requis:||";
				for (Entry<Material, Integer> entry : ob.materiaux.entrySet()) {
					items += "- " +entry.getValue() + " stack(s) de " + entry.getKey() + "||";
				}
				String lore = getMsg("Menu.currentContract.lore", "[Penalite]", ob.penalite.toString()) + items;
				lore = lore.replace("[Recompense]", ob.recompense.toString());
				lore = lore.replace("[Temps]", ob.joursFin.toString());
				lore = lore.replace("[TempsPenalite]", ob.joursPenalite.toString());
				lore = lore.replace("[Prix]", ob.cout.toString());
				if(ob.joursFin>-1) {
					inv.setItem(j, getItemWithLore(Material.PAPER, Utils.getMsg("Menu.dailyContracts.freeObjective", "[Number]",  (j+1)+""), lore));
				}
				else {
					inv.setItem(j, getItemWithLore(Material.PAPER, Utils.getMsg("Menu.dailyContracts.takenObjective", "[Number]",  (j+1)+""), lore));

				}
				j++;
			}
		}
		contract.menuName.put(p, "ContratsDuJour");
		p.openInventory(inv);
	}
	
	
	public static void obectifActuel(Player p) {
        final Inventory inv = Bukkit.createInventory((InventoryHolder)null, 9, getMsg("Menu.currentContract.name", " ", " "));
        Objective ob = getKey(Main.takenObjectives, Main.api.getGuild(p).getId().toString());
		String items = "||Items requis:||";
		for (Entry<Material, Integer> entry : ob.materiaux.entrySet()) {
			items += "- " +entry.getValue() + " stack(s) de " + entry.getKey() + "||";
		}
		String lore = getMsg("Menu.currentContract.lore", "[Penalite]", ob.penalite.toString()) + items;
		lore = lore.replace("[Recompense]", ob.recompense.toString());
		lore = lore.replace("[Temps]", ob.joursFin.toString());
		lore = lore.replace("[TempsPenalite]", ob.joursPenalite.toString());
		lore = lore.replace("[Prix]", ob.cout.toString());
		inv.setItem(4, getItemWithLore(Material.PAPER, getMsg("Menu.currentContract.itemName", " ", " "), lore));
		contract.menuName.put(p, "Objectif");
		p.openInventory(inv);
    }



	
	public static ItemStack getItemWithLore(final Material material, final String customName,  String lore) {
		final ItemStack A = new ItemStack(material);
		final ItemMeta B = A.getItemMeta();
		if (customName != null) {
			B.setDisplayName(customName);
			List<String> lore2 = new ArrayList<String>(Arrays.asList(lore.split("\\|\\|")));
			B.setLore(lore2);
		}
		A.setItemMeta(B);
		return A;
	}
	
	public static <K, V> K getKey(Map<K, V> map, V value) {
		for (Map.Entry<K, V> entry : map.entrySet()) {
			if (value.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

	public static int itemAmount(ItemStack[] inv, Material mat) {
		int number = 0;
        for(int j = 0; j < inv.length; j++){
            if(inv[j] != null){
                if(inv[j].getType() == mat){
                    number = number + inv[j].getAmount();
                   
                }
            }
        }
        return number;
	}
	
	
}
