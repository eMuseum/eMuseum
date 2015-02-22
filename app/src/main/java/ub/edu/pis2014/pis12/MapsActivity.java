package ub.edu.pis2014.pis12;


import java.util.Locale;

import ub.edu.pis2014.pis12.model.Usuari;
import ub.edu.pis2014.pis12.utils.MapUtil;
import ub.edu.pis2014.pis12.utils.OnLocationChanged;
import ub.edu.pis2014.pis12.utils.Utils;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsActivity extends FragmentActivity
{
		//private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
  		//private float distancia;
	
		final static long UPDATE_INTERVAL_MILISECONDS=100;
		final static float MIN_DISTANCE=5;
		private GoogleMap map;
	  	private Location museuLocation;
	  	private int tipoMapa,tipoIdioma;
	  	private float distancia;
	  	private TextView distanceView;
	  	private MapUtil mapa;
	  	
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
	    
	    setContentView(R.layout.mapview);
	    
	    tipoMapa=0;
	    tipoIdioma=0;
	    distancia=0;
	    
	    Bundle extra = getIntent().getExtras();
	    museuLocation = new Location("Museu");
	    museuLocation.setLongitude(extra.getDouble("Longitud"));
	    museuLocation.setLatitude(extra.getDouble("Latitud"));
	   // map = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapView)).getMap();
	    FragmentManager manager=getSupportFragmentManager();
	    Fragment fragment =manager.findFragmentById(R.id.mapView);
	    SupportMapFragment support=(SupportMapFragment) fragment;
	    map=support.getMap();
	    map.setMyLocationEnabled(true);
	    distanceView = (TextView)findViewById(R.id.distance);
	    
	    /*OrientationListener orientacion=new OrientationListener(getApplicationContext()){

			@Override
			public void onOrientationChanged(int orientation) {
				map.setMapType(tipoMapa);
			}
	    	
	    };*/
	  }
	  
	  OnLocationChanged mapLocationChanged = new OnLocationChanged() {
			
			@Override
			public void onChange(Location location) {
				// Netegem el mapa per borrar el globus de la nostra posici� anterior.
				map.clear();
				
				// LatLng(s)
				LatLng actual = new LatLng(location.getLatitude(), location.getLongitude());
				LatLng museu = new LatLng(museuLocation.getLatitude(), museuLocation.getLongitude());
				
				// Afegim nostra posici� en el mapa
				MarkerOptions mp = new MarkerOptions();
				mp.position(actual);
				mp.title("Mi posicion");
				map.addMarker(mp.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
				
				if (location != null) {
					// Mostrem un globus verd en la posici� del museu.
					distanceView.setVisibility(View.VISIBLE);
					double distance = location.distanceTo(museuLocation);
					distance = (Math.round(distance * 100)) / 100;
					if (distance >= 1000) {
						distance = distance / 1000;
						distanceView.setText("Distance: " + distance + " km");
					} else {
						distanceView.setText("Distance: " + distance + " m");
					}
					map.addMarker(new MarkerOptions()
							.position(museu)
							.title("Posicion del Museo")
							.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
					map.addPolyline(new PolylineOptions().add(actual, museu).width(5)
							.color(Color.RED));
				}
				
				// Movem el mapa a la nostra posici�.
				map.moveCamera(CameraUpdateFactory.newLatLng(actual));
				map.animateCamera(CameraUpdateFactory.newLatLngZoom(actual, 16));
				
				// Fem un zoom per apropar el mapa a la nostra posici�.
				map.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);				
			}
		};

	  /*
	   * M�tode que es crida quan canviem de orientaci�.
	   * 
	   */	  
	  public void onConfigurationChanged(Configuration newConfig) {
		    super.onConfigurationChanged(newConfig);
		    
		    map.setMapType(tipoMapa);
		    // Checks the orientation of the screen
		    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
		        Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
		    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
		        Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
		    }
	  }
	  
	  @Override
	  public boolean onCreateOptionsMenu(Menu menu) {
	    getMenuInflater().inflate(R.menu.mapa, menu);
	    return true;
	  }
	  
	  /*
	   * M�tode que ens defineix l\'acci� que far� l'aplicaci� quan premem el bot� 'Back'
	   */
	  public void onBackPressed(){
		  this.finish();
	  }
	  
	  
	  /*
	   * M�tode per tratar l\'opci� que hem escollit en el men� de opcions.
	   * 
	   */
	  public boolean onOptionsItemSelected(MenuItem item){
		  Intent intent=null;
		  switch(item.getItemId()){
		  		case R.id.view_setting://Canviar Vista
		  			//
		  			AlertDialog.Builder builder = new AlertDialog.Builder(this);
		  			
		  			Resources resources = getResources();
		  			
		  			String[] modes = resources.getStringArray(R.array.maps_modes);
		  			builder.setTitle("Selecciona el tipo de mapa:");
		  			//Mostrem el dialog amb els diferents tipus de mapa que l'usuari pot escollir per mostrar-ho.
		  			builder.setSingleChoiceItems(modes, Utils.getIdioma(), new DialogInterface.OnClickListener() {

		  				@Override
		  				public void onClick(DialogInterface dialog, int which) {
		  					//Guardem en un variable el tipu de mapa que ha escollit l'usuari.
		  					tipoMapa=which;
		  				}
		  			});
		  			//Apliquem el canvi
		  			builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
		  			@Override
		  				public void onClick(DialogInterface dialog, int id) {
		  			// User clicked OK, so save the result somewhere
		  			// or return them to the component that opened the dialog
		  					switch(tipoMapa){
		  						case 0:
		  							map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		  							tipoMapa=map.getMapType();
		  							break;
		  						case 1:
		  							map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		  							tipoMapa=map.getMapType();
		  							break;
		  						case 2:
		  							map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		  							tipoMapa=map.getMapType();
		  							break;
		  						case 3:
		  							map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
		  							tipoMapa=map.getMapType();
		  							break;
		  						case 4:
		  							map.setMapType(GoogleMap.MAP_TYPE_NONE);
		  							tipoMapa=map.getMapType();
		  							break;
		  					}

		  				}
		  			});
		  			builder.setNegativeButton(R.string.dialog_cancelar, new DialogInterface.OnClickListener() {
		  			@Override
		  				public void onClick(DialogInterface dialog, int id) {
		  					dialog.cancel();
		  				}
		  			});

		  			AlertDialog dialog=builder.create();
		  			dialog.show();
		  			return true;
		  		case R.id.action_ajuda://Ajuda
		  			intent = new Intent(MapsActivity.this,AjudaActivity.class);
			    	startActivity(intent);
		  			return true;
		  		case R.id.action_tancar_sessio://Tancar Sessi�
		  			Usuari.get().logout();
					intent = new Intent(MapsActivity.this, SplashActivity.class);
					intent.putExtra("skipLogin", true);
					startActivity(intent);
		  			return true;
		  		case R.id.action_iniciar_sessio://Iniciar Sessi�
					Usuari.get().logout();
					intent = new Intent(MapsActivity.this, SplashActivity.class);
					intent.putExtra("skipLogin", true);
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
		  					Intent i=new Intent(MapsActivity.this,MainActivity.class);
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
	  
	  
	  /*
	   * M�tode que es crida per reanudar l'estat del mapa al de la nostra �ltima connexi�.
	   */
	  protected void onResume(){
		  super.onResume();
		  
		  MapUtil.startMapListener(this, mapLocationChanged);
	  }
	  
	  
	  /*
	   * M�tode que es crida quan sortim del mapa per guadar el estat del mapa i poder reanudar-ho posteriorment
	   */
	  protected void onPause(){
		  super.onPause();
		  if(mapa!=null){
			  MapUtil.stopListener(this);
		  }
	  }
	  
	  
	  /*
	   *M�tode per amagar els opcions que considerem oportuns.
	   *Es fa segon si l\'usuari habia logueat o no.
	   */
	  public boolean onPrepareOptionsMenu(Menu menu) {
		  	//Si l'usuario no est� logueat,amaguem l\'opci� de tancar sessi�
			if (!Usuari.get().isLogged()) {
				MenuItem item = menu.findItem(R.id.action_tancar_sessio);
				item.setVisible(false);
			}else{
				//En cas contrari,amaguem l\'opci� de iniciar sessi�
				MenuItem item = menu.findItem(R.id.action_iniciar_sessio);
				item.setVisible(false);
			}
			return true;
		}
}