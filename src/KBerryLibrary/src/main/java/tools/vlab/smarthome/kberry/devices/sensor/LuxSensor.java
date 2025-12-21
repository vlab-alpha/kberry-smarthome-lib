package tools.vlab.smarthome.kberry.devices.sensor;

import tools.vlab.smarthome.kberry.PositionPath;
import tools.vlab.smarthome.kberry.baos.BAOSReadException;
import tools.vlab.smarthome.kberry.baos.messages.os.DataPoint;
import tools.vlab.smarthome.kberry.devices.Command;
import tools.vlab.smarthome.kberry.devices.KNXDevice;
import tools.vlab.smarthome.kberry.devices.LuxCategory;
import tools.vlab.smarthome.kberry.devices.PersistentValue;

import java.util.Vector;

public class LuxSensor extends KNXDevice {

    private final Vector<LuxStatus> listener = new Vector<>();
    private final PersistentValue<Float> currentLux;

    private LuxSensor(PositionPath positionPath, Integer refreshData) {
        super(positionPath, refreshData, Command.LUX_VALUE_ACTUAL);
        this.currentLux = new PersistentValue<>(positionPath, "Lux", 0.0f, Float.class);
    }

    public static LuxSensor at(PositionPath positionPath) {
        return new LuxSensor(positionPath, null);
    }

    public static LuxSensor at(PositionPath positionPath, int refreshIntervallMs) {
        return new LuxSensor(positionPath, refreshIntervallMs);
    }

    public void addListener(LuxStatus listener) {
        this.listener.add(listener);
    }

    public float getCurrentLux() {
        return currentLux.get();
    }

    public LuxCategory getLuxCategory() {
        return LuxCategory.fromLuxValue(this.currentLux.get());
    }

    @Override
    protected void received(Command command, DataPoint dataPoint) {
        switch (command) {
            case LUX_VALUE_ACTUAL -> dataPoint.getFloat9().ifPresent(value -> {
                this.currentLux.set(value);
                listener.forEach(luxStatus -> luxStatus.luxChanged(this, value));
            });
        }
    }

    @Override
    public void load() throws BAOSReadException {
        this.get(Command.LUX_VALUE_ACTUAL).flatMap(DataPoint::getFloat9).ifPresent(currentLux::set);
    }
}
