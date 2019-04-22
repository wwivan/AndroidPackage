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
        int scanFlag = getIntent().getIntExtra("scanFlag", 0); //默认单一扫描
        deviceManager = new DeviceManagerImpl(this,scanFlag);
        deviceManager.setListener(new DeviceManager.Listener() {
            @Override
            public void onScan(String epc, Set<String> epcs) {
                //ScanActivity.this.epsc.addAll(epcs);
                Intent result = new Intent();
                result.putExtra("result", epcs.toArray());
                setResult(Activity.RESULT_OK, result);
                ScanActivity.this.finish();
            }

            @Override
            public void onWrite(String epc) {
                //rfidcode=epc;
            }
        });
        deviceManager.init();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //rfid工具类初始化后，进行设置rfid功率，此功率参数需要在vue页面做持久化保存，并在每次打开wms时，传递到以下方法做设置
                // ，应为程序关闭后，功率设置就会无效，所以需要每次打开程序都传递下功率参数，以调节rfid距离
                //启动和设置rfid, 需要有个1秒的时间间隔，否则会失败
                int range = getIntent().getIntExtra("rfidrange", 30);//取不到值情况下，默认30
                deviceManager.rfidRangeSet(range);
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
