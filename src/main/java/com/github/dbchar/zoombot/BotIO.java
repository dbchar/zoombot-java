package com.github.dbchar.zoombot;

import com.github.dbchar.zoomapi.models.Channel;
import com.github.dbchar.zoomapi.models.Message;
import com.github.dbchar.zoomapi.models.User;
import com.github.dbchar.zoomapi.models.responses.IdAddedDateResponse;
import com.github.dbchar.zoomapi.utils.ListResult;
import com.github.dbchar.zoomapi.utils.Logger;
import com.github.dbchar.zoomapi.utils.Validator;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.github.dbchar.zoomapi.utils.DateUtil.DATE_FORMAT;
import static com.github.dbchar.zoombot.BotIO.MainMenuCommand.*;

public class BotIO {
    // region Public Constants

    public static final int COMMAND_INVALID = -1;
    public static final String COMMAND_QUIT = "q";
    public static final int COMMAND_EXIT = 0;

    // endregion

    // region Bot Commands

    public enum MainMenuCommand {
        M2_CHAT_CHANNEL_SET_FUNCTIONS(1),
        M2_CHAT_CHANNEL_SINGLE_FUNCTION(2),
        M2_CHAT_MESSAGE_SET_FUNCTIONS(3),
        M2_CHAT_MESSAGE_SINGLE_FUNCTION(4),
        M3_CHAT_SET_FUNCTIONS(5),
        M3_CHAT_SINGLE_FUNCTION(6),
        M4_START_MONITOR_CHANNEL(7),
        M4_DISPLAY_MONITOR_CHANNEL_NAME(8),
        M4_STOP_MONITOR_CHANNEL(9),
        M4_STOP_MONITOR_ALL_CHANNELS(10),
        M5_LIST_CHANNELS(11),
        M5_LIST_MEMBERS(12),
        M5_LIST_MESSAGES(13),
        TOGGLE_DEBUG_OUTPUT(98),
        PRINT_MAIN_MENU(99);
        private final int value;

