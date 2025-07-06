package fr.hexzey.explout;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class Events implements Listener
{
	// GESTION DU MOUVEMENT
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		
		// aucune restriction pour les joueurs en spectateur
		if(player.getGameMode() == GameMode.SPECTATOR) return;
		
		// gestion du freeze
		if(Main.freezedPlayers.contains(player))
		{
			// les joueurs ne peuvent pas se deplacer
			// mais peuvent tourner sur eux-memes
			Location start = event.getFrom();
			Location end = event.getTo();
			
			end.setX(start.getX());
			end.setZ(start.getZ());
			
			event.setTo(end);
			return;
		}
		
		// gestion de la sortie du terrain de jeu
		if(event.getTo().getY() <= -32 && event.getPlayer().getGameMode() == GameMode.SURVIVAL)
		{
			// si la partie n'a pas commence, on teleporte le joueur au lobby d'attente
			// sinon si une partie est en cours, on elimine le joueur
			// si aucun cas ne correspond, on ne fait rien
			if(Main.currentStatus == ArenaStatus.Waiting) { player.teleport(Main.waitingRoomLocation); }
			else if(Main.currentStatus == ArenaStatus.Running) { Main.arena.KillPlayer(player); }
			return;
		}
		
		// gestion du rebond du kit slime (slime block)
		// si le joueur se situe sur un bloc de slime, on le propulse en hauteur
		Location playerLoc = player.getLocation();
		Location slimeTargetBlock = player.getLocation().clone();
		slimeTargetBlock.setY(playerLoc.getY()-1);
		if(playerLoc.getWorld().getBlockAt(slimeTargetBlock).getType() == Material.SLIME_BLOCK)
		{
			Vector playerVelocity = player.getVelocity().clone();
			playerVelocity.setY(Main.settings.slime_platform_velocity_Y);
			player.setVelocity(playerVelocity);
			return;
		}
	}
	
	// DESACTIVER LE DROP D'ITEMS POUR LES JOUEURS EN JEU
	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event)
	{
		if(Main.arena.getAlivePlayers().contains(event.getPlayer()) || event.getPlayer().getGameMode() == GameMode.SURVIVAL)
		{
			event.setCancelled(true);
		}
	}
	
	// DESACTIVER LES INTERACTIONS DANS L'INVENTAIRE POUR LES JOUEURS EN JEU
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event)
	{
		if(Main.currentStatus == ArenaStatus.Running && Main.arena.getAlivePlayers().contains((Player)event.getWhoClicked()) && Main.arena.getPlayers().contains((Player)event.getWhoClicked()))
		{
			event.setCancelled(true);
		}
		else
		{
			if(event.getView().getTitle() == "Selectionner une classe") // selection d'une classe par le joueur
			{
				Player player = (Player) event.getWhoClicked();
				ItemStack clickedItem = event.getCurrentItem();
				boolean changedClass = false;
				
				if(clickedItem == null) { return; } // securite anti-crash
				
				if(clickedItem.getType() == Material.BARRIER)
				{
					if(Main.playerClasses.containsKey(player)) { Main.playerClasses.remove(player); }
					changedClass = true;
				}
				else if(clickedItem.getType() == Material.SLIME_BALL)
				{
					Main.playerClasses.put(player, "Slime");
					changedClass = true;
				}
				else if(clickedItem.getType() == Material.IRON_INGOT)
				{
					Main.playerClasses.put(player, "Golem");
					changedClass = true;
				}
				else if(clickedItem.getType() == Material.GREEN_DYE)
				{
					Main.playerClasses.put(player, "Cactus");
					changedClass = true;
				}
				else if(clickedItem.getType() == Material.ENDER_EYE)
				{
					Main.playerClasses.put(player, "Enderman");
					changedClass = true;
				}
				
				if(changedClass == true)
				{
					player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
					if(Main.playerClasses.containsKey(player)) { player.sendMessage(Main.prefix + "Vous etes desormais un " + Main.playerClasses.get(player)); }
					else { player.sendMessage(Main.prefix + "Aucune classe selectionnee."); }
					
					player.getOpenInventory().close();
					Main.UpdateWaitingInventory(player);
				}
			}
		}
	}
	
	// GENERER UNE TNT QUI PEUT EXPLOSER
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if(event.getBlock().getType() == Material.TNT)
		{
			Location loc = event.getBlock().getLocation();
			
			// creer une TNT prete a exploser
			Entity tnt = loc.getWorld().spawn(loc, TNTPrimed.class);
            ((TNTPrimed)tnt).setFuseTicks(10);
            
            event.setCancelled(true);
		}
		
		if(event.getPlayer().getGameMode() == GameMode.SURVIVAL)
		{
			event.setCancelled(true);
		}
	}
	
	// DESACTIVER LES DEGATS DE CHUTE
	@EventHandler
	public void Damage(EntityDamageEvent event)
	{
		if(event.getEntity() instanceof Player)
		{
			Player player = (Player) event.getEntity();
			if(Main.currentStatus == ArenaStatus.Waiting || event.getCause() == DamageCause.FALL)
			{
				event.setCancelled(true);
				return;
			}
			
			// empecher la mort des joueurs (on prefere les passer en mode spectateur)
			if(Main.arena.getAlivePlayers().contains(player) && player.getHealth() <= 0)
			{
				event.setCancelled(true);
				player.setHealth(20);
				Main.arena.KillPlayer(player);
			}
		}
	}
	
	// DESACTIVER LES DEGATS DE LA TNT ( joueurs )
	// ET APPLIQUER UN KNOCKBACK PLUS FORT AU CORPS-A-CORPS
	@EventHandler
	public void PlayerDamage(EntityDamageByEntityEvent event)
	{
		if(Main.currentStatus == ArenaStatus.Waiting) {
			event.setCancelled(true);
			return; // on annule tout si on est en attente dans le lobby
		}
		Entity damager = event.getDamager();
		Entity target = event.getEntity();
		
		// calcul du knockback
		Location damagerLoc = damager.getLocation();
		Location targetLoc = target.getLocation();
		
		Vector knockback = damagerLoc.toVector().subtract(targetLoc.toVector()).normalize().multiply(-1);
		Vector kb_vertical = new Vector(0, 0.75, 0);
		knockback = knockback.add(kb_vertical);
		
		if(target instanceof Player)
		{
			Player targetPlayer = (Player) target;
			
			// tnt = pas de degats
			if(event.getCause() == DamageCause.ENTITY_EXPLOSION) { event.setCancelled(true); }
			else if(event.getCause() == DamageCause.BLOCK_EXPLOSION) { event.setCancelled(true); }
			// si la cible possede la classe GOLEM et que sa competence est active
			ItemStack playerChestplate = targetPlayer.getInventory().getChestplate();
			if(playerChestplate != null && playerChestplate.getType() == Material.IRON_CHESTPLATE)
			{
				knockback = knockback.multiply(Main.settings.golem_kb_reduction_multiplier);
			}
			// si la cible possede la classe CACTUS et que sa competence est active
			ItemStack playerHelmet = targetPlayer.getInventory().getHelmet();
			if(playerHelmet != null && playerHelmet.getType() == Material.CACTUS)
			{
				Vector damagerKnockback = knockback.clone();
				damagerKnockback.multiply(Main.settings.cactus_thorns_kb_multiplier).multiply(-1).setY(0.75);
				if(damager instanceof Snowball)
				{
					Entity sender = (Entity) ((Snowball) damager).getShooter();
					sender.setVelocity(damagerKnockback);
				}
				else { damager.setVelocity(damagerKnockback); }
			}
		}
		event.getEntity().setVelocity(knockback.multiply(Main.settings.close_combat_kb_default).multiply(Main.arena.getKnockbackMultiplicator()));
	}
	
	// DESACTIVER LES DEGATS DE LA TNT ( blocs )
	// ET APPLIQUER UN KNOCKBACK
	@EventHandler
	public void TNTexplosion(EntityExplodeEvent event)
	{
		Entity tnt = event.getEntity();
		event.blockList().clear();
		
		Vector t = tnt.getLocation().toVector();
		List<Entity> targets = tnt.getNearbyEntities(5,5,5);
		
		for(Entity entity : targets)
		{
			Vector e = entity.getLocation().toVector();
			
			Vector knockback = e.subtract(t).normalize();
			knockback = knockback.multiply(Main.settings.explosion_kb_default);
			knockback = knockback.add(new Vector(0, 0.3, 0)); // ajout d'un kb vers le haut (éviter de rendre le sneak des joueurs trop puissant)
			
			if(entity instanceof Player){
				if(Main.playerClasses.get((Player)entity) == "Golem" && Main.arena.IsUltimateActive((Player)entity)) {
					knockback = knockback.multiply(Main.settings.golem_kb_tnt_reduction_multiplier);
				}
			}
			
			entity.setVelocity(knockback.multiply(Main.arena.getKnockbackMultiplicator()));
		}
		
		for(Player p : Bukkit.getOnlinePlayers())
		{
			p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.5f);
		}
		event.setCancelled(true);
	}
	
	// USAGE DE LA CANNE A PECHE
	// (pour grappin)
	@EventHandler
	public void onPlayerFish(PlayerFishEvent event)
	{
		if(event.getState() == PlayerFishEvent.State.REEL_IN || event.getState() == PlayerFishEvent.State.IN_GROUND)
		{
			Player player = event.getPlayer();
			if(Main.arena.GetAvailableHooks(player) <= 0) { return; }
			
			Main.arena.UseHook(player);
			
			Location hookLocation = event.getHook().getLocation();
			Vector velocity = player.getLocation().getDirection().multiply(player.getLocation().distance(hookLocation) / 10);
			player.setVelocity(velocity);
		}
	}
	
	// DESACTIVER LA CASSE DE BLOCS
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		if(event.getPlayer().getGameMode() == GameMode.SURVIVAL) { event.setCancelled(true); }
	}
	
	// DESACTIVER LA FAIM
	@EventHandler
	public void onHungerDeplete(FoodLevelChangeEvent event)
	{
		event.setFoodLevel(20);
	}
	
	// GESTION DES BOULES DE NEIGE
	@EventHandler
	public void onProjectileLaunch(ProjectileLaunchEvent event)
	{
		
		if(event.getEntity().getShooter() instanceof Player)
		{
			Player sender = (Player)event.getEntity().getShooter();
			if(event.getEntity().getType() == EntityType.SNOWBALL)
			{
				event.getEntity().setVelocity(event.getEntity().getVelocity().multiply(1.8));
			}
		}
		
	}
	
	public void UpdateSnowball(Player player)
	{
		ItemBuilder item = new ItemBuilder(player.getInventory().getStorageContents()[2]);
		item.displayName(ChatColor.GREEN + "" + (item.getAmount()-1) + " utilisations avant recharge");
		player.getInventory().setItem(2, item.build());
	}
	
	public void ReloadSnowball(Player player)
	{
		// changer l'item du joueur apres 1 tick (bugfix derniere snowball qui ne se lance pas)
		Bukkit.getScheduler().runTaskLater(Main.plugin, () -> 
		player.getInventory().setItem(
		2, new ItemBuilder(Material.FIREWORK_STAR).amount(1).displayName(ChatColor.RED + "Rechargement...").build())
		, 1L);
		
		// ajouter un nouveau chargeur de snowballs apres 3 secondes sauf si event SnowballMadness
		long reloadTime = Main.settings.snowballReloadTime;
		if(Main.arena.getCurrentEvent() == RandomEvents.SnowballMadness) { reloadTime = 20L; } // 20 ticks = 1 seconde
		Bukkit.getScheduler().runTaskLater(Main.plugin, () -> 
			player.getInventory().setItem(
			2, new ItemBuilder(Material.SNOWBALL).amount(Main.settings.snowballStack).displayName(ChatColor.GREEN + "" + Main.settings.snowballStack + " utilisations avant recharge").build())
			, reloadTime);
	}
	
	public void SetSpecialAbilityUsed(Player player)
	{
		ItemBuilder item = new ItemBuilder(Material.GRAY_DYE);
		item.displayName(ChatColor.RED + "Compétence unique epuisée");
		// adapter le slot selon l'event en cours
		if(Main.arena.getCurrentEvent() == RandomEvents.OnlyTNT) { player.getInventory().setItem(2, item.build()); }
		else if(Main.arena.getCurrentEvent() == RandomEvents.FistOnly) { player.getInventory().setItem(1, item.build()); }
		else { player.getInventory().setItem(3, item.build()); }
	}
	
	// GESTION DES BOULES DE NEIGE
	@EventHandler
	public void onInteractEvent(PlayerInteractEvent event)
	{
		if(event.getItem() == null) { return; } // securite interaction main vide

		Player player = event.getPlayer();
		
		if(event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) // interaction CLIC DROIT
		{
			if(event.getItem().getType() == Material.SNOWBALL) // ITEM = SNOWBALL
			{
				ItemStack currentItem = player.getInventory().getStorageContents()[2];
				if(currentItem == null || currentItem.getAmount() == 1) { ReloadSnowball(player); }
				else { UpdateSnowball(player); }
			}
			if(event.getPlayer().getGameMode() == GameMode.SURVIVAL && Main.currentStatus == ArenaStatus.Waiting) // GESTION DU SELECTEUR DE CLASSES
			{
				if(event.getItem().getType() == Material.BARRIER)
				{
					Main.OpenClassSelectMenu(player);
					event.setCancelled(true);
				}
				else if(event.getItem().getType() == Material.SLIME_BALL)
				{
					Main.OpenClassSelectMenu(player);
					event.setCancelled(true);
				}
				else if(event.getItem().getType() == Material.IRON_INGOT)
				{
					Main.OpenClassSelectMenu(player);
					event.setCancelled(true);
				}
				else if(event.getItem().getType() == Material.GREEN_DYE)
				{
					Main.OpenClassSelectMenu(player);
					event.setCancelled(true);
				}
				else if(event.getItem().getType() == Material.ENDER_EYE)
				{
					Main.OpenClassSelectMenu(player);
					event.setCancelled(true);
				}
			}
			else if(event.getPlayer().getGameMode() == GameMode.SURVIVAL && Main.currentStatus == ArenaStatus.Running) // GESTION DES COMPETENCES DE CHAQUE CLASSE
			{
				if(event.getItem().getType() == Material.SLIME_BALL) // CLASSE SLIME
				{
					SetSpecialAbilityUsed(player); // changer l'item pour afficher "competence unique epuisee"
					
					Location playerLoc = player.getLocation().clone();
					playerLoc.setY(playerLoc.getY()-2);
					World world = playerLoc.getWorld();
					
					int range = Main.settings.slime_platform_range;
					long duration = Main.settings.slime_platform_life_ticks;
					Main.arena.AddPlayerExpBarTimer(player, duration); // barre d'exp indiquant le temps restant avant epuisement de la competence
					for(int x=playerLoc.getBlockX()-range; x<=playerLoc.getBlockX()+range; x++)
					{
						for(int z=playerLoc.getBlockZ()-range; z<=playerLoc.getBlockZ()+range; z++)
						{
							Location blockLoc = new Location(world, x, playerLoc.getY(), z);
							if(world.getBlockAt(blockLoc) == null || world.getBlockAt(blockLoc).getType() == Material.AIR)
							{
								world.getBlockAt(blockLoc).setType(Material.SLIME_BLOCK);
								Bukkit.getScheduler().runTaskLater(Main.plugin, () -> world.getBlockAt(blockLoc).setType(Material.AIR), duration);
							}
						}
					}
					event.setCancelled(true);
				}
				else if(event.getItem().getType() == Material.IRON_INGOT) // CLASSE GOLEM
				{
					SetSpecialAbilityUsed(player); // changer l'item pour afficher "competence unique epuisee"
					Main.arena.AddPlayerExpBarTimer(player, Main.settings.golem_armor_duration); // barre d'exp indiquant le temps restant avant epuisement de la competence
					
					ItemBuilder ironChestplate = new ItemBuilder(Material.IRON_CHESTPLATE);
					player.getInventory().setChestplate(ironChestplate.build());
					
					long duration = Main.settings.golem_armor_duration;
					Main.arena.AddPlayerExpBarTimer(player, duration); // barre d'exp indiquant le temps restant avant epuisement de la competence
					Bukkit.getScheduler().runTaskLater(Main.plugin, () -> player.getInventory().setChestplate(null), duration);

					event.setCancelled(true);
				}
				else if(event.getItem().getType() == Material.GREEN_DYE) // CLASSE CACTUS
				{
					SetSpecialAbilityUsed(player); // changer l'item pour afficher "competence unique epuisee"
					Main.arena.AddPlayerExpBarTimer(player, Main.settings.cactus_thorns_duration); // barre d'exp indiquant le temps restant avant epuisement de la competence
					
					ItemBuilder cactusHead = new ItemBuilder(Material.CACTUS);
					player.getInventory().setHelmet(cactusHead.build());
					
					long duration = Main.settings.cactus_thorns_duration;
					Main.arena.AddPlayerExpBarTimer(player, duration); // barre d'exp indiquant le temps restant avant epuisement de la competence
					Bukkit.getScheduler().runTaskLater(Main.plugin, () -> player.getInventory().setHelmet(null), duration);
					event.setCancelled(true);
				}
				else if(event.getItem().getType() == Material.ENDER_EYE) // CLASSE ENDERMAN
				{
					ArrayList<Player> targets = new ArrayList<Player>(Main.arena.getAlivePlayers());
					targets.remove(event.getPlayer());
					if(targets.size() <= 0)
					{
						player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_HURT, 1.0f, 1.0f);
						player.sendMessage(ChatColor.RED + "Aucune cible n'est disponible !");
						event.setCancelled(true);
						return;
					}
					SetSpecialAbilityUsed(player); // changer l'item pour afficher "competence unique epuisee"
					Player target = targets.get(0);
					for(Player potentialTarget : targets)
					{
						Double currentTargetDistance = player.getLocation().distanceSquared(target.getLocation());
						Double potentialTargetDistance = player.getLocation().distanceSquared(potentialTarget.getLocation());
						if(potentialTargetDistance <= currentTargetDistance) { target = potentialTarget; }
					}
					World world = player.getLocation().getWorld();
					Double playerLocX = player.getLocation().getX();
					Double playerLocY = player.getLocation().getY();
					Double playerLocZ = player.getLocation().getZ();
					Float playerYaw = player.getLocation().getYaw();
					Float playerPitch = player.getLocation().getPitch();
					Double targetLocX = target.getLocation().getX();
					Double targetLocY = target.getLocation().getY();
					Double targetLocZ = target.getLocation().getZ();
					Float targetYaw = target.getLocation().getYaw();
					Float targetPitch = target.getLocation().getPitch();
					
					Location playerLoc = new Location(world, targetLocX, playerLocY, targetLocZ, playerYaw, playerPitch);
					Location targetLoc = new Location(world, playerLocX, targetLocY, playerLocZ, targetYaw, targetPitch);
					player.teleport(playerLoc);
					target.teleport(targetLoc);
					player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
					target.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
					player.sendMessage(ChatColor.GOLD + "Vos positions ont été inversées avec " + target.getName());
					target.sendMessage(ChatColor.GOLD + "Vos positions ont été inversées avec " + player.getName());
					
					
					event.setCancelled(true);
				}
			}
		}
	}
	
	
	// CONNEXION D'UN JOUEUR
	@EventHandler
	public void onConnect(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		
		// on ajoute automatiquement le joueur e la liste des joueurs disponibles
		Main.players.add(player);
		
		// on vide l'inventaire du joueur e la connexion
		event.getPlayer().getInventory().clear();
		
		// si le jeu est encore en attente de joueurs
		if(Main.currentStatus == ArenaStatus.Waiting)
		{
			player.setGameMode(GameMode.SURVIVAL);
			player.teleport(Main.waitingRoomLocation);
			player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
			Main.UpdateWaitingInventory(player);
		}
		else // sinon la partie est deja en cours
		{
			player.setGameMode(GameMode.SPECTATOR);
			Main.arena.getBossBar().addPlayer(player);
			player.setScoreboard(Main.arena.GenerateScoreboard());
		}
	}
	
	// DECONNEXION D'UN JOUEUR
	@EventHandler
	public void onDisconnect(PlayerQuitEvent event)
	{
		// gestion du disconnect lors d'une partie en cours
		if(Main.currentStatus == ArenaStatus.Running && Main.arena.getPlayers().contains(event.getPlayer())) { Main.arena.KickPlayer(event.getPlayer()); }
		//
		if(Main.players.contains(event.getPlayer())) { Main.players.remove(event.getPlayer()); }
		// gestion du disconnect en mode spectateur
		if(Main.spectators.contains(event.getPlayer())) { Main.spectators.remove(event.getPlayer()); }
		
		Main.playerClasses.remove(event.getPlayer()); // retirer la classe attribuee au joueur pour economiser de la RAM
	}
}