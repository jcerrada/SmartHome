import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Vector;


public class DataBaseManager { 
	private static final String DATABASE_NAME                = "smarthomedb";
	private static final String DATABASE_USER                = "SHclient";
	private static final String DATABASE_PASSWD              = "SHclient11235";
	private static final String MINUTES_CONSUMPTION_TABLE    = "MinutesConsumption";
	private static final String CUMULATIVE_CONSUMPTION_TABLE = "CumulativeConsumption";
	private static final String ID                           = "id"; 
	private static final String SERIAL_NUMBER                = "serial_number";
	private static final String DATE                         = "date";
	private static final String START_DATE                   = "start_date";
	private static final String END_DATE                     = "end_date";
	private static final String VOLTAGE                      = "voltage";
	private static final String CURRENT                      = "current";
	private static final String APPARENT_POWER               = "apparent_power";
	private static final String ACTIVE_POWER                 = "active_power";
	private static final String ACTIVE_POWERT1               = "active_powerT1";
	private static final String REACTIVE_POWER               = "reactive_power";
	
	private ProtocolMessage   mMessage;
	private Connection        mConnection;
	private PreparedStatement mPrepStatement;
	private ResultSet         mResultSet;
	

	public DataBaseManager() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			mConnection = DriverManager.getConnection("jdbc:mysql://localhost/" + DATABASE_NAME + "?"
			                                        + "user=" + DATABASE_USER + "&password=" + DATABASE_PASSWD);
			
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void storeData(ProtocolMessage message) {
		mMessage = message;
		switch(mMessage.getSubtipo()) {
			case ProtocolMessage.PW:
				storeConsumption();
				break;
			case ProtocolMessage.SG:
				break;
			case ProtocolMessage.ST:
				storeState();
				break;
		}
		//close();
	}

