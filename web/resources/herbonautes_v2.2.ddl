--
-- Multiple leaders
--
CREATE TABLE H_MISSION_LEADER (
  USER_ID    NUMBER(19, 0),
  MISSION_ID number(19, 0),
  USER_LOGIN varchar2(255 char),
  VISIBLE    NUMBER(1, 0) DEFAULT 1
);

alter table H_MISSION_LEADER
    add constraint FK_H_ML_USER
    foreign key (USER_ID)
    references H_USER;

alter table H_MISSION_LEADER
    add constraint FK_H_ML_MISSION
    foreign key (MISSION_ID)
    references H_MISSION;

INSERT INTO H_MISSION_LEADER
SELECT
  M.LEADER_ID AS USER_ID,
  M.ID AS MISSION_ID,
  U.LOGIN AS USER_LOGIN,
  1 AS VISIBLE
FROM
  H_MISSION M
  INNER JOIN H_USER U ON M.LEADER_ID = U.ID;

--
-- deleted tiles
--
ALTER TABLE H_SPECIMEN
  ADD DELETED_TILES NUMBER(1, 0) DEFAULT 0;