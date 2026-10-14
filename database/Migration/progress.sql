-- This SQL is for file saving and loading.
USE adaptive_reading;

CREATE TABLE IF NOT EXISTS save_slots (
    student_id     CHAR(36)     NOT NULL,
    slot           TINYINT      NOT NULL,
    chapter_id     CHAR(36)     NOT NULL,
    story_node_id  VARCHAR(100) NOT NULL,               -- stable node id from story_nodes
    difficulty     ENUM('EASY','MEDIUM','HARD') NOT NULL,
    saved_at       TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    PRIMARY KEY (student_id, slot),                     -- one row per student + slot
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,

    FOREIGN KEY (chapter_id, story_node_id) REFERENCES story_nodes(chapter_id, node_id),
    CONSTRAINT chk_save_slots_slot CHECK (slot BETWEEN 1 AND 5)
);