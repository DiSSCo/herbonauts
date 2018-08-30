ALTER TABLE H_TAGS_LINKS ADD principal number(1,0);
UPDATE H_TAGS_LINKS set principal = 0;
ALTER TABLE H_TAGS_LINKS MODIFY principal number(1,0) not null;
