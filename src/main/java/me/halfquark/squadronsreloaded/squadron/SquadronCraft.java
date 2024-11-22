package me.halfquark.squadronsreloaded.squadron;

import javax.annotation.Nonnull;

import net.countercraft.movecraft.CruiseDirection;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.craft.*;
import net.countercraft.movecraft.exception.EmptyHitBoxException;
import net.countercraft.movecraft.features.contacts.ContactProvider;
import net.countercraft.movecraft.features.contacts.ContactsManager;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.processing.MovecraftWorld;
import net.countercraft.movecraft.util.Counter;
import net.countercraft.movecraft.util.Tags;
import net.countercraft.movecraft.util.TimingData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import net.countercraft.movecraft.craft.type.CraftType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Logger;

public class SquadronCraft extends BaseCraft implements SubCraft, PilotedCraft, ContactProvider {
	
	private Player pilot;
	private Squadron squadron;

	@NotNull
	TimingData stats = new TimingData();
	
	public SquadronCraft(@Nonnull CraftType type, @Nonnull World world, @Nonnull Player p, @Nonnull Squadron sq) {
		super(type, world);
		pilot = p;
		squadron = sq;
	}

	@Override
	@NotNull
	public Craft getParent() {
		return squadron.getCarrier();
	}

	@Override
	public void setParent(@NotNull Craft parent) {
		return;
	}
	
	public Player getSquadronPilot() {return pilot;}
	public Squadron getSquadron() {return squadron;}
	
	public boolean isLead() {
		return squadron.getLeadCraft().equals(this);
	}

	@Override
	public @NotNull Player getPilot() {
		if (this.getSquadron().getPilot() != null) {
			return this.getSquadron().getPilot();
		}
		return this.pilot;
	}





	@Override
	public Component getDetectedMessage(boolean isNew, Craft craft) {
		if (this != this.getSquadron().getLeadCraft()) {
			return this.getSquadron().getLeadCraft().getDetectedMessage(isNew, craft);
		}
		MovecraftLocation baseCenter, targetCenter;
		try {
			baseCenter = craft.getHitBox().getMidPoint();
			targetCenter = this.getSquadron().getSquadronCenter();
		}
		catch (EmptyHitBoxException e) {
			return null;
		}
		int distSquared = baseCenter.distanceSquared(targetCenter);

		Component notification = Component.empty();
		if (isNew) {
			notification = notification.append(I18nSupport.getInternationalisedComponent("Contact - Squadron New Contact").color(NamedTextColor.RED).decorate(TextDecoration.BOLD));
		}
		else {
			notification = notification.append(I18nSupport.getInternationalisedComponent("Contact - Squadron"));
		}
		notification = notification.append(Component.text( ": "));

		// No longer include the type, that is sort of OP
		// TODO: Obfuscate parts of the name depending on the distance
		Component name = Component.empty();
		String sqName = this.getSquadron().getName();
		if (sqName != null && !sqName.isBlank()) {
			name = name.append(Component.text(sqName));
		} else {
			name = name.append(Component.text(this.getType().getStringProperty(CraftType.NAME)));
		}

		// TODO: Team colors!
		if (this instanceof SinkingCraft) {
			name = name.color(NamedTextColor.RED);
		}
		else if (this.getDisabled()) {
			name = name.color(NamedTextColor.BLUE);
		}
		notification = notification.append(name);

		notification = notification.append(Component.text(", "))
				.append(I18nSupport.getInternationalisedComponent("Contact - Squadron Size"))
				.append(Component.text(": "))
				.append(Component.text(this.getSquadron().getCrafts().size() + " crafts"))
				.append(Component.text(", "))
				.append(I18nSupport.getInternationalisedComponent("Contact - Range"))
				.append(Component.text(": "))
				.append(Component.text((int) Math.sqrt(distSquared)))
				.append(Component.text(" "))
				.append(I18nSupport.getInternationalisedComponent("Contact - To The"))
				.append(Component.text(" "));

		BlockFace direction = ContactsManager.getDirection(baseCenter, targetCenter);
		notification = notification.append(I18nSupport.getInternationalisedComponent("Contact - Direction - " + direction.name()));

		notification = notification.append(Component.text("."));
		return notification;
	}

