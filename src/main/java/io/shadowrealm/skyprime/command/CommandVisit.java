package io.shadowrealm.skyprime.command;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import io.shadowrealm.skyprime.SkyMaster;
import io.shadowrealm.skyprime.storage.Island;
import io.shadowrealm.skyprime.storage.Visibility;
import mortar.api.sched.S;
import mortar.bukkit.command.MortarCommand;
import mortar.bukkit.command.MortarSender;

public class CommandVisit extends MortarCommand
{
	public CommandVisit()
	{
		super("visit", "goto");
	}

	@Override
	public boolean handle(MortarSender sender, String[] args)
	{
		if(args.length != 1)
		{
			sender.sendMessage("/sky visit <player>");
		}

		@SuppressWarnings("deprecation")
		OfflinePlayer op = Bukkit.getOfflinePlayer(args[0]);
		if(op.hasPlayedBefore())
		{
			UUID uuid = SkyMaster.getStorageEngine().getIslandIdByOwner(op.getUniqueId());

			if(SkyMaster.hasIsland(uuid))
			{
				SkyMaster.ensureIslandLoaded(uuid);
				Island isx = SkyMaster.getIsland(uuid).getIsland();

				if(isx.getVisibility().equals(Visibility.PUBLIC) || sender.hasPermission("sky.bypass") || isx.getMembers().contains(sender.player().getUniqueId()))
				{
					SkyMaster.ensureIslandLoaded(uuid);
					SkyMaster.getIsland(uuid).warp(sender.player());

					new S(20)
					{
						@Override
						public void run()
						{
							if(!isx.getVisibility().equals(Visibility.PUBLIC))
							{
								sender.sendMessage("Welcome to " + args[0] + "'s Island!");
							}

							else
							{
								sender.sendMessage("Welcome to " + args[0] + "'s PRIVATE Island! (bypassed).");
							}
						}
					};
				}

				else
				{
					sender.sendMessage(args[0] + " is not accepting visitors at this time.");
				}
			}

			else
			{
				sender.sendMessage(args[0] + " does not have an island.");
			}
		}

		else
		{
			sender.sendMessage(args[0] + " has never been on this server.");
		}

		return true;
	}
}