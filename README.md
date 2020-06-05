# zoombot-java

A simple bot based on [zoomapi-java](https://github.com/dbchar/zoomapi-java).

## Compatibility

This program requires **JDK 11** or above.

## Running the bot

### Config

First, please create a "bot.ini" file and set OAuth client ID, secret, and port.

Due to changes of Zoom's security policy, we are now asking bot users to start ngrok manually.

```shell script
ngrok http PORT_IN_BOT_INI
```

After that, please add both your https and http ngrok tunnel URLs to your "Whitelist URL" on your bot management page.

### Compile and Run

Then, navigate to the root directory of the project.

We strongly suggest running our bot with **IntelliJ IDEA** to get the best experience.

If you want to use command lines to run our bot, then:

If you are using Mac OS or Linux, then run:

```shell script
./gradlew run
```

If you are using Windows, then run:

```shell script
gradlew.bat run
```

### Invalid redirect url

If you encounter `Invalid redirect url (4,700)` when trying to log in Zoom, please add our ngrok URL (like `https://12345678.ngrok.io` and `http://12345678.ngrok.io`) to your app redirect whitelist.

