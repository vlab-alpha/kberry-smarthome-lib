package tools.vlab.kberry.core.devices.sensor;

import tools.vlab.kberry.core.devices.StatusListener;

public interface ElectricStatus extends StatusListener {

    void kwhChanged(ElectricitySensor sensor, float kwh);

    void electricityChanged(ElectricitySensor sensor, int electricity);

}
