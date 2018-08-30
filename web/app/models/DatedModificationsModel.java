package models;

import play.db.jpa.JPABase;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * Created by Jonathan on 18/11/2015.
 */
public abstract class DatedModificationsModel<T extends JPABase> extends Model {

    public abstract void setLastUpdateDate();

    @Override
    public <T extends JPABase> T save() {
        setLastUpdateDate();
        return super.save();
    }

    @Override
    public boolean create() {
        setLastUpdateDate();
        return super.create();
    }

}
