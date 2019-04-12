package com.xuge.svgparser;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.xuge.libsvg.SVGImageView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SVGImageView imageView = findViewById(R.id.svg);
        imageView.setImageResource(R.raw.tarot_13);
    }
}
