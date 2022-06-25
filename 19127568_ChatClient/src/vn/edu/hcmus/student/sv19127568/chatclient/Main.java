package vn.edu.hcmus.student.sv19127568.chatclient;

import vn.edu.hcmus.student.sv19127568.chatclient.forms.ConnectionForm;

import java.io.IOException;

/**
 * vn.edu.hcmus.student.sv19127568.chatclient
 * Created by Thu Nguyen
 * Date 12/28/2021 - 2:28 AM
 * Description: Main class
 */
public class Main {
    /**
     * main function
     * @param args String
     */
    public static void main(String[] args) throws IOException {
        Client.load();
        javax.swing.SwingUtilities.invokeLater(ConnectionForm::createAndShowGUI);
    }
}
