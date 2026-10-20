-- =====================================================================
-- SEED: fake sample data for testing only.
-- Run AFTER all migrations. Never put real student data here.
-- Changed this file? Rebuild cleanly with:  bash database/setup.sh --reset
-- (INSERT IGNORE skips rows that already exist, so edits to old rows are not applied.)
-- =====================================================================

USE adaptive_reading;

-- ---------- ACCOUNT (fake demo learner; no credential is seeded) ----------
INSERT IGNORE INTO accounts (id, username, display_name) VALUES
('aaaaaaaa-0000-0000-0000-000000000001', 'demo_student', 'Demo Student');

INSERT IGNORE INTO students (id, display_name) VALUES
('aaaaaaaa-0000-0000-0000-000000000001', 'Demo Student');

-- ---------- STORY CONTENT ----------
-- >>> REPLACE the lorem ipsum text below with your real story. <<<
INSERT IGNORE INTO chapters (id, title, passage, difficulty) VALUES
('cccccccc-0000-0000-0000-000000000001',
 'Lorem Ipsum Chapter 1',
 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. [CHAPTER 1 READING PASSAGE GOES HERE]',
 'EASY');

-- Insert ALL scenes first, because choices point to scenes.
-- node_id = stable id (saves store it). scene_name = label shown in save slots.
-- speaker NULL = narrator.
INSERT IGNORE INTO story_nodes
  (chapter_id, node_id, scene_name, is_start, speaker, dialogue, background_image) VALUES
('cccccccc-0000-0000-0000-000000000001', 'scene_001', 'Scene 1', 1, NULL,
 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. [SCENE 1 STORY TEXT GOES HERE]',
 'placeholder_bg_1.png'),
('cccccccc-0000-0000-0000-000000000001', 'scene_002', 'Scene 2', 0, 'Character A',
 'Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. [SCENE 2 DIALOGUE GOES HERE]',
 'placeholder_bg_1.png'),
('cccccccc-0000-0000-0000-000000000001', 'scene_003', 'Scene 3', 0, 'Character B',
 'Ut enim ad minim veniam, quis nostrud exercitation ullamco. [SCENE 3 DIALOGUE GOES HERE]',
 'placeholder_bg_2.png'),
('cccccccc-0000-0000-0000-000000000001', 'scene_004', 'Scene 4', 0, 'Character A',
 'Duis aute irure dolor in reprehenderit in voluptate velit esse. [SCENE 4 DIALOGUE GOES HERE]',
 'placeholder_bg_2.png'),
('cccccccc-0000-0000-0000-000000000001', 'scene_005', 'Scene 5', 0, NULL,
 'Excepteur sint occaecat cupidatat non proident, sunt in culpa. [SCENE 5 STORY TEXT GOES HERE]',
 'placeholder_bg_3.png');

-- The "Next" button follows choice_order 1. Scene 5 has no choice = end of chapter.
-- To make branching later, add more rows with choice_order 2, 3 ...
INSERT IGNORE INTO story_choices
  (chapter_id, from_node_id, choice_order, choice_text, to_node_id) VALUES
('cccccccc-0000-0000-0000-000000000001', 'scene_001', 1, 'Next', 'scene_002'),
('cccccccc-0000-0000-0000-000000000001', 'scene_002', 1, 'Next', 'scene_003'),
('cccccccc-0000-0000-0000-000000000001', 'scene_003', 1, 'Next', 'scene_004'),
('cccccccc-0000-0000-0000-000000000001', 'scene_004', 1, 'Next', 'scene_005');

-- ---------- ASSESSMENT (choice 0 is the correct one) ----------
INSERT IGNORE INTO questions (id, chapter_id, question_order, prompt, correct_choice_index) VALUES
('dddddddd-0000-0000-0000-000000000001',
 'cccccccc-0000-0000-0000-000000000001', 1,
 'Lorem ipsum dolor sit amet? [QUESTION 1 GOES HERE]', 0);

INSERT IGNORE INTO question_choices (question_id, choice_index, choice_text) VALUES
('dddddddd-0000-0000-0000-000000000001', 0, 'Lorem ipsum (correct answer goes here)'),
('dddddddd-0000-0000-0000-000000000001', 1, 'Dolor sit amet (wrong answer B)'),
('dddddddd-0000-0000-0000-000000000001', 2, 'Consectetur elit (wrong answer C)');

-- ---------- PROGRESS ----------
-- Nothing seeded: save slots are created by the game (or by the Java test files).