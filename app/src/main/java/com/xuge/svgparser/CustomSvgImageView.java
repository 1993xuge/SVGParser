package com.xuge.svgparser;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
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

import java.util.List;
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
    protected void doRender() {
        super.doRender();

        /*Random random = new Random();

        SparseArray<List<SVG.Path>> sparseArray = svg.getClassifiedPaths();
        currentColor = sparseArray.keyAt(random.nextInt(sparseArray.size()));
        currentPaths = sparseArray.get(currentColor);

        updateDrawable(currentPaths, currentColor, true);*/
    }

    @Override
    public void onMatrixChanged(RectF rect) {
        photoViewAttacher.getDisplayMatrix(curMatrix);
    }

    long drawTime;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw: draw time = " + (System.currentTimeMillis() - drawTime));
    }

    @Override
    public void onPhotoTap(ImageView view, float x, float y) {
        Log.d(TAG, "onPhotoTap: x = " + x + "   y = " + y);
        drawTime = System.currentTimeMillis();
        fillColor(x, y);
    }

    private List<SVG.Path> currentPaths;
    private int currentColor;

    public void selectColor(int color) {
        currentPaths = svg.getPathsByColor(color);
        if (currentPaths == null) {
            throw new RuntimeException("color error");
        }
        currentColor = color;
    }


    private void changeColor(float x, float y) {
        float[] floats = svgPoints(new float[]{x, y});

//        SVG.Path paintPath = null;
//        for (SVG.Path path : currentPaths) {
//            if (isRightPath(path, floats[0], floats[1])) {
//                paintPath = path;
//                break;
//            }
//        }
//
//        if (paintPath != null) {
//            List<SVG.Path> paths = new ArrayList<>();
//            paths.add(paintPath);
//            updateDrawable(paths, currentColor, true);
//        }


        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Picture picture = ((PictureDrawable) getDrawable()).getPicture();
        Canvas canvas = picture.beginRecording(picture.getWidth(), picture.getHeight());

        SVG.Svg rootObj = svg.getRootElement();
        SVG.Box viewBox = rootObj.getViewBox();
        SVG.Box viewPort = new SVG.Box(0, 0, pictureWidth, pictureHeight);
        PreserveAspectRatio positioning = new PreserveAspectRatio(PreserveAspectRatio.Alignment.xMidYMid, PreserveAspectRatio.Scale.meet);
        canvas.concat(SVGAndroidRenderer.calculateViewBoxTransform(viewPort, viewBox, positioning));

        Random random = new Random();
        for (SVG.Path sPath : svg.getPathList()) {
            int color = sPath.getBaseStyle().getColor();
            if (color != Color.BLACK && isRightPath(sPath, floats[0], floats[1])) {
                color = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
                sPath.getBaseStyle().setColor(color);
            }
            paint.setColor(color);
            Path path = sPath.getPath();
            if (path != null) {
                canvas.drawPath(path, paint);
            }
        }
        picture.endRecording();
        invalidate();

    }

    private void updateDrawable(List<SVG.Path> pathList, int color, boolean save) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Picture picture = ((PictureDrawable) getDrawable()).getPicture();
        Canvas canvas = picture.beginRecording(picture.getWidth(), picture.getHeight());

        SVG.Svg rootObj = svg.getRootElement();
        SVG.Box viewBox = rootObj.getViewBox();
        SVG.Box viewPort = new SVG.Box(0, 0, pictureWidth, pictureHeight);
        PreserveAspectRatio positioning = new PreserveAspectRatio(PreserveAspectRatio.Alignment.xMidYMid, PreserveAspectRatio.Scale.meet);
        canvas.concat(SVGAndroidRenderer.calculateViewBoxTransform(viewPort, viewBox, positioning));

        for (SVG.Path sPath : svg.getPathList()) {
            int drawColor = sPath.getBaseStyle().getColor();
            if (drawColor != Color.BLACK && pathList.contains(sPath)) {
                drawColor = color;
                if (save) {
                    sPath.getBaseStyle().setColor(color);
                }
            }
            paint.setColor(drawColor);
            Path path = sPath.getPath();
            if (path != null) {
                canvas.drawPath(path, paint);
            }
        }
        picture.endRecording();
        invalidate();
    }

    private boolean isRightPath(SVG.Path svgPath, float x, float y) {
        Region region = new Region();
        SVG.Box box = svgPath.getBoundingBox();
        Path path = (new SVGAndroidRenderer.PathConverter(svgPath.getD())).getPath();
        region.setPath(path, new Region((int) box.getMinX(), (int) box.getMinY(), (int) box.maxX(), (int) box.maxY()));
        if (region.contains((int) x, (int) y)) {
            return true;
        } else {
            return false;
        }
    }


    private void fillColor(float x, float y) {
        float[] floats = svgPoints(new float[]{x, y});
        SVG.Path paintPath = null;
        long time = System.currentTimeMillis();
        for (SVG.Path sPath : svg.getPathList()) {
            if (sPath.getBaseStyle().getColor() == Color.BLACK) {
                continue;
            }
            long simtime = System.currentTimeMillis();
            Region region = new Region();
            SVG.Box box = sPath.getBoundingBox();
            Path path = sPath.getPath();
            region.setPath(path, new Region((int) box.getMinX(), (int) box.getMinY(), (int) box.maxX(), (int) box.maxY()));
            if (region.contains((int) floats[0], (int) floats[1])) {
                paintPath = sPath;
                break;
            }
            Log.d(TAG, "fillColor: time = " + (System.currentTimeMillis() - simtime));
        }
        Log.d(TAG, "fillColor: find time = " + (System.currentTimeMillis() - time));

        if (paintPath != null) {
            Random random = new Random();
            paintPath.getBaseStyle().setColor(Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256)));

            time = System.currentTimeMillis();
            Picture picture = ((PictureDrawable) getDrawable()).getPicture();
            Canvas canvas = picture.beginRecording(picture.getWidth(), picture.getHeight());
            svg.renderToCanvas(canvas);
            picture.endRecording();
            invalidate();
            Log.d(TAG, "fillColor: render time = " + (System.currentTimeMillis() - time));
