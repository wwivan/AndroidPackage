package me.factory;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;

public class ScanActivity extends AppCompatActivity implements OnClickListener,MyReceiver.Message {

    private DeviceManager deviceManager;
    private Handler handler;
    private Set<String> epsc = new HashSet<>();
    private String RECE_DATA_ACTION = "com.se4500.onDecodeComplete";
    private String START_SCAN_ACTION = "com.geomobile.se4500barcode";
    private String STOP_SCAN = "com.geomobile.se4500barcode.poweroff";
    private MyReceiver receiver;
    private TextView tv_scan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        IntentFilter filter = new IntentFilter();
        filter.addAction(RECE_DATA_ACTION);
        filter.addAction(START_SCAN_ACTION);
        filter.addAction(STOP_SCAN);
        tv_scan = (TextView) findViewById(R.id.tv_scan);
        receiver = new MyReceiver();
        registerReceiver(receiver, filter);
        receiver.setMessage(this);
        deviceManager = new DeviceManagerImpl(this,0);
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
        //Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                //rfid工具类初始化后，进行设置rfid功率，此功率参数需要在vue页面做持久化保存，并在每次打开wms时，传递到以下方法做设置
//                // ，应为程序关闭后，功率设置就会无效，所以需要每次打开程序都传递下功率参数，以调节rfid距离
//                //启动和设置rfid, 需要有个1秒的时间间隔，否则会失败
//                int range = getIntent().getIntExtra("rfidrange", 30);//取不到值情况下，默认30
//                deviceManager.rfidRangeSet(range);
//                deviceManager.start();
//            }
//        }, 1000L);
    }

    private void startScan() {
        SystemProperties.set("persist.sys.scanstopimme", "false");
        Intent intent = new Intent();
        intent.setAction(START_SCAN_ACTION);
        sendBroadcast(intent, null);
    }
    @Override
    public void getMsg(String str) {
        //通过实现MyReceiver.Message接口可以在这里对MyReceiver中的数据进行处理
        tv_scan.setText(str);
        Intent result = new Intent();
        result.putExtra("result", str);
        setResult(801, result);
        ScanActivity.this.finish();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==131){
            handler = new Handler();
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
            }, 100L);
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            default:
                break;
        }
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
        unregisterReceiver(receiver);
        SystemProperties.set("persist.sys.scanstopimme", "true");
    }
}
