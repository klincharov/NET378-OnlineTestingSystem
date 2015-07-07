package client;

import java.io.*;
import java.net.*;
import java.awt.event.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 * Client side of the Client-Server online Testing system
 *
 * @author Georgi Klincharov F45686
 */
public class Client extends JFrame implements ActionListener {

    /**
     * This is the array in which the QUESTIONS received from the Server will be
     * stored.
     */
    public String[] questions;
    /**
     * This is the array in which the ANSWERS received from the Server will be
     * stored.
     */
    public String[] answers;
    /**
     * This is the array in which the CORRECT ANSWERS received from the Server
     * will be stored.
     */
    public String[] corrects;                                                 //other one hides this field

    /**
     * The InputStream through which we will communicate with the Server.
     *
     * No OutputStream is needed at this point. Only if we need to send back the
     * results in order to be stored on the server
     */
    private ObjectInputStream input;

    /**
     * Variable used with the constructor of the Client.
     *
     * Used for the IP address of the Server we will try to connect to.
     */
    private final String serverIP;
    /**
     * Socket variable used for establishing the connection and input stream.
     */
    private Socket connection;
    /**
     * JLabel used for displaying questions on it with .setText(); method.
     */
    JLabel questionLabel;
    /**
     * Array of radio buttons for the answers.
     */
    JRadioButton jButtons[] = new JRadioButton[5];
    /**
     * Buttons for navigation: Next and Bookmark.
     */
    JButton nextBut, bookBut;
    /**
     * Creating a set of buttons with the same ButtonGroup object.
     *
     * Turning "on" one of those buttons turns off all other buttons in the
     * group.
     */
    ButtonGroup buttonGroup;
    /**
     * Helper variables for keeping track of what's going on.
     *
     * They are pretty much self-explanatory.
     */
    int count = 0, current = 0, bookmarkCount = 1, now = 0;
    int[] bookmarkArray;

