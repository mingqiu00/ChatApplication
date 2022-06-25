package vn.edu.hcmus.student.sv19127568.chatclient;

import vn.edu.hcmus.student.sv19127568.chatclient.forms.ChatForm;

import java.io.*;
import java.net.Socket;
import java.util.*;

/**
 * vn.edu.hcmus.student.sv19127568.chatclient
 * Created by Thu Nguyen
 * Date 12/29/2021 - 12:15 AM
 * Description: Client class
 */
public class Client {
    public static String username, password;
    public static HashMap<String, String> users;
    public static Vector<String> activeUsers;
    final static String HOST = "localhost";
    public static DataInputStream dis;
    public static DataOutputStream dos;
    public static Socket client;
    final static int PORT = 3333;

    public static void init() throws IOException {
        client = new Socket(HOST, PORT);

        dis = new DataInputStream(client.getInputStream());
        dos = new DataOutputStream(client.getOutputStream());

        dos.writeUTF(Client.username);

        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    dos.writeUTF("!userList");
                } catch (IOException e) {
                    try {
                        closeConnection();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }, 5000, 5000);

        Thread readMsg = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        String msg = dis.readUTF();
                        if (msg.equals("!userList")) {
                            ObjectInputStream ois = new ObjectInputStream(dis);
                            activeUsers = (Vector<String>) ois.readObject();
                            if (ChatForm.userModel  != null) {
                                for (String user : activeUsers) {
                                    if (!ChatForm.userModel.contains(user)) {
                                        ChatForm.userModel.addElement(user);
                                        if (!ChatForm.userBox.containsKey(user)) {
                                            ChatForm.userBox.put(user, "");
                                        }
                                    }
                                    for (Object s : ChatForm.userModel.toArray()) {
                                        String inactiveUser = (String) s;
                                        if (!activeUsers.contains(inactiveUser)) {
                                            ChatForm.userModel.removeElement(inactiveUser);
                                            ChatForm.userBox.remove(inactiveUser, ChatForm.userBox.get(inactiveUser));
                                        }
                                    }
                                }
                            }
                        } else if (msg.equals("!file")) {
                            String sender = dis.readUTF();
                            String fileName = dis.readUTF();
                            ChatForm.userBox.replace(sender, ChatForm.userBox.get(sender) + sender + " sent you " + fileName + "\n");
                            File file = new File(System.getProperty("user.dir") + "\\MYDATA\\" + fileName);
                            FileOutputStream fos = new FileOutputStream(file);
                            long fileSize = dis.readLong();
                            byte[] buffer = new byte[100000];
                            int bytesRead = 0;
                            while (fileSize > 0 && (bytesRead = dis.read(buffer, 0, (int)Math.min(buffer.length, fileSize))) != -1) {
                                fos.write(buffer, 0, bytesRead);
                                fileSize -= bytesRead;
                            }
                            ChatForm.updateDisplay(sender);
                            fos.close();
                        } else {
                            StringTokenizer st = new StringTokenizer(msg, ":");
                            String sender = st.nextToken();
                            ChatForm.userBox.replace(sender, ChatForm.userBox.get(sender) + msg + "\n");
                            ChatForm.updateDisplay(sender);
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        try {
                            closeConnection();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
        readMsg.start();
    }

    /**
     * check if a client is already existed
     * @param username String
     * @return true or false
     */
    public static boolean isExisted(String username) {
        for(Map.Entry<String, String> entry : users.entrySet()) {
            String key = entry.getKey();
            if (username.equals(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * check if a client enters the correct password for this username
     * @param username String
     * @param password String
     * @return true or false
     */
    public static boolean isValid(String username, String password) {
        return users.get(username).equals(password);
    }

    /**
     * add a new user to the user list
     * @param username String
     * @param password String
     */
    public static void add(String username, String password) {
        users.put(username, password);
        save();
    }

    /**
     * save user list to dat file
     */
    public static void save() {
        try {
            File fout = new File("data/users.dat");
            FileOutputStream fos = new FileOutputStream(fout);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(users);
            oos.flush();
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * load user list from dat file
     */
    public static void load() {
        try {
            File fin = new File("data/users.dat");
            FileInputStream fis = new FileInputStream(fin);
            ObjectInputStream ois = new ObjectInputStream(fis);
            users = (HashMap<String, String>) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * load user list from text file into a hash map
     * @return HashMap user list
     */
    public static HashMap<String, String> hashFromTextFile() {
        HashMap<String, String> map = new HashMap<String, String>();
        BufferedReader br = null;
        try {
            File file = new File("data/users.txt");
            br = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split("`");
                String username = tokens[0].trim();
                String password = tokens[1].trim();
                if (username.length() != 0 && password.length() != 0) {
                    map.put(username, password);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception ignored) {
                }
                ;
            }
        }
        return map;
    }

    public static void closeConnection() throws IOException {
        dis.close();
        dos.close();
        client.close();
        System.exit(0);
    }
}
