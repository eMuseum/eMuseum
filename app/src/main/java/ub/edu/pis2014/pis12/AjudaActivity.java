package ub.edu.pis2014.pis12;

import java.util.ArrayList;
import java.util.Locale;

import ub.edu.pis2014.pis12.utils.Utils;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class AjudaActivity extends Activity {
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
		
		setContentView(R.layout.activity_ajuda);	
		tipoIdioma=0;
		final ArrayList<String> ajuda=new ArrayList<String>();
 		final ListView listView = (ListView) findViewById(R.id.ajudaListView);
 		final Resources res=getResources();
 		String[] listaPreguntas=res.getStringArray(R.array.listaPreguntas);
 		//Carreguem les dades al adapter
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,listaPreguntas);
		final String[] listaRespuestas=res.getStringArray(R.array.listaRespuestas);
		
		//Mostrem la llista de preguntes 
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				
				String titulo = (String)adapter.getItem(position); 
				
				//Creeem un dialog per carregar la resposta corresponent a la pregunta que hem escollit
				AlertDialog.Builder dialogBuilder=new AlertDialog.Builder(AjudaActivity.this);
		    	dialogBuilder.setTitle(titulo);
		    	//Carreguem la resposta al dialog
		    	dialogBuilder.setMessage(listaRespuestas[position]);
		    	//Creamos un boton aceptar en el dialog que si pulsamos en ella nos cerrara el dialog
		    	dialogBuilder.setNeutralButton(R.string.dialog_acceptar, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//Pulsem el boto 'Aceptar' per cerrar el dialog.
						dialog.cancel();
					}
				});
		    	AlertDialog dialog= dialogBuilder.create();
		    	//Mostramos el Dialog
		    	dialog.show();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.ajuda, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		Intent intent = null;
		switch(item.getItemId())
		{
			case R.id.action_settings:
				intent = new Intent(AjudaActivity.this, SettingsActivity.class);
				startActivity(intent);
				return true;
				
			case R.id.action_canviar_idioma://Canviar Vista
	  			//
	  			AlertDialog.Builder builderIdioma = new AlertDialog.Builder(this);
	  			
	  			Resources resources2 = getResources();
	  			
	  			String[] llenguatges = resources2.getStringArray(R.array.app_language);
	  			builderIdioma.setTitle("Selecciona el idioma:");
	  			
	  			//Mostrem el dialog amb els diferents tipus de mapa que l'usuari pot escollir per mostrar-ho.
	  			builderIdioma.setSingleChoiceItems(llenguatges,Utils.getIdioma(), new DialogInterface.OnClickListener() {

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
	  					Intent i=new Intent(AjudaActivity.this,MainActivity.class);
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
}