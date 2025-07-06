package fr.hexzey.explout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

public class Arena
{
	private Location center;
	private int spawnRadius;
	
	private RandomEvents currentEvent;
	
	private ArrayList<Player> players;
	private HashMap<Player, Integer> playerRounds;
	private HashMap<Player, Integer> playerHooks;
	private HashMap<Player, Float> playerExpToRemove; // quantite d'exp a retirer a chaque execution de Time()
	
	private ArrayList<Player> alivePlayers;
	
	private BossBar bossbar;
	private float knockbackMultiplicator;
	private int timer;
	private float time_before_increase;
	
	public Arena(Location _center, int _spawnRadius)
	{
		this.center = _center;
		this.spawnRadius = _spawnRadius;
		
		this.currentEvent = null;
		
		this.players = new ArrayList<Player>();
		this.alivePlayers = new ArrayList<Player>();
		
		this.playerExpToRemove = new HashMap<Player, Float>();
		
		this.bossbar = Bukkit.createBossBar("text", BarColor.PURPLE, BarStyle.SOLID);
		this.knockbackMultiplicator = 1;
		this.timer = 0;
		this.time_before_increase = 0;
	}
	
	public RandomEvents getCurrentEvent() { return this.currentEvent; }
	
	public Location getCenter() { return this.center; }
	public int getSpawnRadius() { return this.spawnRadius; }
	
	public ArrayList<Location> getSpawnPoints(int playerCount)
	{
		ArrayList<Location> spawnPoints = new ArrayList<Location>();
		Location center = this.getCenter();
		double centerX = center.getX();
		double centerY = center.getY();
		double centerZ = center.getZ();
		
		if(playerCount <= 0) { playerCount = 1; }
		
		int radius = this.getSpawnRadius();
		
		double tour = 2*3.14;
		// 3.14 en reference a pi (le nombre en maths)
		// rappel : 1 tour = 2 pi
		double intervalle =  tour / playerCount;
		
		for(double i=0; i<=tour; i+=intervalle)
		{
			// 1. coordonnees XYZ
			double x = radius*(float)Math.sin(i);
			double z = radius*(float)Math.cos(i);
			
			World world = center.getWorld();
			Location loc = new Location(world, centerX+x, centerY, centerZ+z);
			
			// 2. orientation du la vue (yaw pitch) pour regarder vers le centre
			Vector viewDirection = center.toVector().subtract(loc.toVector());
			loc.setDirection(viewDirection.multiply(1));
			
			spawnPoints.add(loc);
		}
		return spawnPoints;
	}
	
	public ArrayList<Player> getPlayers() { return this.players; }
	public void setPlayers(ArrayList<Player> _players) { this.players = _players; }
	public ArrayList<Player> getAlivePlayers() { return this.alivePlayers; }
	public void setAlivePlayers(ArrayList<Player> _alivePlayers) { this.alivePlayers = _alivePlayers; }
	public void AddPlayerExpBarTimer(Player _player, long duration)
	{
		float expToRemovePerTime = 0.05f/(duration/20);
		this.playerExpToRemove.put(_player, expToRemovePerTime);
		_player.setExp(1);
	}
	public boolean IsUltimateActive(Player player) { return this.playerExpToRemove.containsKey(player); }
	public void ResetPlayerExpBarTimer(Player _player)
	{
		if(this.playerExpToRemove.containsKey(_player))
		{
			this.playerExpToRemove.remove(_player);
			if(_player.isOnline()) { _player.setExp(0); }
		}
	}
	public float getKnockbackMultiplicator() { return this.knockbackMultiplicator; }
	public void setKnockbackMultiplicator(float _value) { if(_value > 0) this.knockbackMultiplicator = _value; }
	public BossBar getBossBar() { return this.bossbar; }
	public Integer GetAvailableHooks(Player _player)
	{
		try { return this.playerHooks.get(_player); }
		catch(Exception e) { return 0; }
	}
	public void UseHook(Player _player)
	{
		this.playerHooks.put(_player, this.playerHooks.get(_player)-1);
		
		Inventory inv = _player.getInventory();
		if(this.playerHooks.get(_player) >= 1)
		{
			ItemStack fishingRod = new ItemBuilder(Material.FISHING_ROD).amount(1).displayName(ChatColor.GREEN + this.playerHooks.get(_player).toString() + " utilisations restantes").build();
			_player.getInventory().setItem(0, fishingRod);
		}
		else
		{
			ItemStack gunpowder = new ItemBuilder(Material.GUNPOWDER).amount(1).displayName(ChatColor.RED + "Grappin indisponible !").build();
			_player.getInventory().setItem(0, gunpowder);
		}
	}
	
