/*  Helpers.java The purpose of this program is to support the Vytuous class
 *  for any values which would make the legibility of the code worsen if it
 *  was inluded explicitly.
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
package com.brandongcobb.lucy.utils.inc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.nio.file.Paths;
import java.util.List;
import java.util.HashMap;
import java.util.Arrays;

public class Helpers {

    private static String finalSchema;

    public static Long parseCommaNumber(String number) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < number.length(); i++) {
            char c = number.charAt(i);
            if (c != ',') {
                sb.append(c);
            }
        }
        String cleanedNumber = sb.toString();
        try {
            int intVal = Integer.parseInt(cleanedNumber);
            return (long) intVal; // safely fits in int
        } catch (NumberFormatException e) {
            return Long.parseLong(cleanedNumber);
        }
    }

    // Base directories
    public static final String DIR_BASE = Paths.get("/home/spawd/Vystopia/src/main/java/com/brandongcobb/").toAbsolutePath().toString(); // Placeholder
    public static final String DIR_HOME = System.getProperty("user.home");
    public static final String DIR_TEMP = Paths.get(DIR_BASE, "lucy", "temp").toString();

    // Paths
    public static final String PATH_LUCY = Paths.get(DIR_BASE, "lucy", "Lucy.java").toString();
    public static final String PATH_DISCORD_BOT = Paths.get(DIR_BASE, "lucy", "bots", "DiscordBot.java").toString();
    public static final String PATH_COG = Paths.get(DIR_BASE, "lucy", "cogs", "Cog.java").toString();
    public static final String PATH_EVENT_LISTENERS = Paths.get(DIR_BASE, "lucy", "cogs", "EventListeners.java").toString();
    public static final String PATH_AI_MANAGER = Paths.get(DIR_BASE, "lucy", "utils", "handlers", "AIManager.java").toString();
    public static final String PATH_CONFIG_MANAGER = Paths.get(DIR_BASE, "lucy", "utils", "handlers", "ConfigManager.java").toString();
    public static final String PATH_MESSAGE_MANAGER = Paths.get(DIR_BASE, "lucy", "utils", "handlers", "MessageManager.java").toString();
    public static final String PATH_MODEL_INFO = Paths.get(DIR_BASE, "lucy", "records", "ModelInfo.java").toString();
    public static final String PATH_MODERATION_MANAGER = Paths.get(DIR_BASE, "lucy", "utils", "handlers", "ModerationManager.java").toString();
    public static final String PATH_PATREON_USER = Paths.get(DIR_BASE, "lucy", "utils", "handlers", "PatreonUser.java").toString();
    public static final String PATH_PREDICATOR = Paths.get(DIR_BASE, "lucy", "utils", "handlers", "Predicator.java").toString();
    public static final String PATH_HELPERS = Paths.get(DIR_BASE, "lucy", "utils", "inc", "Helpers.java").toString();
    public static final String PATH_MODEL_REGISTRY = Paths.get(DIR_BASE, "lucy", "utils", "inc", "ModelRegistry.java").toString();

    public static final long[] DISCORD_CHARACTER_LIMITS = new long[]{parseCommaNumber("2,000"), parseCommaNumber("4,000")};
    public static final long DISCORD_CHARACTER_LIMIT = parseCommaNumber("2,000");
    public static final String[] DISCORD_COGS = new String[]{"lucy.cogs.EventListeners"};
    public static final String DISCORD_COMMAND_PREFIX = "!";
    public static final String DISCORD_MODERATION_WARNING = "You have been warned.";
    public static final long DISCORD_OWNER_ID = parseCommaNumber("154,749,533,429,956,608");
    public static final boolean DISCORD_RELEASE_MODE = false;
    public static final long DISCORD_ROLE_PASS = parseCommaNumber("1,308,689,505,158,565,918");
    public static final long DISCORD_TESTING_GUILD_ID = parseCommaNumber("1,300,517,536,001,036,348");

    public static final String LOGGING_LEVEL = "INFO";

    public static final boolean OPENAI_CHAT_ADD_COMPLETION_TO_HISTORY = true;
    public static final Map<String, Object> OPENAI_CHAT_COLORIZE_RESPONSE_FORMAT = createColorizeSchema();
    public static final boolean OPENAI_CHAT_COMPLETION = true;
    public static final Map<String, Object> OPENAI_CHAT_COMPLETION_RESPONSE_FORMAT = new HashMap<>();
    public static final Map<String, String> OPENAI_CHAT_HEADERS = Map.of(
        "Content-Type", "application/json",
        "OpenAI-Organization", "org-3LYwtg7DSFJ7RLn9bfk4hATf",
        "User-Agent", "brandongrahamcobb@icloud.com",
        "OpenAI-Project", "proj_u5htBCWX0LSHxkw45po1Vfz9"
    );
    public static final long OPENAI_CHAT_N = 1;
    public static final Map<String, List<String>> OPENAI_CHAT_MODELS = Map.of(
        "current", List.of("chatgpt-4o-mini-latest", "gpt-4.1", "gpt-4.1-nano", "o1-mini", "o1-preview", "o3-mini", "o4-mini"),
        "deprecated", List.of("chatgpt-4o-latest", "gpt-3.5-turbo", "gpt-4", "gpt-4-32k", "gpt-4o", "gpt-4o-mini", "gpt-4-turbo", "chatgpt-4o-latest")
    );
    public static final boolean OPENAI_CHAT_MODERATION = true;
    public static final String OPENAI_CHAT_MODERATION_MODEL = "gpt-4.1-nano";
    public static final Map<String, Object> OPENAI_CHAT_MODERATION_RESPONSE_FORMAT = createModerationSchema();
    public static final String OPENAI_CHAT_MODERATION_STOP = "";
    public static final boolean OPENAI_CHAT_MODERATION_STORE = false;
    public static final boolean OPENAI_CHAT_MODERATION_STREAM = false;
    public static final String OPENAI_CHAT_MODERATION_SYS_INPUT = "You are a JSON moderation assistant";
    public static final float OPENAI_CHAT_MODERATION_TEMPERATURE = 1.0f;
    public static final float OPENAI_CHAT_MODERATION_TOP_P = 1.0f;
    public static final String OPENAI_CHAT_MODEL = "gpt-4.1-nano";
    public static final boolean OPENAI_CHAT_MODERATION_USE_HISTORY = false;
    public static final boolean OPENAI_CHAT_MODERATION_ADD_COMPLETION_TO_HISTORY = false;
    public static final Map<String, Object> OPENAI_CHAT_RESPONSE_FORMAT = new HashMap<>();
    public static final String OPENAI_CHAT_STOP = "";
    public static final boolean OPENAI_CHAT_STORE = false;
    public static final boolean OPENAI_CHAT_STREAM = false;
    private static final String SUPER_STRING = PATH_AI_MANAGER + " | " + PATH_CONFIG_MANAGER + " | " +
                     PATH_MESSAGE_MANAGER + " | " + " | " + PATH_MODERATION_MANAGER + " | " +
                     PATH_PREDICATOR + " | " + PATH_LUCY;
    public static final String OPENAI_CHAT_COMPLETION_SYS_INPUT = "You are Lucy." + SUPER_STRING;
    public static final float OPENAI_CHAT_TOP_P = 1.0f;
    public static final float OPENAI_CHAT_TEMPERATURE = 0.7f;
    public static final boolean OPENAI_CHAT_USE_HISTORY = true;
    public static final String OPENAI_CHAT_USER = "Brandon Graham Cobb";
    public static final Map<String, String> OPENAI_ENDPOINT_URLS = Map.of(
        "audio", "https://api.openai.com/v1/audio/speech",
        "batch", "https://api.openai.com/v1/audio/batches",
        "chat", "https://api.openai.com/v1/chat/completions",
        "embeddings", "https://api.openai.com/v1/embeddings",
        "files", "https://api.openai.com/v1/files",
        "fine-tuning", "https://api.openai.com/v1/fine_tuning/jobs",
        "images", "https://api.openai.com/v1/images/generations",
        "models", "https://api.openai.com/v1/models",
        "moderations", "https://api.openai.com/v1/moderations",
        "uploads", "https://api.openai.com/v1/uploads"
    );
    public static final String OPENAI_MODERATION_MODEL = "omni-moderation-latest";
    public static final boolean OPENAI_MODERATION_IMAGE = true;

    public static final String USER_AGENT = "https://github.com/brandongrahamcobb/Lucy.git";
    public static final String VERSION = "1.0.0";

    private static Map<String, Object> createColorizeSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "json_schema");
        Map<String, Object> jsonSchema = new HashMap<>();
        jsonSchema.put("name", "colorize");
        jsonSchema.put("description", "A function that returns color values for a given request.");
        Map<String, Object> innerSchema = new HashMap<>();
        innerSchema.put("type", "object");
        Map<String, Object> properties = new HashMap<>();
        properties.put("r", Map.of("type", "integer", "minimum", 0, "maximum", 255));
        properties.put("g", Map.of("type", "integer", "minimum", 0, "maximum", 255));
        properties.put("b", Map.of("type", "integer", "minimum", 0, "maximum", 255));
        innerSchema.put("properties", properties);
        innerSchema.put("required", List.of("r", "g", "b"));
        innerSchema.put("additionalProperties", false);
        jsonSchema.put("schema", innerSchema);
        schema.put("json_schema", jsonSchema);
        return schema;
    }

    private static Map<String, Object> createModerationSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "json_schema");
        Map<String, Object> jsonSchema = new HashMap<>();
        jsonSchema.put("name", "moderation");
        jsonSchema.put("description", "A function that returns moderation results according to a specified schema.");
        Map<String, Object> properties = new HashMap<>();
        properties.put("id", Map.of("type", "string"));
        properties.put("model", Map.of("type", "string"));
        Map<String, Object> categoriesProps = new HashMap<>();
        String[] categoryKeys = {
            "sexual", "hate", "harassment", "self-harm", "sexual/minors",
            "hate/threatening", "violence/graphic", "self-harm/intent",
            "self-harm/instructions", "harassment/threatening", "violence",
        };
        for (String key : categoryKeys) {
            categoriesProps.put(key, Map.of("type", "boolean"));
        }
        Map<String, Object> categories = new HashMap<>();
        categories.put("type", "object");
        categories.put("properties", categoriesProps);
        categories.put("required", Arrays.asList(categoryKeys));
        Map<String, Object> scoresProps = new HashMap<>();
        for (String key : categoryKeys) {
            if (key.equals("animal-derived-technology")) {
                scoresProps.put(key, Map.of("type", "boolean"));
            } else {
                scoresProps.put(key, Map.of("type", "number"));
            }
        }
        Map<String, Object> categoryScores = new HashMap<>();
        categoryScores.put("type", "object");
        categoryScores.put("properties", scoresProps);
        categoryScores.put("required", Arrays.asList(categoryKeys));
        Map<String, Object> resultProps = new HashMap<>();
        resultProps.put("flagged", Map.of("type", "boolean"));
        resultProps.put("categories", categories);
        resultProps.put("category_scores", categoryScores);
        Map<String, Object> resultObject = new HashMap<>();
        resultObject.put("type", "object");
        resultObject.put("properties", resultProps);
        resultObject.put("required", Arrays.asList("flagged", "categories", "category_scores"));
        Map<String, Object> results = new HashMap<>();
        results.put("type", "array");
        results.put("items", resultObject);
        properties.put("results", results);
        Map<String, Object> mainSchema = new HashMap<>();
        mainSchema.put("type", "object");
        mainSchema.put("properties", properties);
        mainSchema.put("required", Arrays.asList("id", "model", "results"));
        mainSchema.put("additionalProperties", false);
        jsonSchema.put("schema", mainSchema);
        schema.put("json_schema", jsonSchema);
        return schema;
    }

}
