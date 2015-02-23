package ub.edu.pis2014.pis12.controlador;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ub.edu.pis2014.pis12.InfoActivity;
import ub.edu.pis2014.pis12.InfoFragment;
import ub.edu.pis2014.pis12.LlistaObresFragment;
import ub.edu.pis2014.pis12.R;
import ub.edu.pis2014.pis12.model.Autor;
import ub.edu.pis2014.pis12.model.Dades;
import ub.edu.pis2014.pis12.model.Element;
import ub.edu.pis2014.pis12.model.Museu;
import ub.edu.pis2014.pis12.model.Obra;
import ub.edu.pis2014.pis12.model.TIPUS_ELEMENT;
import ub.edu.pis2014.pis12.utils.AlphaBoth;
import ub.edu.pis2014.pis12.utils.ElementImageManager;
import ub.edu.pis2014.pis12.utils.MapUtil;
import ub.edu.pis2014.pis12.utils.OnMapUpdate;
import ub.edu.pis2014.pis12.utils.Utils;

public class GridAdapterElements extends BaseAdapter {
    //Activity del gridAdapter
    private FragmentActivity activity;
    private Context context;
    private GridView grid;

    // Llista d'elements
    private List<Element> items = new ArrayList<Element>();

    // Utilitzat durant el proccs Item <-> View
    private LayoutInflater inflater;

    public enum TIPUS_ORDENACIO {
        TIPUS_PER_TIPUS,
        TIPUS_ALFABETIC,
        TIPUS_ALFABETIC_INVERS,
        TIPUS_VALORACIONS,
        TIPUS_VALORACIONS_INVERS
    }

    ;

    // Tipus d'elements que mostrem
    private boolean museus = false;
    private boolean obres = false;
    private boolean autors = false;

    // Variables per indicar a la DB per on buscar
    private int startMuseus = 0;
    private int startObres = 0;
    private int startAutors = 0;
    private int startMultiple = 0;
    private final static int LIMIT = 25;
    private int limit = LIMIT;

    // Limitadors de cerca
    private String searchString = "";
    TIPUS_ORDENACIO ordenacio = null;


    /**
     * Constructor de la classe, inicialitza els elements (items)
     *
     * @param context Activity que crea l'adaptador
     */
    public GridAdapterElements(GridView grid, FragmentActivity activity, Context context, boolean info) {
        this.grid = grid;
        inflater = LayoutInflater.from(context);
        this.activity = activity;
        this.context = context;
        this.startMuseus = 0;
        this.startObres = 0;
        this.startAutors = 0;
        this.startMultiple = 0;

        // Establim la activity en el descarregador
        ElementImageManager.setActivity(activity);
    }

    /**
     * Actualitza l'element a partir del qual retornem de la
     * base de dades
     *
     * @return Retorna true si es una ceca mixta i cal buidar el grid
     */
    public boolean loadMore() {
        // Intentem omplir amb mes elements
        return populate(false);
    }

    /**
     * Afegeix elements a l'adapter segons les opciones preestablertes, tals com
     * quins tipus afegir, parametres de cerca i des de quin element fer-ho
     *
     * @return Retorna true si s'afegeix algun element
     */
    public boolean populate(boolean cl) {
        // Elements actuals i limit des del que afegir
        int current = getCount();

        if (cl) {
            clear();
        }

        // Afegim tots els elements
        if (!isMultiSearch()) {
            if (museus)
                afegirMuseus();

            if (obres)
                afegirObres();

            if (autors)
                afegirAutors();
        } else {
            afegirCercaMultiple();
        }

        // Guardem si hi ha hagut canvis
        boolean changed = current != getCount();
        // Si hi ha hagut canvis o hem fer un clear
        if (changed || cl) {
            // Notifiquem
            notifyDataSetChanged();
            return changed;
        }

        return false;
    }

