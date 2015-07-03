package client;
import java.io.*;
import java.net.*;
import java.awt.event.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

public class Client extends JFrame implements ActionListener{
//TODO REFRACTOR AND RENAME VARS TO MORE MEANINGFUL NAMES!

    
//    private JPanel  panel, welcomePanel, mainPanel;
//    private JTextField userText, userName, facNumber;
//    private JTextArea chatWindow;
//    private JButton startButton;
//    private String message;
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
    
    int count=0, current=0, x=1, y=1, now=0;
    int m[] = new int[10];          
    
    
    //constructor
    public Client(String host){
    serverIP = host;
    
        label = new JLabel();
        add(label);
        
        bg = new ButtonGroup();
        
        for(int i = 0; i < 5; i++){
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
    public void startRunning() throws ClassNotFoundException, IOException{
        try{                            // ORDER IS IMPORTANT !!!
            
            connectToServer();
            setupStreams();
            
            questions = getQuestions();
            answers = getAnswers();
            corrects = getCorrects(); 
            //label.setText(questions[current]);
            set();
            
            //testing what we've got
            System.out.println(questions.length + " " + answers.length + " " + corrects.length);
         
            for (String str : corrects){ 
                System.out.print(Integer.parseInt(str) + " ");
                
            }
            System.out.println("");

                        
           
                                                             
            
        }catch(EOFException eofException){
            //displayMessage("\nClient terminated the connection");
        }catch(IOException ex){
            //displayMessage(ex.getMessage());
            displayMessage("Disconnected!");
            setVisible(false);          //TODO test if works ok !
            System.exit(0);
            
        
            
        
        }finally{
            closeCrap();
                                             
        }
        }
    //connect to server 
    String message;
    private void connectToServer() throws IOException{
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
    
    String[] getQuestions() throws IOException, ClassNotFoundException{
        Object obj = input.readObject();
        message += "\nQuestions received: " + Integer.toString(((String[])obj).length);
        //displayMessage("Questions received: " + Integer.toString(((String[])obj).length));
        return ((String[])obj);
    }
    
    String[] getAnswers() throws IOException, ClassNotFoundException{
        Object obj = input.readObject();
        message += "\nAnswers received: " + Integer.toString(((String[])obj).length);
        displayMessage(message);
        return ((String[])obj);
           
    }
    String[] getCorrects() throws IOException, ClassNotFoundException{
        Object obj = input.readObject();
        return ((String[])obj);
           
    }
    
    //close the streams and sockets
    private void closeCrap(){
        //displayMessage("\nClosing everything down...");
//        ableToType(false);
        try{
            output.flush();
            output.close();
            input.close();
            connection.close();
        }catch (IOException | NullPointerException ex){
            
        }
    }

    
    //display message; change/update chatWindow
    private void displayMessage(final String text) {
        SwingUtilities.invokeLater(
                new Runnable(){
                    @Override
                    public void run(){
                    JOptionPane.showMessageDialog(rootPane, text);
                    
                    }
                }
        );
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource()== nextBut ){                                          
            try {
            //iterating through everything
            if(check()){
                count++;
               }
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        current++;
        System.out.println("current: " + current);
        set();
        
        if(current == 9){
            nextBut.setEnabled(false);
            bookBut.setText("Results");
            }
        }
     
        if (evt.getActionCommand().equals("Bookmark")){
            JButton bk = new JButton("Bookmark " + x);
            bk.setBounds(450, 10 + 30 * x, 120 ,30);
            add(bk);
            bk.addActionListener(this);
            m[x] = current;
            x++;
            current++;
            System.out.println("current: " + current);

            set();
            if(current == 9 ){
                bookBut.setText("Results");
            }           
            setVisible(false);
            setVisible(true);
        }
        for(int i = 0, z = 1; i < x; i++, z++){
            if(evt.getActionCommand().equals("Bookmark " + z)){
                try {
                    if(check()){
                        count++;
                    }
                } catch (IOException | ClassNotFoundException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                } 
                now = current;
                current = m[z];
                        System.out.println("current: " + current);

                set();
                ((JButton)evt.getSource()).setEnabled(false);
                current = now;
                        System.out.println("current: " + current);

            }
        }
        if(evt.getActionCommand().equals("Result")){
            
            current++;
            System.out.println("Correct " + count);
            displayMessage("Correct " + count);
            System.exit(0);
        }
    }
        boolean check() throws IOException, ClassNotFoundException{
            
            // converting to integers for easier comparison
            int[] corrInt = new int[corrects.length];
            
            for (int i = 0; i < corrects.length; i++){
                corrInt[i] = Integer.parseInt(corrects[i]);                     // or .valueOf ?
            }
            
            for(int i = 0; i < corrects.length; i++){

                System.out.print(corrInt[i] + " ");
                
                if (current==i) {
                    System.out.println(jb[corrInt[i]].isSelected());
                    return jb[corrInt[i]].isSelected();
                    
                }
            }
                   
            return false;            
        }

    void set()                                                                  // rework with array
    {  
        try{
        jb[4].setSelected(true);  
        if(current==0)  
        {  
            label.setText("Que1: " + questions[current]);  
            jb[0].setText("Asia");jb[1].setText("Europe");jb[2].setText("Africa");jb[3].setText("America");   
        }  
        if(current==1)  
        {  
            label.setText("Que2: " + questions[current]);  
            jb[0].setText("Tuna");jb[1].setText("Cow");jb[2].setText("Whale");jb[3].setText("Lobster");  
        }  
        if(current==2)  
        {  
            label.setText("Que3: " + questions[current]);  
            jb[0].setText("5");jb[1].setText("6");jb[2].setText("7");jb[3].setText("8");  
        }  
        if(current==3)  
        {  
            label.setText("Que4: " + questions[current]);  
            jb[0].setText("60");jb[1].setText("30");jb[2].setText("50");jb[3].setText("80");  
        }  
        if(current==4)  
        {  
            label.setText("Que5: " + questions[current]);  
            jb[0].setText("Hypertext Markup Language");jb[1].setText("Hyper Tool Marker Language");jb[2].setText("Hypertranslation Micro Language");jb[3].setText("jtek");  
        }  
        if(current==5)  
        {  
            label.setText("Que6: " + questions[current]);  
            jb[0].setText("2 > 3");jb[1].setText("Fish can swim");jb[2].setText("Human can breathe and eat and sleep and walk");jb[3].setText("The first letter on the English alphabet is the letter \"A\"");  
        }  
        if(current==6)  
        {  
            label.setText("Que7: " + questions[current]);  
            jb[0].setText("Gray");jb[1].setText("Rose");jb[2].setText("Pink");jb[3].setText("All of the above");  
                          
        }  
        if(current==7)  
        {  
            label.setText("Que8: " + questions[current]);  
            jb[0].setText("1");jb[1].setText("0");jb[2].setText("The same number");jb[3].setText("-1");         
        }  
        if(current==8)  
        {  
            label.setText("Que9: " + questions[current]);  
            jb[0].setText("Red");jb[1].setText("Yellow");jb[2].setText("Magenta");jb[3].setText("Cyan");  
        }  
        if(current==9)  
        {  
            label.setText("Que10: " + questions[current]);  
            jb[0].setText("Australia");jb[1].setText("Japan");jb[2].setText("Great Britain");jb[3].setText("Brunei");  
        }  
          
        for(int i=0,j=0;i<=90;i+=30,j++)  
            jb[j].setBounds(50,80+i,600,20);  
    
    }catch(NullPointerException ex){
            System.out.println(count); //testing what's going on
}
    }
}




    
