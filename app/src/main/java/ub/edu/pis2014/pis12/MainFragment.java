package ub.edu.pis2014.pis12;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.Spinner;

import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.ShowcaseViews;

import java.util.concurrent.Callable;

import ub.edu.pis2014.pis12.controlador.API;
import ub.edu.pis2014.pis12.controlador.GridAdapterElements;
import ub.edu.pis2014.pis12.controlador.GridAdapterElements.TIPUS_ORDENACIO;
import ub.edu.pis2014.pis12.controlador.ScreenSlidePagerAdapter;
import ub.edu.pis2014.pis12.model.Autor;
import ub.edu.pis2014.pis12.model.Element;
import ub.edu.pis2014.pis12.model.Museu;
import ub.edu.pis2014.pis12.model.Obra;
import ub.edu.pis2014.pis12.model.TIPUS_ELEMENT;
import ub.edu.pis2014.pis12.utils.AlphaBoth;
import ub.edu.pis2014.pis12.utils.ElementImageManager;
import ub.edu.pis2014.pis12.utils.MapUtil;
import ub.edu.pis2014.pis12.utils.Utils;

/**
 * Aquest Fragment mostra la llista de museus juntament amb una utilitat per
 * buscar
 */
public class MainFragment extends Fragment {
    public final static int ACTIVITY_CREATE = 1;
    private static GridAdapterElements adapter = null;
    private static boolean museuChecked = true;
    private static boolean obraChecked = false;
    private static boolean autorChecked = false;
    private static int posicioOrdre = 0;
    // Inentar actualitzar la DB un sol cop
    private static boolean comprobaDB = true;
    private SearchView searchView;

    public static void clearStatic() {
        adapter = null;
    }

    /**
     * Cridat quan es crea per primer cop el fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    /**
     * Cridat un cop ja esta creat. Hem de sobrecarregar aquest metode ja que
     * abans no podiem accedir als widgets
     *
     * @param view View que conte els widgets
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Contexte de l'activity
        final Context context = view.getContext();

        // Obtenir el gridview
        final GridView gridView = (GridView) view.findViewById(R.id.main_grid_museus);

        // Obte el filtre d'ordre
        final Spinner spinner_ordenar = (Spinner) view.findViewById(R.id.view_search_ordre);
        final Spinner spinner_types = (Spinner) view.findViewById(R.id.view_search_type);

        final ArrayAdapter<String> spinner_types_adapter = new ArrayAdapter<String>(context,android.R.layout.simple_spinner_item);

        spinner_types.setAdapter(spinner_types_adapter);
        spinner_types_adapter.add(getSelectedItemsAsString());

        ArrayAdapter<CharSequence> spinner_adapter = ArrayAdapter.createFromResource(this.getActivity(), R.array.ordenar_array, android.R.layout.simple_spinner_item);
        // Especifica el layout que utilitzarem (simple)
        spinner_adapter.setDropDownViewResource(R.layout.spinner_item);
        // Afegin l'adapter del spinNer al spinNer;
        spinner_ordenar.setAdapter(spinner_adapter);

        // Declara l'adapter del grid
        if (adapter == null) {
            adapter = new GridAdapterElements(gridView, getActivity());
            adapter.setMuseus(true);

            //Afegeix els elements a l'adapter i els ordena Alfabeticament
            //   adapter.populate(false);
        } else {
            // Inidiquem la activity
            ElementImageManager.setActivity(getActivity());
            // Provoquem un redibuixat
            gridView.invalidate();
            spinner_ordenar.setSelection(posicioOrdre);
        }

        // Coloca l'adapter al grid
        gridView.setAdapter(adapter);

        // Quan clickes a un element del grid
        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, final int position, long arg3) {
                /*
				 * Declaracio intent per pasar a LlistaObresActivity
				 * Al clicar en un museu canviem d'activity i passem l'ID del museu
				 */
                Element element = (Element) adapter.getItem(position);

