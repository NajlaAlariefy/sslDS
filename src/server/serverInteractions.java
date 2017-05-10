
package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
 
public class serverInteractions {

public void exchange() throws IOException {
        //IF the server contains itself in the serverrecords list, then remove it
            JSONObject serverTraverser = new JSONObject();
            for (int i = 0; i < Server.serverRecords.size(); i++) {
                serverTraverser = (JSONObject) Server.serverRecords.get(i);
                if (serverTraverser.get("hostname").equals(Server.host) )
                {
                    if(serverTraverser.get("port").equals(Server.port))
                    { Server.serverRecords.remove(i);} 
                    
                }
            }
            
            
            
        if (Server.serverRecords.isEmpty()) {
            Server.debug("INFO", "server exchange initiated: empty server record list.");
        } else {
            
            
            
            //Pick a random server to connect from the list
            Server.debug("INFO", "server exchange initiated: randomly selecting a server");
            Random r = new Random();
            JSONObject randomServer = new JSONObject();
            int size = Integer.valueOf(Server.serverRecords.size());
            int index = Integer.valueOf(r.nextInt(size));
            randomServer = (JSONObject) Server.serverRecords.get(index);
            String connect_host = randomServer.get("hostname").toString();
            int connect_port = ((Long) randomServer.get("port")).intValue();

            // Create connection with the selected server from the serverlist
            try {
                Socket socket = null;
                socket = new Socket(connect_host, connect_port);
                String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
                System.out.println(time + " - [INFO] - exchange with " + connect_host + ":" + connect_port + " is successful.");
                
                // Sending the list to the randomly selected server
                // Receiving ackowledgement (success)/(error)                
                JSONObject listToRandomServer = new JSONObject();
                listToRandomServer.put("command", "EXCHANGE");
                listToRandomServer.put("serverList", Server.serverRecords);
                DataOutputStream serverOutput = new DataOutputStream(socket.getOutputStream());
                serverOutput.writeUTF(listToRandomServer.toJSONString());
                Server.debug("SEND EXCHANGE", listToRandomServer.toJSONString());
                DataInputStream serverInput = new DataInputStream(socket.getInputStream());
                String message =  serverInput.readUTF();
                JSONParser parser = new JSONParser();
                JSONObject JSONresponse = (JSONObject) parser.parse(message);
                Server.debug("RECEIVE EXCHANGE",  JSONresponse.toJSONString());
                
                
                // Display distinct servers in the serverRecords
                Set<String> setWithUniqueValues = new HashSet<>(Server.serverRecords);
                ArrayList<String> listWithUniqueValues = new ArrayList<>(setWithUniqueValues);
                Server.serverRecords = listWithUniqueValues;
                socket.close();
            } catch (Exception e) {
                // If the connection with the random server is not established
                Server.debug("INFO","connection with server " + connect_host + ":" + connect_port + " was not successful. ");
                Server.debug("INFO","error message : "  + e);
               
                serverTraverser = new JSONObject();

                for (int i = 0; i < Server.serverRecords.size(); i++) {
                    serverTraverser = (JSONObject) Server.serverRecords.get(i);
                    if (serverTraverser.get("hostname").equals(connect_host) && serverTraverser.get("port").equals(connect_port)) {
                        Server.serverRecords.remove(i);
                Server.debug("INFO","server has been removed");
               
                    }
                }
            } finally {

            }
        }
    }
 
}
