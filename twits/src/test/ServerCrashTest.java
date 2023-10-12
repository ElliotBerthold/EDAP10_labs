package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import msg.client.Twit;
import msg.client.test.MessagingLog;
import msg.client.test.ServerControl;

public class ServerCrashTest {
    @BeforeEach
    void setUp(TestInfo info) throws Exception {
        ServerControl.restartServer(info.getDisplayName());
    }

    @Test
    void test50Twits() throws InterruptedException {
        final int NBR_TWITS = 50;
        final int NBR_MESSAGES  = 5000;     // number of messages from each client
        final int MESSAGE_DELAY = 0;   // delay should be zero ms

        for (int i = 0; i < NBR_TWITS; i++) {
            new Twit(NBR_MESSAGES, MESSAGE_DELAY).start();
        }
        MessagingLog.expect(NBR_TWITS, NBR_TWITS * NBR_MESSAGES);
    }
}