    /**
     * Retorna true si estem fent una cerca de mes d'un tipus d'element
     * a la vegada.
     *
     * @return Retorna true si es cerca multiple, false sino
     */
    private boolean isMultiSearch() {
        return ((museus ? 1 : 0) + (obres ? 1 : 0) + (autors ? 1 : 0)) > 1;
    }

    /**
     * Indica si volem o no museus en la cerca
     * Reinicia el punt per on els buquem i el limit a afegir
     *
     * @param state Per buscar true, sino false
     */
    public void setMuseus(boolean state) {
        museus = state;
        startMuseus = startObres = startAutors = startMultiple = 0;
        populate(true);
    }

    /**
     * Indica si volem o no obres en la cerca
     * Reinicia el punt per on els buquem i el limit a afegir
     *
     * @param state Per buscar true, sino false
     */
    public void setObres(boolean state) {
        obres = state;
        startMuseus = startObres = startAutors = startMultiple = 0;
        populate(true);
    }

    /**
     * Indica si volem o no autors en la cerca
     * Reinicia el punt per on els buquem i el limit a afegir
     *
     * @param state Per buscar true, sino false
     */
    public void setAutors(boolean state) {
        autors = state;
        startMuseus = startObres = startAutors = startMultiple = 0;
        populate(true);
    }

    /**
     * Modifica la cadena Where (la limitacio de cerca en la DB)
     *
     * @param s Cadena de cerca
     */
    public void setWhere(String s) {
        searchString = s;
        startMuseus = startObres = startAutors = startMultiple = 0;
    }

    /**
     * Retorna la cadena de cerca, previament establerta o null
     *
     * @return Cadena de cerca
     */
    public String getWhere() {
        if (searchString.length() > 0)
            return searchString;
        return null;
    }

    /**
     * Afegeix elements buscant-los amb una query especial per tal
     * de poder ordenar i afegir multiples a la vegada
     */
    private void afegirCercaMultiple() {
        String where = getWhere();
        String order = getOrdenacio();

        ArrayList<Element> elements = Dades.cercaMixta(museus, autors, obres, where, order, startMultiple + "," + limit);
        startMultiple += elements.size();
        items.addAll(elements);
    }

    /**
     * Afegeix museus, segons un limit i una cerca (que pot ser null, cap)
     */
    private void afegirMuseus() {
        ArrayList<Museu> elements = Dades.getMuseus(startMuseus, limit, getWhere(), getOrdenacio());
        startMuseus += elements.size();
        items.addAll(elements);
    }

    /**
     * Afegeix obres, segons un limit i una cerca (que pot ser null, cap)
     */
    private void afegirObres() {
        ArrayList<Obra> elements = Dades.getObres(startObres, limit, getWhere(), getOrdenacio());
        startObres += elements.size();
        items.addAll(elements);
    }

    /**
     * Afegeix autors, segons un limit i una cerca (que pot ser null, cap)
     */
    public void afegirAutors() {
        ArrayList<Autor> elements = Dades.getAutors(startAutors, limit, getWhere(), getOrdenacio());
        startAutors += elements.size();
        items.addAll(elements);
    }

    /**
     * Afegeix un item a l'adapter
     *
     * @param elem Element a afegir a la llista d'items
     */
    public void setItem(Element elem) {
        items.add(elem);
    }

    /**
     * @return Nombre d'elements en l'adaptador
     */
    @Override
    public int getCount() {
        return items.size();
    }

    /**
     * Retorna un item en concret
     *
     * @param i Index de l'item
     * @return Item demanat si existeix
     */
    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    /**
     * Reordena especificant el tipus
     *
     * @param ordenacio Tipus d'ordenacio
     */
    public void ordenar(TIPUS_ORDENACIO ordenacio) {
        // Guardem el tipus d'ordenacio
        this.ordenacio = ordenacio;

		/*
		 * Per tal de reordenar hem de borrar i afegir exactament els elements que
		 * tenim. Per tant, el que fem es posar el limit al nombre d'elements totals que
		 * ara ja tenim (startX) i el comencament startX a 0.
		 */
        if (isMultiSearch()) {
            limit = startMultiple;
            startMultiple = 0;
        } else {
            if (museus) {
                limit = startMuseus;
                startMuseus = 0;
            } else if (obres) {
                limit = startObres;
                startObres = 0;
            } else if (autors) {
                limit = startAutors;
                startAutors = 0;
            }
        }

        // Recarreguem tot i en ordre
        populate(true);

        // Restaurem el limit
        limit = LIMIT;
    }

