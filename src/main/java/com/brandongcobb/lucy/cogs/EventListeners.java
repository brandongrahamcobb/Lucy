/*  EventListeners.java The purpose of this program is to listen for any of the program's endpoints and handles them.
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
package com.brandongcobb.lucy.cogs;

import com.brandongcobb.lucy.bots.DiscordBot;
import com.brandongcobb.lucy.Lucy;
import com.brandongcobb.lucy.utils.handlers.ConfigManager;
import com.brandongcobb.lucy.utils.handlers.MessageManager.MessageContent;
import com.brandongcobb.lucy.utils.handlers.MessageManager;
import com.brandongcobb.lucy.utils.handlers.ModerationManager;
import com.brandongcobb.lucy.utils.handlers.AIManager;
import com.brandongcobb.lucy.utils.handlers.Predicator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.logging.Logger;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class EventListeners implements Cog {

    private final Lucy app;
    private AIManager aiManager;
    private DiscordApi api;
    private ConfigManager configManager;
    private DiscordBot bot;
    private boolean flagged;
    private int i;
    private Lock lock;
    private long messageId;
    private MessageManager messageManager;
    private ObjectMapper mapper;
    private List<Boolean> overall;
    private List<String> reasons;
    private ModerationManager moderationManager;
    private Predicator predicator;
    private final Set<Long> processedMessages = new HashSet<>();
    private long senderId;

    public EventListeners (Lucy application) {
        this.app = application;
        this.configManager = app.configManager;
        this.bot = app.discordBot;
        this.aiManager = app.aiManager;
        this.lock = app.lock;
        this.messageId = 0L;
        this.messageManager = app.messageManager;
        ObjectMapper mapper = new ObjectMapper();
        this.moderationManager = app.moderationManager;
        List<Boolean> overall = new ArrayList<>();
        this.predicator = app.predicator;
        List<String> reasons = new ArrayList<>();
        this.senderId = 0L;
    }

    @Override
    public void register (DiscordApi api) {
        api.addMessageCreateListener(new MessageCreateListener() {
            @Override
            public void onMessageCreate(MessageCreateEvent event) {
                Message message = event.getMessage();
                if (message.getAuthor().isBotUser()) {
                    return;
                }
                String content = event.getMessageContent();
                List<MessageAttachment> attachments = message.getAttachments();
                CompletableFuture<List<MessageContent>> inputArray = messageManager.processArray(content, attachments);
                User sender = message.getAuthor().asUser().orElse(null);
                senderId = sender.getId();
                if (configManager.getBooleanValue("openai_chat_moderation") && !predicator.isDeveloper(sender)) {
                    List<Boolean> overall = new ArrayList<>();
                    List<String> reasons = new ArrayList<>();
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        CompletableFuture<String> moderationResponse = aiManager.getChatModerationCompletion(sender.getId(), inputArray);
                        moderationResponse.thenAccept(response -> {
                            try {
                                Map<String, Object> responseMap = mapper.readValue(response, new TypeReference<Map<String, Object>>() {});
                                List<Map<String, Object>> results = (List<Map<String, Object>>) responseMap.get("results");
                                if (results != null && !results.isEmpty()) {
                                    Map<String, Object> result = results.get(0);
                                    boolean flagged = (boolean) result.get("flagged");
                                    Map<String, Boolean> categories = (Map<String, Boolean>) result.get("categories");
                                    for (Map.Entry<String, Boolean> entry : categories.entrySet()) {
                                        if (Boolean.TRUE.equals(entry.getValue())) {
                                            String category = entry.getKey()
                                                .replace("/", " → ")
                                               .replace("-", " ");
                                            category = capitalize(category);
                                            reasons.add(category);
                                        }
                                    }
                                    overall.add(flagged);
                                    for (i = 0; i < reasons.size(); i++) {
                                         moderationManager.handleModeration(message, reasons.get(i));
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }).exceptionally(e -> {
                            e.printStackTrace();
                            return null;
                        });
                        boolean hasTrue = overall.stream().anyMatch(Boolean::booleanValue);
                        if (!hasTrue) {
                            if (Boolean.parseBoolean(configManager.getConfigValue("openai_chat_completion").toString()) && message.getMentionedUsers().contains(event.getApi().getYourself())) {
                                CompletableFuture<String> chatResponse = aiManager.getCompletion(senderId, inputArray);
                                chatResponse.thenAccept(response -> {
                                    if (response.length() > 2000) {
                                        List<String> responses = aiManager.splitLongResponse(response, 1950);
                                        String[] responsesArray = responses.toArray(new String[0]);
                                        for (String resp : responsesArray) {
                                            messageManager.sendDiscordMessage(message, resp);
                                        }
                                    } else {
                                        messageManager.sendDiscordMessage(message, response);
                                    }
                                });
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static List<Map<String, Object>> convertStringToList(String jsonString) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonString, new TypeReference<List<Map<String, Object>>>() {});
    }

    private static String capitalize(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }
}

