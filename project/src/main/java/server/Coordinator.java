package server;

import java.util.ArrayList;
import java.util.Date;


/**
 * This class contains methods that handles two-phase-commit
 * transactions between participants
 *
 * @author afk, magnubau, williad
 */
public class Coordinator {

    private ArrayList<ClientHandler> participants = new ArrayList<>();
    private String tractionStatement;
    private int timeout = 15;
    private ArrayList<ClientHandler> respondList = new ArrayList<>(); //A list containing the clients who have responded


    /**
     * constructor that sets the participants list
     *
     * @param participants      An ArrayList of ClientHandler objects
     */
    public Coordinator(ArrayList<ClientHandler> participants){
        this.participants = participants;
    }


    /**
     * This method checks if all participants are ready to commit a new transaction
     *
     * @param query     The description of the transaction
     * @return          true if all participants are ready to commit. else false
     */
    private boolean initTransaction(String query){
        respondList.clear();
        this.tractionStatement = query;
        System.out.println("Initiating transaction:\n" + this.tractionStatement + "\n");
        /*Ask all participants if they are ready to commit*/
        messageAll("NEW TRANSACTION--" + this.tractionStatement + "--READY TO COMMIT?");
        long start = System.currentTimeMillis();
        long timer = 0L;
        int resCount = 0;
        String answer = "";
        /*Wait for all participants to respond*/
        while(resCount < participants.size()){
            for(ClientHandler party: participants){
                answer = party.readFromParticipant();
                if(answer.equals("YES")){
                    resCount++;
                    System.out.println("participant nr. " + party.getId() + " is ready to commit\n");
                    respondList.add(party);
                /*Rollback if a participants answers NO*/
                }else if(answer.equals("NO")){
                    respondList.add(party);
                    System.out.println("Transaction aborted by participant nr. " + party.getId() + "\n");
                    messageAll("TRANSACTION--" + this.tractionStatement + "--ROLLBACK");
                    waitForRollbacked();
                    return false;
                }
            }
            timer = (new Date().getTime() - start) / 1000;
            /*Rollback if participants take too long to respond*/
            if(timer >= this.timeout){
                System.out.println("Transaction aborted due to timeout\n");
                /*Participants that do not respond are removed and shut down*/
                if (respondList.isEmpty()){
                    messageAll("TRANSACTION--" + this.tractionStatement + "--SHUTDOWN");
                    for(ClientHandler party: participants){
                        party.shutdown();
                    }
                    participants.clear();
                }
                for (int i = 0; i < participants.size(); i++){
                    if (!respondList.contains(participants.get(i))){
                        participants.get(i).sendToParticipant("TRANSACTION--" + this.tractionStatement + "--SHUTDOWN");
                        System.out.println(participants.get(i) + " has been removed due to timeout\n");
                        participants.get(i).shutdown();
                        participants.remove(participants.get(i));
                    }
                }
               messageAll("TRANSACTION--" + this.tractionStatement + "--ROLLBACK");
               waitForRollbacked();
               return false;
            }
        }
        System.out.println("transcation successfully initiated\n");
        return true;
    }


    /**
     * This method instructs all participants to commit, and checks if they
     * are all successful
     * @return      true if all participants commit successfully. else false
     */
    private boolean commitTransaction(){
        System.out.println("Commiting transaction\n");
        /*Ask all participants to commit*/
        messageAll("TRANSACTION--" + this.tractionStatement + "--COMMIT");
        long start = System.currentTimeMillis();
        long timer = 0L;
        int resCount = 0;
        String answer = "";
        /*Wait for all participants to respond*/
        while(resCount < participants.size()){
            for(ClientHandler party: participants){
                answer = party.readFromParticipant();
                if(answer.equals("COMMITTED")){
                    resCount++;
                    System.out.println("participant nr. " + party.getId() + " has commited\n");
                }
            }
        }
        System.out.println("Transaction commited\n");
        messageAll("TRANSACTION--" + this.tractionStatement + "--SUCCESS");
        return true;
    }


    /**
     * This method sends a message to all participants
     *
     * @param query     the message to be sent
     */
    private void messageAll(String query){
        String message = query;
        for(ClientHandler party: participants){
            party.sendToParticipant(message);
        }
    }


    /**
     * This method waits for all participants to rollback.
     * @return          true if all participants respond with ROLLBACKED. Else false.
     */
    private boolean waitForRollbacked(){
        long start = System.currentTimeMillis();
        long timer = 0L;
        int resCount = 0;
        String answer = "";
        /*Wait for all participants to respond*/
        while(resCount < participants.size()){
            for(ClientHandler party: participants){
                answer = party.readFromParticipant();
                if(answer.equals("ROLLBACKED")) {
                    resCount++;
                    System.out.println("participant nr. " + party.getId() + " rolled back\n");
                }
            }
            timer = (new Date().getTime() - start) / 1000;
            /*Stop waiting if participants take too long to respond*/
            if(timer >= this.timeout){
                System.out.println("Stopped waiting for rollback due to timeout\n");
                return false;
            }
        }
        messageAll("TRANSACTION--" + this.tractionStatement + "--ROLLBACKED");
        return true;
    }


    /**
     * This method starts a loop that handles incoming transactions
     */
    public void start(){
        messageAll("All participants connected");
        String query = "";
        boolean waiting = true;
        boolean run = true;
        while(run){
            System.out.println("Waiting for transaction request\n");
            while(waiting){
                if(participants.size() == 0){
                    run = false;
                    waiting = false;
                }
                for(ClientHandler party: participants){
                    query = party.readFromParticipant();
                    /*Coordinator recieves valid request*/
                    if(!query.equals("") && query.contains("--")){
                        query = query.split("--")[1];
                        System.out.println("Got request: " + query);
                        waiting = false;
                        break;
                    /*Coordinator recieves invalid request*/
                    }else if(!query.equals("")){
                        System.err.println("Recieved invalid transaction request from Client:\n");
                        System.out.println("query: " + query + "\n");
                    }
                }
            }
            /*Executes a transaction*/
            if (run){
                if(initTransaction(query)){
                    respondList.clear();
                    commitTransaction();
                }
            }
            waiting = true;
        }
        System.out.println("Shutting down\n");
    }
}