	private void storeConsumption() {
		try {
			String insert  = "INSERT INTO " + MINUTES_CONSUMPTION_TABLE + "( "
					   	   + SERIAL_NUMBER + "," + DATE + "," + VOLTAGE + "," + CURRENT + ","
					   	   + APPARENT_POWER + "," + ACTIVE_POWER + ","  
					   	   + ACTIVE_POWERT1 + "," + REACTIVE_POWER 
					   	   + ") VALUES (?,?,?,?,?,?,?,?)"; 
			mPrepStatement = mConnection.prepareStatement(insert);
			Vector<Long>    tiempos      = mMessage.getTiempos();
			Vector<Integer> voltajes     = mMessage.getVoltajes();
			Vector<Integer> intensidades = mMessage.getIntensidades();
			Vector<Integer> potAparentes = mMessage.getPotenciasApar();
			Vector<Integer> potActivas   = mMessage.getPotenciasAct();
			Vector<Integer> potActivasT1 = mMessage.getPotenciasActT1();
			Vector<Integer> potReactivas = mMessage.getPotenciasReac();
			
			mPrepStatement.setString(1, mMessage.getNumSerie());
			for(int i = 0; i < mMessage.getNumMuestras(); i++) {
				mPrepStatement.setTimestamp(2, new Timestamp(tiempos.get(i)));
				mPrepStatement.setInt(3, voltajes.get(i));
				mPrepStatement.setInt(4, intensidades.get(i));
				mPrepStatement.setInt(5, potAparentes.get(i));
				mPrepStatement.setInt(6, potActivas.get(i));
				mPrepStatement.setInt(7, potActivasT1.get(i));
				mPrepStatement.setInt(8, potReactivas.get(i));
				mPrepStatement.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void storeState() {
		try {
			String insert  = "INSERT INTO " + CUMULATIVE_CONSUMPTION_TABLE + "( "
					   	   + SERIAL_NUMBER + "," + START_DATE + "," + END_DATE + "," + APPARENT_POWER 
					   	   + "," + ACTIVE_POWER + "," + REACTIVE_POWER 
					   	   + ") VALUES (?,?,?,?,?,?)"; 
			mPrepStatement = mConnection.prepareStatement(insert);
			Vector<Long>    tiempos      = mMessage.getTiempos();
			
			mPrepStatement.setString(1, mMessage.getNumSerie());
			mPrepStatement.setTimestamp(2, new Timestamp(tiempos.get(0)));
			mPrepStatement.setTimestamp(3, new Timestamp(tiempos.get(1)));;
			mPrepStatement.setInt(4, mMessage.getPotenciasApar().get(0));
			mPrepStatement.setInt(5, mMessage.getPotenciasAct().get(0));
			mPrepStatement.setInt(6, mMessage.getPotenciasReac().get(0));
			mPrepStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void CreateBaseTables() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/" + DATABASE_NAME + "?"
			                                                  + "user=" + DATABASE_USER 
			                                                  +"&password=" + DATABASE_PASSWD);
			
			String create_table = "CREATE TABLE IF NOT EXISTS " + MINUTES_CONSUMPTION_TABLE + "( "
								+ ID             + " INT AUTO_INCREMENT PRIMARY KEY, "
								+ SERIAL_NUMBER  + " CHAR(12) CHARACTER SET utf8 COLLATE utf8_spanish_ci NOT NULL, "
								+ DATE           + " TIMESTAMP NOT NULL, "
								+ VOLTAGE        + " INT NOT NULL, "
								+ CURRENT        + " INT NOT NULL, "
								+ APPARENT_POWER + " INT NOT NULL, "
								+ ACTIVE_POWER   + " INT NOT NULL, "
								+ ACTIVE_POWERT1 + " INT NOT NULL, "
								+ REACTIVE_POWER + " INT NOT NULL) ";
			PreparedStatement prepStat = connection.prepareStatement(create_table);
			prepStat.execute();
			
			create_table = "CREATE TABLE IF NOT EXISTS " + CUMULATIVE_CONSUMPTION_TABLE + "( "
					+ ID             + " INT AUTO_INCREMENT PRIMARY KEY, "
					+ SERIAL_NUMBER  + " CHAR(12) CHARACTER SET utf8 COLLATE utf8_spanish_ci NOT NULL, "
					+ START_DATE     + " TIMESTAMP NOT NULL, "
					+ END_DATE       + " TIMESTAMP NOT NULL, "
					+ APPARENT_POWER + " INT NOT NULL, "
					+ ACTIVE_POWER   + " INT NOT NULL, "
					+ REACTIVE_POWER + " INT NOT NULL) ";
			prepStat = connection.prepareStatement(create_table);
			prepStat.execute();
			
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void close() {
		try {
			mConnection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//String trama = "ODE01:00055582E3FA PW:00626 T1358366133:237,7283,1730,1722,4714,142 T1358366193:237,7543,1794,1787,4724,119 T1358366253:237,8863,2108,2103,4770,65 T1358366313:238,9235,2197,2189,4794,73 T1358366373:237,9633,2287,2281,4794,78 T1358366433:237,9797,2330,2324,4805,82 T1358366493:237,10461,2485,2478,4842,89 T1358366553:237,11109,2636,2629,4862,93 T1358366613:236,11370,2687,2681,4852,96 T1358366673:236,11522,2721,2715,4870,99 T1358366733:236,11617,2742,2735,4867,99 T1358366793:236,11678,2755,2749,4865,99 T1358366853:236,11763,2777,2772,4888,99 T1358366913:236,11833,2796,2792,4898,102 T1358366973:236,11860,2801,2795,4891,102";
		String trama = "ODE01:00055582E3FA ST:00110 T0:1357850645 T1:1358349703 PW:200850,198877,15605 FW:1.1.1_wifi IP:192.168.1.134";
		ProtocolMessage message = new ProtocolMessage(trama);
		DataBaseManager dbm = new DataBaseManager();
		dbm.storeData(message);
	}

}
