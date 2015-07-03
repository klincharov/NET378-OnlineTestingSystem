
package client;
import java.io.IOException;
import javax.swing.JFrame;

public class ClientTest {
    public static void main(String[] args) throws ClassNotFoundException, IOException{
        Client client;
        client = new Client("127.0.0.1");
        client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.startRunning();
        
    }
    
}
