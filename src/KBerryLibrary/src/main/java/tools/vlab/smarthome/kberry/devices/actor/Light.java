package tools.vlab.smarthome.kberry.devices.actor;

import tools.vlab.smarthome.kberry.PositionPath;

public class Light extends OnOffDevice {

    private Light(PositionPath positionPath,Integer refreshData) {
        super(positionPath,refreshData,"Light");
    }


    public static Light at(PositionPath positionPath) {
        return new Light(positionPath, null);
    }
}
