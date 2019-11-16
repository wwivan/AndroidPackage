package me.factory;

import java.util.Set;

public interface DeviceManager {

    void init();

    void start();

    void onResume();

    void onStop();

    void onDestroy();

    void rfidIDSet(String epcid);//写入rfid

    void rfidRangeSet(int num); //功率10-30

    void setListener(Listener listener);

    interface Listener {
        void onScan(String tid, Set<String> tids);
        void onWrite(String epc);
    }

}
