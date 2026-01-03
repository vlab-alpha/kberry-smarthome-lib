package tools.vlab.kberry.core.devices.actor;

import tools.vlab.kberry.core.devices.StatusListener;

public interface OnOffStatus extends StatusListener {

    void onOffStatusChanged(OnOffDevice onOffDevice, boolean isOn);
}