                // Cal distingir en quin mode estem
                if (!Utils.isExpandedMode(getActivity())) {
                    Intent i = null;
                    if (element instanceof Obra) {
                        i = new Intent(getActivity(), InfoActivity.class);
                    } else {
                        i = new Intent(getActivity(), LlistaObresActivity.class);

                    }
                    i.putExtra("id", element.getId());
                    i.putExtra("tipus", TIPUS_ELEMENT.getFromElement(element));
                    startActivityForResult(i, MainFragment.ACTIVITY_CREATE);
                } else {
                    // Si estem en un altre mode em de crear la transicio de fragments
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();

                    ViewPager pager = (ViewPager) getActivity().findViewById(R.id.main_pager);
                    ScreenSlidePagerAdapter adapter = (ScreenSlidePagerAdapter) pager.getAdapter();
                    if (element instanceof Museu) {
                        // Actualitzem el fragment de l'esquerra
                        Fragment fragment = LlistaObresFragment.newInstance(element);
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        ft.replace(R.id.main_left_fragment, fragment).commit();
                    } else if (element instanceof Autor) {
                        // Actualitzem el fragment de l'esquerra
                        Fragment fragment = LlistaObresFragment.newInstance(element);
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        ft.replace(R.id.main_left_fragment, fragment).commit();
                    }

                    new AlphaBoth(null, pager, 500).execute();

                    // Modifiquem el fragment central del ViewPager
                    Fragment fragment = InfoFragment.newInstance(element);
                    adapter.customize(fragment);
                    pager.setCurrentItem(1, true);
                }
            }
        });

        spinner_ordenar.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                posicioOrdre = position;
                adapter.ordenar(TIPUS_ORDENACIO.values()[posicioOrdre]);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //On click type filter
        spinner_types.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        boolean itemsChecked[] = {museuChecked, autorChecked, obraChecked};

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        // Set the dialog title
                        builder.setTitle(R.string.view_search_type)
                                // Specify the list array, the items to be selected by default (null for none),
                                // and the listener through which to receive callbacks when items are selected
                                .setMultiChoiceItems(R.array.types_array, itemsChecked ,
                                        new DialogInterface.OnMultiChoiceClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which,
                                                                boolean isChecked) {

                                                switch (which) {
                                                    case 0:
                                                        museuChecked = isChecked;
                                                        adapter.setMuseus(isChecked);
                                                        break;
                                                    case 1:
                                                        autorChecked = isChecked;
                                                        adapter.setAutors(isChecked);
                                                        break;
                                                    case 2:
                                                        obraChecked = isChecked;
                                                        adapter.setObres(isChecked);
                                                        break;
                                                }
                                                spinner_types_adapter.clear();
                                                spinner_types_adapter.add(getSelectedItemsAsString());
                                            }
                                        })
                                        // Set the action buttons
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                });

                        builder.create();

                        builder.show();

                }
                return true;
            }
        });

        // Solament comprobar la versio 1 cop
        if (comprobaDB) {
            // A menys que l'usuari ho hagi desactivat
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            if (preferences.getBoolean("autoactualitzar_db", true) && API.isNetworkAvailable(context)) {
                // Cridem el metode definit en la activity pare (aixo es un fragment)
                MainActivity.buscarActualitzacions(getActivity(), new Callable<Void>() {

                    @Override
                    public Void call() throws Exception {
                        showTutorial();
                        return null;
                    }
                });
            } else {
                showTutorial();
            }

            // Indicar que ja esta
            comprobaDB = false;
        } else {
            showTutorial();
        }
    }

    @SuppressWarnings("deprecation")
    private void showTutorial() {
        //Comprobamos que es la primera vez que ejecutamos el fragment
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        boolean firstRun = settings.getBoolean("MAIN_FIRST_RUN", true);
        if (firstRun) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("MAIN_FIRST_RUN", false);
            editor.commit();

            ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
            co.hideOnClickOutside = false;
            ShowcaseView.insertShowcaseView(R.id.action_search, getActivity(), R.string.tutorial, R.string.tutorial_main_search, co);
            ShowcaseViews views = new ShowcaseViews(getActivity());
            views.addView(new ShowcaseViews.ItemViewProperties(R.id.action_search, R.string.tutorial, R.string.tutorial_main_search));
            views.addView(new ShowcaseViews.ItemViewProperties(R.id.view_search_ordre, R.string.tutorial, R.string.tutorial_main_settings));
            views.addView(new ShowcaseViews.ItemViewProperties(R.id.main_grid_museus, R.string.tutorial, R.string.tutorial_main_museus));
            views.addView(new ShowcaseViews.ItemViewProperties(R.id.action_search, R.string.tutorial, R.string.tutorial_main_camera));

            Display display = getActivity().getWindowManager().getDefaultDisplay();
            float x = display.getWidth() / 2;
            float y = display.getHeight() / 3;
            views.addAnimatedGestureToView(3, 0, y, x, y, false);

            views.show();
        }
    }

    public String getSelectedItemsAsString() {
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;
        String[] items = getResources().getStringArray(R.array.types_array);
        boolean itemsChecked[] = {museuChecked, autorChecked, obraChecked};
        for (int i = 0; i < items.length; ++i) {
            if (itemsChecked[i]) {
                if (foundOne) {
                    sb.append(", ");
                }
                foundOne = true;
                sb.append(items[i]);
            }
        }
        if (sb.toString().length() == 0) {
            return getResources().getString(R.string.view_search_type);
        }
        return sb.toString();
    }

    public void onPrepareOptionsMenu(Menu menu) {
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconifiedByDefault(true);
        searchView.setIconified(true);
        searchView.setQueryRefinementEnabled(true);
        //When wirte text in SearchView -> search
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String text) {
                if (!searchView.isIconified()) {
                    searchElementsWithString(text);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                if (!searchView.isIconified()) {
                    searchElementsWithString(text);
                }
                return true;
            }
        });
    }

    public void searchElementsWithString(String text) {
        adapter.setWhere("nom like '%" + text + "%'");
        adapter.reloadData();
    }

    @Override
    public void onResume() {
        super.onResume();

        MapUtil.startGridListener(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();

        API.CancelOperation();
        ElementImageManager.cancelDownloads();
        MapUtil.stopListener(getActivity());
    }
}
