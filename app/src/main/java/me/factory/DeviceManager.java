package me.factory;

import java.util.Set;

public interface DeviceManager {

    void init();

    void start();

    void stop();

    void onResume();

    void onStop();

    void onDestory();

    void rfidIDSet(String epcid);//写入rfid

    void rfidRangeSet(int num); //功率10-30

    void setListener(Listener listener);

    interface Listener {
        void onScan(String epc, Set<String> epcs);
        void onWrite(String epc);
    }

}
