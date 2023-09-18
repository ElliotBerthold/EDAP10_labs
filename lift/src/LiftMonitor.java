import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import lift.LiftView;
import lift.Passenger;

public class LiftMonitor {
    private LiftView view;
    private Set<Passenger> passengersInLift;

    private boolean doorsAreMoving = false;

    private int currentFloor = 0;
    private int nextFloor;
    private int direction = 1;

    public LiftMonitor(LiftView view) {
        this.view = view;
        this.passengersInLift = new HashSet<>();
    }

    public synchronized ArrayList<Integer> moveLift() throws InterruptedException {
        ArrayList<Integer> array = new ArrayList<Integer>();
        while (doorsAreMoving) {
            wait();
        }
        array.add(currentFloor);
        nextFloor = currentFloor + direction;
        array.add(nextFloor);
        currentFloor = nextFloor;
        checkIfOutOfBounds();
        notifyAll();
        return array;
    }

    public synchronized void passengerEnter(Passenger passenger) throws InterruptedException {
        while (passenger.getStartFloor() != currentFloor) {
            wait();
        }
        if (!passengersInLift.contains(passenger)) {
            doorsAreMoving = true;
            view.openDoors(currentFloor);
            passenger.enterLift();
            passengersInLift.add(passenger);
            view.closeDoors();
            doorsAreMoving = false;
        }
        notifyAll();

    }

    public synchronized void passengerExit(Passenger passenger) throws InterruptedException {
        while (passenger.getDestinationFloor() != currentFloor) {
            wait();
        }
        if (passengersInLift.contains(passenger)) {
            doorsAreMoving = true;
            view.openDoors(currentFloor);
            passenger.exitLift();
            passengersInLift.remove(passenger);
            view.closeDoors();
            doorsAreMoving = false;
        }
        notifyAll();

    }

    private void checkIfOutOfBounds() {
        if (currentFloor == 6) {
            direction = -1;
        }
        if (currentFloor == 0) {
            direction = 1;
        }
    }

}
