package com.xuge.svgparser;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.PictureDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.OnMatrixChangedListener;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.OnScaleChangedListener;
import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.xuge.libsvg.PreserveAspectRatio;
import com.xuge.libsvg.SVG;
import com.xuge.libsvg.SVGAndroidRenderer;
import com.xuge.libsvg.SVGImageView;

import java.util.Random;

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

        int lX = Math.round(x * pictureWidth);
        int lY = Math.round(y * pictureHeight);
        fillColor(lX, lY);
    }

    private SVG.Path svgPath;

    private void fillColor(float x, float y) {
        float[] floats = svgPoints(new float[]{x, y});
        SVG.Path paintPath = null;
        for (SVG.Path sPath : svg.getPathList()) {
            if (sPath.getBaseStyle().getColor() == Color.BLACK) {
                continue;
            }
            Region region = new Region();
            SVG.Box box = sPath.getBoundingBox();
            Path path = (new SVGAndroidRenderer.PathConverter(sPath.getD())).getPath();
            region.setPath(path, new Region((int) box.getMinX(), (int) box.getMinY(), (int) box.maxX(), (int) box.maxY()));
            if (region.contains((int) floats[0], (int) floats[1])) {
                paintPath = sPath;
            }
        }

        if (paintPath != null) {
            svgPath = paintPath;
            Random random = new Random();
            svgPath.getBaseStyle().setColor(Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
            Picture picture = this.svg.renderToPicture(renderOptions);
            setImageDrawable(new PictureDrawable(picture));
            invalidate();
        }
    }

    private float[] svgPoints(float[] floats) {
        Log.d(TAG, "svgPoints:1 floats x = " + floats[0] + "  y = " + floats[1]);
        Matrix matrix = new Matrix();
        Matrix invertMatrix = new Matrix();
        photoViewAttacher.getDisplayMatrix(matrix);
        matrix.invert(invertMatrix);
        Log.d(TAG, "svgPoints: photoViewAttacher getSuppMatrix = " + matrix.toShortString());
        invertMatrix.mapPoints(floats);
        Log.d(TAG, "svgPoints:2 floats x = " + floats[0] + "  y = " + floats[1]);

        matrix.reset();
        invertMatrix.reset();


        SVG.Svg rootObj = svg.getRootElement();
        SVG.Box viewBox = rootObj.getViewBox();
        SVG.Box viewPort = new SVG.Box(0, 0, pictureWidth, pictureHeight);
        PreserveAspectRatio positioning = new PreserveAspectRatio(PreserveAspectRatio.Alignment.xMidYMid, PreserveAspectRatio.Scale.meet);

        matrix = SVGAndroidRenderer.calculateViewBoxTransform(viewPort, viewBox, positioning);
        Log.d(TAG, "svgPoints: photoViewAttacher getSuppMatrix = " + matrix.toShortString());
        matrix.invert(invertMatrix);
        invertMatrix.mapPoints(floats);
        Log.d(TAG, "svgPoints:3 floats x = " + floats[0] + "  y = " + floats[1]);
        return floats;
    }
}
