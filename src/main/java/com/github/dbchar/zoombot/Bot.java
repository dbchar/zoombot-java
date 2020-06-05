package com.github.dbchar.zoombot;

import com.github.dbchar.zoomapi.components.queries.PageConfiguration;
import com.github.dbchar.zoomapi.models.Channel;
import com.github.dbchar.zoomapi.models.Message;
import com.github.dbchar.zoomapi.utils.Logger;
import com.github.dbchar.zoomapi.utils.services.MonitorService;
import com.github.dbchar.zoomapi.utils.services.MonitorTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.dbchar.zoombot.BotIO.*;
import static com.github.dbchar.zoombot.BotIO.MainMenuCommand.*;

public class Bot extends BaseBot {
  // region Private Properties (Demo Functions)

  private final Map<Integer, Runnable> mainMenuFunctions = new HashMap<>();

  // endregion

  // region Public API

  public Bot(String iniPath) {
    super(iniPath);
  }

  public Bot() {
    super();
  }

  @Override
  public void run() throws Exception {
    super.run();

    addDemoFunctions();

    var command = COMMAND_INVALID;
    while (command != COMMAND_EXIT) {
      printMainMenu();
      command = getUserCommand("");

      if (mainMenuFunctions.containsKey(command)) {
        mainMenuFunctions.get(command).run();
      } else if (command != COMMAND_EXIT) {
        System.out.println("Command '" + command + "' is not supported, please enter a valid command!\n");
      }
    }

    releaseResources();
  }

  private void addDemoFunctions() {
    // add Main Menu
    mainMenuFunctions.put(M2_CHAT_CHANNEL_SET_FUNCTIONS.getValue(), executeSetOfChatChannelFunctions);
    mainMenuFunctions.put(M2_CHAT_CHANNEL_SINGLE_FUNCTION.getValue(), executeSingleChatChannelFunction);
    mainMenuFunctions.put(M2_CHAT_MESSAGE_SET_FUNCTIONS.getValue(), executeSetOfChatMessageFunctions);
    mainMenuFunctions.put(M2_CHAT_MESSAGE_SINGLE_FUNCTION.getValue(), executeSingleChatMessageFunction);
    mainMenuFunctions.put(M3_CHAT_SET_FUNCTIONS.getValue(), executeSetOfChatFunctions);
    mainMenuFunctions.put(M3_CHAT_SINGLE_FUNCTION.getValue(), executeSingleChatFunction);
    mainMenuFunctions.put(M4_START_MONITOR_CHANNEL.getValue(), startMonitoringAChannel);
    mainMenuFunctions.put(M4_DISPLAY_MONITOR_CHANNEL_NAME.getValue(), displayMonitoringChannels);
    mainMenuFunctions.put(M4_STOP_MONITOR_CHANNEL.getValue(), stopMonitoringAChannel);
    mainMenuFunctions.put(M4_STOP_MONITOR_ALL_CHANNELS.getValue(), stopMonitoringAllChannels);
    mainMenuFunctions.put(M5_LIST_CHANNELS.getValue(), listChannelsM5);
    mainMenuFunctions.put(M5_LIST_MEMBERS.getValue(), listMembersM5);
    mainMenuFunctions.put(M5_LIST_MESSAGES.getValue(), listMessagesM5);
    mainMenuFunctions.put(TOGGLE_DEBUG_OUTPUT.getValue(), toggleLoggerState);
  }

  // endregion

  // region Milestone 2

