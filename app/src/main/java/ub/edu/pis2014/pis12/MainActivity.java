package ub.edu.pis2014.pis12;

import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.Callable;

import ub.edu.pis2014.pis12.controlador.API;
import ub.edu.pis2014.pis12.controlador.APINotifier;
import ub.edu.pis2014.pis12.controlador.APIOperation.TASK_TYPE;
import ub.edu.pis2014.pis12.controlador.ScreenSlidePagerAdapter;
import ub.edu.pis2014.pis12.model.Dades;
import ub.edu.pis2014.pis12.model.TIPUS_ELEMENT;
import ub.edu.pis2014.pis12.model.Usuari;
import ub.edu.pis2014.pis12.utils.EMuseumService;
import ub.edu.pis2014.pis12.utils.Utils;
import ub.edu.pis2014.pis12.utils.WifiScanConnection;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

/**
 * FramentActivity inicial de l'aplicaci� EMuseum
 * �nicament ens serveix pel ViewPager, intercanviem Fragments.
 * 
 */
@SuppressLint("ResourceAsColor")
public class MainActivity extends FragmentActivity
{
	public static final int ACTIVITY_CREATE =1;
	
	public static boolean mostrantObra = false;
	private static boolean appInicialitzada = false;
	private static boolean preventDestroy = false;
	
	// Pager i PagerAdapter per tal de mostrar els Fragments
	private long backCount = 0;
	private ViewPager pager;
	private ScreenSlidePagerAdapter pagerAdapter;
	private int tipoIdioma;
	
	/**
	 * Constructor de la classe
	 * 
	 * Inicialitza i posa valors per defecte a alguns Widgets de l'Activity
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		
		super.onCreate(savedInstanceState);
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		String idioma=settings.getString("Language_Code", getResources().getConfiguration().locale.toString().substring(0,2));
		if(idioma!=getResources().getConfiguration().locale.toString().substring(0,2)){
			Configuration config=new Configuration();
			Locale locale=new Locale(idioma);
			Locale.setDefault(locale);
			config.locale = locale;
			getApplicationContext().getResources().updateConfiguration(config, null);	
		}
		
		/*
		 * Si estem en mode normal i no en una tablet i en landscape,
		 * mostrem el xml normal, d'altre forma mostrem el xml per tablet,
		 * que permet tenir m�s d'un fragment per activity
		 */
		int resourceID = R.layout.activity_main;
		if (Utils.isExpandedMode(this))
		{
			resourceID = R.layout.activity_main_tablet;
		}
		
		setContentView(resourceID);
		
		// Inicialitzem les dades
		if (!appInicialitzada)
		{
			appInicialitzada = true;
			Context context = getApplicationContext();
			
			Dades.initialize(context);
			
			// A menys que l'usuari ho hagi desactivat
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			if (preferences.getBoolean("autoconnect_wifi", true))
			{
				// Iniciem el servei per estar pendent de canvis en el wifi
				WifiScanConnection connection = new WifiScanConnection(context){
		
					@Override
					protected void onConnected() {
						// Indiquem que comenci a escanejar
				        service.startWifiScanning();
					}
				};
				
				// Creem el servei a menys que ja estigui creat
				Intent i = new Intent(this, EMuseumService.class);
				context.startService(i);
				
				// Intentem lligar-nos al servei
				context.bindService(i, connection, Context.BIND_AUTO_CREATE);
			}
		}
		
		fillActivity(false);
		
