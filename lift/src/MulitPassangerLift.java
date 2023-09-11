import java.util.ArrayList;

import lift.LiftView;

public class MulitPassangerLift {
    public static int NBR_FLOORS = 7, MAX_PASSENGERS = 4, NBR_OF_PASSANGERS = 4;

    public static void main(String[] args) {

        LiftView view = new LiftView(NBR_FLOORS, MAX_PASSENGERS);
        LiftMonitor2 monitor = new LiftMonitor2(NBR_FLOORS, view, MAX_PASSENGERS);
        for (int i = 0; i < NBR_OF_PASSANGERS; i++) {
            Thread passenger = new PassengerThread(view, monitor);
            passenger.start();
        }

        while (true) {
            try {
                int nextFloor = monitor.getNextFloor();
                int currentFloor = monitor.getCurrentFloor();
                if (nextFloor != currentFloor) {
                    view.moveLift(currentFloor, nextFloor);
                    monitor.updateCurrentFloor(nextFloor);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
