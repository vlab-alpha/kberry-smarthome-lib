package tools.vlab.kberry.core.devices.actor;

import tools.vlab.kberry.core.devices.StatusListener;

public interface JalousieStatus extends StatusListener {

    void positionChanged(Jalousie jalousie, int position);
}
