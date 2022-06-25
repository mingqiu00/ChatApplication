package vn.edu.hcmus.student.sv19127568.chatserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * vn.edu.hcmus.student.sv19127568.chatserver
 * Created by Thu Nguyen
 * Date 12/28/2021 - 11:56 PM
 * Description: client thread to handle multiple clients
 */
public class ClientThread implements Runnable {
    private final String username;
    final DataInputStream dis;
    final DataOutputStream dos;
    Socket client;
    boolean isActive;

    public ClientThread(Socket client, String username, DataInputStream dis, DataOutputStream dos) {
        this.dis = dis;
        this.dos = dos;
        this.username = username;
        this.client = client;
        this.isActive = true;
    }

    @Override
    public void run() {
        String msgReceived = null;
        do {
            try {
                msgReceived = dis.readUTF();
                if (msgReceived.equals("!logout")) {
                    System.out.println("Client " + username + " logged out!");
                    this.isActive = false;
                    this.client.close();
                    break;
                } else if (msgReceived.equals("!userList")) {
                    dos.writeUTF(msgReceived);
                    ObjectOutputStream oos = new ObjectOutputStream(dos);
                    Vector<String> activeUsers = new Vector<>();
                    for (ClientThread user : Server.clients) {
                        if (user.isActive && !user.username.equals(this.username)) {
                            activeUsers.add(user.username);
                        }
                    }
                    oos.writeObject(activeUsers);
                    oos.flush();
                } else {
                    StringTokenizer st = new StringTokenizer(msgReceived, "`");
                    String msgSent = st.nextToken();
                    String recipient = st.nextToken();

                    if (msgSent.equals("!file")) {
                        String fileName = dis.readUTF();
                        File file = new File(System.getProperty("user.dir") + "\\DATA\\" + fileName);
                        FileOutputStream fos = new FileOutputStream(file);
                        long fileSize = dis.readLong();
                        byte[] buffer = new byte[100000];
                        int bytesRead = 0;
                        while (fileSize > 0 && (bytesRead = dis.read(buffer, 0, (int)Math.min(buffer.length, fileSize))) != -1) {
                            fos.write(buffer, 0, bytesRead);
                            fileSize -= bytesRead;
                        }

                        for (ClientThread user : Server.clients) {
                            if (user.username.equals(recipient) && user.isActive) {
                                FileInputStream fis = new FileInputStream(file);
                                byte[] data = new byte[(int) file.length()];
                                BufferedInputStream bis = new BufferedInputStream(fis);
                                bis.read(data, 0, data.length);

                                user.dos.writeUTF("!file");
                                user.dos.writeUTF(username);
                                user.dos.writeUTF(file.getName());
                                user.dos.writeLong(file.length());

                                OutputStream os = user.client.getOutputStream();
                                os.write(data, 0, data.length);
                                os.flush();
                                System.out.println("Send file " + fileName + " from client " + username + " to client " + recipient);
                                break;
                            }
                        }
                    } else {
                        for (ClientThread user : Server.clients) {
                            if (user.username.equals(recipient) && user.isActive) {
                                user.dos.writeUTF(this.username + ": " + msgSent);
                                break;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                try {
                    dis.close();
                    dos.close();
                    client.close();
                    System.exit(0);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } while (true);
    }
}
