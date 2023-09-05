package train.simulation;

import java.util.LinkedList;
import java.util.Queue;

import train.model.Route;
import train.model.Segment;
import train.view.TrainView;

public class Train extends Thread {
    private Queue<Segment> train = new LinkedList<>();
    private Route route;

    private int trainSize;

    private TrainMonitor monitor;

    public Train(int TRAIN_SIZE, TrainView view, TrainMonitor monitor) throws InterruptedException {
        this.route = view.loadRoute();
        this.trainSize = TRAIN_SIZE;
        this.monitor = monitor;
    }

    @Override
    public void run() {
        for (int i = 0; i < trainSize; i++) {
            Segment part = route.next();
            try {
                part.enter();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            train.add(part);
            try {
                monitor.occupieSegment(part);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while (true) {
            Segment part = route.next();
            try {
                monitor.occupieSegment(part);
                part.enter();
                Segment released = train.poll();
                released.exit();
                monitor.releaseSegment(released);
                train.add(part);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
