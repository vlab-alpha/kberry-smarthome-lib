package tools.vlab.kberry.core.devices.sensor;

import tools.vlab.kberry.core.devices.StatusListener;

public interface PresenceStatus extends StatusListener {

    void presenceChanged(PresenceSensor sensor, boolean available);
}
