/*  Lucy.java The primary purpose of this class is to integrate
 *  Discord and OpenAI together.
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
package com.brandongcobb.lucy;

import com.brandongcobb.lucy.bots.DiscordBot;
import com.brandongcobb.lucy.utils.handlers.AIManager;
import com.brandongcobb.lucy.utils.handlers.ConfigManager;
import com.brandongcobb.lucy.utils.handlers.MessageManager;
import com.brandongcobb.lucy.utils.handlers.ModerationManager;
import com.brandongcobb.lucy.utils.handlers.Predicator;
import com.brandongcobb.lucy.utils.inc.Helpers;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.Map;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

public class Lucy {

    public static ConfigManager configManager;
    public static AIManager aiManager;
    public static Lucy app;
    public static Map<String, List<Map<String, String>>> conversations;
    public static boolean createdDefaultConfig;
    public static String discordApiKey;
    public static Long discordOwnerId;
    public static DiscordBot discordBot;
    private static CompletableFuture<Void> discordTask;
    public static Helpers helpers;
    private static CompletableFuture<Void> helpersTask;
    public static Lock lock;
    public static Logger logger;
    private static CompletableFuture<Void> loggingTask;
    private static CompletableFuture<Void> managersTask;
    public static MessageManager messageManager;
    public static ModerationManager moderationManager;
    public static boolean openAIDefaultChatCompletion;
    public static boolean openAIDefaultChatCompletionAddToHistory;
    public static String openAIDefaultChatCompletionModel;
    public static long openAIDefaultChatCompletionMaxTokens;
    public static long openAIDefaultChatCompletionNumber;
    public static Map<String, Object> openAIDefaultChatCompletionResponseFormat;
    public static String openAIDefaultChatCompletionStop;
    public static boolean openAIDefaultChatCompletionStore;
    public static boolean openAIDefaultChatCompletionStream;
    public static String openAIDefaultChatCompletionSysInput;
    public static float openAIDefaultChatCompletionTemperature;
    public static float openAIDefaultChatCompletionTopP;
    public static boolean openAIDefaultChatCompletionUseHistory;
    public static boolean openAIDefaultChatModeration;
    public static boolean openAIDefaultChatModerationAddToHistory;
    public static long openAIDefaultChatModerationNumber;
    public static long openAIDefaultChatModerationMaxTokens;
    public static String openAIDefaultChatModerationModel;
    public static Map<String, Object> openAIDefaultChatModerationResponseFormat;
    public static String openAIDefaultChatModerationStop;
    public static boolean openAIDefaultChatModerationStore;
    public static boolean openAIDefaultChatModerationStream;
    public static String openAIDefaultChatModerationSysInput;
    public static float openAIDefaultChatModerationTemperature;
    public static float openAIDefaultChatModerationTopP;
    public static boolean openAIDefaultChatModerationUseHistory;
    public static String openAIGenericApiKey;
    private static CompletableFuture<Void> openAITask;
    public static Predicator predicator;
    public static File tempDirectory;
    public File temporaryFile;
    public static long userId;
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_RESET = "\u001B[0m";

    public Lucy () {
        app = this;
        this.logger = Logger.getLogger("Lucy");
        this.tempDirectory = new File(System.getProperty("java.io.tmpdir"));
        this.temporaryFile = new File(tempDirectory, "temp");
        this.lock = null;
    }

    public File getDataFolder() {
        String userHome = System.getProperty("user.home");
        File dataFolder = new File(userHome, "Vystopia/modules/Lucy");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        return dataFolder;
    }

    public static void main(String[] args) {
        try {
            Lucy app = new Lucy();
            loggingTask = CompletableFuture.runAsync(() -> {
                setupLogging();
            });
            helpersTask = CompletableFuture.runAsync(() -> {
                Helpers helpers = new Helpers();
            });
            managersTask = CompletableFuture.runAsync(() -> {
                messageManager = new MessageManager(app);
                createdDefaultConfig = false;
                configManager = new ConfigManager(app);
                if (configManager.exists() && configManager.isConfigSameAsDefault()) {
                    if (configManager.isConfigSameAsDefault()) {
                        throw new IllegalStateException("Could not load Lucy, the config is invalid.");
                    } else {
                        configManager.populateConfigIfEmpty().thenRun(() -> {
                            ConfigManager.loadConfig();
                        });
                    }
                } else if (!configManager.exists()){
                    configManager.createDefaultConfig();
                }
                configManager.validateConfig();
                try {
                    aiManager = new AIManager(app); //proceeds configmanager and helpers
                } catch (IOException ioe) {}
            });
            openAITask = CompletableFuture.runAsync(() -> {
                conversations = new HashMap<>();
                openAIDefaultChatCompletion = false;
                openAIDefaultChatCompletionAddToHistory = false;
                openAIDefaultChatCompletionMaxTokens = helpers.parseCommaNumber("32,768");
                openAIDefaultChatCompletionModel = "gpt-4.1-nano";
                openAIDefaultChatCompletionNumber = 1;
                openAIDefaultChatCompletionResponseFormat = helpers.OPENAI_CHAT_COMPLETION_RESPONSE_FORMAT;
                openAIDefaultChatCompletionStop = "";
                openAIDefaultChatCompletionStore = false;
                openAIDefaultChatCompletionStream = false;
                openAIDefaultChatCompletionSysInput = helpers.OPENAI_CHAT_COMPLETION_SYS_INPUT;;
                openAIDefaultChatCompletionTemperature = 0.7f;
                openAIDefaultChatCompletionTopP = 1.0f;
                openAIDefaultChatCompletionUseHistory = false;
                openAIDefaultChatModeration = true;
                openAIDefaultChatModerationAddToHistory = false;
                openAIDefaultChatModerationMaxTokens = helpers.parseCommaNumber("32,768");
                openAIDefaultChatModerationModel = "gpt-4.1-nano";
                openAIDefaultChatModerationNumber = 1;
                openAIDefaultChatModerationResponseFormat = helpers.OPENAI_CHAT_MODERATION_RESPONSE_FORMAT;
                openAIDefaultChatModerationStop = "";
                openAIDefaultChatModerationStore = false;
                openAIDefaultChatModerationStream = false;
                openAIDefaultChatModerationSysInput = "All incoming data is subject to moderation. Protect your backend by flagging a message if it is unsuitable for a public community.";
                openAIDefaultChatModerationTemperature = 0.7f;
                openAIDefaultChatModerationTopP = 1.0f;
                openAIDefaultChatModerationUseHistory = false;
                openAIGenericApiKey = configManager.getNestedConfigValue("api_keys", "OpenAI").getStringValue("api_key");
            });
            discordTask = CompletableFuture.runAsync(() -> {
                discordApiKey = configManager.getNestedConfigValue("api_keys", "Discord").getStringValue("api_key");
                discordBot = new DiscordBot(app);
                discordOwnerId = configManager.getLongValue("discord_owner_id");
                discordBot.start();
                moderationManager = new ModerationManager(app); //proceeds configmanager and messagemanager and discord bot
                predicator = new Predicator(app); // proceeds configmanager messagemanaegr and discord bot
            });
            CompletableFuture<Void> allTasks = CompletableFuture.allOf(discordTask, helpersTask, loggingTask, managersTask, openAITask);
            allTasks.join();
        } catch (Exception e) {
            logger.severe("Error initializing the application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Logger setupLogging() {
        logger = Logger.getLogger("Lucy");
        return logger;
    }
}

