package tools.vlab.kberry.core.devices.sensor;

import tools.vlab.kberry.core.devices.StatusListener;

public interface HumidityStatus extends StatusListener {

    void humidityChanged(HumiditySensor sensor, float humidity);

}
