package me.Aisu.RewardManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class RewardManager extends JavaPlugin {

    // Variables needed to do config file management
    private static FileConfiguration ConfigLoc;
    private static File ConfigFile;
    protected static Logger log;

    public void onEnable() {
        // Initiating the TextManager class
        new TextManager(this);
        // Check if there is a directory, if not make one
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        // If there is no default config, create it
        ConfigFile = new File(getDataFolder(), "config.yml");
        if (!ConfigFile.exists()) {
            saveDefaultConfig();
        }
        reloadConfig();
        // Create an intractable copy of the config
        ConfigLoc = YamlConfiguration.loadConfiguration(ConfigFile);
        log = Logger.getLogger("Minecraft");
        for (Handler h : log.getParent().getHandlers()) {
            log.addHandler(h);
        }
        log.setUseParentHandlers(false);
        log.info("RewardManager V0.0.1 Enabled");
    }

    public void onDisable() {
        log.info("RewardManager V0.0.1 Disabled");
    }

    // List for current rewards chances
    private static final HashMap<String, Integer> RewardChances = new HashMap<>();

    // Reload functions
    public void reloadConfig() {
        ConfigLoc = YamlConfiguration.loadConfiguration(ConfigFile);
    }

    private static String getTime() {
        Date cal = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return (sdf.format(cal.getTime()));
    }

    private static ItemStack ItemBuilder(CommandSender Sender, String strRewardName) {
        // Setting variables needed to build an ItemStack and its metadata
        ItemStack istFinalReward = new ItemStack(Material.DIRT);
        ItemMeta imeFinalMeta = istFinalReward.getItemMeta();
        Material matMaterial;
        int intAmountMin;
        int intAmountMax;
        int intAmount;
        HashMap<String, Integer> Enchantments = new HashMap<>();
        List<String> strLore;
        String strRewardLocation = "Rewards." + strRewardName;
        String strName = "";
        // Getting the info from the config to build the ItemStack
        matMaterial = Material.getMaterial(Objects.requireNonNull(ConfigLoc.getString(strRewardLocation + ".Item.MaterialName")).toUpperCase());
        try {
            intAmountMin = Integer.parseInt(Objects.requireNonNull(ConfigLoc.getString(strRewardLocation + ".Item.Amount.Min")));
            intAmountMax = Integer.parseInt(Objects.requireNonNull(ConfigLoc.getString(strRewardLocation + ".Item.Amount.Max")));
        } catch (NullPointerException | NumberFormatException e) {
            Sender.sendMessage(ChatColor.RED + "The reward you got seems to have incorrect amounts.");
            Sender.sendMessage(ChatColor.RED + "Please contact an admin, developer or owner about this issue,");
            Sender.sendMessage(ChatColor.RED + "tell them the reward is named " + ChatColor.AQUA + strRewardName + ChatColor.RED + ".");
            Sender.sendMessage(ChatColor.RED + "This error occurred at " + ChatColor.AQUA + getTime() + ChatColor.RED + ".");
            log.warning( ChatColor.RED + "INVALID " + ChatColor.AQUA + "AMOUNT " + ChatColor.RED + "SETUP IN REWARDPOOLS CONFIG.YML @ " + ChatColor.AQUA + strRewardLocation);
            return null;
        }
        if (intAmountMin != intAmountMax) {
            intAmount = new Random().nextInt(intAmountMax - intAmountMin) + intAmountMin;
        } else {
            intAmount = intAmountMin;
        }
        try {
            strName = TextManager.colorize(Objects.requireNonNull(ConfigLoc.getString(strRewardLocation + ".Item.Name")));
        } catch (NullPointerException ignored) {
        }
        // Try to fetch enchantments
        try {
            for (String strEnch : Objects.requireNonNull(ConfigLoc.getConfigurationSection(strRewardLocation + ".Item.Enchantments")).getKeys(false)) {
                try {
                    Enchantments.put(strEnch, Integer.parseInt(Objects.requireNonNull(ConfigLoc.getString(strRewardLocation + ".Item.Enchantments." + strEnch))));
                } catch (NumberFormatException e) {
                    log.warning(ChatColor.RED + "INVALID " + ChatColor.AQUA + "ENCHANTMENT_LEVEL " + ChatColor.RED + "IN REWARDPOOLS CONFIG.YML @ " + ChatColor.AQUA + strRewardLocation + ".Item.Enchantments." + strEnch);
                }
            }
        } catch (NullPointerException e) {
            // If something is in the enchants but is not a recognized enchant notify log and player something went wrong
            if (!ConfigLoc.getStringList(strRewardLocation + ".Item.Enchantments").isEmpty()) {
                Sender.sendMessage(ChatColor.RED + "The reward you got seems to have incorrect enchantments.");
                Sender.sendMessage(ChatColor.RED + "Please contact an admin, developer or owner about this issue,");
                Sender.sendMessage(ChatColor.RED + "tell them the reward is named " + ChatColor.AQUA + strRewardName + ChatColor.RED + ".");
                Sender.sendMessage(ChatColor.RED + "This error occurred at " + ChatColor.AQUA + getTime() + ChatColor.RED + ".");
                log.warning(ChatColor.RED + "INVALID " + ChatColor.AQUA + "ENCHANTMENT " + ChatColor.RED + "SETUP IN REWARDPOOLS CONFIG.YML @ " + ChatColor.AQUA + strRewardLocation);
                return null;
            }
        }
        // Set the item lore
        strLore = ConfigLoc.getStringList(strRewardLocation + ".Item.Lore");
        // Build the item
        if (matMaterial != null) {
            istFinalReward.setType(matMaterial);
        } else {
            Sender.sendMessage(ChatColor.RED + "The reward you got seems to have an incorrect Material.");
            Sender.sendMessage(ChatColor.RED + "Please contact an admin, developer or owner about this issue,");
            Sender.sendMessage(ChatColor.RED + "tell them the reward is named " + ChatColor.AQUA + strRewardName + ChatColor.RED + ".");
            Sender.sendMessage(ChatColor.RED + "This error occurred at " + ChatColor.AQUA + getTime() + ChatColor.RED + ".");
            log.warning(ChatColor.RED + "INVALID " + ChatColor.AQUA + "MATERIALNAME " + ChatColor.RED + "IN REWARDPOOLS CONFIG.YML @ " + ChatColor.AQUA + strRewardLocation);
            return null;
        }
        if (intAmount >= 1) {
            istFinalReward.setAmount(intAmount);
        } else {
            istFinalReward.setAmount(0);
        }
        // Build the item metadata
        if (!strName.equals("")) {
            imeFinalMeta.setDisplayName(strName);
        }
        if (!Enchantments.isEmpty()) {
            imeFinalMeta = TextManager.enchanter(Enchantments, imeFinalMeta, strRewardName);
            // If something is in the enchants but is not a recognized enchant notify log and player something went wrong
            if (imeFinalMeta == null) {
                Sender.sendMessage(ChatColor.RED + "The reward you got seems to have incorrect enchantments.");
                Sender.sendMessage(ChatColor.RED + "Please contact an admin, developer or owner about this issue,");
                Sender.sendMessage(ChatColor.RED + "tell them the reward is named " + ChatColor.AQUA + strRewardName + ChatColor.RED + ".");
                Sender.sendMessage(ChatColor.RED + "This error occurred at " + ChatColor.AQUA + getTime() + ChatColor.RED + ".");
                log.warning(ChatColor.RED + "INVALID " + ChatColor.AQUA + "ENCHANTMENT " + ChatColor.RED + "SETUP IN REWARDPOOLS CONFIG.YML @ " + ChatColor.AQUA + strRewardLocation);
                return null;
            }
        }
        if (!strLore.isEmpty()) {
            for (int i = 0; i <= strLore.size() - 1; i++) {
                strLore.set(i, TextManager.colorize(strLore.get(i)));
            }
            imeFinalMeta.setLore(strLore);
        }
        // Put the metadata in the ItemStack
        istFinalReward.setItemMeta(imeFinalMeta);
        return istFinalReward;
    }

    // Reward manager
    public static void GiveTable(CommandSender Sender , String strRecipient, String strRewardGroup) {
        boolean PlayerExists = false;
        boolean RewardExists = false;
        String RewardType = null;
        Player p = Bukkit.getPlayer(strRecipient);
        assert p != null;
        if (p.isOnline()) {
            PlayerExists = true;
        }
        for (String TempRewardType : Objects.requireNonNull(RewardManager.ConfigLoc.getConfigurationSection("RewardSource")).getKeys(false)) {
            if (TempRewardType.equalsIgnoreCase(strRewardGroup)) {
                RewardExists = true;
                RewardType = TempRewardType;
            }
        }
        // Check if both given values exist
        if (!PlayerExists || !RewardExists) {
            if (!PlayerExists) Sender.sendMessage(ChatColor.RED + "Player does not exist");
            if (!RewardExists) Sender.sendMessage(ChatColor.RED + "Reward does not exist");
            return;
        }
        // Setting base variables
        String strRewardName = null;
        String strRewardSource = "RewardSource." + RewardType;
        int Chance;
        int TotChance = 0;
        int roll;
        // Getting all possible rewards and their chance
        RewardChances.clear();
        if (Objects.requireNonNull(ConfigLoc.getConfigurationSection(strRewardSource)).getKeys(false).isEmpty()) {
            Sender.sendMessage(ChatColor.RED + "The RewardPool you rolled seems not to be empty");
            Sender.sendMessage(ChatColor.RED + "Please contact an admin, developer or owner about this issue,");
            Sender.sendMessage(ChatColor.RED + "tell them there is no rewards in " + ChatColor.AQUA + RewardType + ChatColor.RED + ".");
            Sender.sendMessage(ChatColor.RED + "This error occurred at " + ChatColor.AQUA + getTime() + ChatColor.RED + ".");
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "INVALID " + ChatColor.AQUA + "REWARDS " + ChatColor.RED + "IN REWARDPOOLS CONFIG.YML @ " + ChatColor.AQUA + RewardType);
            return;
        }
        for (String strReward : Objects.requireNonNull(ConfigLoc.getConfigurationSection(strRewardSource)).getKeys(false)) {
            RewardChances.put(strReward, Integer.parseInt(Objects.requireNonNull(ConfigLoc.getString(strRewardSource + "." + strReward))));
        }
        // Count up all the chances in the items
        for (String str : RewardChances.keySet()) {
            Chance = RewardChances.get(str);
            TotChance += Chance;
        }
        // Roll on the total chance
        roll = new Random().nextInt(TotChance) + 1;
        // Check which item correlates with the roll
        for (String str : RewardChances.keySet()) {
            Chance = RewardChances.get(str);
            if (roll <= Chance) {
                strRewardName = str;
                break;
            }
            roll -= Chance;
        }
        Give(Sender, strRecipient, strRewardName);
    }

    // Reward manager
    public static void Give(CommandSender Sender, String strRecipient, String strRewardName) {
        Player Recipient = null;
        Player p = Bukkit.getPlayer(strRecipient);
        assert p != null;
        if (p.isOnline()) {
            Recipient = p;
        }
        String strRewardLocation;
        String strType;
        String strMessage;
        // Setting config location to get data from
        strRewardLocation = "Rewards." + strRewardName;
        List<String> strCommands = new ArrayList<>();
        ItemStack Reward;
        // Checking if reward exists
        if (Objects.requireNonNull(ConfigLoc.getConfigurationSection("Rewards")).getKeys(false).contains(strRewardName)) {
            strType = ConfigLoc.getString(strRewardLocation + ".Type");
            // Checking type of reward
            assert strType != null;
            if (strType.equalsIgnoreCase("Command") || strType.equalsIgnoreCase("Both")) {
                // Getting all commands
                strCommands = ConfigLoc.getStringList(strRewardLocation + ".Commands");
            }
            // Checking type of reward
            if (strType.equalsIgnoreCase("Item") || strType.equalsIgnoreCase("Both")) {
                // Setting variables needed to build an ItemStack and its metadata

                // Put the metadata in the ItemStack
                Reward = ItemBuilder(Sender, strRewardName);
                if (Reward == null) {
                    return;
                }
                assert Recipient != null;
                Recipient.getInventory().addItem(Reward);
                if (Objects.requireNonNull(Reward.getItemMeta()).getDisplayName() != null) {
                    strRewardName = Reward.getItemMeta().getDisplayName();
                }
            }
            // Send the player a customized message
            if (ConfigLoc.getString(strRewardLocation + ".Message") != null) {
                strMessage = TextManager.colorize(Objects.requireNonNull(ConfigLoc.getString(strRewardLocation + ".Message")).replace("%ITEM%", strRewardName));
                assert Recipient != null;
                Recipient.sendMessage(strMessage);
            }
            // If the reward does not exist notify logs and the player
        } else {
            Sender.sendMessage(ChatColor.RED + "The reward you got seems not to be in the reward list");
            Sender.sendMessage(ChatColor.RED + "Please contact an admin, developer or owner about this issue,");
            Sender.sendMessage(ChatColor.RED + "tell them there is no reward named " + ChatColor.AQUA + strRewardName + ChatColor.RED + ".");
            return;
        }
        for (String strCommand : strCommands) {
            if (!strCommand.equals("")) {
                strCommand = TextManager.colorize(strCommand);
                assert Recipient != null;
                strCommand = strCommand.replace("%player%", Recipient.getName());
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), strCommand);
            }
        }
    }

    // Command listener
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Checking if command was issued towards this plugin
        if (cmd.getName().equalsIgnoreCase("RP") && !(sender instanceof Player)) {
            switch (args.length) {
                case 3: {
                    if (args[0].equalsIgnoreCase("Give")) {
                        Give(sender, args[1], args[2]);
                        return true;
                    }
                }
                case 4: {
                    if (args[0].equalsIgnoreCase("Give")) {
                        if (args[1].equalsIgnoreCase("Table")) {
                            GiveTable(sender, args[2], args[3]);
                            return true;
                        }
                    }
                }
            }
        }
        if (cmd.getName().equalsIgnoreCase("RP") && (sender instanceof Player player)) {
            // Getting player
            if (player.hasPermission("RewardPools_Manager")) {
                switch (args.length) {
                    case 0: {
                        Bukkit.getConsoleSender().getServer().dispatchCommand(player, "help RewardPools");
                        return true;
                    }
                    // Check if there is one argument
                    case 1: {
                        // Check if the argument is "Reload"
                        if (args[0].equalsIgnoreCase("Reload")) {
                            if (player.hasPermission("RewardPools_Manager.Reload")) {
                                reloadConfig();
                                for (Player plrTempP : Bukkit.getServer().getOnlinePlayers()) {
                                    if (plrTempP.hasPermission("RewardPools_Manager")) {
                                        // "ratting out" who issued the command
                                        plrTempP.sendMessage(player.getName() + " issued a reload of the RewardPools Chances");
                                    }
                                }
                                return true;
                            }
                        }
                    }
                    case 2: {
                        Bukkit.getConsoleSender().getServer().dispatchCommand(player, "help RewardPools");
                        return true;
                    }
                    case 3: {
                        // Check if the argument is Give
                        if (args[0].equalsIgnoreCase("Give")) {
                            Give(player, args[1], args[2]);
                        }
                        return true;
                    }
                    case 4: {
                        if (args[0].equalsIgnoreCase("Give")) {
                            if (args[1].equalsIgnoreCase("Table")) {
                                for (String RewardType : Objects.requireNonNull(ConfigLoc.getConfigurationSection("RewardSource")).getKeys(false)) {
                                    if (RewardType.equalsIgnoreCase(args[3])) {
                                        GiveTable(sender, args[1], args[2]);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}