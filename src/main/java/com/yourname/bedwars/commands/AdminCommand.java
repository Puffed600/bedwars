package com.yourname.bedwars.commands;

import com.yourname.bedwars.BedwarsPlugin;
import com.yourname.bedwars.arena.Arena;
import com.yourname.bedwars.arena.ArenaManager;
import com.yourname.bedwars.arena.Team;
import com.yourname.bedwars.generator.Generator;
import com.yourname.bedwars.generator.GeneratorType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.Bed;

import java.util.Set;

public class AdminCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bedwars.admin")) {
            sender.sendMessage(ChatColor.RED + "You lack permission.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /bw <create | addteam | setlobby | setspawn | setbed | setshop | setupgradeshop | addgenerator | setboundary | join | forcestart | stopgame | leave | save | info>");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to use these commands.");
            return true;
        }

        Player p = (Player) sender;
        ArenaManager am = BedwarsPlugin.getInstance().getArenaManager();

        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length < 2) { p.sendMessage(ChatColor.RED + "Usage: /bw create <arenaName>"); return true; }
                if (am.getArena(args[1]) != null) { p.sendMessage(ChatColor.RED + "Arena already exists."); return true; }
                am.getArenas().add(new Arena(args[1]));
                p.sendMessage(ChatColor.GREEN + "Arena " + args[1] + " created!");
                break;

            case "join":
                if (args.length < 2) { p.sendMessage(ChatColor.RED + "Usage: /bw join <arenaName>"); return true; }
                Arena aJoin = am.getArena(args[1]);
                if (aJoin == null) { p.sendMessage(ChatColor.RED + "Arena not found."); return true; }
                aJoin.joinPlayer(p);
                break;

            case "leave":
                Arena aLeave = am.arenaForPlayer(p);
                if (aLeave == null) { p.sendMessage(ChatColor.RED + "You are not in an arena."); return true; }
                aLeave.removePlayer(p);
                p.getInventory().clear();
                p.sendMessage(ChatColor.GREEN + "You left the arena.");
                break;

            case "forcestart":
                if (args.length < 2) { p.sendMessage(ChatColor.RED + "Usage: /bw forcestart <arenaName>"); return true; }
                Arena aForce = am.getArena(args[1]);
                if (aForce == null) { p.sendMessage(ChatColor.RED + "Arena not found."); return true; }
                aForce.forceStart();
                p.sendMessage(ChatColor.GREEN + "Forcing arena " + args[1] + " to start!");
                break;

            case "stopgame":
                if (args.length < 2) { p.sendMessage(ChatColor.RED + "Usage: /bw stopgame <arenaName>"); return true; }
                Arena aStop = am.getArena(args[1]);
                if (aStop == null) { p.sendMessage(ChatColor.RED + "Arena not found."); return true; }
                aStop.stopGame();
                p.sendMessage(ChatColor.GREEN + "Stopping arena " + args[1] + "!");
                break;

            case "addteam":
                if (args.length < 3) { p.sendMessage(ChatColor.RED + "Usage: /bw addteam <arenaName> <color>"); return true; }
                Arena aAdd = am.getArena(args[1]);
                if (aAdd == null) { p.sendMessage(ChatColor.RED + "Arena not found."); return true; }
                try {
                    ChatColor color = ChatColor.valueOf(args[2].toUpperCase());
                    aAdd.addTeam(new Team(args[2].toUpperCase(), color));
                    p.sendMessage(ChatColor.GREEN + "Added team " + color + args[2].toUpperCase() + ChatColor.GREEN + " to arena.");
                } catch (IllegalArgumentException e) {
                    p.sendMessage(ChatColor.RED + "Invalid color. Use RED, BLUE, GREEN, YELLOW, etc.");
                }
                break;

            case "setlobby":
                if (args.length < 2) { p.sendMessage(ChatColor.RED + "Usage: /bw setlobby <arenaName>"); return true; }
                Arena aLobby = am.getArena(args[1]);
                if (aLobby == null) { p.sendMessage(ChatColor.RED + "Arena not found."); return true; }
                aLobby.setLobby(p.getLocation());
                p.sendMessage(ChatColor.GREEN + "Lobby set for arena " + args[1]);
                break;

            case "setspawn":
                if (args.length < 3) { p.sendMessage(ChatColor.RED + "Usage: /bw setspawn <arenaName> <color>"); return true; }
                Arena aSpawn = am.getArena(args[1]);
                if (aSpawn == null) { p.sendMessage(ChatColor.RED + "Arena not found."); return true; }
                Team tSpawn = aSpawn.getTeam(args[2]);
                if (tSpawn == null) { p.sendMessage(ChatColor.RED + "Team not found. Use /bw addteam first."); return true; }
                tSpawn.setSpawn(p.getLocation());
                p.sendMessage(ChatColor.GREEN + "Spawn set for team " + tSpawn.getColoredName());
                break;

            case "setbed":
                if (args.length < 3) { p.sendMessage(ChatColor.RED + "Usage: /bw setbed <arenaName> <color>"); return true; }
                Arena aBed = am.getArena(args[1]);
                if (aBed == null) { p.sendMessage(ChatColor.RED + "Arena not found."); return true; }
                Team tBed = aBed.getTeam(args[2]);
                if (tBed == null) { p.sendMessage(ChatColor.RED + "Team not found."); return true; }
                
                Block target = p.getTargetBlock((Set<Material>) null, 50);
                if (target == null || target.getType() != Material.BED_BLOCK) {
                    p.sendMessage(ChatColor.RED + "Debug: You are looking at " + (target == null ? "NULL" : target.getType().name()) + ", not a BED_BLOCK!");
                    return true;
                }
                
                Block partner = getPartnerBed(target);
                if (partner == null) { p.sendMessage(ChatColor.RED + "Debug: Could not find partner bed block."); return true; }
                
                tBed.setBedHead(target.getLocation());
                tBed.setBedFoot(partner.getLocation());
                p.sendMessage(ChatColor.GREEN + "Bed bound for team " + tBed.getColoredName());
                break;

            case "setshop":
                if (args.length < 3) { p.sendMessage(ChatColor.RED + "Usage: /bw setshop <arenaName> <color>"); return true; }
                Arena aShop = am.getArena(args[1]);
                if (aShop == null) { p.sendMessage(ChatColor.RED + "Arena not found."); return true; }
                Team tShop = aShop.getTeam(args[2]);
                if (tShop == null) { p.sendMessage(ChatColor.RED + "Team not found."); return true; }
                
                tShop.setShop(p.getLocation());
                am.spawnVillager(p.getLocation(), "\u00A7aItem Shop", "bw_shop");
                p.sendMessage(ChatColor.GREEN + "Shop location set for team " + tShop.getColoredName() + "!");
                break;

            case "setupgradeshop":
                if (args.length < 3) { p.sendMessage(ChatColor.RED + "Usage: /bw setupgradeshop <arenaName> <color>"); return true; }
                Arena aUp = am.getArena(args[1]);
                if (aUp == null) { p.sendMessage(ChatColor.RED + "Arena not found."); return true; }
                Team tUp = aUp.getTeam(args[2]);
                if (tUp == null) { p.sendMessage(ChatColor.RED + "Team not found."); return true; }
                
                tUp.setUpgradeShop(p.getLocation());
                am.spawnVillager(p.getLocation(), "\u00A7bUpgrades", "bw_upgrades");
                p.sendMessage(ChatColor.GREEN + "Upgrade Shop location set for team " + tUp.getColoredName() + "!");
                break;

            case "addgenerator":
                if (args.length < 3) { p.sendMessage(ChatColor.RED + "Usage: /bw addgenerator <arenaName> <IRON|GOLD|DIAMOND|EMERALD>"); return true; }
                Arena aGen = am.getArena(args[1]);
                if (aGen == null) { p.sendMessage(ChatColor.RED + "Arena not found."); return true; }
                try {
                    GeneratorType type = GeneratorType.valueOf(args[2].toUpperCase());
                    Generator gen = new Generator(aGen, type, p.getLocation());
                    aGen.getGeneratorManager().register(gen);
                    if (!aGen.getGeneratorManager().isRunning()) aGen.startGenerators();
                    p.sendMessage(ChatColor.GREEN + type.name() + " generator added at your location!");
                } catch (IllegalArgumentException e) {
                    p.sendMessage(ChatColor.RED + "Invalid type. Use IRON, GOLD, DIAMOND, or EMERALD.");
                }
                break;

            case "setboundary":
                if (args.length < 3) { p.sendMessage(ChatColor.RED + "Usage: /bw setboundary <arenaName> <pos1|pos2>"); return true; }
                Arena aBound = am.getArena(args[1]);
                if (aBound == null) { p.sendMessage(ChatColor.RED + "Arena not found."); return true; }
                
                if (args[2].equalsIgnoreCase("pos1")) {
                    aBound.setPos1(p.getLocation());
                    p.sendMessage(ChatColor.GREEN + "Boundary Pos1 set for arena " + args[1] + " at your location!");
                } else if (args[2].equalsIgnoreCase("pos2")) {
                    aBound.setPos2(p.getLocation());
                    p.sendMessage(ChatColor.GREEN + "Boundary Pos2 set for arena " + args[1] + " at your location!");
                } else {
                    p.sendMessage(ChatColor.RED + "Invalid position. Use pos1 or pos2.");
                }
                break;

            case "save":
                if (args.length < 2) { p.sendMessage(ChatColor.RED + "Usage: /bw save <arenaName>"); return true; }
                Arena aSave = am.getArena(args[1]);
                if (aSave == null) { p.sendMessage(ChatColor.RED + "Arena not found."); return true; }
                am.saveArena(aSave);
                p.sendMessage(ChatColor.GREEN + "Arena " + args[1] + " saved to arenas.yml!");
                break;

            case "info":
                if (args.length < 2) { p.sendMessage(ChatColor.RED + "Usage: /bw info <arenaName>"); return true; }
                Arena aInfo = am.getArena(args[1]);
                if (aInfo == null) { p.sendMessage(ChatColor.RED + "Arena not found."); return true; }
                
                p.sendMessage(ChatColor.AQUA + "=== Arena: " + aInfo.getName() + " ===");
                p.sendMessage(ChatColor.AQUA + "Lobby set: " + (aInfo.getLobby() != null));
                p.sendMessage(ChatColor.AQUA + "Pos1 set: " + (aInfo.getPos1() != null));
                p.sendMessage(ChatColor.AQUA + "Pos2 set: " + (aInfo.getPos2() != null));
                p.sendMessage(ChatColor.AQUA + "Teams: " + aInfo.getTeams().size());
                for (Team t : aInfo.getTeams()) {
                    p.sendMessage(ChatColor.AQUA + " - " + t.getColoredName() + 
                        " | Spawn: " + (t.getSpawn() != null) + 
                        " | Bed: " + (t.getBedHead() != null) + 
                        " | Shop: " + (t.getShop() != null) + 
                        " | Upgrades: " + (t.getUpgradeShop() != null));
                }
                p.sendMessage(ChatColor.AQUA + "Generators: " + aInfo.getGeneratorManager().getGenerators().size());
                break;

            default:
                p.sendMessage(ChatColor.RED + "Unknown subcommand.");
                break;
        }
        return true;
    }

    private Block getPartnerBed(Block b) {
        if (!(b.getState().getData() instanceof Bed)) return null;
        Bed bedData = (Bed) b.getState().getData();
        BlockFace partnerFace = bedData.isHeadOfBed() ? bedData.getFacing().getOppositeFace() : bedData.getFacing();
        return b.getRelative(partnerFace);
    }
}