    /**
     * Retorna una string amb la ordenacio adequada.
     * Null significa que no s'ordenin els elements o be, si es una
     * cerca multiple (no un sol tipus d'element), ordena per tipus.
     *
     * @return Cadena d'ordenacio o null (per tipus)
     */
    public String getOrdenacio() {
        if (ordenacio == null)
            return null;

        switch (ordenacio) {
            case TIPUS_PER_TIPUS:
                return null;

            case TIPUS_ALFABETIC:
                return "nom ASC";

            case TIPUS_ALFABETIC_INVERS:
                return "nom DESC";

            case TIPUS_VALORACIONS:
                return "valoracio DESC";

            case TIPUS_VALORACIONS_INVERS:
                return "valoracio ASC";

            default:
                return null;
        }
    }

    /**
     * Elimina tots els elements de l'adapter
     */
    private void clear() {
        items.clear();
    }

    /**
     * Retorna l'identificador d'un item
     *
     * @param i Posicio de l'item
     * @return L'identificador si es troba
     */
    @Override
    public long getItemId(int i) {
        return items.get(i).getId();
    }

    /**
     * Crea la vista per cada un dels elements. Es a dir, assigna els
     * valors a cada un dels Widgets del layout per l'element.
     *
     * @param position         Posicio/index
     * @param view      Layout a utilitzar
     * @param viewGroup
     * @return La vista/layout amb els camps omplerts
     */
    @Override
    public View getView(final int position, View view, final ViewGroup viewGroup) {
// Declarem cada un dels elements que ens interessen
        View v = view;

        // No tenim vista? Utitilitzem la vista per defecte
        if (v == null) {
            // Inicialitzem els valors per defecte
            v = inflater.inflate(R.layout.view_griditem, viewGroup, false);
            v.setTag(R.id.griditem_imatge, v.findViewById(R.id.griditem_imatge));
            v.setTag(R.id.griditem_title, v.findViewById(R.id.griditem_title));
            v.setTag(R.id.griditem_subinfo, v.findViewById(R.id.griditem_subinfo));
            v.setTag(R.id.griditem_info, v.findViewById(R.id.griditem_info));
            v.setTag(R.id.view_element, v.findViewById(R.id.view_element));
        }

        // Obtenim l'Item, els widgets, i el ImageView de la info

        final Element element = items.get(position);
        final ImageView icono_info = (ImageView) v.getTag(R.id.griditem_info);
        final ImageView imatge = (ImageView) v.getTag(R.id.griditem_imatge);
        final TextView title = (TextView) v.getTag(R.id.griditem_title);
        final TextView text_subinfo = (TextView) v.getTag(R.id.griditem_subinfo);
        final View view_element = (View) v.getTag(R.id.view_element);
        final Resources resources = context.getResources();
        if (element instanceof Museu)
            icono_info.setImageDrawable(resources.getDrawable(R.mipmap.info_verde));

        else if (element instanceof Autor)
            icono_info.setImageDrawable(resources.getDrawable(R.mipmap.info_rosa));

        //Quan toques la ImageView de la info
        icono_info.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Canvia la imatge que mostra per donar la sensacio de clicat
                // Retornem sempre false per indicar que no hem acabat la gestio (onClick)

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (element instanceof Museu)
                            icono_info.setImageDrawable(resources.getDrawable(R.mipmap.info_verde_clicked));

                        else if (element instanceof Autor)
                            icono_info.setImageDrawable(resources.getDrawable(R.mipmap.info_rosa_clicked));

                        break;

                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        if (element instanceof Museu)
                            icono_info.setImageDrawable(resources.getDrawable(R.mipmap.info_verde));

                        else if (element instanceof Autor)
                            icono_info.setImageDrawable(resources.getDrawable(R.mipmap.info_rosa));

                        break;
                }

