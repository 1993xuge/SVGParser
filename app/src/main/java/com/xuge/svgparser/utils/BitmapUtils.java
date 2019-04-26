package com.xuge.svgparser.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.xuge.svgparser.CustomApplication;
import com.xuge.svgparser.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created at 2019-04-26 11:38.
 *
 * @author yixu.wang
 */
public class BitmapUtils {

    public static Bitmap drawableToBitmap(Drawable drawable, int dstWith, int dstHeight) {
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return Bitmap.createScaledBitmap(bitmap, dstWith, dstHeight, false);
    }

    private static Bitmap drawableToBitmapWithWaterMask(Drawable drawable, int dstWith, int dstHeight) {
        if (drawable == null) {
            throw new IllegalArgumentException("drawableToBitmapWithWaterMask# drawable == null");
        }

        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();

        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);

        canvas.drawBitmap(watermark, paddingLeft, paddingTop, null);
        return Bitmap.createScaledBitmap(bitmap, dstWith, dstHeight, false);
    }

    private static Bitmap createWaterMaskBitmap(Bitmap src, Bitmap watermark,
                                                int paddingLeft, int paddingTop) {
        if (src == null) {
            return null;
        }
        int width = src.getWidth();
        int height = src.getHeight();
        //创建一个bitmap
        // 创建一个新的和SRC长度宽度一样的位图
        Bitmap newb = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        //将该图片作为画布
        Canvas canvas = new Canvas(newb);
        //在画布 0，0坐标上开始绘制原始图片
        canvas.drawBitmap(src, 0, 0, null);
        //在画布上绘制水印图片
        canvas.drawBitmap(watermark, paddingLeft, paddingTop, null);
        return newb;
    }

    private static Bitmap getWaterMaskBitmap() {
        Resources resources = CustomApplication.getContext().getResources();
        Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_launcher);
        return bitmap;
    }

    public static void saveBitmap(Bitmap bm, String fileName, String path) throws IOException {
        File foder = new File(path);
        if (!foder.exists()) {
            foder.mkdirs();
        }
        File myCaptureFile = new File(path, fileName);
        if (!myCaptureFile.exists()) {
            myCaptureFile.createNewFile();
        }
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
        bm.compress(Bitmap.CompressFormat.PNG, 80, bos);
        bos.flush();
        bos.close();
    }
}
