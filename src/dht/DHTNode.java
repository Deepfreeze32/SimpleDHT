package dht;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a node for the DHT. It accepts some commands, and responds accordingly.
 * If the file requested hashes to a value greater than this node's key value, it 
 * returns the IP address of the next server in the DHT. If it hashes to the correct
 * value but the file does not exist, it says that file is not in the DHT or has 
 * been removed. If it does exist, it returns the text of the file.
 * @author Deepfreeze32
 */
public class DHTNode extends Thread {

    private static String self;
    private static String nextNode;
    private static int keyVal;
    private static Properties keylist;
    public static final int PORT_NUMBER = 1138;
    protected Socket socket;

    public static void main(String[] args) {
        ServerSocket server = null;
        try {
            server = new ServerSocket(PORT_NUMBER);
            while (true) {
                DHTNode forked = new DHTNode(server.accept());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Unable to start server or accept connections");
            System.exit(1);
        } finally {
            try {
                server.close();
            } catch (IOException ex) {
                // not much can be done: log the error
                // exits since this is the end of main
                ex.printStackTrace();
            }
        }
    }

    private DHTNode(Socket socket) throws IOException {
        this.socket = socket;
        Properties prop = new Properties();
        keylist = new Properties();
        prop.load(new FileInputStream("props/config.properties"));
        keylist.load(new FileInputStream("props/keylist.properties"));
        try {
            self = java.net.InetAddress.getLocalHost().getHostName();
            nextNode = prop.getProperty("NEXT");
            System.out.println(prop.getProperty("KEYVAL"));
            keyVal = Integer.parseInt(prop.getProperty("KEYVAL"));
        } catch (UnknownHostException ex) {
            Logger.getLogger(DHTNode.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Problem getting hostname.");
            System.exit(1);
        }
        //fork the process
        start();
    }

    // the server services client requests in the run method
    @Override
    public void run() {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String clientMessage;

            while ((clientMessage = in.readLine()) != null) {
                System.out.println("Received: " + clientMessage);
                if (clientMessage.contains("shutdown")) {
                    out.println("goodbye");
                    System.exit(0);
                    break;
		//They want to know our key.	
                } else if (clientMessage.toLowerCase().equals("keyval")) {
                    out.println(keyVal);
		//Retrieve the article.
                } else if (clientMessage.contains("article")) {
                    String request = clientMessage.substring(8);
                    int req = Integer.parseInt(request);
                    int key = Integer.parseInt(keylist.getProperty("" + req));
                    System.out.println(key);
                    if (key > keyVal) {
                        System.out.println("Failed.");
                        out.println("FAIL: " + nextNode);
                    } else {
                        System.out.println("It's ours!");
                     
                        //Get file info
                        String fname = "const/" + key + ".txt";
                        System.out.println(fname);
                        File f = new File(fname);
                        if (f.exists()) {
                            FileInputStream fis = new FileInputStream(fname);
                            int x = 0;
                            while (true) {
                                x = fis.read();
                                if (x == -1) {
                                    break;
                                }
                                out.write(x);
                            }
                        } else {
                            out.println("File has not been inserted or is on a different node that has left the swarm.");
                        }
                        out.close();
                        break;
                    }
		//Get the successor node. 
                } else if (clientMessage.contains("successor")) {
                    out.println(nextNode);
		//Get the key of the article number
                } else if (clientMessage.contains("artkey")) {
                    String request = clientMessage.substring(7);
                    System.out.println(request);
                    int req = Integer.parseInt(request);
                    int key = Integer.parseInt(keylist.getProperty("" + req));
                    out.println(key);
		//Insert an article.
                } else if (clientMessage.contains("insert")) {
                    String request = clientMessage.substring(7);
                    int req = Integer.parseInt(request);
                    int key = Integer.parseInt(keylist.getProperty("" + req));
                    System.out.println(key);
                    if (key > keyVal) {
                        System.out.println("Failed.");
                        out.println("FAIL: " + nextNode);
                    } else {
                        System.out.println("It's ours!");
                        out.println(key);
                        File file = new File("const/" + key + ".txt");

                        // if file doesnt exists, then create it
                        if (!file.exists()) {
                            file.createNewFile();
                        }

                        FileOutputStream fos = new FileOutputStream(file, false);
                        int x = 0;
                        while (true) {
                            x = in.read();
                            if (x == -1) {
                                break;
                            }
                            fos.write(x);
                        }
                        fos.close();
                    }
                    in.close();
                    break;
                } else if (clientMessage.contains("farticle")) {
                    String request = clientMessage.substring(9);
                    int req = Integer.parseInt(request);
                    int key = Integer.parseInt(keylist.getProperty("" + req));
                    System.out.println(key);

                    String fname = "const/" + key + ".txt";
                    System.out.println(fname);
                    File f = new File(fname);
                    if (f.exists()) {
                        FileInputStream fis = new FileInputStream(fname);
                        int x = 0;
                        while (true) {
                            x = fis.read();
                            if (x == -1) {
                                break;
                            }
                            out.write(x);
                        }
                    } else {
                        out.println("File has not been inserted or is on a different node that has left the swarm.");
                    }
                    out.close();
                    break;
                    //continue;

                } else if (clientMessage.contains("finsert")) {
                    String request = clientMessage.substring(8);
                    int req = Integer.parseInt(request);
                    int key = Integer.parseInt(keylist.getProperty("" + req));

                    out.println(key);
                    File file = new File("const/" + key + ".txt");

                    // if file doesnt exists, then create it
                    if (!file.exists()) {
                        file.createNewFile();
                    }

                    FileOutputStream fos = new FileOutputStream(file, false);
                    int x = 0;
                    while (true) {
                        x = in.read();
                        if (x == -1) {
                            break;
                        }
                        fos.write(x);
                    }
                    fos.close();
                    in.close();
                    break;
                } else {
                    out.println("Unrecognized command.");
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Unable to get streams from client");
        } finally {
            try {
                socket.close();
            } catch (IOException ex) {
                // not much can be done: log the error
                ex.printStackTrace();
            }
        }
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
