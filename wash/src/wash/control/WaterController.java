package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;
import static wash.control.WashingMessage.Order.*;

public class WaterController extends ActorThread<WashingMessage> {
    private WashingIO io;
    private int targetLevel = 10; // 0..20 so halfway
    private int dt = 5000;
    private boolean pumpOn = false;
    private boolean drainOpen = false;
    private WashingMessage sender;

    // TODO: add attributes

    public WaterController(WashingIO io) {
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
                        case WATER_FILL:
                            io.drain(false);
                            drainOpen = false;
                            pumpOn = true;
                            io.fill(true);
                            break;
                        case WATER_DRAIN:
                            io.fill(false);
                            pumpOn = false;
                            drainOpen = true;
                            io.drain(true);
                            break;
                        default:
                            io.drain(false);
                            drainOpen = false;
                            io.fill(false);
                            pumpOn = false;
                            break;
                    }

                } else {
                    if (io.getWaterLevel() >= targetLevel && pumpOn) {
                        io.fill(false);
                        if (sender != null) {
                            sender.sender().send(new WashingMessage(this, ACKNOWLEDGMENT));
                        }
                        pumpOn = false;
                    }
                    if (io.getWaterLevel() == 0 && drainOpen) {
                        io.drain(false);
                        if (sender != null) {
                            sender.sender().send(new WashingMessage(this, ACKNOWLEDGMENT));
                        }
                        drainOpen = false;
                    }

                }
                // if (pumpOn && io.getWaterLevel() <= targetLevel) {
                // io.fill(true);
                // } else {
                // io.fill(false);
                // pumpOn = false;
                // }
                // if (drainOpen && io.getWaterLevel() >= targetLevel) {
                // io.drain(true);
                // } else {
                // io.drain(false);
                // drainOpen = false;
                // }
                // if (Math.abs(io.getWaterLevel() - targetLevel) < 4 && (drainOpen || pumpOn))
                // {
                // temp.sender().send(new WashingMessage(this, ACKNOWLEDGMENT));
                // }
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
