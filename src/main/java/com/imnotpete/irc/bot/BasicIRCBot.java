package com.imnotpete.irc.bot;

import java.net.*;
import java.io.*;
 
public class BasicIRCBot {
	private BufferedReader IRCir;
	private BufferedWriter IRCor;

	private Socket IRCServerS;

	public boolean running;

	private String botName;
	private String botDescription;
	
    protected void connect(String serverHostname, int serverPort) {
        try	{
            IRCServerS = new Socket(serverHostname, serverPort);
        } catch(Exception e) {
            System.err.println("error connecting to IRC server");
            e.printStackTrace();
            System.exit(0);
        }

        InputStream IRCis = null;
        OutputStream IRCos = null;

        try	{
            IRCis = IRCServerS.getInputStream();
            IRCos = IRCServerS.getOutputStream();
        } catch(Exception e) {
            System.err.println("error opening streams to IRC server");
            e.printStackTrace();
            System.exit(0);
        }

        IRCir = new BufferedReader(new InputStreamReader(IRCis));
        IRCor = new BufferedWriter(new OutputStreamWriter(IRCos));
    }

    protected void disconnect() {
        try {
            IRCir.close();
            IRCor.close();
        } catch(IOException e) {
            System.err.println("Error disconnecting from IRC server");
            e.printStackTrace();
        }
    }

    protected boolean ircsend(String message) {
        try {
            IRCor.write(message);
            IRCor.newLine();
            IRCor.flush();
        } catch(IOException e) {
            return false;
        }

        return true;
    }

    public BasicIRCBot(String botName, String botDescription) {
        this.botName = botName;
        this.botDescription = botDescription;
    }

    protected void logoff() {
//        BufferedReader br = IRCir;
        BufferedWriter bw = IRCor;

        try {
            bw.write("quit terminating");
            bw.newLine();
            bw.flush();
        } catch(Exception e) {
            System.out.println("logoff error: " + e);
            System.exit(0);
        }
    }

    protected void logon() {
//        BufferedReader br = IRCir;
        BufferedWriter bw = IRCor;

        try {
            // send user info
            bw.write("user " + botName + " ware2 irc :" + botDescription);
            bw.newLine();
            bw.write("nick " + botName);
            bw.newLine();
            bw.flush();
        } catch(Exception e) {
            System.out.println("logon error: " + e);
            System.exit(0);
        }
    }

    private void parse_privmsg(String username, String params) {
        String message;
        String channel = null;

        if (params.startsWith("#")) {
            channel = params.substring(0, params.indexOf(" :"));
        }

        params = params.substring(params.indexOf(' ') + 1);

        if (params.substring(0,1).equals(":")) {
            message = params.substring(1);
        } else {
            message = params.substring(0);
        }

        srv_privmsg(username, channel, message);
    }

    private void parse_notice(String username, String params) {
        String message;

        params = params.substring(params.indexOf(' ') + 1);

        if(params.substring(0,1).equals(":")) {
            message = params.substring(1);
        } else {
            message = params.substring(0);
        }

        srv_notice(username, message);
    }

    private void parse_invite(String prefix, String params) {
        srv_invite(prefix.substring(0, prefix.indexOf("!")), params.substring(params.indexOf("#")));
    }

    private boolean pingpong(String msg) throws IOException {
        if(msg.substring(0,4).equalsIgnoreCase("ping")) {
            String pongmsg = "pong " + msg.substring(5);
            IRCor.write(pongmsg);
            IRCor.newLine();
            IRCor.flush();

            return true;
        }

        return false;
    }

    protected void send_notice(String username, String message) {
        String command = "notice " + username + " :" + message;

        ircsend(command);
    }

    protected void send_privmsg(String username, String message) {
        String command = "privmsg " + username + " :" + message;

        ircsend(command);
    }

    protected void service() {
        try {
            if(IRCir.ready()) {
                String msg = IRCir.readLine();

                if(!pingpong(msg)) {
                    String prefix = null;
                    String command;
                    String params;

                    if(msg.substring(0,1).equals(":")) {
                        prefix = msg.substring(1, msg.indexOf(' '));
                        msg = msg.substring(msg.indexOf(' ') + 1);
                    }

                    command = msg.substring(0, msg.indexOf(' '));
                    params = msg.substring(msg.indexOf(' ') + 1);

                    if(command.equalsIgnoreCase("privmsg")) {
                        String username;
                        if(null != prefix && prefix.indexOf('!') != -1) {
                            username = prefix.substring(0, prefix.indexOf("!"));
                        } else {
                            username = prefix;
                        }

                        parse_privmsg(username, params);
                    }

                    if (command.equalsIgnoreCase("notice")) {
                        parse_notice(prefix, params);
                    }

                    if (command.equalsIgnoreCase("invite")) {
                        parse_invite(prefix, params);
                    }
                }
            } else {
                try {
                    Thread.sleep(100);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch(IOException e) {
            System.out.println("error: " + e);
            System.exit(0);
        }
    }

    protected void srv_privmsg(String username, String channel, String command) { }

    protected void srv_notice(String username, String command) { }

    protected void srv_invite(String username, String channel) { }
}