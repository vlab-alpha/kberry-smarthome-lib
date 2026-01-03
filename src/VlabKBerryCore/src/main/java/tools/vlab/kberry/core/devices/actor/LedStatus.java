package tools.vlab.kberry.core.devices.actor;

import tools.vlab.kberry.core.devices.RGB;
import tools.vlab.kberry.core.devices.StatusListener;

public interface LedStatus extends StatusListener {

    void colorChanged(Led led, RGB color);

    void isOnChanged(Led led, boolean onOff);
}
