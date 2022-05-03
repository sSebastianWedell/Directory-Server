package va.ue03;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class EchoServer extends Thread {

    private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[1024]; //Buffergroesse von 256 kb
    
    /**
     * Konstruktor, bindet den Echo-Server an Port 11111
     * 
     * @throws SocketException
     */
    public EchoServer() throws SocketException 
    {
    	socket = new DatagramSocket(11111);
    }
    
    
    /**
     * Main mit Instanziierung des Echo-Servers und starten des Threads
     * 
     * @param args
     */
//    public static void main(String[] args) {
//    	try {
//    		// Echo-Server laeuft in einem eigenen Thread
//			EchoServer es = new EchoServer();
//			es.run();
//		} catch (SocketException e) 
//    	{
//			e.printStackTrace();
//		}
//	}
    
    /**
     *  Thread-Run
     *  
     *  Der Echo-Server laeuft in einer Endlosschleife, bis die uebertragende Nachricht "end" enthaelt.
     */
    public void run() 
    {
    	System.out.println("Start Server at 127.0.0.1");
        running = true;
        while (running) {
        	buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try 
            {
				socket.receive(packet);
			} 
            catch (IOException e) 
            {
				e.printStackTrace();
			}
            
            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            
            packet = new DatagramPacket(buf, buf.length, address, port);
            
            // Empfangene Nachricht besteht aus der Data-Load vom empfangenen Packet
            String received = new String(packet.getData(), 0, packet.getLength()); 
            
            // Begrenze den empfangenen String auf die letzte geschweifte Klammer (Buffer ist laenger als emfange Nachricht)
            received = received.substring(0,received.lastIndexOf("}")+1 );
            
            // Wenn "end" empfangen wird dann beende die Schleife und damit den Thread
            if (received.equals("end")) 
            {
            	running = false;
            	continue;
            } 
            else
            {
            	// Empfangener String wird ueber die breakMsg-Funktion manipuliert und zurueckgeschickt
            	String returnMsg  = breakMsg(received);
            	packet.setData(returnMsg.getBytes());
            	try 
            	{
            		socket.send(packet); // Zuruecksenden vom empfangenen Paket
            	} 
            	catch (IOException e) 
            	{
            		e.printStackTrace();
            	}
            }
        }
        socket.close();
    }
    
    /**
     * Veraendere das empfangene Paket 
     * @param msg - empfangene Nachricht als String
     * @return - veraenderter JSON-String
     * 
     * Die empfangene Nachricht wird als JSON geparsed und in ein JSONObject gecasted.
     * 
     */
    private String breakMsg(String msg)
    {
    	//JSOn-String in ein JSON-Object parsen und casten
    	System.out.println("JSON-String to Break: " + msg);
    	
    	JSONObject jo = (JSONObject)JSONValue.parse(msg);
    	
    	String command = (String)jo.getOrDefault("Command", null);
    	if(command != null)
    	{
    	    // wir pruefen was der Client uns so nettes geschickt hat
    		switch (command) 
    		{
    		// wir senden die Nachricht zurück, passen den JSON an
    		case "register":
    		    String name = (String)jo.getOrDefault("name", null);
                if(name != null)
                {
                    jo.remove("name");
                    jo.put("name", name+"-ABC");
                }
                String value = (String)jo.getOrDefault("value", null);
                if(value != null)
                {
                    jo.remove("value");
                    jo.put("value", value+"-XXX");
                }
    		    String SID = (String)jo.getOrDefault("SID", null);
                if(SID != null)
                {
                    jo.remove("SID");
                    jo.put("SID", SID+"-FAILED");
                }
                break;
    		case "unregister":
    		    name = (String)jo.getOrDefault("name", null);
                if(name != null)
                {
                    jo.remove("name");
                    jo.put("name", name+"-KAPUTT");
                }
                SID = (String)jo.getOrDefault("SID", null);
                if(SID != null)
                {
                    jo.remove("SID");
                    jo.put("SID", SID+"-CHANGED");
                }
                break;
			case "query": // Wenn das JSON den Query-Command hat, dann veraendere die mitgeschickte SID
				SID = (String)jo.getOrDefault("SID", null);
				if(SID != null)
				{
					jo.remove("SID");
					jo.put("SID", SID+"-changed");
				}
				break;
			case "reset":
			    SID = (String)jo.getOrDefault("SID", null);
                if(SID != null)
                {
                    jo.remove("SID");
                    jo.put("SID", SID+"-RESET");
                }
                break;
			case "exit":
			    System.exit(0);
			    break;
			default:
			    // dieser Fall kann nie auftreten, weil die Schnittstelle nur über vordefinierte Methoden angesprochen werden kann
			    // Ein komplett ungültiger Befehl
				break;
			}
    	}
    	
    	// hier ist ein bisschen unklar:
    	// in der Aufgabenstellung steht: 
    	/*
    	 * "In diesem werden bei den Requests (Hinweg) der Name der Operation samt Parameter
            angegeben, und auf dem Rückweg (Response) das Ergebnis. In jedem Fall wird auf dem
            Rückweg ein Returnwert über den Erfolg (z.B. Wert für OK) oder den Misserfolg der Anfrage
            mitgesendet."
            
            d.h. der Server schickt einen Status als response (OK, ERROR, ...)
            
            gleichzeitig steht aber bei Aufgabe4 der Server soll einfach den Request von Client "kaputt" machen
            und ihn zurück schicken (wie beispielhaft bei query)
            
             also sendet der Server doch keinen Status (OK, ERROR, ...), sondern im Prinzip das gleiche Format
             wie Client, aber nur mit "kaputtem" Inhalt
    	 */
    	
    	String changedJSONstring = jo.toJSONString();
    	System.out.println("Broken JSON-String: " + changedJSONstring);
    	
    	return changedJSONstring;	// JSONObject in String umwandeln.
    }
}