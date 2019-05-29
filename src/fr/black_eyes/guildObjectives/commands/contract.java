package fr.black_eyes.guildObjectives.commands;

import java.util.HashMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.black_eyes.guildObjectives.Main;
import fr.black_eyes.guildObjectives.Utils;


public class contract implements CommandExecutor {


	public static HashMap<Player, String> menuName = new HashMap<Player, String>();
	
	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
			Player player= null;
			if(sender instanceof Player) {
				player = (Player)sender;
			}
			else {
				return false;
			}
			if(args.length>0) {
				if(args[0].equals("reload")){
					if(!sender.hasPermission("contract.admin")  ) {
						Utils.msg(sender, "noPermission", "[Permission]", "contract.admin");
						return false;
					}
					else {
						Main.reload();
						player.sendMessage("§aPlugin contracts reloaded");
						return true;
					}
				}
				if(args[0].equals("reset")){
					if(!sender.hasPermission("contract.admin")  ) {
						Utils.msg(sender, "noPermission", "[Permission]", "contract.admin");
						return false;
					}
					else {
						Utils.reset();
						player.sendMessage("§aAll daily contract where reset, players that already took a contract can do it another time");
						return true;
					}
				}
			}
			if(!sender.hasPermission("contract.use")  ) {
				Utils.msg(sender, "noPermission", "[Permission]", "contract.use");
				return false;
			}
			String id = null;
			try{
				id = Main.api.getGuild(player).getId().toString();
			}catch(NullPointerException e) {
				Utils.msg(sender, "notInAnyGuild", " ", " ");
				return false;
			}
			if(Main.getInstance().getData().isSet("takenObjectives."+id+".dette")) {
				Utils.msg(sender, "guildHasDebt", "[Debt]", ""+Main.getInstance().getData().getInt("takenObjectives."+id+".dette"));
				return false;
			}
			else if(Main.getInstance().getData().isSet("takenObjectives."+id+".joursPenalite")) {
				Utils.msg(sender, "guildHasDaysPenalty", "[Days]", ""+Main.getInstance().getData().getInt("takenObjectives."+id+".joursPenalite"));
				return false;
			}
			else if(Main.getInstance().getData().isSet("takenObjectives."+id+".objective.joursPenalite")) {
				Utils.obectifActuel(player);
				return true;
			}
			if(Main.getInstance().getData().isSet("takenObjectives."+id+".dejaFait")) {
				if(Main.getInstance().getData().getBoolean("takenObjectives."+id+".dejaFait") == true) {
					Utils.msg(player, "alreadyDoneContract", " ", " ");
					return false;
				}
			}
			else {
				Utils.showContracts(player);
			}

		return false;
	}

	
}
