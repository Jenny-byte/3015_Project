import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class TPCserver {
	ServerSocket srvSocket;
	ArrayList<Socket> list = new ArrayList<Socket>();

	public TPCserver(int port) throws IOException {
		srvSocket = new ServerSocket(port);

		while (true) {
			System.out.printf("My server is listening at port %d...\n", port);
			Socket cSocket = srvSocket.accept();

			synchronized (list) {
				list.add(cSocket);
				System.out.printf("Total %d clients are connected.\n", list.size());
			}

			Thread t = new Thread(() -> {
				try {
					serve(cSocket);
				} catch (IOException e) {
					System.err.println("connection dropped.");
				}
				synchronized (list) {
					list.remove(cSocket);
				}
			});
			t.start();
		}
	}

	private void serve(Socket clientSocket) throws IOException {
		byte[] buffer = new byte[1024];
		System.out.printf("Established a connection to host %s:%d\n\n", clientSocket.getInetAddress(),
				clientSocket.getPort());

		try {
			
			DataInputStream in = new DataInputStream(clientSocket.getInputStream());
			while (true) {

				long size = in.readLong();
				String receivedData = "";
				while (size > 0) {
					int len = in.read(buffer, 0, buffer.length);
					receivedData = new String(buffer, 0, len);
					size -= len;
					System.out.print(".");
				}

				respond(clientSocket, receivedData);
			}
		} catch (Exception e) {
			System.err.println("ERROR: Connection dropped");
		}
	}

	private void respond(Socket clientSocket, String receivedData) {

		String data = receivedData.trim();
		String dataArray[] = data.split(" ");
		String commend = dataArray[0];

		switch (commend) {
		case "login":
			String username = dataArray[1];
			String password = dataArray[2];
			verifyPassward(clientSocket, username, password);
			break;

		case "ls":
			// ls(socket, directory);
			break;

		case "md":
			// md(socket, directory);
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
			
		case "exit":
			try {
				clientSocket.close();
			} catch (Exception e) {
				System.err.println("ERROR: Connection dropped");
			}
			break;

		default:
			sendReply(clientSocket, "UnknownCommand");
			break;
		}
	}

	private void verifyPassward(Socket clientSocket, String username, String password) {
		String reply = "invalid";

		if (password.equals("12345")) { // valid login
			reply = "valid";
		}
		
		reply = "login " + username + " " + password;
		sendReply(clientSocket, reply);
	}

	private void sendReply(Socket clientSocket, String reply) {
		try {
			
			DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
			byte[] data = reply.getBytes();
			out.writeLong(reply.length());
			out.write(reply.getBytes(), 0, reply.length());
		} catch (Exception e) {
			System.out.println("Fail to send your reply.");
		}
	}
}
