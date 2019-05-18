package me.factory;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSON;
import com.just.agentweb.AgentWeb;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private AgentWeb mAgentWeb;
    //开始设置地址
    //public static final String defaultUrl = "http://192.168.31.137:8080/app/#/Device";
    //开始盘点
    //public static final String defaultUrl = "http://192.168.43.252:8080/app/#/Device";
    public static final String defaultUrl = " http://192.168.31.137:8080/app/";
    //开始红外扫描
    //public static final String defaultUrl = "http://192.168.31.137:8080/app/#/DeviceScanCode";
    //开始摄像头扫描
    //public static final String defaultUrl = "http://192.168.43.252:8080/app/#/DeviceCamera";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LinearLayout view = findViewById(R.id.main);
        String url = getIntent().getStringExtra("url");
        mAgentWeb = AgentWeb.with(this)
                .setAgentWebParent(view, new LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator()
                .createAgentWeb()
                .ready()
                .go(TextUtils.isEmpty(url) ? defaultUrl : url);

        mAgentWeb.getJsInterfaceHolder().addJavaObject("android", new AndroidInterface(mAgentWeb, this));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (mAgentWeb.handleKeyEvent(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!mAgentWeb.back()) {
            this.finish();
        }
    }

    @Override
    protected void onPause() {
        mAgentWeb.getWebLifeCycle().onPause();
        super.onPause();

    }

    @Override
    protected void onResume() {
        mAgentWeb.getWebLifeCycle().onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mAgentWeb.getWebLifeCycle().onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            Object[] ecps = (Object[]) data.getSerializableExtra("result");
            String result = JSON.toJSONString(ecps);
            try {
                JSONObject reponse = new JSONObject();
                reponse.put("action", "inventoryStart");
                reponse.put("result", result);
                mAgentWeb.getJsAccessEntrace().quickCallJs("receiveMsgFromNative", reponse.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else if (requestCode == 1002 && resultCode == RESULT_OK) {
            Object[] ecps = (Object[]) data.getSerializableExtra("result");
            String result = JSON.toJSONString(ecps);
            try {
                JSONObject reponse = new JSONObject();
                reponse.put("action", "inventoryMore");
                reponse.put("result", result);
                mAgentWeb.getJsAccessEntrace().quickCallJs("receiveMsgFromNative", reponse.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (requestCode == 1003 && resultCode == 801) {
            String rfidcode = data.getStringExtra("result");
            try {
                JSONObject reponse = new JSONObject();
                reponse.put("action", "scanCodeStart");
                reponse.put("result", rfidcode);
                mAgentWeb.getJsAccessEntrace().quickCallJs("receiveMsgFromNative", reponse.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else if (requestCode == 1001 && resultCode == 802) {
            String rfidcode = data.getStringExtra("result");
            try {
                JSONObject reponse = new JSONObject();
                reponse.put("action", "rfidwriteStart");
                reponse.put("result", rfidcode);
                mAgentWeb.getJsAccessEntrace().quickCallJs("receiveMsgFromNative", reponse.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else if (requestCode == 1001 && resultCode == 803) {
            String rfidcode = data.getStringExtra("result");
            try {
                JSONObject reponse = new JSONObject();
                reponse.put("action", "cameraStart");
                reponse.put("result", rfidcode);
                mAgentWeb.getJsAccessEntrace().quickCallJs("receiveMsgFromNative", reponse.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
