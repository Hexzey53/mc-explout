package fr.hexzey.explout;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class HeadBuilder
{
	private ItemStack playerHead;
	private String displayName;
	private String playerName;
	
	public HeadBuilder(String _playerName)
	{
		playerHead = new ItemStack(Material.PLAYER_HEAD);
		displayName = "Custom Head";
		
		playerName = _playerName;
	}
	
	public void setDisplayName(String _name) { this.displayName = _name; }
	
	public ItemStack build()
	{
		ItemStack item = playerHead.clone();
        SkullMeta skull = (SkullMeta) item.getItemMeta();
        skull.setDisplayName(displayName);
        skull.setOwner(playerName);
        item.setItemMeta(skull);
        
        return item;
	}
}
