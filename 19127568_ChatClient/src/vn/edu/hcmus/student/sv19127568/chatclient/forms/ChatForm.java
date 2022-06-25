package vn.edu.hcmus.student.sv19127568.chatclient.forms;

import vn.edu.hcmus.student.sv19127568.chatclient.Client;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

/**
 * vn.edu.hcmus.student.sv19127568.chatclient
 * Created by Thu Nguyen
 * Date 1/2/2022 - 12:47 AM
 * Description: Main chat form
 */
public class ChatForm extends JPanel implements ActionListener {
    public static String targetUser;
    public static JTextArea txtChat, txtDisplay;
    JButton btnSend, btnChooseFile;
    JFileChooser fileChooser;
    public static JList<String> userList;
    public static DefaultListModel<String> userModel;
    public static HashMap<String, String> userBox = new HashMap<>();
    final JFrame frame;

    /**
     * ChatForm constructor
     * @param frame JFrame
     */
    public ChatForm(JFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());

        userList = new JList<String>();
        userModel = new DefaultListModel<>();
        userList.setModel(userModel);
        userList.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String s = userList.getSelectedValue();
                if (s != null) {
                    targetUser = s;
                    txtDisplay.setText(userBox.get(targetUser));
                    btnSend.setEnabled(true);
                    btnChooseFile.setEnabled(true);
                }
            }
        });

        JSplitPane mainSplitPane = new JSplitPane();
        mainSplitPane.setLeftComponent(new JScrollPane(userList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        JPanel txtPanel = new JPanel();
        txtPanel.setLayout(new BoxLayout(txtPanel, BoxLayout.PAGE_AXIS));

        txtDisplay = new JTextArea(23, 20);
        txtDisplay.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        txtDisplay.setMargin(new Insets(5,5,10,5));
        txtDisplay.setEditable(false);
        Font font = txtDisplay.getFont();
        txtDisplay.setFont(font.deriveFont(font.getSize() + 2.0f));
        JScrollPane displayScroll = new JScrollPane(txtDisplay);
        displayScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        displayScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        txtPanel.add(displayScroll);

        txtChat = new JTextArea(2, 20);
        txtChat.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        txtChat.setMargin(new Insets(10,5,10,5));
        font = txtChat.getFont();
        txtChat.setFont(font.deriveFont(font.getSize() + 2.0f));
        JScrollPane chatScroll = new JScrollPane(txtChat);
        chatScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        txtPanel.add(chatScroll);

        JPanel btnPanel = new JPanel(new FlowLayout());
        btnSend = new JButton("Send");
        btnSend.addActionListener(this);
        btnSend.setActionCommand("send");
        btnSend.setEnabled(false);
        btnPanel.add(btnSend);

        btnChooseFile = new JButton("Choose file");
        btnChooseFile.addActionListener(this);
        btnChooseFile.setActionCommand("file");
        btnChooseFile.setEnabled(false);
        btnPanel.add(btnChooseFile);
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        rightPanel.add(txtPanel, BorderLayout.CENTER);
        rightPanel.add(btnPanel, BorderLayout.PAGE_END);

        mainSplitPane.setRightComponent(rightPanel);
        add(mainSplitPane);
    }

    /**
     * create and show main chat GUI
     */
    public static void createAndShowGUI() {
        // Create and set up the window
        JFrame mainFrame = new JFrame("Chat with people");
        mainFrame.setPreferredSize(new Dimension(600,600));
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    Client.dos.writeUTF("!logout");
                    Client.closeConnection();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Add contents to the window
        ChatForm mainForm = new ChatForm(mainFrame);
        mainForm.setOpaque(true);
        mainFrame.add(mainForm);
        mainFrame.setContentPane(mainForm);

        // Display the window
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("send".equals(e.getActionCommand())) {
            String msg = txtChat.getText();
            if (msg.length() != 0) {
                if (targetUser != null) {
                    Thread sendMsg = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String msgSent = msg + "`" + targetUser;
                            try {
                                Client.dos.writeUTF(msgSent);
                                txtChat.setText("");
                                txtDisplay.append("me:" + msg + "\n");
                                userBox.replace(targetUser, txtDisplay.getText());
                            } catch (IOException e) {
                                try {
                                    Client.closeConnection();
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    });
                    sendMsg.start();
                } else {
                    showMessageDialog(this.frame, "Please choose someone to chat with!", "No recipient", WARNING_MESSAGE);
                }
            } else {
                showMessageDialog(this.frame, "Please type something to chat!", "Empty message", WARNING_MESSAGE);
            }
        }
        if ("file".equals(e.getActionCommand())) {
            if (targetUser != null) {
                int returnVal = fileChooser.showOpenDialog(this.frame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    try {
                        FileInputStream fis = new FileInputStream(file);
                        byte[] data = new byte[(int) file.length()];
                        BufferedInputStream bis = new BufferedInputStream(fis);
                        bis.read(data, 0, data.length);

                        Client.dos.writeUTF("!file`" + targetUser);

                        Client.dos.writeUTF(file.getName());
                        Client.dos.writeLong(file.length());

                        OutputStream os = Client.client.getOutputStream();
                        os.write(data, 0, data.length);
                        os.flush();
                    } catch (IOException ex) {
                        try {
                            Client.closeConnection();
                        } catch (IOException er) {
                            er.printStackTrace();
                        }
                    }
            } else {
                    showMessageDialog(this.frame, "Please choose someone to send your file!", "No recipient", WARNING_MESSAGE);
                }
            }
        }
    }

    public static void updateDisplay(String sender) {
        Object[] userArray = userModel.toArray();
        for (int i = 0; i < userArray.length; i++) {
            if (sender.equals(userArray[i])) {
                userModel.set(i, userModel.get(i) + "*");
            }
        }
        if (sender.equals(targetUser)) {
            txtDisplay.setText(userBox.get(sender));
        }
    }
}
