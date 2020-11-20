import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Scanner;

public class project {
	ServerSocket srvSocket;
	ArrayList<DatagramPacket> clientList = new ArrayList<DatagramPacket>();
	// ArrayList<Socket> list = new ArrayList<Socket>();
	// boolean isRunning = true;

	public project() throws IOException {
		
		Thread t1 = new Thread(() -> {
			try {
				udpServer(9998);

			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		t1.start();

		Thread t2 = new Thread(() -> {
			tpcClient(9999);

		});

		t2.start();

		Thread t3 = new Thread(() -> {
			try {
				TPCserver tpcServer = new TPCserver(9999);

			} catch (IOException e) {

				e.printStackTrace();
			}
		});
		t3.start();
	}

	public void udpServer(int port) throws IOException {

		// can't delete the client!!

		InetAddress myIp = InetAddress.getLocalHost();
		String computerName = myIp.getHostName();
		System.out.println("My computer name: " + computerName);
		DatagramSocket socket = new DatagramSocket(port);
		DatagramPacket packet = new DatagramPacket(computerName.getBytes(), computerName.length(),
				InetAddress.getByName("255.255.255.255"), port);

		socket.send(packet);
		System.out.println("Searching servers...");
		while (true) {
			DatagramPacket receivedPacket = new DatagramPacket(new byte[1024], 1024);
			socket.receive(receivedPacket);
			String receivedData = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
			String srcAddr = receivedPacket.getAddress().toString();

			// only the computerName is different can be added to the list
			//!receivedData.equals(computerName) && 
			if (!receivedData.equals(computerName) && !checkPacketExistInList(srcAddr)) {
				synchronized (clientList) {
					clientList.add(receivedPacket);
				}

				// reply the computer name
				packet = new DatagramPacket(computerName.getBytes(), computerName.length(), receivedPacket.getAddress(),
						receivedPacket.getPort());
				socket.send(packet);
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			socket.send(packet);
		}
	}

	public boolean checkPacketExistInList(String srcAddr) { //check packet exist in list by ip address
		for (DatagramPacket client : clientList) {
			String ListIpAddress = client.getAddress().toString();
			if (ListIpAddress.equals(srcAddr)) {
				return true;
			}

		}
		return false;
	}

	public void printList(ArrayList<DatagramPacket> cList) {
		int i = 1;
		for (DatagramPacket client : cList) {
			String computerName = new String(client.getData(), 0, client.getLength());
			System.out.println(i + ". " + computerName + " (" + client.getAddress().toString() + ")");
			i++;
		}
	}

	private void tpcClient(int tcpPort) {

		boolean welcome = true;
		while (true) {

			System.out.println("----------------------------------------");
			System.out.println("Please input the option number:");
			System.out.println("1. List out all the avaliable servers.\n" + "2. Find a server to connect and login.\n"
					+ "3. Exit\n");
			Scanner scanner2 = new Scanner(System.in);
			int option = scanner2.nextInt();

			if (option == 1) {
				synchronized (clientList) {
					System.out.printf("Total %d server(s) in the list:\n", clientList.size());
					printList(clientList);
				}

			} else if (option == 2) {
				if (clientList.isEmpty()) {
					System.out.println("No server can be chosen.");
				} else {
					chooseServerConnect(tcpPort);
				}
			} else if (option == 3) {
				System.out.println("bye!");
				welcome = false;
				System.exit(0);

			} else {
				System.out.println("Invalid input!");
			}
		}
	}

	public void chooseServerConnect(int tcpPort) {
		Scanner scanner = new Scanner(System.in);
		boolean inputServer = true;

		while (inputServer == true) {

			System.out.printf("Total %d server(s) in the list.\n", clientList.size());
			printList(clientList);
			System.out.println();
			System.out.println("Please enter the number of the server that you want to connect.");
			int serverNum = scanner.nextInt();
			if (serverNum > clientList.size()) {
				System.out.println("No such server!");
			} else {
				DatagramPacket chosenPacket;
				synchronized (clientList) {
					chosenPacket = clientList.get((serverNum - 1));
				}
				String computerName = new String(chosenPacket.getData(), 0, chosenPacket.getLength());
				String ipAddress = chosenPacket.getAddress().toString();
				ipAddress = ipAddress.substring(1, ipAddress.length());
				System.out.println("Chosen server: " + computerName + " with IP address " + ipAddress);

				String s = null;
				int p = 0;
				try {
					s = ipAddress;
					p = tcpPort;
				} catch (IndexOutOfBoundsException | NumberFormatException e) {
					System.err.println("Usage: java chosenServerConnect ipaddress portNum");
					System.exit(-1);
				}

				try {
					TPCclient tpcClient = new TPCclient(s, p);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

	}

	public static void main(String[] args) throws IOException {
		project s = new project();
	}

}