  private final Runnable executeSetOfChatChannelFunctions = () -> {
    println("# Executing a set of Chat Channel Functions...");

    // 1
    getUserInput("# Part 1: Test listing channels (Press Enter to continue)");
    listChannels();

    // 2
    getUserInput("# Part 2: Test creating a channel (Press Enter to continue)");
    var name = getUserInput("Please input a name for the channel: ");
    var channelResult = getClient().getChatChannelsComponent().create(name, List.of());
    var channelId = (String) null;
    if (channelResult.isSuccessOrRefreshToken(getClient())) {
      printChannelWithTitle("Succeed to create a channel", channelResult.getItem());
      channelId = channelResult.getItem().getId();
    } else {
      printMessageWithDivider("Fail to create a channel.\nReason: " + channelResult.getErrorMessage());
      return;
    }

    // 3
    getUserInput("# Part 3: Test getting a channel (Press Enter to continue)");
    getAChannel(channelId);

    // 4
    getUserInput("# Part 4: Test updating a channel (Press Enter to continue)");
    name = getUserInput("Please input a name for the channel: ");
    var booleanResult = getClient().getChatChannelsComponent().update(channelId, name);
    if (booleanResult.isSuccessOrRefreshToken(getClient())) {
      getAChannel(channelId);
    } else {
      return;
    }

    // 5
    getUserInput("# Part 5: Test listing members of a channel (Press Enter to continue)");
    listChannelMembers(channelId);

    // 6
    getUserInput("# Part 6: Test inviting a member to a channel (Press Enter to continue)");
    listExternalContacts();
    var email = getValidUserInputEmail();
    getClient().getChatChannelsComponent().inviteMembers(channelId, List.of(email));
    listChannelMembers(channelId);

    // 7
    getUserInput("# Part 7: Test removing a member from a channel (Press Enter to continue)");
    listChannelMembers(channelId);
    var memberId = getUserInput("Please input a member id (not email): ");
    getClient().getChatChannelsComponent().deleteMembers(channelId, memberId);
    listChannelMembers(channelId);

    // 8
    getUserInput("# Part 8: Test deleting a channel (Press Enter to continue)");
    listChannels();
    println("Deleting " + channelId);
    getClient().getChatChannelsComponent().delete(channelId);
    listChannels();

    // 9
//    print("By default we are testing with our channel 'test-leave-join'.")
    getUserInput("# Part 9: Test leaving a channel (Press Enter to continue)");
//    channelId = "1cb910ea028d4dee9c960bb4e14e8fdc"
    listChannels();
    channelId = getUserInput("Please input a valid channel ID from above: ");
    println("Leaving channel " + channelId);
    getClient().getChatChannelsComponent().leave(channelId);
    listChannels();

    // 10
    getUserInput("# Part 10: Test joining a channel (Press Enter to continue)");
    println("Joining channel " + channelId);
    getClient().getChatChannelsComponent().join(channelId);
    listChannels();
  };

  private final Runnable executeSingleChatChannelFunction = () -> {
    var command = COMMAND_INVALID;

    while (command != COMMAND_EXIT) {
      printChatChannelMenu(getUser().getEmail());
      command = getUserCommand(null);
      println();

      switch (command) {
        case 1:
          listChannels();
          break;
        case 2:
          createAChannel();
          break;
        case 3:
          getAChannel();
          break;
        case 4:
          updateAChannel();
          break;
        case 5:
          deleteAChannel();
          break;
        case 6:
          listChannelMembers();
          break;
        case 7:
          inviteChannelMembers();
          break;
        case 8:
          joinAChannel();
          break;
        case 9:
          leaveAChannel();
          break;
        case 10:
          removeAChannelMember();
          break;
        default:
          break;
      }
    }
  };

