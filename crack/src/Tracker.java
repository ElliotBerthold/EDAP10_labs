import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import rsa.ProgressTracker;

public class Tracker implements ProgressTracker {
    private int totalProgress = 0;

    private JProgressBar progressBar;
    private JProgressBar mainProgressBar;

    public Tracker(JProgressBar progressBar, JProgressBar mainProgressBar) {
        this.progressBar = progressBar;
        this.mainProgressBar = mainProgressBar;
    }

    /**
     * Called by Factorizer to indicate progress. The total sum of
     * ppmDelta from all calls will add upp to 1000000 (one million).
     * 
     * @param ppmDelta portion of work done since last call,
     *                 measured in ppm (parts per million)
     */
    @Override
    public void onProgress(int ppmDelta) {
        int tureppmDelta = Math.min(ppmDelta, 1000000 - progressBar.getValue());
        totalProgress += tureppmDelta;
        SwingUtilities.invokeLater(() -> {
            incrementMainProgressBar(tureppmDelta);
            progressBar.setValue(totalProgress);
        });

        // System.out.println("progress = " + totalProgress + "/1000000");
    }

    private synchronized void incrementMainProgressBar(int ppmDelta) {
        mainProgressBar.setValue(mainProgressBar.getValue() + ppmDelta);
    }
}
