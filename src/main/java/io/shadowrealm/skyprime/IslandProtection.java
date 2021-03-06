package io.shadowrealm.skyprime;

import io.shadowrealm.skyprime.storage.Island;
import io.shadowrealm.skyprime.storage.Visibility;
import lombok.Getter;
import lombok.Setter;
import mortar.lang.json.JSONObject;
import org.bukkit.entity.Player;

public class IslandProtection
{

	@Getter
	@Setter
	private Island island;

	@Getter
	@Setter
	private Visibility visibility = Config.ISLAND_PROTECTION_PUBLIC ? Visibility.PUBLIC : Visibility.PRIVATE;

	@Getter
	@Setter
	private boolean publicPickup = Config.ISLAND_PROTECTION_PICKUP;

	/**
	 * Indicates public players being able to build
	 *
	 * Allows players to:
	 * - Place and break blocks
	 * - Place and break hanging entities
	 * - Fill or empty buckets
	 */
	@Setter
	@Getter
	private boolean publicBuild = Config.ISLAND_PROTECTION_BUILD;

	/**
	 * Indicates a player able to interact with blocks
	 */
	@Setter
	@Getter
	private boolean publicInteractBlock = Config.ISLAND_PROTECTION_BLOCK_INTERACT;

	/**
	 * Indicates public players able to use blocks: chests, brewing stands, etc
	 */
	@Setter
	@Getter
	private boolean publicUseBlock = Config.ISLAND_PROTECTION_BLOCK_USE;

	/**
	 * Indicates public players able to PVP
	 */
	@Setter
	@Getter
	private boolean publicPVP = Config.ISLAND_PROTECTION_PVP;

	/**
	 * Indicates public players able to kill entities
	 */
	@Setter
	@Getter
	private boolean publicKill = Config.ISLAND_PROTECTION_MOB_KILL;

	/**
	 * Indicates public players able to damage entities
	 */
	@Setter
	@Getter
	private boolean publicInteractEntity = Config.ISLAND_PROTECTION_MOB_INTERACT;

	public IslandProtection(Island i)
	{
		this.island = i;
	}

	public JSONObject toJSON()
	{
		final JSONObject o = new JSONObject();
		o.put("public", this.isPublicVisibility());
		o.put("public-pickup", this.isPublicPickup());
		o.put("public-build", this.publicBuild);
		o.put("public-interact-block", this.publicInteractBlock);
		o.put("public-use-block", this.publicUseBlock);
		o.put("public-pvp", this.publicPVP);
		o.put("public-kill", this.publicKill);
		o.put("public-interact-entity", this.publicInteractEntity);
		return o;
	}

	public void fromJSON(JSONObject o)
	{
		this.setPublicVisbility(o.has("public") && o.getBoolean("public"));
		this.setPublicPickup(o.has("public-pickup") && o.getBoolean("public-pickup"));

		this.publicBuild = o.has("public-build") && o.getBoolean("public-build");
		this.publicInteractBlock = o.has("public-interact-build") && o.getBoolean("public-interact-build");
		this.publicUseBlock = o.has("public-use-build") && o.getBoolean("public-use-build");
		this.publicPVP = o.has("public-pvp") && o.getBoolean("public-pvp");
		this.publicKill = o.has("public-kill") && o.getBoolean("public-kill");
		this.publicInteractEntity = o.has("public-interact-entity") && o.getBoolean("public-interact-entity");
	}

	public boolean isAdmin(Player p)
	{
		return p.isOp() || SkyPrime.perm.admin.bypass.has(p);
	}

	public boolean isOwner(Player p)
	{
		return island.getOwner().equals(p.getUniqueId());
	}

	public boolean isMember(Player p)
	{
		return island.getMembers().contains(p.getUniqueId());
	}

	public boolean isAllowed(Player p)
	{
		return isMember(p) || isOwner(p) || isAdmin(p);
	}

	public boolean canBuild(Player p)
	{
		return isAllowed(p) || this.publicBuild;
	}

	public boolean canInteractBlock(Player p)
	{
		return isAllowed(p) || this.publicInteractBlock;
	}

	public boolean canUseBlock(Player p)
	{
		return isAllowed(p) || this.publicUseBlock;
	}

	public boolean canPVP(Player p)
	{
		return isAllowed(p) || this.publicPVP;
	}

	public boolean canKill(Player p)
	{
		return isAllowed(p) || this.publicKill;
	}

	public boolean canInteractEntity(Player p)
	{
		return isAllowed(p) || this.publicInteractEntity;
	}

	public boolean canPickup(Player p)
	{
		return isAllowed(p) || this.publicPickup;
	}

	public boolean canVisit(Player player)
	{
		return isAllowed(player) || this.isPublicVisibility();
	}

	public void setPublicVisbility(boolean p)
	{
		this.visibility = p ? Visibility.PUBLIC : Visibility.PRIVATE;
	}

	public boolean isPublicVisibility()
	{
		return this.getVisibility().equals(Visibility.PUBLIC);
	}

}