  private final Runnable executeSetOfChatMessageFunctions = () -> {
    println("# Executing a set of Chat Message Functions...");

    // 0
    final var channels = listChannels();
    if (channels == null) {
      return;
    }
    if (channels.isEmpty()) {
      println("Please join at least one channel.");
      return;
    }
    var i = 0;
    while (true) {
      if (1 <= i && i <= channels.size()) break;
      else i = getUserCommand("First, please input an integer [1," + channels.size() + "] to select a channel:");
    }
    var channel = channels.get(i - 1);
    var channelId = channel.getId();
    println("You have selected channel " + channel.getName());

    // 1
    getUserInput("# Part 1: Test sending messages (Press Enter to continue)");
    var message = getUserInput("Then, please send a message to the channel:\n");
    var stringResult = getClient().getChatMessagesComponent().send(message, null, channelId);
    var messageId = (String) null;
    if (stringResult.isSuccessOrRefreshToken(getClient())) {
      messageId = stringResult.getItem();
    } else {
      println("Something goes wrong. Please retry.");
      return;
    }

    // 2
    getUserInput("# Part 2: Test listing messages (Press Enter to continue)");
    println("Then please review the message history.");
    this.listChannelMessages(channelId);
    println("Did you see \"" + message + "\" there? Great.");

    // 3
    getUserInput("# Part 3: Test updating messages (Press Enter to continue)");
    println("Then we are going to update \"" + message + "\".");
    message = getUserInput("Please input a new message:\n");
    var booleanResult = getClient().getChatMessagesComponent().update(messageId, message, null, channelId);
    if (booleanResult.isSuccessOrRefreshToken(getClient())) {
      this.listChannelMessages(channelId);
      println("Did you see \"" + message + "\" there? Great.");
    } else {
      println("Something goes wrong. Please retry.");
      return;
    }

    // 4
    getUserInput("# Part 4: Test removing messages (Press Enter to continue)");
    println("Then we are going to delete \"" + message + "\".");
    booleanResult = getClient().getChatMessagesComponent().delete(messageId, null, channelId);
    if (booleanResult.isSuccessOrRefreshToken(getClient())) {
      this.listChannelMessages(channelId);
      println("Did you see \"" + message + "\" gone? Great.");
    } else {
      println("Something goes wrong. Please retry.");
      return;
    }

    println("# Execution finished.");
  };

  private final Runnable executeSingleChatMessageFunction = () -> {
    while (true) {
      final var channels = this.listChannels();
      if (channels == null || channels.isEmpty()) {
        break;
      }

      final var range = "[0, " + channels.size() + "]";
      int i;
      while (true) {
        i = getUserCommand("Please select a channel (0 to exit): ");
        if (COMMAND_EXIT <= i && i <= channels.size()) break;
        else println("Input should be within " + range);
      }
      if (i == COMMAND_EXIT) break;

      final var channel = channels.get(i - 1);
      final var channelName = channel.getName();
      println("You have selected channel " + channelName);

      final var channelId = channel.getId();
      if (channelId != null && !channelId.isEmpty()) {
        var command = COMMAND_INVALID;
        while (command != COMMAND_EXIT) {
          printChatMessageMenu(channelName);
          command = getUserCommand("");
          if (command == 1) {
            this.listChannelMessages(channelId);
          } else if (command == 2) {
            this.sendChannelMessages(channelId);
          } else if (command == 3) {
            this.updateAChannelMessage(channelId);
          } else if (command == 4) {
            this.deleteAChannelMessage(channelId);
          }
        }
      }
    }
  };

  // endregion

  // region Milestone 3

  private final Runnable executeSetOfChatFunctions = () -> {
    println("# Executing a set of Chat Functions...");
    try {
      // 0
      var channelName = listAndSelectChannel().getName();

      // 1
      getUserInput("# Part 1: Test sending messages (Press Enter to continue)");
      sendMessage(channelName, getUserInput("Please input the first message:"));
      sendMessage(channelName, getUserInput("Please input the second message:"));
      sendMessage(channelName, getUserInput("Please input the third message:"));

      // 2
      getUserInput("# Part 2: Test listing history (Press Enter to continue)");
      listHistory(channelName);

      // 4
      getUserInput("# Part 3: Test searching messages (Press Enter to continue)");
      searchMessageByContent(channelName, getUserInput("Please input a string to search messages by content:"));
      searchMessageBySender(channelName, getUserInput("Please input a string to search messages by their senders:"));

    } catch (Exception e) {
      System.err.println("Execution failed: " + e.getMessage());
    }
  };

