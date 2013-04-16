/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
 *
 * @author tcc10a
 */
public class DHTNode extends Thread {

    private static String self;
    private static String nextNode;
    //private static final int port = 1138;
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
            }
        }
    }

    private DHTNode(Socket socket) throws IOException {
        this.socket = socket;
        Properties prop = new Properties();
        keylist = new Properties();
        prop.load(new FileInputStream("/home/tcc10a/props/config.properties"));
        keylist.load(new FileInputStream("/home/tcc10a/props/keylist.properties"));
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
            boolean inserting = false;
            int insertingKey = 0;

            while ((clientMessage = in.readLine()) != null) {
                if (inserting) {
                    File file = new File("/home/tcc10a/const/" + insertingKey + ".txt");

                    // if file doesnt exists, then create it
                    if (!file.exists()) {
                        file.createNewFile();
                    }

                    FileOutputStream fos = new FileOutputStream(file);
                    int x = 0;
                    while (true) {
                        x = in.read();
                        if (x == -1) {
                            break;
                        }
                        fos.write(x);
                    }
                    fos.close();

                    inserting = false;
                    insertingKey = 0;
                    continue;
                }
                System.out.println("Received: " + clientMessage);
                if (clientMessage.contains("shutdown")) {
                    out.println("goodbye");
                    System.exit(0);
                    break;
                } else if (clientMessage.toLowerCase().equals("keyval")) {
                    out.println(keyVal);
                    //break;
                } else if (clientMessage.contains("article")) {
                    String request = clientMessage.substring(8);
                    //System.out.println(request);
                    int req = Integer.parseInt(request);
                    //System.out.println(req);
                    int key = Integer.parseInt(keylist.getProperty("" + req));
                    System.out.println(key);
                    if (key > keyVal) {
                        System.out.println("Failed.");
                        out.println("FAIL: " + nextNode);
                        //continue;
                    } else {
                        System.out.println("It's ours!");
                        //out.println(key);
                        //Get file info...somehow
                        //BufferedReader br = new BufferedReader(new FileReader("/home/dht/const/" + key + ".txt"));
                        //String file = br.readLine();
                        String fname = "/home/tcc10a/const/" + key + ".txt";
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
                    }
                } else if (clientMessage.contains("successor")) {
                    out.println(nextNode);
                } else if (clientMessage.contains("artkey")) {
                    String request = clientMessage.substring(7);
                    //System.out.println(request);
                    int req = Integer.parseInt(request);
                    //System.out.println(req);
                    int key = Integer.parseInt(keylist.getProperty("" + req));
                    out.println(key);
                } else if (clientMessage.contains("insert")) {
                    String request = clientMessage.substring(8);
                    //System.out.println(request);
                    int req = Integer.parseInt(request);
                    //System.out.println(req);
                    int key = Integer.parseInt(keylist.getProperty("" + req));
                    System.out.println(key);
                    if (key > keyVal) {
                        System.out.println("Failed.");
                        out.println("FAIL: " + nextNode);
                        //continue;
                    } else {
                        System.out.println("It's ours!");
                        out.println(key);
                        File f = new File("/home/tcc10a/const/"+key+".txt");
                        if (!f.exists()) {
                            insertingKey = key;
                            inserting = true;
                            //continue;
                        }
                    }
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
