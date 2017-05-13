package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import static java.lang.Compiler.command;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.apache.commons.cli.*;

import Utilities.Resource;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import sun.security.krb5.internal.HostAddress;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import javax.print.DocFlavor.STRING;

public class Client {

    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    /*
    
    0 - setting global variable debug 
    
    1 - initialized arguments (hardcoded)
    
    2 - command line interface parsing
    
    3 - Host & Port validity check
    
    4 - Parsing all CLI
    
    5 - Request connection from server
    
    6 -  converting INPUT to JSON OBJECT using Resource class's function (inputToJSON)             
    
    7 - SEND COMMAND TO SERVER             
    
    8-  RECEIVING RESPONSE FROM ANY COMMAND INVOKED  (could be an error, like 'invalid command')
                
    9- Implementing client side for some commands (some commands don't need further operations, like remove/share/publish/exchange)
                        
    
     */
    public static boolean debug = true;

    @SuppressWarnings("empty-statement")
    public static void main(String[] args) throws ParseException, URISyntaxException, ParseException, org.apache.commons.cli.ParseException, IOException {

        /*
    
             1- initialized arguments (hardcoded)
        
         */
        String host = "localhost";
        int port = 3781;
        List<String> tags = Arrays.asList("");

        String commandName = "QUERY";
        String name = "";
        String description = "";
        String URI = "";
        String owner = "";
        String channel = "";
        String ezserver = "";
        String secret = "";
        String servers = "";
        boolean secure = true;
        boolean relay = true;
        System.setProperty("javax.net.ssl.trustStore", "clientKeyStore/myGreatName");
			
        /*
             2- CLI parsing
         */
        boolean isValid = false;
        Options options = new Options();
        options.addOption("channel", true, "channel");
        options.addOption("debug", "print debug information");
        options.addOption("description", true, "resource description");
        options.addOption("exchange", "exchange server list with server");
        options.addOption("fetch", "fetch resources from server");
        options.addOption("host", true, "server host, a domain name or IP address");
        options.addOption("name", true, "resource name");
        options.addOption("owner", true, "owner");
        options.addOption("port", true, "server port, an integer");
        options.addOption("publish", false, "publish resource on server");
        options.addOption("query", "query for resources from server");
        options.addOption("remove", "remove resource from server");
        options.addOption("secret", true, "secret");
        options.addOption("servers", true, "server list, host1:port1,host2:port2,...");
        options.addOption("share", "share resource on server");
        options.addOption("tags", true, "resource tags, tag1,tag2,tag3,...");
        options.addOption("uri", true, "resource URI");
        options.addOption("subscribe", "subscribe to the ");
        options.addOption("secure", "connect to the secure port");

        CommandLineParser clparser = new DefaultParser();
        CommandLine cmd = null;
        cmd = clparser.parse(options, args);

        /*
        
            3 - Host & Port validity check
        
         */
        if (cmd.hasOption("host") && cmd.hasOption("port")) {
            if (cmd.getOptionValue("host").equals(" ") || cmd.getOptionValue("port").equals(" ")) {
                debug("ERROR", "missing host/port information");

            } else {
                String port1 = cmd.getOptionValue("port");
                if (cmd.getOptionValue("port").matches("^-?\\d+$")) {

                    port = Integer.parseInt(port1);
                    isValid = true;
                    host = cmd.getOptionValue("host");

                } else {
                    isValid = false;
                }

            }

        } else {
            debug("ERROR", "invalid host/port information");
        }

        /*
        
            4 - Parsing all CLI
        
         */
        if (cmd.hasOption("publish")) {
            commandName = "PUBLISH";

        } else if (cmd.hasOption("remove")) {
            commandName = "REMOVE";

        } else if (cmd.hasOption("share")) {
            commandName = "SHARE";

        } else if (cmd.hasOption("query")) {
            commandName = "QUERY";

        } else if (cmd.hasOption("fetch")) {
            commandName = "FETCH";

        } else if (cmd.hasOption("exchange")) {
            commandName = "EXCHANGE";

        } else if (cmd.hasOption("subscribe")) {
            commandName = "SUBSCRIBE";
        }

        if (cmd.hasOption("servers")) {
            servers = cmd.getOptionValue("servers");
        }
        if (cmd.hasOption("uri")) {
            URI = cmd.getOptionValue("uri");
        }
        if (cmd.hasOption("owner")) {
            owner = (cmd.getOptionValue("owner").trim());
        }
        if (cmd.hasOption("channel")) {
            channel = (cmd.getOptionValue("channel").trim());
        }
        if (cmd.hasOption("name")) {
            name = (cmd.getOptionValue("name").trim());
        }
        if (cmd.hasOption("description")) {
            description = cmd.getOptionValue("description").trim();
        }
        if (cmd.hasOption("secret")) {
            secret = cmd.getOptionValue("secret");
        }
        if (cmd.hasOption("relay")) {
            relay = Boolean.valueOf(cmd.getOptionValue("relay"));
        }
        if (cmd.hasOption("debug")) {
            debug = Boolean.valueOf(cmd.getOptionValue("debug"));
        }
        if (cmd.hasOption("tags")) {
            String[] tags1 = cmd.getOptionValue("tags").split(",");
            for (int i = 0; i < tags1.length; i++) {
                tags.add(tags1[i].trim());
            }
        }
        if (cmd.hasOption("secure")) {
            secure = true;
        }

        //REMOVE LATER
        isValid = true;

        if (isValid) {

            if (secure) {
                SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                SSLSocket cs = (SSLSocket) sslsocketfactory.createSocket(host, port);
                InputStream sinput = cs.getInputStream();
                OutputStream soutput = cs.getOutputStream();
                
                OutputStreamWriter securewriter = new OutputStreamWriter(soutput);
                BufferedWriter bufferedwriter = new BufferedWriter(securewriter);
                InputStreamReader securereader = new InputStreamReader(sinput);
                BufferedReader bufferedreader = new BufferedReader(securereader);

                //  DataInputStream sinput = new DataInputStream(cs.getInputStream());
                // DataOutputStream soutput = new DataOutputStream(cs.getOutputStream());
                debug("INFO", "requesting a secure connection with server");
                debug("INFO", "connection with server is established");

                /*
                    6 -  converting INPUT to JSON OBJECT using Resource class's function (inputToJSON)
                 */
                JSONObject command = new JSONObject();
                Resource resource = new Resource();
                command = resource.inputToJSON(commandName, name, owner, description, channel, URI, tags, ezserver, secret, relay, servers);

                /*
                
                    7 - SEND COMMAND TO SERVER
                
                 */
                try {

                    bufferedwriter.write(command.toJSONString());
                    debug("SEND", command.toJSONString());
                } catch (Exception e) {
                    debug("ERROR", e.toString());
                }

                /*
                    
                    8-  RECEIVING RESPONSE FROM ANY COMMAND INVOKED  (could be an error, like 'invalid command')
                
                 */
                while (true) {
                    String string = null;
                    if ((string = bufferedreader.readLine()) != null) {
                        System.out.println("the input is available");
                        String response = string;
                        JSONParser parser = new JSONParser();
                        JSONObject JSONresponse = (JSONObject) parser.parse(response);
                        debug("RECEIVE", JSONresponse.toJSONString());

                        /*
                        
                        
                            9- DOING CLIENT SIDE CODE FOR SOME OF COMMANDS (some commands don't need further operations, like remove/share/publish/exchange)
                        
                        
                         */
                        clientCommands clientCommand = new clientCommands();
                        switch (commandName) {
                            case "FETCH":
                              //  clientCommand.fetch(JSONresponse, bufferedreader);
                                break;
                            case "QUERY":
                                clientCommand.securequery(JSONresponse, bufferedreader);
                                break;
                        }
                        break;
                    }

                }

            } else {

                try (Socket socket = new Socket(host, port);) {

                    /*
                    5 -  INITIATING CONNECTION REQUEST
                     */
                    DataInputStream input = new DataInputStream(socket.getInputStream());
                    DataOutputStream output = new DataOutputStream(socket.getOutputStream());

                    debug("INFO", "requesting connection with server");
                    debug("INFO", "connection with server is established");

                    /*
                    6 -  converting INPUT to JSON OBJECT using Resource class's function (inputToJSON)
                     */
                    JSONObject command = new JSONObject();
                    Resource resource = new Resource();
                    command = resource.inputToJSON(commandName, name, owner, description, channel, URI, tags, ezserver, secret, relay, servers);

                    /*
                
                    7 - SEND COMMAND TO SERVER
                
                     */
                    output.writeUTF(command.toJSONString());
                    debug("SEND", command.toJSONString());

                    /*
                    
                    8-  RECEIVING RESPONSE FROM ANY COMMAND INVOKED  (could be an error, like 'invalid command')
                
                     */
                    while (true) {

                        if (input.available() > 0) {
                            String response = input.readUTF();
                            JSONParser parser = new JSONParser();
                            JSONObject JSONresponse = (JSONObject) parser.parse(response);
                            debug("RECEIVE", JSONresponse.toJSONString());

                            /*
                        
                        
                            9- DOING CLIENT SIDE CODE FOR SOME OF COMMANDS (some commands don't need further operations, like remove/share/publish/exchange)
                        
                        
                             */
                            clientCommands clientCommand = new clientCommands();
                            switch (commandName) {
                                case "FETCH":
                                    clientCommand.fetch(JSONresponse, input);
                                    break;
                                case "QUERY":
                                    clientCommand.query(JSONresponse, input);
                                    break;
                            }
                            break;
                        }

                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException ex) {
                    System.out.println(ex);
                    Logger.getLogger(Client.class.getName()).log(Level.ALL, null, ex);
                } finally {
                    // socket.close();
                }
            }

        } else {
            debug("ERROR", "connection aborted");

        }

    }

    public static void debug(String type, String message) {

        if (debug) {
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
            System.out.println(time + " - [" + type + "] - " + message);
        }
    }

}
