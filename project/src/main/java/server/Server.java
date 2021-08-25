package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.*;


/**
 * This class handles connection requests from clients and creates
 * a new instance of ClientHandler for each new client, and adds it
 * a shared list
 *
 * @author afk, magnubau, williad
 */
public class Server {
    final static int PORTNR = 3000;
    /**
     * This main method goes in loops waiting for new connections to
     * @param args default arguments
     */
    public static void main(String[] args) {
        int id = 0;
        System.out.println("Server is running. Waiting for clients...");
        try {
            ServerSocket server = new ServerSocket(PORTNR);
            ArrayList<ClientHandler> participants = new ArrayList<>();
            boolean wait = true;
            while (wait) {

                ExecutorService executor = Executors.newCachedThreadPool();
                Callable<Object> waiter = new Callable<Object>() {
                    public Object call() throws IOException {
                        return server.accept();
                    }
                };
                Future<Object> promise = executor.submit(waiter);
                try {
                    Object res = promise.get(10, TimeUnit.SECONDS);
                    participants.add(new ClientHandler((Socket) res, id));
                    participants.get(participants.size() - 1).sendToParticipant("You are connected with id " + id);
                    System.out.println("Client connected with id: " + id + "\nNumber of clients: " + participants.size() + "\n");

                    /* ---------------- FOR DEBUG ------------- */
                    //if (participants.size() == 2) wait = false;
                    /* ---------------------------------------- */

                } catch (TimeoutException toe) {

                    System.out.println("Stopped waiting for clients");
                    wait = false;
                } catch (InterruptedException | ExecutionException ie) {
                    ie.printStackTrace();
                    wait = false;
                } finally {
                    promise.cancel(true);
                }
                id++;
            }
            for (ClientHandler i : participants) {
                System.out.println(i + " is connected,");
            }
            Coordinator coordinator = new Coordinator(participants);
            coordinator.start();
            for (ClientHandler party : participants) {
                party.shutdown();
            }
            server.close();
        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
}
