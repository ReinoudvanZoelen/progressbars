package sample;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.concurrent.locks.ReentrantLock;

public class Controller implements Initializable {

    //region UI Elements
    @FXML
    Label label1;
    @FXML
    Label label2;
    @FXML
    Label label3;
    @FXML
    CheckBox multiplier;

    @FXML
    ProgressBar progressBar1;
    @FXML
    ProgressBar progressBar2;
    @FXML
    ProgressBar progressBar3;

    @FXML
    Button cancelButton;
    //endregion

    ArrayList<Service> threads = new ArrayList<Service>();
    ReentrantLock threadLock = new ReentrantLock();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Starting program...");
    }

    @FXML
    public void StartCalculation1() {
        startCalculation(label1, progressBar1, 5);
    }

    @FXML
    public void StartCalculation2() {
        startCalculation(label2, progressBar2, 10);
    }

    @FXML
    public void StartCalculation3() {
        startCalculation(label3, progressBar3, 50);
    }


    private void startCalculation(Label outputLabel, ProgressBar outputProgressbar, int countTo) {
        Service<Void> backgroundThread = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {

                    @Override
                    protected Void call() throws Exception {

                        int counter = countTo;

                        if (multiplier.isSelected()) counter *= 2;

                        for (int i = 0; i <= counter; i++) {
                            System.out.println("Progress: " + i);
                            updateMessage("Progress: " + i + " out of " + counter);
                            updateProgress(i, counter);
                            Thread.sleep(1000);
                        }

                        return null;
                    } // End call()
                }; // End new Task<Void>
            } // End createTask()
        }; // End new Service<Void>

        threadLock.lock();
        try {
            threads.add(backgroundThread);
        } finally {
            threadLock.unlock();
        }

        backgroundThread.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                outputLabel.textProperty().unbind();
                outputProgressbar.progressProperty().unbind();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                outputLabel.setText("Thread voltooid");
                outputProgressbar.setProgress(0);

                threadLock.lock();
                try {
                    threads.remove(backgroundThread);
                } finally {
                    threadLock.unlock();
                }
            }
        });

        backgroundThread.setOnCancelled(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                outputLabel.textProperty().unbind();
                outputProgressbar.progressProperty().unbind();

                outputLabel.setText("Thread gestopt");
                outputProgressbar.setProgress(0);

                threadLock.lock();
                try {
                    threads.remove(backgroundThread);
                } finally {
                    threadLock.unlock();
                }
            }
        });

        outputLabel.textProperty().bind(backgroundThread.messageProperty());
        outputProgressbar.progressProperty().bind(backgroundThread.progressProperty());

        backgroundThread.start();
    }


    @FXML
    public void StopThreads() {
        cancelAllThreads();

        boolean hasRunningThread = removeStoppedThreads();

        if (hasRunningThread) {
            System.out.println("A thread is still running. Try again.");
            StopThreads();
        } else {
            System.out.println("0 threads are running.");
        }
    }

    private synchronized void cancelAllThreads() {
        threadLock.lock();
        try {
            for (Service service : threads) {
                service.cancel();
            }
        } finally {
            threadLock.unlock();

        }
    }

    private boolean removeStoppedThreads() {
        boolean hasRunningThread = false;

        threadLock.lock();
        try {
            // Use iterator so entries can be removed while looping through
            Iterator<Service> serviceIterator = threads.iterator();
            while (serviceIterator.hasNext()) {
                Service ser = serviceIterator.next();
                hasRunningThread = ser.isRunning();
                if (!ser.isRunning()) serviceIterator.remove();
            }
        } finally {
            threadLock.unlock();
        }

        return hasRunningThread;
    }

    @FXML
    public void toggleMultiplier() {
        try {
            System.out.println("Restarting threads with multiplier");
            for (Service s : threads) {
                s.restart();
            }
        } finally {
            threadLock.unlock();
        }
    }
}
