package net.liveforcode.emojitools.gui.dialogs;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.liveforcode.emojitools.EmojiTools;
import net.liveforcode.emojitools.gui.dialogs.dialogcontrollers.OperationProgressDialogController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OperationProgressDialog {

    private Stage stage;
    private OperationProgressDialogController controller;

    private List<DialogCloseListener> closeListeners = new ArrayList<>();

    public OperationProgressDialog(String headerText) {
        this.stage = new Stage();
        stage.setTitle(headerText);
        stage.getIcons().add(EmojiTools.getLogoImage());
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);

        stage.setOnCloseRequest(e -> {
            cancel();
            e.consume();
        });

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/Dialogs/OperationProgressDialog.fxml"));
            Parent root = loader.load();

            this.controller = loader.getController();
            controller.setParent(this);
            controller.setHeaderText(headerText);

            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addCloseListener(DialogCloseListener listener) {
        if (!closeListeners.contains(listener))
            closeListeners.add(listener);
    }

    public void display() {
        stage.showAndWait();
    }

    public void cancel() {
        closeListeners.forEach(DialogCloseListener::onDialogClosing);
        close();
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

    public interface DialogCloseListener {
        void onDialogClosing();
    }

}
