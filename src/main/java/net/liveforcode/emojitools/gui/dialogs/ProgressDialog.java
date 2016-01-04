package net.liveforcode.emojitools.gui.dialogs;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.liveforcode.emojitools.gui.dialogs.dialogcontrollers.ProgressDialogController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProgressDialog implements ProgressDialogController.ProgressDialogListener {

    private Stage stage;
    private ProgressDialogController controller;

    private List<CancelListener> cancelListenerList = new ArrayList<>();

    public ProgressDialog(String headerText) {
        this.stage = new Stage();
        stage.setOnCloseRequest(e -> {
            onCancelButtonFired();
            e.consume();
        });

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/GUI/Dialogs/ProgressDialog.fxml"));
            Parent root = loader.load();

            this.controller = loader.getController();
            controller.setProgressDialogListener(this);
            controller.setHeaderText(headerText);

            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addCancelListener(CancelListener listener) {
        if (!cancelListenerList.contains(listener))
            cancelListenerList.add(listener);
    }

    public void display() {
        stage.showAndWait();
    }

    @Override
    public void onCancelButtonFired() {
        cancelListenerList.forEach(CancelListener::onCancelButtonFired);
        stage.close();
    }

    public void close() {
        stage.close();
    }

    public void bindProgressToProperty(ReadOnlyDoubleProperty property) {
        controller.bindProgressToProperty(property);
    }

    public void bindMessagesToProperty(ReadOnlyStringProperty property) {
        controller.bindMessagesToProperty(property);
    }

    public interface CancelListener {

        void onCancelButtonFired();

    }
}
