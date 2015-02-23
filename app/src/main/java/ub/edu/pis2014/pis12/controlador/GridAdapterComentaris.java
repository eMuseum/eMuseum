package ub.edu.pis2014.pis12.controlador;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import ub.edu.pis2014.pis12.R;
import ub.edu.pis2014.pis12.controlador.APIOperation.TASK_TYPE;
import ub.edu.pis2014.pis12.model.Comentari;
import ub.edu.pis2014.pis12.model.Element;
import ub.edu.pis2014.pis12.model.Usuari;

public class GridAdapterComentaris extends BaseAdapter {
    //Activity del gridAdapter
    private FragmentActivity activity;
    private Context context;
    // Llista de comentaris
    private List<Comentari> comentaris = new ArrayList<Comentari>();
    // Utilitzat durant el proces Item <-> View
    private LayoutInflater inflater;
    // Callback per cridar al modificar els elements
    private Callable<Void> callback = null;
    // Si esta o no carregant
    private boolean loading = false;

    /**
     * Constructor de la classe, inicialitza els comentaris (items)
     *
     * @param context Activity que crea l'adaptador
     * @param isMuseu
     * @param isObra
     */
    public GridAdapterComentaris(FragmentActivity activity) {
        this.context = activity.getApplicationContext();
        inflater = LayoutInflater.from(context);
        this.activity = activity;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
        notifyDataSetChanged();
    }

    public boolean isLoading() {
        return loading;
    }

    /**
     * Afegeix diversos comentaris a l'adapter
     *
     * @param i       Posicio
     * @param Element a afegir a la llista d'items
     */
    public void addItems(ArrayList<Comentari> c) {
        comentaris.addAll(c);
    }

    /**
     * Afegeix un comentari(item) a l'adapter en una posicio
     *
     * @param i       Posicio
     * @param Element a afegir a la llista d'items
     */
    public void addItem(int i, Comentari comentari) {
        comentaris.add(i, comentari);
    }

    /**
     * @return Nombre de comentaris en l'adaptador
     */
    @Override
    public int getCount() {
        return comentaris.size();
    }

    /**
     * Retorna un comentari en concret
     *
     * @param i Index del comentari
     * @return Comentari demanat si existeix
     */
    @Override
    public Comentari getItem(int i) {
        return comentaris.get(i);
    }

    /**
     * Retorna l'index d'un comentari
     *
     * @param ele Element del cual volem l'index
     * @return Index del comentari
     */
    public int getIndex(Element ele) {
        return comentaris.indexOf(ele);

    }

    /**
     * Elimina tots els comentaris de l'adapter
     */
    public void eliminarTots() {
        comentaris.clear();
    }

    /**
     * Retorna l'identificador d'un comentari
     *
     * @param i Posicio del comentari
     * @return L'identificador si es troba
     */
    @Override
    public long getItemId(int i) {
        return comentaris.get(i).getId();
    }

    /**
     * Esborra tots els comentaris de l'adapter
     */
    public void clear() {
        comentaris.clear();
    }

    public void borrarComentari(Comentari comentari) {
        comentaris.remove(comentari);
    }

    /**
     * Inverteix l'ordre de la llista
     */
    public void Reverse() {
        Collections.reverse(comentaris);
    }

