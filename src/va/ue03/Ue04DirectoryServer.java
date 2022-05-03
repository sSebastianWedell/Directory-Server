package va.ue03;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Ue04DirectoryServer extends Thread {
    private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[1024]; // Buffergroesse von 256 kb
    private FileWriter fw;
    public static final String JSONFILE = "server.json";
    
    public static void main(String[] args) throws IOException {
        // Ue04DirectoryServer-Server laeuft in einem eigenen Thread
        Ue04DirectoryServer es = new Ue04DirectoryServer();
        es.run();
    }
    
    /**
     * wir speichern die registrierten Clients in folgender Sturktur ab:
     * <SESSION_ID, SET VON CLIENTS> 
     * 
     * Damit wird sichergestellt, dass doppelte Client-Einträge nicht auftauchen.
     * Client sind dann doppelt, wenn sie gleichen Namen haben 
     * 
     * Beispiel: 
     * register "Client1", "value", "1" 
     * register "Client2", "value", "1"
     * register "Client3", "value", "1"
     * register "Client1", "value", "1" (existiertender Client versucht sich unter gleicher Session_id zu registrieren)
     * register "Client1", "value", "2"
     * 
     * erzeugt folgende Struktur:
     * <"1"; [Client1, Client2, Client3]>
     * <"2"; [Client1]>
     * 
     */
    private Map<String, Set<ClientData>> data = new HashMap<>();
    
    /**
     * Konstruktor, bindet den Ue04DirectoryServer-Server an Port 11111
     * @throws IOException 
     */
    public Ue04DirectoryServer() throws IOException {
        socket = new DatagramSocket(11111);
        // lade die Daten
        loadData();
    }

   

    public void run() {
        System.out.println("Start Server at 127.0.0.1");
        running = true;
        while (running) {
            buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }

            InetAddress address = packet.getAddress();
            int port = packet.getPort();

            packet = new DatagramPacket(buf, buf.length, address, port);

            // Empfangene Nachricht besteht aus der Data-Load vom empfangenen
            // Packet
            String received = new String(packet.getData(), 0,
                    packet.getLength());

            // Begrenze den empfangenen String auf die letzte geschweifte
            // Klammer (Buffer ist laenger als emfange Nachricht)
            received = received.substring(0, received.lastIndexOf("}") + 1);

            // Wenn "end" empfangen wird dann beende die Schleife und damit den
            // Thread
            if (received.equals("end")) {
                running = false;
                continue;
            } else {
                // Empfangener String wird ueber die sendResponse-Funktion
                // analysiert und Status zurueckgeschickt
                String returnMsg = sendResponse(received);
                packet.setData(returnMsg.getBytes());
                try {
                    socket.send(packet); // Zuruecksenden vom empfangenen Paket
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        socket.close();
    }

    /**
     * Speichere den Datenbestand (map) im json ab
     */
    private void saveData() {
        
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
            fw = new FileWriter(JSONFILE);
            fw.write(json.toString());
        } catch (JsonProcessingException exc) {
            exc.printStackTrace();
        } catch (IOException exc) {
            exc.printStackTrace();
        } 
        finally {
            try {
                fw.flush();
                fw.close();
            } catch (IOException exc) {
                exc.printStackTrace();
            }
        }
    }
    
    /**
     * Lade den Datenbestand (map) aus dem json
     */
    private void loadData() {
        JSONParser parser = new JSONParser();
        try {
            HashMap<String,Object> obj = (HashMap) parser.parse(new FileReader(JSONFILE));
            for (Entry<String, Object> map : obj.entrySet()) {
                Set<ClientData> set = new HashSet<>();
                JSONArray a = (JSONArray) map.getValue();
                for (Object object : a) {
                    JSONObject json = (JSONObject) object;
                    String name = (String) json.get("name");
                    String value = (String) json.get("value");
                    set.add(new ClientData(name, value));
                }
                data.put(map.getKey(), set);
            }
            
        } catch (FileNotFoundException exc) {
            // ist ok, falls am Anfang keine Datei da ist
        } catch (IOException exc) {
            exc.printStackTrace();
        } catch (ParseException exc) {
            // ist ok, am Anfang ist nichts da
        }
        
    }
    /**
     * Client sendet register <name> <value> <SID>
     * Server send Status zurueck 
     * OK
     * (alles gut: NAME, VALUE, SID sind vorhanden Client registriert sich zum
     * ersten Mal) 
     * 
     * ERROR (Client ist bereits registriert NAME, VALUE, SID nicht
     * vorhanden)
     * 
     * @param jo
     */
    private void handleRegister(JSONObject jo) {
        String name = (String) jo.getOrDefault("name", null);
        if (name == null) {
            jo.put("Status", "Error: name not set");
            return;
        }
        String value = (String) jo.getOrDefault("value", null);
        if (value == null) {
            jo.put("Status", "Error: value not set");
            return;
        }
        String SID = (String) jo.getOrDefault("SID", null);
        if (SID == null) {
            jo.put("Status", "Error: SID not set");
            return;
        }
        // neuer Client hat sich registriert:
        Set<ClientData> set = data.get(SID);
        ClientData clientData = new ClientData(name, value);
        if (set == null) {
            // niemand hat sich hier bis jetzt registriert
            Set<ClientData> clientDatas = new HashSet<>();
            clientDatas.add(clientData);
            data.put(SID, clientDatas);
        } else {
            // clients gibt es schon
            boolean add = set.add(clientData);
            if (!add) {
                // dieser client ist bereits registiert
                jo.put("Status", "Error: Client " + clientData.getName()
                        + " already registered");
                return;
            }
            data.put(SID, set);
        }
        // Die "saubere" Antwort soll reinen Status zurück liefern, ohne den Request vom Client 
        jo.remove("name");
        jo.remove("value");
        jo.remove("SID");
        jo.remove("Command");
        // client erfolgreich angemeldet (in die map hinzugefügt)
        jo.put("Status", "OK");
        // speichere die Änderungen im Json ab
        saveData();
    }

    /**
     * Client sendet unregister <name> <SID> 
     * Server send Status zurueck 
     * OK
     * (alles gut: NAME SID Client sind vorhanden ) 
     * 
     * ERROR (NAME SID Client sind
     * nicht vorhanden)
     * 
     * @param jo
     */
    private void handleUnregister(JSONObject jo) {
        String name = (String) jo.getOrDefault("name", null);
        if (name == null) {
            jo.put("Status", "Error: name not set");
            return;
        }
        String SID = (String) jo.getOrDefault("SID", null);
        if (SID == null) {
            jo.put("Status", "Error: SID not set");
            return;
        }
        Set<ClientData> set = data.get(SID);
        if (set == null) {
            // ungültige SID
            jo.put("Status", "Error: SID " + SID + " does not exist");
            return;
        }
        try {
            ClientData cd = set.stream().filter(e -> e.getName().equals(name))
                    .findFirst().get();
            set.remove(cd);
            // Falls dies der letzte Client an einer Session war, lösche den Eintrag komplett
            if (set.isEmpty()) {
                data.remove(SID);
            }
        } catch (NoSuchElementException e) {
            // Client ist in dieser Session nicht registriert
            jo.put("Status", "Error: Client " + name
                    + " does not exist for Session " + SID);
            return;
        }
        jo.remove("name");
        jo.remove("SID");
        jo.remove("Command");
        // client erfolgreich abgemeldet (aus map gelöscht)
        jo.put("Status", "OK");
        // speichere die Änderungen im Json ab
        saveData();
    }

    /**
     * Client sendet query <SID> 
     * Server send Status zurueck 
     * 
     * OK (alles gut: SID
     * vorhanden ) 
     * 
     * ERROR (SID nicht vorhanden)
     * 
     * und (im Falle von OK) aktuelle Liste aller Namen mit ihren Werten für die
     * SID
     * 
     * @param jo
     */
    private void handleQuery(JSONObject jo) {
        String SID = (String) jo.getOrDefault("SID", null);
        if (SID == null) {
            jo.put("Status", "Error: SID not set");
            return;
        }
        Set<ClientData> set = data.get(SID);
        if (set == null) {
            // ungültige SID
            jo.put("Status", "Error: SID " + SID + " does not exist");
            return;
        }
        // Die "saubere" Antwort soll reinen Status (+Client-Liste) zurück liefern, ohne den Request vom Client
        jo.remove("SID");
        jo.remove("Command");
        // SID gefunden, Daten zurueck senden
        jo.put("Status", "OK");
        
        // Hole alle Clients, welche sich zu der übergebenen SID registriert haben
        JSONArray jArray = new JSONArray();
        for (ClientData clientData : set) {
            JSONObject json = new JSONObject();
            json.put("name", clientData.getName());
            json.put("value", clientData.getValue());
            jArray.add(json);
        }
        jo.put("Clients", jArray);
    }

    /**
     * Client sendet reset <SID> 
     * 
     * Server send Status zurueck 
     * 
     * OK (alles gut: SID
     * vorhanden )
     * 
     * ERROR (SID nicht vorhanden)
     * 
     * @param jo
     */
    private void handleReset(JSONObject jo) {
        String SID = (String) jo.getOrDefault("SID", null);
        if (SID == null) {
            jo.put("Status", "Error: SID not set");
            return;
        }
        Set<ClientData> set = data.remove(SID);
        if (set == null) {
            // ungültige SID
            jo.put("Status", "Error: SID " + SID + " does not exist");
            return;
        }
        // Die "saubere" Antwort soll reinen Status zurück liefern, ohne den Request vom Client
        jo.remove("SID");
        jo.remove("Command");
        // SID gefunden, loesche alle Daten in der Map
        jo.put("Status", "OK");
        // speichere die Änderungen im Json ab
        saveData();
    }

    /**
     * Beende den Server und speichere alle Daten ab
     */
    private void handleExit() {
        // speichere die Änderungen im Json ab
        saveData();
        System.exit(0);
    }

    /**
     * Sende die Antwort zurueck. Eine Antwort besteht aus dem Status (OK,
     * ERROR) und (im Falle von query) einer zusätzlichen Information
     * 
     * @param msg - empfangene Nachricht als String
     * @return - Antwort
     * 
     */
    private String sendResponse(String msg) {
        // JSOn-String in ein JSON-Object parsen und casten
        System.out.println("JSON-String to Parse: " + msg);

        JSONObject jo = (JSONObject) JSONValue.parse(msg);

        String command = (String) jo.getOrDefault("Command", null);
        if (command != null) {
            // wir pruefen was der Client uns so nettes geschickt hat
            switch (command) {
            // wir senden die Nachricht zurück (Status + evtl. Daten)
            case "register":
                handleRegister(jo);
                break;
            case "unregister":
                handleUnregister(jo);
                break;
            case "query":
                handleQuery(jo);
                break;
            case "reset":
                handleReset(jo);
                break;
            case "exit":
                handleExit();
                break;
            default:
                // dieser Fall kann nie auftreten, weil die Schnittstelle nur
                // über vordefinierte Methoden angesprochen werden kann
                // Ein komplett ungültiger Befehl
                break;
            }
        }

        String response = jo.toJSONString();
        System.out.println("Response : " + response);

        return response; // JSONObject in String umwandeln.
    }

}
