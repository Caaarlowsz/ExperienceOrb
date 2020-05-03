package aw.rmjtromp.experienceorb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import aw.rmjtromp.RunicCore.RunicCore;
import aw.rmjtromp.RunicCore.core.Core;
import aw.rmjtromp.RunicCore.core.other.extensions.RunicItemStack;
import aw.rmjtromp.RunicCore.core.other.extensions.RunicPlayer;
import aw.rmjtromp.RunicCore.utilities.Debug.Debuggable;
import aw.rmjtromp.RunicCore.utilities.RunicUtils;
import aw.rmjtromp.RunicCore.utilities.configs.Config;

public final class Orb implements Listener, Debuggable {
	
	private static final RunicCore plugin = RunicCore.getInstance();
	
	@Override
	public String getName() {
		return "Orb";
	}
	
	public enum OrbType {
		COMMON("Common", 50, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzQ2OWZhNjFlNWE2YmVlZWVkM2RlNGFkNzEwMWJhMTFlY2RkMGMzMTdlMjY4Zjc4NTMzMWFlZjZlMjlhMmE5YSJ9fX0="),
		RARE("Rare", 500, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjIwMTdiOTM4NmJjYjAzMGY4ZTFhYmEwODg5ZTEwYTgyNGMzNmRmNjZhOWQ4Y2ZiYTNjOTk4YmU1OTdjZGQwMiJ9fX0="),
		LEGENDARY("Legendary", 1500, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmIwMmUyZjhkYWUxMGFkYjFmMDNjMmJhNmI2ZWNmNzJiZDViMjllNmU4OThjZTI1M2ZiZTRlMjdjNGIzODgwMCJ9fX0=");
		
		private String texture, friendlyName;
		private int experience = 0;
		OrbType(String friendlyName, int exp, String texture) {
			this.texture = texture;
			this.experience = exp;
			this.friendlyName = friendlyName;
		}
		
		public String getTexture() {
			return texture;
		}
		
		public String getFriendlyName() {
			return friendlyName;
		}
		
		public int getExperience() {
			return experience;
		}
	}
	
	public void Nigger()
	{
		
	}
	
	public enum OrbFlag {
		UNCOLLECTIBLE,
		PERMANENT;
	}
	
	public static Orb create(String id) {
		if(id == null || id.isEmpty()) return null;
		if(config == null) config = Core.getConfig();
		Location location;
		OrbType type = OrbType.COMMON;
		List<OrbFlag> flags = new ArrayList<OrbFlag>();
		
		String path = "extensions.kitpvp.experienceorb."+id;
		
		if(config.contains(path)) {
			location = config.contains(path+".location") && config.isString(path+".location") ? RunicUtils.str2loc(config.getString(path+".location")) : null;
			OrbType pretype = config.contains(path+".type") && config.isString(path+".type") ? OrbType.valueOf(config.getString(path+".type", "common").toUpperCase()) : OrbType.valueOf(config.getString(path+".type", "common").toUpperCase());
			type = pretype == null ? OrbType.COMMON : pretype;
			if(config.contains(path+".flags") && config.isList(path+".flags")) {
				List<String> stringFlags = config.getStringList(path+".flags");
				for(String flag : stringFlags) {
					OrbFlag f = OrbFlag.valueOf(flag.toUpperCase());
					if(f != null) flags.add(f);
				}
			}
			
			if(location == null) {
				Bukkit.getServer().getLogger().log(Level.WARNING, "[RunicCore] [KitPvP] [ExperienceOrb] Invalid location for orb '"+id+"', removing orb.");
				return null;
			}
			
			return new Orb(id, location, type, (OrbFlag[]) flags.toArray());
		}
		
		
		return null;
	}

	public static Orb create(Location location, OrbType type, OrbFlag...flags) {
		if(location != null && type != null) return new Orb(RunicUtils.generateRandomWord(), location, type, flags);
		return null;
	}

	public static Orb create(Location location, OrbType type) {
		if(location != null && type != null) return new Orb(RunicUtils.generateRandomWord(), location, type);
		return null;
	}
	
	public static Orb create(Location location) {
		if(location != null) return new Orb(RunicUtils.generateRandomWord(), location, OrbType.COMMON);
		return null;
	}
	
	private static HashMap<UUID, Orb> orbs = new HashMap<UUID, Orb>();
	private static BukkitTask task = null;
	private static Config config;

	private ArmorStand orb;
	private List<OrbFlag> flags = new ArrayList<OrbFlag>();
	private OrbType type = OrbType.COMMON;
	private Location initialLocation;
	private double frame = 0;
	private double Y = 0;
	private String id;
	
	private Orb(String id, Location location, OrbType type, OrbFlag...flags) {
		orb = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
		orb.setHelmet(RunicItemStack.getSkull(type.getTexture()));
		orb.setVisible(false);
		orb.setGravity(false);
		orb.setSmall(true);
		orb.setCustomName(ChatColor.translateAlternateColorCodes('&', type == OrbType.LEGENDARY ? "&c&lLegedary Orb" : type == OrbType.RARE ? "&6&lRare Orb" : "&e&lCommon Orb"));
		orb.setCustomNameVisible(true);
		orbs.put(orb.getUniqueId(), this);
		this.id = id;
		this.type = type;
		initialLocation = location;
		Y = orb.getLocation().getY();
		
		for(OrbFlag flag  : flags) addFlag(flag);
		
		if(config == null) config = Core.getConfig();
		if(task == null) task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runRepeatingTask(), 0, 1);
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	private Orb(Config config, String path) {
		
	}
	
	private static Runnable runRepeatingTask() {
		return () -> {
			for(Orb orb : orbs.values()) {
				if(orb != null && orb.orb.isValid()) {
					double height = 0.5*(Math.cos((2*Math.PI*(orb.frame/20))/3))+1;
					
					Location loc = orb.getLocation();
					loc.setY(orb.Y+height);
					orb.orb.teleport(loc);
					
					if(orb.frame >= 60) orb.frame = 0;
					else orb.frame++;
				} else orb.remove();
			}
		};
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
		Orb orb = orbs.containsKey(e.getRightClicked().getUniqueId()) ? orbs.get(e.getRightClicked().getUniqueId()) : null;
		if(orb != null && this.equals(orb)) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
		if(e.getRightClicked() instanceof ArmorStand) {
			Orb orb = orbs.containsKey(e.getRightClicked().getUniqueId()) ? orbs.get(e.getRightClicked().getUniqueId()) : null;
			if(orb != null && this.equals(orb)) {
				e.setCancelled(true);
				if(!orb.hasFlag(OrbFlag.UNCOLLECTIBLE)) {
					RunicPlayer player = RunicPlayer.cast(e.getPlayer());
					player.playSound(getLocation(), Sound.ORB_PICKUP, 1, 1);
					// &aReceived 25 experience &7(+25% Network Booster)&a.
					player.sendMessage("&aReceived "+getType().getExperience()+" experience.");
					remove();
					
					// call event
					Bukkit.getPluginManager().callEvent(new PlayerOrbCollectEvent(player, getType()));
				}
			}
		}
	}
	
	public void remove() {
		if(orb.isValid()) orb.remove();
		if(orbs.containsValue(this)) orbs.remove(orb.getUniqueId());
		orb = null;
		HandlerList.unregisterAll(this);
	}
	
	public OrbType getType() {
		return type;
	}
	
	public Collection<OrbFlag> getFlags() {
		return flags;
	}
	
	public void addFlag(OrbFlag flag) {
		if(!hasFlag(flag)) {
			// already has this flag, no need to re-add it, or update it
			flags.add(flag);
			
			String path = "extensions.kitpvp.experienceorb."+id;
			if(hasFlag(OrbFlag.PERMANENT)) {
				// update the orb completely
				if(config.contains(path)) config.set(path, null);
				config.set(path+".location", RunicUtils.loc2str(initialLocation));
				config.set(path+".type", getType().toString().toLowerCase());
				List<String> flagList = new ArrayList<String>();
				for(OrbFlag f : getFlags()) flagList.add(f.toString().toLowerCase());
				if(flagList.size() > 0) config.set(path+".flags", flagList);
			}
		}
	}
	
	public void removeFlag(OrbFlag flag) {
		if(hasFlag(flag)) flags.remove(flag);
	}
	
	public boolean hasFlag(OrbFlag flag) {
		return flag != null ? flags.contains(flag) : false;
	}
	
	public Location getLocation() {
		return orb != null ? orb.getEyeLocation() : null;
	}
	
	public boolean isValid() {
		if(this != null) {
			if(this.orb == null || !this.orb.isValid() || !orbs.containsKey(this.orb.getUniqueId())) {
				this.remove();
			} else return true;
		}
		return false;
	}
	
	public static void removeAll() {
		removeAll(false);
	}
	
	public static void removeAll(boolean force) {
		List<Orb> orbs = new ArrayList<Orb>();
		orbs.addAll(getOrbs());
		for(Orb orb : orbs) {
			if(force || !orb.hasFlag(OrbFlag.PERMANENT)) {
				orb.remove();
			}
		}
		orbs.clear();
	}
	
	public static Collection<Orb> getOrbs() {
		return orbs.values();
	}
	
	public static Orb getOrbImLookingAt(RunicPlayer player) {
		List<Entity> entities = player.getNearbyEntities(10, 10, 10);
		for(Entity entity : entities) {
			if(entity instanceof ArmorStand) {
				ArmorStand as = (ArmorStand) entity;
				if(orbs.containsKey(as.getUniqueId())) {
					Orb orb = orbs.get(as.getUniqueId());
					if(orb.isValid()) {
					    Location eye = player.getEyeLocation();
					    Vector toEntity = as.getEyeLocation().toVector().subtract(eye.toVector());
					    double dot = toEntity.normalize().dot(eye.getDirection());

					    if(dot > 0.99D) {
					    	return orb;
					    }
					}
				}
			}
		}
		return null;
	}
	
	@EventHandler
	public void onPluginDisable(PluginDisableEvent e) {
		if(e.getPlugin().equals(plugin)) {
			if(orbs.size() > 0) removeAll(true);
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj != null && orb != null) {
			if(obj instanceof Orb) {
				if(orb.getUniqueId().equals(((Orb) obj).orb.getUniqueId())) return true;
			} else if (obj instanceof ArmorStand) {
				if(orb.getUniqueId().equals(((ArmorStand) obj).getUniqueId())) return true;
			}
		}
		return false;
	}
	
}
