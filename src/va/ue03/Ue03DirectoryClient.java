package va.ue03;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

/**
 * 
 * @author Alexander Kramer
 * @StudenId s0528394
 * 
 * @TODO
 */
public class Ue03DirectoryClient 
{
	private static JSONObject jo;
	private EchoClient el;

	public Ue03DirectoryClient() 
	{
		try {
			el = new EchoClient();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) 
	{
		Ue03DirectoryClient uedc = new Ue03DirectoryClient();
//		uedc.register("Client9", "127.0.0.1:8080", "2");
//		uedc.query("bla");
//		uedc.query(null);
		uedc.query("1");
		
		//TODO: test query, register, unregister, reset, exit
		//TODO: schreibe unit-tests dazu
		//TODO: alle möglichen Fälle testen (query -> liefert nur letztes Element, muss aber alle liefern!)
		
	}	

	public String query(String SID)
	{
		jo = new JSONObject();
		jo.put("Command", "query");
		if(SID != null)
		{
			jo.put("SID", SID);
		}
		return sendMsg(jo.toJSONString());
	}
	
	public String register(String name, String value, String SID) {
	    jo = new JSONObject();
        jo.put("Command", "register");
        if (name != null) {
            jo.put("name", name);
        }
        if (value != null) {
            jo.put("value", value);
        }
        if(SID != null)
        {
            jo.put("SID", SID);
        }
        return sendMsg(jo.toJSONString());
	}
	
    public String unregister(String name, String SID) {
        jo = new JSONObject();
        jo.put("Command", "unregister");
        if (name != null) {
            jo.put("name", name);
        }
        if(SID != null)
        {
            jo.put("SID", SID);
        }
        return sendMsg(jo.toJSONString());
    }
    
    public String reset(String SID)
    {
        jo = new JSONObject();
        jo.put("Command", "reset");
        if(SID != null)
        {
            jo.put("SID", SID);
        }
        return sendMsg(jo.toJSONString());
    }
    
	
	public int returnNumber(int number)
	{
		return number;
	}

	private String sendMsg(String msg)
	{
		try {
			return el.sendEcho(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