    /**
     * Crea la vista per cada un dels comentaris. es a dir, assigna els
     * valors a cada un dels Widgets del layout pel comentari.
     *
     * @param i         Posicio/index
     * @param view      Layout a utilitzar
     * @param viewGroup
     * @return La vista/layout amb els camps omplerts
     */
    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        // Declarem cada un dels elements que ens interessen
        View v = view;
        final TextView TextViewUsu;
        // No tenim vista? Utitilitzem la vista per defecte
        if (v == null) {
            // Inicialitzem els valors per defecte
            v = inflater.inflate(R.layout.view_griditemcomentari, viewGroup, false);
            v.setTag(R.id.griditemcomentari_title, v.findViewById(R.id.griditemcomentari_title));
            v.setTag(R.id.textView_usuari, v.findViewById(R.id.textView_usuari));
            v.setTag(R.id.textView_date, v.findViewById(R.id.textView_date));
            v.setTag(R.id.griditem_editar, v.findViewById(R.id.griditem_editar));
            v.setTag(R.id.griditem_borrar, v.findViewById(R.id.griditem_borrar));
        }
        //Obtenim els textsView on anira la data i l'autor del comentari
        final TextView data = (TextView) v.getTag(R.id.textView_date);
        TextView usuari = (TextView) v.getTag(R.id.textView_usuari);

        // Obtenim el comentari, i el text on colocarem l'String del comentari
        final Comentari comentari = getItem(i);
        TextViewUsu = (TextView) v.getTag(R.id.griditemcomentari_title);

        //Posem el text
        TextViewUsu.setText(comentari.getText());
        //Posem la data
        data.setText(comentari.getDataFormat());
        //Posem l'usuari (autor del comentari)
        usuari.setText(comentari.getUsuari() + ":");

        final ImageView icono_borrar = (ImageView) v.getTag(R.id.griditem_borrar);
        final ImageView icono_editar = (ImageView) v.getTag(R.id.griditem_editar);

        if (Usuari.get().isLogged()) {
            if (comentari.getUsuari().toLowerCase(Locale.ENGLISH).equals(Usuari.get().getUsername().toLowerCase(Locale.ENGLISH))) {
                //Quan clickes la ImageView del icono borrar
                icono_borrar.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);

                        dialog.setMessage(R.string.grid_comentaris_borrar);
                        dialog.setCancelable(false);
                        dialog.setPositiveButton(R.string.dialog_si, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                API.StartOperation(context, TASK_TYPE.API_DELETE_COMMENT, new APINotifier() {

                                    @Override
                                    public void onResult(boolean result) {
                                        if (result) {
                                            borrarComentari(comentari);
                                            notifyDataSetChanged();
                                        }
                                    }
                                }, String.valueOf(comentari.getId()));
                            }
                        });
                        dialog.setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        dialog.show();

                    }
                });

                //Quan cliques al icono d'editar
                icono_editar.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
                        final EditText editText = new EditText(activity);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT);
                        editText.setLayoutParams(lp);
                        editText.setText(TextViewUsu.getText());
                        dialog.setMessage(R.string.grid_comentaris_editar_titol);
                        dialog.setCancelable(false);
                        dialog.setView(editText);
                        dialog.setPositiveButton(R.string.grid_comentaris_editar, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                API.StartOperation(context, TASK_TYPE.API_EDIT_COMMENT, new APINotifier() {

                                    @Override
                                    public void onResult(boolean result) {
                                        if (result) {
                                            TextViewUsu.setText(editText.getText().toString());
                                        }
                                    }
                                }, String.valueOf(comentari.getId()), editText.getText().toString());
                            }
                        });
                        dialog.setNegativeButton(R.string.dialog_cancelar, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        dialog.show();

                    }
                });
            } else {
                icono_editar.setVisibility(View.INVISIBLE);
                icono_borrar.setVisibility(View.INVISIBLE);
            }
        } else {
            icono_editar.setVisibility(View.INVISIBLE);
            icono_borrar.setVisibility(View.INVISIBLE);
        }

        v.setBackgroundResource(R.drawable.widget_comentari_dark);


        return v;
    }

    public View getLoadingView(ViewGroup viewGroup) {
        View v = LayoutInflater.from(context).inflate(R.layout.view_griditemloading, viewGroup, false);
        v.setBackgroundResource(R.drawable.widget_comentari_dark);

        return v;
    }

    public void setCallable(Callable<Void> callback) {
        this.callback = callback;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (callback != null) {
            try {
                callback.call();
            } catch (Exception e) {
            }
        }
    }
}
