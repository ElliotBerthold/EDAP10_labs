package wash.control;

import actor.ActorThread;
import wash.control.WashingMessage.Order;

import static wash.control.WashingMessage.Order.*;

import wash.io.WashingIO;

public class TemperatureController extends ActorThread<WashingMessage> {

    // TODO: add attributes
    private WashingIO io;
    private int targetTemperature = 20;
    private double currentTemperature;
    private int dt = 10000;
    private WashingMessage sender = null;
    private boolean heatingOn = false;

    // When keeping the temperature at 40◦C, the relay must not be activated more
    // often than once every 200 seconds. At 60◦C, it must not be activated more
    // often than once every 100 seconds.

    public TemperatureController(WashingIO io) {
        // TODO
        this.io = io;
    }

    @Override
    public void run() {
        try {
            while (true) {
                WashingMessage m = receiveWithTimeout(dt / Settings.SPEEDUP);

                if (m != null) {
                    sender = m;
                    switch (m.order()) {
                        case TEMP_SET_40:
                            heatingOn = true;
                            targetTemperature = 40;
                            break;
                        case TEMP_SET_60:
                            heatingOn = true;
                            targetTemperature = 60;
                            break;
                        default:
                            heatingOn = false;
                            io.heat(false);
                            m.sender().send(new WashingMessage(this, ACKNOWLEDGMENT));
                            break;
                    }
                } else {
                    currentTemperature = io.getTemperature();
                    // System.out.println("Updatetime: " + updateTime);
                    // System.out.println("Target temperature: " + targetTemperature);
                    if (heatingOn && io.getWaterLevel() > 0) {
                        regulateTemperature();
                    }
                }

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void regulateTemperature() {
        double muUpper = dt / 1000 * 0.0478 + 0.2;
        double muLower = dt / 1000 * 9.52 / 1000;
        double upperBound = targetTemperature - muUpper;
        double lowerBound = targetTemperature - 2 + muLower;

        if (upperBound <= currentTemperature) {
            io.heat(false);
        }
        if (lowerBound >= currentTemperature) {
            io.heat(true);
        }
        if (currentTemperature >= lowerBound && currentTemperature <= upperBound && sender != null) {
            sender.sender().send(new WashingMessage(this, ACKNOWLEDGMENT));
            sender = null;
        }
    }

    public void send(ActorThread<WashingMessage> actorThread, Order tempIdle) {
    }

}
