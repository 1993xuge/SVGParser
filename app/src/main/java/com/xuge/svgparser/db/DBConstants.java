package com.xuge.svgparser.db;

/**
 * Created at 2019/4/24 下午7:30.
 *
 * @author yixu.wang
 */
public class DBConstants {

    public static final String TABLE_PAINT_PATH_DATA = "paint_path_data";
    public static final String COLUMN_PATH_ID = "path_id";
    public static final String COLUMN_PAINT_COLOR = "paint_color";

    public static final String STATEMENT_CREATE_TABLE_PAINT_PATH_DATA =
            "CREATE TABLE " + TABLE_PAINT_PATH_DATA + " ( "
                    + COLUMN_PATH_ID + " TEXT PRIMARY KEY, "
                    + COLUMN_PAINT_COLOR + " INTEGER DEFAULT -1);";
}
