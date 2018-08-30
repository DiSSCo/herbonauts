ALTER TABLE H_TAGS ADD published number(1,0);
UPDATE H_TAGS SET published=1;
ALTER TABLE H_TAGS MODIFY published number(1,0) NOT NULL;

alter table H_MISSION drop constraint fk_mission_tag_1;
alter table H_MISSION add constraint fk_mission_tag_1 foreign key (principal_tag) references H_TAGS (id);

-- Modifications H_MISSION pour proposition
ALTER TABLE H_MISSION ADD PROPOSITION_SUBMITTED NUMBER(1, 0);
ALTER TABLE H_MISSION ADD PROPOSITION_VALIDATED NUMBER(1, 0);
UPDATE H_MISSION SET PROPOSITION_VALIDATED = 0;
UPDATE H_MISSION SET PROPOSITION_SUBMITTED = 0;

DELETE FROM H_BADGE WHERE type = 'CANDLE';

alter table H_MISSION_CART_QUERY add constraint fk_h_cartq_mission1 foreign key (mission_id) references H_MISSION (id) on delete cascade;
alter table H_MISSION_CART_QUERY_SEL add constraint fk_h_cartqsel_cartq1 foreign key (cart_query_id) references H_MISSION_CART_QUERY (id) on delete cascade;
alter table H_MISSION_CART_QUERY_SEL_D add constraint fk_h_cartqseld_cartq1 foreign key (cart_query_id) references H_MISSION_CART_QUERY (id) on delete cascade;
alter table H_MISSION_CART_QUERY_TERM add constraint fk_h_cartqterm_cartq1 foreign key (cart_query_id) references H_MISSION_CART_QUERY (id) on delete cascade;

