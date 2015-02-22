package ub.edu.pis2014.pis12;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Classe molt simple en la que unicament extenem un TextView
 * En aquest seria obviable pero la mantenim per referencia i per si
 * cal fer futures modificacions al GridView
 * 
 * @author Guillem
 *
 */
public class MainGridItem extends TextView
{
    public MainGridItem(Context context)
    {
        super(context);
    }

    public MainGridItem(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public MainGridItem(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
}
