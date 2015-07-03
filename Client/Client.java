
package client;

import java.io.*;
import java.net.*;
import java.awt.event.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

public class Client extends JFrame implements ActionListener {
//TODO REFRACTOR AND RENAME VARS TO MORE MEANINGFUL NAMES!
//TODO JAVADOC!!!

    public String[] questions;
    public String[] answers;
    public String[] corrects;                                                 //other one hides this field

    private ObjectOutputStream output;
    private ObjectInputStream input;

    private final String serverIP;
    private Socket connection;
    JLabel label;
    JRadioButton jb[] = new JRadioButton[5];
    JButton nextBut, bookBut;
    ButtonGroup bg;

    int count = 0, current = 0, x = 1, y = 1, now = 0;
    int m[] = new int[10];

    //constructor
    public Client(String host) {
        serverIP = host;

        label = new JLabel();
        add(label);

        bg = new ButtonGroup();

        for (int i = 0; i < 5; i++) {
            jb[i] = new JRadioButton();
            add(jb[i]);
            bg.add(jb[i]);

        }
        nextBut = new JButton("Next");

        bookBut = new JButton("Bookmark");
        bookBut.addActionListener(this);
        add(bookBut);

        nextBut.addActionListener(this);
        add(nextBut);

        label.setBounds(30, 40, 600, 20);                                           // int x, int y, int width, int height; X&Y position of upperleft 
        jb[0].setBounds(50, 80, 100, 20);
        jb[1].setBounds(50, 110, 100, 20);
        jb[2].setBounds(50, 140, 100, 20);
        jb[3].setBounds(50, 170, 100, 20);

        nextBut.setBounds(150, 240, 100, 30);
        bookBut.setBounds(270, 240, 100, 30);

        setTitle("Online Test!");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        setVisible(true);
        setSize(600, 350); //600 350 original

        setResizable(false);
        setLocationRelativeTo(null);
    }

    //connect to the server
    public void startRunning() throws ClassNotFoundException, IOException {
        try {                            // ORDER IS IMPORTANT !!!

            connectToServer();
            setupStreams();

            questions = getQuestions();
            answers = getAnswers();
            corrects = getCorrects();
            //label.setText(questions[current]);
            set();

            //testing what we've got
            System.out.println(questions.length + " " + answers.length + " " + corrects.length);

            for (String str : corrects) {
                System.out.print(Integer.parseInt(str) + " ");

            }
            System.out.println("");

        } catch (EOFException eofException) {
            //displayMessage("\nClient terminated the connection");
        } catch (IOException ex) {
            //displayMessage(ex.getMessage());
            displayMessage("Disconnected!");
            setVisible(false);          
            System.exit(0);

        } finally {
            closeCrap();

        }
    }
    //connect to server 
    String message;

    private void connectToServer() throws IOException {
        connection = new Socket(InetAddress.getByName(serverIP), 8080);
        message = ("You are connected to " + connection.getInetAddress().getHostName());
    }

    //setup streams
    private void setupStreams() throws IOException {
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush(); //house keeping
        input = new ObjectInputStream(connection.getInputStream());
        //displayMessage("Streams connected! \n");
    }

    String[] getQuestions() throws IOException, ClassNotFoundException {
        Object obj = input.readObject();
        message += "\nQuestions received: " + Integer.toString(((String[]) obj).length);
        //displayMessage("Questions received: " + Integer.toString(((String[])obj).length));
        return ((String[]) obj);
    }

    String[] getAnswers() throws IOException, ClassNotFoundException {
        Object obj = input.readObject();
        message += "\nAnswers received: " + Integer.toString(((String[]) obj).length);
        displayMessage(message);
        return ((String[]) obj);

    }

    String[] getCorrects() throws IOException, ClassNotFoundException {
        Object obj = input.readObject();
        return ((String[]) obj);

    }

    //close the streams and sockets
    private void closeCrap() {
        //displayMessage("\nClosing everything down...");
//        ableToType(false);
        try {
            output.flush();
            output.close();
            input.close();
            connection.close();
        } catch (IOException | NullPointerException ex) {

        }
    }

    //display message; change/update chatWindow
    private void displayMessage(final String text) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(rootPane, text);

                    }
                }
        );
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == nextBut) {
            try {
                //iterating through everything
                if (check()) {
                    count++;
                }
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }

            current++;
            set();

            if (current == questions.length) {
                nextBut.setEnabled(false);
                bookBut.setText("Results");
                
                jb[4].setSelected(true); //remove selector
                label.setText("");
                
                for (int i = 0; i < 4; i++) {
                    jb[i].setText("");
                    
                }

            }
        }

        if (evt.getActionCommand().equals("Bookmark")) {
            JButton bk = new JButton("Bookmark " + x);
            bk.setBounds(450, 10 + 30 * x, 120, 30);
            add(bk);
            bk.addActionListener(this);
            m[x] = current;
            x++;
            current++;

            set();
            if (current == questions.length - 1) {
                bookBut.setText("Results");
            }
            setVisible(false);
            setVisible(true);
        }
        for (int i = 0, z = 1; i < x; i++, z++) {
            if (evt.getActionCommand().equals("Bookmark " + z)) {
                try {
                    if (check()) {
                        count++;
                    }
                } catch (IOException | ClassNotFoundException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
                now = current;
                current = m[z];

                set();
                ((JButton) evt.getSource()).setEnabled(false);
                current = now;

            }
        }
        if (evt.getActionCommand().equals("Results")) {  //!!! Results != Result

            current++;
            System.out.println("Correct " + count);
            displayMessage("Correct " + count + " out of " + questions.length + "!");
            //System.exit(0);
        }

    }

    void set() { //WORKING!
        try {
            //sets the question label
            jb[4].setSelected(true); // everything is left blank so no accidental point by just clicking next could occur

            if (current < questions.length) {
                label.setText("Question " + (current + 1) + ": " + questions[current]);

                int l = current * 4;
                for (int k = 0; k < 4; k++) {

                    jb[k].setText(answers[k + l]);

                }

//            if (current < questions.length) {
//                label.setText("Question " + (current + 1) + ": " + questions[current]);
//                for (int j = current; j < (current * 4); j++) {
//
//                    // k goes through 0, 1 ,2 ,3 needed for the radio buttons
//                    int l = current * 4;
//                    for (int k = 0; k < 4; k++) { // was (i+3)
//                        System.out.println("Cur= " + current + " * 4= " + (current * 4) + " J= " + j + " K= " + k);
//
//                        //System.out.println(" J= " + j + " K= " + k);
//                        jb[k].setText(answers[j+k] + " @ " + (j+k));
//
//                    }
//                }
//            }
                for (int i = 0, j = 0; i <= 90; i += 30, j++) {
                    jb[j].setBounds(50, 80 + i, 600, 20);
                }
            }

        } catch (NullPointerException ex) {
            System.out.println("Error at: " + count + ex.getMessage()); //testing what's going on
        }
    }

    boolean check() throws IOException, ClassNotFoundException {

        // converting to integers for easier comparison
        int[] corrInt = new int[corrects.length];

        for (int i = 0; i < corrects.length; i++) {
            corrInt[i] = Integer.parseInt(corrects[i]);                     // or .valueOf ?
        }

        for (int i = 0; i < corrects.length; i++) {

            //System.out.print(corrInt[i] + " ");
            if (current == i) {
                System.out.println(jb[corrInt[i]].isSelected());
                return jb[corrInt[i]].isSelected();

            }
        }

        return false;
    }

}
