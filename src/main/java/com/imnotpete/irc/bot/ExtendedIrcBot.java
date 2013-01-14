package com.imnotpete.irc.bot;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.math.BigDecimal;

public class ExtendedIRCBot extends BasicIRCBot {
	private static ArrayList<String> trusted;
	public static Properties properties = new Properties();
	private static final String[] MAGIC8BALL_PHRASES = {
		"signs point to yes", "yes", "most likely", "without a doubt",
	  	"yes - definitely", "as i see it, yes", "you may rely on it",
		"outlook good", "it is certain", "it is decidedly so", "reply hazy, try again",
		"better not tell you now", "ask again later", "concentrate and ask again",
		"cannot predict now", "my sources say no", "very doubtful",
		"my reply is no", "outlook not so good", "don't count on it"};

	private static final Random RANDOM = new Random();
	private static final long STARTUP_TIME = System.currentTimeMillis();

	private InviteHandler invite = new InviteHandler(this);
	private NoticeHandler notice = new NoticeHandler(this);

	public ExtendedIRCBot(String botName, String botDescription) {
		super(botName, botDescription);
	}

	public static void main(java.lang.String[] args) {
		try {
			properties.load(new FileInputStream("bot.properties"));
		} catch (IOException e) {
			try {
				properties.load(ClassLoader.getSystemResourceAsStream("bot.properties"));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		List<String> list = Arrays.asList(properties.getProperty("trusted").split(":"));
		trusted = new ArrayList<String>(list);

		ExtendedIRCBot IRC = new ExtendedIRCBot(properties.getProperty("botName"), properties.getProperty("botName"));
		IRC.connect(properties.getProperty("server"), 6667);
		IRC.logon();

		IRC.running = true;
		while(IRC.running) {
			IRC.service();
		}

		IRC.logoff();
		IRC.disconnect();
	}

	protected void srv_invite(String username, String channel) {
		if (trusted.contains(username)) {
			invite.process(channel);
		}
	}

	protected void srv_notice(String username, String message) {
		notice.process(username, message);
	}

	protected void srv_privmsg(String username, String channel, String message) {
		if (message.contains("If this is your nickname")) {
			if (username.equals("nickserv")) {
				send_privmsg("nickserv", "identify foobar99");
			}
		}

		if (message.contains("!learn ")) {
			if (trusted.contains(username)) {
				String newUser = message.substring(message.indexOf("!learn ") + 7);
				if (! trusted.contains(newUser)) {
					trusted.add(newUser);
					updateTrusted();
				}
			}
		}

		if (message.contains("!unlearn ")) {
			if (trusted.contains(username)) {
				String badUser = message.substring(message.indexOf("!unlearn ") + 9);
				if (trusted.contains(badUser)) {
					trusted.remove(badUser);
					updateTrusted();
				}
			}
		}

		if (message.equals("!quit")) {
			if(trusted.contains(username) && null == channel) {
				send_notice(username, "testbot ends now");
				running = false;
			}
		}

		if (null != channel) {
		if (message.equalsIgnoreCase("!uptime")) {
		long now = System.currentTimeMillis();
		double time = now - STARTUP_TIME;
		time = time / 1000 / 60 / 60; //convert milliseconds to hours
		String timeString = String.valueOf(time);
		timeString = timeString.substring(0, timeString.indexOf(".") + 3);
		send_privmsg(channel, "I've been running for " + timeString + " hours");
		}

			if (message.equals("!opme")) {
				if(trusted.contains(username)) {
					ircsend("MODE " + channel + " +o " + username);
				}
			}

			if (message.equals("!sing")) {
				   send_privmsg(channel, "lalalalalalla");
			}

			if (message.equalsIgnoreCase("kilroy was here")) {
				send_privmsg(channel, "Domo Arigoto, Mr. Roboto. Domo (domo). Domo (domo).");
			}

			if (message.equals("!list trusted")) {
				if (trusted.contains(username)) {
					StringBuffer sb = new StringBuffer("trusted users are: ");
					for (String user : trusted) {
						sb.append(user).append(" ");
					}
					send_privmsg(channel, sb.toString());
				}
			}

			if (message.equals("!list commands")) {
				String commands = "!learn <nick>, !unlearn <nick>, !opme, !sing, !8ball <question>, !list trusted, !list commands";
				send_privmsg(channel, commands);
			}

			if (message.startsWith("!8ball") && message.trim().length() > 6) {
				try {
					send_privmsg(channel, get8Ball());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void updateTrusted() {
		StringBuffer sb = new StringBuffer();
		for (String string : trusted) {
			sb.append(string).append(":");
		}

		String trustedString = sb.deleteCharAt(sb.lastIndexOf(":")).toString();
		properties.setProperty("trusted", trustedString);

		try {
			properties.store(new FileOutputStream("bot.properties"), null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String get8Ball() {
		int rnd = RANDOM.nextInt(MAGIC8BALL_PHRASES.length);
		return MAGIC8BALL_PHRASES[rnd];
	}
}
