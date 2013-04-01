/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dht;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tcc10a
 */
public class DHT {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        String job = args[0];
        boolean server = job.equals("server");
        if (args.length < 2) {
            System.err.println("Usage: <server> <port> \n      | <client> <host> <port> ");
            System.exit(1);
        } else if (!server && args.length < 3) {
            System.err.println("Usage: <server> <port> \n      | <client> <host> <port> ");
            System.exit(1);
        }
        
        if (server) {
            DHTNode node = new DHTNode(Integer.parseInt(args[0]));
            try {
                if (node.listen()) {
                    System.out.println("Shutdown command recieved");
                }
            } catch (IOException ex) {
                Logger.getLogger(DHT.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Socket exception detected");
                System.exit(1);
            }
        } else {
            
        }
    }
}
