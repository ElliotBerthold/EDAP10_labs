package factory.simulation;

import factory.model.Tool;
import factory.model.Widget;

public class PaintThread extends Thread {

    private Tool paint;
    private FactoryMonitor monitor;

    public PaintThread(Tool tool, FactoryMonitor monitor) {
        this.paint = tool;
        this.monitor = monitor;

    }

    @Override
    public void run() {
        while (true) {
            paint.waitFor(Widget.BLUE_MARBLE);
            try {
                monitor.stopConveyor(paint);
                paint.performAction();
                monitor.startConveyor(paint);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
