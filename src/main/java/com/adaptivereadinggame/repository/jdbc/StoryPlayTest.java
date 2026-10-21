// Java file that is provided by Claude for testing with some edits.
package com.adaptivereadinggame.repository.jdbc;

import java.sql.*;
import java.util.*;

/**
 * Automatic test for all four database areas:
 * accounts, story content, assessment, and progress (save slots).
 * Everything it inserts is ROLLED BACK at the end, so it is safe to run again and again.
 * Needs the seed data (bash database/setup.sh) and MySQL 8.0.16+ (for CHECK rules).
 *
 * Run:  export DB_PASS='yourpassword'
 *       java -Dstdout.encoding=UTF-8 -cp "target/classes:$(cat /tmp/cp.txt)" com.adaptivereadinggame.repository.jdbc.DbFullCheck
 */
public class DbFullCheck {

    private static final String CHAPTER_ID  = "cccccccc-0000-0000-0000-000000000001";
    private static final String QUESTION_ID = "dddddddd-0000-0000-0000-000000000001";

    private static final String SAVE_SQL =
        "INSERT INTO save_slots (student_id, slot, chapter_id, story_node_id, difficulty) "
      + "VALUES (?, ?, ?, ?, 'EASY') "
      + "ON DUPLICATE KEY UPDATE story_node_id = VALUES(story_node_id), "
      + "saved_at = CURRENT_TIMESTAMP(3)";

    private static int failures = 0;

