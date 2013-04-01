/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dht;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tcc10a
 */
public class DHTNode {
    private static String self;
    private static String nextNode;
    private static int port;
    private static int keyVal;
    private ServerSocket accept;
    private Socket next;
    
    public DHTNode(int port) {
        DHTNode.port = port;
        Properties prop = new Properties();
        try {
            self = java.net.InetAddress.getLocalHost().getHostName();
            prop.load(DHTNode.class.getClassLoader().getResourceAsStream("/home/dht/props/config.properties"));
            nextNode = prop.getProperty("NEXT");
            keyVal = Integer.parseInt(prop.getProperty("KEY"));
            accept = new ServerSocket(DHTNode.port);
            next = new Socket(nextNode,DHTNode.port);
        } catch (UnknownHostException ex) {
            Logger.getLogger(DHTNode.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Problem getting hostname.");
            System.exit(1);
        } catch (IOException ex) {
            Logger.getLogger(DHTNode.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Problem opening properties file.");
            System.exit(1);
        }
    }
    
    public boolean listen() throws IOException {
        while(true) {
            Socket connectionSocket = accept.accept();
            BufferedReader inFromClient =
               new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            String clientMessage = inFromClient.readLine();
            System.out.println("Received: " + clientMessage);
            if (clientMessage.toLowerCase().equals("shutdown")) {
                connectionSocket.close();
                break;
            } else if (clientMessage.toLowerCase().equals("keyval")) {
                outToClient.writeBytes(Integer.toString(keyVal));
                break;
            } else if (clientMessage.substring(0, 7).equalsIgnoreCase("article")) {
                String request = clientMessage.substring(8);
                
            }
            
        }
        return true;
    }
}
