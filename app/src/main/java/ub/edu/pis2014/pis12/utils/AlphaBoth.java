package ub.edu.pis2014.pis12.utils;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

/**
 * Classe filla que permet implementar una animacio de l'opacitat
 *
 * @author Guillem
 */
final public class AlphaBoth extends AnimationToggle {

    /**
     * Constructor de la classe
     *
     * @param button        Llen�ador de l'animacio
     * @param what          Receptor de l'animacio
     * @param animationTime Temps de l'animacio
     */
    public AlphaBoth(View button, View what, long animationTime) {
        super(button, what);
        setAnimationTime(animationTime / 2);
        // Valors per defecte
        setMax(1.0F);
        setMin(0.0F);
    }

    /**
     * Crea l'animacio
     */
    @Override
    protected Animation doAnimation() {
        AlphaAnimation anim = null;

        if (!toggled) {
            anim = new AlphaAnimation(max, min);
        } else {
            anim = new AlphaAnimation(min, max);
        }

        return anim;
    }

    /**
     * Si s'acaba l'animacio
     */
    @Override
    protected void doAnimationEnd(Animation animation) {
        /* Si no esta canviada (!toggled) significa que ara ha
		 * fet la transicio cap a no visible
		 */
        if (toggled)
            doAnimation();
    }

    /**
     * No gestionem aquest event
     */
    @Override
    protected void doAnimationRepeat(Animation animation) {
    }

    /**
     * No gestionem aquest event
     */
    @Override
    protected void doAnimationStart(Animation animation) {
    }
}
