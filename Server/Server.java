package server;

//~--- non-JDK imports --------------------------------------------------------
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

//~--- JDK imports ------------------------------------------------------------
import java.io.IOException;
import java.io.ObjectOutputStream;

import java.net.ServerSocket;
import java.net.Socket;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import static javax.swing.JFrame.EXIT_ON_CLOSE;

/**
 * Handler class which handles every new Client-thread.
 *
 * @author Georgi Klincharov F45686
 */
public class Handler extends JFrame implements Runnable {

    private static ServerSocket server;
    private final JTextArea mainWindow;
    private ObjectOutputStream output;
    private final Socket connection;

    /**
     * Constructor for the handler of the used in Server.java
     *
     * @param client which accepted from the socket.
     */
    public Handler(Socket client) {
        this.connection = client;

        // setting up the window and it's elements
        setTitle("Testing Server");
        mainWindow = new JTextArea();
        add(new JScrollPane(mainWindow));
        setSize(300, 300);
        setResizable(false);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        mainWindow.setEditable(false);
        mainWindow.setOpaque(false);
    }

    /**
     * Run everything.
     */
    @Override
    public void run() {

        try {
            server = new ServerSocket(8080);    // 100 people allowed to wait on port 8080 (backlog, queue length)
        } catch (IOException ex) {
            displayMessage("Error establishing connection! "
                    + "Port already in use!");
        }

        displayMessage("Reading XML...");

        try {
            displayMessage("Questions found: " + readXML("questions").length);
        } catch (JDOMException | IOException ex) {
            displayMessage("Error reading XML data! " + ex.getMessage());
        }
        displayMessage("Waiting for connection...");
        displayMessage("Now connected to " + connection.getInetAddress().getHostName()
                + " / " + connection.getLocalPort());

        // runs forever
        while (true) {
            setupStream();
            sendArrays();
        }
    }

    /**
     * Setup the Output Stream.
     *
     * @throws IOException when the streams couldn't be established.
     */
    private void setupStream() {
        try {
            output = new ObjectOutputStream(connection.getOutputStream());
            output.flush();    // clear leftover data

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Close everything up.
     *
     */
    private void close() {
        System.out.println("Closing connections... \n");

        try {
            output.close();

            // close the whole socket
            connection.close();
        } catch (IOException ex) {
            System.out.println("Could not close connection! " + ex.getMessage());
        }
    }

    /**
     * This method uses the ObjectOutputStream and readXML() method to read and
     * send questions, answers and correct answers to the Client.
     *
     * @throws JDOMException in case of problems with the XML file.
     */
    private void sendArrays() {

        try {
            output.writeObject(readXML("questions"));    // push the object into output stream
            output.writeObject(readXML("answers"));
            output.writeObject(readXML("corrects"));
            output.flush();                              // house keeping

            // good place to test what's coming out from readXML();
//          System.out.print(Integer.toString(readXML("questions").length)
//                  + " " + Integer.toString(readXML("answers").length)
//                  + " " + Integer.toString(readXML("corrects").length));
        } catch (IOException ex) {
            Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JDOMException ex) {
            Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * This method displays the given String and displays it.
     *
     * @param text the string passed to the method, to be displayed in an
     * MessageDialog
     */
    private void displayMessage(final String text) {
        SwingUtilities.invokeLater(new Runnable() {    // new thread
            @Override
            public void run() {
                mainWindow.append("\n" + text);               // updates the chat window text are with new message
            }
        });
    }

    /**
     * This method reads the XML file and sorts the questions, answers, correct
     * questions each in a separate array.
     *
     * The arrays are after that returned. Correct answers are saved as String
     * and not int, because of the return type of the method (String[]). Client
     * has to convert to int.
     *
     * @param name
     *
     * @return the desired String array with the parameter's name
     *
     * @throws JDOMException when there are problems with the XML content/style
     * @throws IOException when the file is missing or couldn't be read
     */
    private String[] readXML(String name) throws JDOMException, IOException {
        try {
            Document doc = new SAXBuilder().build("exam.xml");

            // shortcut for the root element
            Element root = doc.getRootElement();

            // list with everything bellow the root element
            List<Element> questionList = root.getChildren();

            // the needed arrays
            String[] questions = new String[questionList.size()];

            // 4 answers per question
            String[] answers = new String[questionList.size() * 4];
            String[] corrects = new String[questionList.size()];

            // loop through the questions
            for (int temp = 0; temp < questionList.size(); temp++) {

                // put questions in array and remove tabulations and new lines
                questions[temp] = questionList.get(temp).getText().trim();

                // get the answers
                for (int i = 0; i < 4; i++) {
                    answers[temp * 4 + i] = questionList.get(temp).getChildren("answer").get(i).getText();
                }

                // get every question element and it's answers nested in it
                Element question = questionList.get(temp);

                // convert correct to string
                String correct = question.getChild("correct").getText();    // gets the correct answers in the form of 0 1 2 3 strings

                corrects[temp] = correct;

                // good place to test print everything
                // System.out.println(questions[temp]);
            }

            // good place for test printing
//          for (String corr : corrects) {
//              //System.out.print(corr + " ");
//          }
//          for (String answer : answers) {
//              //System.out.println(answer);
//          }
            // determinig what to return
            switch (name) {
                case "questions":
                    return questions;

                case "answers":
                    return answers;

                case "corrects":
                    return corrects;
            }
        } catch (JDOMException | IOException ex) {
            displayMessage("XML error or missing file!\n" + ex.getMessage());
        }

        return null;
    }
}
