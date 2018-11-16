import java.io.*;
import java.net.*;

public class IMG_server implements Runnable {
	public static final int serverPort = 9999;
	@Override
	public void run() {
		try {
			System.out.println("Connecting...");
			ServerSocket serverSocket = new ServerSocket(serverPort);

			while(true) {
				Socket sock = serverSocket.accept();
				String ip = sock.getInetAddress().toString();
				System.out.println("Listening...");
				try {
					BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

					String str = in.readLine();

					File file = new File("/var/www/html/Received_image/", ip+".jpg");
					FileOutputStream output = new FileOutputStream(file);
					byte[] buf = new byte[4000000];

					int readBytes;
					while((readBytes = sock.getInputStream().read(buf)) != -1) {
						output.write(buf, 0, readBytes);
						output.flush();
					}
					in.close();
					output.close();
					System.out.println("received complete");
				}
				catch(Exception e) {
				}
				finally {
					sock.close();
				}
			}
		}
		catch(Exception e) {
		}
	}

	public static void main(String[] argv) {
		Thread TCPserver = new Thread(new IMG_server());
		TCPserver.start();
	}
}
