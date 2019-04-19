/*
    Copyright 2011, 2015 Pixplicity, Larva Labs LLC and Google, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

    Sharp is heavily based on prior work. It was originally forked from
        https://github.com/pents90/svg-android
    And changes from other forks have been consolidated:
        https://github.com/b2renger/svg-android
        https://github.com/mindon/svg-android
        https://github.com/josefpavlik/svg-android
 */

package com.xuge.libsvg;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.drawable.PictureDrawable;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

public class SharpDrawable extends PictureDrawable {

    private static final String TAG = SharpDrawable.class.getSimpleName();

    private static final Bitmap.Config CACHE_CONFIG = Bitmap.Config.ARGB_8888;

    private float mScaleX = 1f;
    private float mScaleY = 1f;

    private boolean mCaching = false;
    private Rect mCacheBounds;
    private Bitmap mCacheBitmap;
    private float mCacheScale = 1f;

    /**
     * Construct a new drawable referencing the specified picture. The picture
     * may be null.
     *
     * @param picture The picture to associate with the drawable. May be null.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SharpDrawable(@Nullable Picture picture) {
        super(picture);
    }

    /**
     * Construct a new drawable referencing the specified picture. The picture
     * may be null. A view may be provided so that its LayerType is set to
     * LAYER_TYPE_SOFTWARE.
     *
     * @param view    {@link View} that will hold this drawable
     * @param picture The picture to associate with the drawable. May be null.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SharpDrawable(@Nullable View view, @Nullable Picture picture) {
        super(picture);
//        prepareView(view);
    }

    /**
     * Prepare a view for rendering this SharpDrawable by setting its LayerType to
     * LAYER_TYPE_SOFTWARE.
     *
     * @param view
     */
    public static void prepareView(@Nullable final View view) {
        if (view != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            } else {
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                    }
                });
            }
        }
    }

    @Override
    public void draw(Canvas parentCanvas) {
        long start = System.currentTimeMillis();
        super.draw(parentCanvas);
//        Log.d(TAG, "draw: ");
//        Picture picture = getPicture();
//        if (picture != null) {
//            Rect bounds = getBounds();
//            Canvas canvas = null;
//            Log.d(TAG, "draw: mCaching = " + mCaching);
//            if (mCaching) {
//                Log.d(TAG, "draw: mCacheBitmap = " + mCacheBitmap + "   mCacheBounds = " + mCacheBounds
//                        + "   !mCacheBounds.equals(bounds) = " + (!mCacheBounds.equals(bounds)));
//                if (mCacheBitmap == null || mCacheBounds == null || !mCacheBounds.equals(bounds)) {
//                    // Redraw needed
//                    resetCache();
//                    // creates a new mutable bitmap
//                    int w = (int) (bounds.width() * mCacheScale);
//                    int h = (int) (bounds.height() * mCacheScale);
//                    Log.v(TAG, "cache bitmap " + w + "x" + h);
//                    mCacheBitmap = Bitmap.createBitmap(w, h, CACHE_CONFIG);
//                    if (mCacheBounds == null) {
//                        mCacheBounds = new Rect(bounds);
//                    } else {
//                        mCacheBounds.set(bounds);
//                    }
//                    // start drawing onto this bitmap
//                    canvas = new Canvas(mCacheBitmap);
//                    canvas.save();
//                    canvas.scale(mCacheScale, mCacheScale);
//                }
//            } else {
//                canvas = parentCanvas;
//            }
//            if (canvas != null) {
//                canvas.save();
//                canvas.clipRect(bounds);
//                Log.v(TAG, "canvas " + canvas.getWidth() + "x" + canvas.getHeight());
//                Log.v(TAG, "bounds mScaleX = " + mScaleX + "   mScaleY = " + mScaleY);
//                canvas.translate(bounds.left, bounds.top);
//                canvas.scale(mScaleX, mScaleY, 0, 0);
//                canvas.drawPicture(picture);
//                canvas.restore();
//            }
//            Log.d(TAG, "draw: mCacheBitmap = " + mCacheBitmap);
//            if (mCacheBitmap != null) {
//                if (canvas != null) {
//                    canvas.restore();
//                }
//                parentCanvas.save();
//                parentCanvas.scale(1f / mCacheScale, 1f / mCacheScale, 0, 0);
//                parentCanvas.drawBitmap(mCacheBitmap, 0, 0, null);
//                parentCanvas.restore();
//            }
//        }
        Log.v(TAG, "Drawing " + hashCode() + " complete in " + (System.currentTimeMillis() - start) + " ms.");
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        Picture picture = getPicture();
        int width = right - left;
        int height = bottom - top;
        mScaleX = (float) width / (float) picture.getWidth();
        mScaleY = (float) height / (float) picture.getHeight();
        super.setBounds(left, top, right, bottom);
    }

    /**
     * Experimental feature to cache the drawable into a bitmap. The cache is not always
     * automatically redrawn, as the way in which the SharpDrawable is drawn may change without it
     * being informed. To manually redraw the cache, invoke {@link #resetCache()}.
     *
     * @param caching boolean
     */
    @SuppressWarnings("unused")
    public void setCaching(boolean caching) {
        mCaching = caching;
    }

    @SuppressWarnings("unused")
    public void setCacheScale(float scale) {
        mCacheScale = scale;
    }

    @SuppressWarnings("unused")
    public void resetCache() {
        if (!mCaching) {
            throw new IllegalStateException("Cache is not enabled");
        }
        if (mCacheBitmap != null) {
            // recycle the old bitmap
            mCacheBitmap.recycle();
            mCacheBitmap = null;
        }
    }

}
