package va.ue03;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Kurzer Ablauf zu den Tests:
 * 
 * Tests sind in einer bestimmten Reihenfolge...das erleichtert das Testen
 * zuerst anmelden (register) -> hier melden wir Client1 und Client2 bei SID1 und Client1 nochmal bei SID2 an
 * dann abfragen (query)      -> einfache Queries bei SID1 und SID2
 * dann abmelden (unregister) -> Client1 meldet sich von SID2 ab
 * anschliessend reset        -> Cleint1 und Client2 sind somit von SID1 abgemeldet
 * 
 * ACHTUNG: 
 *  - damit Tests laufen, muss vorher Server gestartet werden
 *  - ein Server muss immer neugestartet werden, bevor man einen Testlauf macht (sonst würde 1.Testlauf funktioniert und 2.Testlauf dann nicht mehr)
 *  - Am Anfang bitte die server.json-Datei löschen!!!
 */
@TestMethodOrder(OrderAnnotation.class)
class AllTest {
    // ACHTUNG: damit Tests laufen, muss vorher Server gestartet werden
    Ue03DirectoryClient uedc;

    @BeforeEach
    void setUp() throws Exception {
        uedc = new Ue03DirectoryClient();
    }
    
    //-------------REGISTER    
    
    /**
     * Test der Register-Funktion
     */
    @Test
    @Order(1)
    void testRegisterPositive() {
        // Am Anfang gibt es server.json-Datei nicht
        assertFalse(Files.exists(Paths.get(Ue04DirectoryServer.JSONFILE)), 
                "server.json-Datei darf zur Beginn von Tests nicht vorliegen. Bitte löschen!");
        
        // Client1 registriert sich an SID 1
        String name = "Client1";
        String value = "127.0.0.1:8080";
        String SID = "1";
        String echoAnswer = uedc.register(name, value, SID);
        JSONObject jo = (JSONObject) JSONValue.parse(echoAnswer);
        String status = (String) jo.get("Status");
        // alles gut, Registrierung erfolgreich
        assertEquals(status, "OK");
        // Datei angelegt
        assertTrue(Files.exists(Paths.get(Ue04DirectoryServer.JSONFILE)));
    }
    
    /**
     * Test der Register-Funktion (doppelter Client)
     */
    @Test
    @Order(2)
    void testRegisterDuplicate() {
        // Client1 registriert sich wieder an SID 1
        String name = "Client1";
        String value = "127.0.0.1:8080";
        String SID = "1";
        String echoAnswer = uedc.register(name, value, SID);
        JSONObject jo = (JSONObject) JSONValue.parse(echoAnswer);
        String status = (String) jo.get("Status");
        // Nicht erlaubt, da Client1 bereits an SID 1 registiriert ist
        assertEquals(status, "Error: Client " + name + " already registered");
    } 
    
    /**
     * Test der Register-Funktion ohne Namen
     */
    @Test
    @Order(3)
    void testRegisterNoName() {
        // Client registriert sich ohne Namen
        String name = null;
        String value = "127.0.0.1:8080";
        String SID = "1";
        String echoAnswer = uedc.register(name, value, SID);
        JSONObject jo = (JSONObject) JSONValue.parse(echoAnswer);
        String status = (String) jo.get("Status");
        // Fehler
        assertEquals(status, "Error: name not set");
    } 
    
    /**
     * Test der Register-Funktion ohne SID
     */
    @Test
    @Order(4)
    void testRegisterNoSID() {
        // Client registriert sich ohne SID
        String name = "Client1";
        String value = "127.0.0.1:8080";
        String SID = null;
        String echoAnswer = uedc.register(name, value, SID);
        JSONObject jo = (JSONObject) JSONValue.parse(echoAnswer);
        String status = (String) jo.get("Status");
        // Fehler
        assertEquals(status, "Error: SID not set");
    }


    /**
     * Test der Register-Funktion ohne Value
     */
    @Test
    @Order(5)
    void testRegisterNoValue() {
     // Client registriert sich ohne Value
        String name = "Client1";
        String value = null;
        String SID = "1";
        String echoAnswer = uedc.register(name, value, SID);
        JSONObject jo = (JSONObject) JSONValue.parse(echoAnswer);
        String status = (String) jo.get("Status");
        // Fehler
        assertEquals(status, "Error: value not set");
    }
    
