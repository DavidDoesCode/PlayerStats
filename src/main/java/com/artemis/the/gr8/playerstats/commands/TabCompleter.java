package com.artemis.the.gr8.playerstats.commands;

import com.artemis.the.gr8.playerstats.utils.EnumHandler;
import com.artemis.the.gr8.playerstats.utils.OfflinePlayerHandler;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class TabCompleter implements org.bukkit.command.TabCompleter {

    private final OfflinePlayerHandler offlinePlayerHandler;
    private final EnumHandler enumHandler;

    private List<String> statCommandTargets;
    private List<String> excludeCommandOptions;
    private List<String> itemsThatCanBreak;
    private List<String> entitiesThatCanDie;

    public TabCompleter() {
        offlinePlayerHandler = OfflinePlayerHandler.getInstance();
        enumHandler = EnumHandler.getInstance();
        prepareLists();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("statistic")) {
            return getStatCommandSuggestions(args);
        }
        else if (command.getName().equalsIgnoreCase("statisticexclude")) {
            return getExcludeCommandSuggestions(args);
        }
        return null;
    }

    private @Nullable List<String> getExcludeCommandSuggestions(@NotNull String[] args) {
        if (args.length == 0) {
            return null;
        }

        List<String> tabSuggestions = new ArrayList<>();
        if (args.length == 1) {
            tabSuggestions = excludeCommandOptions;
        }
        else if (args.length == 2) {
            tabSuggestions = switch (args[0]) {
                case "add" -> offlinePlayerHandler.getLoadedOfflinePlayerNames();
                case "remove" -> offlinePlayerHandler.getExcludedPlayerNames();
                case "info" -> {
                    ArrayList<String> loadedPlayers = offlinePlayerHandler.getLoadedOfflinePlayerNames();
                    loadedPlayers.addAll(offlinePlayerHandler.getExcludedPlayerNames());
                    yield loadedPlayers;
                }
                default -> tabSuggestions;
            };
        }
        return getDynamicTabSuggestions(tabSuggestions, args[args.length-1]);
    }

    private @Nullable List<String> getStatCommandSuggestions(@NotNull String[] args) {
        if (args.length == 0) {
            return null;
        }

        List<String> tabSuggestions = new ArrayList<>();
        if (args.length == 1) {
            tabSuggestions = firstStatCommandArgSuggestions();
        }
        else {
            String previousArg = args[args.length-2];

            //after checking if args[0] is a viable statistic, suggest sub-stat or targets
            if (enumHandler.isStatistic(previousArg)) {
                Statistic stat = enumHandler.getStatEnum(previousArg);
                if (stat != null) {
                    tabSuggestions = suggestionsAfterFirstStatCommandArg(stat);
                }
            }
            else if (previousArg.equalsIgnoreCase("player")) {
                if (args.length >= 3 && enumHandler.isEntityStatistic(args[args.length-3])) {
                    tabSuggestions = statCommandTargets;  //if arg before "player" was entity-sub-stat, suggest targets
                }
                else {  //otherwise "player" is the target: suggest playerNames
                    tabSuggestions = offlinePlayerHandler.getLoadedOfflinePlayerNames();
                }
            }

            //after a substatistic, suggest targets
            else if (enumHandler.isSubStatEntry(previousArg)) {
                tabSuggestions = statCommandTargets;
            }
        }
        return getDynamicTabSuggestions(tabSuggestions, args[args.length-1]);
    }

    /**
     * These tabSuggestions take into account that the commandSender
     * will have been typing, so they are filtered for the letters
     * that have already been typed.
     */
    private List<String> getDynamicTabSuggestions(@NotNull List<String> completeList, String currentArg) {
        return completeList.stream()
                .filter(item -> item.toLowerCase().contains(currentArg.toLowerCase()))
                .collect(Collectors.toList());
    }

    private @NotNull List<String> firstStatCommandArgSuggestions() {
        List<String> suggestions = enumHandler.getAllStatNames();
        suggestions.add("examples");
        suggestions.add("info");
        suggestions.add("help");
        return suggestions;
    }

    private List<String> suggestionsAfterFirstStatCommandArg(@NotNull Statistic stat) {
        switch (stat.getType()) {
            case BLOCK -> {
                return enumHandler.getAllBlockNames();
            }
            case ITEM -> {
                if (stat == Statistic.BREAK_ITEM) {
                    return itemsThatCanBreak;
                } else {
                    return enumHandler.getAllItemNames();
                }
            }
            case ENTITY -> {
                return entitiesThatCanDie;
            }
            default -> {
                return statCommandTargets;
            }
        }
    }

    private void prepareLists() {
        statCommandTargets = new ArrayList<>();
        statCommandTargets.add("top");
        statCommandTargets.add("player");
        statCommandTargets.add("server");
        statCommandTargets.add("me");

        excludeCommandOptions = new ArrayList<>();
        excludeCommandOptions.add("add");
        excludeCommandOptions.add("list");
        excludeCommandOptions.add("remove");
        excludeCommandOptions.add("info");

        //breaking an item means running its durability negative
        itemsThatCanBreak = Arrays.stream(Material.values())
                .parallel()
                .filter(Material::isItem)
                .filter(item -> item.getMaxDurability() != 0)
                .map(Material::toString)
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        //the only statistics dealing with entities are killed_entity and entity_killed_by
        entitiesThatCanDie = Arrays.stream(EntityType.values())
                .parallel()
                .filter(EntityType::isAlive)
                .map(EntityType::toString)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }
}