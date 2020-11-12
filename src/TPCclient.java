import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class TPCclient {
	

	public TPCclient(String server, int tcpPort) throws IOException{

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

		System.out.println("Please input your name:");
		String name = scanner.nextLine().trim();

		System.out.println("Please input messages:");

		while (true) {
			// String str = scanner.nextLine();
			String str = name + ": " + scanner.nextLine();
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeInt(str.length());
			out.write(str.getBytes(), 0, str.length());
		}
	}
	

}
