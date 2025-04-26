/*  Predicator.java The purpose of this program is to run lambda functions before executing code on the main thread.
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

import com.brandongcobb.lucy.Lucy;
import com.brandongcobb.lucy.bots.DiscordBot;
import com.brandongcobb.lucy.utils.handlers.ConfigManager;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.user.User;

public class Predicator {

    private Lucy app;
    private ConfigManager configManager;
    private DiscordBot bot;

    public Predicator(Lucy application) {
        Lucy.predicator = this;
        this.app = application;
        this.configManager = app.configManager;
        this.bot = app.discordBot;
    }

    public boolean atHome(Server server) {
        return server != null && server.getId() == (long) configManager.getConfigValue("discord_testing_guild_id");
    }

    public boolean releaseMode(User user, ServerTextChannel channel) {
        return (Long) user.getId() == configManager.getConfigValue("discord_owner_id") || // Your developer ID
               (boolean) configManager.getConfigValue("discord_release_mode") ||
               (channel instanceof PrivateChannel);
    }

    public boolean isDeveloper(User user) {
        return user != null && String.valueOf(user.getId()).equals(configManager.getLongValue("discord_owner_id"));
    }

    public boolean isVeganUser(User user) {
        List<Long> serverIds = (List<Long>) configManager.getConfigValue("discord_testing_guild_ids");
        for (Long serverId : serverIds) {
            Server server = getServerById(serverId);
            if (server != null) {
                Optional<Role> veganRoleOpt = server.getRoles().stream()
                    .filter(role -> role.getName().equals("vegan"))
                    .findFirst();
                if (veganRoleOpt.isPresent()) {
                    Role veganRole = veganRoleOpt.get();
                    if (veganRole.getUsers().contains(user)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Server getServerById(long serverId) {
        Set<Server> servers = bot.getApi().getServers(); // Get all servers
        for (Server server : servers) {
            if (server.getId() == serverId) {
                return server; // Return the server wrapped in an Optional
            }
        }
        return null;
    }

    public boolean isReleaseMode(ServerTextChannel channel, User user) {
        return (Long) user.getId() == configManager.getConfigValue("discord_owner_id") || // Your developer ID
               (boolean) configManager.getConfigValue("discord_release_mode") ||
               (channel instanceof PrivateChannel);
    }
}
