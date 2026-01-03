package tools.vlab.kberry.core.devices.actor;

import tools.vlab.kberry.core.devices.StatusListener;

public interface DimmerStatus extends StatusListener {

    void isOnChanged(Dimmer dimmer, boolean onOff);
    void brightnessChanged(Dimmer dimmer, int percent);
}
