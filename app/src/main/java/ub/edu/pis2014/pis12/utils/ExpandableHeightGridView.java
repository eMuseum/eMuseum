package ub.edu.pis2014.pis12.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.concurrent.Callable;

import ub.edu.pis2014.pis12.controlador.GridAdapterComentaris;

public class ExpandableHeightGridView extends LinearLayout {
    private GridAdapterComentaris adapter;
    private ScrollViewEx scroll;
    private boolean loadingAtTop = false;
    private boolean loadingDrawn = false;

    public ExpandableHeightGridView(Context context) {
        super(context);
    }

    public ExpandableHeightGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public ExpandableHeightGridView(Context context, AttributeSet attrs,
                                    int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setScroll(ScrollViewEx scroll) {
        this.scroll = scroll;
    }

    public void setAdapter(GridAdapterComentaris adapter) {
        this.adapter = adapter;
        adapter.setCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                addAll();
                return null;
            }
        });
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
    }

    private void addAll() {
        ViewGroup v = this;

        v.removeAllViews();

        if (loadingAtTop && adapter.isLoading()) {
            View c = adapter.getLoadingView(this);
            c.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            v.addView(c);
        }

        for (int i = 0; i < adapter.getCount(); i++) {
            View c = adapter.getView(i, null, this);
            c.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            v.addView(c);
        }

        if (!loadingAtTop && adapter.isLoading()) {
            View c = adapter.getLoadingView(this);
            c.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            v.addView(c);
        }

        v.requestLayout();
        scroll.requestLayout();
    }

    public void setLoading(boolean loading, boolean atTop) {
        loadingDrawn = false;
        loadingAtTop = atTop;
        adapter.setLoading(loading);
    }

    /**
     * Si acabem d'enviar algo, surt l'icono de carregar.
     * Hem d'esperar al requestLayout, pero aquest es asincronic,
     * per tant, sobrecarreguem el metode per fer l'scroll
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (adapter == null) {
            return;
        }

        if (!loadingDrawn && adapter.isLoading()) {
            if (loadingAtTop) {
                if (this.getChildCount() == 1) {
                    scroll.smoothScrollTo(0, this.getTop() + this.getChildAt(0).getHeight());
                }
            } else {
                scroll.smoothScrollTo(0, this.getTop() + this.getHeight());
            }
            loadingDrawn = true;
        }
    }
}