//            Picture picture = this.svg.renderToPicture(renderOptions);
//            setImageDrawable(new PictureDrawable(picture));
        }
    }

    private float[] svgPoints(float[] floats) {
//        Log.d(TAG, "svgPoints:1 floats x = " + floats[0] + "  y = " + floats[1]);
        Matrix matrix = new Matrix();
        Matrix invertMatrix = new Matrix();
        photoViewAttacher.getDisplayMatrix(matrix);
        matrix.invert(invertMatrix);
//        Log.d(TAG, "svgPoints: photoViewAttacher getSuppMatrix = " + matrix.toShortString());
        invertMatrix.mapPoints(floats);
//        Log.d(TAG, "svgPoints:2 floats x = " + floats[0] + "  y = " + floats[1]);

        matrix.reset();
        invertMatrix.reset();


        SVG.Svg rootObj = svg.getRootElement();
        SVG.Box viewBox = rootObj.getViewBox();
        SVG.Box viewPort = new SVG.Box(0, 0, pictureWidth, pictureHeight);
        PreserveAspectRatio positioning = new PreserveAspectRatio(PreserveAspectRatio.Alignment.xMidYMid, PreserveAspectRatio.Scale.meet);

        matrix = SVGAndroidRenderer.calculateViewBoxTransform(viewPort, viewBox, positioning);
//        Log.d(TAG, "svgPoints: photoViewAttacher getSuppMatrix = " + matrix.toShortString());
        matrix.invert(invertMatrix);
        invertMatrix.mapPoints(floats);
//        Log.d(TAG, "svgPoints:3 floats x = " + floats[0] + "  y = " + floats[1]);
        return floats;
    }
}
