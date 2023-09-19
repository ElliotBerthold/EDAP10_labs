import lift.LiftView;
import lift.Passenger;

public class PassengerThread extends Thread {
    private Passenger passenger;
    private LiftMonitor monitor;
    private LiftView view;

    public PassengerThread(LiftView view, LiftMonitor monitor) {
        this.view = view;
        this.monitor = monitor;
    }

    @Override
    public void run() {
        while (true) {
            passenger = view.createPassenger();
            passenger.begin();
            monitor.addPassengerToQueue(passenger);
            try {
                monitor.tryToEnterLift(passenger);
                passenger.enterLift();
                monitor.notifyPassengerEntered(passenger);

                monitor.tryToExitLift(passenger); // Kolla att du är på rätt våning
                passenger.exitLift();
                monitor.notifyPassengerExited(passenger);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                passenger.end();
            }
        }
    }
}

// passenger = view.createPassenger();
// passenger.begin();
// monitor.addPassengerToQueue(passenger);
// try {
// monitor.tryToEnterLift(passenger);
// passenger.enterLift();
// monitor.notifyMonitorThatPassengerEntered(passenger);

// monitor.tryToExitLift(passenger);
// passenger.exitLift();
// monitor.notifyMonitorThatPassengerExited(passenger);
// } catch (InterruptedException e) {
// e.printStackTrace();
// } finally {
// passenger.end();
// }