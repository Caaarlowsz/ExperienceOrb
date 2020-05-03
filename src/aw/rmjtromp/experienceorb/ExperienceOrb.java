package aw.rmjtromp.experienceorb;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import aw.rmjtromp.RunicCore.core.Core;
import aw.rmjtromp.RunicCore.core.other.extensions.RunicPlayer;
import aw.rmjtromp.RunicCore.utilities.Debug.Debuggable;
import aw.rmjtromp.RunicCore.utilities.Help;
import aw.rmjtromp.RunicCore.utilities.RunicCommand;
import aw.rmjtromp.RunicCore.utilities.RunicExtension;
import aw.rmjtromp.RunicCore.utilities.RunicUtils;
import aw.rmjtromp.RunicCore.utilities.configs.MessageConfig.MESSAGE;
import aw.rmjtromp.RunicCore.utilities.placeholders.Placeholder;
import aw.rmjtromp.experienceorb.Orb.OrbFlag;
import aw.rmjtromp.experienceorb.Orb.OrbType;

public class ExperienceOrb extends RunicExtension implements Listener, CommandExecutor, Debuggable {

	@Override
	public String getName() {
		return "ExperienceOrb";
	}

	@Override
	public String getVersion() {
		return "1.0.0";
	}
	
	private enum PERMISSION {
		ORB_SPAWN("runic.orb.spawn"),
		ORB_KILLALL("runic.orb.killall");
		
		private String permission;
		PERMISSION(String permission) {
			this.permission = permission;
		}
		
		@Override
		public String toString() {
			return permission;
		}
	}
	
	private Help help;
	private List<Orb> orbs = new ArrayList<>();
	
	@Override
	public void onEnable() {
		registerCommand(new RunicCommand("orb")
				.setDescription("experienceorb that give players experience")
				.setUsage("/orb")
				.setExecutor(this));
		help = Help.create();
		help.addCommand("help [page]", "Returns this list of commands");
		help.addCommand("killall [-force]", "Kills all the existing orbs");
		help.addCommand("spawn <type> [-perm] [-uncollectible]", "Spawns an experience orb");
		help.addCommand("select", "Selects the orb you're looking at");
		help.addCommand("remove [-force]", "Remove the orb selected");
		help.addCommand("settype <type> [-update]", "Changes the type of the orb selected");
	}
	