	@Override
	public boolean contactPickedUpBy(Craft craft) {
		if (craft == this.getParent()) {
			return false;
		}
		if (craft instanceof SquadronCraft sc) {
			return sc.getSquadron() != this.getSquadron();
		}
		List<UUID> contactsOfDetector = craft.getDataTag(Craft.CONTACTS);
		for (SquadronCraft squadronCraft : this.getSquadron().getCrafts()) {
			if (squadronCraft == this) {
				continue;
			}
			// Already picked up this squadron!
			if (contactsOfDetector.contains(squadronCraft.getUUID())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public MovecraftLocation getContactLocation() {
		return this.squadron.getSquadronCenter();
	}

	@Override
	public double getDetectionMultiplier(boolean underwater, MovecraftWorld movecraftWorld) {
		NamespacedKey key = underwater ? CraftType.PER_WORLD_UNDERWATER_DETECTION_MULTIPLIER : CraftType.PER_WORLD_DETECTION_MULTIPLIER;
		if (this.getSquadron().getLeadCraft() != null) {
			return (double)this.getSquadron().getLeadCraft().getType().getPerWorldProperty(key, movecraftWorld);
		} else {
			return (double)this.getType().getPerWorldProperty(key, movecraftWorld);
		}
	}

	@Override
	public double getSpeed() {
		if (this == this.getSquadron().getLeadCraft() || this.getSquadron().getLeadCraft() == null) {
			return super.getSpeed();
		}
		return this.getSquadron().getLeadCraft().getSpeed();
	}

	public int getTickCooldown() {
		if (this == this.getSquadron().getLeadCraft() || this.getSquadron().getLeadCraft() == null) {
			return super.getTickCooldown();
		}

		final Craft leadCraft = this.getSquadron().getLeadCraft();


		if (this instanceof SinkingCraft) {
			return this.type.getIntProperty(CraftType.SINK_RATE_TICKS);
		} else {
			Counter<Material> materials = (Counter)this.getDataTag(Craft.MATERIALS);
			int chestPenalty = 0;
			Material m;
			if (!materials.isEmpty()) {
				for(Iterator var3 = Tags.CHESTS.iterator(); var3.hasNext(); chestPenalty += materials.get(m)) {
					m = (Material)var3.next();
				}
			}

			chestPenalty *= (int)this.type.getDoubleProperty(CraftType.CHEST_PENALTY);
			if (!this.getCruising()) {
				return ((Integer)this.type.getPerWorldProperty(CraftType.PER_WORLD_TICK_COOLDOWN, this.w) + chestPenalty) * (this.type.getBoolProperty(CraftType.GEAR_SHIFTS_AFFECT_TICK_COOLDOWN) ? this.getCurrentGear() : 1);
			} else if (this.getCruiseDirection() != CruiseDirection.UP && this.getCruiseDirection() != CruiseDirection.DOWN) {
				int cruiseTickCooldown = (Integer)this.type.getPerWorldProperty(CraftType.PER_WORLD_CRUISE_TICK_COOLDOWN, this.w);
				double count;
				if (leadCraft.getType().getDoubleProperty(CraftType.DYNAMIC_FLY_BLOCK_SPEED_FACTOR) == 0.0) {
					if (leadCraft.getType().getDoubleProperty(CraftType.DYNAMIC_LAG_SPEED_FACTOR) != 0.0 && leadCraft.getType().getDoubleProperty(CraftType.DYNAMIC_LAG_POWER_FACTOR) != 0.0 && !(Math.abs(leadCraft.getType().getDoubleProperty(CraftType.DYNAMIC_LAG_POWER_FACTOR)) > 1.0)) {
						int cruiseSkipBlocks = (Integer)this.type.getPerWorldProperty(CraftType.PER_WORLD_CRUISE_SKIP_BLOCKS, this.w);
						Logger var10000;
						double var10001;
						if (this.stats.getCount() == 0) {
							if (Settings.Debug) {
								Bukkit.getLogger().info("First cruise: ");
								Bukkit.getLogger().info("\t- Skip: " + cruiseSkipBlocks);
								Bukkit.getLogger().info("\t- Tick: " + cruiseTickCooldown);
								Bukkit.getLogger().info("\t- MinSpeed: " + leadCraft.getType().getDoubleProperty(CraftType.DYNAMIC_LAG_MIN_SPEED));
								var10000 = Bukkit.getLogger();
								int var15 = this.type.getBoolProperty(CraftType.GEAR_SHIFTS_AFFECT_TICK_COOLDOWN) ? this.getCurrentGear() : 1;
								var10000.info("\t- Gearshifts: " + var15);
								var10000 = Bukkit.getLogger();
								var10001 = 20.0 * (((double)cruiseSkipBlocks + 1.0) / leadCraft.getType().getDoubleProperty(CraftType.DYNAMIC_LAG_MIN_SPEED));
								int var10002 = this.type.getBoolProperty(CraftType.GEAR_SHIFTS_AFFECT_TICK_COOLDOWN) ? this.getCurrentGear() : 1;
								var10000.info("\t- Cooldown: " + (int)Math.round(var10001 * (double)var10002));
							}

							return (int)Math.round(20.0 * (((double)cruiseSkipBlocks + 1.0) / leadCraft.getType().getDoubleProperty(CraftType.DYNAMIC_LAG_MIN_SPEED)) * (double)(leadCraft.getType().getBoolProperty(CraftType.GEAR_SHIFTS_AFFECT_TICK_COOLDOWN) ? this.getCurrentGear() : 1));
						} else {
							if (Settings.Debug) {
								Bukkit.getLogger().info("Cruise: ");
								Bukkit.getLogger().info("\t- Skip: " + cruiseSkipBlocks);
								Bukkit.getLogger().info("\t- Tick: " + cruiseTickCooldown);
								Bukkit.getLogger().info("\t- SpeedFactor: " + leadCraft.getType().getDoubleProperty(CraftType.DYNAMIC_LAG_SPEED_FACTOR));
								Bukkit.getLogger().info("\t- PowerFactor: " + leadCraft.getType().getDoubleProperty(CraftType.DYNAMIC_LAG_POWER_FACTOR));
								Bukkit.getLogger().info("\t- MinSpeed: " + leadCraft.getType().getDoubleProperty(CraftType.DYNAMIC_LAG_MIN_SPEED));
								var10000 = Bukkit.getLogger();
								var10001 = this.getMeanCruiseTime();
								var10000.info("\t- CruiseTime: " + var10001 * 1000.0 + "ms");
							}

							count = 20.0 * ((double)cruiseSkipBlocks + 1.0) / (double)((float)cruiseTickCooldown);
							count -= leadCraft.getType().getDoubleProperty(CraftType.DYNAMIC_LAG_SPEED_FACTOR) * Math.pow(this.getMeanCruiseTime() * 1000.0, leadCraft.getType().getDoubleProperty(CraftType.DYNAMIC_LAG_POWER_FACTOR));
							count = Math.max(leadCraft.getType().getDoubleProperty(CraftType.DYNAMIC_LAG_MIN_SPEED), count);
							return (int)Math.round(20.0 * ((double)cruiseSkipBlocks + 1.0) / count) * (leadCraft.getType().getBoolProperty(CraftType.GEAR_SHIFTS_AFFECT_TICK_COOLDOWN) ? this.getCurrentGear() : 1);
						}
					} else {
						return (cruiseTickCooldown + chestPenalty) * (leadCraft.getType().getBoolProperty(CraftType.GEAR_SHIFTS_AFFECT_TICK_COOLDOWN) ? this.getCurrentGear() : 1);
					}
				} else if (materials.isEmpty()) {
					return ((Integer)leadCraft.getType().getPerWorldProperty(CraftType.PER_WORLD_TICK_COOLDOWN, this.w) + chestPenalty) * (leadCraft.getType().getBoolProperty(CraftType.GEAR_SHIFTS_AFFECT_TICK_COOLDOWN) ? this.getCurrentGear() : 1);
				} else {
					EnumSet<Material> flyBlockMaterials = this.type.getMaterialSetProperty(CraftType.DYNAMIC_FLY_BLOCK);
					count = 0.0;

					Material m2;
					for(Iterator var7 = flyBlockMaterials.iterator(); var7.hasNext(); count += (double)materials.get(m2)) {
						m2 = (Material)var7.next();
					}

					double ratio = count / (double)this.hitBox.size();
					double speed = leadCraft.getType().getDoubleProperty(CraftType.DYNAMIC_FLY_BLOCK_SPEED_FACTOR) * 1.5 * (ratio - 0.5) + 20.0 / (double)cruiseTickCooldown + 1.0;
					return Math.max((int)Math.round(20.0 * (double)((Integer)leadCraft.getType().getPerWorldProperty(CraftType.PER_WORLD_CRUISE_SKIP_BLOCKS, this.w) + 1) / speed) * (leadCraft.getType().getBoolProperty(CraftType.GEAR_SHIFTS_AFFECT_TICK_COOLDOWN) ? this.getCurrentGear() : 1), 1);
				}
			} else {
				return ((Integer)leadCraft.getType().getPerWorldProperty(CraftType.PER_WORLD_VERT_CRUISE_TICK_COOLDOWN, this.w) + chestPenalty) * (leadCraft.getType().getBoolProperty(CraftType.GEAR_SHIFTS_AFFECT_TICK_COOLDOWN) ? this.getCurrentGear() : 1);
			}
		}
	}

	final static List<UUID> EMPTY_CONTACT_LIST = new ArrayList<>();

	@Override
	public @NotNull List<UUID> getContactUUIDs(Craft self, @NotNull Set<Craft> candidates) {
		if (this == this.getSquadron().getLeadCraft() || this.getSquadron().getLeadCraft() == null) {
			return ContactProvider.super.getContactUUIDs(self, candidates);
		}
		return EMPTY_CONTACT_LIST;
	}

	@Override
	public double getMeanCruiseTime() {
		return stats.getRecentAverage();
	}

	@Override
	public void addCruiseTime(float cruiseTime) {
		stats.accept(cruiseTime);
	}

}
