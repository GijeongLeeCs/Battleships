package Networking.Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ClientThread extends Thread {

    private final DatagramSocket socket;
    private final byte[] incoming = new byte[512];

    private final Client client;


    public ClientThread(DatagramSocket socket, Client client) {
        this.socket = socket;
        this.client = client;
    }

    @Override
    public void run() {
        while (!socket.isClosed()) {
            DatagramPacket packet = new DatagramPacket(incoming, incoming.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                client.gui.handle("info;server offline;");
            }
            String message = new String(packet.getData(), 0, packet.getLength());
            client.process(message);
        }
    }
}