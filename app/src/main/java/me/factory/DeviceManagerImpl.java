package me.factory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.UHFManager;
import com.speedata.libuhf.bean.SpdInventoryData;
import com.speedata.libuhf.interfaces.OnSpdInventoryListener;

import java.util.HashSet;
import java.util.Set;

import static android.content.Context.POWER_SERVICE;

public class DeviceManagerImpl implements DeviceManager {

    private IUHFService iuhfService;
    private Activity activity;
    private PowerManager pM = null;
    private PowerManager.WakeLock wK = null;
    private int init_progress = 0;
    private SoundPool soundPool;
    private int soundId;
    private long scant = 0;
    private Set<String> firm = new HashSet<>();
    private boolean inSearch = false;

    private Listener listener;

    public DeviceManagerImpl(Activity activity) {
        this.activity = activity;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void init() {
        UHFManager.setStipulationLevel(20);
        try {
            iuhfService = UHFManager.getUHFService(activity);
        } catch (Exception e) {
            e.printStackTrace();
            boolean cn = "CN".equals(activity.getResources().getConfiguration().locale.getCountry());
            if (cn) {
                Toast.makeText(activity, "模块不存在", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "Module does not exist", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        newWakeLock();
        sendUpddateService();
        //盘点选卡
        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        if (soundPool == null) {
            Log.e("as3992", "Open sound failed");
        }
        soundId = soundPool.load("/system/media/audio/ui/VideoRecord.ogg", 0);
        Log.w("as3992_6C", "id is " + soundId);
    }

    @Override
    public void start() {
        int i = iuhfService.setQueryTagGroup(0, 0, 0);
        if (i == 0) {
            //设置通话项成功
        }
        i = iuhfService.setDynamicAlgorithm();
        if (i == 0) {
            //设置成功
        }
        iuhfService.setOnInventoryListener(new OnSpdInventoryListener() {
            @Override
            public void getInventoryData(SpdInventoryData var1) {
                handler.sendMessage(handler.obtainMessage(1, var1));
            }
        });

        inSearch = true;
        scant = 0;
        firm.clear();
        //取消掩码
        iuhfService.selectCard(1, "", false);
        iuhfService.inventoryStart();
    }

    @Override
    public void stop() {
        inSearch = false;
        iuhfService.inventoryStop();
    }

    @Override
    public void onResume() {
        try {
            if (iuhfService != null) {
                if (openDev()) {
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        if (inSearch) {
            iuhfService.inventoryStop();
            inSearch = false;
        }
        soundPool.release();

        try {
            if (iuhfService != null) {
                iuhfService.closeDev();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDestory() {
        if (wK != null) {
            wK.release();
        }
        UHFManager.closeUHFService();
        this.listener = null;
    }

    private void newWakeLock() {
        init_progress++;
        pM = (PowerManager) activity.getSystemService(POWER_SERVICE);
        if (pM != null) {
            wK = pM.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK
                    | PowerManager.ON_AFTER_RELEASE, "lock3992");
            if (wK != null) {
                wK.acquire();
                init_progress++;
            }
        }
        if (init_progress == 1) {
            Log.w("3992_6C", "wake lock init failed");
        }
    }

    /**
     * 上电开串口
     *
     * @return
     */
    private boolean openDev() {
        if (iuhfService.openDev() != 0) {
            new AlertDialog.Builder(activity).setTitle("警告！")
                    .setMessage("扫描设备打开失败")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            activity.finish();
                        }
                    }).show();
            return true;
        }
        return false;
    }


    private void sendUpddateService() {
        Intent intent = new Intent();
        intent.setAction("uhf.update");
        activity.sendBroadcast(intent);
    }

    //新的Listener回调参考代码
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    scant++;
                    if (scant % 1 == 0) {
                        soundPool.play(soundId, 1, 1, 0, 0, 1);
                    }
                    SpdInventoryData var1 = (SpdInventoryData) msg.obj;
                    if (firm.add(var1.epc)) {
                        soundPool.play(soundId, 1, 1, 0, 0, 1);
                        if(listener != null){
                            listener.onScan(var1.epc, firm);
                        }
                    }
                    break;
            }

        }
    };

}