package com.adaptivereadinggame.uiux;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

/** Routes visible hotspot clicks within the visual-only UI/UX reference. */
public final class UiuxScreenController {
    @FXML
    private void navigate(ActionEvent event) {
        if (!(event.getSource() instanceof Button button)) return;
        String id = button.getId();
        if (id != null && id.startsWith("uiux-to-")) {
            UiuxPrototypeApp.navigateTo(id.substring("uiux-to-".length()));
        }
    }
}
