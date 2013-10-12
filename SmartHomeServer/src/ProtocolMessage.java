import java.util.StringTokenizer;
import java.util.Vector;


public class ProtocolMessage {
	public static final int ODEX = 0;
	public static final int TIME = 100;
	public static final int PW   = 1;
	public static final int SG   = 2;
	public static final int ST   = 3;
	
	private String          mTrama;
	private String          mIP;
	private String          mFirmware;
	private String          mNumSerie;
	private int             mTipo;
	private int             mSubtipo;
	private int             mLongitud;
	private Vector<Long>    mTiempos;
	private Vector<Integer> mVoltajes;
	private Vector<Integer> mIntensidades;
	private Vector<Integer> mPotenciasApar;
	private Vector<Integer> mPotenciasAct;
	private Vector<Integer> mPotenciasActT1;
	private Vector<Integer> mPotenciasReac;

	public ProtocolMessage(String trama) {
		mTiempos       = new Vector<Long>();
		mVoltajes       = new Vector<Integer>();
		mIntensidades   = new Vector<Integer>();
		mPotenciasApar = new Vector<Integer>();
		mPotenciasAct  = new Vector<Integer>();
		mPotenciasActT1 = new Vector<Integer>();
		mPotenciasReac = new Vector<Integer>();
		mTrama         = trama;
		if(!mTrama.startsWith("ODE")) {
			mTipo = TIME;
			return;
		}
		mTipo     = ODEX;
		mNumSerie = mTrama.substring(6, 18);
		mLongitud = Integer.parseInt(mTrama.substring(22, 27));
		
		//StringTokenizer datos = new StringTokenizer(mTrama.substring(28, mLongitud -1)," ");
		String[] datos = mTrama.substring(28, mLongitud - 1).split("\\s+");
		switch(mTrama.substring(19, 21)) {
			case "PW":
				mSubtipo = PW;
				decoTramaConsumo(datos); 
				break;
			case "SG":
				mSubtipo = SG;
				decoTramaEventos(datos);
				break;
			case "ST":	
				mSubtipo =ST;
				decoTramaEstado(datos);
		}
	}
	
	/**
	 * Crea una trama de tipo consumo a partir de los datos pasados por par�metros.
	 * @return the mNumSerie
	 */
	public static String crearTrama(String numSerie, Vector<Long> tiempos, 
							 Vector<Integer> voltajes, Vector<Integer> intensidades, 
							 Vector<Integer> potenciasApar, Vector<Integer> potenciasAct,
							 Vector<Integer> potenciasActT1, Vector<Integer> potenciasReac) 
	{
		String cabecera = "ODE01:" + numSerie + " PW:"; 
		String datos    = "";
		int    numDatos = tiempos.size();
		for(int i = 0; i < numDatos; i++) {
			datos += " T" + tiempos.get(i)/1000 + ":" + voltajes.get(i)     + "," + intensidades.get(i) + "," +
					 potenciasApar.get(i)  + "," + potenciasAct.get(i) + "," + 
					 potenciasActT1.get(i) + "," + potenciasReac.get(i);
		}
		datos += "\r";
		return String.format(cabecera + "%05d" + datos, cabecera.length() + 5 + datos.length());
	}
	
	/**
	 * Crea una trama de tipo evento a partir de los datos pasados por par�metros.
	 * @return the mNumSerie
	 */
	public static String crearTrama(String numSerie, Vector<Long> tiempos,
			 				 Vector<Integer> potenciasApar, Vector<Integer> potenciasAct,
			 				 Vector<Integer> potenciasActT1, Vector<Integer> potenciasReac) 
	{
		String cabecera = "ODE01:" + numSerie + " SG:";
		String datos    = "";
		int    numDatos = tiempos.size();
		for(int i = 0; i < numDatos; i++) {
			datos += " EV:" + tiempos.get(i)/1000 + "," + potenciasApar.get(i)  + "," + 
					   potenciasAct.get(i)  + "," + potenciasActT1.get(i) + "," + 
					   potenciasReac.get(i) + ",-0";
		}
		datos += "\r";
		return String.format(cabecera + "%05d" + datos, cabecera.length() + 5 + datos.length());
	}
	
