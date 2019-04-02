package me.factory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.util.HashSet;
import java.util.Set;

public class ScanActivity extends AppCompatActivity {

    private DeviceManager deviceManager;

    private Set<String> epsc = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        deviceManager = new DeviceManagerImpl(this);
        deviceManager.setListener(new DeviceManager.Listener() {
            @Override
            public void onScan(String epc, Set<String> epcs) {
                ScanActivity.this.epsc.addAll(epcs);
            }
        });
        deviceManager.init();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                deviceManager.start();
            }
        }, 1000L);

        Button btnFinish = findViewById(R.id.btnFinish);
        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent result = new Intent();
                result.putExtra("result", epsc.toArray());
                setResult(Activity.RESULT_OK, result);
                ScanActivity.this.finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        deviceManager.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        deviceManager.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deviceManager.onDestory();
    }
}
