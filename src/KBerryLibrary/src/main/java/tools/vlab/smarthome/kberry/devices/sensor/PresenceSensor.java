package tools.vlab.smarthome.kberry.devices.sensor;

import tools.vlab.smarthome.kberry.PositionPath;
import tools.vlab.smarthome.kberry.baos.BAOSReadException;
import tools.vlab.smarthome.kberry.baos.messages.os.DataPoint;
import tools.vlab.smarthome.kberry.devices.Command;
import tools.vlab.smarthome.kberry.devices.KNXDevice;
import tools.vlab.smarthome.kberry.devices.PersistentValue;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

public class PresenceSensor extends KNXDevice {

    private final Vector<PresenceStatus> listener = new Vector<>();
    private final PersistentValue<Long> lastTrueMS;
    private final AtomicBoolean currentValue = new AtomicBoolean(false);

    private PresenceSensor(PositionPath positionPath, Integer refreshData) {
        super(positionPath, refreshData, Command.PRESENCE_STATUS);
        this.lastTrueMS = new PersistentValue<>(positionPath, "presence", 0L, Long.class);
    }

    public static PresenceSensor at(PositionPath positionPath) {
        return new PresenceSensor(positionPath, null);
    }

    public void addListener(PresenceStatus listener) {
        this.listener.add(listener);
    }

    public long getLastPresentSecond() {
        return this.lastTrueMS.get() != 0 ? ((System.currentTimeMillis() - this.lastTrueMS.get()) / 1000) : -1;
    }


    public boolean isPresent() {
        return currentValue.get();
    }

    @Override
    public void received(Command command, DataPoint dataPoint) {
        switch (command) {
            case PRESENCE_STATUS -> dataPoint.getBoolean().ifPresent(newValue -> {
                long currentTime = System.currentTimeMillis();
                boolean oldOrCurrentValue = this.currentValue.getAndSet(newValue);
                if (oldOrCurrentValue != newValue) {
                    if (newValue) {
                        lastTrueMS.set(currentTime);
                    }
                    listener.forEach(presenceStatus -> presenceStatus.presenceChanged(this, newValue));
                }
            });
        }
    }

    @Override
    public void load() throws BAOSReadException {
        this.get(Command.PRESENCE_STATUS).flatMap(DataPoint::getBoolean).ifPresent(currentValue::set);
    }
}
