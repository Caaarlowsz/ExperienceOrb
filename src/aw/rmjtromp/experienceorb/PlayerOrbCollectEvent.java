package aw.rmjtromp.experienceorb;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import aw.rmjtromp.RunicCore.core.other.extensions.RunicPlayer;
import aw.rmjtromp.experienceorb.Orb.OrbType;

public final class PlayerOrbCollectEvent extends Event {
	
	private static final HandlerList HANDLERS_LIST = new HandlerList();
	private RunicPlayer player;
	private OrbType type;
	
	public PlayerOrbCollectEvent(RunicPlayer player, OrbType type) {
		this.player = player;
		this.type = type;
	}
	
	public RunicPlayer getPlayer() {
		return player;
	}
	
	public OrbType getType() {
		return type;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}
	
    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

}
