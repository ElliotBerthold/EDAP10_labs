package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

import static wash.control.WashingMessage.Order.*;

/**
 * Program 3 for washing machine. This also serves as an example of how washing
 * programs can be structured.
 * 
 * This short program stops all regulation of temperature and water levels,
 * stops the barrel from spinning, and drains the machine of water.
 * 
 * It can be used after an emergency stop (program 0) or a power failure.
 */
public class WashingProgram2 extends ActorThread<WashingMessage> {

    private WashingIO io;
    private ActorThread<WashingMessage> temp;
    private ActorThread<WashingMessage> water;
    private ActorThread<WashingMessage> spin;

    public WashingProgram2(WashingIO io,
            ActorThread<WashingMessage> temp,
            ActorThread<WashingMessage> water,
            ActorThread<WashingMessage> spin) {
        this.io = io;
        this.temp = temp;
        this.water = water;
        this.spin = spin;
    }

    @Override
    public void run() {
        try {
            System.out.println("washing program 2 started");

            // Lock the hatch
            io.lock(true);
            // Instruct SpinController to rotate barrel slowly, back and forth
            // Expect an acknowledgment in response.
            water.send(new WashingMessage(this, WATER_FILL));
            receive();

            temp.send(new WashingMessage(this, TEMP_SET_40));
            receive();

            spin.send(new WashingMessage(this, SPIN_SLOW));
            receive();

            Thread.sleep(21 * 60000 / Settings.SPEEDUP);

            temp.send(new WashingMessage(this, TEMP_IDLE));
            receive();

            water.send(new WashingMessage(this, WATER_DRAIN));
            receive();

            water.send(new WashingMessage(this, WATER_FILL));
            receive();

            temp.send(new WashingMessage(this, TEMP_SET_60));
            receive();

            Thread.sleep(31 * 60000 / Settings.SPEEDUP);

            temp.send(new WashingMessage(this, TEMP_IDLE));
            receive();

            water.send(new WashingMessage(this, WATER_DRAIN));
            receive();
            for (int i = 0; i < 5; i++) {
                water.send(new WashingMessage(this, WATER_FILL));
                receive();
                Thread.sleep(2 * 60000 / Settings.SPEEDUP);
                water.send(new WashingMessage(this, WATER_DRAIN));
                receive();
            }

            spin.send(new WashingMessage(this, SPIN_FAST));
            receive();
            io.drain(true);

            Thread.sleep(6 * 60000 / Settings.SPEEDUP);

            spin.send(new WashingMessage(this, SPIN_OFF));
            receive();

            io.drain(false);

            // Now that the barrel has stopped, it is safe to open the hatch.
            io.lock(false);

            System.out.println("washing program 2 finished");
        } catch (InterruptedException e) {

            // If we end up here, it means the program was interrupt()'ed:
            // set all controllers to idle

            temp.send(new WashingMessage(this, TEMP_IDLE));
            water.send(new WashingMessage(this, WATER_IDLE));
            spin.send(new WashingMessage(this, SPIN_OFF));
            System.out.println("washing program terminated");
        }
    }
}
