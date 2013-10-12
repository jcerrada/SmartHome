import java.awt.image.BufferStrategy;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.text.MaskFormatter;


public class DataGenerator implements Runnable {
	private static final String HOST         = "localhost";
	private static final int    PORT         = 1730;
	
	private Socket         mConnection;
	private BufferedReader mIn;
	private PrintWriter    mOut;
	
	private String          mFileName;
	private String          mIP;
	private String          mFirmware;
	private String          mSerial;
	private int             mType;
	private int             mSubType;
	private int             mLength;
	private Vector<Long>    mTimes         = new Vector<Long>();
	private Vector<Integer> mVoltages      = new Vector<Integer>();
	private Vector<Integer> mCurrents      = new Vector<Integer>();
	private Vector<Integer> mApparentPower = new Vector<Integer>();
	private Vector<Integer> mActivePower   = new Vector<Integer>();
	private Vector<Integer> mActivePowerT1 = new Vector<Integer>();
	private Vector<Integer> mReactivePower = new Vector<Integer>();
	private Vector<String>  mDataPackets   = new Vector<String>();

	public DataGenerator(String filename) {
		mFileName = filename;
		mIP       = "192.168.1.32"; //my ip
		mFirmware = "1.1.1_wifi";   //my ODEnergy's firmware
		mSerial   = "000666803CCA";//my ODEnergy's serial
		mType     = ProtocolMessage.ODEX; //the type of plots we want to generate
		mSubType  = ProtocolMessage.PW; //the subtype of plots we want to generate
		
		try {
			mConnection = new Socket(HOST, PORT);
			mOut        = new PrintWriter(mConnection.getOutputStream(), true);
			mIn         = new BufferedReader(new InputStreamReader(mConnection.getInputStream()));
			
			System.out.println("Los flujos han sido inicializados");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private int generateReactivePower(int prevReacPower, int increment) {
		int baseReacPower = 0,
			maxPower      = 200,
			baseIncrement = Math.abs(increment),
			signum        = (increment < 0)? -1: 1;
		if(prevReacPower == 0) {
			baseReacPower = 200;
			maxPower      = 500;
			baseIncrement = mActivePower.lastElement();
			if(baseIncrement == 0)
				return 0;
		}
		if(prevReacPower < maxPower && signum == -1 && baseIncrement != 0)
			return prevReacPower + (new Random(System.currentTimeMillis()).nextInt() % baseIncrement) / 10;
		if(baseIncrement > maxPower)
			return (int) (baseReacPower + prevReacPower + 100*Math.log10(baseIncrement)*signum);
		return prevReacPower + signum*baseIncrement + (new Random(System.currentTimeMillis()).nextInt() % baseIncrement) / 10;
	}
	
	private int generateReactivePower2(int activePower) {
		float reactivePower = activePower;
		if(reactivePower < 500);
		else if(reactivePower < 1000)
			reactivePower = reactivePower * (1 - (reactivePower - 500)/1000);
		else if(reactivePower < 1500) {
			reactivePower  = reactivePower * ((reactivePower)/2000 - 0.2f*(reactivePower/1300)) + 100;
		}
		else if(reactivePower < 2000) {
			reactivePower  = reactivePower * ((reactivePower - 500)/2000 - 0.25f*(reactivePower/2000));
		}
		else
			reactivePower  = reactivePower * ((reactivePower)/3500 - 0.20f*(reactivePower/2500)) + 100;
		reactivePower += (new Random(System.currentTimeMillis()).nextInt() % reactivePower)/10;
		return (int)reactivePower;
	}
	
	private int generateReactivePower(int activePower) {
		float reactivePower = activePower;
		if(reactivePower < 500)
			reactivePower = (reactivePower * 400) / 500;
		else if(reactivePower < 1000)
			reactivePower = 400 + ((reactivePower - 500) * 200) / 500;
		else if(reactivePower < 1500)
			reactivePower = 600 + ((reactivePower - 1000) * 100) / 500;
		else if(reactivePower < 2000)
			reactivePower = 700 + ((reactivePower - 1500) * 100) / 500 ;
		else if(reactivePower < 2500)
			reactivePower = 800 + ((reactivePower - 2000) * 100) / 500 ;
		else
			reactivePower = 900 + ((reactivePower - 2500) * 100) / 500 ;
		return (int)reactivePower;
	}
	
	private int calculateApparentPower(int activePower, int reactivePower) {
		double actPowerSquare   = Math.pow(activePower, 2), 
			   reactPowerSquare = Math.pow(reactivePower, 2);
		return (int)Math.sqrt(actPowerSquare + reactPowerSquare);
	}
	
	private void clearData() {
		mTimes.clear();
		mVoltages.clear();
		mCurrents.clear();
		mActivePower.clear();
		mActivePowerT1.clear();
		mReactivePower.clear();
		mApparentPower.clear();
	}
	
	private void readFile() {
		BufferedReader in = null;
		try {
			int      activePower, reactivePower, apparentPower;
			String   month = mFileName.split("\\.")[0];
			String[] tokens;
			in            = new BufferedReader(new FileReader(new File(mFileName)));
			String   line = in.readLine();// This instruction if for reading the first line, 'April'
			Calendar c    = Calendar.getInstance();
			switch(month) {
				case "April":	c.set(2013, Calendar.APRIL, 1, 0, 0, 0); break;
				case "May":		c.set(2013, Calendar.MAY, 1, 0, 0, 0); break;
				case "June":	c.set(2013, Calendar.JUNE, 1, 0, 0, 0); break;
				case "July":	c.set(2013, Calendar.JULY, 1, 0, 0, 0); break;
			}
			while((line = in.readLine()) != null) {
				if(line.startsWith("--")) {
					System.out.println(line);
					continue;
				}
				tokens = line.split("\\s+");
				for(int i = 1; i < tokens.length; i++) {
					activePower   = (int)(new Float(tokens[i]) * 1000);
					reactivePower = generateReactivePower(activePower);
					apparentPower = calculateApparentPower(activePower, reactivePower);
					mTimes.add(c.getTimeInMillis());
					mVoltages.add(220);
					mCurrents.add(15);
					mActivePower.add(activePower);
					mActivePowerT1.add(activePower);
					mReactivePower.add(reactivePower);
					mApparentPower.add(apparentPower);
					c.add(Calendar.HOUR_OF_DAY, 1);
				}
				mDataPackets.add(ProtocolMessage.crearTrama(mSerial, mTimes, mVoltages, mCurrents,
						                                    mApparentPower, mActivePower, mActivePowerT1, 
						                                    mReactivePower));
				clearData();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	
	private String readPacket() {
		try {
			return (mIn.readLine() + '\n');
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return "ERROR";
	}
	
	private void sendPacket(String packet) {
		mOut.println(packet);
		if(mOut.checkError()) 
			System.out.println("There was an error sending the packet (client): " + packet);
	}
	
	@Override
	public void run() {
		readFile();
		int numPackets = mDataPackets.size();
		System.out.println("Número de tramas: " + numPackets);
		try {			
			for(int i = 0; i < numPackets; i++) {
				sendPacket(mDataPackets.get(i));
				System.out.println(i + mDataPackets.get(i));
				System.out.println(readPacket());
			}
			mIn.close();
			mOut.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DataGenerator[] dgs =  new DataGenerator[4];
		dgs[0] = new DataGenerator("April.txt");
		dgs[1] = new DataGenerator("May.txt");
		dgs[2] = new DataGenerator("June.txt");
		dgs[3] = new DataGenerator("July.txt");
		for(int i = 0; i < dgs.length; i++) {
			new Thread(dgs[i]).start();
		}
		
	}
}
