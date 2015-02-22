package ub.edu.pis2014.pis12;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

import com.espian.showcaseview.ShowcaseViews;

import ub.edu.pis2014.pis12.controlador.GridAdapterElements;
import ub.edu.pis2014.pis12.controlador.ScreenSlidePagerAdapter;
import ub.edu.pis2014.pis12.model.Autor;
import ub.edu.pis2014.pis12.model.Dades;
import ub.edu.pis2014.pis12.model.Element;
import ub.edu.pis2014.pis12.model.Museu;
import ub.edu.pis2014.pis12.model.Obra;
import ub.edu.pis2014.pis12.model.TIPUS_ELEMENT;
import ub.edu.pis2014.pis12.utils.AlphaBoth;
import ub.edu.pis2014.pis12.utils.ElementImageManager;
import ub.edu.pis2014.pis12.utils.Utils;

@SuppressLint("ResourceAsColor")
public class LlistaObresFragment extends Fragment {

    //Per pasar a la nova activity en el intent
    public final static int ACTIVITY_CREATE = 1;

    //inicialitza la variable museu
    private int id_element;

    // Tipus d'element del que mostrem la llista
    private TIPUS_ELEMENT tipus;

    private static boolean isAutor = false;

    /**
     * Aquest metode permet crear un nou fragment i passar-l'hi parametres
     * a partir d'un element
     *
     * @param element Element del que agafar parametres
     * @return Retorna el nou fragment
     */
    public static LlistaObresFragment newInstance(Element element) {
        Bundle args = new Bundle();

        args.putInt("id", element.getId());
        args.putSerializable("tipus", TIPUS_ELEMENT.getFromElement(element));

        LlistaObresFragment fragment = new LlistaObresFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Permet crear un nou fragment passant-l'hi un Bundle, per exemple,
     * dels extras d'un intent
     *
     * @param args Bundle amb parametres
     * @return Retorna el nou fragment
     */
    public static LlistaObresFragment newInstance(Bundle args) {
        LlistaObresFragment fragment = new LlistaObresFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Al crear unicament inflem el container amb el layout
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MainActivity.mostrantObra = true;
        return inflater.inflate(R.layout.fragment_llista_obres, container, false);
    }

    /**
     * Al estar ja creada, carreguem i configurem cada un dels elements
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Dades de l'intent anterior = museu escollit
        Bundle extras = getArguments();

        if (extras != null) {
            id_element = extras.getInt("id");
            tipus = (TIPUS_ELEMENT) extras.getSerializable("tipus");
        }

        // Obtenim i declarem el gridView
        final GridView gridview = (GridView) view.findViewById(R.id.llistaObresActivity_gridView);
        final GridAdapterElements adapter = new GridAdapterElements(gridview, getActivity(), view.getContext(), false);
        adapter.setObres(true);

        // Element d'on estem mostrant la llista d'Obres i afegeix els elements a l'adapter
        final Element element;

        switch (tipus) {
            case TIPUS_MUSEU:
                element = (Museu) Dades.getMuseu(id_element);
                isAutor = false;
                adapter.setWhere("mid=" + id_element);
                break;

            case TIPUS_AUTOR:
                element = (Autor) Dades.getAutor(id_element);
                isAutor = true;
                adapter.setWhere("aid=" + id_element);
                break;

            default:
                element = null;
                // ERROR
                break;
        }

        adapter.populate(true);

        // Establim el titol
        final TextView titol = (TextView) view.findViewById(R.id.llistaObresActivity_title);
        titol.setText(element.getTitol());

        //Quan cliquem al titol ens porta a la info de l'element
        titol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Si no estem en mode tablet
                if (!Utils.isExpandedMode(getActivity())) {
                    Intent i = new Intent(getActivity(), InfoActivity.class);
                    i.putExtra("id", element.getId());
                    i.putExtra("tipus", tipus);
                    startActivityForResult(i, LlistaObresFragment.ACTIVITY_CREATE);
                } else {
                    ViewPager pager = (ViewPager) getActivity().findViewById(R.id.main_pager);
                    ScreenSlidePagerAdapter adapter = (ScreenSlidePagerAdapter) pager.getAdapter();

                    new AlphaBoth(null, pager, 500).execute();

                    Fragment fragment = InfoFragment.newInstance(element);
                    adapter.customize(fragment);
                    pager.setCurrentItem(1, true);
                }
            }
        });

        //Coloca l'adapter al grid
        gridview.setAdapter(adapter);
        // Quan clickes a un element del grid
        gridview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                /*
				 * Intent per pasar a InfoObraActivity passant les dades: 
				 * 	L'ID del museu,l'obra escollida i si es una obra o un museu
				 */
                Obra obra = (Obra) adapter.getItem(position);

                // Si esta en mode tablet, hem de carregar fragments
                if (Utils.isExpandedMode(getActivity())) {
                    ViewPager pager = (ViewPager) getActivity().findViewById(R.id.main_pager);
                    ScreenSlidePagerAdapter adapter = (ScreenSlidePagerAdapter) pager.getAdapter();

                    new AlphaBoth(null, pager, 500).execute();

                    Fragment fragment = InfoFragment.newInstance(obra);
                    adapter.customize(fragment);
                    pager.setCurrentItem(1, true);
                }
                // Sino, actuem mitjancant intents
                else {
                    Intent i = new Intent(getActivity(), InfoActivity.class);
                    i.putExtra("id", obra.getId());
                    i.putExtra("tipus", TIPUS_ELEMENT.TIPUS_OBRA);
                    startActivityForResult(i, LlistaObresFragment.ACTIVITY_CREATE);
                }
            }
        });

        //Comprobamos que es la primera vez que ejecutamos el fragment
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        boolean firstRun = settings.getBoolean("LLISTA_FIRST_RUN", true);
        if (firstRun) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("LLISTA_FIRST_RUN", false);
            editor.commit();

            ShowcaseViews views = new ShowcaseViews(getActivity());
            views.addView(new ShowcaseViews.ItemViewProperties(R.id.llistaObresActivity_gridView, R.string.tutorial, R.string.tutorial_llistaObres));
            views.show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        ElementImageManager.cancelDownloads();
    }

    public static boolean getTipus() {
        return isAutor;
    }
}
