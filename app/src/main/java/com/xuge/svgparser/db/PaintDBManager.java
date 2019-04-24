package com.xuge.svgparser.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.xuge.svgparser.CustomApplication;

import java.util.HashMap;
import java.util.Map;

/**
 * Created at 2019/4/24 下午7:32.
 *
 * @author yixu.wang
 */
public class PaintDBManager {
    private static final String TAG = PaintDBManager.class.getSimpleName();

    private static class Holder {
        private static PaintDBManager sInstance = new PaintDBManager();
    }

    public static PaintDBManager getInstance() {
        return Holder.sInstance;
    }

    private PaintDBHelper dbHelper;
    private SQLiteDatabase db;

    public PaintDBManager() {
        dbHelper = new PaintDBHelper(CustomApplication.getContext());
        db = dbHelper.getWritableDatabase();
    }

    public void replacePathPaintData(PathPaintData pathPaintData) {
        db.beginTransaction();
        try {
            if (pathPaintData != null) {
                ContentValues contentValues = createContentValues(pathPaintData);
                if (contentValues != null) {
                    db.replaceOrThrow(DBConstants.TABLE_PAINT_PATH_DATA, null, contentValues);
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, this.hashCode() + "   replacePathPaintData : Exception = " + e.toString());
        } finally {
            db.endTransaction();
        }
    }

    public Map<String, Integer> queryPaintPathData() {
        Cursor cursor = null;
        Map<String, Integer> paintPathDataMap = new HashMap<>();
        try {
            cursor = db.query(true, DBConstants.TABLE_PAINT_PATH_DATA, null, null, null, null, null, null, null);
            while (cursor != null && cursor.moveToNext()) {
                PathPaintData pathPaintData = createPathPaintData(cursor);
                paintPathDataMap.put(pathPaintData.getPathId(), pathPaintData.getColor());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, this.hashCode() + "   queryPaintPathData : Exception = " + e.toString());
        } finally {
            closeCursor(cursor);
        }
        return paintPathDataMap;
    }

    private ContentValues createContentValues(PathPaintData data) {
        ContentValues cv = new ContentValues();
        cv.put(DBConstants.COLUMN_PATH_ID, data.getPathId());
        cv.put(DBConstants.COLUMN_PAINT_COLOR, data.getColor());
        return cv;
    }

    private PathPaintData createPathPaintData(Cursor cursor) {
        String pathId = cursor.getString(cursor.getColumnIndex(DBConstants.COLUMN_PATH_ID));
        int color = cursor.getInt(cursor.getColumnIndex(DBConstants.COLUMN_PAINT_COLOR));
        return new PathPaintData(pathId, color);
    }

    public void closeCursor(Cursor cursor) {
        if (cursor != null) {
            try {
                cursor.close();
            } catch (Throwable e) {
            }
        }
    }
}
