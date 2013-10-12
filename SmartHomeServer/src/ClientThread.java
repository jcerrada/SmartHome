import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;


public class ClientThread implements Runnable {
	private Socket          mClient;
	private BufferedReader  mIn;
	private PrintWriter     mOut;
	private ProtocolMessage mMessage;
	private DataBaseManager mDBManager;

	public ClientThread(Socket client) {
		try {
			mClient    = client;
			mDBManager = new DataBaseManager();
			mIn        = new BufferedReader(new InputStreamReader(mClient.getInputStream()));
			mOut       = new PrintWriter(mClient.getOutputStream(), true);
			
			Thread thread = new Thread(this);
			thread.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String readPacket() {
		try {
			String packet = mIn.readLine();
			return (packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "ERROR READING A PACKET.";
	}
	
	private void sendPacket(String packet) {
		mOut.print(packet + '\n');
		if(mOut.checkError())
			System.out.println("There was an error trying to send the packet: " + packet);
	}

	private void close() {
		try {
			if(mOut != null)
				mOut.close();
			if(mIn != null)
				mIn.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	@Override
	public void run() {
		try {
			while (!mClient.isClosed()) {
				System.out.println("Before receiving packet.");
				mMessage = new ProtocolMessage(readPacket());
				System.out.println("After receiving packet.");
				mMessage.imprimirTrama();
				if (mMessage.getTipo() == ProtocolMessage.TIME) {
					sendPacket(Long.toString(System.currentTimeMillis() / 1000L));
					continue;
				}
				mDBManager.storeData(mMessage);
				sendPacket("ODEOK");
			}
			close();
			//COMPROBAR SI LA TRAMA ES V�LIDA Y SI LO ES CONTESTAMOS AL DISPOSITIVO Y ALMACENAMOS LA TRAMA
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
