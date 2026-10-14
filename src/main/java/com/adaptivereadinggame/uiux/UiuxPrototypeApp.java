package com.adaptivereadinggame.uiux;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/** Standalone JavaFX viewer for the 40 visual-only FXML reference screens. */
public final class UiuxPrototypeApp extends Application {
    private static final String VIEW_ROOT = "/com/adaptivereadinggame/view/";
    private static final String CSS = "/com/adaptivereadinggame/style/uiux-prototype.css";
    private static UiuxPrototypeApp running;

    private Stage stage;
    private BorderPane root;
    private ScrollPane preview;
    private ComboBox<UiuxPrototypeScreens.Screen> screenPicker;
    private Label status;
    private int selectedIndex;

    @Override
    public void start(Stage stage) throws Exception {
        running = this;
        this.stage = stage;

        root = new BorderPane();
        root.setTop(buildToolbar());
        preview = new ScrollPane();
        preview.setFitToHeight(false);
        preview.setFitToWidth(false);
        preview.setPannable(true);
        root.setCenter(preview);

        Scene scene = new Scene(root, 1380, 860);
        URL stylesheet = getClass().getResource(CSS);
        if (stylesheet != null) scene.getStylesheets().add(stylesheet.toExternalForm());
        stage.setTitle("Adaptive Reading Game - JavaFX UI/UX Prototype");
        stage.setMinWidth(900);
        stage.setMinHeight(650);
        stage.setScene(scene);

        if (Boolean.getBoolean("arg.uiux.verify")) {
            verifyScreens();
            Platform.exit();
            return;
        }
        showScreen(0);
        stage.show();
    }

    private VBox buildToolbar() {
        Label title = new Label("Adaptive Reading Game - UI/UX screen preview");
        title.getStyleClass().add("prototype-title");

        Label disclaimer = new Label("Visual-only reference: sample screens and routes; no real login, scoring, saves, model, or database actions.");
        disclaimer.getStyleClass().add("prototype-notice");
        disclaimer.setWrapText(true);

        screenPicker = new ComboBox<>();
        screenPicker.getItems().setAll(UiuxPrototypeScreens.ALL);
        screenPicker.setPrefWidth(390);
        screenPicker.setAccessibleText("Choose a UI/UX screen state");
        screenPicker.setOnAction(event -> {
            if (screenPicker.getValue() != null) {
                int index = UiuxPrototypeScreens.ALL.indexOf(screenPicker.getValue());
                if (index >= 0 && index != selectedIndex) showScreen(index);
            }
        });

        Button previous = new Button("Previous");
        previous.setOnAction(event -> showScreen(Math.max(0, selectedIndex - 1)));
        Button next = new Button("Next");
        next.setOnAction(event -> showScreen(Math.min(UiuxPrototypeScreens.ALL.size() - 1, selectedIndex + 1)));
        status = new Label();
        HBox.setHgrow(status, Priority.ALWAYS);
        HBox navigation = new HBox(10, previous, next, screenPicker, status);
        navigation.setPadding(new Insets(8, 18, 8, 18));
        navigation.getStyleClass().add("prototype-toolbar");

        VBox header = new VBox(6, title, disclaimer, navigation);
        header.setPadding(new Insets(14, 18, 0, 18));
        header.getStyleClass().add("prototype-header");
        return header;
    }

    private void showScreen(int index) {
        if (index < 0 || index >= UiuxPrototypeScreens.ALL.size()) return;
        UiuxPrototypeScreens.Screen screen = UiuxPrototypeScreens.ALL.get(index);
        URL resource = getClass().getResource(VIEW_ROOT + screen.fxmlName());
        if (resource == null) {
            showError("Missing FXML", "Could not find " + screen.fxmlName());
            return;
        }
        try {
            Parent screenRoot = FXMLLoader.load(resource);
            preview.setContent(screenRoot);
            selectedIndex = index;
            if (screenPicker.getValue() != screen) screenPicker.setValue(screen);
            status.setText("Screen " + (index + 1) + " of " + UiuxPrototypeScreens.ALL.size()
                    + "  |  " + screen.id() + " - " + screen.title());
        } catch (IOException | RuntimeException failure) {
            showError("Could not load screen", screen.fxmlName() + "\n" + failure.getMessage());
        }
    }

    private void verifyScreens() {
        try {
            for (UiuxPrototypeScreens.Screen screen : UiuxPrototypeScreens.ALL) {
                URL resource = getClass().getResource(VIEW_ROOT + screen.fxmlName());
                if (resource == null) throw new IllegalStateException("Missing resource: " + screen.fxmlName());
                FXMLLoader.load(resource);
            }
            System.out.println("Loaded all " + UiuxPrototypeScreens.ALL.size() + " UI/UX FXML screens.");
        } catch (IOException failure) {
            throw new IllegalStateException("UI/UX FXML smoke check failed", failure);
        }
    }

    static void navigateTo(String id) {
        if (running == null) return;
        if ("36".equals(id)) {
            running.status.setText("Screen 36 is the design's application-closed endpoint; this preview remains open.");
            return;
        }
        for (int i = 0; i < UiuxPrototypeScreens.ALL.size(); i++) {
            if (UiuxPrototypeScreens.ALL.get(i).id().equals(id)) {
                running.showScreen(i);
                return;
            }
        }
        running.showError("Unknown screen route", "No FXML screen exists for route " + id);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