		tipoIdioma=0;
	}
	
	/**
	 * Permet buscar actualitzacions de la base de dades de museus, autors i obres mitjan�ant la
	 * API
	 * 
	 * Es situa en l'activity i no en el fragment degut a que aixi permetem el seu us en el menu
	 * contextual d'opcions i no solament en la primera carrega del fragment
	 */
	public static void buscarActualitzacions(final Activity context, final Callable<Void> onEnd)
	{
		// Mostra per sobre la pantalla de carrega
		final View loadingLayout = context.findViewById(R.id.api_loading);
		final TextView loadingText = (TextView) context.findViewById(R.id.api_loading_text);
		loadingLayout.setVisibility(View.VISIBLE);
		loadingText.setText(R.string.API_loading_db);
		
		// Intenta actualitzar la versi�
		API.StartOperation(context, TASK_TYPE.API_CHECK_VERSION, new APINotifier() {
			@Override
			public void onResult(boolean result) {
				// Amaga la pantalla de carrega
				loadingLayout.setVisibility(View.GONE);
				
				if (result)
				{
					// Pregunta si vol o no actualitzar
					new AlertDialog.Builder(context)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(R.string.main_actualitzar_title)
					.setMessage(R.string.main_actualitzar_msg)
					.setPositiveButton(R.string.dialog_si, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// Si la resposta es s�, inicia la desc�rrega
							API.StartOperation(context, TASK_TYPE.API_DOWNLOAD_DB, null);
							loadingLayout.setVisibility(View.VISIBLE);
							loadingText.setText(R.string.API_downloading_db);
						}
					})
					.setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {

							if (onEnd != null)
							{
								try {
									onEnd.call();
								} catch (Exception e) {}
							}
						}
					})
					.show();
				}
				else if (onEnd != null)
				{
					try {
						onEnd.call();
					} catch (Exception e) {}
				}					
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (preventDestroy)
		{
			preventDestroy = false;
			return;
		}

		// Marcar per si tornem a iniciar
		appInicialitzada = false;
		
		// Intentem parar el servei, a menys que no estigui iniciat
		Intent i = new Intent(this, EMuseumService.class);
		if (EMuseumService.isRuning(this))
		{		
			// Connexio al servei
			WifiScanConnection connection = new WifiScanConnection(this){
	
				@Override
				protected void onConnected() {
					// El parem ja o indiquem que pari quan acabi de descarregar
					service.stopOrFlag();
				}
			};
	
			// Intentem lligar-nos al servei
			if (!bindService(i, connection, Context.BIND_AUTO_CREATE))
			{
				// Sino podem el parem
				stopService(i);
			}
		}
		
		// Tancar la DB
		Dades.close();
	}

	/**
	 * Permet gestionar el click a la fletxa de retorn.
	 * En aquest cas, retornem a la llista de museus si no hi som.
	 * 
	 */
	@Override
	public void onBackPressed()
	{
		if (!Utils.isExpandedMode(this) && pager.getCurrentItem() != 1)
		{
			backCount = 0 ;
			pager.setCurrentItem(1);
			
		}
		// En cas d'estar mostrant la imatge en mode expandid, tornem enrere
		else if (tabletImageExpanded())
		{
			// No fem res... cutre pero funciona
		}
		else if (Utils.isExpandedMode(this) && !(getSupportFragmentManager().findFragmentById(R.id.main_left_fragment) instanceof MainFragment))
		{
			backCount = 0;
			mostrantObra = false;
			
			// Si estem en mode tablet i la part esquerra no mostra la llista de museus,
			// vol dir que estem a la llista d'obres. Mostrem els museus.
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.replace(R.id.main_left_fragment, new MainFragment()).commit();
		}
		else
		{
			// Deixem 3 segons per tornar a premer
			long time = (new Date()).getTime();
			if(time - backCount > 3000)
			{
				backCount = time;
		    	Toast.makeText(this, R.string.press_back_again, Toast.LENGTH_SHORT).show();
			}
			else
			{
				backCount = 0;
				
				Intent intent = new Intent(MainActivity.this, SplashActivity.class);
				intent.putExtra("EXIT", true);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		}
	}
	
	private boolean tabletImageExpanded()
	{
		Fragment fragment = pagerAdapter.getItem(1);
		if (Utils.isExpandedMode(this) && fragment instanceof InfoFragment)
		{			
			InfoFragment infoFragment = (InfoFragment)fragment;
			if (infoFragment.imageExpanded())
			{
				infoFragment.returnToInfo();
				return true; 
			}
		}
		
		return false;
	}

	/**
	 * Inicialitza el menu
	 * 
	 * @param menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	public boolean onOptionsItemSelected(MenuItem item){
		Intent intent;
		switch(item.getItemId())
		{		
			case R.id.action_settings:
				intent = new Intent(MainActivity.this, SettingsActivity.class);
				startActivity(intent);
				return true;
		
			case R.id.action_ajuda:
				intent = new Intent(MainActivity.this, AjudaActivity.class);
		    	startActivity(intent);
		    	Toast.makeText(this, R.string.action_ajuda, Toast.LENGTH_SHORT).show();
				return true;
				
			case R.id.action_buscar_actualitzacio:
				buscarActualitzacions(this, null);
				return true;

			case R.id.action_tancar_sessio:
				Usuari.get().logout();
				intent = new Intent(MainActivity.this, SplashActivity.class);
				intent.putExtra("skipLogin", true);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				return true;
			
			case R.id.action_iniciar_sessio:
				Usuari.get().logout();
				intent = new Intent(MainActivity.this, SplashActivity.class);
				intent.putExtra("skipLogin", true);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				return true;
			
			case R.id.action_canviar_idioma://Canviar Vista
	  			//
	  			AlertDialog.Builder builderIdioma = new AlertDialog.Builder(this);
	  			
	  			Resources resources2 = getResources();
	  			
	  			String[] llenguatges = resources2.getStringArray(R.array.app_language);
	  			builderIdioma.setTitle("Selecciona el idioma:");
	  			//Mostrem el dialog amb els diferents tipus de mapa que l'usuari pot escollir per mostrar-ho.
	  			builderIdioma.setSingleChoiceItems(llenguatges, Utils.getIdioma(), new DialogInterface.OnClickListener() {
	  				
	  				@Override
	  				public void onClick(DialogInterface dialog, int which) {
	  					//Guardem en un variable el tipu de mapa que ha escollit l'usuari.
	  					tipoIdioma=which;
	  				}
	  			});
	  			//Apliquem el canvi
	  			builderIdioma.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
	  			@Override
	  				public void onClick(DialogInterface dialog, int id) {
	  			// User clicked OK, so save the result somewhere
	  			// or return them to the component that opened the dialog
		  				Configuration config = new Configuration();
	  					Locale locale=null;
	  					switch(tipoIdioma){
	  						case 0:
	  							locale = new Locale("ca"); 
	  							Locale.setDefault(locale);
	  							config.locale = locale;
	  							getApplicationContext().getResources().updateConfiguration(config, null);
	  							break;
	  						case 1:
	  							locale = new Locale("es"); 
	  							Locale.setDefault(locale);
	  							config.locale = locale;
	  							getApplicationContext().getResources().updateConfiguration(config, null);
	  							break;
	  						case 2:
	  							locale = new Locale("zh"); 
	  							Locale.setDefault(locale);
	  							config.locale = locale;
	  							getApplicationContext().getResources().updateConfiguration(config, null);
	  							break;
	  						case 3:
	  							locale = new Locale("en"); 
	  							Locale.setDefault(locale);
	  							config.locale = locale;
	  							getApplicationContext().getResources().updateConfiguration(config, null);
	  							break;
	  						default:
	  							break;
	  					}
	  					preventDestroy=true;
	  					SharedPreferences settings =PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	  					SharedPreferences.Editor editor=settings.edit();
	  					editor.putString("Language_Code", locale.getLanguage());
	  					editor.commit();
						finish();
	  					startActivity(getIntent());
	  				}
	  			});
	  			builderIdioma.setNegativeButton(R.string.dialog_cancelar, new DialogInterface.OnClickListener() {
	  			@Override
	  				public void onClick(DialogInterface dialog, int id) {
	  					dialog.cancel();
	  				}
	  			});

	  			AlertDialog dialogIdioma=builderIdioma.create();
	  			dialogIdioma.show();
	  			return true;
				
			default:
				return super.onOptionsItemSelected(item);			
		}
	}
	
	@Override
    public void onConfigurationChanged(Configuration newConfig) 
    {
        super.onConfigurationChanged(newConfig);

        int resourceID = R.layout.activity_main;
		if (Utils.isExpandedMode(this))
		{
			resourceID = R.layout.activity_main_tablet;
		}
		
		setContentView(resourceID);
		fillActivity(true);
    }
	
	public boolean onPrepareOptionsMenu(Menu menu) {
	    if (!Usuari.get().isLogged()){
	    	MenuItem item = menu.findItem(R.id.action_tancar_sessio);
	        item.setVisible(false);
	    }else{
	    	MenuItem item = menu.findItem(R.id.action_iniciar_sessio);
	        item.setVisible(false);
	    }
		return true;
	}
	
	private void fillActivity(boolean orientationChange)
	{
		// Inicialitzem el Pager
		pager = (ViewPager) findViewById(R.id.main_pager);
		
		// Creem l'adapter
		pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
		
		// Si hi ha hagut un canvi d'orientacio
		if (orientationChange)
		{			
			// Hem de canviar d'activity en cas de que el que actualment estiguem mirant
			// Sigui diferent de la llista d'obres
			// Guardem ara ja que tot seguit els borrarem del ViewPager
			Fragment portrait = null;
			
			Iterator<Fragment> it = getSupportFragmentManager().getFragments().iterator();
			while (it.hasNext())
			{
				Fragment fragment = it.next();
				
				if (fragment instanceof InfoFragment ||
							fragment instanceof LlistaObresFragment || 
							fragment instanceof TabletIniciFragment ||
							fragment instanceof MainFragment)
					portrait = fragment;
				else if (!(fragment instanceof CameraFragment))
					continue;
					
				FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
				trans.remove(fragment).commitAllowingStateLoss();
			}
			
			// Canvi de landscape a vertical?
			if (!Utils.inLandscape(this) && portrait != null)
			{				
				// Solament mostrem la infoObra si es la d'una obra, no museu
				if (portrait instanceof InfoFragment && ((InfoFragment)portrait).canBeSwitched() && mostrantObra)
				{
					// Si es tracta d'una obra, la mostrem
					Intent i = new Intent (MainActivity.this, InfoActivity.class);
					i.putExtras(portrait.getArguments());
					i.putExtra("fromMain", true);
					startActivityForResult(i, ACTIVITY_CREATE);
				}
				else
				{
					// Cas de llista d'obres, ja que si es de museus tot el que hem de fer
					// es canviar el fragment
					portrait = getSupportFragmentManager().findFragmentById(R.id.main_left_fragment);
					if (portrait instanceof LlistaObresFragment)
					{
						Intent i = new Intent (MainActivity.this, LlistaObresActivity.class);
						i.putExtras(portrait.getArguments());
						startActivityForResult(i, ACTIVITY_CREATE);
					}
				}
			}
		}

		// Afegim els elements al ViewPager
		pagerAdapter.addItem(new CameraFragment());
		if (!Utils.isExpandedMode(this))
		{
			// Si no estem en mode tablet, el del mig es la llista de museus
			pagerAdapter.addItem(new MainFragment());
		}
		else
		{
			Fragment left = null;
			// Hem de comprobar tamb� que el canvi no sigui des d'aqu� mateix, que significa que est�vem
			// mirant la llista de museus
			if (!orientationChange && getIntent() != null && getIntent().getExtras() != null)
			{
				// Si venim de portrait i tenim ja alguna obra/llista oberta
				pagerAdapter.addItem(InfoFragment.newInstance(getIntent().getExtras(), false));
				
				Bundle args = getIntent().getExtras();
				if (args.getSerializable("tipus") == TIPUS_ELEMENT.TIPUS_OBRA)
				{
					args.putSerializable("tipus", TIPUS_ELEMENT.TIPUS_MUSEU);
					args.putInt("id", Dades.getObra(args.getInt("id")).getMuseuId());
				}
				left = LlistaObresFragment.newInstance(args);
			}
			else
			{
				// Sino, el del mig es l'ajuda/informacio
				pagerAdapter.addItem(new TabletIniciFragment());
				left = new MainFragment();
			}
			
			// Mostrem en l'esquerra la llista de museus
			FragmentManager manager = getSupportFragmentManager();			
	        FragmentTransaction ft = manager.beginTransaction();
            //ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
	        ft.replace(R.id.main_left_fragment, left);
            ft.commitAllowingStateLoss();
		}
		
		// Establim l'adapter
		pager.setAdapter(pagerAdapter);
		
		// Per defecte mostrem la llista de museus (0=QR,1=Museus)
		pager.setCurrentItem(1);
		
		// Gestionar canvis de fragment
		pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				CameraFragment fragment = (CameraFragment)pagerAdapter.getItem(0);
				
				if (arg0 == ViewPager.SCROLL_STATE_DRAGGING)
				{
					fragment.setCameraFocused(false);
			    }
				else
				{
					if (pager.getCurrentItem() == 0)
					{
						fragment.setCameraFocused(true);
					}
				}
			}
		});
		
		// Evitar que el keyboard surti directament
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}
}