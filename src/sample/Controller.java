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

                        if(multiplier.isSelected()) counter *= 2;

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

        threads.add(backgroundThread);

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

                outputLabel.setText("Label");
                outputProgressbar.setProgress(0);

                threads.remove(backgroundThread);
            }
        });


        outputLabel.textProperty().bind(backgroundThread.messageProperty());
        outputProgressbar.progressProperty().bind(backgroundThread.progressProperty());

        backgroundThread.start();
    }


    @FXML
    public void StopThreads() {

        for (Service service : threads) {
            service.cancel();
        }

        boolean hasRunningThread = false;

        // Use iterator so entries can be removed
        Iterator<Service> serviceIterator = threads.iterator();
        while (serviceIterator.hasNext()) {
            Service ser = serviceIterator.next();
            hasRunningThread = ser.isRunning();
            if (!ser.isRunning()) serviceIterator.remove();
        }

        if (hasRunningThread) {
            System.out.println("A thread is still running. Try again.");
        } else {
            System.out.println("0 threads are running.");
        }
    }

    @FXML
    public void toggleMultiplier(){
        System.out.println("Restarting threads with multiplier");
        for(Service s:threads){
            s.restart();
        }
    }
}
