import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

public class project {

	DatagramSocket socket;

	public project(int port) throws SocketException {
		socket = new DatagramSocket(port);
	}

	public void senderUDP(int port, String computerName) throws IOException {
		
	
		InetAddress destination = InetAddress.getByName("255.255.255.255");
		DatagramPacket packet = new DatagramPacket(computerName.getBytes(), computerName.length(), destination, port);
		socket.send(packet);

	}

	public void receiverUDP(String computerName) throws IOException {
		
		
		DatagramPacket receivedPocket = new DatagramPacket(new byte[1024], 1024);
		socket.receive(receivedPocket);
		byte[] data = receivedPocket.getData();
		String str = new String(data, 0, receivedPocket.getLength());
		String srcAddr = receivedPocket.getAddress().toString();
		
		if(!str.equals(computerName)) {
			System.out.println("IP address of the sender: " + srcAddr);
			DatagramPacket packet2 = new DatagramPacket(computerName.getBytes(), computerName.length(), receivedPocket.getAddress(), receivedPocket.getPort());
			socket.send(packet2);
		}
		
	}

	public static void main(String[] args)throws IOException {
		
		int port = 9998;
		project pj = new project(port);
		Scanner scanner = new Scanner(System.in);
		System.out.println("Please input your computer name:");
		String computerName = scanner.nextLine().trim();
		pj.senderUDP(port, computerName);
		while(true) {
			System.out.println("Listening...");
			pj.receiverUDP(computerName);
		}
		
	}

}
