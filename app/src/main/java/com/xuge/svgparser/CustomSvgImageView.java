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
import android.util.SparseArray;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.OnMatrixChangedListener;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.OnScaleChangedListener;
import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.xuge.libsvg.PreserveAspectRatio;
import com.xuge.libsvg.SVG;
import com.xuge.libsvg.SVGAndroidRenderer;
import com.xuge.libsvg.SVGImageView;
import com.xuge.svgparser.db.PaintDBManager;
import com.xuge.svgparser.db.PathPaintData;

import java.util.ArrayList;
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

//        updateHitColorWithIndex(0);
//        updateDrawable(currentPaths, currentColor, true);

        Log.d("currentColor", "doRender: currentColor = " + currentColor);
    }

    private void clearHit() {
        if (currentPaths == null) {
            return;
        }
        for (SVG.Path path : currentPaths) {
            SVG.CustomStyle customStyle = path.getPaintStyle();
            if (customStyle != null) {
                customStyle.setShowHitColor(false);
            }
        }
    }

    private void updateHitColorWithIndex(int index) {
        SparseArray<List<SVG.Path>> sparseArray = svg.getClassifiedPaths();
        updateHitColor(sparseArray.keyAt(index));
    }

    private void updateHitColor(int color) {
        Log.d("wyx", "updateHitColor: color = " + Integer.toHexString(color));
        clearHit();

        currentPaths = svg.getPathsByColor(color);
        if (currentPaths == null) {
            throw new RuntimeException("error color");
        }

        currentColor = color;

        for (SVG.Path path : currentPaths) {
            SVG.CustomStyle customStyle = path.getPaintStyle();
            if (customStyle != null) {
                customStyle.setShowHitColor(true);
            } else {
                customStyle = new SVG.CustomStyle(true);
                path.setPaintStyle(customStyle);
            }
        }

        Picture picture = ((PictureDrawable) getDrawable()).getPicture();
        Canvas canvas = picture.beginRecording(picture.getWidth(), picture.getHeight());
        svg.renderToCanvas(canvas);
        picture.endRecording();
        invalidate();
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
        fillColor2(x, y);
    }

    private List<SVG.Path> currentPaths;
    private int currentColor;

    public void selectColor(int color) {
        updateHitColor(color);
    }

    public void selectColorIndex(int index) {
        updateHitColorWithIndex(index);
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
        for (SVG.Path sPath : currentPaths) {
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
        Region pathRegion = new Region();
        Region clipRegion = new Region();

        for (SVG.Path sPath : svg.getPathList()) {
            if (sPath.getBaseStyle().getColor() == Color.BLACK) {
                continue;
            }
            long simtime = System.currentTimeMillis();
            SVG.Box box = sPath.getBoundingBox();
            Path path = sPath.getPath();
            clipRegion.set((int) box.getMinX(), (int) box.getMinY(), (int) box.maxX(), (int) box.maxY());
            pathRegion.setPath(path, clipRegion);

            if (pathRegion.contains((int) floats[0], (int) floats[1])) {
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

    private void fillColor2(float x, float y) {
        long time = System.currentTimeMillis();
        SVG.Path paintPath = findPath(x, y);
        Log.d("xuge123", "fillColor2: findPath time = " + (System.currentTimeMillis() - time) + "   path = " + paintPath);
        if (paintPath != null) {
//            Random random = new Random();
//            paintPath.getBaseStyle().setColor(Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256)));

            paintPath.getPaintStyle().setColor(currentColor);
            PaintDBManager.getInstance().replacePathPaintData(new PathPaintData(paintPath.getId(), currentColor));

            Picture picture = ((PictureDrawable) getDrawable()).getPicture();
            Canvas canvas = picture.beginRecording(picture.getWidth(), picture.getHeight());
            svg.renderToCanvas(canvas);
            picture.endRecording();
            invalidate();
//            Picture picture = this.svg.renderToPicture(renderOptions);
//            setImageDrawable(new PictureDrawable(picture));
        }
    }

    int max = 20;

    private SVG.Path findPath(float x, float y) {
        max = getMax();
        Log.d("xuge123", "findPath: x = " + x + "  y = " + y);
        float[] floats = svgPoints(new float[]{x, y});
        x = floats[0];
        y = floats[1];
        Log.d("xuge123", "findPath: x = " + floats[0] + "  y = " + floats[1]);

        Region clickRegion = new Region();
        Path path = new Path();
        path.addCircle(x, y, max, Path.Direction.CW);
        clickRegion.setPath(path, new Region((int) x - max, (int) y - max, (int) x + max, (int) y + max));

        long time = System.currentTimeMillis();
        List<SVG.Path> availablePaths = findAvailablePaths(x, y, clickRegion);
        Log.d("xuge123", "findPath: findAvailablePaths time = " + (System.currentTimeMillis() - time) + "   availablePaths = " + availablePaths);
        if (availablePaths.size() == 0) {
            return null;
        }

        if (availablePaths.size() == 1) {
            return availablePaths.get(0);
        }

        time = System.currentTimeMillis();
        SVG.Path paintPath = findClickPath((int) x, (int) y, availablePaths);
        Log.d("xuge123", "findPath: findClickPath time = " + (System.currentTimeMillis() - time) + "   paintPath = " + paintPath);

        return paintPath;
    }

    private List<SVG.Path> findAvailablePaths(float x, float y, Region clickRegion) {
        List<SVG.Path> availablePaths = new ArrayList<>();
        Log.d("xuge123", "findAvailablePaths: currentPaths size = " + currentPaths.size() + "   clickRegion = " + clickRegion.getBounds());
        Region temp = new Region();
        for (SVG.Path sPath : currentPaths) {
            Region region = new Region();
            SVG.Box box = sPath.getBoundingBox();
            Path path = sPath.getPath();
            region.setPath(path, new Region((int) box.getMinX(), (int) box.getMinY(), (int) box.maxX(), (int) box.maxY()));

            if (region.contains((int) x, (int) y)) {
                Log.d("xuge123", "findAvailablePaths: contains break ");
                // 在Path内部，退出循环
                availablePaths.clear();
                availablePaths.add(sPath);
                break;
            }

            // TODO: 2019/4/22  findAvailablePaths 判断是否涂过色
            if (false) {
                continue;
            }

            boolean isIntersect = temp.op(region, clickRegion, Region.Op.INTERSECT);
            Log.d("xuge123", "findAvailablePaths: isIntersect = " + isIntersect);
            if (isIntersect) {
                availablePaths.add(sPath);
            }
        }
        return availablePaths;
    }

    private SVG.Path findClickPath(int x, int y, List<SVG.Path> availablePaths) {
        Region temp = new Region();
        Region clickRegion = new Region();

        Region pathRegion = new Region();
        Region clipRegion = new Region();
        SVG.Path clickPath = null;

        int regionRadius = max;

        List<SVG.Path> tempList = new ArrayList<>();

        int left = 0;
        int right = max;
        while ((right - left) > 1 && availablePaths.size() > 1) {
            regionRadius = left + (right - left) / 2;
            Log.d("xuge123", "findClickPath: left = " + left + "   right = " + right + "   availablePaths = " + availablePaths.size());

            Region r = new Region(x - regionRadius, y - regionRadius, x + regionRadius, y + regionRadius);
            Path tempPath = new Path();
            tempPath.addCircle(x, y, regionRadius, Path.Direction.CW);
            clickRegion.set(x - regionRadius, y - regionRadius, x + regionRadius, y + regionRadius);
            clickRegion.setPath(tempPath, r);
            for (SVG.Path path : availablePaths) {

                SVG.Box box = path.getBoundingBox();
                Log.d("xuge123", "findClickPath: box = " + box);
                clipRegion.set((int) box.getMinX(), (int) box.getMinY(), (int) box.maxX(), (int) box.maxY());
                pathRegion.setPath(path.getPath(), clipRegion);

                boolean result = temp.op(clickRegion, pathRegion, Region.Op.INTERSECT);
                if (result) {
                    tempList.add(path);
                }
            }

            Log.d("xuge123", "findClickPath: tempList size = " + tempList.size());
            //
            if (tempList.isEmpty()) {
                left = regionRadius + 1;
            } else {
                right = regionRadius - 1;
                availablePaths.clear();
                availablePaths.addAll(tempList);
            }
            tempList.clear();
        }

        /*here:
        for (int i = 1; i <= max; i++) {
            Log.d("xuge123", "findClickPath: i = " + i);
            clickRegion.set(x - i, y - i, x + i, y + i);
            for (SVG.Path path : availablePaths) {

                SVG.Box box = path.getBoundingBox();
                clipRegion.set((int) box.getMinX(), (int) box.getMinY(), (int) box.maxX(), (int) box.maxY());
                pathRegion.setPath(path.getPath(), clipRegion);

                boolean result = temp.op(clickRegion, pathRegion, Region.Op.INTERSECT);
                if (result) {
                    clickPath = path;
                    break here;
                }
            }
        }*/

        Log.d("xuge123", "findClickPath: find   " + availablePaths.size());
        if (availablePaths.size() > 0) {
            clickPath = availablePaths.get(0);
        }
        Log.d("xuge123", "findClickPath: clickPath = " + clickPath);
        return clickPath;
    }

    private int getMax() {
        SVG.Svg rootObj = svg.getRootElement();
        SVG.Box viewBox = rootObj.getViewBox();
        SVG.Box viewPort = new SVG.Box(0, 0, pictureWidth, pictureHeight);
        PreserveAspectRatio positioning = new PreserveAspectRatio(PreserveAspectRatio.Alignment.xMidYMid, PreserveAspectRatio.Scale.meet);

        Matrix matrix = SVGAndroidRenderer.calculateViewBoxTransform(viewPort, viewBox, positioning);
        max = (int) matrix.mapRadius(40);
        Log.d("xuge123", "getMax: after max = " + max);
        return max;
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