    public static void main(String[] args) {
        String url  = env("DB_URL", "jdbc:mysql://localhost:3306/adaptive_reading");
        String user = env("DB_USER", "argdev");
        String pass = System.getenv("DB_PASS");
        if (pass == null) {
            System.out.println("Set DB_PASS first:  export DB_PASS='yourpassword'");
            return;
        }

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("Connected to MySQL.");
            conn.setAutoCommit(false);   // everything below is undone by rollback()
            try {
                if (queryInt(conn, "SELECT COUNT(*) FROM chapters WHERE id = ?", CHAPTER_ID) == 0) {
                    System.out.println("Seed data missing. Run:  bash database/setup.sh --reset");
                    return;
                }
                String studentId = testAccounts(conn);
                testStoryContent(conn);
                testAssessment(conn, studentId);
                testProgress(conn, studentId);
            } finally {
                conn.rollback();
            }
        } catch (SQLException e) {
            failures++;
            System.out.println("Database error: " + e.getMessage());
        }
        System.out.println(failures == 0
                ? "\nALL CHECKS PASSED"
                : "\n" + failures + " CHECK(S) FAILED");
    }

    // ---------- 1. ACCOUNTS ----------
    private static String testAccounts(Connection conn) throws SQLException {
        System.out.println("\n[1] Accounts");
        String id = UUID.randomUUID().toString();
        String username = "test_" + id.substring(0, 8);

        exec(conn, "INSERT INTO accounts (id, username, display_name) VALUES (?, ?, ?)",
                id, username, "Test User");
        exec(conn, "INSERT INTO credentials (account_id, encoded_verifier) VALUES (?, ?)",
                id, "PLACEHOLDER_NOT_A_REAL_HASH");
        exec(conn, "INSERT INTO students (id, display_name) VALUES (?, ?)", id, "Test User");

        check("account can be found by username",
                id.equals(queryString(conn, "SELECT id FROM accounts WHERE username = ?", username)));
        check("credential row exists for the account",
                queryInt(conn, "SELECT COUNT(*) FROM credentials WHERE account_id = ?", id) == 1);
        check("student profile exists for the account",
                queryInt(conn, "SELECT COUNT(*) FROM students WHERE id = ?", id) == 1);
        check("duplicate username is rejected",
                fails(conn, "INSERT INTO accounts (id, username, display_name) VALUES (?, ?, ?)",
                        UUID.randomUUID().toString(), username, "Copycat"));
        check("student without an account is rejected",
                fails(conn, "INSERT INTO students (id, display_name) VALUES (?, ?)",
                        UUID.randomUUID().toString(), "Ghost"));
        return id;
    }

    // ---------- 2. STORY CONTENT ----------
    // These checks look at the SHAPE of the story, not its words, so they keep
    // working when you replace placeholder text with the real story.
    private static void testStoryContent(Connection conn) throws SQLException {
        System.out.println("\n[2] Story content");
        List<String> nodes = queryList(conn,
                "SELECT node_id FROM story_nodes WHERE chapter_id = ? ORDER BY node_id", CHAPTER_ID);
        check("chapter has scenes (" + nodes.size() + " found)", nodes.size() > 0);
        check("chapter has exactly one start scene",
                queryInt(conn, "SELECT COUNT(*) FROM story_nodes WHERE chapter_id = ? AND is_start = 1",
                        CHAPTER_ID) == 1);

        List<String> badIds = new ArrayList<>();
        for (String id : nodes) {
            if (!id.matches("\\d{4}-\\d{4}-\\d{4}")) badIds.add(id);
        }
        checkEmpty("every scene id looks like 0001-0002-0003", badIds);

        checkEmpty("every scene has at least one line", queryList(conn,
                "SELECT n.node_id FROM story_nodes n WHERE n.chapter_id = ? AND NOT EXISTS "
              + "(SELECT 1 FROM story_lines l WHERE l.chapter_id = n.chapter_id AND l.node_id = n.node_id)",
                CHAPTER_ID));
        checkEmpty("lines are numbered 1, 2, 3 ... with no gaps", queryList(conn,
                "SELECT node_id FROM story_lines WHERE chapter_id = ? GROUP BY node_id "
              + "HAVING MIN(line_no) <> 1 OR MAX(line_no) <> COUNT(*)", CHAPTER_ID));
        checkEmpty("a scene has either ONE automatic Next or choice buttons, never both", queryList(conn,
                "SELECT from_node_id FROM story_choices WHERE chapter_id = ? GROUP BY from_node_id "
              + "HAVING SUM(choice_text IS NULL) > 0 AND COUNT(*) > 1", CHAPTER_ID));
        checkEmpty("every scene can be reached from the start scene", unreachableNodes(conn));

        String first = nodes.isEmpty() ? "none" : nodes.get(0);
        check("a link to a scene that does not exist is rejected",
                fails(conn, "INSERT INTO story_choices "
                          + "(chapter_id, from_node_id, choice_order, choice_style, choice_text, to_node_id) "
                          + "VALUES (?, ?, 98, 'SPEECH', 'Bad link', 'does_not_exist')", CHAPTER_ID, first));
        check("a line for a scene that does not exist is rejected",
                fails(conn, "INSERT INTO story_lines "
                          + "(chapter_id, node_id, line_no, line_type, dialogue) "
                          + "VALUES (?, 'does_not_exist', 1, 'SPEECH', 'x')", CHAPTER_ID));
        check("a choice with text but no style is rejected (needs MySQL 8.0.16+)",
                fails(conn, "INSERT INTO story_choices "
                          + "(chapter_id, from_node_id, choice_order, choice_style, choice_text, to_node_id) "
                          + "VALUES (?, ?, 99, NULL, 'Half filled', ?)", CHAPTER_ID, first, first));
    }

    // Walks every link starting at the start scene and returns the scenes it never reached.
    private static List<String> unreachableNodes(Connection conn) throws SQLException {
        Map<String, List<String>> next = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT from_node_id, to_node_id FROM story_choices WHERE chapter_id = ?")) {
            ps.setString(1, CHAPTER_ID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    next.computeIfAbsent(rs.getString(1), k -> new ArrayList<>()).add(rs.getString(2));
                }
            }
        }
        String start = queryString(conn,
                "SELECT node_id FROM story_nodes WHERE chapter_id = ? AND is_start = 1", CHAPTER_ID);
        Set<String> seen = new HashSet<>();
        Deque<String> todo = new ArrayDeque<>();
        if (start != null) { seen.add(start); todo.add(start); }
        while (!todo.isEmpty()) {
            String node = todo.poll();
            for (String target : next.getOrDefault(node, List.of())) {
                if (seen.add(target)) todo.add(target);
            }
        }
        List<String> missing = queryList(conn,
                "SELECT node_id FROM story_nodes WHERE chapter_id = ? ORDER BY node_id", CHAPTER_ID);
        missing.removeAll(seen);
        return missing;
    }

    // ---------- 3. ASSESSMENT ----------
    private static void testAssessment(Connection conn, String studentId) throws SQLException {
        System.out.println("\n[3] Assessment");

        // First-answer draft: saved once, before feedback.
        String session = UUID.randomUUID().toString();
        exec(conn, "INSERT INTO first_answer_drafts "
                 + "(session_id, question_id, student_id, selected_choice_index) VALUES (?, ?, ?, 1)",
                session, QUESTION_ID, studentId);
        check("first answer draft is saved",
                queryInt(conn, "SELECT selected_choice_index FROM first_answer_drafts "
                             + "WHERE session_id = ? AND question_id = ?", session, QUESTION_ID) == 1);
        check("a second answer for the same question is rejected",
                fails(conn, "INSERT INTO first_answer_drafts "
                          + "(session_id, question_id, student_id, selected_choice_index) VALUES (?, ?, ?, 2)",
                        session, QUESTION_ID, studentId));
        check("an answer with a non-existent choice is rejected",
                fails(conn, "INSERT INTO first_answer_drafts "
                          + "(session_id, question_id, student_id, selected_choice_index) VALUES (?, ?, ?, 9)",
                        UUID.randomUUID().toString(), QUESTION_ID, studentId));

        // Completed assessments. Two extra test chapters are created (and rolled back later).
        String chapter2 = newTestChapter(conn);
        String chapter3 = newTestChapter(conn);
        String attempt1 = UUID.randomUUID().toString();
        String attempt2 = UUID.randomUUID().toString();
        String insert = "INSERT INTO assessments (id, student_id, chapter_id, correct_answers, "
                      + "total_questions, assessed_difficulty, next_difficulty, completed_at) "
                      + "VALUES (?, ?, ?, ?, ?, 'EASY', 'MEDIUM', ?)";

        // Saved newest first on purpose, to prove history comes back oldest first.
        exec(conn, insert, attempt1, studentId, CHAPTER_ID, 1, 1, "2026-01-01 11:00:00.000");
        exec(conn, insert, attempt2, studentId, chapter2,  3, 5, "2026-01-01 10:00:00.000");

        check("completed assessment is saved",
                queryInt(conn, "SELECT correct_answers FROM assessments WHERE id = ?", attempt2) == 3);
        check("history returns this student's results oldest first",
                attempt2.equals(queryString(conn,
                    "SELECT id FROM assessments WHERE student_id = ? ORDER BY completed_at, id LIMIT 1",
                    studentId)));
        check("same student + chapter cannot be saved twice (insert-once)",
                fails(conn, insert, UUID.randomUUID().toString(), studentId, CHAPTER_ID, 0, 1,
                        "2026-01-02 10:00:00.000"));
        check("original result is still there after the duplicate was rejected",
                queryInt(conn, "SELECT correct_answers FROM assessments WHERE student_id = ? AND chapter_id = ?",
                        studentId, CHAPTER_ID) == 1);
        check("more correct answers than questions is rejected (needs MySQL 8.0.16+)",
                fails(conn, insert, UUID.randomUUID().toString(), studentId, chapter3, 6, 5,
                        "2026-01-03 10:00:00.000"));
    }

    // ---------- 4. PROGRESS (SAVE SLOTS 1-5) ----------
    private static void testProgress(Connection conn, String studentId) throws SQLException {
        System.out.println("\n[4] Progress (save slots)");

        List<String> nodes = queryList(conn,
                "SELECT node_id FROM story_nodes WHERE chapter_id = ? ORDER BY node_id", CHAPTER_ID);
        if (nodes.size() < 3) {
            check("needs at least 3 scenes in the story to test saving", false);
            return;
        }
        String a = nodes.get(0), b = nodes.get(1), c = nodes.get(2);

        exec(conn, SAVE_SQL, studentId, 1, CHAPTER_ID, a);
        check("slot 1 can be saved and loaded",
                a.equals(queryString(conn,
                    "SELECT story_node_id FROM save_slots WHERE student_id = ? AND slot = 1", studentId)));

        exec(conn, SAVE_SQL, studentId, 1, CHAPTER_ID, c);
        check("saving to slot 1 again overwrites it",
                c.equals(queryString(conn,
                    "SELECT story_node_id FROM save_slots WHERE student_id = ? AND slot = 1", studentId)));

        check("an empty slot loads nothing",
                queryString(conn, "SELECT story_node_id FROM save_slots WHERE student_id = ? AND slot = 2",
                        studentId) == null);

        exec(conn, SAVE_SQL, studentId, 5, CHAPTER_ID, b);
        check("slot 5 works",
                b.equals(queryString(conn,
                    "SELECT story_node_id FROM save_slots WHERE student_id = ? AND slot = 5", studentId)));
        check("the slot list shows the scene name of a save",
                queryString(conn,
                    "SELECT n.scene_name FROM save_slots sv JOIN story_nodes n "
                  + "ON n.chapter_id = sv.chapter_id AND n.node_id = sv.story_node_id "
                  + "WHERE sv.student_id = ? AND sv.slot = 5", studentId) != null);
        check("saving to slot 1 and 5 leaves exactly 2 slots used",
                queryInt(conn, "SELECT COUNT(*) FROM save_slots WHERE student_id = ?", studentId) == 2);
        check("slot 6 is rejected (needs MySQL 8.0.16+)",
                fails(conn, SAVE_SQL, studentId, 6, CHAPTER_ID, a));
        check("saving a scene that does not exist is rejected",
                fails(conn, SAVE_SQL, studentId, 3, CHAPTER_ID, "no_such_scene"));
    }

    // ---------- SMALL HELPERS ----------

    private static String newTestChapter(Connection conn) throws SQLException {
        String id = UUID.randomUUID().toString();
        exec(conn, "INSERT INTO chapters (id, title, passage, difficulty) VALUES (?, 'TEST', 'TEST', 'MEDIUM')", id);
        return id;
    }

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "  PASS  " : "  FAIL  ") + name);
        if (!ok) failures++;
    }

    // PASS when the list is empty; otherwise FAIL and print what was found.
    private static void checkEmpty(String name, List<String> problems) {
        check(name + (problems.isEmpty() ? "" : "  -> " + problems), problems.isEmpty());
    }

    private static List<String> queryList(Connection conn, String sql, Object... params)
            throws SQLException {
        List<String> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getString(1));
            }
        }
        return list;
    }

    private static void exec(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            ps.executeUpdate();
        }
    }

    // Returns true when the database REFUSES the statement (which is what we want to see).
    private static boolean fails(Connection conn, String sql, Object... params) {
        try {
            exec(conn, sql, params);
            return false;
        } catch (SQLException e) {
            return true;
        }
    }

    private static String queryString(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    private static int queryInt(Connection conn, String sql, Object... params) throws SQLException {
        String v = queryString(conn, sql, params);
        return v == null ? -1 : Integer.parseInt(v);
    }

    private static String env(String key, String fallback) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? fallback : v;
    }
}
