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
        // Enable log handling
        log = Logger.getLogger("Minecraft");
        for (Handler h : log.getParent().getHandlers()) {
            log.addHandler(h);
        }
        log.setUseParentHandlers(false);

        // Check if there is a directory, if not make one
        if (!getDataFolder().exists()) {
            log.info("RewardManager: There is no Data Folder present for this plugin.");
            if (!getDataFolder().mkdir()) {
                log.info("RewardManager: Failed to make Data folder.");
            } else {
                log.info("RewardManager: Succesfully made Data folder.");
            }
        } else {
            ConfigFile = new File(getDataFolder(), "config.yml");
            // If there is no default config, create it
            if (!ConfigFile.exists()) {
                saveDefaultConfig();
            }
            // Create an intractable copy of the config
            reloadConfig();
        }

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
        Date date = new Date();
        return (new SimpleDateFormat("HH:mm:ss").format(date.getTime()));
    }

    private static ItemStack ItemBuilder(CommandSender Sender, String strRewardName) {
        // Setting variables needed to build an ItemStack and its metadata
        ItemStack FinalReward = new ItemStack(Material.DIRT);
        ItemMeta FinalMeta = FinalReward.getItemMeta();
        Material material;
        int AmountMin;
        int AmountMax;
        int Amount;
        HashMap<String, Integer> Enchantments = new HashMap<>();
        List<String> Lore;
        String RewardLocation = "Rewards." + strRewardName;
        String Name = "";
        // Getting the info from the config to build the ItemStack
        material = Material.getMaterial(Objects.requireNonNull(ConfigLoc.getString(RewardLocation + ".Item.MaterialName")).toUpperCase());
        try {
            AmountMin = Integer.parseInt(Objects.requireNonNull(ConfigLoc.getString(RewardLocation + ".Item.Amount.Min")));
            AmountMax = Integer.parseInt(Objects.requireNonNull(ConfigLoc.getString(RewardLocation + ".Item.Amount.Max")));
        } catch (NullPointerException | NumberFormatException e) {
            Sender.sendMessage(ChatColor.RED + "The reward you got seems to have incorrect amounts.");
            Sender.sendMessage(ChatColor.RED + "Please contact an admin, developer or owner about this issue,");
            Sender.sendMessage(ChatColor.RED + "tell them the reward is named " + ChatColor.AQUA + strRewardName + ChatColor.RED + ".");
            Sender.sendMessage(ChatColor.RED + "This error occurred at " + ChatColor.AQUA + getTime() + ChatColor.RED + ".");
            log.warning( ChatColor.RED + "INVALID " + ChatColor.AQUA + "AMOUNT " + ChatColor.RED + "SETUP IN REWARDMANAGER CONFIG.YML @ " + ChatColor.AQUA + RewardLocation);
            return null;
        }
        if (AmountMin != AmountMax) {
            Amount = new Random().nextInt(AmountMax - AmountMin) + AmountMin;
        } else {
            Amount = AmountMin;
        }
        try {
            Name = TextManager.colorize(Objects.requireNonNull(ConfigLoc.getString(RewardLocation + ".Item.Name")));
        } catch (NullPointerException ignored) {
        }
        // Try to fetch enchantments
        try {
            for (String strEnch : Objects.requireNonNull(ConfigLoc.getConfigurationSection(RewardLocation + ".Item.Enchantments")).getKeys(false)) {
                try {
                    Enchantments.put(strEnch, Integer.parseInt(Objects.requireNonNull(ConfigLoc.getString(RewardLocation + ".Item.Enchantments." + strEnch))));
                } catch (NumberFormatException e) {
                    log.warning(ChatColor.RED + "INVALID " + ChatColor.AQUA + "ENCHANTMENT_LEVEL " + ChatColor.RED + "IN REWARDMANAGER CONFIG.YML @ " + ChatColor.AQUA + RewardLocation + ".Item.Enchantments." + strEnch);
                }
            }
        } catch (NullPointerException e) {
            // If something is in the enchants but is not a recognized enchant notify log and player something went wrong
            if (!ConfigLoc.getStringList(RewardLocation + ".Item.Enchantments").isEmpty()) {
                Sender.sendMessage(ChatColor.RED + "The reward you got seems to have incorrect enchantments.");
                Sender.sendMessage(ChatColor.RED + "Please contact an admin, developer or owner about this issue,");
                Sender.sendMessage(ChatColor.RED + "tell them the reward is named " + ChatColor.AQUA + strRewardName + ChatColor.RED + ".");
                Sender.sendMessage(ChatColor.RED + "This error occurred at " + ChatColor.AQUA + getTime() + ChatColor.RED + ".");
                log.warning(ChatColor.RED + "INVALID " + ChatColor.AQUA + "ENCHANTMENT " + ChatColor.RED + "SETUP IN REWARDMANAGER CONFIG.YML @ " + ChatColor.AQUA + RewardLocation);
                return null;
            }
        }
        // Set the item lore
        Lore = ConfigLoc.getStringList(RewardLocation + ".Item.Lore");
        // Build the item
        if (material != null) {
            FinalReward.setType(material);
        } else {
            Sender.sendMessage(ChatColor.RED + "The reward you got seems to have an incorrect Material.");
            Sender.sendMessage(ChatColor.RED + "Please contact an admin, developer or owner about this issue,");
            Sender.sendMessage(ChatColor.RED + "tell them the reward is named " + ChatColor.AQUA + strRewardName + ChatColor.RED + ".");
            Sender.sendMessage(ChatColor.RED + "This error occurred at " + ChatColor.AQUA + getTime() + ChatColor.RED + ".");
            log.warning(ChatColor.RED + "INVALID " + ChatColor.AQUA + "MATERIALNAME " + ChatColor.RED + "IN REWARDMANAGER CONFIG.YML @ " + ChatColor.AQUA + RewardLocation);
            return null;
        }
        if (Amount >= 1) {
            FinalReward.setAmount(Amount);
        } else {
            FinalReward.setAmount(0);
        }
        // Build the item metadata
        if (!Name.equals("")) {
            FinalMeta.setDisplayName(Name);
        }
        if (!Enchantments.isEmpty()) {
            FinalMeta = TextManager.enchanter(Enchantments, FinalMeta, strRewardName);
            // If something is in the enchants but is not a recognized enchant notify log and player something went wrong
            if (FinalMeta == null) {
                Sender.sendMessage(ChatColor.RED + "The reward you got seems to have incorrect enchantments.");
                Sender.sendMessage(ChatColor.RED + "Please contact an admin, developer or owner about this issue,");
                Sender.sendMessage(ChatColor.RED + "tell them the reward is named " + ChatColor.AQUA + strRewardName + ChatColor.RED + ".");
                Sender.sendMessage(ChatColor.RED + "This error occurred at " + ChatColor.AQUA + getTime() + ChatColor.RED + ".");
                log.warning(ChatColor.RED + "INVALID " + ChatColor.AQUA + "ENCHANTMENT " + ChatColor.RED + "SETUP IN REWARDMANAGER CONFIG.YML @ " + ChatColor.AQUA + RewardLocation);
                return null;
            }
        }
        if (!Lore.isEmpty()) {
            for (int i = 0; i <= Lore.size() - 1; i++) {
                Lore.set(i, TextManager.colorize(Lore.get(i)));
            }
            FinalMeta.setLore(Lore);
        }
        // Put the metadata in the ItemStack
        FinalReward.setItemMeta(FinalMeta);
        return FinalReward;
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
        String RewardName = null;
        String RewardSource = "RewardSource." + RewardType;
        int Chance;
        int TotChance = 0;
        int roll;
        // Getting all possible rewards and their chance
        RewardChances.clear();
        if (Objects.requireNonNull(ConfigLoc.getConfigurationSection(RewardSource)).getKeys(false).isEmpty()) {
            Sender.sendMessage(ChatColor.RED + "The RewardPool you rolled seems not to be empty");
            Sender.sendMessage(ChatColor.RED + "Please contact an admin, developer or owner about this issue,");
            Sender.sendMessage(ChatColor.RED + "tell them there is no rewards in " + ChatColor.AQUA + RewardType + ChatColor.RED + ".");
            Sender.sendMessage(ChatColor.RED + "This error occurred at " + ChatColor.AQUA + getTime() + ChatColor.RED + ".");
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "INVALID " + ChatColor.AQUA + "REWARDS " + ChatColor.RED + "IN REWARDMANAGER CONFIG.YML @ " + ChatColor.AQUA + RewardType);
            return;
        }
        for (String strReward : Objects.requireNonNull(ConfigLoc.getConfigurationSection(RewardSource)).getKeys(false)) {
            RewardChances.put(strReward, Integer.parseInt(Objects.requireNonNull(ConfigLoc.getString(RewardSource + "." + strReward))));
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
                RewardName = str;
                break;
            }
            roll -= Chance;
        }
        Give(Sender, strRecipient, RewardName);
    }

    // Reward manager
    public static void Give(CommandSender Sender, String strRecipient, String RewardName) {
        Player player = Bukkit.getPlayer(strRecipient);
        assert player != null;
        String RewardLocation;
        String Type;
        String Message;
        // Setting config location to get data from
        RewardLocation = "Rewards." + RewardName;
        List<String> Commands = new ArrayList<>();
        ItemStack Reward;
        // Checking if reward exists
        if (Objects.requireNonNull(ConfigLoc.getConfigurationSection("Rewards")).getKeys(false).contains(RewardName)) {
            Type = ConfigLoc.getString(RewardLocation + ".Type");
            // Checking type of reward
            assert Type != null;
            if (Type.equalsIgnoreCase("Command") || Type.equalsIgnoreCase("Both")) {
                // Getting all commands
                Commands = ConfigLoc.getStringList(RewardLocation + ".Commands");
            }
            // Checking type of reward
            if (Type.equalsIgnoreCase("Item") || Type.equalsIgnoreCase("Both")) {
                // Setting variables needed to build an ItemStack and its metadata

                // Put the metadata in the ItemStack
                Reward = ItemBuilder(Sender, RewardName);
                if (Reward == null) {
                    return;
                }
                player.getInventory().addItem(Reward);
                if (Objects.requireNonNull(Reward.getItemMeta()).getDisplayName() != null) {
                    RewardName = Reward.getItemMeta().getDisplayName();
                }
            }
            // Send the player a customized message
            if (ConfigLoc.getString(RewardLocation + ".Message") != null) {
                Message = TextManager.colorize(Objects.requireNonNull(ConfigLoc.getString(RewardLocation + ".Message")).replace("%ITEM%", RewardName));
                player.sendMessage(Message);
            }
            // If the reward does not exist notify logs and the player
        } else {
            Sender.sendMessage(ChatColor.RED + "The reward you got seems not to be in the reward list");
            Sender.sendMessage(ChatColor.RED + "Please contact an admin, developer or owner about this issue,");
            Sender.sendMessage(ChatColor.RED + "tell them there is no reward named " + ChatColor.AQUA + RewardName + ChatColor.RED + ".");
            return;
        }
        for (String Command : Commands) {
            if (!Command.equals("")) {
                Command = TextManager.colorize(Command);
                Command = Command.replace("%player%", player.getName());
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), Command);
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
            if (player.hasPermission("RewardManager_Manager")) {
                switch (args.length) {
                    case 0: {
                        Bukkit.getConsoleSender().getServer().dispatchCommand(player, "help RewardManager");
                        return true;
                    }
                    // Check if there is one argument
                    case 1: {
                        // Check if the argument is "Reload"
                        if (args[0].equalsIgnoreCase("Reload")) {
                            if (player.hasPermission("RewardManager_Manager.Reload")) {
                                reloadConfig();
                                for (Player plrTempP : Bukkit.getServer().getOnlinePlayers()) {
                                    if (plrTempP.hasPermission("RewardManager_Manager")) {
                                        // "ratting out" who issued the command
                                        plrTempP.sendMessage(player.getName() + " issued a reload of the RewardManager Chances");
                                    }
                                }
                                return true;
                            }
                        }
                    }
                    case 2: {
                        Bukkit.getConsoleSender().getServer().dispatchCommand(player, "help RewardManager");
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