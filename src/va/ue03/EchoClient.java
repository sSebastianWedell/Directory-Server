package va.ue03;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class EchoClient {
    private DatagramSocket socket;
    private InetAddress address;

    private byte[] sendbuf;
    private byte[] receivebuf = new byte[1024];

    public EchoClient() throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        address = InetAddress.getByName("127.0.0.1");
    } 

    public String sendEcho(String msg) throws IOException 
    {
    	// System.out.println("buffer: " + msg); 	// Zu sendene Nachricht
    	sendbuf = msg.getBytes();					// Erzeuge Buffer
        DatagramPacket packet = new DatagramPacket(sendbuf, sendbuf.length, address, 11111);
        
        
        socket.send(packet);					// Sende Nachricht
        packet = new DatagramPacket(receivebuf, 1024);
        socket.receive(packet);
        
        
        String received = new String(packet.getData(), 0, packet.getLength());
        return received;
    }

    public void close() 
    {
        socket.close();
    }
    
    private void handleMsg(String msg)
    {
    	
    }
}