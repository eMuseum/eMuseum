package ub.edu.pis2014.pis12.utils;

/**
 * Classe molt simple encarregada de notificar quan una animacio
 * s'executa
 */
public abstract class AnimationNotifier {
    /**
     * Metode abstracte utilitzat durant la crida
     *
     * @param animation Objecte animador
     * @param toggled   Estat de l'animacio
     */
    public abstract void onToggled(AnimationToggle animation, boolean toggled);
}
