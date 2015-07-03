
package Server;

import javax.swing.JFrame;
import org.jdom2.JDOMException;


public class ServerTest {
    public static void main(String[] args) throws JDOMException{
        Server serv = new Server();
        serv.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        serv.startRunning();
        
        
    }
}
