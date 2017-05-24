package server;

import Utilities.Resource;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import jdk.internal.dynalink.beans.StaticClass;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.apache.commons.cli.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/*

we want to check the connection if it's secure or not in the exchange command. It just breaks out if it times out. 
After that we need to exchange/query/interval
Then we need to set the certificate whenever we receiver it

That's all

----
we were trying to test exchange
and we noticed that we removed the server insecure port and we have to add it
also, client is not receiving the exchange succces/ message

yours,
past Najla

*/

/*
1- declare variables
2- parse command line
3- open server for connections  
4- while there is a client 
5- initiate client server 



6- while true, take in command and arguments                   -------- serveClient.JAVA
7- parse the command                                            --------- parseCommand.JAVA
8- process commands (from serverCommands) and return response ---------serverCommands.JAVA
9- still take in any commands while true                        ------- serveClient.JAVA

 */
public class Server {

    private static String randomString() {
        String SALTCHARS = "abcdefghijklmnopqrstuvwxyz1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 30) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }

    // VARIABLE DECLARATION
    public static String host = "localhost";
    public static int port = 8000;
    public static int sport = 8888;
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    public static String secret = randomString();
    public static ArrayList serverResources = new ArrayList();
    public static ArrayList serverRecords = new ArrayList();
    public static ArrayList secureServerRecords = new ArrayList();
    public static boolean debug = true;
    private static int counter = 0;    // identifies the user number connected
    static int exchangeInterval = 60000;     // a minute between each server exchange
    static int connectionIntervalLimit = 10000;    // a second between each connection

    public static void main(String[] args) throws org.apache.commons.cli.ParseException, InterruptedException, IOException {

        //CLI PARSING
        Options options = new Options();
        options.addOption("advertisedhostname", true, "advertised hostname");
        options.addOption("connectionintervallimit", true, "connection interval limit in seconds");
        options.addOption("exchangeinterval", true, "exchange interval in seconds");
        options.addOption("port", true, "server port, an integer");
        options.addOption("sport", true, "secure server port, an integer");
        options.addOption("secret", true, "secret");
        options.addOption("debug", true, "print debug information");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        cmd = parser.parse(options, args);
        if (cmd.hasOption("advertisedhostname")) {
            host = cmd.getOptionValue("advertisedhostname");
        }
        if (cmd.hasOption("connectionintervallimit")) {
            connectionIntervalLimit = (int) Double.parseDouble(cmd.getOptionValue("connectionintervallimit")) * 1000;
        }
        if (cmd.hasOption("exchangeinterval")) {
            exchangeInterval = (int) Double.parseDouble(cmd.getOptionValue("exchangeinterval")) * 1000;
        }
        if (cmd.hasOption("port")) {
            port = Integer.parseInt(cmd.getOptionValue("port"));
        }
        if (cmd.hasOption("sport")) {
            sport = Integer.parseInt(cmd.getOptionValue("port"));
        }
        if (cmd.hasOption("secret")) {
            secret = cmd.getOptionValue("secret");
        }
        if (cmd.hasOption("debug")) {
            debug = Boolean.parseBoolean(cmd.getOptionValue("debug"));
        }
        //SERVER INTERACTIONS BY EXCHANGE INITIATED
        class serverExchanges extends TimerTask {

            public void run() {
                serverInteractions si = new serverInteractions();
                try {
                    si.exchange();
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        Timer timer = new Timer();
       // timer.schedule(new serverExchanges(), 0, exchangeInterval);

        
        
        
        
        
        
        
        
        
        
        // OPEN SERVER FOR CONNECTIONS 
        debug("INFO", "starting the EZShare Server");
        debug("INFO", "using advertised hostname: " + host);
        debug("INFO", "bound to port: " + port);
        debug("INFO", "bound to SECURE port ONLY: " + sport);
        debug("INFO", "using secret: " + secret);
        debug("INFO", "interval exchange started ");
        
        
        
        
        //SECURE
        //Specify the keystore details (this can be specified as VM arguments as well)
        //the keystore file contains an application's own certificate and private key
        System.setProperty("javax.net.ssl.keyStore", "/home/alisha/sslDS/keystore.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "server123");
        System.setProperty("javax.net.ssl.trustStore", "truststore.jks");
        SSLServerSocketFactory sslsocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        SSLServerSocket sslserversocket = (SSLServerSocket) sslsocketfactory.createServerSocket(sport);
        
        

        // NON-SECURE   
        ServerSocketFactory factory = ServerSocketFactory.getDefault();
        ServerSocket server = factory.createServerSocket(port);


        //WHILE TRUE, WAIT FOR ANY CLIENT          
        while (true) {

            if (counter >= 1) {
                TimeUnit.SECONDS.sleep(1);
            }
            counter++;
            serveClient sc = new serveClient();
            
            
            //SECURE
            SSLSocket sslsocket = (SSLSocket) sslserversocket.accept();

            Thread ts = new Thread(() -> {
                try {
                    //   sc.serveClient(client, counter, exchangeInterval);
                    sc.serveSecureClient(sslsocket, counter, exchangeInterval);
                } catch (URISyntaxException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            ts.start();
            
            
            
            //NON-SECURE
            Socket client = server.accept();
             Thread t = new Thread(() -> {
                try {
                       sc.serveClient(client, counter, exchangeInterval);
                  
                } catch (URISyntaxException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            t.start();
            debug("INFO", "client" + counter + " requesting connection");
         
        }

        /* } catch (Exception e) {
            debug("ERROR",e.toString());
        }
         */
    }

    static void debug(String type, String message) {
        if (debug) {
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
            System.out.println(time + " - [" + type + "] - " + message);
        }
    }
}

/*
Communication pattern: 
 
client: REQUEST command
server: REPLY command

(if fetch or query, REPLY is long so listen many times until end)

(if query relay is true:

server1: REQUEST command (query)
server2: REPLY command (query list)

)  
 */
