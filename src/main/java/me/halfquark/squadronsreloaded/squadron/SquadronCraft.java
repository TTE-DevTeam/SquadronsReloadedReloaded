package me.halfquark.squadronsreloaded.squadron;

import net.countercraft.movecraft.CruiseDirection;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.craft.*;
import net.countercraft.movecraft.craft.type.*;
import net.countercraft.movecraft.craft.type.property.BlockSetProperty;
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
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.logging.Logger;

public class SquadronCraft extends BaseCraft implements SubCraft, PilotedCraft, ContactProvider {
	
	private UUID pilotUUID;
	private WeakReference<Player> pilot;
	private Squadron squadron;

	@NotNull
	TimingData stats = new TimingData();
	
	public SquadronCraft(@Nonnull TypeSafeCraftType type, @Nonnull World world, @Nonnull Player p, @Nonnull Squadron sq) {
		super(type, world);
		pilotUUID = p.getUniqueId();
		pilot = new WeakReference<>(p);
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

	@Nullable
	public Player getSquadronPilot() {
		if (this.pilot.get() == null) {
			this.pilot = new WeakReference<> (Bukkit.getPlayer(this.getPilotUUID()));
		}
		return this.pilot.get();
	}
	public Squadron getSquadron() {return squadron;}
	
	public boolean isLead() {
		if (squadron.getLeadCraft() == null) {
			return false;
		}
		return squadron.getLeadCraft().equals(this);
	}

	@Override
	public @NotNull Player getPilot() {
		if (this.getSquadron().getPilot() != null) {
			return this.getSquadron().getPilot();
		}
		return this.getSquadronPilot();
	}

	@Override
	public @NotNull UUID getPilotUUID() {
		return this.pilotUUID;
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
			name = name.append(Component.text(this.getCraftProperties().getName()));
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

	// Copied from AbstractInformationSign
	protected static final Style STYLE_COLOR_GREEN = Style.style(TextColor.color(0, 255, 0));
	protected static final Style STYLE_COLOR_YELLOW = Style.style(TextColor.color(255, 255, 0));
	protected static final Style STYLE_COLOR_RED = Style.style(TextColor.color(255, 0, 0));
	protected static final Style STYLE_COLOR_WHITE = Style.style(TextColor.color(255, 255, 255));

	// Copied from ContactSign
	protected final int MAX_DISTANCE_COLOR_RED = 64 * 64;
	protected final int MAX_DISTANCE_COLOR_YELLOW = 128 * 128;

	@Override
	public Component getContactsLine(Craft detector) {
		MovecraftLocation baseCenter = detector.getHitBox().getMidPoint();
		MovecraftLocation targetCenter = this.getContactLocation();
		int distanceSquared = baseCenter.distanceSquared(targetCenter);

		String craftTypeName = "Squadron";
		if (this.getSquadron().getName() != null && !this.getSquadron().getName().isBlank()) {
			craftTypeName = this.getSquadron().getName();
		}
		// TODO: Colored prefix for squadrons
		if (craftTypeName.length() > 9)
			craftTypeName = craftTypeName.substring(0, 7);

		Style style = STYLE_COLOR_GREEN;
		if (distanceSquared <= MAX_DISTANCE_COLOR_RED) {
			style = STYLE_COLOR_RED;
		}
		else if (distanceSquared <= MAX_DISTANCE_COLOR_YELLOW) {
			style = STYLE_COLOR_YELLOW;
		}

		style = style.decorate(TextDecoration.ITALIC);

		Component result = Component.text(craftTypeName + " ").style(style);

		double distance = Math.sqrt(distanceSquared);
		distance = distance / 1000;

		String directionStr = "" + String.format("%.2f", distance);
		final BlockFace direction = ContactsManager.getDirection(baseCenter, targetCenter);
		directionStr = directionStr + " " + ContactsManager.getDirectionAppreviation(direction);
		result = result.append(Component.text(directionStr).style(STYLE_COLOR_WHITE));
		String tmp = PlainTextComponentSerializer.plainText().serialize(result);
		return result;
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
		PropertyKey<PerWorldData<Double>> key = underwater ? PropertyKeys.UNDERWATER_DETECTION_MULTIPLIER : PropertyKeys.DETECTION_MULTIPLIER;
		double sum = 0;
		int counter = 0;
		for (SquadronCraft sc : this.getSquadron().getCrafts()) {
			counter++;
			sum += sc.getCraftProperties().get(key, movecraftWorld);
		}
		return sum / ((double)counter);
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
			return this.getCraftProperties().get(PropertyKeys.SINK_RATE_TICKS);
		} else {
			Counter<NamespacedKey> materials = (Counter)this.getDataTag(Craft.BLOCKS);
			int chestPenalty = 0;
			Material m;
			if (!materials.isEmpty()) {
				for(Iterator var3 = Tags.CHESTS.iterator(); var3.hasNext(); chestPenalty += materials.get(m.getKey())) {
					m = (Material)var3.next();
				}
			}

			chestPenalty *= this.getCraftProperties().get(PropertyKeys.CHEST_PENALTY);
			if (!this.getCruising()) {
				return (this.getCraftProperties().get(PropertyKeys.TICK_COOLDOWN, this.w) + chestPenalty) * (this.getCraftProperties().get(PropertyKeys.GEAR_SHIFT_AFFECT_TICK_COOLDOWN) ? this.getCurrentGear() : 1);
			} else if (this.getCruiseDirection() != CruiseDirection.UP && this.getCruiseDirection() != CruiseDirection.DOWN) {
				int cruiseTickCooldown = this.getCraftProperties().get(PropertyKeys.CRUISE_TICK_COOLDOWN, this.w);
				double count;
				if (leadCraft.getCraftProperties().get(PropertyKeys.DYNAMIC_FLY_BLOCK_SPEED_FACTOR) == 0.0) {
					if (leadCraft.getCraftProperties().get(PropertyKeys.DYNAMIC_LAG_SPEED_FACTOR) != 0.0 && leadCraft.getCraftProperties().get(PropertyKeys.DYNAMIC_LAG_POWER_FACTOR) != 0.0 && !(Math.abs(leadCraft.getCraftProperties().get(PropertyKeys.DYNAMIC_LAG_POWER_FACTOR)) > 1.0)) {
						int cruiseSkipBlocks = this.getCraftProperties().get(PropertyKeys.CRUISE_SKIP_BLOCKS, this.w);
						Logger var10000;
						double var10001;
						if (this.stats.getCount() == 0) {
							if (Settings.Debug) {
								Bukkit.getLogger().info("First cruise: ");
								Bukkit.getLogger().info("\t- Skip: " + cruiseSkipBlocks);
								Bukkit.getLogger().info("\t- Tick: " + cruiseTickCooldown);
								Bukkit.getLogger().info("\t- MinSpeed: " + leadCraft.getCraftProperties().get(PropertyKeys.DYNAMIC_LAG_MIN_SPEED));
								var10000 = Bukkit.getLogger();
								int var15 = this.getCraftProperties().get(PropertyKeys.GEAR_SHIFT_AFFECT_TICK_COOLDOWN) ? this.getCurrentGear() : 1;
								var10000.info("\t- Gearshifts: " + var15);
								var10000 = Bukkit.getLogger();
								var10001 = 20.0 * (((double)cruiseSkipBlocks + 1.0) / leadCraft.getCraftProperties().get(PropertyKeys.DYNAMIC_LAG_MIN_SPEED));
								int var10002 = this.getCraftProperties().get(PropertyKeys.GEAR_SHIFT_AFFECT_TICK_COOLDOWN) ? this.getCurrentGear() : 1;
								var10000.info("\t- Cooldown: " + (int)Math.round(var10001 * (double)var10002));
							}

							return (int)Math.round(20.0 * (((double)cruiseSkipBlocks + 1.0) / leadCraft.getCraftProperties().get(PropertyKeys.DYNAMIC_LAG_MIN_SPEED)) * (double)(leadCraft.getCraftProperties().get(PropertyKeys.GEAR_SHIFT_AFFECT_TICK_COOLDOWN) ? this.getCurrentGear() : 1));
						} else {
							if (Settings.Debug) {
								Bukkit.getLogger().info("Cruise: ");
								Bukkit.getLogger().info("\t- Skip: " + cruiseSkipBlocks);
								Bukkit.getLogger().info("\t- Tick: " + cruiseTickCooldown);
								Bukkit.getLogger().info("\t- SpeedFactor: " + leadCraft.getCraftProperties().get(PropertyKeys.DYNAMIC_LAG_SPEED_FACTOR));
								Bukkit.getLogger().info("\t- PowerFactor: " + leadCraft.getCraftProperties().get(PropertyKeys.DYNAMIC_LAG_POWER_FACTOR));
								Bukkit.getLogger().info("\t- MinSpeed: " + leadCraft.getCraftProperties().get(PropertyKeys.DYNAMIC_LAG_MIN_SPEED));
								var10000 = Bukkit.getLogger();
								var10001 = this.getMeanCruiseTime();
								var10000.info("\t- CruiseTime: " + var10001 * 1000.0 + "ms");
							}

							count = 20.0 * ((double)cruiseSkipBlocks + 1.0) / (double)((float)cruiseTickCooldown);
							count -= leadCraft.getCraftProperties().get(PropertyKeys.DYNAMIC_LAG_SPEED_FACTOR) * Math.pow(this.getMeanCruiseTime() * 1000.0, leadCraft.getCraftProperties().get(PropertyKeys.DYNAMIC_LAG_POWER_FACTOR));
							count = Math.max(leadCraft.getCraftProperties().get(PropertyKeys.DYNAMIC_LAG_MIN_SPEED), count);
							return (int)Math.round(20.0 * ((double)cruiseSkipBlocks + 1.0) / count) * (leadCraft.getCraftProperties().get(PropertyKeys.GEAR_SHIFT_AFFECT_TICK_COOLDOWN) ? this.getCurrentGear() : 1);
						}
					} else {
						return (cruiseTickCooldown + chestPenalty) * (leadCraft.getCraftProperties().get(PropertyKeys.GEAR_SHIFT_AFFECT_TICK_COOLDOWN) ? this.getCurrentGear() : 1);
					}
				} else if (materials.isEmpty()) {
					return (leadCraft.getCraftProperties().get(PropertyKeys.TICK_COOLDOWN, this.w) + chestPenalty) * (leadCraft.getCraftProperties().get(PropertyKeys.GEAR_SHIFT_AFFECT_TICK_COOLDOWN) ? this.getCurrentGear() : 1);
				} else {
					BlockSetProperty flyBlockMaterials = this.getCraftProperties().get(PropertyKeys.DYNAMIC_FLY_BLOCKS);
					count = 0.0;

					NamespacedKey m2;
					for(Iterator<NamespacedKey> var7 = flyBlockMaterials.iterator(); var7.hasNext(); count += (double)materials.get(m2)) {
						m2 = var7.next();
					}

					double ratio = count / (double)this.hitBox.size();
					double speed = leadCraft.getCraftProperties().get(PropertyKeys.DYNAMIC_FLY_BLOCK_SPEED_FACTOR) * 1.5 * (ratio - 0.5) + 20.0 / (double)cruiseTickCooldown + 1.0;
					return Math.max((int)Math.round(20.0 * (double)(leadCraft.getCraftProperties().get(PropertyKeys.CRUISE_SKIP_BLOCKS, this.w) + 1) / speed) * (leadCraft.getCraftProperties().get(PropertyKeys.GEAR_SHIFT_AFFECT_TICK_COOLDOWN) ? this.getCurrentGear() : 1), 1);
				}
			} else {
				return (leadCraft.getCraftProperties().get(PropertyKeys.VERT_CRUISE_TICK_COOLDOWN, this.w) + chestPenalty) * (leadCraft.getCraftProperties().get(PropertyKeys.GEAR_SHIFT_AFFECT_TICK_COOLDOWN) ? this.getCurrentGear() : 1);
			}
		}
	}

	final static List<UUID> EMPTY_CONTACT_LIST = new ArrayList<>();

	@Override
	public @NotNull List<UUID> getContactUUIDs(Craft self, @NotNull Set<Craft> candidates) {
		if (this.getSquadron() == null || this.getSquadron().getCrafts() == null || candidates == null) {
			return EMPTY_CONTACT_LIST;
		}
		candidates.removeAll(this.getSquadron().getCrafts());
		if (this.getSquadron().getCarrier() != null) {
			candidates.remove(this.getSquadron().getCarrier());
		}
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

	@Override
	public void removeUUIDMarkFromTile(TileState tile) {
		// TODO: Properly implement to respect the "effective" parent
		SubCraft.super.removeUUIDMarkFromTile(tile);
	}
}

