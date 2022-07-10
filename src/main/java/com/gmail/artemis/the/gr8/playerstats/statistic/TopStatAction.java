package com.gmail.artemis.the.gr8.playerstats.statistic;

import com.gmail.artemis.the.gr8.playerstats.models.StatRequest;
import com.gmail.artemis.the.gr8.playerstats.utils.MyLogger;
import com.gmail.artemis.the.gr8.playerstats.utils.OfflinePlayerHandler;
import com.google.common.collect.ImmutableList;
import org.bukkit.OfflinePlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveAction;


public class TopStatAction extends RecursiveAction {

    private final int threshold;

    private final ImmutableList<String> playerNames;
    private final StatRequest request;
    private final ConcurrentHashMap<String, Integer> playerStats;

    /**
     * Gets the statistic numbers for all players whose name is on the list, puts them in a ConcurrentHashMap
     * using the default ForkJoinPool, and returns the ConcurrentHashMap when everything is done
     * @param threshold the maximum length of playerNames to process in one task
     * @param playerNames ImmutableList of playerNames for players that should be included in stat calculations
     * @param statRequest a validated statRequest
     * @param playerStats the ConcurrentHashMap to put the results on
     */
    public TopStatAction(int threshold, ImmutableList<String> playerNames, StatRequest statRequest, ConcurrentHashMap<String, Integer> playerStats) {
        this.threshold = threshold;
        this.playerNames = playerNames;

        this.request = statRequest;
        this.playerStats = playerStats;

        MyLogger.subActionCreated(Thread.currentThread().getName());
    }

    @Override
    protected void compute() {
        if (playerNames.size() < threshold) {
            getStatsDirectly();
        }
        else {
            final TopStatAction subTask1 = new TopStatAction(threshold, playerNames.subList(0, playerNames.size()/2), request, playerStats);
            final TopStatAction subTask2 = new TopStatAction(threshold, playerNames.subList(playerNames.size()/2, playerNames.size()), request, playerStats);

            //queue and compute all subtasks in the right order
            invokeAll(subTask1, subTask2);
        }
    }

    private void getStatsDirectly() {
        Iterator<String> iterator = playerNames.iterator();
        if (iterator.hasNext()) {
            do {
                String playerName = iterator.next();
                MyLogger.actionRunning(Thread.currentThread().getName(), playerName, 2);
                OfflinePlayer player = OfflinePlayerHandler.getOfflinePlayer(playerName);
                if (player != null) {
                    int statistic = 0;
                    switch (request.getStatistic().getType()) {
                        case UNTYPED -> statistic = player.getStatistic(request.getStatistic());
                        case ENTITY -> statistic = player.getStatistic(request.getStatistic(), request.getEntity());
                        case BLOCK -> statistic = player.getStatistic(request.getStatistic(), request.getBlock());
                        case ITEM -> statistic = player.getStatistic(request.getStatistic(), request.getItem());
                    }
                    if (statistic > 0) {
                        playerStats.put(playerName, statistic);
                    }
                }
            } while (iterator.hasNext());
        }
    }
}