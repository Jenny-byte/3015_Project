import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
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
					receivedData += new String(buffer, 0, len);
					size -= len;

				}

				respond(clientSocket, receivedData);

			}
		} catch (Exception e) {
			System.err.println("ERROR: Connection dropped");
		}
	}

	private void respond(Socket clientSocket, String receivedData) {

		String input = receivedData.trim();
		int spaceAt = input.trim().indexOf(" ");

		// String directory = "";
		// if (spaceAt > 0) {
		// commend = input.substring(0, spaceAt);
		// directory = input.substring(spaceAt + 1).replaceAll("\"", "").trim();
		// } else {
		// commend = input;
		// }

		String data = receivedData.trim();
		String dataArray[] = data.split(" ");
		String commend = dataArray[0];
		String path = "";

		switch (commend) {
		case "login":
			String username = dataArray[1];
			String password = dataArray[2];
			verifyPassward(clientSocket, username, password);
			break;

		case "ls":
		case "dir":
			path = dataArray[1];
			ls(commend, clientSocket, path);
			break;

		case "mkdir":
		case "md":
			path = dataArray[1];
			md(commend, clientSocket, path);
			break;

		case "upload":
			path = dataArray[1];
			upload(commend, clientSocket, path);

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
				System.out.println("Connection dropped! ");
			}
			break;

		default:
			sendRespond(clientSocket, commend + " Unknown Command!");
			break;
		}
	}

	private void verifyPassward(Socket clientSocket, String username, String password) {
		String reply = "Invalid login!";

		if (password.equals("12345")) { // valid login
			reply = "Successful login!";
		}

		reply = "login " + reply;
		System.out.println(reply);
		sendRespond(clientSocket, reply);
	}

	private void ls(String commend, Socket clientSocket, String path) { // list file
		String reply = commend;
		File obj = new File(path);
		if (!obj.exists()) {
			sendRespond(clientSocket, reply + " File Not Found!");
			return;
		}
		if (obj.isDirectory()) {
			File[] files = obj.listFiles();
			if (files.length == 0) {
				sendRespond(clientSocket, reply + " Empty!");
				return;
			}

			for (File f : files) {
				if (f.isDirectory()) {
					reply += new Date(f.lastModified()) + " " + " <DIR> " + f.getName() + "\n";
				} else {
					reply += new Date(f.lastModified()) + " " + f.length() + "B " + f.getName() + "\n";

				}
			}
			sendRespond(clientSocket, reply);

		} else {
			reply += new Date(obj.lastModified()) + " " + obj.length() + "B " + obj.getName() + "\n";
			sendRespond(clientSocket, reply);
		}
	}

	private void md(String commend, Socket clientSocket, String path) { // make directory
		String reply = commend;
		File obj = new File(path);

		if (obj.exists()) {
			if (obj.isDirectory())
				reply += " Directory already exists";
			else
				reply += " File already exists";

		} else {
			obj.mkdirs();
			reply += " Subdirectory is created successfully";
		}
		sendRespond(clientSocket, reply);
	}

	private void upload(String commend, Socket clientSocket, String path) {
		
		String reply = commend + " ";
		byte[] buffer = new byte[1024];
		try {
			DataInputStream in = new DataInputStream(clientSocket.getInputStream());
			
			int nameLen = in.readInt();
			in.read(buffer, 0, nameLen);
			String name = new String(buffer, 0, nameLen);

			System.out.print("Downloading file %s " + name);

			long size = in.readLong();
			System.out.printf("(%d)", size);

			
			File file = new File(name);
			FileOutputStream out = new FileOutputStream(file);

			while(size > 0) {
				int len = in.read(buffer, 0, buffer.length);
				out.write(buffer, 0, len);
				size -= len;
				System.out.print(".");
			}
			
			reply += name + " (" + size + ") is successful uploaded.";
			sendRespond(clientSocket, reply);
			
			in.close();
			out.close();
		} catch (IOException e) {
			reply += "Unable to download file.";
			sendRespond(clientSocket, reply);
		}
	}

	private void sendRespond(Socket clientSocket, String reply) {
		try {
			DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
			byte[] data = reply.getBytes();
			out.writeLong(reply.length());
			out.write(reply.getBytes(), 0, reply.length());
		} catch (Exception e) {
			System.err.println("ERROR: Fail to send your reply.");
		}
	}
}
