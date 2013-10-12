import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class SmartHomeServer {
	private static final int PORT   = 1730;
	private static final int MAXCON = 100;
	
	private ServerSocket    mServer;
	private Socket          mClient;
	private boolean         mExit;
	private DataBaseManager DBManager;
	
	public SmartHomeServer() {
		mExit = false;
		try {
			mServer = new ServerSocket(PORT, MAXCON);
			DataBaseManager.CreateBaseTables();
			while(!mExit) {
				mClient = mServer.accept();
				System.out.println("New Client!");
				new ClientThread(mClient);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*String h = "Hola,me,llamo,jose,luis";
		String[] uh = h.split("\\,+");
		for(int i=0;i<uh.length;i++)
			System.out.println(uh[i]);*/
		SmartHomeServer server = new SmartHomeServer();
	}
}