        MainMenuCommand(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    // endregion

    // region Private Constants

    private static final String DIVIDER = "------------------------------";
    private static final int INVALID_DAY_INTERVAL = -1;

    // endregion

    // region Public APIs (Get User Input Functions)

    public static int getUserCommand(String placeholder) {
        if (placeholder == null || placeholder.isEmpty()) {
            placeholder = "Please select a command(ex. 1): ";
        }

        System.out.println(placeholder);

        try {
            return new Scanner(System.in).nextInt();
        } catch (Exception expected) {
            System.out.println("Invalid command, please enter a correct command!");
            return COMMAND_INVALID;
        }
    }

    public static String getUserInput(String placeholder) {
        System.out.println(placeholder);
        return new Scanner(System.in).nextLine();
    }

    public static String getValidUserInputEmail() {
        var input = (String) null;

        do {
            input = getUserInput("Please input a email(ex. test@gmail.com): ");
        } while (input.isEmpty() || !Validator.isValidEmail(input));

        return input;
    }

    public static List<String> getValidUserInputEmailList() {
        final var MAX_EMAIL_NUMBER = 5;

        System.out.println("You can enter at most " + MAX_EMAIL_NUMBER + " email address ('q' to quit)");

        var emails = new ArrayList<String>();

        while (emails.size() < MAX_EMAIL_NUMBER) {
            var input = getUserInput("Command 'q' to quit or 'Enter' to continue: ");

            if (input.toLowerCase().equals(COMMAND_QUIT)) {
                break;
            } else if (emails.size() < MAX_EMAIL_NUMBER) {
                System.out.println("Email " + (emails.size() + 1));
                var email = getValidUserInputEmail();
                emails.add(email);
            } else {
                System.out.println("Please input 'q' to quit or 'Enter' to continue.");
            }
        }

        return emails;
    }

    public static boolean getTrueOrFalse(String prompt) {
        var input = "";
        while (!input.equals("y") && !input.equals("n")) {
            input = getUserInput(prompt + " (y/n)");
        }
        return input.equals("y");
    }

    /***
     * Get query date parameters
     * @return List<String> dates, from date: dates.get(0), to date: dates.get(1)
     */
    public static List<String> getValidQueryDateParameters() {
        final var MAX_DAY_INTERVAL = 5;

        System.out.println("The interval between 'From Date' and 'To Date' is from 1 to " + MAX_DAY_INTERVAL);
        System.out.println("Date format should be " + DATE_FORMAT + ", ex. 2020-04-28");
        System.out.println("ATTENTION! Dates are in LOCAL time zone, not GMT.");
        System.out.println("If 'From Date' is not provided by user, the default values of 'From Date' and 'To Date' are today.");
        System.out.println("If 'From Date' is provided by user, the default value of 'To Date' is 'From Date'.");

        var dates = new ArrayList<String>();

        while (dates.size() < 2) {
            var input = getUserInput(
                    (dates.isEmpty() ? "From" : "To") + " Date:\n" +
                            "['q'] Use default;\n" +
                            "['Enter'] Input manually;\n" +
                            "Please input:");

            if (input.toLowerCase().equals(COMMAND_QUIT)) {
                break;
            } else if (dates.size() < 2) {
                var placeholder = dates.size() < 1 ? "From (ex. 2020-04-25): " : "To (ex. 2020-04-28): ";
                var date = getUserInput(placeholder);

                if (Validator.isValidDateString(date, DATE_FORMAT)) {
                    // check days interval first
                    if (dates.size() == 1) {
                        // we need to add 1 since if from and to are the same day, it counts 1
                        var intervals = getDayInterval(dates.get(0), date) + 1;
                        if (intervals > 0 && intervals <= MAX_DAY_INTERVAL) {
                            dates.add(date);
                        } else {
                            System.out.println("Invalid interval of day: " + intervals + ". The range should be [1," + MAX_DAY_INTERVAL + "]");
                        }
                    } else {
                        dates.add(date);
                    }
                }
            } else {
                System.out.println("Please input 'q' to quit or 'Enter' to continue.");
            }
        }

        // calibrate dates
        if (dates.isEmpty()) {
            final var defaultDate = new SimpleDateFormat(DATE_FORMAT).format(new Date());
            dates.add(defaultDate);
            dates.add(defaultDate);
        } else if (dates.size() == 1) {
            dates.add(dates.get(0));
        }

        return dates;
    }

    // endregion

    // region Public APIs (Print Functions)

    public static void printMainMenu() {
        System.out.println("# Main Menu #");
        System.out.println("## Milestone 2 ##");
        System.out.println("[" + M2_CHAT_CHANNEL_SET_FUNCTIONS.value + "] Execute a MEANINGFUL set of Chat Channel Functions;");
        System.out.println("[" + M2_CHAT_CHANNEL_SINGLE_FUNCTION.value + "] Execute a single Chat Channel Function (debug only);");
        System.out.println("[" + M2_CHAT_MESSAGE_SET_FUNCTIONS.value + "] Execute a MEANINGFUL set of Chat Message Functions;");
        System.out.println("[" + M2_CHAT_MESSAGE_SINGLE_FUNCTION.value + "] Execute a single Chat Message Function (debug only);");
        System.out.println("## Milestone 3 ##");
        System.out.println("[" + M3_CHAT_SET_FUNCTIONS.value + "] Execute a MEANINGFUL set of Chat Functions;");
        System.out.println("[" + M3_CHAT_SINGLE_FUNCTION.value + "] Execute a single Chat Function (debug only);");
        System.out.println("## Milestone 4 ##");
        System.out.println("[" + M4_START_MONITOR_CHANNEL.value + "] Start monitoring a channel asynchronously;");
        System.out.println("[" + M4_DISPLAY_MONITOR_CHANNEL_NAME.value + "] Display monitoring channel names;");
        System.out.println("[" + M4_STOP_MONITOR_CHANNEL.value + "] Stop monitoring a channel;");
        System.out.println("[" + M4_STOP_MONITOR_ALL_CHANNELS.value + "] Stop monitoring all channels;");
        System.out.println("## Milestone 5 ##");
        System.out.println("[" + M5_LIST_CHANNELS.value + "] List channels;");
        System.out.println("[" + M5_LIST_MEMBERS.value + "] List members;");
        System.out.println("[" + M5_LIST_MESSAGES.value + "] List messages;");
        System.out.println("## Others ##");
        System.out.println("[" + TOGGLE_DEBUG_OUTPUT.value + "] Toggle debug output (current: " + !Logger.DISABLED + ");");
        System.out.println("[" + PRINT_MAIN_MENU.value + "] Print this menu (useful when monitoring a channel);");
        System.out.println("[" + COMMAND_EXIT + "] Exit;");
    }

    public static void printMainMenuM2() {
        System.out.println("# Main Menu M2 #");
        System.out.println("[1] Execute a MEANINGFUL set of Chat Channel Functions;");
        System.out.println("[2] Execute a single Chat Channel Function (debug only);");
        System.out.println("[3] Execute a MEANINGFUL set of Chat Message Functions;");
        System.out.println("[4] Execute a single Chat Message Function (debug only);");
        System.out.println("[" + COMMAND_EXIT + "] Exit;");
    }

    public static void printChatChannelMenu(String email) {
        System.out.println("# Chat Channel Menu # userEmail=" + email);
        System.out.println("[1] List user's channels;");
        System.out.println("[2] Create a channel;");
        System.out.println("[3] Get a channel;");
        System.out.println("[4] Update a channel;");
        System.out.println("[5] Delete a channel;");
        System.out.println("[6] List channel members;");
        System.out.println("[7] Invite channel members;");
        System.out.println("[8] Join a channel;");
        System.out.println("[9] Leave a channel;");
        System.out.println("[10] Remove a member;");
        System.out.println("[" + COMMAND_EXIT + "] Exit;");
    }

    public static void printChatMessageMenu(String channelName) {
        System.out.println("# Chat Message Menu # channelName=" + channelName);
        System.out.println("[1] List channel messages;");
        System.out.println("[2] Send channel messages;");
        System.out.println("[3] Update a message;");
        System.out.println("[4] Delete a message;");
        System.out.println("[" + COMMAND_EXIT + "] Exit;");
    }

    public static void printMainMenuM3() {
        System.out.println("# Main Menu M3 #");
        System.out.println("[1] Execute a MEANINGFUL set of Chat Functions;");
        System.out.println("[2] Execute a single Chat Function (debug only);");
        System.out.println("[" + COMMAND_EXIT + "] Exit;");
    }

    public static void printChatMenu(String channelName) {
        System.out.println("# Chat Menu # channelName=" + channelName);
        System.out.println("[1] Send channel messages;");
        System.out.println("[2] Get channel message history;");
        System.out.println("[3] Search messages by content;");
        System.out.println("[4] Search messages by sender;");
        System.out.println("[" + COMMAND_EXIT + "] Exit;");
    }

    public static void printTitle(String title) {
        System.out.println(DIVIDER);
        System.out.println("# " + title);
        System.out.println(DIVIDER);
        System.out.println();
    }

    public static void printMessageWithDivider(String message) {
        System.out.println(DIVIDER);
        System.out.println(message);
        System.out.println(DIVIDER);
        System.out.println();
    }

    public static void printUserInfo(User user) {
        System.out.println(DIVIDER);
        System.out.println("# You are logged in as");
        System.out.println(user.getInfo());
        System.out.println(DIVIDER);
    }

    public static void printChannelWithTitle(String title, Channel channel) {
        System.out.println(DIVIDER);
        System.out.println("# " + title);
        System.out.println(channel.getName() + ": " + channel.getId());
        System.out.println(DIVIDER);
        System.out.println();
    }

    public static void printChannelsWithTitle(String title, List<Channel> channels) {
        System.out.println(DIVIDER);
        System.out.println("# " + title);
        var i = 0;
        for (Channel channel : channels) {
            System.out.println("[" + (i + 1) + "] " + channel.getName() + ": " + channel.getId() + "(type: " + channel.getType() + ")");
            i++;
        }
        System.out.println(DIVIDER);
        System.out.println();
    }

    public static void printMembersWithTitle(String title, List<User> members) {
        System.out.println(DIVIDER);
        System.out.println("# " + title);
        var i = 0;
        for (User member : members) {
            System.out.println("[" + (i + 1) + "] ID: " + member.getId() + "\n" + member.getName() + "(" + member.getRole() + "): " + member.getEmail());
            i++;

            if (i < members.size()) {
                System.out.println();
            }
        }
        System.out.println(DIVIDER);
        System.out.println();
    }

    public static void printInviteChannelMembersResultWithTitle(String title, IdAddedDateResponse result) {
        System.out.println(DIVIDER);
        System.out.println("# " + title + " at " + result.getAddedDate());
        System.out.println("Ids: " + result.getIds());
        System.out.println(DIVIDER);
        System.out.println();
    }

    public static void printJoinAChannelResultWithTitle(String title, IdAddedDateResponse result) {
        if (result == null) return;
        System.out.println(DIVIDER);
        System.out.println("# " + title + " at " + result.getAddedDate());
        System.out.println("Id: " + result.getId());
        System.out.println(DIVIDER);
        System.out.println();
    }

    public static void printChannelMessagesWithTitle(String title, List<Message> messages, boolean printId) {
        System.out.println(DIVIDER);
        System.out.println("# " + title);
        var i = 0;
        for (var message : messages) {
            System.out.println("[" + (i + 1) + "]" +
                    (printId ? " " + message.getId() : "") +
                    " " + message.getLocalDateTime() +
                    " " + message.getSender() + ": "
                    + message.getMessage());
            i++;
        }
        System.out.println(DIVIDER);
        System.out.println();
    }

    public static void printMessageListResult(String title, String fromDate, String toDate, ListResult<Message> result) {
        title = title + "\nPeriod (" + TimeZone.getDefault().getID() + "): " + fromDate + " - " + toDate;
        if (result.isSuccess()) {
            printChannelMessagesWithTitle("Succeed to " + title, result.getItems(), false);
        } else {
            printMessageWithDivider("Fail to " + title + "\nReason: " + result.getErrorMessage());
        }
    }

    public static void println(Object object) {
        System.out.println(object);
    }

    public static void println() {
        System.out.println();
    }

    // endregion

    private static int getDayInterval(String fromDate, String toDate) {
        try {
            var formatter = new SimpleDateFormat(DATE_FORMAT);
            var date1 = formatter.parse(fromDate);
            var date2 = formatter.parse(toDate);
            var diff = date2.getTime() - date1.getTime();
            return (int) (diff / (24 * 60 * 60 * 1000));
        } catch (Exception e) {
            return INVALID_DAY_INTERVAL;
        }
    }
}
