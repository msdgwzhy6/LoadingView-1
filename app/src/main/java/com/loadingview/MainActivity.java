package com.loadingview;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {
    private LinearLayout layout;

    LoadingView loadingRoundView;
    private Handler mSuccesHandler;
    private Handler mErrorHandler;
    int succes = 0;
    int error = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        layout = (LinearLayout) findViewById(R.id.layout);
        loadingRoundView = new LoadingView(this);
        loadingRoundView.addPartentViewStartLoading(layout);

        mErrorHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (error <= 50) {
                    if (error == 0) {

                        loadingRoundView.startLoading();

                    }
                    error += 1;
                    mErrorHandler.sendEmptyMessageDelayed(0, 100);
                } else {
                    loadingRoundView.setError();

                }
            }
        };
//
        mSuccesHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (succes <= 50) {
                    succes += 1;
                    mSuccesHandler.sendEmptyMessageDelayed(0, 100);
                } else {
                    loadingRoundView.setSuccess();

                    mErrorHandler.sendEmptyMessageDelayed(0, 1000);
                }
            }
        };

        mSuccesHandler.sendEmptyMessageDelayed(0, 100);

    }

}
