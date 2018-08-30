
    drop table H_TAGS_LINKS cascade constraints;

    drop table H_MESSAGE cascade constraints;

    drop table H_DISCUSSION cascade constraints;

    drop table H_DISCUSSION_CATEGORIES cascade constraints;

    drop table H_TAGS cascade constraints;

    drop table H_CATEGORIES cascade constraints;

    drop table H_TAGS_SUBSCRIPTION cascade constraints;

    DROP TABLE H_USER_PASSED_QUIZ;

    DROP TABLE H_MISSION_CART_QUERY_TERM;
    DROP TABLE H_MISSION_CART_QUERY_SEL_D;
    DROP TABLE H_MISSION_CART_QUERY_SEL;
    DROP TABLE H_MISSION_CART_QUERY;
    DROP TABLE H_REFERENCE_RECORD_INFO;
    DROP TABLE H_REFERENCE_RECORD;
    DROP TABLE H_REFERENCE;
    DROP VIEW VH_MISSION_VALUE_STAT;
    DROP VIEW VH_MISSION_C_USER_STAT;
    DROP VIEW VH_MISSION_CONTRIBUTION_STAT;
    DROP VIEW HV_STAT_CONTRIBUTION_QUESTION;
    DROP VIEW VH_TOP_CONTRIBUTIOR_STAT;
    DROP TABLE H_MISSION_DAILY_STAT;
    DROP TABLE H_CONTRIBUTION_STATIC_VALUE;
    DROP TABLE H_CONTRIBUTION_SP_USER_STAT;
    DROP TABLE H_CONTRIBUTION_SPECIMEN_STAT;
    DROP TABLE H_CONTRIBUTION_QUESTION_STAT;
    DROP TABLE H_CONTRIBUTION_ANSWER;
    DROP TABLE H_CONTRIBUTION_QUESTION;


    alter table H_LINK add HEADER_LINK number(1, 0);
    update h_link set HEADER_LINK = 0;

    create table H_FILE (
        id number(19,0) not null,
        data clob,
        name varchar2(255 char),
        filetype varchar2(255 char),
        primary key (id)
    );

    CREATE TABLE H_CATEGORIES  (
        id                        number(19,0) not null,
        label                     varchar2(50) not null,
        constraint pk_h_categories primary key (id)
    );

    CREATE TABLE H_DISCUSSION  (
        id                        number(19,0) not null,
        title                     varchar2(100),
        creation_date             timestamp not null,
        last_update_date          timestamp not null,
        is_resolved               number(1,0)  not null,
        author_id                 number(19,0) not null,
        constraint pk_h_discussion primary key (id)
    );

    CREATE TABLE H_DISCUSSION_CATEGORIES (
        discussion_id            number(19,0) not null,
        category_id              number(19,0) not null,
        constraint pk_h_discussion_categories primary key (discussion_id, category_id)
    );


    CREATE TABLE H_MESSAGE (
        id                        number(19,0) not null,
        text                      varchar2(2000) not null,
        creation_date             timestamp not null,
        last_update_date          timestamp not null,
        author_id                 number(19,0) not null,
        discussion_id             number(19,0) not null,
        is_resolution             number(1,0)  not null,
        is_first                  number(1,0)  not null,
        moderator_login           varchar2(255),
        constraint pk_h_message primary key (id)
    );

    create table H_TAGS (
        id                         number(19,0) not null,
        label                      varchar2(50) not null,
        tag_type                   varchar2(10) not null,
        last_update_date           timestamp not null,
        published                  number(1,0) not null,
        constraint pk_h_tag primary key (id)
    );

    create table H_TAGS_LINKS (
        tag_id                     number(19,0) not null,
        link_type                  varchar2(10) not null,
        target_id                  number(19,0) not null,
        last_update_date           timestamp not null,
        principal                  number(1,0) not null,
        constraint pk_h_tag_links primary key (tag_id, link_type, target_id)
    );

    create table H_TAGS_SUBSCRIPTION (
        tag_id                     number(19,0) not null,
        user_id                    number(19,0) not null,
        constraint pk_h_tags_subscription primary key (tag_id, user_id)
    );

    alter table H_TAGS_SUBSCRIPTION add constraint fk_h_tag_subsc_user1 foreign key (user_id) references H_USER (id) on delete cascade;
    alter table H_TAGS_SUBSCRIPTION add constraint fk_h_tag_subsc_tag1 foreign key (tag_id) references H_TAGS (id) on delete cascade;

    alter table H_TAGS_LINKS add constraint fk_h_tagslinks_tag1 foreign key (tag_id) references H_TAGS (id) on delete cascade;

    alter table H_MESSAGE add constraint fk_h_message_h_discussion_1 foreign key (discussion_id) references H_DISCUSSION (id) on delete cascade;
    alter table H_MESSAGE add constraint fk_h_message_h_user_1 foreign key (author_id) references H_USER (id) on delete cascade;
    alter table H_DISCUSSION add constraint fk_h_discussion_h_user_1 foreign key (author_id) references H_USER (id) on delete cascade;

    alter table H_DISCUSSION_CATEGORIES add constraint fk_disc_cat_disc_1 foreign key (discussion_id) references H_DISCUSSION (id) on delete cascade;
    alter table H_DISCUSSION_CATEGORIES add constraint fk_disc_cat_cat_1 foreign key (category_id) references H_CATEGORIES (id) on delete cascade;

    insert into h_tags (id, label, tag_type, last_update_date, published)
        select HIBERNATE_SEQUENCE.nextval, 'MISSION ' || m.id, 'MISSION', current_timestamp, 1 from h_mission m;

    insert into h_tags_links select t.id, 'MISSION', m.id, current_timestamp, 1
                             from h_mission m, h_tags t
                             where t.label = 'MISSION ' || m.id;

    insert into h_tags (id, label, tag_type, last_update_date, published)
        select HIBERNATE_SEQUENCE.nextval, code, tag_type, last_update_date, 1
        FROM (select sp.code as code, 'SPECIMEN' as tag_type, current_timestamp as last_update_date from h_specimen sp group by sp.code);

    insert into h_tags_links
        select t.id, tmp.link_type, tmp.target_id, tmp.last_update_date, 1
        FROM (select sp.code as code, 'SPECIMEN' as link_type, sp.id as target_id, current_timestamp as last_update_date from h_specimen sp group by sp.code, sp.id) tmp,
            h_tags t
        WHERE t.label = tmp.code;

    INSERT INTO H_CATEGORIES VALUES (HIBERNATE_SEQUENCE.nextval, 'SOS');


    ALTER TABLE H_MISSION add principal_tag number(19,0);
    UPDATE H_MISSION m SET m.principal_tag = (SELECT t.id
                                              FROM H_TAGS t
                                                  INNER JOIN H_TAGS_LINKS tl
                                                      ON tl.tag_id = t.id
                                              WHERE tl.link_type = 'MISSION'
                                                    AND tl.target_id = m.id);

    ALTER TABLE H_MISSION MODIFY principal_tag number(19,0) not null;
    alter table H_MISSION add constraint fk_mission_tag_1 foreign key (principal_tag) references H_TAGS (id);

    ALTER TABLE H_MISSION ADD last_update_date timestamp;
    ALTER TABLE H_SPECIMEN ADD last_update_date timestamp;
    UPDATE H_MISSION SET last_update_date = current_timestamp;
    UPDATE H_SPECIMEN SET last_update_date = current_timestamp;
    ALTER TABLE H_MISSION MODIFY last_update_date timestamp not null;
    ALTER TABLE H_SPECIMEN MODIFY last_update_date timestamp not null;

    alter table h_botanist add last_update_date timestamp;
    alter table h_user add last_update_date timestamp;
    alter table h_announcement add last_update_date timestamp;
    update h_botanist set last_update_date = current_timestamp;
    update h_user set last_update_date = current_timestamp;
    update h_announcement set last_update_date = current_timestamp;
    alter table h_botanist modify last_update_date timestamp not null;
    alter table h_user modify last_update_date timestamp not null;
    alter table h_announcement modify last_update_date timestamp not null;



    -- LIEN MISSION H_SPECIMEN
    ALTER TABLE H_SPECIMEN ADD (MISSION_ID NUMBER(19, 0));

    -- MODIFICATIONS H_USER
    -- Modification pour API User Recolnat
    ALTER TABLE H_USER ADD (RECOLNAT_UUID VARCHAR2(50));

    -- Rôle "Equipe"
    ALTER TABLE H_USER ADD (TEAM NUMBER(1, 0));
    UPDATE H_USER SET TEAM = 0;


    CREATE TABLE H_USER_PASSED_QUIZ (
        user_id                    number(19, 0) not null, -- specimen_id
        quiz_id                    number(19, 0) not null,
        passed_at                  timestamp,
        constraint pk_h_user_passed_quiz primary key (user_id, quiz_id)
    );

    -- Contributions V2

    CREATE TABLE H_CONTRIBUTION_QUESTION (
        ID  NUMBER(19, 0),
        CONFIGURATION clob,
        LABEL   VARCHAR(255),
        MIN_LEVEL   NUMBER(19,0),
        MISSION_ID  NUMBER(19,0),
        NAME    VARCHAR(255),
        NEEDED_QUIZ_ID  NUMBER(19,0),
        MISSION_ORDER   NUMBER(19,0),
        VALIDATION_LEVEL    NUMBER(19,0),
        TEMPLATE_ID NUMBER(19,0),
        HELP_HTML   CLOB,
        SHORT_LABEL VARCHAR(40),
        DEFAULT_PARENT_REF  NUMBER(19,0),
        SORT_INDEX  NUMBER(19,0),
        EDITABLE    NUMBER(1),
        DEFAULT_MISSION NUMBER(1, 0),
        MISSION_MANDATORY   NUMBER(1),
        DELETED NUMBER(1),
        constraint pk_h_contribution_question primary key (id)
    );

    CREATE TABLE H_CONTRIBUTION_ANSWER (
        id                         number(19, 0) not null,
        specimen_id                number(19, 0) not null,
        mission_id                number(19, 0),
        user_id                    number(19, 0),
        validated                  number(1,0),
        question_id                number(19, 0) not null,
        json_value                 clob,
        created_at                 timestamp,
        deleted                    number(1,0),
        from_answer_id             number(19, 0),
        constraint pk_h_contribution_answer primary key (id)
    );

    CREATE TABLE H_CONTRIBUTION_QUESTION_STAT (
        id                         number(19, 0) not null,
        QUESTION_ID                number(19, 0) not null,
        SPECIMEN_ID                number(19, 0) not null,
        MISSION_ID                 number(19, 0) not null,
        validated                  number(1,0),
        conflicts                  number(1,0),
        ANSWER_COUNT               number(19, 0),
        LAST_MODIFIED_AT           timestamp,
        VALID_ANSWER_ID            number(19, 0),
        constraint pk_h_contribution_q_stat primary key (id)
    );
    -- index question_id
    -- index specimen_id

    CREATE TABLE H_CONTRIBUTION_SPECIMEN_STAT (
        specimen_id                number(19, 0) not null, -- specimen_id
        MISSION_ID                 number(19, 0),
        validated                  number(1,0),
        MIN_USEFUL_LEVEL           number(5, 0),
        UNUSABLE_VALIDATED         number(1, 0),
        conflicts                  number(1,0),
        ANSWER_COUNT               number(19, 0),
        LAST_MODIFIED_AT           timestamp,
        constraint pk_h_specimen_stat primary key (specimen_id)
    );

    CREATE TABLE H_CONTRIBUTION_SP_USER_STAT (
        specimen_id                number(19, 0) not null, -- specimen_id
        MISSION_ID                 number(19, 0),
        USER_ID                    number(19,0),
        MARKED_UNUSABLE            number(1, 0),
        MARKED_SEEN                number(1, 0),
        MIN_UNANSWERED_LEVEL           number(5, 0),
        LAST_MODIFIED_AT           timestamp,
        constraint pk_h_specimen_user_stat primary key (specimen_id,user_id)
    );

    CREATE TABLE H_CONTRIBUTION_STATIC_VALUE (
        ID                         number(19, 0),
        TYPE                       varchar(50),
        specimen_id                number(19, 0),
        MISSION_ID                 number(19, 0),
        USER_ID                    number(19,0),
        REFERENCE_RECORD_ID        number(19, 0),
        TEXT_VALUE                 varchar(1000),
        START_DATE                 timestamp,
        END_DATE                   timestamp,
        NO_INFO                    number(1, 0),
        LONGITUDE                  Decimal(9,6),
        LATITUDE                   Decimal(9,6),
        constraint pk_h_contribution_static primary key (ID)
    );

    CREATE TABLE H_MISSION_DAILY_STAT (
        MISSION_ID                 number(19, 0),
        TYPE                       varchar(50),
        STAT_DATE                  timestamp,
        STAT_VALUE                 number(19, 0),
        constraint pk_h_mission_d_stat primary key (MISSION_ID, TYPE, STAT_DATE)
    );




    -- Modifications des alertes et activités pour modèle contributions V2

    ALTER TABLE H_ACTIVITY ADD ( CONTRIBUTION_ANSWER_ID NUMBER(19, 0));
    ALTER TABLE H_ACTIVITY ADD ( CONTRIBUTION_QUESTION_ID NUMBER(19, 0));


    ALTER TABLE H_ALERT ADD ( CONTRIBUTION_ANSWER_ID NUMBER(19, 0) );
    ALTER TABLE H_ALERT ADD ( CONTRIBUTION_QUESTION_ID NUMBER(19, 0) );
    ALTER TABLE H_ALERT ADD ( SPECIMEN_ID NUMBER(19, 0) );
    ALTER TABLE H_ALERT ADD ( OTHER_USER_ID NUMBER(19, 0) );




    -- Vue mission
    CREATE VIEW HV_MISSION_SIMPLE AS
        SELECT ID, TITLE, IMAGEID, BIGIMAGEID, HASIMAGE, SHORTDESCRIPTION,
            CLOSED, OPENINGDATE, PRIORITY, PUBLISHED, GOAL, LEADER_ID
        FROM H_MISSION;

    -- Vues Stats

    -- CREATE VIEW H_TAGS_USAGE_STAT AS
    --     select tag_id, count(*) COUNT_USAGE, max(LAST_UPDATE_DATE) LAST_USAGE from H_TAGS_LINKS
    --     where
    --     group by tag_id;

    CREATE VIEW H_TAGS_USAGE_STAT AS
    select L.tag_id, count(*) COUNT_USAGE, max(L.LAST_UPDATE_DATE) LAST_USAGE
    from H_TAGS_LINKS l inner join H_TAGS t on l.tag_id  = t.id
    where t.tag_type = 'USER'
    group by tag_id;

    CREATE VIEW HV_STAT_CONTRIBUTION_QUESTION AS
    select
        MISSION_ID,
        QUESTION_ID,
        SUM(CASE WHEN VALIDATED = 1 THEN 1 ELSE 0 END) VALIDATED,
        SUM(CASE WHEN VALIDATED = 0 AND CONFLICTS = 0 THEN 1 ELSE 0 END) IN_PROGRESS,
        SUM(CASE WHEN CONFLICTS = 1 THEN 1 ELSE 0 END) CONFLICTS
    from
        H_CONTRIBUTION_QUESTION_STAT QS
    group by
        MISSION_ID,
        QUESTION_ID;


    CREATE VIEW VH_TOP_CONTRIBUTIOR_STAT AS
    SELECT
        A.USER_ID,
        count(*) AS ANSWER_COUNT
    FROM
        H_CONTRIBUTION_ANSWER A
    WHERE
        a.deleted != 1 and a.user_id is not null
    GROUP BY
        A.USER_ID;

    CREATE VIEW VH_MISSION_CONTRIBUTION_STAT AS
    SELECT
        A.MISSION_ID,
        A.USER_ID,
        count(*) AS ANSWER_COUNT
    FROM
        H_CONTRIBUTION_ANSWER A
    WHERE
        a.deleted != 1
    GROUP BY
        A.MISSION_ID, A.USER_ID;

    -- IDEM EN MATERIALIZED

    CREATE VIEW VMH_MISSION_CONTRIBUTION_STAT AS
    SELECT
        A.MISSION_ID,
        A.USER_ID,
        count(*) AS ANSWER_COUNT
    FROM
        H_CONTRIBUTION_ANSWER A
    WHERE
        a.deleted != 1
    GROUP BY
        A.MISSION_ID, A.USER_ID;


    CREATE VIEW VH_MISSION_C_USER_STAT AS
        SELECT
            A.MISSION_ID,
            A.QUESTION_ID,
            A.USER_ID,
            count(*) AS ANSWER_COUNT
        FROM
            H_CONTRIBUTION_ANSWER A
        WHERE
            a.deleted != 1
        GROUP BY  A.MISSION_ID, A.QUESTION_ID, A.USER_ID;

    CREATE VIEW VH_MISSION_VALUE_STAT AS
    select
        MISSION_ID,
        TYPE,
        TEXT_VALUE,
        COUNT(*) AS ANSWER_COUNT
    from
        H_CONTRIBUTION_STATIC_VALUE
    GROUP BY
        MISSION_ID, TYPE, TEXT_VALUE;



    -- REFERENCES
    CREATE TABLE H_REFERENCE (
      ID NUMBER(19, 0),
      ALLOW_USER_CREATION NUMBER(1, 0),
      LABEL VARCHAR2(100),
      NAME VARCHAR2(50),
      PARENT_ID NUMBER(19, 0),
      constraint pk_h_reference primary key (ID)
    );

    CREATE TABLE H_REFERENCE_RECORD (
      ID NUMBER(19, 0),
      ID_V1 NUMBER(19, 0),
      REFERENCE_ID NUMBER(19, 0),
      LABEL VARCHAR2(100),
      VALUE VARCHAR2(50),
      PARENT_ID NUMBER(19, 0),
      IMAGE_ID NUMBER(19, 0),
      LAST_UPDATE_DATE timestamp not null,
      constraint pk_h_reference_record primary key (ID)
    );
    -- ALTER TABLE H_REFERENCE_RECORD ADD CONSTRAINT constraint_name UNIQUE (VALUE, REFERENCE_ID);

    CREATE TABLE H_REFERENCE_RECORD_INFO (
      RECORD_ID NUMBER(19, 0),
      FIELD_NAME VARCHAR2(50),
      FIELD_VALUE VARCHAR2(50),
      constraint pk_h_reference_record_i primary key (RECORD_ID, FIELD_NAME)
    );

    -- Panier specimen
    CREATE TABLE H_MISSION_CART_QUERY (
        ID                      number(19, 0),
        MISSION_ID              number(19, 0),
        TEXT_FILE_ID            number(19, 0),
        HITS                    number(19, 0),
        SYNC                    number(1, 0),
        DELETED                 number(1, 0),
        NO_COLLECT_INFO         number(1, 0),
        ALL_SELECTED            number(1, 0),
        ALL_SELECTED_D          number(1, 0),
        constraint pk_h_mission_ca_qu primary key (ID)
    );

    CREATE TABLE H_MISSION_CART_QUERY_SEL (
        CART_QUERY_ID           number(19, 0),
        SPECIMEN_ID             VARCHAR2(50),
        constraint pk_h_mission_ca_qsel primary key (CART_QUERY_ID, SPECIMEN_ID)
    );

    CREATE TABLE H_MISSION_CART_QUERY_SEL_D (
        CART_QUERY_ID           number(19, 0),
        SPECIMEN_ID             VARCHAR2(50),
        constraint pk_h_mission_ca_qseld primary key (CART_QUERY_ID, SPECIMEN_ID)
    );

    CREATE TABLE H_MISSION_CART_QUERY_TERM (
        CART_QUERY_ID           number(19, 0),
        FIELD_NAME              VARCHAR2(50),
        FIELD_VALUE             VARCHAR2(255),
        constraint pk_h_mission_ca_qt primary key (CART_QUERY_ID, FIELD_NAME)
    );

    CREATE TABLE H_RECOLNAT_TRANSFER (
        ID            number(19, 0),
        SPECIMEN_ID   number(19, 0),
        QUESTION_NAME             VARCHAR2(50),
        NO_TRANSFER_CAUSE             VARCHAR2(50),
        TRANSFER_DONE             NUMBER(1),
        TRANSFER_DATE  TIMESTAMP,
        constraint pk_h_recolnat_transfer primary key (ID)
    );



    -- Modifications H_MISSION pour panier de specimens
    ALTER TABLE H_MISSION ADD LOADING_CART NUMBER(1, 0);
    UPDATE H_MISSION SET LOADING_CART = 0;
    ALTER TABLE H_MISSION ADD RECOLNAT_TRANSFER_PROGRESS NUMBER(1, 0);
    UPDATE H_MISSION SET RECOLNAT_TRANSFER_PROGRESS = 0;
    ALTER TABLE H_MISSION ADD RECOLNAT_TRANSFER_DONE NUMBER(1, 0);
    UPDATE H_MISSION SET RECOLNAT_TRANSFER_DONE = 0;
    ALTER TABLE H_MISSION ADD RECOLNAT_TRANSFER_DATE TIMESTAMP;
    ALTER TABLE H_SPECIMEN ADD SPECIFICEPITHET VARCHAR2(100);


    -- Modifications H_MISSION pour proposition
    ALTER TABLE H_MISSION ADD PROPOSITION NUMBER(1, 0);
    UPDATE H_MISSION SET PROPOSITION = 0;
    ALTER TABLE H_MISSION ADD VALIDATOR_ID NUMBER(19, 0);


    -- Modifications H_MISSION pour proposition
    ALTER TABLE H_MISSION ADD PROPOSITION_SUBMITTED NUMBER(1, 0);
    ALTER TABLE H_MISSION ADD PROPOSITION_VALIDATED NUMBER(1, 0);
    UPDATE H_MISSION SET PROPOSITION_VALIDATED = 0;
    UPDATE H_MISSION SET PROPOSITION_SUBMITTED = 0;

    alter table H_MISSION_CART_QUERY add constraint fk_h_cartq_mission1 foreign key (mission_id) references H_MISSION (id) on delete cascade;
    alter table H_MISSION_CART_QUERY_SEL add constraint fk_h_cartqsel_cartq1 foreign key (cart_query_id) references H_MISSION_CART_QUERY (id) on delete cascade;
    alter table H_MISSION_CART_QUERY_SEL_D add constraint fk_h_cartqseld_cartq1 foreign key (cart_query_id) references H_MISSION_CART_QUERY (id) on delete cascade;
    alter table H_MISSION_CART_QUERY_TERM add constraint fk_h_cartqterm_cartq1 foreign key (cart_query_id) references H_MISSION_CART_QUERY (id) on delete cascade;

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
        title            VARCHAR2(100),
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
        principal                  number(1,0) not null,
        CONSTRAINT pk_h_tag_links_tmp PRIMARY KEY (tag_id, link_type, target_id)
    );

    INSERT INTO H_DISCUSSION_TMP
        SELECT
            HIBERNATE_SEQUENCE.nextval,
            tmp.mission_id,
            tmp.creation_date,
            tmp.creation_date,
            0,
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
            d.last_update_date,
            1
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

    UPDATE H_DISCUSSION_TMP SET title = '';
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
            0,
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
            d.last_update_date,
            1
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

    UPDATE H_DISCUSSION_TMP SET title = '';
    INSERT INTO H_DISCUSSION SELECT * FROM H_DISCUSSION_TMP;
    INSERT INTO H_TAGS_LINKS SELECT * FROM H_TAGS_LINKS_TMP;
    INSERT INTO H_MESSAGE SELECT * FROM H_MESSAGE_TMP;

    DROP TABLE H_MESSAGE_TMP;

    DROP TABLE H_DISCUSSION_TMP;

    DROP TABLE H_TAGS_LINKS_TMP;


