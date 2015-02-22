package ub.edu.pis2014.pis12.utils;

import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

/**
 * Classe filla que permet implementar una animacio per expandir
 * o contraure el view
 * 
 * @author Guillem
 *
 */
final public class HeightToggle extends AnimationToggle {

	/**
	 * Constructor de la classe
	 * 
	 * @param button Llencador de l'animacio
	 * @param what Receptor de l'animacio
	 * @param animationTime Temps de l'animacio
	 * @param hide Indica si cal amagar el view despres d'obtenir el tamany
	 */
	public HeightToggle(View button, final View what, long animationTime, final boolean hide)
	{
		super(button, what);
		setAnimationTime(animationTime);

		final ViewTreeObserver observer = what.getViewTreeObserver();
		observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			/*
			 * La crida a removeGlobalOnLayoutListener esta obsoleta des de l'api 16,
			 * pero degut a que la minima es 8, ho mantenim
			 */
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				setMax(what.getHeight());
				if (hide)
					what.setVisibility(View.GONE);
				
				// Eliminem l'observer
				what.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}
		});
		
		setMin(0.0F);
	}

	/**
	 * Crea l'animacio
	 */
	@Override
	protected Animation doAnimation() {
		HeightAnimation anim = null;

		if (toggled)
		{
			anim = new HeightAnimation(what, max - getCurrent() + min, max);
		}
		else
		{
			anim = new HeightAnimation(what, getCurrent(), min);
		}

		return anim;
	}

	// No gestionat
	@Override
	protected void doAnimationEnd(Animation animation) {
		what.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		what.requestLayout();
	}

	// No gestionat
	@Override
	protected void doAnimationRepeat(Animation animation) {		
	}

	// No gestionat
	@Override
	protected void doAnimationStart(Animation animation) {
	}

	/**
	 * Permet contraure o expandir un View modificant la seva altura
	 * Requereix saber abans l'altura maxima
	 * 
	 * @author Guillem
	 *
	 */
	private class HeightAnimation extends Animation {
		// Variables utilitzades durant el proces
		protected final int originalHeight;
		protected final View view;
		protected float perValue;

		/**
		 * Constructor de l'animacio
		 * 
		 * @param view View al que aplicar l'animacio
		 * @param fromHeight Tamany del que comencem
		 * @param toHeight Tamany al que volem arribar
		 */
		public HeightAnimation(View view, float fromHeight, float toHeight) {
			this.view = view;
			this.originalHeight = (int)fromHeight;
			this.perValue = (toHeight - fromHeight);
		}

		/**
		 * Aplica les tranformacions propies, es a dir en aquest cas modifica
		 * l'alcada, basant-se en l'escala de temps
		 */
		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			if (!hasEnded())
			{
				view.getLayoutParams().height = (int) (originalHeight + perValue * interpolatedTime);
				view.requestLayout();
			}
		}

		/**
		 * Indica que modifica el tamany del View
		 */
		@Override
		public boolean willChangeBounds() {
			return true;
		}
	}
}