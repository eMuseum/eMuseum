package ub.edu.pis2014.pis12.utils;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Gestiona de forma externa una animacio que ocorri al fer click
 * sobre un element.
 * Adicionalment, la animacio pot estar dirigida a un segon element
 * diferent del que ho llenca.
 *
 * @author Guillem
 */
public abstract class AnimationToggle {
    // Ultim click al llencador
    protected long lastClick = 0;
    // Estat actual
    protected boolean toggled = false;
    // Animant ara mateix
    protected boolean running = false;

    // Temps d'animacio
    protected long animationTime = 1000;
    // Valor minim de l'animacio
    protected float min = 0;
    // Valor maxim de l'animacio
    protected float max = 0;

    // Llencador de l'animacio, qui l'inicia
    protected View button = null;
    // Receptor de l'animacio, qui la rep
    protected View what = null;

    // Notificador d'events
    private AnimationNotifier notifier = null;

    /* Ens permet tenir mes d'una animacio per View, d'altre forma solament
     * prodriem tenir 1 animacio per cada llencador i receptor
     */
    private static Map<View, ArrayList<AnimationToggle>> animationsMap = null;

    static {
        animationsMap = new HashMap<View, ArrayList<AnimationToggle>>();
    }

    /**
     * Constructor de l'animacio
     * MODIFICA el click listener
     *
     * @param button Qui llenca l'animacio
     * @param what   Qui rep l'animacio
     */
    AnimationToggle(View button, View what) {
        // Ho guardem
        this.button = button;
        this.what = what;

        if (button == null)
            return;

        // Si es el primer cop que afegim aquest boto
        if (!animationsMap.containsKey(button)) {
            // Afegim el click listener
            button.setOnClickListener(clickListener);
            // Creem la llista d'animacions
            ArrayList<AnimationToggle> list = new ArrayList<AnimationToggle>();
            // Ens afegim a la llista
            list.add(this);
            // Afegim el llencador
            animationsMap.put(button, list);
        } else {
            // Obtenim la llista
            ArrayList<AnimationToggle> list = animationsMap.get(button);
            // Ens hi afegim
            list.add(this);
        }
    }

    /**
     * Temps transcorregut des de l'ultim cop que s'ha crida
     * aquesta mateixa funcio
     *
     * @return Temps en milisegons
     */
    protected long elapsed() {
        long now = new Timestamp((new Date()).getTime()).getTime();
        long t = now - lastClick;
        lastClick = now;

        return t;
    }

    /**
     * Retorna el valor actual de l'animacio, entre min y max
     *
     * @return Valor de l'animacio
     */
    protected float getCurrent() {
        long t = elapsed();
        if (t > animationTime)
            return max;

        return t * max / (float) animationTime;
    }

    /**
     * Estableix el temps que durara l'animacio
     *
     * @param animationTime Temps en milisegons
     */
    public void setAnimationTime(long animationTime) {
        this.animationTime = animationTime;
    }

    /**
     * Retorna el temps que durara l'animacio
     *
     * @return Temps en milisegons
     */
    public long getAnimationTime() {
        return this.animationTime;
    }

    /**
     * Estableix el valor minim de l'animacio
     *
     * @param min Valor minim
     */
    public void setMin(float min) {
        this.min = min;
    }

    /**
     * Estableix el valor maxim de l'animacio
     *
     * @param max Valor maxim
     */
    public void setMax(float max) {
        this.max = max;
    }

    /**
     * Modifica l'estat actual de l'animacio, previ
     * a qualsevol crida
     */
    public AnimationToggle setToggled() {
        toggled = !toggled;
        return this;
    }

    /**
     * Retorna el receptor de l'animacio
     *
     * @return Receptor de l'animacio
     */
    private View getWhat() {
        return this.what;
    }

    /**
     * Estableix un notificador d'execucio d'events
     *
     * @param notifier Callback pel notificador
     */
    public void setAnimationNotifier(AnimationNotifier notifier) {
        this.notifier = notifier;
    }

    /**
     * Crida el metode del notificador si se n'ha establit un
     */
    private void fireNotification() {
        if (notifier != null)
            notifier.onToggled(this, toggled);
    }

    /**
     * Objecte estatic que gestionara tots els clicks als elements llencadors
     */
    private static OnClickListener clickListener = new View.OnClickListener() {
        /**
         * En cas de detectar click
         *
         * @param v Element llen�ador
         */
        @Override
        public void onClick(View v) {
            // Obtenim la llista per aquest llencador
            ArrayList<AnimationToggle> list = animationsMap.get(v);
            Iterator<AnimationToggle> it = list.iterator();

			/*
			 *  Ens permet tenir una llista de les animacions que executa el llencador
			 *  organitzada segons els receptors, i per tant, realitzar-les a la vegada
			 */
            Map<View, AnimationSet> animations = new HashMap<View, AnimationSet>();

            // Per cada animacio
            while (it.hasNext()) {
                // Obtenim l'objecte
                AnimationToggle toggle = (AnimationToggle) it.next();
                // Canviem l'estat
                toggle.setToggled();

                // Generem l'animacio
                Animation animation = toggle.doAnimation();
                // Parametres de l'animacio
                animation.setAnimationListener(toggle.animationListener);
                animation.setDuration(toggle.getAnimationTime());
                animation.setFillAfter(true);
				
				/*
				 * Un AnimationSet ens permet realitzar mes d'una animacio
				 * en un sol element.
				 * Per tant, afegirem les animacions al set i despres l'executarem
				 */
                AnimationSet rootSet = null;
                // Si ja esta creat (no es el primer element)
                if (animations.containsKey(toggle.getWhat())) {
                    rootSet = animations.get(toggle.getWhat());
                }
                // Sino el creem
                else {
                    rootSet = new AnimationSet(true);
                    rootSet.setInterpolator(new AccelerateInterpolator());
                    animations.put(toggle.getWhat(), rootSet);
                }

                // Els canvis han de persistir
                rootSet.setFillAfter(true);
                // Afegim l'animacio
                rootSet.addAnimation(animation);

                // Notifiquem que hem executat
                toggle.fireNotification();
            }

            // Per cada element que hem d'animar, realitzem el set
            for (Map.Entry<View, AnimationSet> entry : animations.entrySet()) {
                entry.getKey().startAnimation(entry.getValue());
            }
        }
    };

    public void execute() {
        setToggled();
        Animation animation = doAnimation();
        // Parametres de l'animacio
        animation.setAnimationListener(animationListener);
        animation.setDuration(getAnimationTime());
        animation.setFillAfter(true);
        fireNotification();
        what.startAnimation(animation);
    }

    /**
     * Retorna si esta o no en execucio l'animacio
     *
     * @return Retorna true si s'esta executant, no sino
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Objecte que gestionara un canvi en l'estat de l'animacio.
     * Fa crides als metodes abstractes (implementats pels fills)
     */
    private AnimationListener animationListener = new AnimationListener() {

        @Override
        public void onAnimationEnd(Animation animation) {
            running = false;
            doAnimationEnd(animation);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            doAnimationRepeat(animation);
        }

        @Override
        public void onAnimationStart(Animation animation) {
            running = true;
            doAnimationStart(animation);
        }
    };

    // Metodes abstractes implementats pels fills
    protected abstract Animation doAnimation();

    protected abstract void doAnimationEnd(Animation animation);

    protected abstract void doAnimationRepeat(Animation animation);

    protected abstract void doAnimationStart(Animation animation);
}
