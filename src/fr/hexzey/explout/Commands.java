package fr.hexzey.explout;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor
{
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args)
	{
		if(sender instanceof Player)
		{
			Player player = (Player)sender;
			
			if (cmd.getName().equalsIgnoreCase("explout"))
			{
				if(args.length >= 1)
				{
					if(args[0].equalsIgnoreCase("start"))
					{
						// un admin souhaite commencer une partie
						// 1. verifier qu'aucune partie n'est deja en cours
						if(Main.currentStatus != ArenaStatus.Waiting)
						{
							player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_HURT, 1.0f, 1.0f);
							player.sendMessage(Main.prefix + ChatColor.RED + "Une partie est deja en cours !");
							return true;
						}
						// 2. verifier qu'il y a assez de joueurs
						if( (Main.players.size() - Main.spectators.size()) < 1)
						{
							player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_HURT, 1.0f, 1.0f);
							player.sendMessage(Main.prefix + ChatColor.RED + "Il n'y a pas assez de joueurs !");
							return true;
						}
						// 3. lorsque tout est correct, ajouter les joueurs dans l'arena et commencer la partie
						ArrayList<Player> playerList = new ArrayList<Player>(Main.players);
						for(Player spectator : Main.spectators)
						{
							playerList.remove(spectator);
						}
						Main.arena.setPlayers(new ArrayList<Player>(playerList));
						Main.arena.StartGame();
						
						player.sendMessage(Main.prefix + ChatColor.GREEN + "Demarrage de la partie... !");
						return true;
					}
				}
			}
			
			if(cmd.getName().equalsIgnoreCase("spec"))
			{
				if(Main.spectators.contains(player))
				{
					Main.spectators.remove(player);
					player.sendMessage(Main.prefix + ChatColor.AQUA + "Vous etes desormais enregistre en tant que joueur.");
					if(Main.currentStatus == ArenaStatus.Waiting)
					{
						player.setGameMode(GameMode.SURVIVAL);
						player.teleport(Main.waitingRoomLocation);
					}
					player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
				}
				else
				{
					if(Main.arena.getPlayers().contains(player))
					{
						player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_HURT, 1.0f, 1.0f);
						player.sendMessage(Main.prefix + ChatColor.RED + "Vous ne pouvez pas devenir spectateur en pleine partie.");
					}
					else
					{
						Main.spectators.add(player);
						player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
						player.sendMessage(Main.prefix + ChatColor.AQUA + "Vous etes desormais enregistre en tant que spectateur.");
						player.setGameMode(GameMode.SPECTATOR);
					}
				}
			}
		}
		return false;
	}
}