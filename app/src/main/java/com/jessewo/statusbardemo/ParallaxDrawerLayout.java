package com.jessewo.statusbardemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.AbsSavedState;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by Jessewo on 2017/8/21.
 * 侧滑菜单(左右滑动时,menuView有视差效果, mainView阴影)
 */
public class ParallaxDrawerLayout extends FrameLayout {

    private static final String TAG = "ParallaxDrawerLayout";
    private static final boolean debug = false;
    private final int mScreenWidth;
    private final int mScreenHeight;

    private View mMenuView;
    private MainView mMainView;

    private ViewDragHelper mViewDragHelper;

    private static final int MENU_CLOSED = 1;
    private static final int MENU_OPENED = 2;
    private int mMenuState = MENU_CLOSED;

    private int mDragOrientation;
    private static final int LEFT_TO_RIGHT = 3;
    private static final int RIGHT_TO_LEFT = 4;

    private static final float SPRING_BACK_VELOCITY = 1500;
    private static final int SPRING_BACK_DISTANCE = 80;
    private int mSpringBackDistance;

    private static final int MENU_MARGIN_RIGHT = 64;//控制菜单宽度
    private int mMenuWidth;

    private static final int MENU_OFFSET = 128;
    private int mMenuOffset;

    private static final float TOUCH_SLOP_SENSITIVITY = 1.f;
    private static final String DEFAULT_SHADOW_OPACITY = "00";
    private String mShadowOpacity = DEFAULT_SHADOW_OPACITY;
    private boolean mDragger;
    private boolean showMainViewShadow = false;//控制mainView shadow
    private boolean showMenuShadow = true;//控制menu shadow

    public ParallaxDrawerLayout(Context context) {
        this(context, null);
    }

    public ParallaxDrawerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ParallaxDrawerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getResources().getDisplayMetrics().heightPixels;

        mSpringBackDistance = dp2px(SPRING_BACK_DISTANCE);

        mMenuOffset = dp2px(MENU_OFFSET);
        //菜单宽度
        mMenuWidth = mScreenWidth - dp2px(MENU_MARGIN_RIGHT);

