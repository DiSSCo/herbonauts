ALTER TABLE H_REFERENCE_RECORD ADD last_update_date timestamp ;
UPDATE H_REFERENCE_RECORD set last_update_date = current_timestamp ;
ALTER TABLE H_REFERENCE_RECORD MODIFY last_update_date timestamp not null;