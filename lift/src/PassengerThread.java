import lift.LiftView;
import lift.Passenger;

public class PassengerThread extends Thread {
    private Passenger passenger;
    private LiftMonitor2 monitor;
    private LiftView view;

    public PassengerThread(LiftView view, LiftMonitor2 monitor) {
        this.view = view;
        this.passenger = view.createPassenger();
        this.monitor = monitor;
    }

    @Override
    public void run() {
        passenger.begin();
        monitor.addPassengerToQueue(passenger);
        try {
            monitor.enterLift(passenger);
            view.moveLift(passenger.getStartFloor(), passenger.getDestinationFloor());
            monitor.updateCurrentFloor(passenger.getDestinationFloor());
            monitor.exitLift(passenger);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            passenger.end();
        }

    }
}
