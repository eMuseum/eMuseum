package ub.edu.pis2014.pis12;

import android.app.SearchManager;
import android.content.Context;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
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
import ub.edu.pis2014.pis12.utils.AlphaToggle;
import ub.edu.pis2014.pis12.utils.ElementImageManager;
import ub.edu.pis2014.pis12.utils.MapUtil;
import ub.edu.pis2014.pis12.utils.RotateToggle;
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
    // Variables utilitzades per les animacions
    AlphaToggle alphaToggle = null;
    RotateToggle rotateToggle = null;
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

        //Obte els checkboxs i el EditText del buscador
        final CheckBox buscar_museu = (CheckBox) view.findViewById(R.id.view_search_per_museu);
        final CheckBox buscar_obra = (CheckBox) view.findViewById(R.id.view_search_per_obra);
        final CheckBox buscar_autor = (CheckBox) view.findViewById(R.id.view_search_per_autor);

        // Obte el filtre d'ordre
        final Spinner spinner_ordenar = (Spinner) view.findViewById(R.id.view_search_ordre);

        // Crea un ArrayAdapter utilitzan els items de l'arxiu ordenar_array.xml
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

            buscar_museu.setChecked(museuChecked);
            buscar_autor.setChecked(autorChecked);
            buscar_obra.setChecked(obraChecked);
            spinner_ordenar.setSelection(posicioOrdre);
        }

        // Coloca l'adapter al grid
        gridView.setAdapter(adapter);
/*
        // Views utilitzades per indicar a l'usuari com carregar mes elements
        final RelativeLayout loadMore = (RelativeLayout) view.findViewById(R.id.load_more_layout);
        final View loadMoreImg = view.findViewById(R.id.load_more_img);
        final View upForMore = view.findViewById(R.id.txt_up_more);
        final View upClick = view.findViewById(R.id.txt_up_click);
/*
        // Classe per gestionar l'scroll i la carrega de nous elements
        new ScrollLoader(gridView, new ScrollLoaderListener() {

            @Override
            public boolean isEnabled() {
                // Solament ho activem si tenim algun element en el grid
                return adapter.getCount() > 0;
            }

            @Override
            public void onEventDown() {
                // Mostrem els textos d'ajuda
                upForMore.setVisibility(View.VISIBLE);
                upClick.setVisibility(View.GONE);
            }

            @Override
            public void onEventMove(float alphaY, boolean carregarElements) {
                // Si tenim desplacament positiu
                if (alphaY > 0) {
                    // Mostrem els elements d'ajuda
                    loadMore.setVisibility(View.VISIBLE);
                    loadMoreImg.getLayoutParams().height = (int) (alphaY / 4);
                    loadMoreImg.requestLayout();
                } else {
                    // Amaguem els elements d'ajuda
                    loadMore.setVisibility(View.GONE);
                }

                // Si ja es poden carregar
                if (carregarElements) {
                    // Mostrem l'ajuda de que es pot deixar anar
                    upForMore.setVisibility(View.GONE);
                    upClick.setVisibility(View.VISIBLE);
                } else {
                    // Mostrem l'ajuda de pujar
                    upForMore.setVisibility(View.VISIBLE);
                    upClick.setVisibility(View.GONE);
                }
            }

            @Override
            public void onEventUp(float alphaY, boolean carregarElements) {
                // Si cal carregar nous elements
                if (carregarElements) {
                    // Carregar mes elements
                    int position = gridView.getLastVisiblePosition();
                    if (adapter.loadMore()) {
                        gridView.smoothScrollToPosition(position + 1);
                    }
                }

                // Restrablim les variables
                loadMore.setVisibility(View.GONE);
            }
        });

        */

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

        // Quan clickes al desplegable d'opcions, es mostra
        final View showSettings = view.findViewById(R.id.main_show_settings);
        final View settingsView = view.findViewById(R.id.main_search_settings);

        // Quan el checkbox de buscar museus canvia d'estat
        buscar_museu.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                museuChecked = isChecked;
                adapter.setMuseus(isChecked);
            }
        });

        // Quan el checkbox de buscar obres canvia d'estat
        buscar_obra.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                obraChecked = isChecked;
                adapter.setObres(isChecked);
            }
        });

        // Quan el checkbox de buscar autors canvia d'estat
        buscar_autor.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                autorChecked = isChecked;
                adapter.setAutors(isChecked);
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

        // Crea i estableix les animacions del filtre de cerca
        alphaToggle = new AlphaToggle(showSettings, settingsView, 1000);
        rotateToggle = new RotateToggle(showSettings, showSettings, 1000);
        rotateToggle.setParams(RotateAnimation.RELATIVE_TO_SELF, 0.50f, RotateAnimation.RELATIVE_TO_SELF, 0.15f);

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
            views.addView(new ShowcaseViews.ItemViewProperties(R.id.main_show_settings, R.string.tutorial, R.string.tutorial_main_settings));
            views.addView(new ShowcaseViews.ItemViewProperties(R.id.main_grid_museus, R.string.tutorial, R.string.tutorial_main_museus));
            views.addView(new ShowcaseViews.ItemViewProperties(R.id.action_search, R.string.tutorial, R.string.tutorial_main_camera));

            Display display = getActivity().getWindowManager().getDefaultDisplay();
            float x = display.getWidth() / 2;
            float y = display.getHeight() / 3;
            views.addAnimatedGestureToView(3, 0, y, x, y, false);

            views.show();
        }
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
