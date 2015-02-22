package ub.edu.pis2014.pis12.utils;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

/**
 * Classe filla que permet implementar una animacio de l'opacitat
 * 
 * @author Guillem
 *
 */
final public class AlphaToggle extends AnimationToggle {
	
	/**
	 * Constructor de la classe
	 * 
	 * @param button Llencador de l'animacio
	 * @param what Receptor de l'animacio
	 * @param animationTime Temps de l'animacio
	 */
	public AlphaToggle(View button, View what, long animationTime)
	{
		super(button, what);
		setAnimationTime(animationTime);
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
		
		if (toggled)
		{
			anim = new AlphaAnimation(max - getCurrent() + min, max);
		}
		else
		{
			anim = new AlphaAnimation(getCurrent(), min);
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
		if (!toggled)
		{
			what.setVisibility(View.GONE);
			setAllClickable(false);
		}
		else
		{
			setAllClickable(true);
		}
	}

	/**
	 * No gestionem aquest event
	 */
	@Override
	protected void doAnimationRepeat(Animation animation) {		
	}

	/**
	 * L'animacio s'inicia
	 */
	@Override
	protected void doAnimationStart(Animation animation) {
		// Si esta canviada (toggled) significa que va cap a visible
		if (toggled)
			what.setVisibility(View.VISIBLE);
	}

	/**
	 * Metode auxiliar que permet desactivar completament un
	 * view (Layout) i els seus fills
	 * 
	 * @param state
	 */
	protected void setAllClickable(boolean state)
	{
		if (!(what instanceof ViewGroup))
			return;
		
		ViewGroup v = ((ViewGroup)what);
		for (int i = 0; i < v.getChildCount(); i++)
		{
		    View child = v.getChildAt(i);
		    child.setClickable(state);
		}
		what.setClickable(state);
	}
}
