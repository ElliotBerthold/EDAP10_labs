import java.util.LinkedList;
import java.util.Queue;

import lift.LiftView;
import lift.Passenger;

public class LiftMonitor2 {
    private Queue<Passenger> toEnter;
    private Queue<Passenger> toExit;
    private int currentFloor = 0;
    private LiftView view;

    private int NBR_FLOORS;
    private int MAX_PASSANGERS;

    public LiftMonitor2(int NBR_FLOORS, LiftView view, int MAX_PASSANGERS) {
        this.NBR_FLOORS = NBR_FLOORS;
        this.MAX_PASSANGERS = MAX_PASSANGERS;
        this.toEnter = new LinkedList<>();
        this.toExit = new LinkedList<>();
        this.view = view;
    }

    public synchronized void addPassengerToQueue(Passenger p) {
        toEnter.add(p);
        notifyAll();
    }

    public synchronized int getNextFloor() throws InterruptedException {
        while (toEnter.isEmpty() || !toExit.isEmpty()) {
            wait();
        }
        notifyAll();
        return toEnter.peek().getStartFloor();
    }

    public synchronized void enterLift(Passenger passenger) throws InterruptedException {
        while ((passenger.getStartFloor() != currentFloor || !toEnter.contains(passenger))
                || toExit.size() >= MAX_PASSANGERS) {
            wait();
        }
        Passenger firstInLine = toEnter.poll();
        view.openDoors(currentFloor);
        passenger.enterLift();
        view.closeDoors();
        toExit.add(firstInLine);
        notifyAll();
    }

    public synchronized void exitLift(Passenger passenger) throws InterruptedException {
        while (passenger.getDestinationFloor() != currentFloor || !toExit.contains(passenger)) {
            wait();
        }
        view.openDoors(currentFloor);
        passenger.exitLift();
        view.closeDoors();
        toExit.poll();
        notifyAll();
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public void updateCurrentFloor(int floor) {
        currentFloor = floor;
    }

    // private boolean doorsAreInMovment() {
    // return true;
    // }
}
