package ub.edu.pis2014.pis12;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Locale;

import ub.edu.pis2014.pis12.model.Usuari;
import ub.edu.pis2014.pis12.utils.Utils;

public class LlistaObresActivity extends FragmentActivity {
    //Per pasar a la nova activity en el intent
    public final static int ACTIVITY_CREATE = 1;
    private int tipoIdioma;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String idioma = settings.getString("Language_Code", getResources().getConfiguration().locale.toString().substring(0, 2));
        if (idioma != getResources().getConfiguration().locale.toString().substring(0, 2)) {
            Configuration config = new Configuration();
            Locale locale = new Locale(idioma);
            Locale.setDefault(locale);
            config.locale = locale;
            getApplicationContext().getResources().updateConfiguration(config, null);
        }

        setContentView(R.layout.activity_llista_obres);

        tipoIdioma = 0;
        // Si estem en el mode expandit (tablet), mostrem l'activity adecuada
        if (Utils.isExpandedMode(this)) {
            Bundle extras = getIntent().getExtras();

            Intent i = new Intent(LlistaObresActivity.this, MainActivity.class);
            i.putExtra("id", extras.getInt("id"));
            i.putExtra("tipus", extras.getSerializable("tipus"));
            startActivity(i);
            finish();
        }

        // Carreguem i mostrem el fragment en el punt adequat
        FragmentManager manager = getSupportFragmentManager();

        // Solament si no esta ja carregat
        if (manager.findFragmentById(R.id.obres_llista_container) != null)
            return;

        FragmentTransaction ft = manager.beginTransaction();
        ft.add(R.id.obres_llista_container, LlistaObresFragment.newInstance(getIntent().getExtras())).commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Gestiona la seleccio d'un element en el menu d'opcions
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_settings:
                intent = new Intent(LlistaObresActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_ajuda:
                intent = new Intent(LlistaObresActivity.this, AjudaActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_tancar_sessio:
                Usuari.get().logout();
                intent = new Intent(LlistaObresActivity.this, SplashActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("skipLogin", true);
                startActivity(intent);
                return true;

            case R.id.action_iniciar_sessio:
                Usuari.get().logout();
                intent = new Intent(LlistaObresActivity.this, SplashActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("skipLogin", true);
                startActivity(intent);
                return true;

            case R.id.action_buscar_actualitzacio:
                MainActivity.buscarActualitzacions(this, null);
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
                        tipoIdioma = which;
                    }
                });
                //Apliquem el canvi
                builderIdioma.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so save the result somewhere
                        // or return them to the component that opened the dialog
                        Configuration config = new Configuration();
                        Locale locale = null;
                        switch (tipoIdioma) {
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
                        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("Language_Code", locale.getLanguage());
                        editor.commit();
                        finish();
                        Intent i = new Intent(LlistaObresActivity.this, MainActivity.class);
                        startActivity(i);
                    }
                });
                builderIdioma.setNegativeButton(R.string.dialog_cancelar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                AlertDialog dialogIdioma = builderIdioma.create();
                dialogIdioma.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!Usuari.get().isLogged()) {
            MenuItem item = menu.findItem(R.id.action_tancar_sessio);
            item.setVisible(false);
        } else {
            MenuItem item = menu.findItem(R.id.action_iniciar_sessio);
            item.setVisible(false);
        }
        return true;
    }

}