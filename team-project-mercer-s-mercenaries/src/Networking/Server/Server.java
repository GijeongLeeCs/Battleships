package Networking.Server;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.HashSet;

public class Server {

    private final byte[] incoming = new byte[512];
    private final int PORT = 8000;

    private final DatagramSocket socket;
    private final HashMap<String, Integer> ports; // Maps usernames to ports
    private final HashMap<String, InetAddress> IPs; // Maps usernames to IPs
    private final HashMap<String, GameState> hosts; // Maps hosts to games
    private final HashMap<String, String> opponents; // Maps users to their opponents.
    private final HashSet<String> usernames; // Contains the list of used usernames.

    private final InetAddress address;

    /**
     * Creates a new server, binding to port 8000. Port 8000 was chosen arbitrarily.
     */
    public Server() {

        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        try {
            socket = new DatagramSocket(PORT, address);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        ports = new HashMap<>();
        hosts = new HashMap<>();
        IPs = new HashMap<>();
        opponents = new HashMap<>();
        usernames = new HashSet<>();
    }


    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }

    /**
     * Response and request headers
     * New connections begin with "init;username"
     * Game hosting messages begin with "host;username;"
     * Game joining messages begin with "join;username_self;username_host" (Shows an appropriate message in the chat window)
     * Game joining requests begin with "jngm;" (join game)
     * Ready signals from the boards begin with "redy;"
     * Game start signals from the server begin with "strt;"
     * Requests begin with "rqst;" (i.e. other user requests your current board)
     * Moves begin with "move;username;row;col;"
     * Chat messages begin with "chat;username;"
     * Host lists begin with "list;"
     * Disconnect messages are of the format "dsct;username;"
     * Rematches begin with "rmch;"
     */
    @SuppressWarnings("InfiniteLoopStatement")
    private void run() {
        System.out.println("Server started on address " + address.getHostAddress() + " and port " + PORT);

        while (true) { // We want this to run until we close the program.
            DatagramPacket packet = new DatagramPacket(incoming, incoming.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String message = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Server received: " + message + " from address: " + packet.getAddress().getHostAddress() +
                    " from port: " + packet.getPort());

            String name;
            String opponent;
            String host;
            String[] info;
            GameState gs;

            switch (message.substring(0, 5)) {
                case "init;" -> {
                    name = message.substring(5);
                    String msg;
                    if (usernames.contains(name)) {
                        msg = "info;Name used";
                    } else {
                        msg = "info;Name registered";
                        usernames.add(name);
                        ports.put(name, packet.getPort());
                        IPs.put(name, packet.getAddress());
                    }
                    sendPacket(msg, packet.getPort(), packet.getAddress());
                }
                case "host;" -> {
                    name = message.substring(5, message.indexOf(';', 5));
                    hosts.put(name, new GameState()); // Represents that the game hasn't started yet
                    sendPacket("info;Host received", packet.getPort(), packet.getAddress());
                }
                case "join;" -> {
                    if (hosts.isEmpty()) {
                        name = message.substring(5, message.indexOf(';', 5));
                        sendPacket("info;No hosts found", packet.getPort(), packet.getAddress());
                        hosts.put(name, new GameState()); // Represents that the game hasn't started yet
                    } else {
                        StringBuilder send = new StringBuilder("list;");
                        for (String hostname : hosts.keySet()) {
                            if (!hosts.get(hostname).gameStarted) {
                                send.append(hostname).append(";");
                            }
                        }
                        sendPacket(send.toString(), packet.getPort(), packet.getAddress());
                    }
                }
                case "jngm;" -> {
                    info = message.split(";");
                    if (hosts.get(info[2]).gameStarted) {
                        sendPacket("User is already in a game.", packet.getPort(), packet.getAddress());
                    } else {
                        opponents.put(info[1], info[2]);
                        opponents.put(info[2], info[1]);
                        hosts.get(info[2]).gameStarted = true;
                        sendPacket("join;" + info[1] + ";" + info[2], ports.get(info[2]), IPs.get(info[2]));
                        sendPacket("join;" + info[1] + ";" + info[2], packet.getPort(), packet.getAddress());
                    }
                }
                case "redy;" -> {
                    info = message.split(";");
                    if (hosts.get(info[1]) != null) {
                        host = info[1];
                        gs = hosts.get(info[1]);
                        hosts.get(host).hostReady = true;
                    } else {
                        host = opponents.get(info[1]);
                        hosts.get(host).joinReady = true;
                        gs = hosts.get(host);
                    }
                    if (gs.joinReady && gs.hostReady) {
                        sendPacket("strt;", ports.get(host), IPs.get(host));
                        sendPacket("strt;", ports.get(opponents.get(host)), IPs.get(opponents.get(host)));
                    }
                }
                case "rqst;" -> {
                    info = message.split(";");
                    if (info[2].equals("board")) {
                        opponent = opponents.get(info[1]);
                        sendPacket("rqst;board", ports.get(opponent), IPs.get(opponent));
                    }
                }
                case "rspn;", "move;" -> { // We need to forward these requests
                    info = message.split(";");
                    opponent = opponents.get(info[1]);
                    sendPacket(message, ports.get(opponent), IPs.get(opponent));
                }
                case "chat;" -> {
                    info = message.split(";");
                    if (opponents.get(info[1]) == null) {
                        for (String oName : usernames) {
                            if (opponents.get(oName) == null && !oName.equals(info[1])) {
                                sendPacket(message, ports.get(oName), IPs.get(oName)); // Pass on messages if not in game
                            }
                        }
                    } else {
                        opponent = opponents.get(info[1]);
                        sendPacket(message, ports.get(opponent), IPs.get(opponent));
                    }
                }
                case "dsct;" -> {
                    info = message.split(";");
                    name = info[1];
                    if (opponents.get(name) != null && ports.get(opponents.get(name)) != null && IPs.get(opponents.get(name)) != null) {
                        sendPacket(message, ports.get(opponents.get(name)), IPs.get(opponents.get(name)));
                    }
                    hosts.remove(name);
                    ports.remove(name);
                    IPs.remove(name);
                    usernames.remove(name);
                    opponents.remove(name);
                    System.out.println(name + " deregistered");
                }
                case "rmch;" -> {
                    info = message.split(";");
                    name = info[1];
                    if (hosts.get(name) != null) {
                        host = name;
                        opponent = opponents.get(name);
                        gs = hosts.get(name);
                        gs.hostRematch = true;
                    } else {
                        host = opponents.get(name);
                        opponent = name;
                        gs = hosts.get(host);
                        gs.joinRematch = true;
                    }
                    if (gs.joinRematch && gs.hostRematch) {
                        gs.hostReady = false;
                        gs.joinReady = false;
                        gs.hostRematch = false;
                        gs.joinRematch = false;
                        sendPacket("rmch;" + opponent + ";" + host, ports.get(host), IPs.get(host));
                        sendPacket("rmch;" + opponent + ";" + host, ports.get(opponents.get(host)), IPs.get(opponents.get(host)));
                    }
                }
            }
        }
    }

    private void sendPacket(String message, int port, InetAddress ip) {
        DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), ip, port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Server sent: " + message + " to address: " + ip.getHostAddress() + " to port: " + port);
    }

    /**
     * Represents the state of an individual game. Lets the server know when a game has two people connected, and when
     * both of them have placed their ships.
     */
    private static class GameState {
        boolean gameStarted = false;
        boolean hostReady = false;
        boolean joinReady = false;
        boolean hostRematch = false;
        boolean joinRematch = false;
    }
}