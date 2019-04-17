/*
 Copyright 2011, 2012 Chris Banes.
 <p/>
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 <p/>
 http://www.apache.org/licenses/LICENSE-2.0
 <p/>
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.github.chrisbanes.photoview;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

/**
 * Does a whole lot of gesture detecting.
 */
class CustomGestureDetector {

    private static final int INVALID_POINTER_ID = -1;

    private int mActivePointerId = INVALID_POINTER_ID;
    private int mActivePointerIndex = 0;
    private final ScaleGestureDetector mDetector;

    // VelocityTracker 是一个跟踪触摸事件滑动速度的帮助类，用于实现flinging 及其他类似的手势
    private VelocityTracker mVelocityTracker;
    private boolean mIsDragging;
    private float mLastTouchX;
    private float mLastTouchY;
    private final float mTouchSlop;
    private final float mMinimumVelocity;
    private OnGestureListener mListener;

    CustomGestureDetector(Context context, OnGestureListener listener) {
        final ViewConfiguration configuration = ViewConfiguration
                .get(context);
        // 获得一个fling手势动作的最小速度值。
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();

        // 返回一个int类型的值，表示被系统认为的滑动的最小距离，小于这个值系统不认为是一次滑动 。
        mTouchSlop = configuration.getScaledTouchSlop();

        mListener = listener;
        ScaleGestureDetector.OnScaleGestureListener mScaleListener = new ScaleGestureDetector.OnScaleGestureListener() {

            // 缩放操作进行中
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                // 当一个缩放手势正在进行中时持续触发该事件，即双指正在屏幕上滑动。
                // 该方法的返回值为boolean，主要用来判断是否结束当前缩放手势事件，即本次缩放事件是否已被处理。
                // 如果已被处理，那么detector就会重置缩放事件；
                // 如果未被处理，detector会继续进行计算，修改getScaleFactor()的返回值，直到被处理为止。

                // 当返回true时，则结束当前缩放手势的一系列事件，缩放因子将会初始化为1.0
                // 当返回false时，当前缩放手势的一系列事件继续进行，缩放因子不会被初始化，而是根据手势不断的持续变化。

                // 返回值可以用来限制缩放值的最大比例上限和最小比例下限。


                // getScaleFactor() : 返回从之前的缩放手势和当前的缩放手势两者的比例因子，即缩放值，默认1.0。
                // 当手指做缩小操作时，该值小于1.0，当手指做放大操作时，该值大于1.0。
                float scaleFactor = detector.getScaleFactor();

                if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor)) {
                    // isNaN : 用于描述非法的float
                    // isInfinite : 如果指定的整数是无限大方法返回true，否则返回false。
                    return false;
                }

                // detector.getFocusX() : 返回当前缩放手势的焦点X坐标，焦点即两手指的中心点。
                // detector.getFocusY() : 返回当前缩放手势的焦点Y坐标，即两手指的中心点。
                if (scaleFactor >= 0) {
                    mListener.onScale(scaleFactor,
                            detector.getFocusX(), detector.getFocusY());
                }
                return true;
            }

            // 缩放操作开始前
            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                // 当一个缩放手势开始时触发该事件，即双指按下屏幕时。
                // 该方法的返回值为boolean，主要用来判断探测器是否应该继续识别处理这个手势。
                return true;
            }

            // 缩放操作结束后
            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                // 当一个缩放手势结束后触发该事件，即双指抬起离开屏幕时。
            }
        };
        mDetector = new ScaleGestureDetector(context, mScaleListener);
    }

    private float getActiveX(MotionEvent ev) {
        try {
            return ev.getX(mActivePointerIndex);
        } catch (Exception e) {
            return ev.getX();
        }
    }

    private float getActiveY(MotionEvent ev) {
        try {
            return ev.getY(mActivePointerIndex);
        } catch (Exception e) {
            return ev.getY();
        }
    }

    public boolean isScaling() {
        return mDetector.isInProgress();
    }

    public boolean isDragging() {
        return mIsDragging;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        try {
            mDetector.onTouchEvent(ev);
            return processTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            // Fix for support lib bug, happening when onDestroy is called
            return true;
        }
    }

    private boolean processTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            // down事件
            case MotionEvent.ACTION_DOWN:
                // 获取第一个手指的id
                mActivePointerId = ev.getPointerId(0);

                mVelocityTracker = VelocityTracker.obtain();
                if (null != mVelocityTracker) {
                    // 将用户的滑动事件传给velocityTracker
                    mVelocityTracker.addMovement(ev);
                }

                mLastTouchX = getActiveX(ev);
                mLastTouchY = getActiveY(ev);
                mIsDragging = false;
                break;
            case MotionEvent.ACTION_MOVE:
                final float x = getActiveX(ev);
                final float y = getActiveY(ev);
                final float dx = x - mLastTouchX, dy = y - mLastTouchY;

                if (!mIsDragging) {
                    // Use Pythagoras to see if drag length is larger than
                    // touch slop
                    mIsDragging = Math.sqrt((dx * dx) + (dy * dy)) >= mTouchSlop;
                }

                if (mIsDragging) {
                    mListener.onDrag(dx, dy);
                    mLastTouchX = x;
                    mLastTouchY = y;

                    if (null != mVelocityTracker) {
                        mVelocityTracker.addMovement(ev);
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                mActivePointerId = INVALID_POINTER_ID;
                // Recycle Velocity Tracker
                if (null != mVelocityTracker) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            case MotionEvent.ACTION_UP:
                // 所有手指都up时，回调该事件
                mActivePointerId = INVALID_POINTER_ID;
                if (mIsDragging) {
                    if (null != mVelocityTracker) {
                        mLastTouchX = getActiveX(ev);
                        mLastTouchY = getActiveY(ev);

                        // Compute velocity within the last 1000ms
                        mVelocityTracker.addMovement(ev);
                        mVelocityTracker.computeCurrentVelocity(1000);

                        final float vX = mVelocityTracker.getXVelocity(), vY = mVelocityTracker
                                .getYVelocity();

                        // If the velocity is greater than minVelocity, call
                        // listener
                        if (Math.max(Math.abs(vX), Math.abs(vY)) >= mMinimumVelocity) {
                            // up事件中，如果x方向或y方向的速度最大值 大于 Fling的最小值，则回调onFling
                            mListener.onFling(mLastTouchX, mLastTouchY, -vX,
                                    -vY);
                        }
                    }
                }

                // Recycle Velocity Tracker
                if (null != mVelocityTracker) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                // 每当一个手指 up时，都会回调该事件
                final int pointerIndex = Util.getPointerIndex(ev.getAction());
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // 当前正在处理的手指 up了。重新选择手指
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                    mLastTouchX = ev.getX(newPointerIndex);
                    mLastTouchY = ev.getY(newPointerIndex);
                }
                break;
        }

        // 更新手指的index
        mActivePointerIndex = ev
                .findPointerIndex(mActivePointerId != INVALID_POINTER_ID ? mActivePointerId
                        : 0);
        return true;
    }
}
