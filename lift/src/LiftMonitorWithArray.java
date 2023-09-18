import java.util.stream.IntStream;

import lift.LiftView;
import lift.Passenger;

public class LiftMonitorWithArray {
    private LiftView view;

    private int[] toEnter;
    private int[] toExit;

    private int currentNbrOfPassengersInLift = 0;

    private boolean doorsAreOpen = false;
    private boolean liftIsMoving = false;

    private int currentFloor;
    private int MAX_PASSENGERS;

    public LiftMonitorWithArray(LiftView view, int NBR_FLOORS, int MAX_PASSENGERS) {
        this.view = view;
        toEnter = new int[NBR_FLOORS];
        toExit = new int[NBR_FLOORS];
        this.MAX_PASSENGERS = MAX_PASSENGERS;
    }

    private void openDoorsAtCurrentFloor() {
        if (!doorsAreOpen) {
            view.openDoors(currentFloor);
            doorsAreOpen = true;
        }
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public synchronized void updateCurrentFloor(int floor) {
        currentFloor = floor;
        liftIsMoving = false;
        notifyAll();
    }

    public synchronized void addPassengerToQueue(Passenger p) {
        toEnter[p.getStartFloor()]++;
        notifyAll();
    }

    public synchronized int getNextFloor() throws InterruptedException {
        view.showDebugInfo(toEnter, toExit);
        while ((IntStream.of(toEnter).sum() == 0) && doorsAreOpen
                || (toEnter[currentFloor] != 0) && IntStream.of(toExit).sum() < MAX_PASSENGERS) {
            wait();
        }
        liftIsMoving = true;
        notifyAll();
        for (int i = 0; i < toExit.length; i++) {
            if (toExit[i] > 0) {
                return i;
            }
        }
        for (int i = 0; i < toEnter.length; i++) {
            if (toEnter[i] > 0) {
                return i;
            }
        }
        return 0;
    }

    public synchronized void notifyMonitorThatPassengerEntered(Passenger p) {
        toEnter[p.getStartFloor()]--;
        toExit[p.getDestinationFloor()]++;
        notifyAll();
    }

    public synchronized void tryToEnterLift(Passenger p) throws InterruptedException {
        while (p.getStartFloor() != currentFloor || liftIsMoving || currentNbrOfPassengersInLift >= MAX_PASSENGERS) {
            wait();
        }
        currentNbrOfPassengersInLift++;
        openDoorsAtCurrentFloor();
    }

    public synchronized void closeDoors() throws InterruptedException {

        while ((toEnter[currentFloor] > 0 && IntStream.of(toExit).sum() < MAX_PASSENGERS)
                || toExit[currentFloor] > 0) {
            wait();
        }
        if (doorsAreOpen) {
            view.closeDoors();
            doorsAreOpen = false;
        }
        notifyAll();
    }

    public synchronized void tryToExitLift(Passenger p) throws InterruptedException {
        while (p.getDestinationFloor() != currentFloor || liftIsMoving) {
            wait();
        }
        currentNbrOfPassengersInLift--;
        openDoorsAtCurrentFloor();
    }

    public synchronized void notifyMonitorThatPassengerExited(Passenger p) {
        toExit[p.getDestinationFloor()]--;
        notifyAll();
    }

}
