package io.graversen.minecraft.rcon.service;

import io.graversen.minecraft.rcon.MinecraftClient;
import io.graversen.minecraft.rcon.RconConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

class ConnectTask implements Callable<MinecraftClient> {
    private static Logger LOG = LoggerFactory.getLogger(ConnectTask.class);
    private final ConnectOptions connectOptions;
    private final RconDetails rconDetails;

    ConnectTask(ConnectOptions connectOptions, RconDetails rconDetails) {
        this.connectOptions = connectOptions;
        this.rconDetails = rconDetails;
    }

    @Override
    public MinecraftClient call() throws Exception {
        int currentAttempt = 1;

        while (currentAttempt <= connectOptions.getMaxRetries()) {
            LOG.debug("Connection attempt {}", currentAttempt);
            currentAttempt++;

            try {
                return MinecraftClient.connect(rconDetails.getHostname(), rconDetails.getPassword(), rconDetails.getPort());
            } catch (Exception e) {
                LOG.debug("Connection attempt failed due to: {}", e.getMessage());

                if (currentAttempt < connectOptions.getMaxRetries() + 1) {
                    sleep();
                }
            }
        }

        throw new RconConnectException("Unable to connect to Minecraft server after %d retries", currentAttempt - 1);
    }

    private void sleep() {
        try {
            Thread.sleep(connectOptions.getTimeBetweenRetries().toMillis());
        } catch (InterruptedException e) {
            // Nothing
        }
    }
}