    /**
     * Test der Register-Funktion
     */
    @Test
    @Order(6)
    void testRegisterSameSidAnotherClient() {
        // Client2 registriert sich an SID 1
        String name = "Client2";
        String value = "127.0.0.1:8080";
        String SID = "1";
        String echoAnswer = uedc.register(name, value, SID);
        JSONObject jo = (JSONObject) JSONValue.parse(echoAnswer);
        String status = (String) jo.get("Status");
        // alles gut, Registrierung erfolgreich
        assertEquals(status, "OK");
    }
    
    /**
     * Test der Register-Funktion
     */
    @Test
    @Order(7)
    void testRegisterSameClientAnotherSid() {
        // Client1 registriert sich an SID 2
        String name = "Client1";
        String value = "127.0.0.1:8080";
        String SID = "2";
        String echoAnswer = uedc.register(name, value, SID);
        JSONObject jo = (JSONObject) JSONValue.parse(echoAnswer);
        String status = (String) jo.get("Status");
        // alles gut, Registrierung erfolgreich
        assertEquals(status, "OK");
    } 
   

    
    //-------------QUERY
    
    /**
     * Test der Query-Funktion auf mitgeschickte SID
     */
    @Test
    @Order(8)
    void testQueryPositiveSingleValue() {
        // wir schicken eine Query für die es eine Session gibt
        String SID = "2";
        String echoAnswer = uedc.query(SID);
        JSONObject jo = (JSONObject) JSONValue.parse(echoAnswer);
        String status = (String) jo.get("Status");
        // Status ok
        assertEquals(status, "OK");
        // Client1 ist bei Session2 angemeldet
        JSONArray jArray = (JSONArray) jo.get("Clients");
        for (Object object : jArray) {
            JSONObject obj = (JSONObject) object;
            assertEquals("Client1", obj.get("name"));
            assertEquals("127.0.0.1:8080", obj.get("value"));
        }
    }
    
    /**
     * Test der Query-Funktion auf mitgeschickte SID
     */
    @Test
    @Order(9)
    void testQueryPositiveMultipleValue() {
        // wir schicken eine Query für die es eine Session gibt
        String SID = "1";
        String echoAnswer = uedc.query(SID);
        JSONObject jo = (JSONObject) JSONValue.parse(echoAnswer);
        String status = (String) jo.get("Status");
        // Status ok
        assertEquals(status, "OK");
        // Client1 und Client2 sind bei Session1 angemeldet
        JSONArray jArray = (JSONArray) jo.get("Clients");
        int i = 1;
        for (Object object : jArray) {
            JSONObject obj = (JSONObject) object;
            assertEquals("Client"+i++, obj.get("name"));
            assertEquals("127.0.0.1:8080", obj.get("value"));
        }
    }    

    /**
     * Test der query-function auf leere SID
     */
    @Test
    @Order(10)
    void testQueryEmptySID() {
        // SID null
        String echoAnswer = uedc.query(null);
        JSONObject jo = (JSONObject) JSONValue.parse(echoAnswer);
        String status = (String) jo.get("Status");
        // Fehler
        assertEquals(status, "Error: SID not set");
    }
    
    /**
     * Test der query-function auf ungültige SID
     */
    @Test
    @Order(11)
    void testQueryInvalidSID() {
        // SID ungültig
        String SID = "42";
        String echoAnswer = uedc.query(SID);
        JSONObject jo = (JSONObject) JSONValue.parse(echoAnswer);
        String status = (String) jo.get("Status");
        // Fehler
        assertEquals(status, "Error: SID " + SID + " does not exist");
    }
    
    //-------------UNREGISTER
    /**
     * Test der Unregister-Funktion
     */
    @Test
    @Order(12)
    void testUnregisterPositive() {
        // Client1 meldet sich von SID 2 ab
        String name = "Client1";
        String SID = "2";
        String echoAnswer = uedc.unregister(name, SID);
        JSONObject jo = (JSONObject) JSONValue.parse(echoAnswer);
        String status = (String) jo.get("Status");
        // alles gut, Abmeldung erfolgreich
        assertEquals(status, "OK");
        // anschließend prüfen wir mit dem Query, dass unter SID2 niemand da ist
        echoAnswer = uedc.query("2");
        jo = (JSONObject) JSONValue.parse(echoAnswer);
        status = (String) jo.get("Status");
        // Fehler: da Client1 der Einzige unter SID 2 war
        assertEquals(status, "Error: SID " + SID + " does not exist");
        
    }
    
