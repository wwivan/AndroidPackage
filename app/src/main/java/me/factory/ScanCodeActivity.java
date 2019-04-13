package me.factory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.scandecode.ScanDecode;
import com.scandecode.inf.ScanInterface;
import java.util.HashSet;
import java.util.Set;

public class ScanCodeActivity extends AppCompatActivity {
    //scan qrcode barcode
    private ScanInterface scanDecode;
    private Button btnSingleScan;
    private TextView tv_scan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scancode);
        //scar start
        btnSingleScan = (Button) findViewById(R.id.buttonscan);
        tv_scan = (TextView) findViewById(R.id.tv_scan);
        Intent result = new Intent();
        result.putExtra("result", "12344321");
        setResult(801, result);
        ScanCodeActivity.this.finish();
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
                scanDecode.starScan();//启动扫描
            }
        });
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
      //  deviceManager.onDestory();
    }
}
