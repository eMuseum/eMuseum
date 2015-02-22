package ub.edu.pis2014.pis12.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;

import ub.edu.pis2014.pis12.MainActivity;
import ub.edu.pis2014.pis12.R;
import ub.edu.pis2014.pis12.model.Dades;
import ub.edu.pis2014.pis12.model.Museu;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

/**
 * 
 * @author Muriology S'encarrega d'obtenir les dades de la base de dades en
 *         segon pla.
 * 
 */
public class EMuseumService extends Service {

	// Service intern
	private boolean stopService = false;
	
	// Notifications
	private Resources resources = null;
	private NotificationManager notificationManager = null;
	private NotificationCompat.Builder notificationBuilder = null;
	
	// Download related
	private boolean isDownloading = false;
	private URL url = null;
	private File db = null;
	private DownloadFilesTask down = null;
	private OnDBDownload onDBDownload = null;
	
	// Wifi related
	private boolean scanWifi = false;
	private WifiManager wifiManager = null; 
	private Thread wifiThread = null;
	
	// Modes de seguretat del wifi
	enum WIFI_SECURITY_MODES {
		WEP("WEP"), PSK("PSK"), EAP("EAP"), OPEN("OPEN");

		private final String name;

		private WIFI_SECURITY_MODES(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}
	};

	// Connector
	private final IBinder binder = new LocalBinder();

	@Override
	public void onCreate() {
	}

	/**
	 * Crida't per l'aplicacio per indicar que corri en 2n pla
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int idArrancada) {
		// Obtenim el manager de notificacions
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// Obtenir textos
		resources = getResources();

		// Creem una notificacio que ens servira per tenir en segon pla el
		// servei i que mai
		// es tanqui
		notificationBuilder = new NotificationCompat.Builder(this)
				.setContentText(
						resources.getString(R.string.service_description))
				.setContentTitle(resources.getString(R.string.service_title))
				.setSmallIcon(R.mipmap.notification)
				.setAutoCancel(false)
				.setOngoing(true)
				.setOnlyAlertOnce(true)
				.setContentIntent(
						PendingIntent.getActivity(this, 10, new Intent(this,
								MainActivity.class)
								.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), 0));

		// Comencem en 2n pla
		startForeground(1, notificationBuilder.build());

		// Volem que no es tanqui, i si es tanca, que reinicii
		return START_STICKY;
	}
	
	/**
	 * Para el servei, a menys que hi hagi una descarga en proces.
	 * En aquest ultim cas marca que es tanqui al acabar
	 */
	public void stopOrFlag()
	{
		// Si estem descarregant, marquem
		if (isDownloading)
		{
			stopService = true;
		}
		else
		{
			// Sino, tanquem
			stopSelf();
		}
	}
	
	/**
	 * Metode per descarregar una nova versio de la base de dades
	 * interna de l'aplicacio
	 * 
	 * @param url URL d'on descarregar
	 * @param db Arxiu on guardar
	 * @return Retorna true si ha anat be, false sino
	 */
	public boolean startDBDownload(String url, File db) {
		// Intentem fe run objecte URL
		try {
			this.url = new URL(url);
		} catch (MalformedURLException e) {
		}

		// Guardem l'arxiu
		this.db = db;

		// Si algo no ha anat be, retornem false
		if (url == null || db == null) {			
			return false;
		}

		// Actualitzem la notificacio
		notificationBuilder.setContentText("0% "
				+ resources.getString(R.string.service_downloaded));
		notificationManager.notify(1, notificationBuilder.build());

		// Indiquem que comencem la descarrega
		isDownloading = true;

		// Creo una nova tasca assincronica que correra dins del servei
		down = new DownloadFilesTask() {
			@Override
			protected void onPostExecute(Boolean result) {
				// Marquem que ja no descarreguem
				isDownloading = false;
				
				// Actualitzem la notificacio per dir que ja ha acabat
				notificationBuilder.setContentText("100% "
						+ resources.getString(R.string.service_downloaded));
				notificationManager.notify(1, notificationBuilder.build());

				// Cridem el callback si n'hi ha
				if (onDBDownload != null) {
					onDBDownload.onResult(result);
				}

				// Tanquem si hem de tancar
				if (stopService)
				{
					stopSelf();
				}
			}
		};

		// Executem paralelament
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			down.execute();
		} else {
			down.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}