    /**
     * Test der Unregister-Funktion ohne Namen
     */
    @Test
    @Order(13)
    void testUnregisterNoName() {
        // Client meldet sich ohne Namen ab
        String name = null;
        String SID = "1";
        String echoAnswer = uedc.unregister(name, SID);
        JSONObject jo = (JSONObject) JSONValue.parse(echoAnswer);
        String status = (String) jo.get("Status");
        // Fehler
        assertEquals(status, "Error: name not set");
    } 
    
    /**
     * Test der Unregister-Funktion ohne SID
     */
    @Test
    @Order(14)
    void testUnregisterNoSID() {
        // Client meldet sich ohne SID ab
        String name = "Client1";
        String SID = null;
        String echoAnswer = uedc.unregister(name, SID);
        JSONObject jo = (JSONObject) JSONValue.parse(echoAnswer);
        String status = (String) jo.get("Status");
        // Fehler
        assertEquals(status, "Error: SID not set");
    }


    /**
     * Test der Unregister-Funktion ungültige SID
     */
    @Test
    @Order(15)
    void testUnregisterInvalidSID() {
        // Client meldet sich von ungütliger SID ab
        String name = "Client1";
        String SID = "42";
        String echoAnswer = uedc.unregister(name, SID);
        JSONObject jo = (JSONObject) JSONValue.parse(echoAnswer);
        String status = (String) jo.get("Status");
        // Fehler
        assertEquals(status, "Error: SID " + SID + " does not exist");
    }
    
    /**
     * Test der Unregister-Funktion mit ungültigem Client-Namen
     */
    @Test
    @Order(16)
    void testUnregisterInvalidClient() {
        // Client3 ist gar nicht bei SID 1 angemeldet
        String name = "Client3";
        String SID = "1";
        String echoAnswer = uedc.unregister(name, SID);
        JSONObject jo = (JSONObject) JSONValue.parse(echoAnswer);
        String status = (String) jo.get("Status");
        // Fehler
        assertEquals(status, "Error: Client " + name + " does not exist for Session " + SID);
    }
    
    //-------------RESET
    /**
     * Test der reset-function auf leere SID
     */
    @Test
    @Order(17)
    void testResetInvalidSID() {
        // SID ungültig
        String SID = "42";
        String echoAnswer = uedc.reset(SID);
        JSONObject jo = (JSONObject) JSONValue.parse(echoAnswer);
        String status = (String) jo.get("Status");
        // Fehler
        assertEquals(status, "Error: SID " + SID + " does not exist");
    }
    
    /**
     * Test der reset-function auf leere SID
     */
    @Test
    @Order(18)
    void testResetEmptySID() {
        // SID null
        String echoAnswer = uedc.reset(null);
        JSONObject jo = (JSONObject) JSONValue.parse(echoAnswer);
        String status = (String) jo.get("Status");
        // Fehler
        assertEquals(status, "Error: SID not set");
    }
    
    /**
     * Test der Reset-Funktion auf mitgeschickte SID
     */
    @Test
    @Order(19)
    void testResetPositiveSingleValue() {
        //bei SID1 sind 2 Clients angemeldet
        String SID = "1";
        String echoAnswer = uedc.query(SID);
        JSONObject jo = (JSONObject) JSONValue.parse(echoAnswer);
        String status = (String) jo.get("Status");
        // Status ok
        assertEquals(status, "OK");
        
        // wir löschen die Clients auf SID1
        echoAnswer = uedc.reset(SID);
        jo = (JSONObject) JSONValue.parse(echoAnswer);
        status = (String) jo.get("Status");
        // Status ok
        assertEquals(status, "OK");
        
        // nun liefert query einen Fehler
        echoAnswer = uedc.query(SID);
        jo = (JSONObject) JSONValue.parse(echoAnswer);
        status = (String) jo.get("Status");
        // Fehler
        assertEquals(status, "Error: SID " + SID + " does not exist");
    }
    
}
