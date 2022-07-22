package com.klapeks.wg;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.klapeks.libs.commands.ComplexMatiaCommand;
import com.klapeks.libs.commands.Messaging;
import com.klapeks.libs.nms.NMS;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.SessionManager;
import com.sk89q.worldguard.session.handler.Handler;

public class AntiFly extends JavaPlugin implements Listener {
	
	public static StateFlag FLYING_ALLOWED = new StateFlag("flying-allowed", true);
	@Override
	public void onLoad() {
		FlagRegistry fr = WorldGuard.getInstance().getFlagRegistry();
		fr.register(FLYING_ALLOWED);
	}
	
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);
		SessionManager sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();
		sessionManager.registerHandler(FlyHandler.FACTORY, null);
	}
	
	@EventHandler
	public void onFly(PlayerToggleFlightEvent e) {
		if (!e.isFlying()) return;
		Player player = e.getPlayer();
		Location loc = BukkitAdapter.adapt(player.getLocation());
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionQuery query = container.createQuery();
		ApplicableRegionSet set = query.getApplicableRegions(loc);
		set.forEach(reg -> {
			if (e.isCancelled()) return;
			if (reg.getFlag(FLYING_ALLOWED) != State.DENY) return;
			Messaging.msg(player, "region.flying_disabled");
			e.setCancelled(true);
		});
//		WorldGuardPlugin.inst()..getRegionManager(p.getWorld()).getApplicableRegions(p.getLocation())
	}
	public static class FlyHandler extends Handler {
		public static final Factory FACTORY = new Factory();
		public static class Factory extends Handler.Factory<FlyHandler> {
	        @Override
	        public FlyHandler create(Session session) {
	            return new FlyHandler(session);
	        }
	    }
		protected FlyHandler(Session session) {
			super(session);
		}
		
		@Override
		public boolean onCrossBoundary(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet,
				Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType) {
			Player pl = Bukkit.getPlayer(player.getUniqueId());
			entered.forEach(reg -> {
				if (!pl.isFlying()) return;
				if (reg.getFlag(FLYING_ALLOWED) != State.DENY) return;
				Messaging.msg(pl, "region.flying_disabled");
				pl.setFlying(false);
			});
			return super.onCrossBoundary(player, from, to, toSet, entered, exited, moveType);
		}
		
	}
}
