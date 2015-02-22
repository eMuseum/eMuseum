package ub.edu.pis2014.pis12.utils;

public abstract class ScrollLoaderListener {
    abstract public boolean isEnabled();

    abstract public void onEventDown();

    abstract public void onEventMove(float alphaY, boolean carregarElements);

    abstract public void onEventUp(float alphaY, boolean carregarElements);
}
