package tools.vlab.kberry.core.devices.sensor;

import tools.vlab.kberry.core.devices.StatusListener;

public interface VOCStatus extends StatusListener {

    void vocChanged(VOCSensor sensor, float voc);

}
