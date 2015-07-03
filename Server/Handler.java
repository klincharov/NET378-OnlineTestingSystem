
package server;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class Handler extends JFrame implements Runnable {

    private final JTextArea chatWindow;

    private ObjectOutputStream output;
    private ObjectInputStream input;
    private static ServerSocket server;
    private Socket connection;

    public Handler(Socket client) {

        this.connection = client;
        setTitle("Testing Server");
        //add(userText, BorderLayout.NORTH);
        chatWindow = new JTextArea();
        add(new JScrollPane(chatWindow));
        setSize(300, 300);
        setResizable(false);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        chatWindow.setEditable(false);
        chatWindow.setOpaque(false);
    }

    @Override
    public void run() {
        ServerSocket server = null;
        try {
            server = new ServerSocket(8080); // 100 people allowed to wait on port 8080 (backlog, queue length)
        } catch (IOException ex) {
            Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
        }
        displayMessage("Reading XML...");
        try {
            displayMessage("\nQuestions found: " + readXML("questions").length);
        } catch (JDOMException | IOException ex) {
            Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
        }
        displayMessage("\nWaiting for connection...");
        displayMessage("\nNow connected to " + connection.getInetAddress().getHostName());
        while (true) { //runs forever
            try { //ORDER IS IMPORTANT!!!!

                setupStreams();

                sendArrays();                                                   // readXML inside

            } catch (EOFException eof) { //End_Of_File, in our case End Of Stream
                System.out.println("\nServer ended the connection!"); //the other user leaves
            } catch (IOException ex) {
                Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JDOMException ex) {
                displayMessage("Error: XML file corrupt or missing!");
            } finally {
                try {
                    closeCrap();
                } catch (Exception ex) {
                    Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    //get stream to send and receive data
    private void setupStreams() throws IOException {
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();                                                         //clear leftover data
        input = new ObjectInputStream(connection.getInputStream());
        //only the other side can flush 
        displayMessage("\nStreams connected!");
    }

    private void closeCrap() throws IOException {
        System.out.println("\nClosing connections... \n");
        input.close();
        output.close();
        connection.close();

        try {
            input.close();
            output.close();
            connection.close(); //close the whole socket
        } catch (IOException ioException) {
            System.out.println("Connection reset");
        }
    }

    private void sendArrays() throws JDOMException {
        try {

            output.writeObject(readXML("questions"));                          //push the object into output stream
            output.writeObject(readXML("answers"));
            output.writeObject(readXML("corrects"));

            output.flush();                                                     //house keeping

            displayMessage("\nArrays sent!");
//testing TODO REMOVE
            System.out.print(Integer.toString(readXML("questions").length)
                    + " " + Integer.toString(readXML("answers").length)
                    + " " + Integer.toString(readXML("corrects").length));

        } catch (IOException ioException) {
            chatWindow.append("\n ERROR. COULD NOT BE SEND");
        }
    }

    private void displayMessage(final String text) {
        SwingUtilities.invokeLater(
                new Runnable() { // new thread
                    @Override
                    public void run() {
                        chatWindow.append(text); //updates the chat window text are with new message
                    }
                }
        );

    }

    private String[] readXML(String name) throws JDOMException, IOException {

        try {
            Document doc = new SAXBuilder().build("exam.xml");

            Element root = doc.getRootElement(); // shortcut for the root element

            List<Element> questionList = root.getChildren();
            //final int qCount = questionList.size();                                // ofsetting the questions 

            String[] questions = new String[questionList.size()];
            String[] answers = new String[questionList.size() * 4];
            String[] corrects = new String[questionList.size()];

            for (int temp = 0; temp < questionList.size(); temp++) {                  // everything in readXML method ????

                Element question = questionList.get(temp);
                questions[temp] = questionList.get(temp).getText().trim();          // put questions in array and remove tabulations and new lines; old one .replace("\t\n", "")

                //questionList.subList(temp, temp);                                 // TO SPLIT INTO GROUPS OF 4 ???
                for (int i = 0; i < 4; i++) {

                    answers[temp * 4 + i] = questionList.get(temp).getChildren("answer").get(i).getText();
                }

                String correct = question.getChild("correct").getText();            // gets the correct answers in the form of 0 1 2 3 strings

                corrects[temp] = correct;
                //System.out.println(questions[temp]);            // test printing questions FIX TABULATIONS !!!
            }

            for (String corr : corrects) {
                //System.out.print(corr + " ");
            }
            for (String answer : answers) {
                //System.out.println(answer);
            }
            //System.out.println("Questions: " + questions.length + " answers: " + answers.length + " correct : " + corrAnswers.length);

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
