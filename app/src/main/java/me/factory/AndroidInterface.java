package me.factory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
//import android.os.SystemProperties;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.example.yzxing.CameraMainActivity;
import com.just.agentweb.AgentWeb;
import com.scandecode.ScanDecode;
import com.scandecode.inf.ScanInterface;

import org.json.JSONException;
import org.json.JSONObject;

public class AndroidInterface {

    private Handler deliver = new Handler(Looper.getMainLooper());
    private AgentWeb agent;
    private Context context;
    private String START_SCAN_ACTION = "com.geomobile.se4500barcode";
    //红外扫码
    private ScanInterface scanDecode;

    public AndroidInterface(final AgentWeb agent, final Context context) {
        this.agent = agent;
        this.context = context;

        scanDecode = new ScanDecode(context);
        scanDecode.initService("true");//初始化扫描服务
        scanDecode.getBarCode(new ScanInterface.OnScanListener() {
            @Override
            public void getBarcode(String data) {
                Intent result = new Intent();
                result.putExtra("result", data);
                try {
                    JSONObject reponse = new JSONObject();
                    reponse.put("action", "scanCodeStart");
                    reponse.put("result", data);
                    agent.getJsAccessEntrace().quickCallJs("receiveMsgFromNative", reponse.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void getBarcodeByte(byte[] bytes) {
                //返回原始解码数据
                //mReception.append(DataConversionUtils.byteArrayToString(bytes) +"\n");
            }
        });
    }

    @JavascriptInterface
    public void callAndroid(final String msg) {
        deliver.post(new Runnable() {
            @Override
            public void run() {
                if (BuildConfig.DEBUG) {
                    //Toast.makeText(context.getApplicationContext(), "" + msg, Toast.LENGTH_LONG).show();
                }
                try {
                    JSONObject jsonObject = new JSONObject(msg);
                    String action = jsonObject.getString("action");
                    // 开始盘点
                    if ("inventoryStart".equals(action)) {
                        Intent intent = new Intent(context, ScanActivity.class);
                        int rfidrange = jsonObject.getInt("range");
                        intent.putExtra("rfidrange",rfidrange);
                        ((Activity) context).startActivityForResult(intent, 1001);
                    }else if ("inventoryMore".equals(action)) {
                        Intent intent = new Intent(context, ScanMoreActivity.class);
                        int rfidrange = jsonObject.getInt("range");
                        intent.putExtra("rfidrange",rfidrange);
                        ((Activity) context).startActivityForResult(intent, 1002);
                    }else if("scanCodeStart".equals(action)){
//                        SystemProperties.set("persist.sys.scanstopimme", "false");
                        Intent intent = new Intent();
                        intent.setAction(START_SCAN_ACTION);
                        ((Activity) context).sendBroadcast(intent, null);
//                        Intent intent = new Intent(context, ScanCodeActivity.class);
//                        ((Activity) context).startActivityForResult(intent, 1003);
                    }else if("rfidwriteStart".equals(action)){
                        Intent intent = new Intent(context, RfidwriteActivity.class);
                        ((Activity) context).startActivityForResult(intent, 1001);
                    }else if("cameraStart".equals(action)){
                        Intent intent = new Intent(context, CameraMainActivity.class);
                        ((Activity) context).startActivityForResult(intent, 1001);
                    }
                } catch (Exception e) {
                    Toast.makeText(context.getApplicationContext(), "请求参数错误", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                Log.i("Info", "main Thread:" + Thread.currentThread());
            }
        });
        Log.i("Info", "Thread:" + Thread.currentThread());

    }


}
