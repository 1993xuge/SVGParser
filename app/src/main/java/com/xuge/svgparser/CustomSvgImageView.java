package com.xuge.svgparser;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.OnMatrixChangedListener;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.OnScaleChangedListener;
import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.xuge.libsvg.SVGImageView;

/**
 * Created at 2019/4/17 下午3:44.
 *
 * @author yixu.wang
 */
public class CustomSvgImageView extends SVGImageView implements OnPhotoTapListener, OnMatrixChangedListener {

    private static final String TAG = CustomSvgImageView.class.getSimpleName();
    private Matrix curMatrix;
    private PhotoViewAttacher photoViewAttacher;

    public CustomSvgImageView(Context context) {
        this(context, null);
    }

    public CustomSvgImageView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public CustomSvgImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setScaleType(ImageView.ScaleType.MATRIX);

        curMatrix = new Matrix();
        photoViewAttacher = new PhotoViewAttacher(this);
        photoViewAttacher.getDisplayMatrix(curMatrix);
        photoViewAttacher.setMaximumScale(25);

        photoViewAttacher.setOnPhotoTapListener(this);
        photoViewAttacher.setOnMatrixChangeListener(this);

        photoViewAttacher.setOnScaleChangeListener(new OnScaleChangedListener() {
            @Override
            public void onScaleChange(float scaleFactor, float focusX, float focusY) {
                if (photoViewAttacher.getScale() > 6) {
                    setLayerType(LAYER_TYPE_SOFTWARE, null);
                } else {
                    setLayerType(LAYER_TYPE_HARDWARE, null);
                }
            }
        });
    }


    @Override
    public void onMatrixChanged(RectF rect) {
        photoViewAttacher.getDisplayMatrix(curMatrix);
    }

    @Override
    public void onPhotoTap(ImageView view, float x, float y) {
        Log.d(TAG, "onPhotoTap: x = " + x + "   y = " + y);
    }
}
