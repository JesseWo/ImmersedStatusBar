package com.jessewo.statusbardemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.example.immersedstatusbar.core.StatusBarUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.title)
    TextView mTitle;
    @Bind(R.id.drawer_view)
    ParallaxDrawerLayout mDrawerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mTitle.setText("Immersed Status Bar");
        mToolbar.setNavigationIcon(R.mipmap.ic_arrow_back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerView.openMenu();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        StatusBarUtil.setLightStatusBar(this, true);
    }
}
