package com.xuge.svgparser;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.xuge.libsvg.SVG;
import com.xuge.libsvg.SVGImageView;
import com.xuge.libsvg.SVGParseException;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "xuge";

    RecyclerView recyclerView;
    private List<Integer> colorList;
    private CustomSvgImageView customSvgImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        immersiveStyleBar();
        setContentView(R.layout.activity_main);
        colorList = new ArrayList<>();

        final SVGImageView imageView = findViewById(R.id.svg);

        try {
            final SVG svg = SVG.getFromResource(MainActivity.this, R.raw.lion);

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


            imageView.post(new Runnable() {
                @Override
                public void run() {
                    svg.setDocumentWidth(imageView.getMeasuredWidth());
                    svg.setDocumentHeight(imageView.getMeasuredHeight());
                    imageView.setSVG(svg);
                    Log.d(TAG, "run: getDocumentWidth = " + svg.getDocumentWidth()
                            + "   getDocumentHeight = " + svg.getDocumentHeight());
                    Log.d(TAG, "run: " + svg.getDocumentViewBox());

                }
            });
        } catch (SVGParseException e) {
            e.printStackTrace();
        }

        initRecycler();
    }

    private void initRecycler() {
        recyclerView = findViewById(R.id.recycler);
        CustomAdapter adapter = new CustomAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
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

    private class CustomAdapter extends RecyclerView.Adapter {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = new View(viewGroup.getContext());
            int width = (int) getResources().getDimension(R.dimen.color_view_width);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(width, width);
            view.setLayoutParams(layoutParams);
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

        public ColorViewHolder(@NonNull View itemView) {
            super(itemView);
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
        }
    }

    private void selectColor(int color) {

    }


}
