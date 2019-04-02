package me.factory;

import java.util.Set;

public interface DeviceManager {

    void init();

    void start();

    void stop();

    void onResume();

    void onStop();

    void onDestory();

    void setListener(Listener listener);

    interface Listener {
        void onScan(String epc, Set<String> epcs);
    }

}
