package ub.edu.pis2014.pis12;

import java.util.Date;
import java.util.Locale;

import ub.edu.pis2014.pis12.controlador.API;
import ub.edu.pis2014.pis12.controlador.APINotifier;
import ub.edu.pis2014.pis12.controlador.APIOperation.TASK_TYPE;
import ub.edu.pis2014.pis12.model.Dades;
import ub.edu.pis2014.pis12.model.Usuari;
import ub.edu.pis2014.pis12.utils.EMuseumService;
import ub.edu.pis2014.pis12.utils.Utils;
import ub.edu.pis2014.pis12.utils.WifiScanConnection;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.espian.showcaseview.ShowcaseViews;

/**
 * Aquesta activiti es la inicial. Permet registrarse, loguejar o
 * entrar directament a l'aplicacio
 * 
 */
public class SplashActivity extends Activity {
	public final static int ACTIVITY_CREATE = 1;
	
	private long backCount = 0;	
	private Button login = null;
	private Button registrar = null;
	private Button ometre = null;
	private int tipoIdioma;
	private SharedPreferences settings=null;;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		if (getIntent().getBooleanExtra("EXIT", false))
		{
			finish();
			return;
		}

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
		 * mostrem el xml sense el text inicial, d'altre forma mostrem el xml per tablet,
		 * que permet veure mes elements a la pantalla
		 */
		int resourceID = R.layout.activity_splash;
		if (Utils.isTabletDevice(this))
		{
			resourceID = R.layout.activity_splash_tablet;
		}
		
		setContentView(resourceID);
		
		tipoIdioma=0;
		// Establir el contexte de l'usuari
		final Context context = this;
		Usuari.get().setContext(context);
		
		
		// Layouts de carrega i login
		final View loadingLayout = findViewById(R.id.loading_layout);
		final View loginLayout = findViewById(R.id.login_layout);
		
		//Comprobamos que es la primera vez que ejecutamos el app 
		boolean firstRun = settings.getBoolean("FIRST_RUN", true);
		if (firstRun) 
		{
			loginLayout.setVisibility(View.VISIBLE);
			loadingLayout.setVisibility(View.GONE);
			
			// Fer-ho per primer cop i guardar perque no torni a executar-se
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("FIRST_RUN", false);
			editor.commit();   
			showOverLay();
		}
		
		// Guardem referencies al frame del login i a la barra de registre
		final FrameLayout loginFrame = (FrameLayout)findViewById(R.id.login_frame);
		final LinearLayout registerFrame = (LinearLayout)findViewById(R.id.register_frame);
		
		// Programem un observer per quan, un cop carregat el layout, obtenir el
		// tamany (height)
		final ViewTreeObserver observer = registerFrame.getViewTreeObserver();
		observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			/*
			 * La crida a removeGlobalOnLayoutListener esta obsoleta des de l'api 16,
			 * pero degut a que la minima es 8, ho mantenim
			 */
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				/*
				 * Establim que el marginBottom del frame del login sigui igual a l'altura
				 * de la barra de registre per evitar solapaments.
				 */
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
				lp.setMargins(0, 0, 0, registerFrame.getHeight());
				loginFrame.setLayoutParams(lp);
				
