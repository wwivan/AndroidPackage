package me.factory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.just.agentweb.AgentWeb;

import org.json.JSONObject;

public class AndroidInterface {

    private Handler deliver = new Handler(Looper.getMainLooper());
    private AgentWeb agent;
    private Context context;

    public AndroidInterface(AgentWeb agent, Context context) {
        this.agent = agent;
        this.context = context;
    }

    @JavascriptInterface
    public void callAndroid(final String msg) {
        deliver.post(new Runnable() {
            @Override
            public void run() {
                if (BuildConfig.DEBUG) {
                    Toast.makeText(context.getApplicationContext(), "" + msg, Toast.LENGTH_LONG).show();
                }
                try {
                    JSONObject jsonObject = new JSONObject(msg);
                    String action = jsonObject.getString("action");
                    // 开始盘点
                    if ("inventoryStart".equals(action)) {
                        Intent intent = new Intent(context, ScanActivity.class);
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
