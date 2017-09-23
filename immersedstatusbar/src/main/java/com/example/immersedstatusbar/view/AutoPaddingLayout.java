package com.example.immersedstatusbar.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by Jessewo on 4/9/2016.
 */
public class AutoPaddingLayout extends RelativeLayout {

    public AutoPaddingLayout(Context context) {
        this(context, null);
    }

    public AutoPaddingLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoPaddingLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        AutoPadding.init(context, attrs, this);
    }
}