	/**
	 * Crea una trama de tipo estado a partir de los datos pasados por par�metros.
	 * @return the mNumSerie
	 */
	public static String crearTrama(String numSerie, Vector<Long> tiempos,
			 				 Integer potenciaAparAcum, Integer potenciaActAcum,
			 				 Integer potenciaReacAcum, String firmware, String ip) 
	{
		String cabecera = "ODE01:" + numSerie + " ST:";
		String datos    = " T0:" + tiempos.get(0)/1000 + " T1:" + tiempos.get(1)/1000 +
						  " PW:" + potenciaAparAcum + "," + potenciaActAcum +
						  "," + potenciaReacAcum + " FW:" + firmware + " IP:" + ip + "\r";
		return String.format(cabecera + "%05d" + datos, cabecera.length() + 5 + datos.length());
	}
	
	/**
	 * @return the mNumSerie
	 */
	public String getNumSerie() {
		return mNumSerie;
	}

	/**
	 * @return the mTipo
	 */
	public int getTipo() {
		return mTipo;
	}

	/**
	 * @return the mSubtipo
	 */
	public int getSubtipo() {
		return mSubtipo;
	}

	/**
	 * @return the mTiempos
	 */
	public Vector<Long> getTiempos() {
		return mTiempos;
	}

	/**
	 * @return the mVoltajes
	 */
	public Vector<Integer> getVoltajes() {
		return mVoltajes;
	}

	/**
	 * @return the mIntensidades
	 */
	public Vector<Integer> getIntensidades() {
		return mIntensidades;
	}

	/**
	 * @return the mPotenciasApar
	 */
	public Vector<Integer> getPotenciasApar() {
		return mPotenciasApar;
	}

	/**
	 * @return the mPotenciasAct
	 */
	public Vector<Integer> getPotenciasAct() {
		return mPotenciasAct;
	}

	/**
	 * @return the mPotenciasActT1
	 */
	public Vector<Integer> getPotenciasActT1() {
		return mPotenciasActT1;
	}

	/**
	 * @return the mPotenciasReac
	 */
	public Vector<Integer> getPotenciasReac() {
		return mPotenciasReac;
	}
	
	/**
	 * @return the mIP
	 */
	public String getIP() {
		return mIP;
	}

	/**
	 * @return the mFirmware
	 */
	public String getFirmware() {
		return mFirmware;
	}

	public int getNumMuestras() {
		return mTiempos.size();
	}

	private void decoTramaConsumo(String[] datos) {
		for(int i = 0; i < datos.length; i++) {
			mTiempos.add(Long.valueOf(datos[i].substring(1, 11))*1000);
			String[] datos_aux = datos[i].substring(12, datos[i].length()).split("\\,+");
			mVoltajes.add(Integer.valueOf(datos_aux[0]));
			mIntensidades.add(Integer.valueOf(datos_aux[1]));
			mPotenciasApar.add(Integer.valueOf(datos_aux[2]));
			mPotenciasAct.add(Integer.valueOf(datos_aux[3]));
			mPotenciasActT1.add(Integer.valueOf(datos_aux[4]));
			mPotenciasReac.add(Integer.valueOf(datos_aux[5]));
		}
	}
	
	private void decoTramaEventos(String[] datos) {
		for(int i = 0; i < datos.length; i++) {
			String[] datos_aux = datos[i].substring(3, datos[i].length()).split("\\,+");
			mTiempos.add(Long.valueOf(datos_aux[0])*1000); //multiplicamos por 1000 pra darle precision micro
			mPotenciasApar.add(Integer.valueOf(datos_aux[1]));
			mPotenciasAct.add(Integer.valueOf(datos_aux[2]));
			mPotenciasActT1.add(Integer.valueOf(datos_aux[3]));
			mPotenciasReac.add(Integer.valueOf(datos_aux[4]));
		}
	}