	public void KillPlayer(Player _player)
	{
		this.alivePlayers.remove(_player);
		_player.setGameMode(GameMode.SPECTATOR);
		
		Location center2 = this.getCenter().clone();
		center2.setY(center2.getY()+5);
		_player.teleport(center2);
		
		_player.playSound(_player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
		_player.sendMessage(Main.prefix + ChatColor.RED + "Vous etes elimine !");
		
		if(this.alivePlayers.size() <= 1) { this.EndRound(this.alivePlayers.get(0)); }
		
		for(Player player : Bukkit.getOnlinePlayers())
		{
			if(player != _player)
			{
				player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
				player.sendMessage(Main.prefix + ChatColor.RED + _player.getName() + " est elimine !");
			}
		}
	}
	
	// exclure un joueur de la partie en cours (ex: deconnexion)
	public void KickPlayer(Player _player)
	{
		this.ResetPlayerExpBarTimer(_player);
		// retirer le joueur de la liste des joueurs (en vie + en jeu)
		if(this.alivePlayers.contains(_player)) { this.alivePlayers.remove(_player); }
		this.players.remove(_player);
		
		for(Player player : Bukkit.getOnlinePlayers())
		{
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_HURT, 1.0f, 1.0f);
			player.sendMessage(Main.prefix + ChatColor.RED + _player.getName() + " abandonne la partie !");
		}
		
		// securites pour eviter le blocage de la partie (ex: un joueur se retrouve seul)
		// cas 1 : il n'y a plus aucun joueur dans la partie
		if(this.players.size() == 0) { this.EndGame(null); } // la partie se termine sans gagnant
		// cas 2 : il reste un seul joueur dans la partie
		else if(this.players.size() <= 1) { this.EndGame(this.players.get(0)); } // le dernier joueur present gagne la partie
		// cas 3 : il ne reste qu'un seul joueur en vie dans la manche en cours
		else if(this.alivePlayers.size() <= 1) { this.EndRound(this.alivePlayers.get(0)); } // le dernier joueur en vie gagne la manche
	}
	
	public void StartGame()
	{
		// passage du jeu en mode "running" = une partie est en cours
		Main.currentStatus = ArenaStatus.Running;
		
		WorldBorder border = this.center.getWorld().getWorldBorder();
		border.setCenter(0.5d, 0.5d);
		border.setSize(95);
		
		this.playerRounds = new HashMap<Player, Integer>();
		for(Player player : this.players)
		{
			this.playerRounds.put(player, 0);
			if (player.getOpenInventory().getType() == InventoryType.CRAFTING) { player.getOpenInventory().close(); } // securite: fermer les inventaires ouverts
			player.getInventory().clear();
		}
		
		this.bossbar = Bukkit.createBossBar("text", BarColor.PURPLE, BarStyle.SOLID);
		StartRound();
	}
	
	public Scoreboard GenerateScoreboard()
	{
		ScoreboardBuilder sbb = new ScoreboardBuilder("explout", "dummy", "ExplOut");
		sbb.AddLine("Classement :");
		sbb.AddLine("");
		ArrayList<String> rank = new ArrayList<String>();
		for(Player player : this.players)
		{
			int rounds = this.playerRounds.get(player);
			String str = rounds + "%" + player.getName();
			rank.add(str);
		}
		Collections.sort(rank, Collections.reverseOrder());
		for(String ranks : rank)
		{
			String[] content = ranks.split("%");
			String rounds = content[0];
			String playerName = content[1];
			if(Integer.valueOf(rounds) >= Main.settings.roundsToWin-1) { sbb.AddLine(ChatColor.GOLD + playerName + ": " + rounds); }
			else if(Integer.valueOf(rounds) > 0) { sbb.AddLine(ChatColor.WHITE + playerName + ": " + rounds); }
			else { sbb.AddLine(ChatColor.GRAY + playerName + ": " + rounds); }
		}
		return sbb.build();
	}
	