    //constructor
    /**
     * The constructor of our Client initialized in ClientTest.java
     *
     * @param host the address of the server.
     */
    public Client(String host) {

        serverIP = host;

        questionLabel = new JLabel();
        add(questionLabel);

        buttonGroup = new ButtonGroup();

        for (int i = 0; i < 5; i++) {
            jButtons[i] = new JRadioButton();
            add(jButtons[i]);
            buttonGroup.add(jButtons[i]);

        }
        nextBut = new JButton("Next");

        bookBut = new JButton("Bookmark");
        bookBut.addActionListener(this);
        add(bookBut);

        nextBut.addActionListener(this);
        add(nextBut);

        questionLabel.setBounds(30, 40, 600, 20);                                           // int bookmarkCount, int y, int width, int height; X&Y position of upperleft 
        jButtons[0].setBounds(50, 80, 100, 20);
        jButtons[1].setBounds(50, 110, 100, 20);
        jButtons[2].setBounds(50, 140, 100, 20);
        jButtons[3].setBounds(50, 170, 100, 20);

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

    /**
     * Setup and initialize everything and connect to server.
     *
     */
    public void startRunning() {
        //EOFException ???

        connectToServer();
        setupStream();
        questions = getQuestions();
        answers = getAnswers();
        corrects = getCorrects();
        set();
    }
    /**
     * Container for the upcoming messages. Everything else will be appended at
     * the end and then displayed.
     */
    String message;

    /**
     * Establish the connection and append the message.
     *
     * @throws IOException when the connection couldn't be established.
     */
    private void connectToServer() {
        try {
            connection = new Socket(InetAddress.getByName(serverIP), 8080);
            message = ("You are connected to " + connection.getInetAddress().getHostName());
        } catch (IOException ex) {
            displayMessage("Error connecting to server " + ex.getMessage());
            close();
        }

    }

    /**
     * Setup the Input Stream.
     *
     * @throws IOException when the streams couldn't be established.
     */
    private void setupStream() {

        try {
            input = new ObjectInputStream(connection.getInputStream());
        } catch (IOException ex) {
            displayMessage("Error establishing stream! " + ex.getMessage());
        }
    }

    /**
     * This method uses the ObjectInpuStream and reads the object. The object is
     * then casted as a String array as needed.
     *
     * Order for sending/getting arrays is important!
     *
     * @return String[] array with the exam's questions.
     */
    String[] getQuestions() {
        try {
            Object obj = input.readObject();
            message += "\nQuestions received: " + Integer.toString(((String[]) obj).length);
            //displayMessage("Questions received: " + Integer.toString(((String[])obj).length));
            return ((String[]) obj);
        } catch (IOException | ClassNotFoundException ex) {
            displayMessage("Error getting questions! " + ex.getMessage());
            return null;
        }
    }

    /**
     * This method uses the ObjectInpuStream and reads the object. The object is
     * then casted as a String array as needed.
     *
     * Order for sending/getting arrays is important!
     *
     * @return String[] array with the exam's answers.
     */
    String[] getAnswers() {
        try {
            Object obj = input.readObject();
            message += "\nAnswers received: " + Integer.toString(((String[]) obj).length);

            return ((String[]) obj);
        } catch (IOException | ClassNotFoundException ex) {
            displayMessage("Error getting answers! " + ex.getMessage());
            return null;
        }

    }

    /**
     * This method uses the ObjectInpuStream and reads the object. The object is
     * then casted as a String array as needed.
     *
     * Order for sending/getting arrays is important!
     *
     * @return String[] array with the exam's CORRECT answers.
     */
    String[] getCorrects() {
        try {
            Object obj = input.readObject();
            message += "\nCorrect answers received: " + Integer.toString(((String[]) obj).length);

            displayMessage(message);

            return ((String[]) obj);
        } catch (IOException | ClassNotFoundException ex) {
            displayMessage("Error getting correct answers! " + ex.getMessage());
            return null;
        }

    }

    /**
     * This method closes all the connections and performs housekeeping.
     *
     */
    private void close() {

        try {
            input.close();
            connection.close();
        } catch (IOException | NullPointerException ex) {
            displayMessage("Error terminating connections! " + ex.getMessage());

        }
    }

    /**
     * This method displays the given String and displays it.
     *
     * @param text the string passed to the method, to be displayed in an
     * MessageDialog
     */
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

    /**
     * Listens for action events and performs the according actions
     *
     * @param evt ActionEvent
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        this.bookmarkArray = new int[questions.length];
        // if the source is the "Next" button
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

            // if we have reached the last question:
            if (current == questions.length) {
                nextBut.setEnabled(false);

                //"Bookmark" button is now "Results" button
                bookBut.setText("Results");

                // remove selector - nothing is selected
                jButtons[4].setSelected(true);
                // remove last question - nothing is displayed 
                questionLabel.setText("");

                // remove all answers - nothing is displayed
                for (int i = 0; i < 4; i++) {
                    jButtons[i].setText("");

                }

            }
        }

        // if the source is the "Bookmark" button:
        if (evt.getActionCommand().equals("Bookmark")) {

            // create new bookmark for the question and place it 
            JButton bk = new JButton("Bookmark " + bookmarkCount);
            bk.setBounds(450, 10 + 30 * bookmarkCount, 120, 30);
            add(bk);
            bk.addActionListener(this);
            bookmarkArray[bookmarkCount] = current;
            bookmarkCount++;
            current++;

            set();
            if (current == questions.length - 1) {
                bookBut.setText("Results");
            }
            // refresh the GUI with the new buttons
            setVisible(false);
            setVisible(true);
        }
        // loop through the placed bookmarks
        for (int i = 0, z = 1; i < bookmarkCount; i++, z++) {
            if (evt.getActionCommand().equals("Bookmark " + z)) {
                try {
                    if (check()) {
                        count++;
                    }
                } catch (IOException | ClassNotFoundException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
                now = current;
                current = bookmarkArray[z];

                set();
                // disable the used bookmark button
                ((JButton) evt.getSource()).setEnabled(false);
                current = now;

            }
        }
        // if the source is the "Results" button:
        if (evt.getActionCommand().equals("Results")) {  //!!! Results != Result

            current++;
            System.out.println("Correct " + count);
            displayMessage("Correct " + count + " out of " + questions.length + "!");
            //System.exit(0);
        }

    }

    /**
     * Method for setting/displaying information on the screen.
     *
     */
    void set() {

        try {
            // every answer's selector is left blank so no accidental point by 
            // just clicking next could occur
            jButtons[4].setSelected(true);

            // loop through questions and sets the question questionLabel
            if (current < questions.length) {
                questionLabel.setText("Question " + (current + 1) + ": " + questions[current]);

                // helping integer for offsetting the answers
                final int L = current * 4;

                // loop and place every pair of 4 answers
                for (int k = 0; k < 4; k++) {

                    jButtons[k].setText(answers[k + L]);

                }

                for (int i = 0, j = 0; i <= 90; i += 30, j++) {
                    jButtons[j].setBounds(50, 80 + i, 600, 20);
                }
            }

        } catch (NullPointerException ex) {
            System.out.println("Error at question number " + count + ex.getMessage());
        }
    }

    /**
     * Checks if the selected answer is selected
     *
     * @return boolean for the question. Increments count if true.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    boolean check() throws IOException, ClassNotFoundException {

        // converting from String to int for easier comparison
        int[] corrInt = new int[corrects.length];

        for (int i = 0; i < corrects.length; i++) {
            corrInt[i] = Integer.parseInt(corrects[i]);
        }

        // loop through the correct answers
        for (int i = 0; i < corrects.length; i++) {

            // checks if the correct answer is selected
            if (current == i) {
                
                // good point to print to console for easier testing
                return jButtons[corrInt[i]].isSelected();
            }
        }

        return false;
    }

}
