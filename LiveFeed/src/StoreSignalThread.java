import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class StoreSignalThread extends Thread {
	
	private int port = 6667;
	
	private static ServerSocket ss;
	
	@Override
	public void run(){
		try {
			ss = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		while(true){
			try {
				Socket s = ss.accept();
				
				InputStream in = s.getInputStream();
				int p = in.read();
				System.out.println("DEBUG: Byte read...");
				if (p==1){
					System.out.println("DEBUG: startStoring signal");
					Main.startStore = true;
				}else if (p==2){
					System.out.println("DEBUG: stopStoring signal");
					Main.stopStore = true;
				}
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
		}
	}
}
