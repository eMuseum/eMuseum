package ub.edu.pis2014.pis12.utils;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

/**
 * Classe filla que permet implementar una animacio de rotacio
 *
 * @author Guillem
 */
final public class RotateToggle extends AnimationToggle {
    // Variables adicionals pel control de la rotacio
    private int xType = 0;
    private int yType = 0;
    private float xOffset = 0;
    private float yOffset = 0;

    /**
     * Constructor de la classe
     *
     * @param button        Llensador de l'animacio
     * @param what          Receptor de l'animacio
     * @param animationTime Temps de l'animacio
     */
    public RotateToggle(View button, View what, long animationTime) {
        super(button, what);
        setAnimationTime(animationTime);
        // Valors per defecte
        setMax(180.0F);
        setMin(0.0F);
    }

    /**
     * Estableix els parametres adicionals per a la rotacio
     *
     * @param xType   Tipus de gir en X
     * @param xOffset Distancia en X
     * @param yType   Tipus de gir en Y
     * @param yOffset Distancia en Y
     */
    public void setParams(int xType, float xOffset, int yType, float yOffset) {
        this.xType = xType;
        this.xOffset = xOffset;
        this.yType = yType;
        this.yOffset = yOffset;
    }

    /**
     * Crea l'animacio
     */
    @Override
    protected Animation doAnimation() {
        RotateAnimation anim = null;

        if (toggled) {
            anim = new RotateAnimation(max - getCurrent() + min, max, xType, xOffset, yType, yOffset);
        } else {
            anim = new RotateAnimation(getCurrent(), min, xType, xOffset, yType, yOffset);
        }

        return anim;
    }

    // No gestionat
    @Override
    protected void doAnimationEnd(Animation animation) {
    }

    // No gestionat
    @Override
    protected void doAnimationRepeat(Animation animation) {
    }

    // No gestionat
    @Override
    protected void doAnimationStart(Animation animation) {
    }
}
