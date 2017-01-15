import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.imageio.ImageIO;

public class SendingFrame extends Thread {
	private static int port = 6666;
	private static ServerSocket serverSocket;
	private static Socket socket;
	public BufferedImage frame;

	public void run() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		while (true) {
			try {
				socket = serverSocket.accept();
				OutputStream out = socket.getOutputStream();
				ImageIO.write(frame, "jpg", out);
				socket.close();
			} catch (IOException e) {
				System.out.println(String.format("connection_prob"));
				e.printStackTrace();
			}

		}

	}
}