                return false;
            }
        });

        //Quan clickes la ImageView de la info
        icono_info.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	/*
            	 * Declaracio intent per pasar a infoObresActivity
            	 * Al clicar en el boto info d'un museu canviem d'activity passem l'ID del museu
            	 * i el boolea indicant si es una obra o un museu
            	 */

                if (Utils.isExpandedMode(context)) {
                    ViewPager pager = (ViewPager) activity.findViewById(R.id.main_pager);
                    ScreenSlidePagerAdapter adapter = (ScreenSlidePagerAdapter) pager.getAdapter();
                    Fragment fragment = null;

                    new AlphaBoth(null, pager, 500).execute();

                    if (element instanceof Museu) {
                        fragment = InfoFragment.newInstance((Museu) element);
                    } else if (element instanceof Autor) {
                        fragment = InfoFragment.newInstance((Autor) element);
                    }

                    adapter.customize(fragment);
                    pager.setCurrentItem(1, true);
                } else {
                    Intent i = new Intent(context, InfoActivity.class);
                    i.putExtra("id", element.getId());
                    i.putExtra("tipus", TIPUS_ELEMENT.getFromElement(element));

                    if (element instanceof Museu)
                        icono_info.setImageDrawable(resources.getDrawable(R.mipmap.info_verde));

                    else if (element instanceof Autor)
                        icono_info.setImageDrawable(resources.getDrawable(R.mipmap.info_rosa));

                    context.startActivity(i);
                }
            }
        });

        //Set the image, if not exists, download from server
        ImageController.getInstance().setImageWithURL(grid, activity.getApplicationContext(), element.getImatgeURL(),
                imatge, position);
        // Posem el text
        title.setText(element.getTitol());

        //Establim el color i la visibilitat del icono 'info' segons el tipus d'element
        if (element instanceof Museu) {
            icono_info.setVisibility(View.VISIBLE);
            text_subinfo.setVisibility(View.VISIBLE);

            //Per veure com queda i ajustar els textView
            v.setBackgroundResource(R.drawable.widget_element);
            view_element.setBackgroundResource(R.drawable.widget_museu_element);

            text_subinfo.setText(MapUtil.getDistanceFromLastKnown(((Museu) element).getLatitud(), ((Museu) element).getLongitud()));

            MapUtil.addListener(new OnMapUpdate((Museu) element) {
                public void onUpdate(double distance) {

                    if (distance < 1000)
                        text_subinfo.setText((int) distance + "m");
                    else
                        text_subinfo.setText((int) distance / 1000 + "km");
                }
            });
            /*TODO
            Location location = new Location("Stored");
            location.setLatitude(((Museu) element).getLatitud());
            location.setLongitude(((Museu) element).getLongitud());
            text_subinfo.setText(MapController.getInstance().getDistanceTo(location));
            */
        } else if (element instanceof Obra) {
            icono_info.setVisibility(View.GONE);
            text_subinfo.setVisibility(View.VISIBLE);
            text_subinfo.setText(Dades.getAutor(((Obra) element).getAutorId()).getTitol());
            v.setBackgroundResource(R.drawable.widget_element);
            view_element.setBackgroundResource(R.drawable.widget_obra_element);

            if (LlistaObresFragment.getTipus())
                text_subinfo.setText(Dades.getMuseu(((Obra) element).getMuseuId()).getTitol());
            else
                text_subinfo.setText(Dades.getAutor(((Obra) element).getAutorId()).getTitol());
        } else if (element instanceof Autor) {
            icono_info.setVisibility(View.VISIBLE);
            text_subinfo.setVisibility(View.GONE);
            v.setBackgroundResource(R.drawable.widget_element);
            view_element.setBackgroundResource(R.drawable.widget_autor_element);
        }

        return v;
    }
}