	private void decoTramaEstado(String[] datos) {
		mTiempos.add(Long.valueOf(datos[0].substring(3, 13))*1000); //multiplicamos por 1000 pra darle precision micro
		mTiempos.add(Long.valueOf(datos[1].substring(3, 13))*1000); //timestamp 10 digits vs 13 digits
		String[] datos_aux = datos[2].substring(3, datos[2].length()).split("\\,+");
		mPotenciasApar.add(Integer.valueOf(datos_aux[0]));
		mPotenciasAct.add(Integer.valueOf(datos_aux[1]));
		mPotenciasReac.add(Integer.valueOf(datos_aux[2]));
		mFirmware = datos[3].substring(3, datos[3].length());
		mIP       = datos[4].substring(3, datos[4].length());
	}
	
	public void imprimirTrama() {
		System.out.println(mTrama);
	}
	
	public static void main(String[] args) {
		/*//String trama = "ODE01:00055582E3FA PW:00626 T1358366133:237,7283,1730,1722,4714,142 T1358366193:237,7543,1794,1787,4724,119 T1358366253:237,8863,2108,2103,4770,65 T1358366313:238,9235,2197,2189,4794,73 T1358366373:237,9633,2287,2281,4794,78 T1358366433:237,9797,2330,2324,4805,82 T1358366493:237,10461,2485,2478,4842,89 T1358366553:237,11109,2636,2629,4862,93 T1358366613:236,11370,2687,2681,4852,96 T1358366673:236,11522,2721,2715,4870,99 T1358366733:236,11617,2742,2735,4867,99 T1358366793:236,11678,2755,2749,4865,99 T1358366853:236,11763,2777,2772,4888,99 T1358366913:236,11833,2796,2792,4898,102 T1358366973:236,11860,2801,2795,4891,102";
		//String trama = "ODE01:00055582E3FA ST:00110 T0:1357850645 T1:1358349703 PW:200850,198877,15605 FW:1.1.1_wifi IP:192.168.1.134";
		String trama = "ODE01:00055582E3FA SG:00126 EV:1358369262,-54,-63,-318,-3,-3 EV:1358369315,-62,-67,-3,-4,-2 EV:1358369329,-56,-58,-397,-13,-8";
		ProtocolMessage message = new ProtocolMessage(trama);
		System.out.println(message.mTipo);
		System.out.println(message.mSubtipo);
		System.out.println(message.mNumSerie);
		System.out.println(message.mLongitud);
		System.out.println(message.mTiempos);
		//System.out.println(message.mVoltajes);
		//System.out.println(message.mIntensidades);
		System.out.println(message.mPotenciasApar);
		System.out.println(message.mPotenciasAct);
		//System.out.println(message.mPotenciasActT1);
		System.out.println(message.mPotenciasReac);
		System.out.println(message.mFirmware);
		System.out.println(message.mIP);*/
		//String trama = "ODE01:00055582E3FA PW:00626 T1358366133:237,7283,1730,1722,4714,142 T1358366193:237,7543,1794,1787,4724,119 T1358366253:237,8863,2108,2103,4770,65 T1358366313:238,9235,2197,2189,4794,73 T1358366373:237,9633,2287,2281,4794,78 T1358366433:237,9797,2330,2324,4805,82 T1358366493:237,10461,2485,2478,4842,89 T1358366553:237,11109,2636,2629,4862,93 T1358366613:236,11370,2687,2681,4852,96 T1358366673:236,11522,2721,2715,4870,99 T1358366733:236,11617,2742,2735,4867,99 T1358366793:236,11678,2755,2749,4865,99 T1358366853:236,11763,2777,2772,4888,99 T1358366913:236,11833,2796,2792,4898,102 T1358366973:236,11860,2801,2795,4891,102";
		//String trama = "ODE01:00055582E3FA SG:00126 EV:1358369262,-54,-63,-318,-3,-3 EV:1358369315,-62,-67,-3,-4,-2 EV:1358369329,-56,-58,-397,-13,-8";
		String trama = "ODE01:00055582E3FA ST:00110 T0:1357850645 T1:1358349703 PW:200850,198877,15605 FW:1.1.1_wifi IP:192.168.1.134";
		ProtocolMessage message = new ProtocolMessage(trama);
		System.out.println(ProtocolMessage.crearTrama(message.getNumSerie(), message.getTiempos(), message.getPotenciasApar().get(0), message.getPotenciasAct().get(0), message.getPotenciasReac().get(0), message.getFirmware(), message.getIP()));
	}
}
