package fr.hexzey.explout;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;

public class Main extends JavaPlugin
{
	public static Plugin plugin;
	
	public static Location waitingRoomLocation;
	
	public static ArenaStatus currentStatus;
	public static Arena arena;
	
	public static ArrayList<Player> players;
	public static ArrayList<Player> spectators;
	
	public static HashMap<Player, String> playerClasses;
	
	public static ArrayList<Player> freezedPlayers;
	
	public static String prefix = ChatColor.BOLD + "" + ChatColor.WHITE + "[" + ChatColor.GOLD + "ExplOut" + ChatColor.WHITE + "]" + ChatColor.RESET + " ";
	
	public static Settings settings;
	
	// code execute au demarrage du plugin
	public void onEnable()
	{
		plugin = this;
		// 1. enregistrement des events
		getServer().getPluginManager().registerEvents(new Events(), this);
		// 2. enregistrement des commands
		getCommand("explout").setExecutor(new Commands());
		getCommand("spec").setExecutor(new Commands());
		
		settings = new Settings();
		
		currentStatus = ArenaStatus.Waiting;
		
		arena = null;
		FileManager.LoadArenas();
		
		waitingRoomLocation = null;
		FileManager.LoadWaitingRoom();
		
		players = new ArrayList<Player>();
		spectators = new ArrayList<Player>();
		playerClasses = new HashMap<Player, String>();
		freezedPlayers = new ArrayList<Player>();
		
		for(Player player : Bukkit.getOnlinePlayers())
		{
			player.setGameMode(GameMode.SURVIVAL);
			player.teleport(Main.waitingRoomLocation);
			Main.UpdateWaitingInventory(player); // afficher l'inventaire de selection de classe
			players.add(player);
		}
	}
	
	// code execute avant l'arret du plugin
	public void onDisable()
	{
		for(Player player : Bukkit.getOnlinePlayers())
		{
			Main.arena.getBossBar().removePlayer(player);
			player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		}
	}
	
	public static void SetPlayerClass(Player _player, String _className)
	{
		// il est impossible de changer de classe en pleine partie
		if(Main.currentStatus == ArenaStatus.Running) { return; }
		Main.playerClasses.put(_player, _className);
	}
	
	public static void OpenClassSelectMenu(Player _player)
	{
		Inventory menu = Bukkit.createInventory(null, 9, "Selectionner une classe");
		
		// 0 : BARRIER = AUCUNE CLASSE
		ItemBuilder builder = new ItemBuilder(Material.BARRIER).displayName(ChatColor.WHITE + "Aucune").lore("Jouer avec aucune classe");
		if(Main.playerClasses.containsKey(_player) == false)
		{
			menu.setItem(0, builder.buildGlow());
		}
		else
		{
			menu.setItem(0, builder.build());
		}
		
		
		// 1 : SLIME_BALL = CLASSE SLIME
		builder = new ItemBuilder(Material.SLIME_BALL).displayName(ChatColor.WHITE + "Slime");
		builder.addLine("Cree une plateforme sous vos pieds qui vous empeche");
		builder.addLine("de tomber et vous renvoie en l'air");
		if(Main.playerClasses.get(_player) == "Slime") { menu.setItem(1, builder.buildGlow()); }
		else { menu.setItem(1, builder.build()); }
		
		// 2 : IRON_INGOT = CLASSE GOLEM
		builder = new ItemBuilder(Material.IRON_INGOT).displayName(ChatColor.WHITE + "Golem");
		builder.lore("Vous equipe d'une armure qui reduit l'intensite des kb subis");
		if(Main.playerClasses.get(_player) == "Golem") { menu.setItem(2, builder.buildGlow()); }
		else { menu.setItem(2, builder.build()); }
		
		// 3 : GREEN_DYE = CLASSE CACTUS
		builder = new ItemBuilder(Material.GREEN_DYE).displayName(ChatColor.WHITE + "Cactus");
		builder.lore("Vous renvoyez les kb subis a vos adversaires");
		if(Main.playerClasses.get(_player) == "Cactus") { menu.setItem(3, builder.buildGlow()); }
		else { menu.setItem(3, builder.build()); }
		
		// 4 : ENDER_EYE = CLASSE ENDERMAN
		builder = new ItemBuilder(Material.ENDER_EYE).displayName(ChatColor.WHITE + "Enderman");
		builder.addLine("Vous inversez vos positions avec le joueur le plus proche");
		builder.addLine("Attention: la hauteur (axe y) n'est pas transférée !");
		if(Main.playerClasses.get(_player) == "Enderman") { menu.setItem(4, builder.buildGlow()); }
		else { menu.setItem(4, builder.build()); }
		
		_player.playSound(_player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1.0f, 1.0f);
		_player.openInventory(menu);
	}
	
	public static void UpdateWaitingInventory(Player _player)
	{
		_player.getInventory().clear();
		
		ItemBuilder builder = new ItemBuilder(Material.BARRIER);
		
		if(Main.playerClasses.containsKey(_player)) // le joueur possede deja une classe
		{
			String playerClass = Main.playerClasses.get(_player);
			if(playerClass == "Slime") { builder.material(Material.SLIME_BALL); }
			else if(playerClass == "Golem") { builder.material(Material.IRON_INGOT); }
			else if(playerClass == "Cactus") { builder.material(Material.GREEN_DYE); }
			else if(playerClass == "Enderman") { builder.material(Material.ENDER_EYE); }
		}
		builder.displayName(ChatColor.LIGHT_PURPLE + "Selectionner une classe");
		_player.getInventory().setItem(0, builder.buildGlow());
	}
}
