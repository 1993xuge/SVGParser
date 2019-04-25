package com.xuge.svgparser;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.xuge.libsvg.SVG;
import com.xuge.svgparser.db.PaintDBManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "xuge";

    RecyclerView recyclerView;
    CustomAdapter adapter;
    private List<Integer> colorList;
    private CustomSvgImageView customSvgImageView;

    private int selectedColorIndex = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        immersiveStyleBar();
        setContentView(R.layout.activity_main);
        colorList = new ArrayList<>();

        customSvgImageView = findViewById(R.id.svg);
        InputStream is = null;
        try {
            is = getResources().openRawResource(R.raw.data1);
            Log.d(TAG, "onCreate: parse start");
            CustomInputStream customInputStream = new CustomInputStream(is);
            Log.d(TAG, "onCreate: customInputStream = " + customInputStream.getClass().getSimpleName());
            final SVG svg = SVG.getFromInputStream(customInputStream);
            Log.d(TAG, "onCreate: parse end");

//            final SVG svg = SVG.getFromInputStream(is);

            initSvg(svg);

            SparseArray<List<SVG.Path>> sparseArray = svg.getClassifiedPaths();
            for (int i = 0; i < sparseArray.size(); i++) {
                int key = sparseArray.keyAt(i);
                colorList.add(key);
                List<SVG.Path> paths = sparseArray.get(key);
                Log.d("wyx", "+++++++++++++++++++++  " + Integer.toHexString(key) + "  +++++++++++++++++++++");
                for (SVG.Path path : paths) {
                    Log.d("wyx", path.getId());
                }
            }


            customSvgImageView.post(new Runnable() {
                @Override
                public void run() {
                    svg.setDocumentWidth(customSvgImageView.getMeasuredWidth());
                    svg.setDocumentHeight(customSvgImageView.getMeasuredHeight());
                    customSvgImageView.setSVG(svg);
                    customSvgImageView.selectColorIndex(0);
                    Log.d(TAG, "run: getDocumentWidth = " + svg.getDocumentWidth()
                            + "   getDocumentHeight = " + svg.getDocumentHeight());
                    Log.d(TAG, "run: " + svg.getDocumentViewBox());

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "onCreate: Exception = " + e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        initRecycler();
    }

    private void initSvg(SVG svg) {
        Map<String, Integer> paintDataMap = PaintDBManager.getInstance().queryPaintPathData();
        if (paintDataMap != null && paintDataMap.size() > 0) {
            for (String id : paintDataMap.keySet()) {
                SVG.SvgElementBase element = svg.getElementById(id);
                if (element != null && (element instanceof SVG.Path)) {
                    int color = paintDataMap.get(id);

                    SVG.CustomStyle style = element.getPaintStyle();
                    if (style == null) {
                        style = new SVG.CustomStyle();
                    }
                    style.setColor(color);
                    element.setPaintStyle(style);
                }
            }
        }
    }

    private void initRecycler() {
        recyclerView = findViewById(R.id.recycler);
        adapter = new CustomAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        //添加Android自带的分割线
        recyclerView.addItemDecoration(new CustomItemDecoration());
    }

    private void immersiveStyleBar() {
        int flag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        getWindow().getDecorView().setSystemUiVisibility(flag);
    }

    private class CustomItemDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            //从第二个条目开始，距离上方Item的距离
            outRect.right = 20;
        }
    }

    private class CustomAdapter extends RecyclerView.Adapter {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_paint_color, viewGroup, false);
            return new ColorViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            ((ColorViewHolder) viewHolder).bindColor(colorList.get(i));
        }

        @Override
        public int getItemCount() {
            return colorList != null ? colorList.size() : 0;
        }
    }

    private class ColorViewHolder extends RecyclerView.ViewHolder {

        int color = Color.BLACK;

        View paintView;
        View checkView;

        public ColorViewHolder(@NonNull View itemView) {
            super(itemView);
            paintView = itemView.findViewById(R.id.view_paint_color);
            checkView = itemView.findViewById(R.id.iv_check);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectColor(color);

                }
            });
        }

        public void bindColor(int color) {
            this.color = color;
            itemView.setBackgroundColor(color);
            Log.d("wyx", "bindColor: selectedColorIndex = " + selectedColorIndex + "   this = " + colorList.indexOf(color));
            if (colorList.indexOf(color) == selectedColorIndex) {
                checkView.setVisibility(View.VISIBLE);
            } else {
                checkView.setVisibility(View.GONE);
            }
        }
    }

    private void selectColor(int color) {
        customSvgImageView.selectColor(color);
        int old = selectedColorIndex;
        selectedColorIndex = colorList.indexOf(color);
        adapter.notifyItemChanged(old);
        adapter.notifyItemChanged(selectedColorIndex);
    }
}
