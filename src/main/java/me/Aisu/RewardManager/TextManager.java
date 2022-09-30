package me.Aisu.RewardManager;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Objects;

public class TextManager {
    public TextManager(RewardManager plugin) {
        plugin.getServer().getPluginManager();
    }

    // Colour code decoder for yml
    static String colorize(String msg) {
        if (msg.isEmpty()) {
            return "";
        }
        String newMsg = ChatColor.translateAlternateColorCodes('&', msg);
        newMsg = newMsg.replaceAll("&newline", "\n");
        return newMsg;
    }

    // Optional command replacement with customizable command
    static String colorize(String msg, String Command) {
        String newMsg = colorize(msg);
        newMsg = newMsg.replaceAll("%Command%", Command);
        return newMsg;
    }

    // Apply enchants to a meta with alias conversion
    static ItemMeta enchanter(HashMap<String, Integer> enchants, ItemMeta startMeta, String Reward) {
        String enchant;
        for (String ench : enchants.keySet()) {
            enchant = ench.toLowerCase();
            // Go through the alias list for enchantments
            /* Prot					*/
            enchant = enchant.replaceAll("protection_environmental", "protection").replaceAll("protection invironmental", "protection");
            /* Fire Prot			*/
            enchant = enchant.replaceAll("fire protection", "fire_protection").replaceAll("protection_fire", "fire_protection").replaceAll("protection fire", "fire_protection");
            /* Feather Fall			*/
            enchant = enchant.replaceAll("feather falling", "feather_falling").replaceAll("protection_fall", "feather_falling").replaceAll("protection fall", "feather_falling");
            /* Blast Prot			*/
            enchant = enchant.replaceAll("protection explosions", "blast_protection").replaceAll("protection_explosions", "blast_protection");
            /* Projectile Prot		*/
            enchant = enchant.replaceAll("protection_projectile", "projectile_protection").replaceAll("protection projectile", "projectile_protection");
            /* respiration			*/
            enchant = enchant.replaceAll("oxygen", "oxygenrespiration");
            /* aqua affinity		*/
            enchant = enchant.replaceAll("aqua affinity", "aqua_affinity").replaceAll("water_worker", "aqua_affinity").replaceAll("water worker", "aqua_affinity");
            /* depth strider		*/
            enchant = enchant.replaceAll("depth strider", "depth_strider");
            /* frost walker			*/
            enchant = enchant.replaceAll("frost walker", "frost_walker");
            /* sharpness       		*/
            enchant = enchant.replaceAll("damage all", "sharpness").replaceAll("damage_all", "sharpness");
            /* smite                */
            enchant = enchant.replaceAll("damage_undead", "smite").replaceAll("damage undead", "smite");
            /* bane of arthropods   */
            enchant = enchant.replaceAll("bane of arthropods", "bane_of_arthropods").replaceAll("damage_arthropods", "bane_of_arthropods").replaceAll("damage arthropods", "bane_of_arthropods");
            /* fire aspect			*/
            enchant = enchant.replaceAll("fire aspect", "fire_aspect");
            /* looting				*/
            enchant = enchant.replaceAll("loot_bonus_mobs", "looting").replaceAll("loot bonus mobs", "looting");
            /* efficiency			*/
            enchant = enchant.replaceAll("dig_speed", "efficiency").replaceAll("dig speed", "efficiency");
            /* silk touch			*/
            enchant = enchant.replaceAll("silk touch", "silk_touch");
            /* unbreaking			*/
            enchant = enchant.replaceAll("durability", "unbreaking");
            /* fortune				*/
            enchant = enchant.replaceAll("loot_bonus_blocks", "fortune").replaceAll("loot bonus blocks", "fortune");
            /* power				*/
            enchant = enchant.replaceAll("arrow_damage", "power").replaceAll("arrow damage", "power");
            /* punch				*/
            enchant = enchant.replaceAll("arrow_knockback", "punch").replaceAll("arrow knockback", "punch");
            /* flame				*/
            enchant = enchant.replaceAll("arrow_fire", "flame").replaceAll("arrow fire", "flame");
            /* infinity				*/
            enchant = enchant.replaceAll("arrow_infinite", "infinity").replaceAll("arrow infinite", "infinity");
            /* luck of the sea		*/
            enchant = enchant.replaceAll("luck of the sea", "luck_of_the_sea").replaceAll("luck", "luck_of_the_sea");
            /* curse of binding		*/
            enchant = enchant.replaceAll("curse of binding", "binding_curse").replaceAll("curse_of_binding", "binding_curse").replaceAll("binding curse", "binding_curse");
            /* curse of vanishing	*/
            enchant = enchant.replaceAll("curse of vanishing", "vanishing_curse").replaceAll("curse_of_vanishing", "vanishing_curse").replaceAll("vanishing curse", "vanishing_curse");
            /* quickcharge			*/
            enchant = enchant.replaceAll("quick charge", "quick_charge");
            /* sweeping edge		*/
            enchant = enchant.replaceAll("sweeping edge", "sweeping").replaceAll("sweeping_edge", "sweeping");
            // if enchant isn't empty
            if (!enchant.equals("")) {
                // Add proper enchantment
                try {
                    startMeta.addEnchant(Objects.requireNonNull(Enchantment.getByKey(NamespacedKey.minecraft(enchant))), enchants.get(ench), true);
                } catch (NullPointerException | IllegalArgumentException e) {
                    RewardManager.log.warning(ChatColor.RED + "INVALID" + ChatColor.AQUA + " ENCHANTMENT: \"" + enchant + "\"" + ChatColor.RED + " IN REWARDPOOLS CONFIG.YML @ " + ChatColor.AQUA + Reward);
                    return null;
                }
            }
        }
        return startMeta;
    }
}