		return true;
	}

	/**
	 *  Estableix el callback de descarrega
	 *  
	 * @param onDBDownload Callback a cridar quan acabi la descarrega
	 */
	public void setOnDBDownload(OnDBDownload onDBDownload) {
		this.onDBDownload = onDBDownload;
	}

	/**
	 * Comenca i deixa en segon pla l'escaneig de xarxes wifi
	 * 
	 * @return Retorna true si hi ha el wifi activat, false sino
	 */
	public boolean startWifiScanning() {
		// Obtenim el manager del wifi
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		// Comprovem que tinguem wifi
		if (!wifiManager.isWifiEnabled())
			return false;
		
		// Indiquem que volem escanejar
		scanWifi = true;
		
		// Registrem un callback de sistema per cada cop que hi ha una actualitzacio
		// de les xarxes wifi properes
		IntentFilter i = new IntentFilter();
		i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		registerReceiver(wifiReceiver, i);
		
		// Iniciem en un thread en 2n pla l'escaneig de xarxes WIFI
		wifiThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (scanWifi)
				{
					// Indiquem que escanegi
					wifiManager.startScan();
					try {
						// Esperem 5 minuts fins el seguent escaneig
						Thread.sleep(5*60*1000);
					} catch (InterruptedException e) {}
				}
			}
		});
		wifiThread.start();
		
		return true;
	}
	
	BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
		
		// Si rebem algo
		public void onReceive(Context c, Intent i) {
			// Guardem un iterador a la llista de resultats
			Iterator<ScanResult> it = wifiManager.getScanResults().iterator();
			
			// Creem per avancat una configuracio wifi
			WifiConfiguration wifiConfiguration = new WifiConfiguration();

			while (it.hasNext()) {
				ScanResult network = it.next();

				if (stopService)
					return;
				
				// Comprovem si la SSID es d'algun museu
				Museu museu = null;
				ArrayList<Museu> museus = Dades.getMuseus(1, "wifi_ssid=\""
						+ network.SSID + "\"", null);
				if (museus.size() > 0) {
					museu = museus.get(0);
				}

				// Si ho es, intentem connectar
				if (museu != null) {
					String networkPassword = museu.getWifiPassword();

					// Obtenim el mode de seguretat del WIFI
					WIFI_SECURITY_MODES mode = getScanResultSecurity(network, wifiConfiguration);

					// Inidiquem el SSID en la configuracio
					wifiConfiguration.SSID = "\"" + network.SSID + "\"";

					// Segons la seguretat, establim la contrasenya
					if (mode == WIFI_SECURITY_MODES.WEP) {
						wifiConfiguration.wepKeys[0] = "\"" + networkPassword + "\"";
						wifiConfiguration.wepTxKeyIndex = 0;
					} else if (mode == WIFI_SECURITY_MODES.PSK) {
						wifiConfiguration.preSharedKey = "\"" + networkPassword + "\"";
					}

					// Valors per defecte
					wifiConfiguration.hiddenSSID = true;
					wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
					wifiConfiguration.allowedGroupCiphers
							.set(WifiConfiguration.GroupCipher.TKIP);
					wifiConfiguration.allowedGroupCiphers
							.set(WifiConfiguration.GroupCipher.CCMP);
					wifiConfiguration.allowedPairwiseCiphers
							.set(WifiConfiguration.PairwiseCipher.TKIP);
					wifiConfiguration.allowedPairwiseCiphers
							.set(WifiConfiguration.PairwiseCipher.CCMP);
					wifiConfiguration.allowedProtocols
							.set(WifiConfiguration.Protocol.RSN);
					wifiConfiguration.allowedProtocols
							.set(WifiConfiguration.Protocol.WPA);

					// Connectem
					int networkId = wifiManager
							.addNetwork(wifiConfiguration);
					wifiManager.disconnect();
					wifiManager.enableNetwork(networkId, true);
					wifiManager.reconnect();
				}
			}
		}
	};
	
	/**
	 * Retorna el tipus de seguretat d'una xarxa wifi, donat el resultat de l'escaneig
	 * 
	 * @param scanResult Resultat de l'escaneig
	 * @param conf Configuracio on guardar valors de seguretat
	 * @return El tipus de wifi
	 */
	private WIFI_SECURITY_MODES getScanResultSecurity(ScanResult scanResult,
			WifiConfiguration conf) {
		final String cap = scanResult.capabilities;

		// Guardem l'array de valors
		WIFI_SECURITY_MODES[] modes = WIFI_SECURITY_MODES.values();
		
		// Per cada tipus menys OPEN
		for (int i = modes.length - 2; i >= 0; i--) {
			if (cap.contains(modes[i].toString())) {
				WIFI_SECURITY_MODES mode = modes[i];

				// Segons el tipus establim els parametres
				switch (mode) {
				case WEP:
					conf.allowedKeyManagement
							.set(WifiConfiguration.KeyMgmt.NONE);
					conf.allowedGroupCiphers
							.set(WifiConfiguration.GroupCipher.WEP40);
					break;

				case PSK:
					conf.allowedKeyManagement
							.set(WifiConfiguration.KeyMgmt.WPA_PSK);
					break;

				case EAP:
					conf.allowedKeyManagement
							.set(WifiConfiguration.KeyMgmt.NONE);
					break;

				default:
					break;
				}

				return mode;
			}
		}

		// OPEN, no te seguretat
		conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		return WIFI_SECURITY_MODES.OPEN;
	}
	
	/**
	 * Indiquem que cal finalitzar l'escaneig de WIFI
	 */
	public void stopWifiScanning()
	{
		scanWifi = false;
	}

	/**
	 * Quan es tanca el servei cal tancar qualsevol thread actiu
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		// Cancelem descarregues
		if (down != null)
		{
			down.cancel(true);
		}
		
		// Cancelem WIFI
		if (wifiThread != null)
		{
			scanWifi = false;
			unregisterReceiver(wifiReceiver);
			wifiThread.interrupt();
		}
	}

	/**
	 * Teoricament aquest metode serviria per cridar al servei des de una altre
	 * app, cosa que no necessitem.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	/**
	 * Tasca que ens permetra descarregar la nova versio de la base de dades
	 * evitant errors the threats es el que google recomana fer en
	 * http://developer
	 * .android.com/reference/android/os/NetworkOnMainThreadException.html i
	 * http://developer.android.com/training/articles/perf-anr.html
	 * 
	 * @author Cristian
	 * 
	 */
	private class DownloadFilesTask extends AsyncTask<Void, Integer, Boolean> {

		protected Boolean doInBackground(Void... none) {

			InputStream is = null;
			FileOutputStream fos = null;
			try {
				URLConnection urlConnection = url.openConnection();
				urlConnection.connect();
				int totalSize = urlConnection.getContentLength();

				is = urlConnection.getInputStream();
				fos = new FileOutputStream(db);

				byte data[] = new byte[1024];
				int count = 0;// ens determina fins on hem de llegir la data.
				int totalCount = 0;

				String downloaded = resources
						.getString(R.string.service_downloaded);

				// Descarreguem fins al final
				while ((count = is.read(data)) != -1) {
					fos.write(data, 0, count);// escribim

					totalCount += count;
					notificationBuilder
							.setContentText((totalCount * 100 / totalSize)
									+ "% " + downloaded);
					notificationManager.notify(1, notificationBuilder.build());
				}
			} catch (FileNotFoundException e) {
				return false;
			} catch (IOException e) {
				return false;
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
					}
				}

				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
					}
				}
			}

			return true;
		}
	}
		
	/**
	 * Busca si el servei d'EMuseum esta o no corrent
	 * 
	 * @return Retorna true si esta corrent, false sino
	 */
	public static boolean isRuning(Context context)
	{
		ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (EMuseumService.class.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    
	    return false;
	}

	public class LocalBinder extends Binder {
		public EMuseumService getService() {
			// Retorna la instancia del servei
			return EMuseumService.this;
		}
	}
}