package com.loadingview;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private LoadingView loadingview;
    private Handler mSuccesHandler;
    private Handler mErrorHandler;
    int succes = 0;
    int error = 0;
//    private Button btn1;
//    private Button btn2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        loadingview = new LoadingView(this);

        mErrorHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (error <= 50) {
                    if (error == 0) {

                        loadingview.addViewStartLoading(loadingview, MainActivity.this);

                    }
                    error += 1;
                    mErrorHandler.sendEmptyMessageDelayed(0, 100);
                } else {
                    loadingview.setError();
                }
            }
        };

        mSuccesHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (succes <= 50) {
                    succes += 1;
                    mSuccesHandler.sendEmptyMessageDelayed(0, 100);
                } else {
                    loadingview.setSuccess();

                    mErrorHandler.sendEmptyMessageDelayed(0, 1000);
                }
            }
        };

        loadingview.addViewStartLoading(loadingview, this);
        mSuccesHandler.sendEmptyMessageDelayed(0, 100);

//        btn1 = (Button) findViewById(R.id.btn1);
//        btn1.setOnClickListener(this);
//        btn2 = (Button) findViewById(R.id.btn2);
//        btn2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.btn1:
//                Toast.makeText(this, "操作1", Toast.LENGTH_SHORT).show();
//                break;
//            case R.id.btn2:
//                Toast.makeText(this, "操作2", Toast.LENGTH_SHORT).show();
//                break;
        }
    }
}
