package vn.edu.hcmus.student.sv19127568.chatclient.forms;

import vn.edu.hcmus.student.sv19127568.chatclient.Client;
import vn.edu.hcmus.student.sv19127568.chatclient.utils.SpringUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import static javax.swing.JOptionPane.*;

/**
 * vn.edu.hcmus.student.sv19127568.chatclient
 * Created by Thu Nguyen
 * Date 12/31/2021 - 10:57 PM
 * Description: Connection Form
 */
public class ConnectionForm extends JPanel implements ActionListener {
    JTextField txtUsername;
    JPasswordField txtPassword;
    JButton btnLogin, btnSignup;
    JDialog signupDialog;
    final JFrame frame;
    final static int GAP = 10;

    /**
     * ConnectionForm constructor
     * @param frame JFrame
     */
    public ConnectionForm(JFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        add(mainPanel, BorderLayout.CENTER);
        mainPanel.add(createEntryFields());
        add(createButtonBars(), BorderLayout.PAGE_END);
    }

    /**
     * create entry fields to receive user's input
     * @return panel JComponent
     */
    protected JComponent createEntryFields() {
        JPanel panel = new JPanel(new SpringLayout());
        String[] labelStrings = {"Username: ", "Password: "};
        JLabel[] labels = new JLabel[labelStrings.length];
        JComponent[] fields = new JComponent[labels.length];
        int fieldNum = 0;

        txtUsername = new JTextField();
        txtUsername.setColumns(20);
        fields[fieldNum++] = txtUsername;

        txtPassword = new JPasswordField();
        txtPassword.setColumns(20);
        fields[fieldNum++] = txtPassword;

        //Associate label/field pairs, add everything,
        //and lay it out.
        for (int i = 0; i < labelStrings.length; i++) {
            labels[i] = new JLabel(labelStrings[i], JLabel.TRAILING);
            labels[i].setLabelFor(fields[i]);
            panel.add(labels[i]);
            panel.add(fields[i]);
        }
        SpringUtil.makeCompactGrid(panel,
                labelStrings.length, 2,
                GAP, GAP, //init x,y
                GAP, GAP);//xpad, ypad
        return panel;
    }

    /**
     * create button bars
     * @return panel JComponent
     */
    protected  JComponent createButtonBars() {
        JPanel panel = new JPanel(new FlowLayout());
        btnLogin = new JButton("Log in");
        btnLogin.addActionListener(this);
        btnLogin.setActionCommand("login");
        panel.add(btnLogin);
        panel.add(Box.createRigidArea(new Dimension(5, 0)));
        btnSignup = new JButton("Sign up");
        btnSignup.addActionListener(this);
        btnSignup.setActionCommand("signup");
        panel.add(btnSignup);
        return panel;
    }

    private void createSignupDialog() {
        signupDialog = new JDialog(this.frame, "Create an account", true);
        signupDialog.setLayout(new BorderLayout());
        JPanel entryPanel = new JPanel(new SpringLayout());

        String[] labelStrings = {"Username: ", "Password: ", "Confirm password: "};
        JLabel[] labels = new JLabel[labelStrings.length];
        JTextField txtNewUsername, txtNewPassword, txtConfirmPassword;
        JComponent[] fields = new JComponent[labelStrings.length];
        int fieldNum = 0;

        txtNewUsername = new JTextField();
        txtNewUsername.setColumns(20);
        fields[fieldNum++] = txtNewUsername;

        txtNewPassword = new JPasswordField();
        txtNewPassword.setColumns(20);
        fields[fieldNum++] = txtNewPassword;

        txtConfirmPassword = new JPasswordField();
        txtConfirmPassword.setColumns(20);
        fields[fieldNum++] = txtConfirmPassword;

        //Associate label/field pairs, add everything,
        //and lay it out.
        for (int i = 0; i < labelStrings.length; i++) {
            labels[i] = new JLabel(labelStrings[i], JLabel.TRAILING);
            labels[i].setLabelFor(fields[i]);
            entryPanel.add(labels[i]);
            entryPanel.add(fields[i]);
        }
        SpringUtil.makeCompactGrid(entryPanel,
                labelStrings.length, 2,
                GAP, GAP, //init x,y
                GAP, GAP);//xpad, ypad
        signupDialog.add(entryPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton btnOK, btnCancel;
        btnOK = new JButton("OK");
        btnOK.addActionListener(e -> {
            String username = txtNewUsername.getText();
            String password = txtNewPassword.getText();
            String cfPassword = txtConfirmPassword.getText();
            if (username.length() != 0 && password.length() != 0 && cfPassword.length() != 0) {
                if (Client.isExisted(username)) {
                    showMessageDialog(this.frame, "Please log in or check your information!", "Existing account", ERROR_MESSAGE);
                } else {
                    if (password.equals(cfPassword)) {
                        Client.add(username, password);
                        showMessageDialog(this.frame, "Sign up successfully!");
                        signupDialog.setVisible(false);
                    } else {
                        showMessageDialog(this.frame, "Please check your confirm password!", "Not matching confirm password", ERROR_MESSAGE);
                    }
                }
            } else {
                showMessageDialog(this.frame, "Please fill out all fields to connect!", "Missing fields", WARNING_MESSAGE);
            }
        });
        btnPanel.add(btnOK);
        btnPanel.add(Box.createRigidArea(new Dimension(5, 0)));

        btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> signupDialog.setVisible(false));
        btnPanel.add(btnCancel);
        signupDialog.add(btnPanel, BorderLayout.PAGE_END);

        signupDialog.setSize(new Dimension(400, 240));
        signupDialog.setLocationRelativeTo(null);
        signupDialog.setVisible(true);
    }

    /**
     * create and show the connection form GUI
     */
    public static void createAndShowGUI() {
        //Create and set up the window.
        JFrame mainFrame = new JFrame("Connection");
        mainFrame.setPreferredSize(new Dimension(340,180));
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add contents to the window.
        ConnectionForm connectionForm = new ConnectionForm(mainFrame);
        mainFrame.add(connectionForm);
        connectionForm.setOpaque(true);
        mainFrame.setContentPane(connectionForm);

        //Display the window.
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("login".equals(e.getActionCommand())) {
            Client.username = txtUsername.getText();
            Client.password = txtPassword.getText();
            if (Client.username.length() != 0 && Client.password.length() != 0) {
                if (Client.isExisted(Client.username)) {
                    if (Client.isValid(Client.username, Client.password)) {
                        this.frame.setVisible(false);
                        try {
                            Client.init();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        javax.swing.SwingUtilities.invokeLater(ChatForm::createAndShowGUI);
                    } else {
                        showMessageDialog(this.frame, "Please check your password!", "Invalid password", ERROR_MESSAGE);
                    }
                } else {
                    showMessageDialog(this.frame, "Please sign up or check your information!", "Not existing account", ERROR_MESSAGE);                }
            } else {
                showMessageDialog(this.frame, "Please fill out all fields to connect!", "Missing fields", WARNING_MESSAGE);
            }
        }
        if ("signup".equals(e.getActionCommand())) {
            javax.swing.SwingUtilities.invokeLater(this::createSignupDialog);
        }
    }
}
