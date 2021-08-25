package server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Class that handles the communication with one participant
 *
 * @author afk, magnubau, williad
 */
public class ClientHandler{
    private Socket connection = null;
    private int id;
    private BufferedReader reader;
    private PrintWriter writer;
    private InputStreamReader readConnection;


    /**
     * Takes in a socket connection and a client id as argument
     * @param connection of type socket
     * @param id client id
     *
     * @author afk, magnubau, williad
     */
    public ClientHandler(Socket connection, int id){
        this.connection = connection;
        this.id = id;

        try {
            readConnection = new InputStreamReader(connection.getInputStream());
            reader = new BufferedReader(readConnection);
            writer = new PrintWriter(connection.getOutputStream(), true);
        }
        catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }


    /**
     * Receves a message from cordinator and sends it to the participant
     * This method uses the PrintWriter function println
     * @param message the message to be sent to the participant
     * @return true if message is sent, false otherwise
     */
    public boolean sendToParticipant(String message) {
        try {
            writer.println(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Reads the response from the participant and returns it to the coordinator
     * Uses the method readLine from BufferedReader to read the response
     * @return the response
     */
    public String readFromParticipant(){
        String line = "";
        try {
            if (this.reader.ready()) {
                line = reader.readLine();
            }
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
        return line;
    }


    /**
     * This method closes the connection, writer and reader
     */
    public void shutdown(){
        try{
            connection.close();
            reader.close();
            writer.close();
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    public String toString(){
        return "ClientHandler for participant with id " + this.getId();
    }

    public void setReader(BufferedReader reader) {
        this.reader = reader;
    }

    public void setWriter(PrintWriter writer) {
        this.writer = writer;
    }

}