				// Eliminem l'observer
				registerFrame.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}
		});
		
		// Boto d'entrar
		login = (Button)findViewById(R.id.loginButton);		
		login.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				findViewById(R.id.login_layout).setVisibility(View.GONE);
				findViewById(R.id.loading_layout).setVisibility(View.VISIBLE);
				
				final EditText username = (EditText)findViewById(R.id.login_username);
				final EditText password = (EditText)findViewById(R.id.login_password);
				
				setButtonsEnabled(false);
				API.StartOperation(context, TASK_TYPE.API_SESSION_SAVE, new APINotifier() {
					@Override
					public void onResult(boolean result) {
						setButtonsEnabled(true);
						if (result)
						{
							Intent i = new Intent(SplashActivity.this, MainActivity.class);
							startActivity(i);
						}
						else
						{							
							loadingLayout.setVisibility(View.GONE);
							loginLayout.setVisibility(View.VISIBLE);
						}
					}
				},
					username.getText().toString(), password.getText().toString());
			}
		});
		
		// El boto ometre ens porta directament a l'activity de museus
		ometre = (Button)findViewById(R.id.botoOmetre);		
		ometre.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(SplashActivity.this, MainActivity.class);
				startActivity(i);
			}
		});
		
		// El boto registrar porta a registrarse
		registrar = (Button)findViewById(R.id.splash_registrarse);		
		registrar.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(SplashActivity.this, Registrarse.class);
				startActivity(i);
			}
		});
		
		TextView recuperar_Pass = (TextView)findViewById(R.id.textView_recuperarPass);
		recuperar_Pass.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				Intent i = new Intent(SplashActivity.this, RecuperarPass.class);
				startActivity(i);	
			}
		});
		
		// Evitar que el keyboard surti directament
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		// Si s'especifica que no s'ha de fer login
		boolean skipLogin = savedInstanceState != null || getIntent().getBooleanExtra("skipLogin", false);
		if (skipLogin)
		{
			loginLayout.setVisibility(View.VISIBLE);
			loadingLayout.setVisibility(View.GONE);
			return;
		}
		
		// Intentem fer un login amb les dades guardades
		// Un cop acabat, mostrarem el layout de login o la nova activity segons el resultat
		setButtonsEnabled(false);
		boolean connectedAPI = API.StartOperation(context, TASK_TYPE.API_SESSION_LOAD, new APINotifier() {
			@Override
			public void onResult(boolean result) {
				setButtonsEnabled(true);
				if (result)
				{
					Intent i = new Intent(SplashActivity.this, MainActivity.class);
					startActivity(i);
				}
				else
				{	
					loadingLayout.setVisibility(View.GONE);
					loginLayout.setVisibility(View.VISIBLE);
				}
			}
		});
		
		if (!connectedAPI)
		{
			loginLayout.setVisibility(View.VISIBLE);
			loadingLayout.setVisibility(View.GONE);
		}
	
	}
	
	private void setButtonsEnabled(boolean state)
	{
		login.setEnabled(state);
		registrar.setEnabled(state);
		//ometre.setEnabled(state);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		// Cancelem tots els threads
		API.CancelOperation();
	}
	
	@Override
	public void onBackPressed() {
		long time = (new Date()).getTime();
		if(time - backCount > 3000)
		{
			backCount = time;
	    	Toast.makeText(this, R.string.press_back_again, Toast.LENGTH_SHORT).show();
		}
		else
		{
			backCount = 0;
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/*
	 * Metodo al que se llama si clickeamos una de las opciones del menu
	 * (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	public boolean onOptionsItemSelected(MenuItem item){
		Intent intent = null;
		switch(item.getItemId())
		{
			case R.id.action_settings:
				intent = new Intent(SplashActivity.this, SettingsActivity.class);
				startActivity(intent);
				return true;
			
			case R.id.action_ajuda:
				intent = new Intent(SplashActivity.this,AjudaActivity.class);
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
	  					SharedPreferences.Editor editor=settings.edit();
	  					editor.putString("Language_Code", locale.getLanguage());
	  					Log.i("Activity", locale.getLanguage());
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
	
	/*
	 * El metodo muestra el tutorial de la aplicacion
	 */
	public void showOverLay(){
		ShowcaseViews views = new ShowcaseViews(SplashActivity.this);
		views.addView(new ShowcaseViews.ItemViewProperties(R.id.loginButton, R.string.tutorial, R.string.tutorial_login));
		views.addView(new ShowcaseViews.ItemViewProperties(R.id.splash_registrarse, R.string.tutorial, R.string.tutorial_registrarse));
		views.addView(new ShowcaseViews.ItemViewProperties(R.id.botoOmetre, R.string.tutorial, R.string.tutorial_ometre));
		views.show();
	}
	
	/*
	 * El metodo nos permite controlar las opciones del menu
	 * (non-Javadoc)
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem item1 = menu.findItem(R.id.action_buscar_actualitzacio);
        item1.setVisible(false);
	    MenuItem item2 = menu.findItem(R.id.action_tancar_sessio);
	    item2.setVisible(false);
	    MenuItem item3 = menu.findItem(R.id.action_iniciar_sessio);
	    item3.setVisible(false);
		return true;
	}
}
