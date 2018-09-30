--
-- Geolocalisation des Herbonautes
--

ALTER TABLE H_USER
ADD LATITUDE DECIMAL(9, 6)
ADD LONGITUDE DECIMAL(9, 6)
ADD DELETED NUMBER(1, 0) DEFAULT 0
ADD ALERT_MISSION NUMBER(1, 0) DEFAULT 1
ADD ALERT_SPECIMEN NUMBER(1, 0) DEFAULT 1;

--
-- Parrainage
--
CREATE TABLE H_USER_INVITATION (
  ID NUMBER(19, 0) NOT NULL,
  FROM_USER_ID NUMBER(19, 0),
  TO_EMAIL VARCHAR(255) NOT NULL,
  TOKEN VARCHAR(50) NOT NULL,
  INVITATION_DATE TIMESTAMP,
  PRIMARY KEY (ID)
);

--
-- Lien de confirmation de changement de mail
--
CREATE TABLE H_EMAIL_CONFIRMATION (
  ID NUMBER(19, 0) NOT NULL,
  USER_ID NUMBER(19, 0),
  TOKEN VARCHAR(50) NOT NULL,
  EMAIL VARCHAR(255) NOT NULL,
  CHANGE_DATE TIMESTAMP,
  PRIMARY KEY (ID)
);

--
-- Specimen master (pour gestion des specimens par mission)ssssss
--
CREATE TABLE H_SPECIMEN_MASTER (
    id number(19,0) not null,
    code varchar2(255 char),
    collection varchar2(255 char),
    institute varchar2(255 char),
    family varchar2(255 char),
    genus varchar2(255 char),
    SPECIFICEPITHET VARCHAR2(100),
    sonneratURL varchar2(255 char),
    tropicosURL varchar2(255 char),
    primary key (id)
);

ALTER TABLE H_SPECIMEN ADD MASTER_ID number(19,0);
ALTER TABLE H_SPECIMEN DROP CONSTRAINT CODEBARRE_UNIQUE;

CREATE INDEX h_spe_mas_code_idx ON H_SPECIMEN_MASTER(CODE);

-- Création des spécimens master (id specimen utilisé)
INSERT INTO H_SPECIMEN_MASTER
SELECT
  id,
  code, collection, institute,
  family, genus, specificepithet,
  sonneraturl, tropicosurl
FROM
  H_SPECIMEN;

-- Update master id
UPDATE H_SPECIMEN SET MASTER_ID = ID WHERE MASTER_ID IS NULL;

--
-- Mission import exception (pour éviter d'importer des specimens dans plusieurs missions)
--
CREATE TABLE H_MISSION_IMPORT_EXCEPTION (
    ID                         number(19, 0),
    MISSION_ID                 number(19, 0),
    IGNORE_MISSION_ID          number(19,0),
    constraint pk_h_mission_exc primary key (ID)
);
