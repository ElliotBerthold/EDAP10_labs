package train.simulation;

import train.view.TrainView;

public class TrainSimulation {

    public static void main(String[] args) throws InterruptedException {
        final int TRAIN_SIZE = 4;
        final int NUMBER_OF_ROUTES = 20;

        TrainMonitor monitor = new TrainMonitor();
        TrainView view = new TrainView();
        for (int i = 0; i < NUMBER_OF_ROUTES; i++) {
            Thread t = new Train(TRAIN_SIZE, view, monitor);
            t.start();
        }
    }

}
