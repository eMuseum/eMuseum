package ub.edu.pis2014.pis12;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ub.edu.pis2014.pis12.controlador.API;
import ub.edu.pis2014.pis12.controlador.APINotifier;
import ub.edu.pis2014.pis12.controlador.APIOperation.TASK_TYPE;
import ub.edu.pis2014.pis12.utils.Utils;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.espian.showcaseview.ShowcaseViews;

public class Registrarse extends Activity {
	private boolean passCorrecte = false;
	public final static int ACTIVITY_CREATE=1;
	private int tipoIdioma;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
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
		
		setContentView(R.layout.activity_registrarse);
		
		/*ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();

		co.hideOnClickOutside = false;
		co.fadeInDuration = 500;
		co.fadeOutDuration = 500;
		ShowcaseView sv = ShowcaseView.insertShowcaseView(R.id.registrat_registrarse, this, "Tutorial", "Disfrute de la experiencia con esta aplicacion", co);
		*/

		//Comprobamos que es la primera vez que ejecutamos el fragment 
		
		tipoIdioma=0;
		boolean firstRun = settings.getBoolean("REGISTER_FIRST_RUN", true);
		if (firstRun)
		{
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("REGISTER_FIRST_RUN", false);
			editor.commit();
			
			showOverLay();
		}
		
		// Restaurar per defecte, en cas de que no sigui el primer cop que carreguem
		final View loadingLayout = findViewById(R.id.loading_layout);
		final View registerLayout = findViewById(R.id.register_layout);
		
		loadingLayout.setVisibility(View.GONE);
		registerLayout.setVisibility(View.VISIBLE);
		
		// Guardem referncies al frame del login i a la barra de registre
		final FrameLayout loginFrame = (FrameLayout)findViewById(R.id.login_frame);
		final LinearLayout registerFrame = (LinearLayout)findViewById(R.id.register_frame);

		// Evitar que el keyboard surti directament
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
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
				 * Establim que el marginBottom del frame del login sigui igual a l'alcada
				 * de la barra de registre per evitar solapaments.
				 */
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
				lp.setMargins(0, 0, 0, registerFrame.getHeight());
				loginFrame.setLayoutParams(lp);
				
