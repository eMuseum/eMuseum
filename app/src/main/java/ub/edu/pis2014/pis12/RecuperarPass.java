package ub.edu.pis2014.pis12;

import java.util.Locale;

import ub.edu.pis2014.pis12.controlador.API;
import ub.edu.pis2014.pis12.controlador.APINotifier;
import ub.edu.pis2014.pis12.controlador.APIOperation.TASK_TYPE;
import ub.edu.pis2014.pis12.utils.Utils;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RecuperarPass extends Activity {

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
		
		setContentView(R.layout.activity_recuperarpass);
		
		final Context context = this;
		
		final View loginLayout = findViewById(R.id.recuperar_layout_pass);
		final View loadingLayout = findViewById(R.id.loading_layout_pass);
		
		loginLayout.setVisibility(View.VISIBLE);
		loadingLayout.setVisibility(View.GONE);
		
		final EditText email = (EditText)findViewById(R.id.recuperar_email);
		Button enviar = (Button)findViewById(R.id.enviar_email);
		
		//Quan clicquem al boto 'enviar'
		enviar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String emailUsuari = email.getText().toString();
				if (Utils.checkEmail(emailUsuari)){
					//enviar contrasenya al email -> comprovar si el email existeix
					//mentres envia:
					loginLayout.setVisibility(View.GONE);
					loadingLayout.setVisibility(View.VISIBLE);
					
					API.StartOperation(context, TASK_TYPE.API_RECOVER, new APINotifier() {
						
						@Override
						public void onResult(boolean result) {
							//Quan ha acabat d'enviar el mail
							loginLayout.setVisibility(View.VISIBLE);
							loadingLayout.setVisibility(View.GONE);
						}
					}, emailUsuari);
				}
				else{
					Toast.makeText(context, R.string.toast_registre_email_err, Toast.LENGTH_LONG).show();
				}
			}
		});
	}
		
}


