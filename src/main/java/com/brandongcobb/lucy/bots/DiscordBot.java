/*  DiscordBot.java The purpose of this program is to be a drop in replacement
    for any discord bot library (JDA, Javacord, Discord4J). Current: Javacord.
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
package com.brandongcobb.lucy.bots;

import com.brandongcobb.lucy.bots.DiscordBot;
import com.brandongcobb.lucy.cogs.EventListeners;
import com.brandongcobb.lucy.cogs.Cog;
import com.brandongcobb.lucy.utils.handlers.AIManager;
import com.brandongcobb.lucy.utils.handlers.ConfigManager;
import com.brandongcobb.lucy.utils.handlers.MessageManager;
import com.brandongcobb.lucy.utils.handlers.ModerationManager;
import com.brandongcobb.lucy.utils.handlers.Predicator;
import com.brandongcobb.lucy.Lucy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.List;
import java.util.logging.Logger;
import java.util.Map;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.entity.intent.Intent;

public class DiscordBot {

    public static AIManager aiManager;
    private static Lucy app;
    private DiscordApi api;
    private String discordApiKey;
    private static ConfigManager configManager;
    private Lock lock;
    private static Logger logger;
    public static MessageManager messageManager;
    public static ModerationManager moderationManager;
    public static Predicator predicator;

    public DiscordBot(Lucy application) {
        this.app = application;
        this.configManager = app.configManager;
        this.discordApiKey = configManager.getNestedConfigValue("api_keys", "Discord").getStringValue("api_key");
        this.lock = app.lock;
        this.logger = app.logger;
        initiateDiscordApi();
    }

    private void initiateDiscordApi() {
        this.api = new DiscordApiBuilder().setToken(discordApiKey).addIntents(Intent.MESSAGE_CONTENT).login().join();
        loadCogs();
    }

    private void loadCogs() {
        List<Cog> cogs = new ArrayList<>();
        cogs.add(new EventListeners(app));
        for (Cog cog : cogs) {
            cog.register(this.api);
        }
    }

    public DiscordApi getApi() {
        return this.api;
    }

    public void start() {
        logger.info("Discord bot started!");
    }
}
