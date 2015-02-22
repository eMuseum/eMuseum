package ub.edu.pis2014.pis12.controlador;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

/**
 * Adaptador del nostre PageViewer que ens permet mostrar cada un dels
 * Fragments amb un Swipe
 */
public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
    private ArrayList<Fragment> fragments = null;
    private FragmentManager fm = null;
    private Fragment custom = null;

    /*
     * Constructor, requereix un manager de fragments
     */
    public ScreenSlidePagerAdapter(FragmentManager fm) {
        super(fm);

        fragments = new ArrayList<Fragment>();
        this.fm = fm;
    }

    /**
     * Afegeix un element a l'adapter
     *
     * @param fragment Fragment a afegir
     */
    public void addItem(Fragment fragment) {
        fragments.add(fragment);
    }

    /**
     * Donada una posicio retorna l'index.
     *
     * @param position Posicio del Fragment
     * @return Fragment de la posicio
     */
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    /**
     * Permet modificar el fragment central, mostrant-ne un altre
     * en lloc de l'original
     *
     * @param fragment Fragment nou a mostrar
     */
    public void customize(Fragment fragment) {
        // Eliminem el primer (cache)
        fm.beginTransaction().remove(getItem(1)).commitAllowingStateLoss();
        // Guardem el propi
        custom = fragment;
        // Notifiquem canvis
        notifyDataSetChanged();
    }

    /**
     * Retorna el nombre d'elements, en aquest cas 3
     */
    @Override
    public int getCount() {
        return fragments.size();
    }

    /**
     * Permet indicar si hi ha hagut algun canvi en alguna posicio
     */
    @Override
    public int getItemPosition(Object object) {
        /*
		 * Si tenim un element customitzat, i l'objecte amb el que es crida
		 * es l'antic, i no pas el nou
		 */
        if (custom != null && object == getItem(1) && object != custom) {
            // Actualitzem l'element en la llista
            fragments.set(1, custom);
            // Retornem que ha estat modificat
            return POSITION_NONE;
        }

        // Retornem que tot segueix igual
        return POSITION_UNCHANGED;
    }
}
