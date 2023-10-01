package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;
import wash.io.WashingIO.Spin;
import static wash.control.WashingMessage.Order.*;

public class SpinController extends ActorThread<WashingMessage> {

    // TODO: add attributes
    private WashingIO io;
    private int spinDirection = 0;

    public SpinController(WashingIO io) {
        this.io = io;
    }

    @Override
    public void run() {

        // this is to demonstrate how to control the barrel spin:
        // io.setSpinMode(Spin.IDLE);

        try {

            // ... TODO ...

            while (true) {
                // wait for up to a (simulated) minute for a WashingMessage
                WashingMessage m = receiveWithTimeout(60000 / Settings.SPEEDUP);
                // if m is null, it means a minute passed and no message was received
                if (m != null) {
                    System.out.println("got " + m + "in spinController");
                    switch (m.order()) {
                        case SPIN_SLOW:
                            spinDirection = -1;
                            break;
                        case SPIN_FAST:
                            spinDirection = 2;
                            break;
                        default:
                            spinDirection = 0;
                            io.setSpinMode(Spin.IDLE);
                            break;
                    }
                    m.sender().send(new WashingMessage(this, ACKNOWLEDGMENT));
                } else {
                    switch (spinDirection) {
                        case -1:
                            io.setSpinMode(Spin.LEFT);
                            spinDirection = 1;
                            break;
                        case 1:
                            io.setSpinMode(Spin.RIGHT);
                            spinDirection = -1;
                            break;
                        case 2:
                            io.setSpinMode(Spin.FAST);
                            break;
                        default:
                            io.setSpinMode(Spin.IDLE);
                            break;
                    }
                }
                // ... TODO ...
            }
        } catch (InterruptedException unexpected) {
            // we don't expect this thread to be interrupted,
            // so throw an error if it happens anyway
            throw new Error(unexpected);
        }
    }
}
