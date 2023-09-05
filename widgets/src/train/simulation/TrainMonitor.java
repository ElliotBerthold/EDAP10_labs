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

/*
 * If we remove the wait() and the notifyAll() but keep the synchronized then
 * the whole simulation stops when one train gets stuck in the while loop. Its
 * CPU is maxed.
 * 
 * If we remove the synchronized then the other trains keep moving but we can
 * see that the trains that are stopped have a maxed CPU preformance meaning
 * that they are stuck in the while loop. You can see
 * that sometimes the exeption is thrown since two trains try to enter the same
 * segments. This is because the threads at the same time try to enter and then
 * it crashes. It happens rearly
 * 
 * If we change the while to if then all the trains eventually crashes if they
 * enter the same segments. The condition has to be checked again. It could work
 * if we only have trainlength 1 but with 3 we need the while loop to check that
 * the next segment is actualy free. However since two unrelated colissions
 * notifyes all the trains could still crash
 * 
 * With 8 length deadlook occurs. Trains get stuck in waiting loop around
 * intersections. They create a "full circle" and just waits for the other
 * trains tail to move but in tur the other train waits for its tail to move.
 * Infinet stopped loop. The problem here is routes, between intersections the
 * distance is less than 8 segments and thus the deadlook can occur. We also
 * have problems when "spawing", a train could "spawn" on top of another one and
 * thus crashing. We think that 4 is the maximum train length
 * 
 * Case a - busy-wait (one thread is lock in while loop)
 * Case b - busy-wait and race condition (two train could wait for eachother but
 * others can crash since they dont excecute in correct order)
 * Case c - race condition (the trains dont wait in correct order, one notifyAll
 * makes all trains move again even when they should not move)
 * Case d - deadlook (trains gets stuck in infinet loop of waiting for eachother
 * but none can move)
 */