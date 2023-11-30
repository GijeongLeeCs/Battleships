package Networking.Client;

import view_controller.BoardGUI;

import java.io.IOException;
import java.net.*;

public class Client {

    private DatagramSocket socket;

    private static final int SERVER_PORT = 8000; // send to server

    final BoardGUI gui;
    final InetAddress address;

    public Client(String IP, BoardGUI gui) throws UnknownHostException {

        try {
            address = InetAddress.getByName(IP.trim());
        } catch (UnknownHostException e) {
            throw e;
        }

        try {
            socket = new DatagramSocket();
            socket.connect(address, SERVER_PORT);
        } catch (SocketException e) {
            System.out.println("Unable to connect to host!");
            e.printStackTrace();
        }

        ClientThread thread = new ClientThread(socket, this);
        thread.start();
        this.gui = gui;
    }

    public void sendPacket(String msg) {
        try {
            socket.send(new DatagramPacket(msg.getBytes(), msg.length(), address, 8000));
        } catch (IOException e) {
            System.out.println("Error Sending data!\n" + msg);
            e.printStackTrace();
        }
    }

    public void process(String message) {
        if(message.getBytes().length > 512) {
            throw new RuntimeException("Request is too long!");
        }
        System.out.println("Client received: " + message);
        gui.handle(message);
    }

    public void disconnect(String username) {
        sendPacket("dsct;" + username + ";");
        try {
            socket.close();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
}