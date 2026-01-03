package tools.vlab.kberry.core.devices.sensor;

import tools.vlab.kberry.core.devices.StatusListener;

public interface TemperatureStatus extends StatusListener {

    void temperatureChanged(TemperatureSensor sensor, float celsius);
}
