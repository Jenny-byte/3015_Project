import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class TPCclient {
	boolean loginValid = false;
	private Object retrun;

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

					respondLogin(socket, receivedData);
					String data = receivedData.trim();
					String dataArray[] = data.split(" ");
					String commend = dataArray[0];
					if (commend.equals("downloadReady")) {
						download(socket);
					} else {
						System.out.println(receivedData.substring(commend.length() + 1, receivedData.length()));
					}
					if (loginValid == true)
						System.out.println("Please enter the commend:");

				}
			} catch (IOException ex) {
				System.err.println("Connection dropped!");
				System.exit(-1);
			}
		});
		t.start();

		Scanner scanner = new Scanner(System.in);
		String commend = "";

		while (!loginValid) {
			commend = login();
			sendRequest(socket, commend);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// System.out.println(loginValid);
		}

		while (true) {
			commend = scanner.nextLine();
			String data = commend.trim();
			String dataArray[] = data.split(" ");
			if (dataArray[0].equals("upload")) {
				if (dataArray.length < 2) {
					System.out.println("You are missing the path of the file.");
					System.out.println("Please enter the commend:");
				} else {
					sendRequest(socket, dataArray[0]);
					upload(socket, dataArray[1]);
				}
			} else {
				sendRequest(socket, commend);
			}
		}
	}

	private void upload(Socket socket, String filename) {
		try {
			Thread.sleep(500);
			File file = new File(filename);

			if (!file.exists()) {
				System.err.println("File " + filename + " doesn't exist.");
				return;
			}
			if (file.isDirectory()) {
				System.err.println(filename + " is a directory which can't be uploaded.");
				return;
			}

			DataOutputStream out = new DataOutputStream(socket.getOutputStream());

			// file name
			out.writeInt(file.getName().length());
			out.write(file.getName().getBytes());

			// file
			long size = file.length();
			out.writeLong(size);

			FileInputStream in = new FileInputStream(file);
			System.out.println(file.getName() + " (size: " + size + "B) is uploading");

			byte[] buffer = new byte[1024];
			while (size > 0) {
				int len = in.read(buffer, 0, buffer.length);
				out.write(buffer, 0, len);
				size -= len;
			}
			in.close();
			return;

		} catch (Exception e) {
			System.out.println("Fail to upload the file");
		}
	}

	private void download(Socket socket) {
		byte[] buffer = new byte[1024];

		try {
			DataInputStream in = new DataInputStream(socket.getInputStream());

			int nameLen = in.readInt();
			in.read(buffer, 0, nameLen);
			String name = new String(buffer, 0, nameLen);

			long size = in.readLong();

			File file = new File(name);
			FileOutputStream out = new FileOutputStream(file);

			System.out.print("Downloading file " + name);
			while (size > 0) {
				int len = in.read(buffer, 0, buffer.length);
				out.write(buffer, 0, len);
				size -= len;
				System.out.print(".");
			}

			System.out.println("\nFile " + name + " download completed");
			out.close();
			return;

		} catch (IOException e) {
			System.out.println("\nFail to download the file.");
		}
	}

	public String login() {
		System.out.println("Please enter your username:");
		Scanner scanner = new Scanner(System.in);
		String username = scanner.nextLine().trim();

		System.out.println("Please enter your password:");
		String password = scanner.nextLine().trim();

		String request = "login " + username + " " + password;
		return request;
	}

	public void respondLogin(Socket socket, String receivedData) {
		String data = receivedData.trim();
		String dataArray[] = data.split(" ");
		String commend = dataArray[0];

		if (commend.equals("login")) {
			if (dataArray[1].equals("Successful")) {
				loginValid = true;
			}
		}

	}

	private void sendRequest(Socket socket, String request) {
		try {
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			byte[] data = request.getBytes();
			out.writeLong(request.length());
			out.write(request.getBytes(), 0, request.length());
		} catch (Exception e) {
			System.out.println("Fail to send your request.");
		}
	}

}
