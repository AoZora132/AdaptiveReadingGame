-- =====================================================================
-- SEED: fake sample data for testing only. Safe to run many times
-- (INSERT IGNORE skips rows that already exist).
-- Run AFTER all migrations. Never put real student data here.
-- =====================================================================

USE adaptive_reading;

-- ---------- ACCOUNT (fake demo learner; no credential is seeded) ----------
INSERT IGNORE INTO accounts (id, username, display_name) VALUES
('aaaaaaaa-0000-0000-0000-000000000001', 'demo_student', 'Demo Student');

INSERT IGNORE INTO students (id, display_name) VALUES
('aaaaaaaa-0000-0000-0000-000000000001', 'Demo Student');

-- ---------- STORY CONTENT (PLACEHOLDER: replace the text with your real story) ----------
INSERT IGNORE INTO chapters (id, title, passage, difficulty) VALUES
('cccccccc-0000-0000-0000-000000000001',
 'PLACEHOLDER: Chapter 1 title',
 'PLACEHOLDER: Reading passage for chapter 1 goes here.',
 'EASY');

-- Insert ALL nodes first, because choices point to nodes.
INSERT IGNORE INTO story_nodes (chapter_id, node_id, is_start, speaker, dialogue, background_image) VALUES
('cccccccc-0000-0000-0000-000000000001', 'scene_001', 1, NULL,
 'PLACEHOLDER: Opening narration for scene 1.', 'placeholder_bg_1.png'),
('cccccccc-0000-0000-0000-000000000001', 'scene_002', 0, 'Character A',
 'PLACEHOLDER: Dialogue for scene 2 (first choice).', 'placeholder_bg_1.png'),
('cccccccc-0000-0000-0000-000000000001', 'scene_003', 0, 'Character B',
 'PLACEHOLDER: Dialogue for scene 3 (second choice).', 'placeholder_bg_2.png'),
('cccccccc-0000-0000-0000-000000000001', 'scene_end', 0, NULL,
 'PLACEHOLDER: Ending narration.', 'placeholder_bg_2.png');

INSERT IGNORE INTO story_choices (chapter_id, from_node_id, choice_order, choice_text, to_node_id) VALUES
('cccccccc-0000-0000-0000-000000000001', 'scene_001', 1, 'PLACEHOLDER: First choice text',  'scene_002'),
('cccccccc-0000-0000-0000-000000000001', 'scene_001', 2, 'PLACEHOLDER: Second choice text', 'scene_003'),
('cccccccc-0000-0000-0000-000000000001', 'scene_002', 1, 'PLACEHOLDER: Continue',           'scene_end'),
('cccccccc-0000-0000-0000-000000000001', 'scene_003', 1, 'PLACEHOLDER: Continue',           'scene_end');

-- ---------- ASSESSMENT (PLACEHOLDER question; choice 0 is the correct one) ----------
INSERT IGNORE INTO questions (id, chapter_id, question_order, prompt, correct_choice_index) VALUES
('dddddddd-0000-0000-0000-000000000001',
 'cccccccc-0000-0000-0000-000000000001', 1,
 'PLACEHOLDER: Comprehension question 1?', 0);

INSERT IGNORE INTO question_choices (question_id, choice_index, choice_text) VALUES
('dddddddd-0000-0000-0000-000000000001', 0, 'PLACEHOLDER: Correct answer'),
('dddddddd-0000-0000-0000-000000000001', 1, 'PLACEHOLDER: Wrong answer B'),
('dddddddd-0000-0000-0000-000000000001', 2, 'PLACEHOLDER: Wrong answer C');

-- ---------- PROGRESS ----------
-- Nothing seeded: save slots are created by the game (or by DbFullCheck.java).