--
-- Création des index
--

CREATE INDEX h_specimen_mission_id_idx ON H_SPECIMEN(MISSION_ID);
CREATE INDEX h_tags_tagLabel_idx ON H_TAGS(label);

CREATE INDEX H_CON_ANS_US_IX  ON H_CONTRIBUTION_ANSWER(USER_ID);
CREATE INDEX H_CON_ANS_SP_US_IX  ON H_CONTRIBUTION_ANSWER(SPECIMEN_ID, USER_ID);
CREATE INDEX H_CON_ANS_MS_US_IX  ON H_CONTRIBUTION_ANSWER(MISSION_ID, USER_ID);
CREATE INDEX H_CON_ANS_MQU_IX  ON H_CONTRIBUTION_ANSWER(MISSION_ID, QUESTION_ID, USER_ID);
CREATE INDEX H_CON_QUE_STA_SP_US_IX  ON H_CONTRIBUTION_QUESTION_STAT (SPECIMEN_ID, QUESTION_ID);
CREATE INDEX H_CON_ANS_US_DEL_IX  ON H_CONTRIBUTION_ANSWER(USER_ID, DELETED);

-- ref records

CREATE INDEX H_REF_RECORD_IX1  ON H_REFERENCE_RECORD(REFERENCE_ID);
CREATE INDEX H_REF_RECORD_IX2  ON H_REFERENCE_RECORD(REFERENCE_ID, PARENT_ID);
CREATE INDEX H_REF_RECORD_IX3  ON H_REFERENCE_RECORD_INFO(RECORD_ID);

-- tags

CREATE INDEX H_TAG_STAT_IX ON H_TAGS_LINKS(tag_id);


-- Valeurs statiques

CREATE INDEX H_CON_STATIC_IX2 ON H_CONTRIBUTION_STATIC_VALUE (TYPE, MISSION_ID);
CREATE INDEX H_CON_STATIC_IX3 ON H_CONTRIBUTION_STATIC_VALUE (TYPE, TEXT_VALUE);



-- Stats page profile
CREATE INDEX H_CON_SP_US_ST_IX1 ON H_CONTRIBUTION_SP_USER_STAT(MISSION_ID, USER_ID);
--CREATE INDEX H_CON_SP_US_ST_IX1 ON H_CONTRIBUTION_SP_USER_STAT(LAST_MODIFIED_AT);