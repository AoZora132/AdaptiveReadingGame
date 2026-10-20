//Test Java file created by Claude.

package com.adaptivereadinggame.repository.jdbc;

import java.sql.*;
import java.util.Scanner;

public class StoryPlayTest {

    private static final String STUDENT_ID = "aaaaaaaa-0000-0000-0000-000000000001";
    private static final String CHAPTER_ID = "cccccccc-0000-0000-0000-000000000001";
    private static final int MAX_SLOTS = 5;

    public static void main(String[] args) {
        String url  = env("DB_URL", "jdbc:mysql://localhost:3306/adaptive_reading");
        String user = env("DB_USER", "Dev3");
        String pass = System.getenv("DB_PASS");
        if (pass == null) {
            System.out.println("Set DB_PASS first:  export DB_PASS='yourpassword'");
            return;
        }

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Scanner sc = new Scanner(System.in)) {

            String difficulty = loadDifficulty(conn);
            String currentNode = findStartNode(conn);
            if (difficulty == null || currentNode == null) {
                System.out.println("No story found. Run:  bash database/setup.sh --reset");
                return;
            }

            boolean running = true;
            while (running) {
                showScene(conn, currentNode);
                System.out.println("1) Next   2) Save   3) Load   0) Quit");
                System.out.print("> ");
                if (!sc.hasNextLine()) break;
                String input = sc.nextLine().trim();

                switch (input) {
                    case "1": {
                        String next = findNextNode(conn, currentNode);
                        if (next == null) {
                            System.out.println("\n(This is the last scene. There is no next scene.)");
                        } else {
                            currentNode = next;
                        }
                        break;
                    }
                    case "2":
                        saveToSlot(conn, sc, currentNode, difficulty);
                        break;
                    case "3": {
                        String loaded = loadFromSlot(conn, sc);
                        if (loaded != null) currentNode = loaded;
                        break;
                    }
                    case "0":
                        running = false;
                        break;
                    default:
                        System.out.println("Please type 1, 2, 3, or 0.");
                }
            }
            System.out.println("Bye!");
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // ---------- SHOW A SCENE ----------

    private static void showScene(Connection conn, String nodeId) throws SQLException {
        String sql = "SELECT scene_name, speaker, dialogue, background_image "
                   + "FROM story_nodes WHERE chapter_id = ? AND node_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, CHAPTER_ID);
            ps.setString(2, nodeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("\n==============================");
                    System.out.println(rs.getString("scene_name")
                            + "   (background: " + rs.getString("background_image") + ")");
                    System.out.println("==============================");
                    String speaker = rs.getString("speaker");
                    System.out.println((speaker == null ? "Narrator" : speaker) + ":");
                    System.out.println(rs.getString("dialogue"));
                    System.out.println();
                }
            }
        }
    }

    private static String findStartNode(Connection conn) throws SQLException {
        return queryString(conn,
            "SELECT node_id FROM story_nodes WHERE chapter_id = ? AND is_start = 1 LIMIT 1",
            CHAPTER_ID);
    }

    // "Next" follows the first choice (choice_order 1) of the current scene.
    private static String findNextNode(Connection conn, String nodeId) throws SQLException {
        return queryString(conn,
            "SELECT to_node_id FROM story_choices "
          + "WHERE chapter_id = ? AND from_node_id = ? ORDER BY choice_order LIMIT 1",
            CHAPTER_ID, nodeId);
    }

    private static String loadDifficulty(Connection conn) throws SQLException {
        return queryString(conn, "SELECT difficulty FROM chapters WHERE id = ?", CHAPTER_ID);
    }

    // ---------- SAVE ----------

    private static void saveToSlot(Connection conn, Scanner sc, String nodeId, String difficulty)
            throws SQLException {
        String[] labels = printSlots(conn);
        System.out.print("Save to which slot (1-" + MAX_SLOTS + ", 0 = cancel)? ");
        int slot = readNumber(sc);
        if (slot < 1 || slot > MAX_SLOTS) {
            System.out.println("Save cancelled.");
            return;
        }
        if (labels[slot] != null) {
            System.out.print("Slot " + slot + " already has a save. Overwrite? (y/n) ");
            if (!sc.hasNextLine() || !sc.nextLine().trim().equalsIgnoreCase("y")) {
                System.out.println("Save cancelled.");
                return;
            }
        }

        // Insert a new row, or overwrite this student's row for this slot.
        String sql = "INSERT INTO save_slots "
                   + "(student_id, slot, chapter_id, story_node_id, difficulty) "
                   + "VALUES (?, ?, ?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE chapter_id = VALUES(chapter_id), "
                   + "story_node_id = VALUES(story_node_id), "
                   + "difficulty = VALUES(difficulty), saved_at = CURRENT_TIMESTAMP(3)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, STUDENT_ID);
            ps.setInt(2, slot);
            ps.setString(3, CHAPTER_ID);
            ps.setString(4, nodeId);
            ps.setString(5, difficulty);
            ps.executeUpdate();
        }
        System.out.println("Saved to slot " + slot + ".");
    }

    // ---------- LOAD ----------

    // Returns the saved scene id, or null if the load was cancelled or the slot is empty.
    private static String loadFromSlot(Connection conn, Scanner sc) throws SQLException {
        String[] labels = printSlots(conn);
        System.out.print("Load which slot (1-" + MAX_SLOTS + ", 0 = cancel)? ");
        int slot = readNumber(sc);
        if (slot < 1 || slot > MAX_SLOTS) {
            System.out.println("Load cancelled.");
            return null;
        }
        if (labels[slot] == null) {
            System.out.println("Slot " + slot + " is empty.");
            return null;
        }
        String nodeId = queryString(conn,
            "SELECT story_node_id FROM save_slots WHERE student_id = ? AND slot = ?",
            STUDENT_ID, slot);
        System.out.println("Loaded slot " + slot + ".");
        return nodeId;
    }

    // ---------- SHOW SLOTS 1-5 ----------

    // Prints every slot and returns an array: labels[slot] is null when the slot is empty.
    private static String[] printSlots(Connection conn) throws SQLException {
        String[] labels = new String[MAX_SLOTS + 1];
        String sql = "SELECT sv.slot, n.scene_name, sv.saved_at "
                   + "FROM save_slots sv "
                   + "JOIN story_nodes n ON n.chapter_id = sv.chapter_id "
                   + "                  AND n.node_id = sv.story_node_id "
                   + "WHERE sv.student_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, STUDENT_ID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int slot = rs.getInt("slot");
                    if (slot >= 1 && slot <= MAX_SLOTS) {
                        labels[slot] = rs.getString("scene_name")
                                + "   (saved " + rs.getTimestamp("saved_at") + ")";
                    }
                }
            }
        }
        System.out.println("\n--- Save slots ---");
        for (int i = 1; i <= MAX_SLOTS; i++) {
            System.out.println("Slot " + i + ": " + (labels[i] == null ? "(empty)" : labels[i]));
        }
        return labels;
    }

    // ---------- SMALL HELPERS ----------

    private static int readNumber(Scanner sc) {
        if (!sc.hasNextLine()) return -1;
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // Runs a SELECT and returns the first column of the first row, or null.
    private static String queryString(Connection conn, String sql, Object... params)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    private static String env(String key, String fallback) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? fallback : v;
    }
}