	private String incorrect_usage, sender_not_a_player, no_permission, not_enough_arguments, orb_killall, orb_no_orbs, orb_spawned, orb_invalid_orb_type;
	@Override
	public void loadConfigurations() {
		incorrect_usage = Core.getMessages().getMessage(MESSAGE.INCORRECT_USAGE);
		sender_not_a_player = Core.getMessages().getMessage(MESSAGE.SENDER_NOT_A_PLAYER);
		no_permission = Core.getMessages().getMessage(MESSAGE.NO_PERMISSION);
		not_enough_arguments = Core.getMessages().getMessage(MESSAGE.NOT_ENOUGH_ARGUMENTS);

		orb_killall = Core.getMessages().getString("extensions.experienceorb.killall", "&e{COUNT} &7killed.");
		orb_invalid_orb_type = Core.getMessages().getString("extensions.experienceorb.invalid-orb-type", "&cInvalid orb type provided.");
		orb_spawned = Core.getMessages().getString("extensions.experienceorb.spawned", "&e{TYPE} experience orb &7spawned at your location.");
		orb_no_orbs = Core.getMessages().getString("extensions.experienceorb.no-orbs-to-kill", "&cThere are no orbs to kill.");
		
		// load orbs from config
		// remove all existing orbs (for reload)
		if(orbs.size() > 0) {
			for(Orb orb : orbs) {
				// removes valid orbs the correct way
				if(Orb.getOrbs().contains(orb)) {
					orb.remove();
				}
			}
			// if the list still contains invalid orbs, remove them
			if(orbs.size() > 0) orbs.clear();
		}
		
		// re-check config for the orbs and add them
		if(Core.getConfig().contains("extensions.experienceorb.permanent-orbs") && Core.getConfig().hasKeys("extensions.experienceorb.permanent-orbs")) {
			Set<String> permanentOrbs = Core.getConfig().getKeys("extensions.experienceorb.permanent-orbs");
			for(String orbName : permanentOrbs) {
				Location location;
				OrbType type = OrbType.COMMON;
				List<OrbFlag> flags = new ArrayList<OrbFlag>();
				
				if(Core.getConfig().contains("extensions.experienceorb.permanent-orbs."+orbName+".location") && Core.getConfig().isString("extensions.experienceorb.permanent-orbs."+orbName+".location")) {
					String loc = Core.getConfig().getString("extensions.experienceorb.permanent-orbs."+orbName+".location");
					location = RunicUtils.str2loc(loc);
					
					if(Core.getConfig().contains("extensions.experienceorb.permanent-orbs."+orbName+".common") && Core.getConfig().isString("extensions.experienceorb.permanent-orbs."+orbName+".common")) {
						String t = Core.getConfig().getString("extensions.experienceorb.permanent-orbs."+orbName+".location", "common");
						OrbType ot = OrbType.valueOf(t);
						if(ot != null) type = ot;
						else warn("Orb '"+orbName+"' invalid type: '"+t+"', assuming COMMON.");
					} else warn("Orb '"+ orbName+"' type not defined, assuming COMMON.");
					
					if(Core.getConfig().contains("extensions.experienceorb.permanent-orbs."+orbName+".flags") && Core.getConfig().isList("extensions.experienceorb.permanent-orbs."+orbName+".flags")) {
						List<String> flagList = Core.getConfig().getStringList("extensions.experienceorb.permanent-orbs."+orbName+".flags", null);
						for(String flag : flagList) {
							OrbFlag f = OrbFlag.valueOf(flag);
							if(f != null) flags.add(f);
							else warn("Orb '"+orbName+"' invalid flag: '"+f+"', skipping.");
						}
					}
					
					Orb orb = flags.size() > 0 ? Orb.create(location, type, (OrbFlag[]) flags.toArray()) : Orb.create(location, type);
					orbs.add(orb);
				} else {
					Core.getConfig().set("extensions.experienceorb.permanent-orbs."+orbName+".location", null);
					error("Orb '"+orbName+"' location not defined, orb removed.");
				}
			}
		}
		debug(orbs.size()+" orbs were spawned.");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) {
			RunicPlayer player = RunicPlayer.cast(sender);
			if(args.length == 0) player.sendMessage(Placeholder.parse(not_enough_arguments, player).getString());
			else if(args.length == 1) {
				if(args[0].equalsIgnoreCase("killall")) {
					if(player.hasPermission(PERMISSION.ORB_KILLALL.toString())) {
						int orbsSize = 0;
						for(Orb o : Orb.getOrbs()) if(!o.hasFlag(OrbFlag.PERMANENT)) orbsSize++;
						if(orbsSize > 0) {
							Orb.removeAll();
							player.sendMessage(Placeholder.parse(orb_killall, player).set("{COUNT}", (orbsSize+" "+(orbsSize > 1 ? "orbs" : "orb"))).getString());
						} else player.sendMessage(Placeholder.parse(orb_no_orbs, player).getString());
					} else player.sendMessage(Placeholder.parse(no_permission, player).getString());
				} else if(args[0].equalsIgnoreCase("spawn")) player.sendMessage(Placeholder.parse(not_enough_arguments, player).set("{COMMAND}", label.toLowerCase()+" spawn <type>").getString());
				else if(args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) player.sendMessage(String.join("\n", help.getHelp(label)));
				else player.sendMessage(Placeholder.parse(incorrect_usage, player).set("{COMMAND}", label.toLowerCase()+" help").getString());
			} else if(args.length >= 2) {
				if(args[0].equalsIgnoreCase("spawn")) {
					if(player.hasPermission(PERMISSION.ORB_SPAWN.toString())) {
						OrbType[] types = OrbType.values();
						for(OrbType type : types) {
							if(args[1].equalsIgnoreCase(type.toString())) {
								Orb orb = Orb.create(player.getLocation(), type);
								if(args.length >= 3) {
									for(int i = 3; i < args.length; i++) {
										if(!args[i].isEmpty()) {
											if(args[i].equalsIgnoreCase("-permanent") || args[i].equalsIgnoreCase("-perm")) {
												orb.addFlag(OrbFlag.PERMANENT);
											} else if(args[i].equalsIgnoreCase("-uncollectible")) {
												orb.addFlag(OrbFlag.UNCOLLECTIBLE);
											}
										}
									}
								}
								player.sendMessage(Placeholder.parse(orb_spawned, player).set("{TYPE}", type.getFriendlyName()).getString());
								return true;
							}
						}
						
						player.sendMessage(Placeholder.parse(orb_invalid_orb_type, player).getString());
					} else player.sendMessage(Placeholder.parse(no_permission, player).getString());
				} else if(args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
					int page = 1;
					if(NumberUtils.isNumber(args[1])) page = Integer.parseInt(args[1]);
					player.sendMessage(String.join("\n", (String[]) help.getHelp(page).toArray()));
				} else if(args[0].equalsIgnoreCase("killall")) {
					if(player.hasPermission(PERMISSION.ORB_KILLALL.toString())) {
						if(Orb.getOrbs().size() > 0) {
							for(int i = 2; i < args.length; i++) {
								
								if(!args[i].isEmpty()) {
									if(args[i].equalsIgnoreCase("-force") || args[i].equalsIgnoreCase("-all")) {
										Orb.removeAll(true);
										player.sendMessage(Placeholder.parse(orb_killall, player).set("{COUNT}", (Orb.getOrbs().size()+" "+(Orb.getOrbs().size() > 1 ? "orbs" : "orb"))).getString());
										return true;
									}
								}
							}
							
							int orbsSize = 0;
							for(Orb o : Orb.getOrbs()) if(!o.hasFlag(OrbFlag.PERMANENT)) orbsSize++;
							if(orbsSize > 0) {
								Orb.removeAll();
								player.sendMessage(Placeholder.parse(orb_killall, player).set("{COUNT}", (orbsSize+" "+(orbsSize > 1 ? "orbs" : "orb"))).getString());
							} else player.sendMessage(Placeholder.parse(orb_no_orbs, player).getString());
						} else player.sendMessage(Placeholder.parse(orb_no_orbs, player).getString());
					}
				} else player.sendMessage(Placeholder.parse(incorrect_usage, player).set("{COMMAND}", label.toLowerCase()+" help").getString());
			}
		} else sender.sendMessage(Placeholder.parse(sender_not_a_player).getString());
		return true;
	}

}
