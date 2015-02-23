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
import ub.edu.pis2014.pis12.utils.MapUtil;
import ub.edu.pis2014.pis12.utils.OnMapUpdate;
import ub.edu.pis2014.pis12.utils.Utils;

public class GridAdapterElements extends BaseAdapter {
    TIPUS_ORDENACIO ordenacio = null;
    //Activity del gridAdapter
    private FragmentActivity activity;
    private Context context;
    private GridView grid;
    // Llista d'elements
    private List<Element> items = new ArrayList<Element>();
    // Utilitzat durant el proccs Item <-> View
    private LayoutInflater inflater;
    ;
    // Tipus d'elements que mostrem
    private boolean museus = false;
    private boolean obres = false;
    private boolean autors = false;

    // Limitadors de cerca
    private String searchString = "";

    /**
     * Constructor de la classe, inicialitza els elements sense cap autor/museu definit
     *
     * @param activity Activity que crea l'adaptador
     * @param grid     gridView que conte aquesta classe
     */
    public GridAdapterElements(GridView grid, FragmentActivity activity) {
        this.grid = grid;
        this.context = activity.getApplicationContext();
        this.inflater = LayoutInflater.from(context);
        this.activity = activity;
    }

    /**
     * Constructor de la classe, inicialitza els elements amb autor/museu definit
     *
     * @param activity Activity que crea l'adaptador
     * @param grid gridView que conte aquesta classe
     */
    public GridAdapterElements(GridView grid, FragmentActivity activity, Element element) {
        this.grid = grid;
        this.context = activity.getApplicationContext();
        this.inflater = LayoutInflater.from(context);
        this.activity = activity;

        if (element instanceof Museu) {
            items = Dades.getObres((Museu) element);
        } else if (element instanceof Autor) {
            items = Dades.getObres((Autor) element);
        }
    }

    /**
     * Indica si volem o no museus en la cerca
     *
     * @param state Per buscar true, sino false
     **/
    public void setMuseus(boolean state) {
        museus = state;
        reloadData();
    }

    /**
     * Indica si volem o no obres en la cerca
     *
     * @param state Per buscar true, sino false
     **/
    public void setObres(boolean state) {
        obres = state;
        reloadData();
    }

    /**
     * Indica si volem o no autors en la cerca
     *
     * @param state Per buscar true, sino false
     **/
    public void setAutors(boolean state) {
        autors = state;
        reloadData();
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
     * Modifica la cadena Where (la limitacio de cerca en la DB)
     *
     * @param s Cadena de cerca
     */
    public void setWhere(String s) {
        searchString = s;
    }

    /**
     * Reload data with parameters order, checked elements, a search text
     */
    public void reloadData() {
        String where = getWhere();
        String order = getOrdenacio();

        ArrayList<Element> elements = Dades.cercaMixta(museus, autors, obres, where, order, null);
        items = elements;
        notifyDataSetChanged();
    }

    /**
     * Reordena especificant el tipus
     *
     * @param ordenacio Tipus d'ordenacio
     */
    public void ordenar(TIPUS_ORDENACIO ordenacio) {
        // Guardem el tipus d'ordenacio
        this.ordenacio = ordenacio;
        reloadData();
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

        imatge.setVisibility(View.INVISIBLE);

        //Set the image, if not exists, download from server
        ImageController.getInstance().setImageWithURL(grid, activity.getApplicationContext(), element.getImatgeURL(),
                imatge, position);
        // Posem el text
        title.setText(element.getTitol());

        //Establim el color i la visibilitat del icono 'info' segons el tipus d'element
        if (element instanceof Museu) {
            icono_info.setImageDrawable(resources.getDrawable(R.mipmap.info_verde));
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

        } else if (element instanceof Obra) {
            icono_info.setVisibility(View.GONE);
            text_subinfo.setVisibility(View.VISIBLE);
            text_subinfo.setText(Dades.getAutor(((Obra) element).getAutorId()).getTitol());
            v.setBackgroundResource(R.drawable.widget_element);
            view_element.setBackgroundResource(R.drawable.widget_obra_element);

            if (LlistaObresFragment.getTipus())
                text_subinfo.setText(Dades.getMuseu(element.getMuseuId()).getTitol());
            else
                text_subinfo.setText(Dades.getAutor(((Obra) element).getAutorId()).getTitol());
        } else if (element instanceof Autor) {
            icono_info.setImageDrawable(resources.getDrawable(R.mipmap.info_rosa));
            icono_info.setVisibility(View.VISIBLE);
            text_subinfo.setVisibility(View.GONE);
            v.setBackgroundResource(R.drawable.widget_element);
            view_element.setBackgroundResource(R.drawable.widget_autor_element);
        }

        return v;
    }

    public enum TIPUS_ORDENACIO {
        TIPUS_PER_TIPUS,
        TIPUS_ALFABETIC,
        TIPUS_ALFABETIC_INVERS,
        TIPUS_VALORACIONS,
        TIPUS_VALORACIONS_INVERS
    }
}
