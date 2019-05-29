package fr.black_eyes.guildObjectives;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import fr.black_eyes.guildObjectives.commands.contract;
import fr.black_eyes.guildObjectives.listeners.InventoryListeners;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.api.GuildsAPI;
import me.glaremasters.guilds.guild.Guild;




//Faire commande de reload
//initialiser menu temps



public class Main extends JavaPlugin{
	
	public static Objective[] objectifs = new Objective[54];
	public static HashMap<Objective, String> takenObjectives = new HashMap<Objective, String>();
	private File dataFile;
	private FileConfiguration data;
	private File configFile;
	private FileConfiguration config;
	private File langFile;
	private FileConfiguration lang;
	private static Main instance;
	public static GuildsAPI api;
	
	public void onDisable() {
		try {
			instance.getData().save(instance.getDataF());
		} catch (IOException | IllegalArgumentException e) {
			e.printStackTrace();
		
		}	
	}
	
	public static void reload() {
		takenObjectives.clear();
		try {
			instance.getData().save(instance.getDataF());
			instance.getData().load(instance.getDataF());
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
		if(Main.getInstance().getData().getConfigurationSection("dailyObjectives").getKeys(false).size() == 0) {
	        	Utils.resetDailyContracts();
	    }
        Utils.loadObjectives("dailyObjectives.");
        for(String key : instance.getData().getConfigurationSection("takenObjectives").getKeys(false)) {
        	if(instance.getData().isSet("takenObjectives."+key+".objective.cout")) {
        		takenObjectives.put(Utils.getTakenObjective("takenObjectives."+key+".objective."), key);
        	}
        }
	}
	
	public void onEnable() {
		instance = this;
		
        this.getCommand("contract").setExecutor(new contract());
        this.getServer().getPluginManager().registerEvents(new InventoryListeners(), this);
        super.onEnable();
        if(!initFiles()) {
        	getLogger().info("§cThe data file couldn't be initialised");
        	return;
        }
        setLang("Menu.dailyContracts.freeObjective", "Objectif n°[Number] (Cliquez pour le prendre)");
        setLang("Menu.dailyContracts.takenObjective", "Objectif n°[Number] (&4Déjà pris!)");
        setLang("Menu.currentContract.itemName", "Objectif (Cliquez pour le terminer)");
        setLang("alreadyDoneContract", "&cDésolé, vous avez déjà fait un contrat aujourd'hui");
        api = Guilds.getApi();
        if(data.getConfigurationSection("dailyObjectives").getKeys(false).size() == 0) {
        	Utils.resetDailyContracts();
        }
        Utils.loadObjectives("dailyObjectives.");
        for(String key : instance.getData().getConfigurationSection("takenObjectives").getKeys(false)) {
        	if(instance.getData().isSet("takenObjectives."+key+".objective.cout")) {
        		takenObjectives.put(Utils.getTakenObjective("takenObjectives."+key+".objective."), key);
        	}
        }
        
        
        
        //check du respawn des coffres toutes les minutes
        //check of chest respawn all minutes
        new BukkitRunnable() {
            public void run() {
            	int hour = LocalTime.now().getHour();
            	int minutes = LocalTime.now().getMinute();
            	if(hour == instance.getConfig().getInt("contract.Reset.heure") && minutes == instance.getConfig().getInt("contract.Reset.minutes")) {
            		Utils.reset();
            	}
            	
            	//prise de la monnaie du coffre pour compenser les dettes
            	for(String keys : instance.getData().getConfigurationSection("takenObjectives").getKeys(false)) {
        			Guild guilde = Guilds.getApi().getGuild(UUID.fromString(keys));
        			double monnaie = guilde.getBalance();
        			if(instance.getData().isSet("takenObjectives."+keys+".dette")) {
	        			double dette = instance.getData().getDouble("takenObjectives."+keys+".dette");
	        			if(dette>0 && monnaie > 0) {
	        				if(dette > monnaie) {
            					instance.getData().set("takenObjectives."+keys+".dette", dette-monnaie);
            					guilde.setBalance(0);
            				}
            				else {
            					guilde.setBalance(monnaie-dette);
            					instance.getData().set("takenObjectives."+keys+".dette", null);
            				}
	        			}
        			}
            	}
        		try {
        			instance.getData().save(instance.getDataF());
        			instance.getData().load(instance.getDataF());
        		} catch (IOException | InvalidConfigurationException e) {
        			e.printStackTrace();
        		}
            }
        }.runTaskTimer(this, 0, 1200);
    }
	public static Main getInstance() {
        return instance;
    }
	
	
	//function to update config on new version
	public void setConfig(String path, Object value) {
		if(this.getConfig().isSet(path))
			return;
		else
			getInstance().getConfig().set(path, value);
			try {
				instance.getConfig().save(instance.getConfigF());
				instance.getConfig().load(instance.getConfigF());
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
	}
	
	//function to edit lang file on new version
	public void setLang(String path, Object value) {
		if(this.getLang().isSet(path))
			return;
		else
			getInstance().getLang().set(path, value);
			try {
				instance.getLang().save(instance.getLangF());
				instance.getLang().load(instance.getLangF());
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
	}
	
	
	
	
	//file initializations
	public File getDataF() {
		return this.dataFile;
	}
	public File getConfigF() {
		return this.configFile;
	}
	public File getLangF() {
		return this.langFile;
	}
	public FileConfiguration getData() {
		return this.data;
	}
	public FileConfiguration getConfig() {
		return this.config;
	}
	public FileConfiguration getLang() {
		return this.lang;
	}
	
	private boolean initFiles() {
		//config
	    configFile = new File(getDataFolder(), "config.yml");
	    langFile = new File(getDataFolder(), "lang.yml");
	    dataFile = new File(getDataFolder(), "data.yml");
	    if (!configFile.exists()) {
	        configFile.getParentFile().mkdirs();
	        saveResource("config.yml", false);
	    }
	    config= new YamlConfiguration();
	    try {
	        config.load(configFile);
	    } catch (IOException | InvalidConfigurationException e) {
	        e.printStackTrace();
	    }
	    
	    //lang
	    if (!langFile.exists()) {
	        langFile.getParentFile().mkdirs();
	        saveResource("lang.yml", false);
	    }
	    lang= new YamlConfiguration();
	    try {
	        lang.load(langFile);
	    } catch (IOException | InvalidConfigurationException e) {
	        e.printStackTrace();
	    }
	   
	    //data
	    if (!dataFile.exists()) {
	        dataFile.getParentFile().mkdirs();
	        saveResource("data.yml", false);
	    }
	    data= new YamlConfiguration();
	    try {
	        data.load(dataFile);
	    } catch ( Exception e) {
	        return false;
	    }
		return true;
	}
	
	//particle initialozation

}
