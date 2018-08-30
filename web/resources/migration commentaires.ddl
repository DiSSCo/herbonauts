CREATE TABLE H_MESSAGE_TMP (
  id               NUMBER(19, 0)  NOT NULL,
  text             VARCHAR2(2000) NOT NULL,
  creation_date    TIMESTAMP      NOT NULL,
  last_update_date TIMESTAMP      NOT NULL,
  author_id        NUMBER(19, 0)  NOT NULL,
  discussion_id    NUMBER(19, 0)  NOT NULL,
  is_resolution    NUMBER(1, 0)   NOT NULL,
  is_first         NUMBER(1, 0)   NOT NULL,
  moderator_login  VARCHAR2(255),
  CONSTRAINT pk_h_message_tmp PRIMARY KEY (id)
);

CREATE TABLE H_DISCUSSION_TMP (
  id               NUMBER(19, 0) NOT NULL,
  title            VARCHAR2(50)  NOT NULL,
  creation_date    TIMESTAMP     NOT NULL,
  last_update_date TIMESTAMP     NOT NULL,
  is_resolved      NUMBER(1, 0)  NOT NULL,
  author_id        NUMBER(19, 0) NOT NULL,
  CONSTRAINT pk_h_discussion_tmp PRIMARY KEY (id)
);

CREATE TABLE H_TAGS_LINKS_TMP (
  tag_id           NUMBER(19, 0) NOT NULL,
  link_type        VARCHAR2(10)  NOT NULL,
  target_id        NUMBER(19, 0) NOT NULL,
  last_update_date TIMESTAMP     NOT NULL,
  CONSTRAINT pk_h_tag_links_tmp PRIMARY KEY (tag_id, link_type, target_id)
);

INSERT INTO H_DISCUSSION_TMP
  SELECT
    HIBERNATE_SEQUENCE.nextval,
    tmp.mission_id,
    tmp.creation_date,
    tmp.creation_date,
    1,
    tmp.user_id
  FROM (SELECT
          c.id,
          c.creation_date,
          c.mission_id,
          c.user_id,
          ROW_NUMBER()
          OVER (PARTITION BY c.mission_id
            ORDER BY c.creation_date ASC) AS rk
        FROM H_COMMENT c
        WHERE c.type = 'MISSION') tmp
  WHERE tmp.rk = 1;

INSERT INTO H_TAGS_LINKS_TMP
  SELECT
    t.id,
    'DISCUSSION',
    d.id,
    d.last_update_date
  FROM H_TAGS t, H_DISCUSSION_TMP d, H_MISSION m
  WHERE m.id || '' = d.title
        AND m.principal_tag = t.id;


INSERT INTO H_MESSAGE_TMP
  SELECT
    HIBERNATE_SEQUENCE.nextval,
    tmp.text,
    tmp.creation_date,
    tmp.creation_date,
    tmp.user_id,
    tmp.discussionId,
    0,
    0,
    NULL
  FROM (SELECT
          c.text,
          c.creation_date,
          c.user_id,
          d.id AS discussionId
        FROM H_COMMENT c, H_DISCUSSION_TMP d
        WHERE c.type = 'MISSION' AND d.title = c.mission_id || '' AND c.text IS NOT NULL
        ORDER BY c.CREATION_DATE) tmp;


UPDATE H_MESSAGE_TMP
SET is_first = 1
WHERE id IN (
  SELECT tmp.id
  FROM (SELECT
          m.id,
          ROW_NUMBER()
          OVER (PARTITION BY m.discussion_id
            ORDER BY m.creation_date ASC) AS rk
        FROM H_MESSAGE_TMP m) tmp
  WHERE tmp.rk = 1);

UPDATE H_DISCUSSION_TMP SET title = 'Anciens commentaires';
INSERT INTO H_DISCUSSION SELECT * FROM H_DISCUSSION_TMP;
INSERT INTO H_TAGS_LINKS SELECT * FROM H_TAGS_LINKS_TMP;
INSERT INTO H_MESSAGE SELECT * FROM H_MESSAGE_TMP;

TRUNCATE TABLE H_MESSAGE_TMP;

TRUNCATE TABLE H_DISCUSSION_TMP;

TRUNCATE TABLE H_TAGS_LINKS_TMP;

INSERT INTO H_DISCUSSION_TMP
  SELECT
    HIBERNATE_SEQUENCE.nextval,
    tmp.specimen_id,
    tmp.creation_date,
    tmp.creation_date,
    1,
    tmp.user_id
  FROM (SELECT
          c.id,
          c.creation_date,
          c.specimen_id,
          c.user_id,
          ROW_NUMBER()
          OVER (PARTITION BY c.specimen_id
            ORDER BY c.creation_date ASC) AS rk
        FROM H_COMMENT c
        WHERE c.type = 'SPECIMEN') tmp
  WHERE tmp.rk = 1;

INSERT INTO H_TAGS_LINKS_TMP
  SELECT
    t.id,
    'DISCUSSION',
    d.id,
    d.last_update_date
  FROM H_TAGS t, H_DISCUSSION_TMP d, H_SPECIMEN s
  WHERE s.id || '' = d.title
        AND s.code = t.label;


INSERT INTO H_MESSAGE_TMP
  SELECT
    HIBERNATE_SEQUENCE.nextval,
    tmp.text,
    tmp.creation_date,
    tmp.creation_date,
    tmp.user_id,
    tmp.discussionId,
    0,
    0,
    NULL
  FROM (SELECT
          c.text,
          c.creation_date,
          c.user_id,
          d.id AS discussionId
        FROM H_COMMENT c, H_DISCUSSION_TMP d
        WHERE c.type = 'SPECIMEN' AND d.title = c.specimen_id || '' AND c.text IS NOT NULL
        ORDER BY c.CREATION_DATE) tmp;


UPDATE H_MESSAGE_TMP
SET is_first = 1
WHERE id IN (
  SELECT tmp.id
  FROM (SELECT
          m.id,
          ROW_NUMBER()
          OVER (PARTITION BY m.discussion_id
            ORDER BY m.creation_date ASC) AS rk
        FROM H_MESSAGE_TMP m) tmp
  WHERE tmp.rk = 1);

UPDATE H_DISCUSSION_TMP SET title = 'Anciens commentaires';
INSERT INTO H_DISCUSSION SELECT * FROM H_DISCUSSION_TMP;
INSERT INTO H_TAGS_LINKS SELECT * FROM H_TAGS_LINKS_TMP;
INSERT INTO H_MESSAGE SELECT * FROM H_MESSAGE_TMP;

DROP TABLE H_MESSAGE_TMP;

DROP TABLE H_DISCUSSION_TMP;

DROP TABLE H_TAGS_LINKS_TMP;