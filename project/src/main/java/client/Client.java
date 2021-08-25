package client;

import java.io.IOException;
import java.net.Socket;

/**
 * Client with main method starting the participant
 *
 * @author afk, magnubau, williad
 */
public class Client {
    public static void main(String[] args) {
        Participant participant = new Participant(System.in);
        try {
            Socket socket = new Socket(participant.getAddress(), participant.getPort());
            participant.startConnection(socket);
        }
        catch (IOException ioe){
            ioe.printStackTrace();
        }
    }
}
