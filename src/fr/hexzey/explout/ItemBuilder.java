package fr.hexzey.explout;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Ruster
 * @version 1.0
 * @github https://github.com/RusterX16
 */

public class ItemBuilder {

    /* Attributes */

    private final ItemStack item;
    private final ItemMeta meta;
    private Material material;
    private String displayName;
    private List<String> lore;
    private Enchantment enchantment;
    private int amount;
    private short durability;
    private byte data;
    private boolean flags;
    private boolean unbreakable;

    /* Constructor(s) */

    public ItemBuilder(ItemStack item) {
        this.item = item;
        meta = item.getItemMeta();
        amount = item.getAmount();
    }

    public ItemBuilder(Material material) {
        item = new ItemStack(material);
        meta = item.getItemMeta();
        this.material = material;
        amount = 1;
    }

    public ItemBuilder(Material material, int amount) {
        item = new ItemStack(material, amount);
        meta = item.getItemMeta();
        this.material = material;
        this.amount = amount;
    }

    public ItemBuilder(Material material, int amount, byte data) {
        item = new ItemStack(material, amount, data);
        meta = item.getItemMeta();
        this.material = material;
        this.amount = amount;
        this.data = data;
    }

    /* Public Methods */

    /**
     * Set a new material to the item
     *
     * @param material
     * @return
     */
    public ItemBuilder material(Material material) {
        item.setType(material);
        this.material = material;
        return this;
    }

    /**
     * Set a custom displayed name to the item
     *
     * @param displayName
     * @return
     */
    public ItemBuilder displayName(String displayName) {
        meta.setDisplayName(displayName);
        this.displayName = displayName;
        return this;
    }


    /**
     * Set a custom lore description to the item using a List of String in parameter
     *
     * @param lore
     * @return
     */
    public ItemBuilder lore(List<String> lore) {
        meta.setLore(lore);
        this.lore = lore;
        return this;
    }

    /**
     * Set a custom lore description to the item using single Strings in parameter
     *
     * @param lore
     * @return
     */
    public ItemBuilder lore(String... lore) {
        meta.setLore(Arrays.asList(lore));
        this.lore = Arrays.asList(lore);
        return this;
    }

    /**
     * Replace the existing lore by a new lore specified in parameter
     *
     * @param line
     * @return
     */
    public ItemBuilder lore(String line) {
        meta.setLore(Collections.singletonList(line));
        this.lore = Collections.singletonList(line);
        return this;
    }

    /**
     * Set a custom lore line in the specified index in parameter
     *
     * @param index
     * @param line
     * @return
     */
    public ItemBuilder setLine(int index, String line) {
        if(lore == null) {
            lore = new ArrayList<>();
        }
        lore.add(index, line);
        meta.setLore(lore);
        return this;
    }

    /**
     * Add a custom lore line to the item
     *
     * @param line
     * @return
     */
    public ItemBuilder addLine(String line) {
        if(lore == null) {
            lore = new ArrayList<>();
        }
        lore.add(line);
        meta.setLore(lore);
        return this;
    }

    /**
     * Remove the current lore of the item
     *
     * @return
     */
    public ItemBuilder removeLore() {
        meta.setLore(null);
        return this;
    }

    /**
     * Remove the lore's given line of the item
     *
     * @param line
     * @return
     */
    public ItemBuilder removeLine(int line) {
        if(lore != null) {
            lore.remove(line);
            meta.setLore(lore);
        }
        return this;
    }

    /**
     * Add an enchantment level 1 to the item
     *
     * @param enchantment
     * @return
     */
    public ItemBuilder addEnchantment(Enchantment enchantment) {
        item.addUnsafeEnchantment(enchantment, 1);
        meta.addEnchant(enchantment, 1, true);
        this.enchantment = enchantment;
        return this;
    }

    /**
     * Add an enchantment to the item with a given level
     *
     * @param enchantment
     * @param level
     * @return
     */
    public ItemBuilder addEnchantment(Enchantment enchantment, int level) {
        item.addUnsafeEnchantment(enchantment, level);
        meta.addEnchant(enchantment, level, true);
        this.enchantment = enchantment;
        return this;
    }

    /**
     * Remove an existing enchantment from the item
     *
     * @param enchantment
     * @return
     */
    public ItemBuilder removeEnchantment(Enchantment enchantment) {
        item.removeEnchantment(enchantment);
        meta.removeEnchant(enchantment);
        this.enchantment = null;
        return this;
    }

    /**
     * Set the amount of the item
     *
     * @param amount
     * @return
     */
    public ItemBuilder amount(int amount) {
        item.setAmount(amount);
        this.amount = amount;
        return this;
    }

    /**
     * Set the durability of the item
     *
     * @param durability
     * @return
     */
    public ItemBuilder durability(short durability) {
        if(meta instanceof Damageable) {
            short max = item.getType().getMaxDurability();
            ((Damageable) meta).setDamage(max - durability);
        }
        this.durability = durability;
        return this;
    }

    public ItemBuilder damage(short damage) {
        if(meta instanceof Damageable) {
            ((Damageable) meta).setDamage(damage);
        }
        this.durability = (short) (durability - damage);
        return this;
    }

    /**
     * Hide the item flags when the flags boolean is on true and display them in opposite
     *
     * @param flags
     * @return
     */
    public ItemBuilder flags(boolean flags) {
        if(flags) {
            meta.addItemFlags(ItemFlag.values());
        } else {
            meta.removeItemFlags(ItemFlag.values());
        }
        this.flags = flags;
        return this;
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        meta.setUnbreakable(unbreakable);
        this.unbreakable = unbreakable;
        return this;
    }

    /**
     * Turn the ItemBuilder object to the ItemStack object
     *
     * @return
     */
    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Added by Hexzey : return the item with a glowing effect
     * @return
     */
    public ItemStack buildGlow()
    {
    	ItemStack glowingItem = this.build();
    	glowingItem.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
		ItemMeta meta = glowingItem.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		glowingItem.setItemMeta(meta);
		
		return glowingItem;
    }

    /* Getters */

    public ItemBuilder getItemBuilder() { return this; }

    public ItemStack getItem() { return item; }

    public ItemMeta getMeta() { return meta; }

    public String getDisplayName() { return displayName; }

    public Material getType() { return material; }

    public List<String> getLore() { return lore; }

    public Enchantment getEnchantment() { return enchantment; }

    public int getAmount() { return amount; }

    public short getDurability() { return durability; }

    public byte getData() { return data; }

    public MaterialData getMaterialData() { return item.getData(); }

    public boolean hasFlags() { return flags; }
}