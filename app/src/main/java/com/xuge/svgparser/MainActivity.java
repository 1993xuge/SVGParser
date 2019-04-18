package com.xuge.svgparser;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.xuge.libsvg.SVG;
import com.xuge.libsvg.SVGImageView;
import com.xuge.libsvg.SVGParseException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "xuge";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        immersiveStyleBar();
        setContentView(R.layout.activity_main);

        final SVGImageView imageView = findViewById(R.id.svg);

        try {
            final SVG svg = SVG.getFromResource(MainActivity.this, R.raw.lion);

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
}