        mViewDragHelper = ViewDragHelper.create(this, TOUCH_SLOP_SENSITIVITY, new CoordinatorCallback());

    }

    /**
     * dp转px
     *
     * @param dpValue dp值
     * @return px值
     */
    public int dp2px(final float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private class CoordinatorCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return mMainView == child || mMenuView == child;
        }

        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            if (capturedChild == mMenuView) {
                mViewDragHelper.captureChildView(mMainView, activePointerId);
            }
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return 1;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (left < 0) {
                left = 0;
            } else if (left > mMenuWidth) {
                left = mMenuWidth;
            }
            return left;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            LOG.e(TAG, "onViewReleased: xvel: " + xvel);
            if (mDragOrientation == LEFT_TO_RIGHT) {
                if (xvel > SPRING_BACK_VELOCITY || mMainView.getLeft() > mSpringBackDistance) {
                    openMenu();
                } else {
                    closeMenu();
                }
            } else if (mDragOrientation == RIGHT_TO_LEFT) {
                if (xvel < -SPRING_BACK_VELOCITY || mMainView.getLeft() < mMenuWidth - mSpringBackDistance) {
                    closeMenu();
                } else {
                    openMenu();
                }
            }

        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if (debug)
                LOG.d(TAG, "onViewPositionChanged: dx:" + dx);
            if (dx > 0) {
                mDragOrientation = LEFT_TO_RIGHT;
            } else if (dx < 0) {
                mDragOrientation = RIGHT_TO_LEFT;
            }
            float scale = (float) (mMenuWidth - mMenuOffset) / (float) mMenuWidth;
            int menuLeft = left - ((int) (scale * left) + mMenuOffset);
            mMenuView.layout(menuLeft, mMenuView.getTop(),
                    menuLeft + mMenuWidth, mMenuView.getBottom());
            float showing = (float) (mScreenWidth - left) / (float) mScreenWidth;
            int hex = 255 - Math.round(showing * 255);
            if (hex < 16) {
                mShadowOpacity = "0" + Integer.toHexString(hex);
            } else {
                mShadowOpacity = Integer.toHexString(hex);
            }
        }

        @Override
        public void onViewDragStateChanged(int state) {
            if (state == ViewDragHelper.STATE_IDLE) {
                //mianView 停止滑动
                int currentState = mMainView.getLeft() == 0 ? MENU_CLOSED : MENU_OPENED;
                if (mMenuState != currentState) {
                    mMenuState = currentState;
                    LOG.d(TAG, "menuOpenState: " + isOpened());
                    if (mMenuStateChangedListener != null) {
                        mMenuStateChangedListener.onMenuStateChanged(isOpened());
                    }
                }
            }
        }
    }

    //加载完布局文件后调用
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mMenuView = getChildAt(0);
        mMainView = (MainView) getChildAt(1);
        mMainView.setParent(this);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        //屏幕左侧边缘才能拉出抽屉
        int touchSlop = mViewDragHelper.getTouchSlop();
        if (!isOpened()) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    float StartX = event.getX();
                    mDragger = StartX <= touchSlop;
                    break;
            }
            if (!mDragger) {
                return false;
            }
        }
        return mViewDragHelper.shouldInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //将触摸事件传递给ViewDragHelper，此操作必不可少
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (debug)
            LOG.d(TAG, "onLayout: ");
        super.onLayout(changed, left, top, right, bottom);
        MarginLayoutParams menuParams = (MarginLayoutParams) mMenuView.getLayoutParams();
        menuParams.width = mMenuWidth;
        mMenuView.setLayoutParams(menuParams);
        if (mMenuState == MENU_OPENED) {
            mMenuView.layout(0, 0, mMenuWidth, bottom);
            mMainView.layout(mMenuWidth, 0, mMenuWidth + mScreenWidth, bottom);
            return;
        }
        mMenuView.layout(-mMenuOffset, top, mMenuWidth - mMenuOffset, bottom);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        final int restoreCount = canvas.save();//保存画布当前的剪裁信息

        final int height = getHeight();
        final int clipLeft = 0;
        int clipRight = mMainView.getLeft();
        if (child == mMenuView) {
            canvas.clipRect(clipLeft, 0, clipRight, height);//剪裁显示的区域
        }

        boolean result = super.drawChild(canvas, child, drawingTime);//绘制当前view

        //恢复画布之前保存的剪裁信息
        //以正常绘制之后的view
        canvas.restoreToCount(restoreCount);

        if (showMainViewShadow) {
            int shadowLeft = mMainView.getLeft();
            if (debug)
                LOG.d(TAG, "drawChild: shadowLeft: " + shadowLeft);
            final Paint shadowPaint = new Paint();
            if (debug)
                LOG.d(TAG, "drawChild: mShadowOpacity: " + mShadowOpacity);
            shadowPaint.setColor(Color.parseColor("#" + mShadowOpacity + "777777"));
            shadowPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(shadowLeft, 0, mScreenWidth, mScreenHeight, shadowPaint);
        }
        if (showMenuShadow) {
            final int top = mMainView.getTop();
            final int bottom = mMainView.getBottom();

            Drawable shadowDrawable = getResources().getDrawable(R.drawable.main_sliding_back_shadow);
            final int shadowWidth = shadowDrawable.getIntrinsicWidth();
            final int right = mMainView.getLeft();
            final int left = right - shadowWidth;

            shadowDrawable.setBounds(left, top, right, bottom);
            shadowDrawable.draw(canvas);
        }

        return result;
    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {//调用mScroller.computeScrollOffset()
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void openMenu() {
        mViewDragHelper.smoothSlideViewTo(mMainView, mMenuWidth, 0);//调用mScroller.startScroll()
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void closeMenu() {
        mViewDragHelper.smoothSlideViewTo(mMainView, 0, 0);
        ViewCompat.postInvalidateOnAnimation(ParallaxDrawerLayout.this);
    }

    public boolean isOpened() {
        return mMenuState == MENU_OPENED;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        final ParallaxDrawerLayout.SavedState ss = new ParallaxDrawerLayout.SavedState(superState);
        ss.menuState = mMenuState;
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof ParallaxDrawerLayout.SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        final ParallaxDrawerLayout.SavedState ss = (ParallaxDrawerLayout.SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        if (ss.menuState == MENU_OPENED) {
            openMenu();
        }
    }

    protected static class SavedState extends AbsSavedState {
        int menuState;

        SavedState(Parcel in, ClassLoader loader) {
            super(in, loader);
            menuState = in.readInt();
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(menuState);
        }

        public static final Creator<SavedState> CREATOR = ParcelableCompat.newCreator(
                new ParcelableCompatCreatorCallbacks<SavedState>() {
                    @Override
                    public ParallaxDrawerLayout.SavedState createFromParcel(Parcel in, ClassLoader loader) {
                        return new ParallaxDrawerLayout.SavedState(in, loader);
                    }

                    @Override
                    public ParallaxDrawerLayout.SavedState[] newArray(int size) {
                        return new ParallaxDrawerLayout.SavedState[size];
                    }
                });
    }

    private OnMenuStateChangedListener mMenuStateChangedListener;

    public void setOnMenuStateChangedListener(OnMenuStateChangedListener listener) {
        mMenuStateChangedListener = listener;
    }

    public interface OnMenuStateChangedListener {
        void onMenuStateChanged(boolean open);
    }
}
