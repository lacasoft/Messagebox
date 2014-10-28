package com.globant.message.box.ui.listener;


import android.graphics.PointF;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


public class SwipeListener implements RecyclerView.OnItemTouchListener {
    private static final String TAG = SwipeListener.class.getSimpleName();

    private View mItemView;
    private boolean mSwipeEvent;
    private boolean mScrollEvent;
    private PointF mStartPosition;
    private final PointF mItemPosition = new PointF();

    @Override
    public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
        final int action = motionEvent.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            setStartPointCapture(motionEvent);
            return false;
        } else if (action == MotionEvent.ACTION_MOVE) {
            mItemView = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());

            if (mItemView == null) {
                mScrollEvent = true;
                mSwipeEvent = false;
            } else {
                mItemPosition.set(mItemView.getX(), mItemView.getY());
                Log.e(TAG, "item position => " + mItemPosition.toString());

                // if item x position has changed just left swipe
                if (mItemPosition.x > 0) {
                    //try to detect left swipe
                    Log.e(TAG, "swipe left");
                } else {
                    // try to detect right swipe
                    Log.e(TAG, "swipe right");
                    mSwipeEvent = true;
                }

                return true;
            }

        }

        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {

        if (mSwipeEvent) {

        }

        Log.e(TAG, "touch event");
    }

    private void setStartPointCapture(final MotionEvent event) {
        if (event.getPointerCount() == 1) { // one single cursor
            mSwipeEvent = false;
            mStartPosition = new PointF(event.getX(), event.getY());
            Log.e(TAG, "start position => " + mStartPosition.toString());
        }
    }
}
