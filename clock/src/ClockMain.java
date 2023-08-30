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

        Semaphore mutex = in.getSemaphore();

        // out.displayTime(15, 2, 37); // arbitrary time: just an example

        Monitor monitor = new Monitor(15, 0, 0, out);

        Thread timeIncrementer = new RealTimeIncrementor(monitor);
        timeIncrementer.start();
        while (true) {
            mutex.acquire();
            UserInput userInput = in.getUserInput();
            Choice c = userInput.choice();
            int h = userInput.hours();
            int m = userInput.minutes();
            int s = userInput.seconds();
            switch (c) {
                case SET_ALARM:
                    if (monitor.alarmOn) {
                        monitor.alarmOn = false;
                        monitor.setAlarm(0, 0, 0);
                        out.setAlarmIndicator(false);
                    } else {
                        monitor.alarmOn = true;
                        monitor.setAlarm(h, m, s);
                        out.setAlarmIndicator(true);
                    }
                    break;
                case SET_TIME:
                    monitor.setTime(h, m, s);
                    break;
                case TOGGLE_ALARM:
                    out.setAlarmIndicator(monitor.alarmOn);
                    break;

            }
            System.out.println("choice=" + c + " h=" + h + " m=" + m + " s=" + s);
        }
    }
}

class Monitor {
    private int[] time = { 0, 0, 0 };
    private int[] alarm = { 0, 0, 0 };
    public boolean alarmOn = false;
    private ClockOutput out;

    Monitor(int hours, int minutes, int seconds, ClockOutput out) {
        this.time[0] = hours;
        this.time[1] = minutes;
        this.time[2] = seconds;
        this.out = out;
    }

    public int[] getTime() {
        // int[] time = {hours, minutes, seconds };
        return time;
    }

    public void increment() {
        time[2]++;
        if (time[2] > 59) {
            time[2] = 0;
            time[1]++;
            if (time[1] > 59) {
                time[1] = 0;
                time[0]++;
                if (time[0] > 23) {
                    time[0] = 0;
                }
            }
        }
        out.displayTime(time[0], time[1], time[2]);
    }

    public void setTime(int hours, int minutes, int seconds) {
        int[] time = { hours, minutes, seconds };
        this.time = time;
    }

    public void setAlarm(int hours, int minutes, int seconds) {
        int[] alarm = { hours, minutes, seconds };
        this.alarm = alarm;

    }

    private boolean shouldAlarm() {
        int timeInSeconds = (time[0] == 0 ? 23 : time[0]) * 24 * 60 + (time[1] == 0 ? 60 : time[1]) * 60 + time[2];
        int alarmInSeconds = alarm[0] * 24 * 60 + alarm[1] * 60 + alarm[2];
        int diff = timeInSeconds - alarmInSeconds;
        return diff <= 20 && diff >= 0;
    }

    public void alarm() {
        if (this.alarmOn && this.shouldAlarm()) {
            out.alarm();
        }
    }
}

class RealTimeIncrementor extends Thread {

    private Monitor monitor;

    RealTimeIncrementor(Monitor monitor) {
        this.monitor = monitor;
    }

    public void run() {
        long t0 = System.currentTimeMillis();
        long target = t0 + 1000;
        while (true) {
            try {
                monitor.increment();
                monitor.alarm();
                long now = System.currentTimeMillis();
                Thread.sleep(target - now);
                target += 1000;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}