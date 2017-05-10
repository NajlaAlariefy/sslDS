
package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
public class serveClient {
    private static final Logger LOGGER=Logger.getLogger(serveClient.class.getName());

     public static void serveSecureClient(SSLSocket client, Integer counter,int exchangeInterval) throws URISyntaxException, IOException {
           Handler consoleHandler=null;
            consoleHandler=new ConsoleHandler();
            LOGGER.addHandler(consoleHandler);
            consoleHandler.setLevel(Level.ALL);
            LOGGER.setLevel(Level.ALL);
        try (SSLSocket clientSocket = client) {
            JSONParser parser = new JSONParser();
                 InputStream sinput = clientSocket.getInputStream();
                OutputStream soutput = clientSocket.getOutputStream();
                
                OutputStreamWriter securewriter = new OutputStreamWriter(soutput);
                BufferedWriter output = new BufferedWriter(securewriter);
                InputStreamReader securereader = new InputStreamReader(sinput);
                BufferedReader input = new BufferedReader(securereader);
 Server.debug("INFO" ,"secure connection with client " + counter + " established.");
            //THIS WILL RUN UNTIL THE SOCKET IS CLOSED BY THE CLIENT'S SIDE
            while (true) {
                String response = null;
                if ((response=input.readLine()) != null) {
                    // Attempt to convert read data to JSON
                    JSONObject command = (JSONObject) parser.parse(response);
                   Server.debug("RECEIVE" ,"input received:" + command.toJSONString());
         
                    parseCommand pc = new parseCommand();
                    pc.parseSecureCommand(command, output,exchangeInterval);
                   

                }
            }

        } catch (IOException | ParseException e) {
          Server.debug("ERROR secureClient",   e.toString());
        }
    }

    public static void serveClient(Socket client, Integer counter,int exchangeInterval) throws URISyntaxException, IOException {
    	boolean debug=true;
            Handler consoleHandler=null;
            consoleHandler=new ConsoleHandler();
            LOGGER.addHandler(consoleHandler);
            consoleHandler.setLevel(Level.ALL);
            LOGGER.setLevel(Level.ALL);
        try (Socket clientSocket = client) {
            JSONParser parser = new JSONParser();
            DataInputStream input = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
            Server.debug("INFO" ,"connection with client " + counter + " established.");
            //THIS WILL RUN UNTIL THE SOCKET IS CLOSED BY THE CLIENT'S SIDE
            while (true) {
                if (input.available() > 0) {
                    // Attempt to convert read data to JSON
                    JSONObject command = (JSONObject) parser.parse(input.readUTF());
                    parseCommand pc = new parseCommand();
                    pc.parseCommand(command, output,exchangeInterval);
                   

                }
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

}
