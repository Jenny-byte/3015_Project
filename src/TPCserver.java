import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class TPCserver {
		ServerSocket srvSocket;
		ArrayList<Socket> list = new ArrayList<Socket>();

		public TPCserver(int port) throws IOException {
			srvSocket = new ServerSocket(port);

			while (true) {
				System.out.printf("Listening at port %d...\n", port);
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

			DataInputStream in = new DataInputStream(clientSocket.getInputStream());
			while (true) {
				int len = in.readInt();
				in.read(buffer, 0, len);
				forward(buffer, len, clientSocket);
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
}
