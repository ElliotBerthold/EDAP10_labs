import lift.LiftView;

public class MulitPassangerLift {
    public static int NBR_FLOORS = 10, MAX_PASSENGERS = 6, NBR_OF_PASSANGERS = 20;

    public static void main(String[] args) {

        LiftView view = new LiftView(NBR_FLOORS, MAX_PASSENGERS);
        // LiftMonitor2 monitor = new LiftMonitor2(NBR_FLOORS, view, MAX_PASSENGERS);
        LiftMonitor monitor = new LiftMonitor(view, NBR_FLOORS, MAX_PASSENGERS);
        for (int i = 0; i < NBR_OF_PASSANGERS; i++) {
            Thread passenger = new PassengerThread(view, monitor);
            passenger.start();
        }

        while (true) { // Styr hissen
            try {
                int nextFloor = monitor.tryToGetNextFloor(); // Kolla om det finns någon i toEnter eller i toExit, om
                                                             // det gör, åk dit annars vänta (aka wait())
                int currentFloor = monitor.getCurrentFloor();
                if (currentFloor != nextFloor) {
                    view.moveLift(currentFloor, nextFloor);
                    monitor.notifyLiftArrived(nextFloor);
                    monitor.closeDoors();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

// try {
// int nextFloor = monitor.getNextFloor();
// int currentFloor = monitor.getCurrentFloor();
// if (nextFloor != currentFloor) {
// view.moveLift(currentFloor, nextFloor);
// monitor.updateCurrentFloor(nextFloor);
// monitor.closeDoors();
// }
// } catch (InterruptedException e) {
// }