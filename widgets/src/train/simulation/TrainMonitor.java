package train.simulation;

import java.util.Set;
import java.util.HashSet;

import train.model.Segment;

public class TrainMonitor {
    private Set<Segment> occupiedSegments = new HashSet<>();

    private void checkIsFree(Segment segment) throws InterruptedException {
        while (occupiedSegments.contains(segment)) {
            wait();
        }
    }

    public synchronized void occupieSegment(Segment segment) throws InterruptedException {
        checkIsFree(segment);
        occupiedSegments.add(segment);
    }

    public synchronized void releaseSegment(Segment segment) {
        occupiedSegments.remove(segment);
        notifyAll();
    }
}
