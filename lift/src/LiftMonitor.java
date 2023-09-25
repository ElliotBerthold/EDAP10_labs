import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.stream.IntStream;

import lift.LiftView;
import lift.Passenger;

public class LiftMonitor {
    private LiftView view;

    private int[] toEnter;
    private int[] toExit;

    private Queue<Passenger> passengersInLift; // KEEPS TRACK OF THE MOVING PASSENGERS INTO OR OUT OF THE LIFT
                                               //
    private boolean liftIsMoving = false;
    private boolean doorsAreOpen = false;
    private boolean doorsAreMoving = false;
    private int currentFloor;

    private int previosFloor;

    private int MAX_PASSANGERS, NBR_FLOORS;

    public LiftMonitor(LiftView view, int NBR_FLOORS, int MAX_PASSANGERS) {
        this.view = view;
        this.MAX_PASSANGERS = MAX_PASSANGERS;
        this.NBR_FLOORS = NBR_FLOORS;
        this.previosFloor = NBR_FLOORS - 1;
        toEnter = new int[NBR_FLOORS];
        toExit = new int[NBR_FLOORS];

        passengersInLift = new LinkedList<>();
    }

    public synchronized void addPassengerToQueue(Passenger p) { // Notify to the lift that someone is waiting
        toEnter[p.getStartFloor()]++;
        notifyAll();
    }

    public synchronized int tryToGetNextFloor() throws InterruptedException { // Get the floor, if doors are open or
                                                                              // noone is waiting on lift, the
                                                                              // put in wait
        while ((IntStream.of(toEnter).sum() == 0 || toExit[currentFloor] > 0) && (doorsAreMoving || doorsAreOpen)) {// Accumulativ
                                                                                                                    // summering
            wait();
        }
        liftIsMoving = true;
        notifyAll();

        for (int i = previosFloor; i >= 0; i--) {
            if (toExit[i] > 0) {
                for (int j = i; j >= 0; j--) {
                    if (toEnter[j] > 0 && passengersInLift.size() < MAX_PASSANGERS) {
                        previosFloor = j;
                        // p.getStartFloor < p.getDestinationFloor == direction
                        return j;
                    }
                }
                // if (passengersInLift.size() > 0) {
                // return passengersInLift.peek().getDestinationFloor();
                // } else {
                previosFloor = i;
                return i;
                // }
            }
        }

        // i detta scenario nextFloor 4 sen 5,

        // OM inte to exti finns kolla i to enter.

        for (int i = 0; i < toEnter.length; i++) { // Åker alltid från lägsta våningen
            if (toEnter[i] > 0) {
                previosFloor = NBR_FLOORS - 1;
                return i;
            }
        }

        // om ingen väntar, stanna på 0
        return 0;
    }

    public synchronized void notifyLiftArrived(int floor) { // Change the current floor when the lift arrives
        currentFloor = floor;
        liftIsMoving = false;
        notifyAll();
    }

    public synchronized void tryToEnterLift(Passenger p) throws InterruptedException { // Step one to get onto the lift,
                                                                                       // put in wait if cant
        while (p.getStartFloor() != currentFloor || liftIsMoving || passengersInLift.size() >= MAX_PASSANGERS) {
            wait();
        }
        passengersInLift.add(p);
        openDoorsAtCurrentFloor();
    }

    public synchronized void notifyPassengerEntered(Passenger p) { // Step two to get onto the lift, notify that you
                                                                   // have entered
        toEnter[p.getStartFloor()]--;
        toExit[p.getDestinationFloor()]++;
        notifyAll();
    }

    public synchronized void tryToExitLift(Passenger p) throws InterruptedException { // Step one to get off the lift,
                                                                                      // put in wait if cant
        while (p.getDestinationFloor() != currentFloor || liftIsMoving) {
            wait();
        }
        passengersInLift.remove(p);
        openDoorsAtCurrentFloor();
    }

    public synchronized void notifyPassengerExited(Passenger p) { // Step two to get off the lift, notify that you have
                                                                  // exited
        toExit[p.getDestinationFloor()]--;
        notifyAll();
    }

    public int getCurrentFloor() { // Helper method to get the current floor
        return currentFloor;
    }

    private void openDoorsAtCurrentFloor() throws InterruptedException {
        while (liftIsMoving) {
            wait();
        }

        if (!doorsAreOpen) {
            doorsAreMoving = true;
            view.openDoors(currentFloor);
            doorsAreOpen = true;
            notifyAll();
        }
    }

    public synchronized void closeDoors() throws InterruptedException {
        while ((toEnter[currentFloor] > 0 && IntStream.of(toExit).sum() < MAX_PASSANGERS)
                || toExit[currentFloor] > 0) {
            wait();
        }
        if (doorsAreOpen) {
            doorsAreMoving = false;
            doorsAreOpen = false;
            view.closeDoors();
            notifyAll();
        }
    }
}

/*
 * IN EACH TRY YOU ARE PUT IN WAIT IF YOU CANT PREFORME THE ACTION
 * 
 * IN EACH NOTIFY THEN THE MONITOR NOTIFYALL TO UPDATE THE WAITING THREADS
 * 
 */