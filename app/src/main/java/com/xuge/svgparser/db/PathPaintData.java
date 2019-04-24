package com.xuge.svgparser.db;

/**
 * Created at 2019/4/24 下午7:38.
 *
 * @author yixu.wang
 */
public class PathPaintData {

    private String pathId;
    private int color;

    public PathPaintData(String pathId, int color) {
        this.pathId = pathId;
        this.color = color;
    }

    public String getPathId() {
        return pathId;
    }

    public void setPathId(String pathId) {
        this.pathId = pathId;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "PathPaintData{" +
                "pathId='" + pathId + '\'' +
                ", color=" + color +
                '}';
    }
}
