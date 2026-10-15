-- This SQL is for The story dialogue, scenes, and other things related to story.

USE adaptive_reading;

-- One prepared reading chapter (one difficulty variant).
CREATE TABLE IF NOT EXISTS chapters (
    id          CHAR(36)     PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    passage     TEXT         NOT NULL,
    difficulty  ENUM('EASY','MEDIUM','HARD') NOT NULL
    -- PLACEHOLDER: logical-story grouping so replays across difficulty variants
    -- cannot bypass scoring. Decide with Dev 2, then uncomment:
    -- , logical_chapter_id CHAR(36) NULL
);

-- One scene / dialogue line. node_id is a STABLE text id such as 'scene_001'
-- (saves store this id, so do not rename ids after release).
CREATE TABLE IF NOT EXISTS story_nodes (
    chapter_id        CHAR(36)     NOT NULL,
    node_id           VARCHAR(100) NOT NULL,
    scene_name        VARCHAR(100) NOT NULL,            -- label shown in save slots, e.g. 'Scene 1'
    is_start          TINYINT(1)   NOT NULL DEFAULT 0,  -- 1 = where the chapter begins
    speaker           VARCHAR(100) NULL,                -- NULL = narrator
    dialogue          TEXT         NOT NULL,
    background_image  VARCHAR(255) NULL,                -- file name inside assets/
    -- PLACEHOLDER: add more scene columns here when needed, for example:
    -- character_sprite VARCHAR(255) NULL,
    -- music            VARCHAR(255) NULL,
    -- node_type        ENUM('STORY','CHOICE','READING_CHECK','END') NOT NULL DEFAULT 'STORY',
    PRIMARY KEY (chapter_id, node_id),
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE
);

-- A player choice that leads from one node to another.
CREATE TABLE IF NOT EXISTS story_choices (
    chapter_id     CHAR(36)     NOT NULL,
    from_node_id   VARCHAR(100) NOT NULL,
    choice_order   INT          NOT NULL,               -- 1, 2, 3 ... order shown on screen
    choice_text    VARCHAR(300) NOT NULL,
    to_node_id     VARCHAR(100) NOT NULL,
    PRIMARY KEY (chapter_id, from_node_id, choice_order),
    FOREIGN KEY (chapter_id, from_node_id) REFERENCES story_nodes(chapter_id, node_id) ON DELETE CASCADE,
    FOREIGN KEY (chapter_id, to_node_id)   REFERENCES story_nodes(chapter_id, node_id) ON DELETE CASCADE
);