import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class TPCclient {

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
					
					
					int len = in.readInt();
					in.read(buffer, 0, len);
					System.out.println(new String(buffer, 0, len));
				}
				
				
				
			} catch (IOException ex) {
				System.err.println("Connection dropped!");
				System.exit(-1);
			}
		});
		t.start();

		Scanner scanner = new Scanner(System.in);
		
		System.out.println("Please enter the commend:");
		int commendNum = scanner.nextInt();

		while (true) {
			// String str = scanner.nextLine();
			String str = replyCommend(commendNum);
			sendReply(socket, str);
		}
	}

	public void login() {
		System.out.println("Please enter your username:");
		Scanner scanner = new Scanner(System.in);
		String username = scanner.nextLine().trim();

		System.out.println("Please enter your password:");
		String password = scanner.nextLine().trim();
	}
	
	public String replyCommend(int commendNum) {
		String reply = "";
		
		return reply;
	}
	
private void sendReply(Socket socket, String reply) {
		try {
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			byte[] data = reply.getBytes();
			out.writeInt(reply.length());
			out.write(reply.getBytes(), 0, reply.length());
		} catch (Exception e) {
			System.out.println("ERROR: Fail to send your reply.");
		}
	}

}
