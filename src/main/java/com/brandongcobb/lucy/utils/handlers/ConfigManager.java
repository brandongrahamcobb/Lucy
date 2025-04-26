package com.brandongcobb.lucy.utils.handlers;

import com.brandongcobb.lucy.Lucy;
import com.brandongcobb.lucy.utils.inc.Helpers;

import java.io.*;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.Scanner;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public  class ConfigManager {

    private static File configFile;
    private static Lucy app;
    public static Map<String, Object> config;
    public static Map<String, Object> defaultConfig;
    public ConfigSection configSection;
    private static Helpers helpers;
    private Map<String, Object> inputConfigMap;
    private static Logger logger;

    public ConfigManager(Lucy application) {
        Lucy.configManager = this;
        this.app = application;
        this.logger = app.logger;
        this.helpers = new Helpers();
        this.config = new HashMap<>();
        this.configFile = new File(app.getDataFolder(), "config.yml");
        loadConfig();
        this.defaultConfig = new HashMap<>();
    }

    public static Map<String, Object> getConfig() {
        return config;
    }

    public boolean exists() {
        return configFile.exists();
    }

    public static void createDefaultConfig() {
        populateConfig(config);
        saveConfig(configFile); // Save the default config
    }

    private static Map<String, Object> populateConfig(Map<String, Object> configMap) {
        configMap.put("api_keys", new HashMap<String, Object>() {{
                put("Discord", new HashMap<String, String>() {{
                put("api_key", "");
                put("client_id", "");
                put("client_secret", "");
                put("redirect_uri", "");
            }});
            put("OpenAI", new HashMap<String, String>() {{
                put("api_key", "");
                put("client_id", "");
                put("client_secret", "");
                put("redirect_uri", "");
            }});
        }});
        configMap.put("discord_owner_id", "YOUR DISCORD ID");
        configMap.put("discord_role_pass", "ID FOR MODERATION BYPASS");
        configMap.put("discord_testing_guild_id", "MAIN GUILD ID");
        configMap.put("openai_chat_completion", false);
        configMap.put("openai_chat_moderation", true);
        configMap.put("openai_chat_stream", true);
        configMap.put("openai_chat_temperature", 0.7);
        configMap.put("openai_chat_top_p", 1.0);
        return configMap;
    }

    
    public CompletableFuture<Void> populateConfigIfEmpty() {
        return CompletableFuture.runAsync(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                Map<String, Object> currentConfig = ConfigManager.getConfig();
    
                // Checking 'api_keys' section
                if (currentConfig.get("api_keys") == null || ((Map<?, ?>) currentConfig.get("api_keys")).isEmpty()) {
                    System.out.println("API keys configuration is missing. Let's set it up.");
                    Map<String, Object> apiKeys = new HashMap<>();
                    for (String api : new String[]{"Discord", "OpenAI"}) {
                        System.out.println("Configuring API for " + api);
                        Map<String, String> apiConfig = new HashMap<>();
                        System.out.print("Enter " + api + " API key: ");
                        apiConfig.put("api_key", scanner.nextLine());
                        System.out.print("Enter " + api + " client ID: ");
                        apiConfig.put("client_id", scanner.nextLine());
                        System.out.print("Enter " + api + " client secret: ");
                        apiConfig.put("client_secret", scanner.nextLine());
                        System.out.print("Enter " + api + " redirect URI: ");
                        apiConfig.put("redirect_uri", scanner.nextLine());
                        apiKeys.put(api, apiConfig);
                    }
                    ((Map<String, Object>) currentConfig.get("api_keys")).putAll(apiKeys);
                }
    
                // Check and populate 'discord_owner_id'
                if (currentConfig.get("discord_owner_id") == null || ((String) currentConfig.get("discord_owner_id")).isEmpty()) {
                    System.out.print("Enter Discord owner ID: ");
                    String ownerId = scanner.nextLine();
                    currentConfig.put("discord_owner_id", ownerId);
                }
    
                // Check and populate 'discord_role_pass'
                if (currentConfig.get("discord_role_pass") == null || ((String) currentConfig.get("discord_role_pass")).isEmpty()) {
                    System.out.print("Enter Discord role pass ID: ");
                    String rolePassId = scanner.nextLine();
                    currentConfig.put("discord_role_pass", rolePassId);
                }
    
                // Check and populate 'discord_testing_guild_id'
                if (currentConfig.get("discord_testing_guild_id") == null || ((String) currentConfig.get("discord_testing_guild_id")).isEmpty()) {
                    System.out.print("Enter Discord testing guild ID: ");
                    String guildId = scanner.nextLine();
                    currentConfig.put("discord_testing_guild_id", guildId);
                }
                // Check and populate 'openai_chat_completion'
                if (currentConfig.get("openai_chat_completion") == null) {
                    System.out.print("Enable OpenAI Chat Completion (true/false): ");
                    boolean value = Boolean.parseBoolean(scanner.nextLine());
                    currentConfig.put("openai_chat_completion", value);
                }
                
                // Check and populate 'openai_chat_moderation'
                if (currentConfig.get("openai_chat_moderation") == null) {
                    System.out.print("Enable OpenAI Chat Moderation (true/false): ");
                    boolean value = Boolean.parseBoolean(scanner.nextLine());
                    currentConfig.put("openai_chat_moderation", value);
                }
                
                // Check and populate 'openai_chat_stream'
                if (currentConfig.get("openai_chat_stream") == null) {
                    System.out.print("Enable OpenAI Chat Stream (true/false): ");
                    boolean value = Boolean.parseBoolean(scanner.nextLine());
                    currentConfig.put("openai_chat_stream", value);
                }
                
                // Check and populate 'openai_chat_temperature'
                if (currentConfig.get("openai_chat_temperature") == null) {
                    System.out.print("Set OpenAI Chat Temperature (true/false): ");
                    boolean value = Boolean.parseBoolean(scanner.nextLine());
                    currentConfig.put("openai_chat_temperature", value);
                }
                
                // Check and populate 'openai_chat_top_p'
                if (currentConfig.get("openai_chat_top_p") == null) {
                    System.out.print("Set OpenAI Chat Top P (true/false): ");
                    boolean value = Boolean.parseBoolean(scanner.nextLine());
                    currentConfig.put("openai_chat_top_p", value);
                }
                ConfigManager.saveConfig(ConfigManager.configFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public boolean isConfigSameAsDefault() {
        return config.equals(defaultConfig);
    }

    public static void loadConfig() {
        if (configFile.exists()) {
            try (InputStream inputStream = new FileInputStream(configFile)) {
                Yaml yaml = new Yaml();
                config = yaml.load(inputStream);
            } catch (IOException e) {
                logger.severe("Failed to load config: " + e.getMessage());
            } catch (Exception e) {
                logger.severe("The config file is corrupted. Please delete it or fix it. Error: " + e.getMessage());
            }
        } else {
            createDefaultConfig();
            try (InputStream inputStream = new FileInputStream(configFile)) {
                Yaml yaml = new Yaml();
                config = yaml.load(inputStream);
            } catch (IOException e) {
                logger.severe("Failed to load config: " + e.getMessage());
            } catch (Exception e) {
                logger.severe("The config file is corrupted. Please delete it or fix it. Error: " + e.getMessage());
            }
        }
    }

    public Object getConfigValue(String key) {
        return config.get(key);
    }

    public String getStringValue(String key) {
        Object value = getConfigValue(key);
        if (value instanceof String) {
            return (String) value;
        }
        return null; // or throw an exception if you expect a String
    }

    public Integer getIntValue(String key) {
        Object value = getConfigValue(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null; // or throw an exception if you expect an Integer
    }
//    public Float getFloatValue(String key) {
//        Object value = getConfigValue(key);
//        if (value instanceof Number) {
//            return ((Number) value).floatValue();
//        } else if (value instanceof String) {
//            return Float.parseFloat((String) value);
//        }
//        return null; // or throw an exception if you expect a Float
//    }

    public Long getLongValue(String key) {
        Object value = getConfigValue(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            return Long.parseLong((String) value);
        }
        return null; // or throw an exception if you expect a Float
    }

    
        // Check if 'api_keys' section exists
    public Boolean getBooleanValue(String key) {
        Object value = getConfigValue(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            // Handle string representations of boolean, e.g., "true", "false"
            return Boolean.parseBoolean((String) value);
        }
        return null; // or throw an exception if you expect a Boolean
    }

    private static void saveConfig(File configFile) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        try {
            if (!app.getDataFolder().exists()) {
                app.getDataFolder().mkdirs(); // Create directories if they don't exist
            }
            try (Writer writer = new FileWriter(configFile)) {
                yaml.dump(config, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ConfigSection getNestedConfigValue(String outerKey, String innerKey) {
        Map<String, Object> outerMap = (Map<String, Object>) config.get(outerKey);
        if (outerMap != null) {
            Object innerValue = outerMap.get(innerKey);
            if (innerValue instanceof Map) {
                return new ConfigSection((Map<String, Object>) innerValue);
            }
        }
        return null; // Return null or handle accordingly if the outer key doesn't exist
    }

    public boolean validateConfig() {
        boolean anyValid = false; // Flag to check if at least one block is compliant

        // Validate each API configuration
        String[] apis = {"Discord", "OpenAI"};
        for (String api : apis) {
            boolean isValid = validateApiConfig(api);
            if (isValid) {
                anyValid = true; // At least one API configuration is valid
            }
        }

        boolean openAIValid = validateOpenAIConfig();
        if (openAIValid) {
            anyValid = true; // PostgreSQL configuration is valid
        }

        if (!anyValid) {
            logger.severe("No valid API configurations found. Please check your configuration.");
            // Optionally, throw an exception or halt further execution
        }
        return anyValid;
    }

    private boolean validateApiConfig(String api) {
        HashMap<String, String> settings = (HashMap<String, String>) ((Map<String, Object>) config.get("api_keys")).get(api);

        if (settings == null) {
            logger.severe(api + " configuration is missing.");
            return false; // Configuration block not present
        }

        boolean hasValidData = false; // Track if any setting is valid

        for (Map.Entry<String, String> entry : settings.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (value == null || value.trim().isEmpty()) {
                logger.warning(api + " setting '" + key + "' is missing or invalid.");
            } else {
                hasValidData = true; // Found at least one valid setting
            }
        }

        return hasValidData; // Returns true if at least one setting is valid
    }

    private boolean validateOpenAIConfig() {
        String openAIChatCompletion = (String) String.valueOf(config.get("openai_chat_completion"));
        String openAIChatModel = (String) String.valueOf(config.get("openai_chat_model"));
        String openAIChatModeration = (String) String.valueOf(config.get("openai_chat_moderation"));
        String openAIChatStop = (String) String.valueOf(config.get("openai_chat_stop"));
        boolean openAIChatStream = (boolean) Boolean.parseBoolean(String.valueOf(config.get("openai_chat_stream")));
        float openAIChatTemperature = (float) Float.parseFloat(String.valueOf(config.get("openai_chat_temperature")));
        float openAIChatTopP = (float) Float.parseFloat(String.valueOf(config.get("openai_chat_top_p")));
        boolean isValid = true;
        if (openAIChatModel == null || openAIChatModel.trim().isEmpty()) {
            logger.warning("OpenAI model setting is missing.");
            isValid = false;
        }
        if (openAIChatStop == null || openAIChatStop.trim().isEmpty()) {
            logger.warning("OpenAI user setting is missing.");
            isValid = false;
        }
        if (openAIChatStream == (boolean) false) {
            logger.warning("OpenAI chat streaming is disabled.");
            isValid = false;
        }
        if (openAIChatTopP < 0.0f || openAIChatTopP > 2.0f) {
            logger.warning("OpenAI chat top P is broken.");
            isValid = false;
        }
        if (openAIChatTemperature < 0.0f && openAIChatTemperature > 2.0f) {
            logger.warning("OpenAI chat temperature is broken.");
            isValid = false;
        }
        return isValid; // Returns true if the Postgres config is properly set
    }

    public class ConfigSection {

        private Map<String, Object> values;

        public ConfigSection(Map<String, Object> values) {
            this.values = values;
        }

        public String getStringValue(String key) {
            Object value = values.get(key);
            if (value instanceof String) {
                return (String) value;
            }
            return null; // or throw an exception if you expect a String
        }
    }
}
