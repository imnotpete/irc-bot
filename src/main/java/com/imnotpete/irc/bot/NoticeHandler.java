package com.imnotpete.irc.bot;
public class NoticeHandler {
	private ExtendedIrcBot bot = null;

	public NoticeHandler(ExtendedIrcBot bot) {
		this.bot = bot;
	}

	public void process(String username, String message) {
		if (null != username) {
			if (username.contains("NickServ")) {
				if (message.contains("If this is your nickname") &&
				null != bot.properties.getProperty("botPassword") &&
				! bot.properties.getProperty("botPassword").equals("")) {
					bot.send_privmsg("nickserv", "identify " + bot.properties.getProperty("botPassword"));
				}
			}
		}
	}
}
