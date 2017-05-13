package server;
import Utilities.Resource;
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
    public static int sport = 3781;
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    public static String secret = randomString();
    public static ArrayList serverResources = new ArrayList();
    public static ArrayList serverRecords = new ArrayList();
    public static boolean debug = true;
    private static int counter = 0;    // identifies the user number connected
    static int exchangeInterval = 60000;     // a minute between each server exchange
    static int connectionIntervalLimit = 1000;    // a second between each connection

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
        timer.schedule(new serverExchanges(), 0, exchangeInterval);

        // OPEN SERVER FOR CONNECTIONS  
        //SECURE
         
                //Specify the keystore details (this can be specified as VM arguments as well)
		//the keystore file contains an application's own certificate and private key
		System.setProperty("javax.net.ssl.keyStore","/Users/najla/Desktop/EZshareP1/bin/keystore.jks");
		//Password to access the private key from the keystore file
		System.setProperty("javax.net.ssl.keyStorePassword","pass123");

		// Enable debugging to view the handshake and communication which happens between the SSLClient and the SSLServer
		//System.setProperty("javax.net.debug","all");
		 
        SSLServerSocketFactory sslsocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
       try( SSLServerSocket sslserversocket = (SSLServerSocket) sslsocketfactory.createServerSocket(sport);) {
           
        
        // NON-SECURE   
        ServerSocketFactory factory = ServerSocketFactory.getDefault();
       // try (ServerSocket server = factory.createServerSocket(port)) {
            debug("INFO", "starting the EZShare Server");
            debug("INFO", "using advertised hostname: " + host);
            debug("INFO", "bound to port: " + port);
            debug("INFO", "bound to SECURE port ONLY: " + sport);
            debug("INFO", "using secret: " + secret);
            debug("INFO", "interval exchange started ");

            //WHILE TRUE, WAIT FOR ANY CLIENT          
            while (true) {

                if (counter >= 1) {
                    TimeUnit.SECONDS.sleep(1);
                }
                counter++;
                serveClient sc = new serveClient();
                //SECURE
                SSLSocket sslclient = (SSLSocket) sslserversocket.accept();
                //NON-SECURE
               // Socket client = server.accept();
                
                debug("INFO", "client" + counter + " requesting connection");

                //START A NEW THREAD FOR CONNECTION
                Thread t = new Thread(() -> {
                    try {
                     //   sc.serveClient(client, counter, exchangeInterval);
                        sc.serveSecureClient(sslclient, counter, exchangeInterval);
                    } catch (URISyntaxException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.ALL, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
                t.start();
            }

        } catch (Exception e) {
            debug("ERROR",e.toString());
        }

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
