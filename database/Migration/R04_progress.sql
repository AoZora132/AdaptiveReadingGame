-- This SQL is for file saving and loading.

USE adaptive_reading;

CREATE TABLE IF NOT EXISTS save_slots (
    student_id     CHAR(36)     NOT NULL,
    slot           TINYINT      NOT NULL CHECK (slot BETWEEN 1 AND 5),  -- 5 save slots
    chapter_id     CHAR(36)     NOT NULL,
    story_node_id  VARCHAR(100) NOT NULL,               -- stable node id from story_nodes
    difficulty     ENUM('EASY','MEDIUM','HARD') NOT NULL,
    saved_at       TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    -- PLACEHOLDER: extra state for durable resume, agreed with Dev 2. Examples:
    -- extra_state JSON NULL,
    -- settings    JSON NULL,
    PRIMARY KEY (student_id, slot),                     -- one row per student + slot
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    -- A save must point at a real story node. If Dev 2 wants saves before story
    -- content exists, replace this line with: FOREIGN KEY (chapter_id) REFERENCES chapters(id)
    FOREIGN KEY (chapter_id, story_node_id) REFERENCES story_nodes(chapter_id, node_id)
);