package factory.simulation;

import factory.model.Tool;
import factory.model.Widget;

public class PressThread extends Thread {
    Tool press;
    FactoryMonitor monitor;

    public PressThread(Tool tool, FactoryMonitor monitor) {
        this.press = tool;
        this.monitor = monitor;
    }

    @Override
    public void run() {
        while (true) {
            press.waitFor(Widget.GREEN_BLOB);
            try {
                monitor.stopConveyor(press);
                press.performAction();
                monitor.startConveyor(press);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
