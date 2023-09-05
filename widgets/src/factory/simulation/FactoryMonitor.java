package factory.simulation;

import java.util.HashMap;
import java.util.Map;

import factory.model.Conveyor;
import factory.model.Tool;

public class FactoryMonitor {

    private Conveyor conveyor;
    private Map<Tool, Boolean> toolsInAction;

    public FactoryMonitor(Conveyor conveyor) {
        this.conveyor = conveyor;
        this.toolsInAction = new HashMap<>();
    }

    public synchronized void stopConveyor(Tool tool) throws InterruptedException {
        toolsInAction.put(tool, true); // Add the tool to active tools.
        conveyor.off();
        notifyAll(); // WE DONT KNOW WHY THIS ONE IS NEEDED BUT IF WE DONT HAVE IT THE PROGRAM FAILS
    }

    public synchronized void startConveyor(Tool tool) throws InterruptedException {
        toolsInAction.remove(tool); // When start remove active tool and update others waitning to start
        notifyAll();
        while (checkIfAnyToolIsStillActing()) { // If any tool is acting wait();
            wait();
        }
        conveyor.on();
    }

    private boolean checkIfAnyToolIsStillActing() { // Checks the map of active tools, if one tool is acting then return
                                                    // true
        for (Map.Entry<Tool, Boolean> entry : toolsInAction.entrySet()) {
            return true && entry.getValue();
        }
        return false;
    }

}
