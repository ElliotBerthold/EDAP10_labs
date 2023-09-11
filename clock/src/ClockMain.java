import java.util.concurrent.Semaphore;

import clock.AlarmClockEmulator;
import clock.io.Choice;
import clock.io.ClockInput;
import clock.io.ClockInput.UserInput;
import clock.io.ClockOutput;

public class ClockMain {
    public static void main(String[] args) throws InterruptedException {
        AlarmClockEmulator emulator = new AlarmClockEmulator();

        ClockInput in = emulator.getInput();
        ClockOutput out = emulator.getOutput();

        Semaphore mutexInput = in.getSemaphore();

        // out.displayTime(15, 2, 37); // arbitrary time: just an example

        Monitor monitor = new Monitor(15, 0, 0, out);

        // Thread timeIncrementer = new RealTimeIncrementor(monitor); //Using Thread
        Thread timeIncrementer = new Thread(() -> incrementTime(monitor)); // Using lambda

        timeIncrementer.start();
        while (true) {
            mutexInput.acquire();
            UserInput userInput = in.getUserInput();
            Choice c = userInput.choice();
            int h = userInput.hours();
            int m = userInput.minutes();
            int s = userInput.seconds();
            switch (c) {
                case SET_ALARM:
                    monitor.setAlarm(h, m, s);
                    out.setAlarmIndicator(true);
                    break;
                case SET_TIME:
                    monitor.setTime(h, m, s);
                    break;
                case TOGGLE_ALARM:
                    out.setAlarmIndicator(monitor.alarmSwitch());
                    break;

            }
            System.out.println("choice=" + c + " h=" + h + " m=" + m + " s=" + s);
        }
    }

    private static void incrementTime(Monitor monitor) {
        long t0 = System.currentTimeMillis();
        long target = t0 + 1000; // 1000ms
        while (true) {
            try {
                monitor.increment(); // 400ms
                monitor.alarm();

                long now = System.currentTimeMillis(); // 400ms
                Thread.sleep(target - now); // 1000-400;
                target += 1000; // 2000ms
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class Monitor {
    private int timeHours;
    private int timeMinutes;
    private int timeSeconds;

    private int alarmHours;
    private int alarmMinutes;
    private int alarmSeconds;

    private boolean alarmOn = false;
    private ClockOutput out;

    private Semaphore mutex = new Semaphore(1);

    Monitor(int hours, int minutes, int seconds, ClockOutput out) {
        timeHours = hours;
        timeMinutes = minutes;
        timeSeconds = seconds;
        this.out = out;
    }

    public void increment() throws InterruptedException {
        mutex.acquire();
        timeSeconds++;
        if (timeSeconds > 59) {
            timeSeconds = 0;
            timeMinutes++;
            if (timeMinutes > 59) {
                timeMinutes = 0;
                timeHours++;
                if (timeHours > 23) {
                    timeHours = 0;
                }
            }
        }
        out.displayTime(timeHours, timeMinutes, timeSeconds);
        mutex.release();
    }

    public void setTime(int hours, int minutes, int seconds) throws InterruptedException {
        mutex.acquire();

        timeHours = hours;
        timeMinutes = minutes;
        timeSeconds = seconds;
        mutex.release();
    }

    public void setAlarm(int hours, int minutes, int seconds) throws InterruptedException {
        mutex.acquire();
        alarmHours = hours;
        alarmMinutes = minutes;
        alarmSeconds = seconds;
        alarmOn = true;
        mutex.release();
    }

    public boolean alarmSwitch() throws InterruptedException {
        mutex.acquire();
        this.alarmOn = !this.alarmOn;
        mutex.release();
        return this.alarmOn;

    }

    private boolean shouldAlarm() {
        int timeInSeconds = (timeHours == 0 ? 23 : timeHours) * 24 * 60
                + (timeMinutes == 0 && alarmMinutes != 0 ? 60 : timeMinutes) * 60 + timeSeconds;
        int alarmInSeconds = alarmHours * 24 * 60 + alarmMinutes * 60 + alarmSeconds;
        int diff = timeInSeconds - alarmInSeconds;
        return diff <= 20 && diff >= 0 && alarmOn;
    }

    public void alarm() throws InterruptedException {
        mutex.acquire();
        if (this.shouldAlarm()) {
            out.alarm();
        }
        mutex.release();
    }

}

// class RealTimeIncrementor extends Thread {

// private Monitor monitor;

// RealTimeIncrementor(Monitor monitor) {
// this.monitor = monitor;
// }

// @Override
// public void run() {
// long t0 = System.currentTimeMillis();
// long target = t0 + 1000; // 1000ms
// while (true) {
// try {
// monitor.increment(); // 400ms
// monitor.alarm();

// long now = System.currentTimeMillis(); // 400ms
// Thread.sleep(target - now); // 1000-400;
// target += 1000; // 2000ms
// } catch (InterruptedException e) {
// e.printStackTrace();
// }
// }
// }
// }
