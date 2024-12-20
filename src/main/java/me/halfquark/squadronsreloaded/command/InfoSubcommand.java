package me.halfquark.squadronsreloaded.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.halfquark.squadronsreloaded.squadron.Squadron;
import me.halfquark.squadronsreloaded.squadron.SquadronCraft;
import me.halfquark.squadronsreloaded.squadron.SquadronManager;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.util.ChatUtils;

public class InfoSubcommand {

	public void run(CommandSender sender, String[] args) {
		Player pSender;
		if(args.length <= 1) {
			if(!(sender instanceof Player)){
	            sender.sendMessage(ChatUtils.MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Squadrons - Must Be Player"));
	            return;
	        }
			pSender = (Player) sender;
			if(!pSender.hasPermission("movecraft.squadron.info")) {
				pSender.sendMessage(ChatUtils.MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Insufficient Permissions"));
				return;
			}
			Squadron sq = SquadronManager.getInstance().getPlayerSquadron(pSender, true);
			if(sq == null){
				sender.sendMessage(ChatUtils.MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Squadrons - No Squadron Found"));
	            return;
			}
			displaySquadron(sq, sender);
			return;
		}
		
		if(!sender.hasPermission("movecraft.squadron.info.others")) {
			sender.sendMessage(ChatUtils.MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Insufficient Permissions"));
			return;
		}
		Player target = Bukkit.getPlayer(args[1]);
		if (target == null) {
            sender.sendMessage(ChatUtils.MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Player - Not Found"));
            return;
        }
		Squadron sq = SquadronManager.getInstance().getPlayerSquadron(target, true);
		if(sq == null){
			sender.sendMessage(ChatUtils.MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Squadrons - Player not piloting"));
            return;
		}
		displaySquadron(sq, sender);
	}
	
	private void displaySquadron(Squadron sq, CommandSender sender) {
		sender.sendMessage(I18nSupport.getInternationalisedString("Squadron:"));
		sender.sendMessage(">" + I18nSupport.getInternationalisedString("Squadron Name:" + sq.getName()));
		sender.sendMessage(">" + I18nSupport.getInternationalisedString("Pilot:" + sq.getPilot().getName()));
		if (sq.getCarrier() != null) {
			sender.sendMessage(">" + I18nSupport.getInternationalisedString("Carrier:" + ((sq.getCarrier().getName() != "")?sq.getCarrier().getName():sq.getCarrier().getType().getStringProperty(CraftType.NAME))));
		}
		sender.sendMessage(">" + I18nSupport.getInternationalisedString("Crafts:"));
		List<Entry<SquadronCraft, Integer>> sortedCrafts = new ArrayList<>(sq.getCraftMap().entrySet());
		Collections.sort(
				sortedCrafts,
				new Comparator<Map.Entry<SquadronCraft, Integer>>() {
			        public int compare(Map.Entry<SquadronCraft, Integer> a, Map.Entry<SquadronCraft, Integer> b) {
			            return Integer.compare(a.getValue(), b.getValue());
			        }
			    });
		for(Entry<SquadronCraft, Integer> entry : sortedCrafts)
			sender.sendMessage(" " + entry.getValue() + ":" + ((entry.getKey().getName() != "")?entry.getKey().getName():entry.getKey().getType().getStringProperty(CraftType.NAME)) + "(" + entry.getKey().getHitBox().size() + ")");

	}
	
}
