package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jdom2.JDOMException;
import server.Handler;

public class Server {
    
    private static ServerSocket server;
    


    public static void run() throws JDOMException {

        ExecutorService executor = Executors.newFixedThreadPool(30); //max 30 threads/clients

        //ServerSocket server = null;
        try {

            server = new ServerSocket(8080);
            System.out.println("Server started!");

            while (true) {

                try {
                    Socket client = server.accept();

                    executor.execute(new Handler(client));

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

}
