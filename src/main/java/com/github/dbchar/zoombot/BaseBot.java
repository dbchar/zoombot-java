package com.github.dbchar.zoombot;

import com.github.dbchar.zoomapi.clients.OAuthZoomClient;
import com.github.dbchar.zoomapi.models.User;
import com.github.dbchar.zoomapi.utils.services.NgrokService;
import org.ini4j.Wini;

import java.io.File;

public class BaseBot {
    // region Private Properties

    private static final String ROOT_PATH_BOT_INI = "./";
    private static final String DEFAULT_FILE_BOT_INI = "bot.ini";
    private final String iniPath;
    private OAuthZoomClient client;
    private User user;

    // endregion

    // region Public Methods

    public BaseBot(String iniFile) {
        this.iniPath = ROOT_PATH_BOT_INI + iniFile;
    }

    public BaseBot() {
        this(DEFAULT_FILE_BOT_INI);
    }

    public void run() throws Exception {
        oauthLogin(iniPath);
        this.user = obtainUser();
        BotIO.printUserInfo(user);
    }

    public OAuthZoomClient getClient() {
        return client;
    }

    public User getUser() {
        return user;
    }

    // endregion

    // region Private Methods

    private void oauthLogin(String iniPath) throws Exception {
        final var CATEGORY = "OAuth";
        final var KEY_CLIENT_ID = "client_id";
        final var KEY_CLIENT_SECRET = "client_secret";
        final var KEY_PORT = "port";

        var ini = new Wini(new File(iniPath));
        var clientId = ini.get(CATEGORY, KEY_CLIENT_ID, String.class);
        var clientSecret = ini.get(CATEGORY, KEY_CLIENT_SECRET, String.class);
        var port = ini.get(CATEGORY, KEY_PORT, int.class);

        // ! YOU MUST HAVE INSTALLED ngrok TO RUN THIS BOT !
        var ngrok = new NgrokService(port);
        ngrok.start();
        client = new OAuthZoomClient(clientId, clientSecret, port, ngrok.getPublicUrl());
//    ngrok.stop();
    }

    private User obtainUser() {
        // First attempt to get user info
        var result = client.getUsersComponent().get("me");
        if (result.isSuccessOrRefreshToken(client)) {
            // First attempt succeeds
            return result.getItem();
        } else {
            // First attempt fails
            System.out.println("Fail to get user info.\nReason: " + result.getErrorMessage());
            System.out.println("Retrying to get user info...");
            // Second attempt
            result = client.getUsersComponent().get("me");
            if (result.isSuccess()) {
                // Second attempt succeeds
                return result.getItem();
            }
        }

        // Second attempt fails
        System.out.println("Fail to get user info. Exiting...");
        System.exit(1);
        return null;
    }

    // endregion
}
