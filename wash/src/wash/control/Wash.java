package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;
import wash.simulation.WashingSimulator;

public class Wash {

    public static void main(String[] args) throws InterruptedException {
        WashingSimulator sim = new WashingSimulator(Settings.SPEEDUP);

        WashingIO io = sim.startSimulation();

        TemperatureController temp = new TemperatureController(io);
        WaterController water = new WaterController(io);
        SpinController spin = new SpinController(io);

        temp.start();
        water.start();
        spin.start();

        while (true) {
            int n = io.awaitButton();
            System.out.println("user selected program " + n);
            ActorThread<WashingMessage> selectedProgram;
            switch (n) {
                case 0:
                    System.out.println("STOP");
                    selectedProgram = new WashingProgram3(io, temp, water, spin);
                    break;
                case 1:
                    System.out.println("PROGRAM 1");
                    selectedProgram = new WashingProgram3(io, temp, water, spin);
                    break;
                case 2:
                    System.out.println("PROGRAM 2");
                    selectedProgram = new WashingProgram3(io, temp, water, spin);
                    break;
                case 3:
                    selectedProgram = new WashingProgram3(io, temp, water, spin);
                    break;
                default:
                    selectedProgram = new ActorThread<>();
                    break;
            }
            selectedProgram.start();
            // TODO:
            // if the user presses buttons 1-3, start a washing program
            // if the user presses button 0, and a program has been started, stop it
        }
    }
};