  private final Runnable executeSingleChatFunction = () -> {
    try {
      final var channelName = listAndSelectChannel().getName();
      var command = COMMAND_INVALID;
      while (command != COMMAND_EXIT) {
        printChatMenu(channelName);
        command = getUserCommand("");
        switch (command) {
          case 1:
            sendMessage(channelName,
                    getUserInput("Please input a message:"));
            break;
          case 2:
            listHistory(channelName);
            break;
          case 3:
            searchMessageByContent(channelName,
                    getUserInput("Please input keywords for message contents:"));
            break;
          case 4:
            searchMessageBySender(channelName,
                    getUserInput("Please input keywords for message senders:"));
            break;
          default:
            break;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  };

  // endregion

  // region Milestone 4

  private final Runnable startMonitoringAChannel = () -> {
    try {
      var channelNameInput = listAndSelectChannel().getName();

      var dateRange = BotIO.getValidQueryDateParameters();
      var fromDate = dateRange.get(0);
      var toDate = dateRange.get(1);

      MonitorTask task = new MonitorTask(
              channelNameInput,
              fromDate,
              toDate,
              getClient());

      // OnMessageReceived: multiple listeners
      task.setOnMessageReceivedListeners(List.of(
              (channelName, message) ->
                      printChannelMessagesWithTitle("Message received at channel '" + channelName + "'", List.of(message), true)
      ));

      // OnMessageUpdated: multiple listeners
      task.setOnMessageUpdatedListeners(List.of(
              (channelName, message) ->
                      printChannelMessagesWithTitle("Message updated at channel '" + channelName + "'", List.of(message), true)
      ));

      // OnMemberAdded: multiple listeners (setOnMemberAddedListeners will clear original listeners first)
      task.setOnMemberAddedListeners(List.of(
              (channelName, user) ->
                      printMembersWithTitle("Member added at channel '" + channelName + "'", List.of(user))
      ));

      MonitorService.INSTANCE.startTask(task);
    } catch (Exception e) {
      System.err.println("Failed to start monitoring a channel: " + e.getMessage());
    }
  };

  private final Runnable stopMonitoringAChannel = () -> {
    mainMenuFunctions.get(M4_DISPLAY_MONITOR_CHANNEL_NAME.getValue()).run();
    MonitorService.INSTANCE.stopTask(getUserInput("Please input a channel name(ex. test): "));
  };

  private final Runnable stopMonitoringAllChannels = MonitorService.INSTANCE::stopAllTasks;

  private final Runnable displayMonitoringChannels = () -> {
    var channelNames = MonitorService.INSTANCE.getMonitoringChannelNames().toArray();
    printMessageWithDivider(channelNames.length == 0
            ? "Not monitoring any channels now."
            : "Monitoring channels: " + Arrays.toString(channelNames));
  };

  private void releaseResources() {
    MonitorService.INSTANCE.stopService();
//    ThrottlerService.INSTANCE.stop();
  }

  // endregion

  // region Milestone 5

  private final Runnable listMessagesM5 = () -> {
    try {
      var useCache = getTrueOrFalse("Use cache?");
      var channel = listAndSelectChannel(useCache);
      var title = "List cached message history in " + channel.getName();
      printTitle(title);

      var dateRange = BotIO.getValidQueryDateParameters();
      var fromDate = dateRange.get(0);
      var toDate = dateRange.get(1);

      printMessageListResult(
              title, fromDate, toDate,
              getClient().getChatComponent().history(
                      channel.getName(),
                      fromDate,
                      toDate,
                      useCache
              )
      );
    } catch (Exception e) {
      e.printStackTrace();
    }
  };

  private final Runnable listMembersM5 = () -> {
    try {
      var useCache = getTrueOrFalse("Use cache?");
      var channel = listAndSelectChannel(useCache);
      var result = getClient().getChatChannelsComponent()
              .listMembers(channel.getId(),
                      new PageConfiguration(PageConfiguration.MAX_PAGE_SIZE),
                      useCache);
      if (result.isSuccessOrRefreshToken(getClient())) {
        printMembersWithTitle("Succeed to list channel members", result.getItems());
        println();
      } else {
        printMessageWithDivider("Fail to list channel members.\nReason: " + result.getErrorMessage());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  };

  private final Runnable listChannelsM5 = () -> listChannels(getTrueOrFalse("Use cache?"));

  // endregion

  // region Others

  private final Runnable toggleLoggerState = () -> Logger.DISABLED = !Logger.DISABLED;

  private Channel listAndSelectChannel(boolean useCache) throws Exception {
    return selectChannel(listChannels(useCache));
  }

  private Channel listAndSelectChannel() throws Exception {
    return listAndSelectChannel(false);
  }

  private Channel selectChannel(List<Channel> channels) throws Exception {
    if (channels == null) {
      throw new Exception("Failed to get channels");
    }
    if (channels.isEmpty()) {
      throw new Exception("Please join at least one channel.");
    }
    var i = 0;
    while (true) {
      if (1 <= i && i <= channels.size()) break;
      else i = getUserCommand("First, please input an integer [1," + channels.size() + "] to select a channel:");
    }
    Channel channel = channels.get(i - 1);
    println("You have selected channel " + channel.getName());
    return channel;
  }

  // endregion

  // region Private Chat Message Functions

  private List<Message> listChannelMessages(String channelId) {
    printTitle("List channel messages");

    var result = getClient().getChatMessagesComponent()
            .list(getUser().getId(), null, channelId, null, null);
    if (result.isSuccessOrRefreshToken(getClient())) {
      printChannelMessagesWithTitle("Succeed to list channel messages", result.getItems(), true);
    } else {
      printMessageWithDivider("Fail to list channel messages.\nReason: " + result.getErrorMessage());
    }

    return result.getItems();
  }

  private void sendChannelMessages(String channelId) {
    while (true) {
      var message = getUserInput("Enter message ('q' to stop): ");
      if (message.equals(COMMAND_QUIT)) break;
      var result = getClient().getChatMessagesComponent()
              .send(message, null, channelId);
      if (result.isSuccessOrRefreshToken(getClient())) {
        println("Message sent. ID=" + result.getItem());
      } else {
        printMessageWithDivider("Fail to send the message.\nReason: " + result.getErrorMessage());
      }
    }
  }

  private void updateAChannelMessage(String channelId) {
    final var messages = listChannelMessages(channelId);
    if (messages == null || messages.isEmpty()) {
      println("No messages received. Please send a message first or switch channels.");
      return;
    }

    final var messageIdx = getUserCommand("Please input a number (0 to quit):");
    if (messageIdx == 0) return;
    final var messageId = messages.get(messageIdx - 1).getId();
    if (messageId.startsWith("{")) {
      println("You cannot update messages from others.");
      return;
    }

    final var messageNew = getUserInput("Please input a new message: ");

    var result = getClient().getChatMessagesComponent()
            .update(messageId, messageNew, null, channelId);
    if (result.isSuccessOrRefreshToken(getClient())) {
      println("Message updated.");
    } else {
      printMessageWithDivider("Fail to update the message.\nReason: " + result.getErrorMessage());
    }
  }

  private void deleteAChannelMessage(String channelId) {
    final var messages = listChannelMessages(channelId);
    if (messages == null || messages.isEmpty()) {
      println("No messages received. Please send a message first or switch channels.");
      return;
    }

    final var messageIdx = getUserCommand("Please input a number (0 to quit):");
    if (messageIdx == 0) return;
    final var messageId = messages.get(messageIdx - 1).getId();
    if (messageId.startsWith("{")) {
      println("You cannot delete messages from others.");
      return;
    }

    var result = getClient().getChatMessagesComponent()
            .delete(messageId, null, channelId);
    if (result.isSuccessOrRefreshToken(getClient())) {
      println("Message deleted.");
    } else {
      printMessageWithDivider("Fail to delete the message.\nReason: " + result.getErrorMessage());
    }
  }

  // endregion

  // region Private Chat Channel Functions

  private List<Channel> listChannels(boolean useCache) {
    printTitle("List user's channels");
    var result = getClient().getChatChannelsComponent().list(null, useCache);
    if (result.isSuccessOrRefreshToken(getClient())) {
      printChannelsWithTitle("Succeed to list user's channels", result.getItems());
    } else {
      printMessageWithDivider("Fail to list user's channels.\nReason: " + result.getErrorMessage());
    }
    return result.getItems();
  }

  private List<Channel> listChannels() {
    return listChannels(false);
  }

  private void createAChannel() {
    printTitle("Create a channel");

    var name = getUserInput("Please input a channel name(ex. test): ");
    var emails = getValidUserInputEmailList();

    var result = getClient().getChatChannelsComponent().create(name, emails);
    if (result.isSuccessOrRefreshToken(getClient())) {
      printChannelWithTitle("Succeed to create a channel", result.getItem());
    } else {
      printMessageWithDivider("Fail to create a channel.\nReason: " + result.getErrorMessage());
    }
  }

  private void getAChannel() {
    getAChannel(null);
  }

  private void getAChannel(String channelId) {
    printTitle("Get a channel");

    if (channelId == null) {
      channelId = getUserInput("Please input a channel id(ex. 45dcf4e6-3ad5-433c-8081-764c1866c46a): ");
    }

    var result = getClient().getChatChannelsComponent().get(channelId);
    if (result.isSuccessOrRefreshToken(getClient())) {
      printChannelWithTitle("Succeed to get a channel", result.getItem());
      println();
    } else {
      printMessageWithDivider("Fail to get a channel.\nReason: " + result.getErrorMessage());
    }
  }

  private void updateAChannel() {
    printTitle("Update a channel");

    var channelId = getUserInput("Please input a channel id(ex. 45dcf4e6-3ad5-433c-8081-764c1866c46a): ");
    var name = getUserInput("Please input a channel name(ex. test): ");

    var result = getClient().getChatChannelsComponent().update(channelId, name);
    if (result.isSuccessOrRefreshToken(getClient())) {
      printMessageWithDivider("Succeed to update a channel");
    } else {
      printMessageWithDivider("Fail to update a channel.\nReason: " + result.getErrorMessage());
    }
  }

  private void deleteAChannel() {
    printTitle("Delete a channel");

    var channelId = getUserInput("Please input a channel id(ex. 45dcf4e6-3ad5-433c-8081-764c1866c46a): ");

    var result = getClient().getChatChannelsComponent().delete(channelId);
    if (result.isSuccessOrRefreshToken(getClient())) {
      printMessageWithDivider("Succeed to delete a channel");
    } else {
      printMessageWithDivider("Fail to delete a channel.\nReason: " + result.getErrorMessage());
    }
  }

  private void listChannelMembers() {
    listChannelMembers(null);
  }

  private void listChannelMembers(String channelId) {
    printTitle("List channel members");

    if (channelId == null) {
      try {
        channelId = listAndSelectChannel().getId();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    var result = getClient().getChatChannelsComponent().listMembers(channelId, new PageConfiguration(PageConfiguration.MAX_PAGE_SIZE));
    if (result.isSuccessOrRefreshToken(getClient())) {
      printMembersWithTitle("Succeed to list channel members", result.getItems());
      println();
    } else {
      printMessageWithDivider("Fail to list channel members.\nReason: " + result.getErrorMessage());
    }
  }

  private void inviteChannelMembers() {
    printTitle("Invite channel members");
    listExternalContacts();

    var channelId = getUserInput("Please input a channel id(ex. 45dcf4e6-3ad5-433c-8081-764c1866c46a): ");
    var emails = getValidUserInputEmailList();

    var result = getClient().getChatChannelsComponent().inviteMembers(channelId, emails);
    if (result.isSuccessOrRefreshToken(getClient())) {
      printInviteChannelMembersResultWithTitle("Succeed to invite channel members", result.getItem());
      println();
    } else {
      printMessageWithDivider("Fail to invite channel members.\nReason: " + result.getErrorMessage());
    }
  }

  private void joinAChannel() {
    printTitle("Join a channel");

    var channelId = getUserInput("Please input a channel id(ex. 45dcf4e6-3ad5-433c-8081-764c1866c46a): ");

    var result = getClient().getChatChannelsComponent().join(channelId);
    if (result.isSuccessOrRefreshToken(getClient())) {
      printJoinAChannelResultWithTitle("Succeed to join a channel", result.getItem());
      println();
    } else {
      printMessageWithDivider("Fail to join a channel.\nReason: " + result.getErrorMessage());
    }
  }

  private void leaveAChannel() {
    printTitle("Leave a channel");

    var channelId = getUserInput("Please input a channel id(ex. 45dcf4e6-3ad5-433c-8081-764c1866c46a): ");

    var result = getClient().getChatChannelsComponent().leave(channelId);
    if (result.isSuccessOrRefreshToken(getClient())) {
      printMessageWithDivider("Succeed to leave a channel");
    } else {
      printMessageWithDivider("Fail to leave a channel.\nReason: " + result.getErrorMessage());
    }
  }

  private void removeAChannelMember() {
    printTitle("Remove a channel member");

    var channelId = getUserInput("Please input a channel id(ex. 45dcf4e6-3ad5-433c-8081-764c1866c46a): ");
    var memberId = getUserInput("Please input a member id(ex. p1d-2aj2rx2mbohcae8tpw): ");

    var result = getClient().getChatChannelsComponent().deleteMembers(channelId, memberId);
    if (result.isSuccessOrRefreshToken(getClient())) {
      printMessageWithDivider("Succeed to remove a member");
    } else {
      printMessageWithDivider("Fail to remove a member.\nReason: " + result.getErrorMessage());
    }
  }

  // endregion

  // region Private Contacts Functions

  private void listExternalContacts() {
    var result = getClient().getContactsComponent().listExternal();
    if (result.isSuccessOrRefreshToken(getClient())) {
      printMembersWithTitle("User's external contacts", result.getItems());
    } else {
      printMessageWithDivider("Fail to get contacts info.\nReason: " + result.getErrorMessage());
    }
  }

  // endregion

  // region Private Chat Functions

  private void sendMessage(String channelName, String message) {
    var title = "Send message to " + channelName;
    printTitle(title);

    var result = getClient().getChatComponent().sendMessage(channelName, message);
    if (result.isSuccessOrRefreshToken(getClient())) {
      printMessageWithDivider("Message sent. Message ID=" + result.getItem());
    } else {
      printMessageWithDivider("Fail to " + title + "\nReason: " + result.getErrorMessage());
    }
  }

  private void listHistory(String channelName) {
    var title = "List message history in " + channelName;
    printTitle(title);

    var dateRange = BotIO.getValidQueryDateParameters();
    var fromDate = dateRange.get(0);
    var toDate = dateRange.get(1);

    printMessageListResult(
            title, fromDate, toDate,
            getClient().getChatComponent().history(
                    channelName,
                    fromDate,
                    toDate
            )
    );
  }

  private void searchMessageByContent(String channelName, String query) {
    var title = "Search messages whose content contains '" + query + "' in " + channelName;
    printTitle(title);

    var dateRange = BotIO.getValidQueryDateParameters();
    var fromDate = dateRange.get(0);
    var toDate = dateRange.get(1);

    printMessageListResult(
            title, fromDate, toDate,
            getClient().getChatComponent().search(
                    channelName,
                    fromDate,
                    toDate,
                    (message) -> message.getMessage().contains(query)
            )
    );
  }

  private void searchMessageBySender(String channelName, String query) {
    var title = "Search messages whose senders' names contain '" + query + "' in " + channelName;
    printTitle(title);

    var dateRange = BotIO.getValidQueryDateParameters();
    var fromDate = dateRange.get(0);
    var toDate = dateRange.get(1);

    printMessageListResult(
            title, fromDate, toDate,
            getClient().getChatComponent().search(
                    channelName,
                    fromDate,
                    toDate,
                    (message) -> (
                            message.getSender() == null
                                    ? ""
                                    : message.getSender()
                    ).contains(query)
            )
    );
  }

  // endregion

  public static void main(String[] args) {
    try {
      if (args == null || args.length == 0) {
        new Bot().run();
      } else {
        new Bot(args[0]).run();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