				// Eliminem l'observer
				registerFrame.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}
		});
		
		// boto registrarse
		final Button registrat = (Button)findViewById(R.id.registrat_registrarse);	
		final EditText pass = (EditText)findViewById(R.id.registrat_password);	
		final EditText confirmPass = (EditText)findViewById(R.id.registrat_repeteix_pass);
		final EditText nick = (EditText)findViewById(R.id.registrat_username);	
		final EditText email = (EditText)findViewById(R.id.registrat_email);
		final ImageView img1 = (ImageView)findViewById(R.id.registrat_img_confirm1);
		final ImageView img2 = (ImageView)findViewById(R.id.registrat_img_confirm2);
		final ImageView img3 = (ImageView)findViewById(R.id.registrat_img_no_confirm1);
		final ImageView img4 = (ImageView)findViewById(R.id.registrat_img_no_confirm2);
		img1.setVisibility(View.GONE);
		img2.setVisibility(View.GONE);
		img3.setVisibility(View.VISIBLE);
		img4.setVisibility(View.VISIBLE);
		
		final Context context = this;

		img1.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Toast.makeText(context, R.string.toast_registre_pass_ok, Toast.LENGTH_LONG).show();
			}

		});
		
		img2.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Toast.makeText(context, R.string.toast_registre_pass_ok, Toast.LENGTH_LONG).show();
			}

		});
		img3.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Toast.makeText(context, R.string.toast_registre_pass_err, Toast.LENGTH_LONG).show();
			}

		});
		
		img4.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Toast.makeText(context, R.string.toast_registre_pass_err, Toast.LENGTH_LONG).show();
			}

		});
		
		
		pass.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(pass.getText().toString().equals(confirmPass.getText().toString()) && pass.getText().toString().length()>=6 && confirmPass.getText().toString().length()>=6){
					img1.setVisibility(View.VISIBLE);
					img2.setVisibility(View.VISIBLE);
					img3.setVisibility(View.GONE);
					img4.setVisibility(View.GONE);
					passCorrecte = true;

				}else{
					img1.setVisibility(View.GONE);
					img2.setVisibility(View.GONE);
					img3.setVisibility(View.VISIBLE);
					img4.setVisibility(View.VISIBLE);
					passCorrecte = false;
					
				}
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		
		confirmPass.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(pass.getText().toString().equals(confirmPass.getText().toString()) && pass.getText().toString().length()>=6 && confirmPass.getText().toString().length()>=6){
					img1.setVisibility(View.VISIBLE);
					img2.setVisibility(View.VISIBLE);
					img3.setVisibility(View.GONE);
					img4.setVisibility(View.GONE);
					passCorrecte = true;

				}else{
					img1.setVisibility(View.GONE);
					img2.setVisibility(View.GONE);
					img3.setVisibility(View.VISIBLE);
					img4.setVisibility(View.VISIBLE);
					passCorrecte = false;
					
				}

				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		
		

		registrat.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				
				//Aquests metodes es canviaran per metodes de veritat que faran comprovacions a la base de dades
				if(passCorrecte && nick.getText().toString().length() >0 && email.getText().toString().length() > 0){
					
					registerLayout.setVisibility(View.GONE);
					loadingLayout.setVisibility(View.VISIBLE);
					
					registrat.setEnabled(false);
					API.StartOperation(context, TASK_TYPE.API_REGISTER, new APINotifier() {
						@Override
						public void onResult(boolean result) {
							registrat.setEnabled(true);
							if (result)
							{
								Toast.makeText(context, context.getString(R.string.toast_registre_fi) + " " + nick.getText().toString() + "!", Toast.LENGTH_LONG).show();

								Intent i = new Intent(Registrarse.this, MainActivity.class);
								startActivity(i);
								finish();
							}
							else
							{
								loadingLayout.setVisibility(View.GONE);
								registerLayout.setVisibility(View.VISIBLE);
							}
						}
					},
						nick.getText().toString(), pass.getText().toString(), email.getText().toString());
				}else{

			    	if( nick.getText().toString().length()==0){
				    	Toast.makeText(context, R.string.toast_registre_nom_err, Toast.LENGTH_LONG).show();
			    	}
			    	else if(!Utils.checkEmail(email.getText().toString())){
						Toast.makeText(context, R.string.toast_registre_email_err, Toast.LENGTH_LONG).show();
			    	}
			    	else if(!passCorrecte){
						Toast.makeText(context, R.string.toast_registre_pass_err, Toast.LENGTH_LONG).show();
			    	}

				}
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		API.CancelOperation();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item){
		Intent intent = null;
		switch(item.getItemId())
		{
			case R.id.action_settings:
				intent = new Intent(Registrarse.this, SettingsActivity.class);
				startActivity(intent);
				return true;

			case R.id.action_ajuda:
				intent = new Intent(Registrarse.this,AjudaActivity.class);
		    	startActivity(intent);
				return true;			
			
			case R.id.action_canviar_idioma://Canviar Vista
	  			//
	  			AlertDialog.Builder builderIdioma = new AlertDialog.Builder(this);
	  			
	  			Resources resources2 = getResources();
	  			
	  			String[] llenguatges = resources2.getStringArray(R.array.app_language);
	  			builderIdioma.setTitle("Selecciona el idioma:");
	  			//Mostrem el dialog amb els diferents tipus de mapa que l'usuari pot escollir per mostrar-ho.
	  			builderIdioma.setSingleChoiceItems(llenguatges, 1, new DialogInterface.OnClickListener() {

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
	  					SharedPreferences settings =PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	  					SharedPreferences.Editor editor=settings.edit();
	  					editor.putString("Language_Code", locale.getLanguage());
	  					editor.commit();
	  					finish();
	  					Intent i=new Intent(Registrarse.this,MainActivity.class);
	  					startActivity(i);
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
	
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		MenuItem item1 = menu.findItem(R.id.action_buscar_actualitzacio);
        item1.setVisible(false);
	    MenuItem item2 = menu.findItem(R.id.action_tancar_sessio);
	    item2.setVisible(false);
	    MenuItem item3 = menu.findItem(R.id.action_iniciar_sessio);
	    item3.setVisible(false);
		return true;
	}

	private void showOverLay(){
		ShowcaseViews views = new ShowcaseViews(Registrarse.this);
		views.addView(new ShowcaseViews.ItemViewProperties(R.id.register_layout,R.string.tutorial,R.string.tutorial_registrarse3));
		views.addView(new ShowcaseViews.ItemViewProperties(R.id.registrat_registrarse, R.string.tutorial, R.string.tutorial_registrarse2));
		views.show();
	}
}
