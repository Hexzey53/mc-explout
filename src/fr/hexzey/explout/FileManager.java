package fr.hexzey.explout;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class FileManager
{
	// fichier contenant les informations pour l'arène de jeu
    public static File ArenaConfigFile = new File("plugins/Explout/arenas.yml");
    public static FileConfiguration ArenaConfig = YamlConfiguration.loadConfiguration(ArenaConfigFile);
    public static void LoadArenas()
    {
    	try
    	{
	    	int i=0;
	    	for(String key : ArenaConfig.getKeys(false))
	    	{	    		
	    		World world = Bukkit.getWorld(ArenaConfig.getString(key + ".world"));
	    		
	    		String subkey = key + ".center";
	    		
	    		Location arenaCenter = new Location(world, ArenaConfig.getDouble(subkey + ".x"), ArenaConfig.getDouble(subkey + ".y"),
	    				ArenaConfig.getDouble(subkey + ".z"));
	    		
	    		Integer spawnRadius = ArenaConfig.getInt(key + ".spawnradius");
	    		
	    		Main.arena = new Arena(arenaCenter, spawnRadius);
	    		i++;
	    	}
	    	Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "EXPLOUT : arenas created");
    	}
    	catch(Exception e)
    	{
    		Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "EXPLOUT : error while creating arenas");
    		e.printStackTrace();
    	}
    }
    
    // fichier contenant les informations pour la salle d'attente
    public static File RoomConfigFile = new File("plugins/Explout/waitingRoom.yml");
    public static FileConfiguration RoomConfig = YamlConfiguration.loadConfiguration(RoomConfigFile);
    public static void LoadWaitingRoom()
    {
    	try
    	{
    		World world = Bukkit.getWorld(RoomConfig.getString("world"));
    		    		
    		Main.waitingRoomLocation = new Location(world, RoomConfig.getDouble("x"), RoomConfig.getDouble("y"),
    				RoomConfig.getDouble("z"));
    		
	    	Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "EXPLOUT : waiting room loaded");
    	}
    	catch(Exception e)
    	{
    		Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "EXPLOUT : error while loading waiting room");
    		e.printStackTrace();
    	}
    }
}
