package ub.edu.pis2014.pis12.utils;

import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.nineoldandroids.view.ViewHelper;

/**
 * Classe filla que permet implementar una animacio per expandir
 * o contraure el view
 * 
 * @author Guillem
 *
 */
final public class PositionToggle extends AnimationToggle {

	/**
	 * Constructor de la classe
	 * 
	 * @param button Llencador de l'animacio
	 * @param what Receptor de l'animacio
	 * @param animationTime Temps de l'animacio
	 * @param hide Indica si cal amagar el view despres d'obtenir el tamany
	 */
	public PositionToggle(View button, final View what, float max, long animationTime)
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
				setMin(ViewHelper.getX(what));
				
				// Eliminem l'observer
				what.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}
		});

		setMax(max);
	}

	/**
	 * Crea l'animacio
	 */
	@Override
	protected Animation doAnimation() {
		PositionAnimation anim = null;
		
		if (toggled)
		{
			anim = new PositionAnimation(what, max - getCurrent() + min, max);
		}
		else
		{
			anim = new PositionAnimation(what, getCurrent(), min);
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

	/**
	 * Permet contraure o expandir un View modificant la seva altura
	 * Requereix saber abans l'altura maxima
	 * 
	 * @author Guillem
	 *
	 */
	private class PositionAnimation extends Animation {
		// Variables utilitzades durant el proces
		protected final int originalX;
		protected final View view;
		protected float perValue;

		/**
		 * Constructor de l'animacio
		 * 
		 * @param view View al que aplicar l'animacio
		 * @param fromHeight Tamany del que comencem
		 * @param toHeight Tamany al que volem arribar
		 */
		public PositionAnimation(View view, float fromX, float toX) {
			this.view = view;
			this.originalX = (int)fromX;
			this.perValue = (toX - fromX);
		}

		/**
		 * Aplica les tranformacions propies, es a dir en aquest cas modifica
		 * l'alcada, basant-se en l'escala de temps
		 */
		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			if (!hasEnded())
			{
				ViewHelper.setX(view, originalX + perValue * interpolatedTime);
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