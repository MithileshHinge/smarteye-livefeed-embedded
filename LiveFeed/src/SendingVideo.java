import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class SendingVideo extends Thread {

	public String filepath;
	private String serverIP = "10.4.3.8";
	private int port = 6668;

	@Override
	public void run() {
		Socket s = null;
		FileInputStream fileIn = null;
		try {
			s = new Socket(serverIP, port);
			OutputStream out = s.getOutputStream();
			File file = new File(filepath);
			fileIn = new FileInputStream(file);
			byte[] buffer = new byte[16 * 1024];
			int count;
			while ((count = fileIn.read(buffer)) > 0) {
				out.write(buffer, 0, count);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fileIn != null) fileIn.close();
				if (s != null) s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

}
