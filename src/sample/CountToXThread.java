package sample;

import javafx.concurrent.Task;

public class CountToXThread extends Task<Void> {

    boolean multiply;
    int countTo;

    public CountToXThread(boolean multiply, int countTo) {
        this.multiply = multiply;
        this.countTo = countTo;
    }

    @Override
    protected Void call() throws Exception {
        if (this.multiply) countTo *= 2;

        for (int i = 0; i <= countTo; i++) {
            System.out.println("Progress: " + i);
            updateMessage("Progress: " + i + " out of " + countTo);
            updateProgress(i, countTo);
            Thread.sleep(1000);
        }

        return null;
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        System.out.println("Thread has been cancelled");
    }
}
