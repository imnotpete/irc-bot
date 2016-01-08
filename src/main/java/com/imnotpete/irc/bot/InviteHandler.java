package com.imnotpete.irc.bot;
public class InviteHandler {

	private static ExtendedIrcBot bot = null;

	public InviteHandler(ExtendedIrcBot bot) {
		this.bot = bot;
	}

	public void process(String channel) {
		bot.ircsend("join " + channel);
	}
}

