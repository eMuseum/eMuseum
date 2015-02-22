package ub.edu.pis2014.pis12.utils;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

/**
 * Permet implementar en un GridView o en un ScrollViewEx
 * que al arribar a baix de tot de l'scroll, es carreguin nous elements
 * si l'usuari continua l'accio d'scroll.
 * <p/>
 * S'implementa sobre ScrollViewEx i no sobre ScrollView perque el segon
 * no te cap metode public per posar un listener d'scroll.
 *
 * @author Guillem
 */
public class ScrollLoader {
    // Variables relacionades amb el gridview desti
    private ScrollLoaderListener listener = null;

    // Carregar nous elements
    private boolean esperarScroll = false;
    private boolean carregarElements = false;
    private float startY = 0;
    float alphaY = 0;

    /**
     * Constructor per GridView
     *
     * @param target   Grid sobre el que actua
     * @param listener Classe que escolta canvis
     */
    public ScrollLoader(AbsListView target, ScrollLoaderListener listener) {
        this.listener = listener;

        target.setOnScrollListener(scrollListener);
        target.setOnTouchListener(touchListener);
    }

    /**
     * Constructor per ScrollViewEx
     *
     * @param target   ScrollViewEx sobre el que actua
     * @param listener Classe que escolta canvis
     */
    public ScrollLoader(ScrollViewEx target, ScrollLoaderListener listener) {
        this.listener = listener;

        target.setScrollViewListener(scrollViewListener);
        target.setOnTouchListener(touchListener);
    }

    /**
     * Classe que sobreescriu els metodes que escolten canvis en l'scroll
     * d'un GridView
     */
    private OnScrollListener scrollListener = new OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        // En cas de produirse scroll (voluntari o no)
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {

            // Volem esperar a que l'usuari faci un scroll mes gran del compte
            // Pero solament si el nombre d'elements que estem mostrant es el mateix que el total
            // d'elements del grid
            esperarScroll = (listener.isEnabled() && (firstVisibleItem + visibleItemCount) >= totalItemCount);

            // L'anterior comprobacio podria donar peu a errors, com ara no poder veure sencer
            // l'ultim element del gridview
            if (esperarScroll) {
                // Obtenim l'ultim element
                View v = view.getChildAt(visibleItemCount - 1);
                if (v != null) {
                    // Si no hi ha marge amagat (diff == 0), podem continuar, sino no
                    int diff = (v.getBottom() - (view.getHeight() + view.getScrollY()));
                    esperarScroll = diff == 0;
                }
            }
        }
    };

    /**
     * Classe que sobreescriu els metodes que escolten canvis en l'scroll
     * d'un ScrollViewEx
     */
    private ScrollViewListener scrollViewListener = new ScrollViewListener() {

        @Override
        public void onScrollChanged(ScrollViewEx scrollView, int x, int y,
                                    int oldx, int oldy) {

            // Busquem la posicio de l'ultim element i la comparem amb l'scroll actual
            View view = (View) scrollView.getChildAt(scrollView.getChildCount() - 1);
            int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));
            esperarScroll = (listener.isEnabled() && diff == 0);
        }
    };

    /**
     * Classe que sobreescriu els metodes que escolten canvis en els
     * events touch d'un GridView o d'un ScrollViewEx indiferentment
     */
    private OnTouchListener touchListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // Si la accio es apretar i estem esperant scroll (estem al final del grid)
            if (event.getAction() == MotionEvent.ACTION_DOWN && esperarScroll) {
                listener.onEventDown();

                // Evitem l'accio per defecte d'android
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_MOVE && esperarScroll) {
                // Si just comencem l'accio
                if (startY == 0) {
                    // Indiquem que de moment no hi ha hagut desplacament Y
                    alphaY = 0;
                    // Guardem la Y inicial
                    startY = event.getY();
                } else {
                    // Actualitzem el desplacament, orientant-l'ho cap adalt
                    alphaY = startY - event.getY();

                    // Caldra carregar nous elements solament quan haguem pujat 1/3 del
                    // tamany total del grid, i quan deixi anar
                    carregarElements = (alphaY >= v.getHeight() / 3.0f);

                    listener.onEventMove(alphaY, carregarElements);

                    // Si estem pujant o si la Y actual es menor a la d'inici (es a dir, estem "cancelant")
                    // No volem que android faci l'accio per defecte, aixi que retornem true
                    return (alphaY > 0 && event.getY() < startY);
                }
            }
            // Si deixem anar o cancelem
            else if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) && esperarScroll) {
                listener.onEventUp(alphaY, carregarElements);

                // Restablir les variables
                startY = 0;
                carregarElements = false;

                // Si no hi ha hagut desplacament, volem que android faci la accio per defecte,
                // Sino, no ho volem
                return alphaY != 0;
            }

            return false;
        }
    };
}
