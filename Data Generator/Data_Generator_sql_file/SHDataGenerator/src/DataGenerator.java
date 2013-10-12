import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Vector;


public class DataGenerator {
	private static final String FILENAME     = "consumption.txt";
	private static final String SQL_FILENAME = "consumption.sql";
	
	//DATABASE 
	private static final String MINUTES_CONSUMPTION_TABLE    = "MinutesConsumption";
	private static final String SERIAL_NUMBER                = "serial_number";
	private static final String DATE                         = "date";
	private static final String VOLTAGE                      = "voltage";
	private static final String CURRENT                      = "current";
	private static final String APPARENT_POWER               = "apparent_power";
	private static final String ACTIVE_POWER                 = "active_power";
	private static final String ACTIVE_POWERT1               = "active_powerT1";
	private static final String REACTIVE_POWER               = "reactive_power";
	
	private BufferedReader mIn  = null;
	private BufferedWriter mOut = null;
	
	private String            mFirmware;
	private String            mIP;
	private String            mSerial;
	private int               mType;
	private int               mSubType;
	private int               mLength;
	private Vector<Timestamp> mTimes         = new Vector<Timestamp>();
	private Vector<Integer>   mVoltages      = new Vector<Integer>();
	private Vector<Integer>   mCurrents      = new Vector<Integer>();
	private Vector<Integer>   mApparentPower = new Vector<Integer>();
	private Vector<Integer>   mActivePower   = new Vector<Integer>();
	private Vector<Integer>   mActivePowerT1 = new Vector<Integer>();
	private Vector<Integer>   mReactivePower = new Vector<Integer>();

	public DataGenerator() {
		mIP       = "192.168.1.32"; //my ip
		mFirmware = "1.1.1_wifi";   //my ODEnergy's firmware
		mSerial   = "000666803CCA";//my ODEnergy's serial
		mType     = ProtocolMessage.ODEX; //the type of plots we want to generate
		mSubType  = ProtocolMessage.PW; //the subtype of plots we want to generate
		

		try {
			//Opening for reading the consumption.txt file
			mIn  = new BufferedReader(new FileReader(new File(FILENAME)));
			
			//Removing previous sql file
			Path sql_file = Paths.get(SQL_FILENAME);
			Files.deleteIfExists(sql_file);
		
			//Creating new sql file
			mOut = Files.newBufferedWriter(sql_file, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	
	private void writeHeader() {
		String header = "START TRANSACTION;";
		try {
			mOut.append(header);
			mOut.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void writeData() {
		int numPackets = mTimes.size();
		try {
			for(int i = 0; i < numPackets; i++) {
				String insert  = "INSERT INTO " + MINUTES_CONSUMPTION_TABLE + "( "
								 + SERIAL_NUMBER + "," + DATE + "," + VOLTAGE + "," + CURRENT + ","
								 + APPARENT_POWER + "," + ACTIVE_POWER + ","  
								 + ACTIVE_POWERT1 + "," + REACTIVE_POWER 
								 + ") VALUES ('" + mSerial + "','" + mTimes.get(i) + "'," + mVoltages.get(i) 
								 + "," + mCurrents.get(i) + "," + mApparentPower.get(i) + ","
								 + mActivePower.get(i) + "," + mActivePowerT1.get(i) + "," 
								 + mReactivePower.get(i)+ ");";
				mOut.append(insert);
				mOut.newLine();
			}
			mOut.flush();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void writeFooter() {
		String footer = "COMMIT;";
		try {
			mOut.append(footer);
			mOut.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
	
	private void closeStreams() {
		try {
			if (mIn != null)
				mIn.close();
			if (mOut != null)
				mOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void generateData() {
		int            activePower, reactivePower, apparentPower;
		String         line;
		String[]       tokens;
		Calendar       c   = Calendar.getInstance();
		c.set(2013, Calendar.APRIL, 1, 0, 0, 0);
		try {
			writeHeader();
			while((line = mIn.readLine()) != null) {
				if(line.startsWith("--")) {
					System.out.println(line);
					continue;
				}
				tokens = line.split("\\s+");
				for(int i = 1; i < tokens.length; i++) {
					activePower   = (int)(new Float(tokens[i]) * 1000);
					reactivePower = generateReactivePower(activePower);
					apparentPower = calculateApparentPower(activePower, reactivePower);
					for(int j = 0; j < 60; j++) {
						mTimes.add(new Timestamp(c.getTimeInMillis()));
						mVoltages.add(220);
						mCurrents.add(15);
						mActivePower.add(activePower);
						mActivePowerT1.add(activePower);
						mReactivePower.add(reactivePower);
						mApparentPower.add(apparentPower);
						c.add(Calendar.MINUTE, 1);
					}
					writeData();
					clearData();
				}
			}
			writeFooter();
			closeStreams();
			System.out.println("The data has been generated. Import the generated sql file to the 'smarthomedb' database.");
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DataGenerator dg =  new DataGenerator();
		dg.generateData();
	}
}
