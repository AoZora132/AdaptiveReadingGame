package com.adaptivereadinggame.uiux;

import java.util.List;

/** Catalog for the visual-only FXML reference. It does not provide game behavior. */
public final class UiuxPrototypeScreens {
    public record Screen(String id, String title, String fxmlName) {
        @Override public String toString() { return id + " - " + title; }
    }
    public static final List<Screen> ALL = List.of(
        new Screen("01", "Welcome", "uiux-01-welcome.fxml"),
        new Screen("02", "Sign up", "uiux-02-sign-up.fxml"),
        new Screen("03", "Signup \u00b7 missing fields", "uiux-03-signup-missing-fields.fxml"),
        new Screen("04", "Signup \u00b7 duplicate name", "uiux-04-signup-duplicate-name.fxml"),
        new Screen("05", "Signup success \u2192 login", "uiux-05-signup-success-login.fxml"),
        new Screen("06", "Log in", "uiux-06-log-in.fxml"),
        new Screen("07", "Login \u00b7 invalid credentials", "uiux-07-login-invalid-credentials.fxml"),
        new Screen("08", "Main menu \u00b7 no progress", "uiux-08-main-menu-no-progress.fxml"),
        new Screen("09", "Main menu \u00b7 returning player", "uiux-09-main-menu-returning-player.fxml"),
        new Screen("10", "New game confirmation", "uiux-10-new-game-confirmation.fxml"),
        new Screen("11", "How to play", "uiux-11-how-to-play.fxml"),
        new Screen("12", "Introduction", "uiux-12-introduction.fxml"),
        new Screen("13", "Story \u00b7 dialogue", "uiux-13-story-dialogue.fxml"),
        new Screen("14", "Story \u00b7 thoughts", "uiux-14-story-thoughts.fxml"),
        new Screen("15", "Story \u00b7 narrative choice", "uiux-15-story-narrative-choice.fxml"),
        new Screen("16", "Story \u00b7 branch continuation", "uiux-16-story-branch-continuation.fxml"),
        new Screen("17", "Reading Check \u00b7 unanswered", "uiux-17-reading-check-unanswered.fxml"),
        new Screen("18", "Reading Check \u00b7 selected B", "uiux-18-reading-check-selected-b.fxml"),
        new Screen("19", "Reading Check \u00b7 selected A", "uiux-19-reading-check-selected-a.fxml"),
        new Screen("20", "Reading Check \u00b7 correct feedback", "uiux-20-reading-check-correct-feedback.fxml"),
        new Screen("21", "Reading Check \u00b7 incorrect feedback", "uiux-21-reading-check-incorrect-feedback.fxml"),
        new Screen("22", "Reading Check \u00b7 final question", "uiux-22-reading-check-final-question.fxml"),
        new Screen("22b", "Reading Check \u00b7 final feedback", "uiux-22b-reading-check-final-feedback.fxml"),
        new Screen("23", "Preparing the next challenge", "uiux-23-preparing-the-next-challenge.fxml"),
        new Screen("24", "Chapter results \u00b7 normal", "uiux-24-chapter-results-normal.fxml"),
        new Screen("25", "Chapter results \u00b7 model fallback", "uiux-25-chapter-results-model-fallback.fxml"),
        new Screen("26", "Game menu / pause", "uiux-26-game-menu-pause.fxml"),
        new Screen("27", "Save \u00b7 occupied and empty slots", "uiux-27-save-occupied-and-empty-slots.fxml"),
        new Screen("28", "Save \u00b7 success", "uiux-28-save-success.fxml"),
        new Screen("29", "Save \u00b7 overwrite confirmation", "uiux-29-save-overwrite-confirmation.fxml"),
        new Screen("30", "Load \u00b7 occupied and empty slots", "uiux-30-load-occupied-and-empty-slots.fxml"),
        new Screen("31", "Load \u00b7 unsaved progress warning", "uiux-31-load-unsaved-progress-warning.fxml"),
        new Screen("32", "Settings", "uiux-32-settings.fxml"),
        new Screen("33", "Return to main menu confirmation", "uiux-33-return-to-main-menu-confirmation.fxml"),
        new Screen("34", "Logout confirmation", "uiux-34-logout-confirmation.fxml"),
        new Screen("35", "Quit confirmation", "uiux-35-quit-confirmation.fxml"),
        new Screen("37", "Story complete", "uiux-37-story-complete.fxml"),
        new Screen("38", "Load \u00b7 unreadable save", "uiux-38-load-unreadable-save.fxml"),
        new Screen("39", "Save \u00b7 persistence failed", "uiux-39-save-persistence-failed.fxml"),
        new Screen("40", "Next chapter \u00b7 loading / completion", "uiux-40-next-chapter-loading-completion.fxml")
    );
    private UiuxPrototypeScreens() {}
}
