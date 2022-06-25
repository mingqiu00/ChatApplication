package vn.edu.hcmus.student.sv19127568.chatserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

/**
 * vn.edu.hcmus.student.sv19127568.chatserver
 * Created by Thu Nguyen
 * Date 12/29/2021 - 12:02 AM
 * Description: Server class
 */
public class Server {
    public static Vector<ClientThread> clients = new Vector<>();
    public static int numOfUsers = 0;

    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(3333);
        Socket client;
        do {
            client = ss.accept();

            DataInputStream dis = new DataInputStream(client.getInputStream());
            DataOutputStream dos = new DataOutputStream(client.getOutputStream());

            String username = dis.readUTF();
            ClientThread newClient = new ClientThread(client, username, dis, dos);
            System.out.println("Client " + username + " logged in!");
            Thread t = new Thread(newClient);

            clients.add(newClient);
            t.start();
            numOfUsers++;
        } while (true);
    }
}
