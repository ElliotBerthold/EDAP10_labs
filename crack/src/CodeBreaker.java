import java.math.BigInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import client.view.ProgressItem;
import client.view.StatusWindow;
import client.view.WorklistItem;
import network.Sniffer;
import network.SnifferCallback;
import rsa.Factorizer;
import rsa.ProgressTracker;

public class CodeBreaker implements SnifferCallback {

    private final JPanel workList;
    private final JPanel progressList;

    private final JProgressBar mainProgressBar;

    private ExecutorService pool;

    // -----------------------------------------------------------------------

    private CodeBreaker() {
        StatusWindow w = new StatusWindow();
        this.pool = Executors.newFixedThreadPool(2);

        workList = w.getWorkList();
        progressList = w.getProgressList();
        mainProgressBar = w.getProgressBar();
        w.enableErrorChecks();
    }

    // -----------------------------------------------------------------------

    public static void main(String[] args) {

        /*
         * Most Swing operations (such as creating view elements) must be performed in
         * the Swing EDT (Event Dispatch Thread).
         * 
         * That's what SwingUtilities.invokeLater is for.
         */

        SwingUtilities.invokeLater(() -> {
            CodeBreaker codeBreaker = new CodeBreaker();
            new Sniffer(codeBreaker).start();
        });
    }

    // -----------------------------------------------------------------------

    /** Called by a Sniffer thread when an encrypted message is obtained. */
    @Override
    public void onMessageIntercepted(String message, BigInteger n) {
        System.out.println("message intercepted (N=" + n + ")...");

        SwingUtilities.invokeLater(() -> {
            createListItem(message, n);
        });
    }

    private Runnable crackTask(JButton removeButton, JButton cancelButton, ProgressItem item, BigInteger n)
            throws InterruptedException {
        return () -> {
            ProgressTracker tracker = new Tracker(item.getProgressBar(), mainProgressBar);
            try {
                SwingUtilities.invokeLater(() -> {
                    cancelButton.setVisible(true);
                });
                String plainText = Factorizer.crack(item.getTextArea().getText(), n, tracker);
                SwingUtilities.invokeLater(() -> {
                    cancelButton.setVisible(false);
                    item.getTextArea().setText(plainText);
                    removeButton.setVisible(true);
                });

            } catch (InterruptedException t) {
                t.printStackTrace();
            }

        };
    }

    private void createListItem(String message, BigInteger n) {
        WorklistItem intercepted = new WorklistItem(n, message);
        JButton button = new JButton("Break");

        JPanel taskWithButton = new JPanel();
        taskWithButton.setLayout(new BoxLayout(taskWithButton, BoxLayout.X_AXIS));

        taskWithButton.add(intercepted);
        button.addActionListener(e -> {
            createProgressItem(message, n, taskWithButton);
        });
        taskWithButton.add(button);
        workList.add(taskWithButton);

    }

    private void createProgressItem(String message, BigInteger n, JPanel taskWithButton) {
        ProgressItem progressItem = new ProgressItem(n, message);
        JButton removeButton = new JButton("Remove");
        JButton cancelButton = new JButton("cancel");

        removeButton.setVisible(false);
        cancelButton.setVisible(false);

        JPanel progressWithButton = new JPanel();
        progressWithButton.setLayout(new BoxLayout(progressWithButton, BoxLayout.X_AXIS));
        progressWithButton.add(progressItem);
        progressWithButton.add(cancelButton);
        progressWithButton.add(removeButton);
        progressList.add(progressWithButton);
        workList.remove(taskWithButton);
        removeButton.addActionListener(e -> {
            mainProgressBar.setValue(mainProgressBar.getValue() - 1000000);
            mainProgressBar.setMaximum(mainProgressBar.getMaximum() - 1000000);
            progressList.remove(progressWithButton);
        });

        try {
            mainProgressBar.setMaximum(mainProgressBar.getMaximum() + 1000000);
            Future<?> task = pool.submit(crackTask(removeButton, cancelButton, progressItem, n));
            cancelButton.addActionListener(e2 -> {
                task.cancel(true);
                int currentProgress = progressItem.getProgressBar().getValue();
                SwingUtilities.invokeLater(() -> {
                    progressItem.getTextArea().setText("[CANCELLED]");
                    progressItem.getProgressBar().setValue(1000000);
                    cancelButton.setVisible(false);
                    removeButton.setVisible(true);
                });
                mainProgressBar.setValue(mainProgressBar.getValue() + (1000000 - currentProgress));

            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
