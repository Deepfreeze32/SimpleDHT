/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dht;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tcc10a
 */
public class DHTClient {

    private static final int port = 1138;
    private static String lastRequest;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here

        if (args.length != 1) {
            System.err.println("Usage: <host>");
            System.exit(1);
        }
        
        lastRequest = null;
        String serverHostname = args[0];

        //System.out.println("Attemping to connect to host " + serverHostname + " on port " + port);

        while (connectLoop(serverHostname)) {
            //Nothing to do here. 
        }
    }

    public static boolean connectLoop(String serverHostname) {
        while (true) {
            System.out.println("Attemping to connect to host " + serverHostname + " on port " + port);
            String next = null;
            try {
                next = connect(serverHostname);
            } catch (IOException ex) {
                Logger.getLogger(DHTClient.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (next == null) {
                break;
            } else if (next.equals("SIGTERM")) {
                return false;
            } 
            serverHostname = next;
        }
        return true;
    }
    
    public static String connect(String serverHostname) throws IOException {
        String nextHost = null;
        Socket echoSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        try {
            echoSocket = new Socket(serverHostname, port);
            out = new PrintWriter(echoSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + serverHostname);
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Couldn't get I/O for " + "the connection to: " + serverHostname);
            System.exit(1);
        }

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String userInput = null;

        System.out.println("Type Message (\"quit\" to quit, \"shutdown\" to shutdown both the client and the server)");
        while (lastRequest != null || ((userInput = stdIn.readLine()) != null)) {
            //If there's a buffered request
            if (lastRequest != null) {
                //execute the request
                userInput = lastRequest;
                //reset the buffer
                lastRequest = null;
            }
            // send message to server
            out.println(userInput);

            // end loop
            if (userInput.equals("quit") || userInput.equals("shutdown")) {
                return "SIGTERM";
            }

            String output = in.readLine();
            if (output == null) {
                //TODO Buffer request 
                //lastRequest = userInput;
                System.out.println("Null response recived. Terminating");
                break;
            }
            if (userInput.contains("article")) {
                lastRequest = userInput;
                if (output.contains("FAIL:")) {
                    String node = output.substring(6);
                    System.out.println("Error: Try node: " + node);
                    nextHost = node;
                    break;
                } else {
                    String request = userInput.substring(8);
                    //System.out.println(request);
                    int req = Integer.parseInt(request);
                    FileOutputStream fos = new FileOutputStream("article" + req + ".txt");
                    int x = 0;
                    while (true) {
                        x = in.read();
                        if (x == -1) {
                            break;
                        }
                        fos.write(x);
                    }
                    fos.close();
                    System.out.println("Wrote to file article" + req + ".txt");
                    System.out.println("Contents of file:\n"+readFile("article" + req + ".txt"));

                    nextHost = null;
                }
            } else {
                System.out.println("Response: " + output);
            }
        }

        out.close();
        in.close();
        //stdIn.close();
        echoSocket.close();
        return nextHost;
    }

    private static String readFile(String path) throws IOException {
        FileInputStream stream = new FileInputStream(new File(path));
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            /* Instead of using default, pass in a decoder. */
            return Charset.defaultCharset().decode(bb).toString();
        } finally {
            stream.close();
        }
    }
}
