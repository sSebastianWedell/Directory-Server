package va.ue03;

import java.util.Objects;

/**
 * Eigener Datentyp, um Client-Daten zu persistieren
 * 
 * Beim register-Befehl übermittelt der Client seinen Namen und Value
 * Diese Daten werden in einer Map abgelegt und später persistiert (JSON)
 * Beim unreigster-Befehl übermittelt der Client nur seinen Namen.
 * Anhand des Namens kann aus der Map der entsprechende Eintrag gelöscht werden
 *
 */
public class ClientData {

    private String name;
    private String value;
    public ClientData(String name, String value) {
        super();
        this.name = name;
        this.value = value;
    }
    
    public String getName() {
        return name;
    }
    
    public String getValue() {
        return value;
    }

    public int hashCode() {
        return Objects.hash(name);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ClientData)) {
            return false;
        }
        ClientData other = (ClientData) obj;
        return Objects.equals(name, other.name);
    }
    
}
