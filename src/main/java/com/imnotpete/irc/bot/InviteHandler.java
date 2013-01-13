package com.imnotpete.irc.bot;
public class InviteHandler {

	private static ExtendedIRCBot bot = null;

	public InviteHandler(ExtendedIRCBot bot) {
		this.bot = bot;
	}

	public void process(String channel) {
		bot.ircsend("join " + channel);
	}
}

