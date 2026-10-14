-- This SQL is for storing the player's answer for the reading comprehension.
USE adaptive_reading;
 
CREATE TABLE IF NOT EXISTS questions (
    id                    CHAR(36) PRIMARY KEY,
    chapter_id            CHAR(36) NOT NULL,
    question_order        INT      NOT NULL,            -- order inside the chapter
    prompt                TEXT     NOT NULL,
    correct_choice_index  INT      NOT NULL CHECK (correct_choice_index >= 0),
    UNIQUE (chapter_id, question_order),
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE
    -- NOTE: MySQL cannot check that correct_choice_index exists in question_choices.
    -- The service or an integration test must check it.
);
 
CREATE TABLE IF NOT EXISTS question_choices (
    question_id   CHAR(36)     NOT NULL,
    choice_index  INT          NOT NULL,                  -- 0, 1, 2 ... order shown on screen
    choice_text   VARCHAR(500) NOT NULL,
    PRIMARY KEY (question_id, choice_index),
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);
 
-- The student's FIRST selected answer, saved once BEFORE feedback is shown.
-- PLACEHOLDER: confirm columns with Dev 2 (assessment session design).
CREATE TABLE IF NOT EXISTS first_answer_drafts (
    session_id            CHAR(36)     NOT NULL,        -- one assessment session
    question_id           CHAR(36)     NOT NULL,
    student_id            CHAR(36)     NOT NULL,
    selected_choice_index INT          NOT NULL,
    answered_at           TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (session_id, question_id),              -- a second answer cannot replace the first
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id, selected_choice_index)
        REFERENCES question_choices(question_id, choice_index) ON DELETE CASCADE
);
 
-- A completed chapter assessment. Insert-once per student + chapter.
CREATE TABLE IF NOT EXISTS assessments (
    id                   CHAR(36)     PRIMARY KEY,      -- the Attempt UUID
    student_id           CHAR(36)     NOT NULL,
    chapter_id           CHAR(36)     NOT NULL,
    correct_answers      INT          NOT NULL,
    total_questions      INT          NOT NULL,
    assessed_difficulty  ENUM('EASY','MEDIUM','HARD') NOT NULL,
    next_difficulty      ENUM('EASY','MEDIUM','HARD') NOT NULL,
    completed_at         TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UNIQUE (student_id, chapter_id),
    CHECK (total_questions BETWEEN 1 AND 100),
    CHECK (correct_answers >= 0 AND correct_answers <= total_questions),
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE
);
 