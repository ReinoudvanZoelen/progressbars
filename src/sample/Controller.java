package sample;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Stop;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    boolean task1running = false;
    boolean task2running = false;
    boolean task3running = false;

    ExecutorService pool = Executors.newFixedThreadPool(3);
    ReentrantLock threadLock = new ReentrantLock();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Starting program...");
    }

    @FXML
    public void StartCalculation1() {
        if (!task1running) {
            startCalculation(label1, progressBar1, 5);
            task1running = true;
        }
    }

    @FXML
    public void StartCalculation2() {
        if (!task2running) {
            startCalculation(label2, progressBar2, 10);
            task2running = true;
        }
    }

    @FXML
    public void StartCalculation3() {
        if (!task3running) {
            startCalculation(label3, progressBar3, 50);
            task3running = true;
        }
    }


    private void startCalculation(Label outputLabel, ProgressBar outputProgressbar, int countTo) {
        Task t = new CountToXThread(multiplier.isSelected(), countTo);

        t.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                unbindGui(outputLabel, outputProgressbar);
            }
        });

        t.setOnCancelled(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                unbindGui(outputLabel, outputProgressbar);
            }
        });

        outputLabel.textProperty().bind(t.messageProperty());
        outputProgressbar.progressProperty().bind(t.progressProperty());

        pool.execute(t);
    }

    @FXML
    public void StopThreads() {
        cancelAllThreads();

        unbindAllGui();
    }

    private void unbindAllGui() {
        unbindGui(label1, progressBar1);
        unbindGui(label2, progressBar2);
        unbindGui(label3, progressBar3);
    }

    private void unbindGui(Label label, ProgressBar progressBar) {
        if (label.getId().equals("label1")) {
            this.task1running = false;
        }
        if (label.getId().equals("label2")) {
            this.task2running = false;
        }
        if (label.getId().equals("label3")) {
            this.task3running = false;
        }

        label.textProperty().unbind();
        progressBar.progressProperty().unbind();

        label.setText("Thread gestopt");
        progressBar.setProgress(0);
    }

    private synchronized void cancelAllThreads() {
        List<Runnable> wereRunning = pool.shutdownNow();
        while (wereRunning.size() > 0) {
            wereRunning = pool.shutdownNow();
        }

        pool.shutdown();
        this.pool = Executors.newFixedThreadPool(3);
        System.out.println("All threads stopped. ");
    }

    @FXML
    public void toggleMultiplier() {
        cancelAllThreads();
        unbindAllGui();
    }
}
