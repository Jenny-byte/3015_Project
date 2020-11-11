import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class project {
	ServerSocket srvSocket;
	ArrayList<DatagramPacket> clientList = new ArrayList<DatagramPacket>();
	ArrayList<Socket> list = new ArrayList<Socket>();

	public project() throws IOException {
		
		Thread t1 = new Thread(() -> {
			try {
				udpServer(9998);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		t1.start();
		
//		int portTCP = 9999;
//		srvSocket = new ServerSocket(portTCP );
//
//		while (true) {
//			System.out.printf("Listening at port %d...\n", portTCP );
//			Socket cSocket = srvSocket.accept();
//
//			synchronized (list) {
//				list.add(cSocket);
//				System.out.printf("Total %d clients are connected.\n", list.size());
//			}
//
//			Thread t2 = new Thread(() -> {
//				try {
//					serveTCP(cSocket);
//				} catch (IOException e) {
//					System.err.println("connection dropped.");
//				}
//				synchronized (list) {
//					list.remove(cSocket);
//				}
//			});
//			t2.start();
//		}
	}
	
	public void udpServer(int port) throws IOException {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Please input your computer name:");
		String computerName = scanner.nextLine().trim();
		
		DatagramSocket socket = new DatagramSocket(port);
		DatagramPacket packet = new DatagramPacket(computerName.getBytes(), computerName.length(), InetAddress.getByName("255.255.255.255"), port);
		socket.send(packet);
		
		DatagramPacket receivedPacket = new DatagramPacket(new byte[1024], 1024);
		System.out.println("Searching clients...");
		while(true) {
			
			socket.receive(receivedPacket);
			String receivedData = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
			String srcAddr = receivedPacket.getAddress().toString();
			
			if (!receivedData.equals(computerName) && !clientList.contains(receivedPacket)) {
				synchronized (clientList) {
					clientList.add(receivedPacket);
					System.out.printf("Total %d clients are in the list.\n", clientList.size());
					printList(clientList);
				}
				packet = new DatagramPacket(computerName.getBytes(), computerName.length(), receivedPacket.getAddress(), receivedPacket.getPort());
				socket.send(packet);
			}
		}
	}
	
	public void printList(ArrayList<DatagramPacket> list){
		int i = 1;
	    for(DatagramPacket client : list){
	    	String computerName = new String(client.getData(), 0, client.getLength());
	        System.out.println(i+ ". " + computerName +" ("+ client.getAddress().toString() +")");
	        i++;
	    }
	}
	
	private void serveTCP(Socket clientSocket) throws IOException {
		byte[] buffer = new byte[1024];
		System.out.printf("Established a connection to host %s:%d\n\n", clientSocket.getInetAddress(),
				clientSocket.getPort());

		DataInputStream in = new DataInputStream(clientSocket.getInputStream());
		DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
		while (true) {
			//functions
		}
	}

	private void forward(byte[] data, int len, Socket clientSocket) {
		synchronized (list) {
			for (int i = 0; i < list.size(); i++) {
				try {
					Socket socket = list.get(i);
					if( socket != clientSocket) {
					DataOutputStream out = new DataOutputStream(socket.getOutputStream());
					out.writeInt(len);
					out.write(data, 0, len);
					}
				} catch (IOException e) {
					// the connection is dropped but the socket is not yet removed.
				}
			}
		}
	}
	
	public static void main(String[] args)throws IOException {
		project s = new project();
	}
	
}
