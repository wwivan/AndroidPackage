package me.factory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.UHFManager;
import com.speedata.libuhf.bean.SpdInventoryData;
import com.speedata.libuhf.interfaces.OnSpdInventoryListener;
import com.speedata.libuhf.utils.SharedXmlUtil;
import com.speedata.libuhf.utils.StringUtils;

import java.util.HashSet;
import java.util.Set;

public class DeviceManagerImpl implements DeviceManager {

    private IUHFService iuhfService;
    private Activity activity;
    private PowerManager.WakeLock wK = null;
    private SoundPool soundPool;
    private int soundId;
    private long scant = 0;
    private Set<String> firm = new HashSet<>();
    private boolean inSearch = false;

    private Listener listener;
    private int scanFlag = 0; //0 单一扫描 1 批量扫描
    public final static String UHF_INV_CON = "uhf_inv_con";

    private static final String TAG = "DeviceManagerImpl";

    public DeviceManagerImpl(Activity activity, int flag) {
        this.activity = activity;
        this.scanFlag = flag;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void init() {
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
        initSoundPool();

    }

    private void initSoundPool() {
        //盘点选卡
        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        soundId = soundPool.load("/system/media/audio/ui/VideoRecord.ogg", 0);
        Log.w("as3992_6C", "id is " + soundId);
    }

    @Override
    public void start() {
        int i = iuhfService.setInvMode(SharedXmlUtil.getInstance(activity).read(UHF_INV_CON, 1), 0, 6);
        if (i == 0) {
            //设置通话项成功
            Log.d(TAG, String.format("设置通话项成功,i: %d", i));
        }
        iuhfService.setOnInventoryListener(new OnSpdInventoryListener() {
            @Override
            public void getInventoryData(SpdInventoryData var1) {
                Log.d(TAG, "getInventoryData返回数据：[epc:" + var1.getEpc() + "rssi" + var1.getRssi() + "tid" + var1.getTid() + "]");
                handler.sendMessage(handler.obtainMessage(1, var1));
            }
        });

        inSearch = true;
        firm.clear();
        //取消掩码
        iuhfService.selectCard(1, "", false);
        iuhfService.inventoryStart();
    }

    @Override
    public void onResume() {
        try {
            if (iuhfService != null) {
                boolean openResult = openDev();
                Log.d(TAG, "onResume: openDev result: " + openResult);
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
    public void onDestroy() {
        UHFManager.closeUHFService();
        this.listener = null;
    }

    /**
     * 上电开串口
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

    @Override
    public void rfidRangeSet(int num) {
        //获取当前功率，只在当前activity有效，关闭则需要重新设置
//        int ivp = iuhfService.getAntennaPower();
//        Toast.makeText(activity, "当前功率："+ivp, Toast.LENGTH_SHORT).show();
        if ((num < 10) || (num > 33)) {
            //Toast.makeText(activity, "功率范围10 ~ 33", Toast.LENGTH_SHORT).show();
            new toast_thread().setr("功率范围10 ~ 33").start();
        } else {
            int rv = iuhfService.setAntennaPower(num);
            if (rv < 0) {
                //Toast.makeText(activity, "功率设置失败!", Toast.LENGTH_SHORT).show();
                //new toast_thread().setr("功率设置失败!").start();
            } else {
                //Toast.makeText(activity, "功率设置成功!", Toast.LENGTH_SHORT).show();
                //new toast_thread().setr("功率设置成功!").start();
            }
        }
    }

    @Override
    public void rfidIDSet(String epcid) {
        final String epc_str = epcid.replace(" ", "");
        final byte[] write = StringUtils.stringToByte(epc_str);
        final int epcl;
        try {
            epcl = Integer.parseInt("3", 10);
        } catch (NumberFormatException e) {
            return;
        }
        final String rfidcode = epcid;
        //isSuccess = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                int writeArea = set_EPC(epcl, "00000000", write);
                if (writeArea != 0) {
                    handler.sendMessage(handler.obtainMessage(2, "参数不正确"));
                } else {
                    handler.obtainMessage(2, rfidcode);
                }
            }
        }).start();
    }

    int set_EPC(int epclength, String passwd, byte[] EPC) {
        soundPool.play(soundId, 1, 1, 0, 0, 1);
        byte[] res;
        if (epclength > 31) {
            return -3;
        }
        if (epclength * 2 < EPC.length) {
            return -3;
        }
        res = iuhfService.read_area(iuhfService.EPC_A, 1, 1, passwd);
        if (res == null) {
            return -5;
        }
        res[0] = (byte) ((res[0] & 0x7) | (epclength << 3));
        byte[] f = new byte[2 + epclength * 2];
        try {
            System.arraycopy(res, 0, f, 0, 2);
            System.arraycopy(EPC, 0, f, 2, epclength * 2);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        SystemClock.sleep(500);
        return iuhfService.writeArea(iuhfService.EPC_A, 1, f.length / 2, passwd, f);
    }


    private void rfidIDRead(String tab) {
        ;
        if (inSearch) {
            inSearch = false;
            iuhfService.inventoryStop();
            //btn_rfid.setText("开始读取");
        } else {
            inSearch = true;
            scant = 0;
            //取消掩码
            iuhfService.selectCard(1, "", false);
            //EventBus.getDefault().post(new MsgEvent("CancelSelectCard", ""));
            iuhfService.inventoryStart();
            //btn_rfid.setText("结束读取");
        }
    }

    private void rfidIDReadClose() {
        if (inSearch) {
            inSearch = false;
            iuhfService.inventoryStop();
            //btn_rfid.setText("开始读取");
        }
    }

    private class toast_thread extends Thread {

        String a;

        public toast_thread setr(String m) {
            a = m;
            return this;
        }

        public void run() {
            super.run();
            Looper.prepare();
            //Toast.makeText(activity, a, Toast.LENGTH_LONG).show();
            Looper.loop();
        }
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
                    if (firm.add(var1.tid)) {
                        soundPool.play(soundId, 1, 1, 0, 0, 1);
                        if (listener != null) {
                            listener.onScan(var1.tid, firm);
                        }
                    }
                    if (scanFlag == 0) iuhfService.inventoryStop();
                    //Toast.makeText(activity, var1.epc, Toast.LENGTH_LONG).show();
                    break;
                case 2:
                    soundPool.play(soundId, 1, 1, 0, 0, 1);
                    if (listener != null) {
                        listener.onWrite(msg.obj.toString());
                    }
                    break;
            }

        }
    };

}
