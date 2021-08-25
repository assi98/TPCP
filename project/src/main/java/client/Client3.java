package client;

import java.io.IOException;
import java.net.Socket;

/**
 * Copy of Client.java
 *
 * @author afk, magnubau, williad
 */
public class Client3 {
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