	public void StartRound()
	{
		// sélection d'un événement aléatoire
		Random rand = new Random();
		int chance = rand.nextInt(2); // nombre entre 0 (pas d'event) et 1 (event) = 50% de chance d'avoir un event
		if(chance > 0) {
			// s'il y a un event, on en choisi un au hasard entre tous ceux qui existent
			List<RandomEvents> lesEventsPossibles = Arrays.asList(RandomEvents.values());
			chance = rand.nextInt(lesEventsPossibles.size());
			this.currentEvent = lesEventsPossibles.get(chance);
		} else {
			this.currentEvent = null;
		}
		
		// ajouter les joueurs dans la liste des joueurs en vie
		this.alivePlayers = new ArrayList<Player>(this.players);
		// reinitialiser les grappins
		this.playerHooks = new HashMap<Player, Integer>();
		// reinitialiser le multiplicateur de kb
		this.knockbackMultiplicator = 1;
		this.bossbar.setTitle("Multiplicateur de kb : x1.00");
		if(this.currentEvent == RandomEvents.DoubleKnockback) { // event DoubleKnockback
			this.knockbackMultiplicator = 2;
			this.bossbar.setTitle("Multiplicateur de kb : " + ChatColor.YELLOW + "x2.00");
			}
		// reinitialiser la bossbar
		this.bossbar.setProgress(0);
		this.timer = 0;
		
		// generer le scoreboard a afficher
		Scoreboard scoreboard = this.GenerateScoreboard();
		for(Player player : Bukkit.getOnlinePlayers())
		{
			player.setScoreboard(scoreboard);
			this.bossbar.addPlayer(player);
		}
		
		int i=0;
		// recuperer les points de spawn pour chaque joueur
		ArrayList<Location> spawnpoints = this.getSpawnPoints(this.players.size());
		int completeDelay = 1;
		for(Player player : this.players)
		{
			player.getInventory().clear(); // par securite on vide l'inventaire des joueurs
			player.setGameMode(GameMode.SURVIVAL);
			player.setHealth(20);
			player.teleport(spawnpoints.get(i));
			this.playerHooks.put(player, Main.settings.maxHooks);
			ResetPlayerExpBarTimer(player);
			Main.freezedPlayers.add(player);
			
			int delay = 1;
			
			// afficher l'événement actif s'il y en a un
			if(this.currentEvent != null) {
				String description = "undefined";
				switch(this.currentEvent) {
					case DoubleKnockback:
						description = "Multiplicateur de knockback x2";
						break;
					case OnlyTNT:
						description = "TNT et canne à pêche uniquement";
						break;
					case SnowballMadness:
						description = "Recharge des boules de neige accélérée";
						break;
					case NoUltimate:
						description = "Classes désactivées";
						break;
					case FistOnly:
						description = "TNT et boules de neiges désactivées";
						break;
					default:
						description = "undefined";
						break;
				}
				final String eventDesc = description;
				Bukkit.getScheduler().runTaskLater(Main.plugin, () -> player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f), delay*20L);
				Bukkit.getScheduler().runTaskLater(Main.plugin, () -> player.sendTitle(ChatColor.GOLD + "EVENEMENT ALEATOIRE", ChatColor.WHITE + eventDesc, 1, 50, 1), delay*20L);
				Bukkit.getScheduler().runTaskLater(Main.plugin, () -> player.sendMessage(Main.prefix + ChatColor.GOLD + "EVENEMENT ALEATOIRE"), delay*20L);
				Bukkit.getScheduler().runTaskLater(Main.plugin, () -> player.sendMessage(Main.prefix + ChatColor.WHITE + eventDesc), delay*20L);
				delay += 3;
			}
			
			// afficher le décompte
			Bukkit.getScheduler().runTaskLater(Main.plugin, () -> player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f), delay*20L);
			Bukkit.getScheduler().runTaskLater(Main.plugin, () -> player.sendMessage(Main.prefix + ChatColor.GREEN + "3"), delay*20L);
			Bukkit.getScheduler().runTaskLater(Main.plugin, () -> player.sendTitle(ChatColor.GREEN + "3", null, 1, 20, 1), delay*20L);
			delay ++;
			Bukkit.getScheduler().runTaskLater(Main.plugin, () -> player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f), delay*20L);
			Bukkit.getScheduler().runTaskLater(Main.plugin, () -> player.sendMessage(Main.prefix + ChatColor.GREEN + "2"), delay*20L);
			Bukkit.getScheduler().runTaskLater(Main.plugin, () -> player.sendTitle(ChatColor.GREEN + "2", null, 1, 20, 1), delay*20L);
			delay ++;
			Bukkit.getScheduler().runTaskLater(Main.plugin, () -> player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 2.0f), delay*20L);
			Bukkit.getScheduler().runTaskLater(Main.plugin, () -> player.sendMessage(Main.prefix + ChatColor.GREEN + "1"), delay*20L);
			Bukkit.getScheduler().runTaskLater(Main.plugin, () -> player.sendTitle(ChatColor.GREEN + "1", null, 1, 20, 1), delay*20L);
			delay ++;
			player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, delay*20, 2)); // slowness 2 pendant tout le delay
			
			// donner les items au joueur
			// 0 - canne à pêche
			Bukkit.getScheduler().runTaskLater(Main.plugin, () -> Main.freezedPlayers.remove(player), delay*20L);
			ItemStack fishingRod = new ItemBuilder(Material.FISHING_ROD).amount(1).displayName(ChatColor.GREEN + this.playerHooks.get(player).toString() + " utilisations restantes").build();
			Bukkit.getScheduler().runTaskLater(Main.plugin, () -> player.getInventory().setItem(0, fishingRod), delay*20L);
			// 1 - tnt
			if(this.currentEvent != RandomEvents.FistOnly) { // event FistOnly désactive la tnt
				ItemStack tnt = new ItemBuilder(Material.TNT).amount(1).build();
				Bukkit.getScheduler().runTaskLater(Main.plugin, () -> player.getInventory().setItem(1, tnt), delay*20L);
			}
			// 2 - boules de neige
			if(this.currentEvent != RandomEvents.OnlyTNT && this.currentEvent != RandomEvents.FistOnly) { // events OnlyTNT & FistOnly desactivent les boules de neige
				ItemBuilder snowballs = new ItemBuilder(Material.SNOWBALL).amount(Main.settings.snowballStack);
				snowballs.displayName(ChatColor.GREEN + "" + Main.settings.snowballStack + " utilisations avant recharge");
				Bukkit.getScheduler().runTaskLater(Main.plugin, () -> player.getInventory().setItem(2, snowballs.build()), delay*20L);	
			}
			
			// 3 - item des classes
			if(this.currentEvent != RandomEvents.NoUltimate) { // event NoUltimate désactive les ultimates
				if(Main.playerClasses.containsKey(player))
				{
					ItemBuilder skill = new ItemBuilder(Material.STRING);
					skill.displayName(ChatColor.LIGHT_PURPLE + "Competence unique");
					String playerClass = Main.playerClasses.get(player);
					if(this.currentEvent == RandomEvents.NoUltimate) { // désactiver les classes si l'event NoUltimate est en cours
						skill.material(Material.GRAY_DYE);
						skill.displayName(ChatColor.RED + "Compétence unique désactivée");
					} else {
						if(playerClass == "Slime") { skill.material(Material.SLIME_BALL); }
						else if(playerClass == "Golem") { skill.material(Material.IRON_INGOT); }
						else if(playerClass == "Cactus") { skill.material(Material.GREEN_DYE); }
						else if(playerClass == "Enderman") { skill.material(Material.ENDER_EYE); }
					}
					if(this.currentEvent == RandomEvents.FistOnly) { // adapter le slot de l'ultimate selon l'event en cours
						Bukkit.getScheduler().runTaskLater(Main.plugin, () -> player.getInventory().setItem(1, skill.buildGlow()), delay*20L);
					} else if(this.currentEvent == RandomEvents.OnlyTNT) {
						Bukkit.getScheduler().runTaskLater(Main.plugin, () -> player.getInventory().setItem(2, skill.buildGlow()), delay*20L);
					} else {
						Bukkit.getScheduler().runTaskLater(Main.plugin, () -> player.getInventory().setItem(3, skill.buildGlow()), delay*20L);
					}
				}
			}

			Bukkit.getScheduler().runTaskLater(Main.plugin, () -> player.playSound(player.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.0f, 1.0f), delay*20L);
			
			
			i++;
			if(delay > completeDelay) { completeDelay = delay; }
		}
		
		this.time_before_increase = Main.settings.kb_multiplicator_seconds;
		// activer le timer une fois la manche démarée (completeDelay)
		Bukkit.getScheduler().runTaskLater(Main.plugin, () -> this.timer = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable() {
		    public void run() {
		        Time();
		    }}, 1L, 1L), completeDelay*20L); // 1L = 20 actualisations par secondes = 0.05 secondes d'ecart entre chaque
		
	}
	
	public void Time() // code execute chaque seconde
	{
		this.time_before_increase-=0.05;
		
		if(this.time_before_increase <= 0)
		{
			// augmenter le multiplicateur selon la valeur renseignee dans les parametres
			// et reinitialiser la valeur du timer
			this.time_before_increase = Main.settings.kb_multiplicator_seconds;
			float kbIncrementation = Main.settings.kb_multiplicator_incrementation;
			if(this.currentEvent == RandomEvents.DoubleKnockback) { kbIncrementation = kbIncrementation *2; } // event DoubleKnockback
			this.knockbackMultiplicator += kbIncrementation;
			
			// afficher le multiplicateur d'une certaine couleur en fonction de sa valeur
			String valueToPrint = "x" + String.format("%.2f",this.knockbackMultiplicator);
			if(this.knockbackMultiplicator >= 3.0f) { valueToPrint = ChatColor.RED + valueToPrint; } // afficher la valeur en rouge
			else if(this.knockbackMultiplicator >= 2.0f) { valueToPrint = ChatColor.YELLOW + valueToPrint; } // afficher la valeur en jaune
			else { valueToPrint = ChatColor.WHITE + valueToPrint; } // afficher la valeur en blanc
			// mise a jour du texte affiche dans la bossbar
			this.bossbar.setTitle("Multiplicateur de kb : " + valueToPrint);
		}
		
		// actualiser l'avancee de la bossbar en fonction du temps restant avant la prochaine augmentation du multiplicateur de kb
		this.bossbar.setProgress(1-(float)this.time_before_increase/Main.settings.kb_multiplicator_seconds);
		
		//playerExpToRemove
		for(Player player : playerExpToRemove.keySet())
		{
			float currentExp = player.getExp();
			float endExp = currentExp-playerExpToRemove.get(player);
			if(currentExp <= 0 || endExp <= 0) { ResetPlayerExpBarTimer(player); }
			else { player.setExp(endExp); }
		}
	}
	
	public void EndRound(Player _roundWinner)
	{
		// on arrete le timer
		Bukkit.getServer().getScheduler().cancelTask(this.timer);
		
		// on augmente de 1 le nombre de rounds gagnes par le dernier joueur en vie
		int playerRounds = this.playerRounds.get(_roundWinner);
		this.playerRounds.put(_roundWinner, playerRounds+1);
		
		// passer tous les joueurs en mode spectateur
		for(Player player : this.players) { player.setGameMode(GameMode.SPECTATOR); }
		
		// s'il atteint un certain nombre de manches a gagner, il remporte la partie
		if(this.playerRounds.get(_roundWinner) >= Main.settings.roundsToWin)
		{
			// afficher le nom du gagnant de la partie pour tous les joueurs connectes et cacher le scoreboard
			for(Player player : Bukkit.getOnlinePlayers())
			{
				player.getInventory().clear();
				player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
				player.sendMessage(Main.prefix + _roundWinner.getDisplayName() + " remporte la partie !");
				player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
				this.bossbar.removePlayer(player);
				if(player == _roundWinner)
				{
					player.sendTitle(ChatColor.GREEN + "VICTOIRE", ChatColor.GOLD + _roundWinner.getDisplayName() + ChatColor.WHITE + " remporte la partie !", 1, 2*20, 1);
				}
				else
				{
					player.sendTitle(ChatColor.RED + "DEFAITE", ChatColor.GOLD + _roundWinner.getDisplayName() + ChatColor.WHITE + " remporte la partie !", 1, 2*20, 1);
				}
			}
			
			Bukkit.getScheduler().runTaskLater(Main.plugin, () -> this.EndGame(_roundWinner), 4*20L);
		}
		else
		{
			// afficher le nom du gagnant de la manche pour tous les joueurs connectes
			for(Player player : Bukkit.getOnlinePlayers())
			{
				player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
				player.sendMessage(Main.prefix + _roundWinner.getDisplayName() + " a remporte la manche !");
				player.sendTitle(ChatColor.GOLD + _roundWinner.getDisplayName(), ChatColor.WHITE + "a remporte la manche !", 1, 2*20, 1);
			}
			
			// demarrer la manche suivante apres une courte pause
			Bukkit.getScheduler().runTaskLater(Main.plugin, () -> this.StartRound(), 4*20L);
		}
	}
	
	public void EndGame(Player _gameWinner)
	{
		for(Player player : Bukkit.getOnlinePlayers())
		{
			this.bossbar.removePlayer(player);
			//player.setScoreboard(null);
			
			if(Main.spectators.contains(player) == false)
			{
				player.getInventory().clear();
				player.setGameMode(GameMode.SURVIVAL);
				player.teleport(Main.waitingRoomLocation);
			}
			Main.UpdateWaitingInventory(player);
		}
		
		// passage du jeu en mode "waiting" = aucune partie est en cours
		Main.currentStatus = ArenaStatus.Waiting;
		this.alivePlayers = new ArrayList<Player>();
		this.players = new ArrayList<Player>();
	}
}
