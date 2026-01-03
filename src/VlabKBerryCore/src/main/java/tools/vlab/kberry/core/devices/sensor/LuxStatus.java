package tools.vlab.kberry.core.devices.sensor;

import tools.vlab.kberry.core.devices.StatusListener;

public interface LuxStatus extends StatusListener {

    void luxChanged(LuxSensor sensor, float lux);
}
