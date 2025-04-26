/*  ModerationManager.java The purpose of this program is to handle all explicit
 *  strings from the program's endpoints.
 *  Copyright (C) 2024  github.com/brandongrahamcobb
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.brandongcobb.lucy.utils.handlers;

import com.brandongcobb.lucy.bots.DiscordBot;
import com.brandongcobb.lucy.utils.handlers.ConfigManager;
import com.brandongcobb.lucy.utils.handlers.MessageManager;
import com.brandongcobb.lucy.Lucy;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.time.Duration;
import java.util.concurrent.locks.Lock;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

public class ModerationManager {

    private Lucy app;
    private ConfigManager configManager;
    private DiscordBot discordBot;
    private final Object fileLock;
    private Lock lock;
    private Logger logger;
    private MessageManager messageManager;
    private File temporaryFile;

    public ModerationManager(Lucy application) {
        Lucy.moderationManager = this;
        this.app = application;
        this.configManager = app.configManager;
        this.discordBot = app.discordBot;
        this.fileLock = new Object();
        this.logger = app.logger;
        this.temporaryFile = app.temporaryFile;
    }

    public void handleModeration(Message message, String reasonStr) {
        User author = message.getAuthor().asUser().orElse(null);
        Server server = message.getServer().orElse(null);
        if (server == null || author == null) {
            return;
        }
        if (author.getRoles(server).stream().anyMatch(role -> role.getName().equals(configManager.getConfigValue("discord_role_pass")))) {
            return;
        }
        long userId = author.getId();
        Map<Long, Integer> userCounts = new HashMap<>();
        synchronized (fileLock) {
            if (temporaryFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(temporaryFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(":");
                        if (parts.length == 2) {
                            try {
                                long id = Long.parseLong(parts[0]);
                                int count = Integer.parseInt(parts[1]);
                                userCounts.put(id, count);
                            } catch (NumberFormatException e) {
                                // ignore malformed lines
                            }
                        }
                    }
                } catch (IOException e) {
                    logger.severe("Failed to read temporaryFile: " + e.getMessage());
                    return;
                }
            }
        }
        int flaggedCount = userCounts.getOrDefault(userId, 0) + 1;
        userCounts.put(userId, flaggedCount);
        synchronized (fileLock) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(temporaryFile, false))) {
                for (Map.Entry<Long, Integer> entry : userCounts.entrySet()) {
                    writer.write(entry.getKey() + ":" + entry.getValue());
                    writer.newLine();
                }
            } catch (IOException e) {
                logger.severe("Failed to write to temporaryFile: " + e.getMessage());
                return;
            }
        }
        message.delete();
        String moderationWarning = (String) configManager.getConfigValue("discord_moderation_warning");
        if (flaggedCount == 1) {
            messageManager.sendDiscordMessage(message, moderationWarning + ". Your message was flagged for: " + reasonStr);
        } else if (flaggedCount >= 2 && flaggedCount <= 4) {
            if (flaggedCount == 4) {
                messageManager.sendDiscordMessage(message, moderationWarning + ". Your message was flagged for: " + reasonStr);
            }
        } else if (flaggedCount >= 5) {
            messageManager.sendDiscordMessage(message, moderationWarning + ". Your message was flagged for: " + reasonStr);
            messageManager.sendDiscordMessage(message, "You have been timed out for 5 minutes due to repeated violations.");
            author.timeout(server, Duration.ofSeconds(300), reasonStr);
            userCounts.put(userId, 0);
            synchronized (fileLock) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(temporaryFile, false))) {
                    for (Map.Entry<Long, Integer> entry : userCounts.entrySet()) {
                        writer.write(entry.getKey() + ":" + entry.getValue());
                        writer.newLine();
                    }
                } catch (IOException e) {
                    logger.severe("Failed to reset counts in temporaryFile: " + e.getMessage());
                }
            }
        }
    }

    public Server getServerById(long serverId) {
        Set<Server> servers = discordBot.getApi().getServers();
        for (Server server : servers) {
            if (server.getId() == serverId) {
                return server;
            }
        }
        return null;
    }
}
