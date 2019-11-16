package me.factory;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.scandecode.ScanDecode;
import com.scandecode.inf.ScanInterface;

public class ScanCodeActivity extends AppCompatActivity implements MyReceiver.Message{
    //scan qrcode barcode
    private Button btnSingleScan;
    private TextView tv_scan;
    private int scancount = 0;
    private ScanInterface scanDecode;
    private String RECE_DATA_ACTION = "com.se4500.onDecodeComplete";
    private String START_SCAN_ACTION = "com.geomobile.se4500barcode";
    private String STOP_SCAN = "com.geomobile.se4500barcode.poweroff";
    private MyReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scancode);
        //scar start
        btnSingleScan = (Button) findViewById(R.id.buttonscan);
        tv_scan = (TextView) findViewById(R.id.tv_scan);
//        Intent result = new Intent();
//        result.putExtra("result", "12344321");
//        setResult(801, result);
//        ScanCodeActivity.this.finish();
//        judgePropert();
        IntentFilter filter = new IntentFilter();
        filter.addAction(RECE_DATA_ACTION);
        filter.addAction(START_SCAN_ACTION);
        filter.addAction(STOP_SCAN);
        receiver = new MyReceiver();
        registerReceiver(receiver, filter);
        receiver.setMessage(this);
        scanDecode = new ScanDecode(this);
        scanDecode.initService("true");//初始化扫描服务
        scanDecode.getBarCode(new ScanInterface.OnScanListener() {
            @Override
            public void getBarcode(String data) {
                tv_scan.setText(data);
                Intent result = new Intent();
                result.putExtra("result", data);
                setResult(801, result);
                ScanCodeActivity.this.finish();
            }

            @Override
            public void getBarcodeByte(byte[] bytes) {
                //返回原始解码数据
                //mReception.append(DataConversionUtils.byteArrayToString(bytes) +"\n");
            }
        });
        btnSingleScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //scanDecode.starScan();//启动扫描
                startScan();
            }
        });
    }
    @Override
     public void getMsg(String str) {
        //通过实现MyReceiver.Message接口可以在这里对MyReceiver中的数据进行处理
        tv_scan.setText(str);
        Intent result = new Intent();
        result.putExtra("result", str);
        setResult(801, result);
        ScanCodeActivity.this.finish();
    }

    private void startScan() {
        SystemProperties.set("persist.sys.scanstopimme", "false");
        Intent intent = new Intent();
        intent.setAction(START_SCAN_ACTION);
        sendBroadcast(intent, null);
    }

    private void judgePropert() {
        String result = SystemProperties.get("persist.sys.keyreport", "true");
        if (result.equals("false")) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.key_test_back_title)
                    .setMessage(R.string.action_dialog_setting_config)
                    .setPositiveButton(
                            R.string.action_dialog_setting_config_sure_go,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    // TODO Auto-generated method stub
                                    Intent intent = new Intent(
                                            Settings.ACTION_ACCESSIBILITY_SETTINGS);
                                    startActivityForResult(intent, 1);
                                }
                            })
                    .setNegativeButton(R.string.action_exit_cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    finish();
                                }
                            }
                    ).show();
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //  deviceManager.onDestroy();
        unregisterReceiver(receiver);
        SystemProperties.set("persist.sys.scanstopimme", "true");
    }
}
