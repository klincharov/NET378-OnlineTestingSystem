package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import server.Handler;

/**
 * The multi-threaded part of the Client-Server online testing system.
 *
 * Uses a helper class Handler to perform the trivial tasks.
 *
 * @author Georgi Klincharov F45686
 */
public class Server {

    private static ServerSocket server;


/**
 * Public run method used for instantiating the server.
 *
 */
    public void run(){


        ExecutorService executor = Executors.newFixedThreadPool(30); //max 30 threads/clients

        try {

            server = new ServerSocket(8082);
            System.out.println("Server started!");

            while (true) {

                try {
                    Socket client = server.accept();

                    // executes submitted Runnable tasks
                    executor.execute(new Handler(client));

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                finally {
                   //executor.shutdownNow();
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

}
