//package com.brandongcobb.vyrtuous.cogs;
//
//import com.brandongcobb.vyrtuous.Vyrtuous;
//import com.brandongcobb.vyrtuous.utils.handlers.ConfigManager;
//import com.brandongcobb.vyrtuous.utils.handlers.DiscordUser;
//import com.brandongcobb.vyrtuous.utils.handlers.OAuthServer;
//import com.brandongcobb.vyrtuous.utils.sec.DiscordOAuth;
//import com.brandongcobb.vyrtuous.utils.sec.PatreonOAuth;
//import com.brandongcobb.vyrtuous.utils.handlers.PatreonUser;
//import java.sql.Timestamp;
//import java.util.logging.Logger;
//import java.util.Timer;
//import java.util.TimerTask;
//import org.javacord.api.DiscordApi;
//import org.javacord.api.event.message.MessageCreateEvent;
//import org.javacord.api.listener.message.MessageCreateListener;
//
//
//public class HybridCommands implements Cog, MessageCreateListener {
//
//    private static String accessToken;
//    private static Vyrtuous app;
//    private static ConfigManager configManager;
//    private DiscordOAuth discordOAuth;
//    private String discordOAuthUrl;
//    private DiscordUser discordUser;
//    private Timer callbackTimer;
//    private boolean listeningForCallback = false;
//    private Logger logger;
//    private PatreonOAuth patreonOAuth;
//    private String patreonOAuthUrl;
//    private PatreonUser patreonUser;
//    private OAuthServer oAuthServer;
//    private String oAuthToken;
//    private Timestamp timestamp;
//
//    private HybridCommands (Vyrtuous application) {
//        this.app = application;
//        this.configManager = app.configManager;
////        this.accessToken = configManager.getNestedConfigValue("api_keys", "Discord").getStringValue("api_key");
//        this.discordOAuth = app.discordOAuth;
//        this.discordUser = app.discordUser;
//        this.logger = app.logger;
//        this.oAuthServer = app.oAuthServer;
//        this.patreonOAuth = app.patreonOAuth;
//        this.patreonUser = app.patreonUser;
//        this.timestamp = app.timestamp;
//    }
//
//    @Override
//    public void register(DiscordApi api) {
//        api.addMessageCreateListener(this);
//    }
//
//    @Override
//    public void onMessageCreate(MessageCreateEvent event) {
//        if (event.getMessageContent().toLowerCase().startsWith(".discord")) {
//            if (listeningForCallback) {
//                event.getChannel().sendMessage("Already listening for callback.");
//                return; // Just return; no need for a boolean return type here
//            }
//            oAuthServer.start();
//            listeningForCallback = true;
//            discordOAuthUrl = discordOAuth.getAuthorizationUrl();
//            event.getChannel().sendMessage("Please visit the following URL to authorize: " + discordOAuthUrl);
//            callbackTimer = new Timer();
//            callbackTimer.schedule(new TimerTask() {
//                @Override
//                public void run() {
//                    listeningForCallback = false; // Reset listening state
//                }
//            }, 600000); // 10 minutes
//            return; // Command processed successfully
//        }
//        if (event.getMessageContent().toLowerCase().startsWith(".patreon")) {
//            if (listeningForCallback) {
//                event.getChannel().sendMessage("Already listening for callback.");
//                return; // Just return; no need for a boolean return type here
//            }
//            oAuthServer.start();
//            listeningForCallback = true;
//            patreonOAuthUrl = patreonOAuth.getAuthorizationUrl();
//            event.getChannel().sendMessage("Please visit the following URL to authorize: " + patreonOAuthUrl);
//            callbackTimer = new Timer();
//            callbackTimer.schedule(new TimerTask() {
//                @Override
//                public void run() {
//                    listeningForCallback = false; // Reset listening state
//                }
//            }, 600000); // 10 minutes
//            return; // Command processed successfully
//        }
//    }
//
////    public void handleOAuthCallback(String code, String type) {
////        try {
////            if (!listeningForCallback) {
////                logger.warning("No OAuth flow is currently active.");
////                return;
////            }
////            switch (type) {
////                case "Discord":
////                    accessToken = discordOAuth.exchangeCodeForToken(code);
////                    long userId = Long.parseLong(String.valueOf(discordUser.getCurrentUserId(accessToken)));
////                    discordUser.userExists(String.valueOf(userId), exists -> {
////                        if (!exists) {
////                            discordUser.createUser(timestamp, userId, 0, "", 1, "", "", 0, "", 0L, "", "", "", "", () -> {});
////                        }
////                        break;
////                    });
////                case "Patreon":
////                    accessToken = patreonOAuth.exchangeCodeForToken(code);
////                    long userId = Long.parseLong(String.valueOf(patreonUser.getCurrentUserId(accessToken)));
////                    int userAmountCents = Integer.parseInt(String.valueOf(patreonUser.getCurrentPatreonAmountCents(accessToken)));
////                    patreonUser.userExists(String.valueOf(userId), exists -> {
////                        if (!exists) {
////                            patreonUser.createUser(timestamp, 0L, 0, "", 1, "", "", userAmountCents, "", userId, "", "", "", "", () -> {});
////                        }
////                        break;
////                    });
////                default:
////                    break;
////                listeningForCallback = false; // Reset the callback flag
////                if (callbackTimer != null) {
////                    callbackTimer.cancel();
////                    callbackTimer = null;
////                }
////            }
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
////    }
//}
