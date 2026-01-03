package tools.vlab.kberry.core.devices.actor;

import tools.vlab.kberry.core.devices.HeaterMode;
import tools.vlab.kberry.core.devices.StatusListener;

public interface FloorHeaterStatus extends StatusListener {

    void actuatorPositionChanged(FloorHeater floorHeater, int position);

    void setPointTemperatureChanged(FloorHeater floorHeater, float temperature);

    void setModeChanged(FloorHeater floorHeater, HeaterMode comfort);
}
