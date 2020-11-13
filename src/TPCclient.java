import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class TPCclient {
	boolean loginValid = false;

	public TPCclient(String server, int tcpPort) throws IOException {

		try {
			tpcClient(server, tcpPort);
		} catch (IOException e) {
			System.err.printf("Unable to connect server %s:%d\n", server, tcpPort);
			System.exit(-1);
		}

	}

	public void tpcClient(String server, int tcpPort) throws IOException {
		Socket socket = new Socket(server, tcpPort);

		Thread t = new Thread(() -> {
			byte[] buffer = new byte[1024];
			try {
				DataInputStream in = new DataInputStream(socket.getInputStream());
				while (true) {

					long size = in.readLong();
					String receivedData = "";
					while (size > 0) {
						int len = in.read(buffer, 0, buffer.length);
						receivedData += new String(buffer, 0, len);
						size -= len;
					}

					respondReceived(socket, receivedData);
				}
			} catch (IOException ex) {
				System.err.println("Connection dropped!");
				System.exit(-1);
			}
		});
		t.start();

		Scanner scanner = new Scanner(System.in);
		String commend = "";
		
		while(!loginValid) {
			commend = login();
		}
		
		System.out.println("Please enter the commend:");
		commend = scanner.nextLine();
		
		
		while (true) {
			// String str = scanner.nextLine();
			//String str = requestCommend(commend);
			sendRequest(socket, commend);
		}
	}

	public String login() {
		System.out.println("Please enter your username:");
		Scanner scanner = new Scanner(System.in);
		String username = scanner.nextLine().trim();

		System.out.println("Please enter your password:");
		String password = scanner.nextLine().trim();
		
		String request = "login "+ username + " " + password;
		return request;
	}

	public String requestCommend(String commend) {
		String request = "";

		return request;
	}

	public void respondReceived(Socket socket, String receivedData) {
		String data = receivedData.trim();
		String dataArray[] = data.split(" ");
		String commend = dataArray[0];

		switch (commend) {
		case "login":
			if(dataArray[1] == "valid") {
				loginValid = true;
			}else {
				System.out.println("Invalid login!");
			}
			break;

		case "ls":
			
			break;

		case "md":
			
			break;

		case "upload":

			break;

		case "download":

			break;

		case "delF":

			break;

		case "delD":

			break;

		case "rename":

			break;
			
		case "detailF":

			break;
		}
			
		
	}

	private void sendRequest(Socket socket, String request) {
		try {
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			byte[] data = request.getBytes();
			out.writeInt(request.length());
			out.write(request.getBytes(), 0, request.length());
		} catch (Exception e) {
			System.out.println("Fail to send your request.");
		}
	}

}
