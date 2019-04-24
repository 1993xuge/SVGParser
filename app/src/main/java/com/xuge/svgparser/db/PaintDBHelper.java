package com.xuge.svgparser.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created at 2019/4/24 下午7:27.
 *
 * @author yixu.wang
 */
public class PaintDBHelper extends SQLiteOpenHelper {
    private final static int DB_VERSION = 2;
    private static final String DB_FILE = "paint.db";

    public PaintDBHelper(Context context) {
        super(context, DB_FILE, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DBConstants.STATEMENT_CREATE_TABLE_PAINT_PATH_DATA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
