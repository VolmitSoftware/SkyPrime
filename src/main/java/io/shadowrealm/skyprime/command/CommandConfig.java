package io.shadowrealm.skyprime.command;

import io.shadowrealm.skyprime.SkyMaster;
import io.shadowrealm.skyprime.storage.Island;
import io.shadowrealm.skyprime.storage.Visibility;
import mortar.bukkit.command.MortarCommand;
import mortar.bukkit.command.MortarSender;
import mortar.lang.collection.GList;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class CommandConfig extends MortarCommand
{
	public CommandConfig()
	{
		super("config", "cfg");
	}

	@Override
	public boolean handle(MortarSender sender, String[] args)
	{
		if(!SkyMaster.hasIsland(sender.player()))
		{
			sender.sendMessage("You cant configure an island you dont have. Use /sky create");
			return true;
		}

		if(!SkyMaster.hasIslandLoaded(sender.player()))
		{
			sender.sendMessage("You cant configure your island. It isnt loaded. Use /sky.");
			return true;
		}

		Island is = SkyMaster.getIsland(sender.player()).getIsland();

		if(args.length == 0)
		{
			sender.sendMessage("name = " + is.getName());
			sender.sendMessage("public = " + is.getProtection().isPublicVisibility());
			sender.sendMessage("despawn.arrow = " + is.getcDespawnArrow());
			sender.sendMessage("despawn.item = " + is.getcDespawnItem());
			sender.sendMessage("merge.xp = " + is.getcMergeXp());
			sender.sendMessage("merge.item = " + is.getcMergeItem());
			sender.sendMessage("hopper.rate = " + is.getcHopperRate());
			sender.sendMessage("hopper.amount = " + is.getcHopperAmount());
			sender.sendMessage("public.pickup = " + is.getProtection().isPublicPickup());
			sender.sendMessage("public.build = " + is.getProtection().isPublicBuild());
			sender.sendMessage("public.block.interact= " + is.getProtection().isPublicInteractBlock());
			sender.sendMessage("public.block.use = " + is.getProtection().isPublicUseBlock());
			sender.sendMessage("public.pvp = " + is.getProtection().isPublicPVP());
			sender.sendMessage("public.mob.kill = " + is.getProtection().isPublicKill());
			sender.sendMessage("public.mob.interact = " + is.getProtection().isPublicInteractEntity());
		}

		else if(args.length == 1)
		{
			sender.sendMessage("/sky config <key> <value>");
		}

		else if(args.length > 1)
		{
			boolean s = true;

			try
			{
				if(args[0].equalsIgnoreCase("despawn.arrow"))
				{
					is.setcDespawnArrow(Integer.valueOf(args[1]));
				}

				else if(args[0].equalsIgnoreCase("name"))
				{
					GList<String> a = new GList<>(args);
					a.remove(0);
					args[1] = a.toString(" ");
					is.setName(args[1]);
				}

				else if(args[0].equalsIgnoreCase("despawn.item"))
				{
					is.setcDespawnItem(Integer.valueOf(args[1]));
				}

				else if(args[0].equalsIgnoreCase("public.pickup"))
				{
					is.getProtection().setPublicPickup(Boolean.valueOf(args[1]));
				}

				else if(args[0].equalsIgnoreCase("public.build"))
				{
					is.getProtection().setPublicBuild(Boolean.valueOf(args[1]));
				}

				else if(args[0].equalsIgnoreCase("public.block.interact"))
				{
					is.getProtection().setPublicInteractBlock(Boolean.valueOf(args[1]));
				}

				else if(args[0].equalsIgnoreCase("public.block.use"))
				{
					is.getProtection().setPublicUseBlock(Boolean.valueOf(args[1]));
				}

				else if(args[0].equalsIgnoreCase("public.pvp"))
				{
					is.getProtection().setPublicPVP(Boolean.valueOf(args[1]));
				}

				else if(args[0].equalsIgnoreCase("public.mob.kill"))
				{
					is.getProtection().setPublicKill(Boolean.valueOf(args[1]));
				}

				else if(args[0].equalsIgnoreCase("public.mob.interact"))
				{
					is.getProtection().setPublicInteractEntity(Boolean.valueOf(args[1]));
				}

				else if(args[0].equalsIgnoreCase("merge.xp"))
				{
					is.setcMergeXp(Double.valueOf(args[1]));
				}

				else if(args[0].equalsIgnoreCase("merge.item"))
				{
					is.setcMergeItem(Double.valueOf(args[1]));
				}

				else if(args[0].equalsIgnoreCase("hopper.rate"))
				{
					is.setcHopperRate(Integer.valueOf(args[1]));
				}

				else if(args[0].equalsIgnoreCase("hopper.amount"))
				{
					is.setcHopperAmount(Integer.valueOf(args[1]));
				}

				else if(args[0].equalsIgnoreCase("public"))
				{
					is.getProtection().setPublicVisbility(Boolean.valueOf(args[1]));
				}

				else
				{
					s = false;
					sender.sendMessage("Unknown Key. Use /sky config");
				}
			}

			catch(Throwable e)
			{
				s = false;
				sender.sendMessage("Invalid value.");
			}

			if(s)
			{
				sender.sendMessage(args[0].toLowerCase() + " set to " + args[1]);
				SkyMaster.getIsland(sender.player()).saveConfig(sender);
			}
		}

		return true;
	}
}
