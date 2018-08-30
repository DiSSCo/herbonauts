package models.discussions;

import org.hibernate.annotations.ManyToAny;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Jonathan on 17/06/2015.
 */
@Entity
@Table(name = "H_CATEGORIES")
public class DiscussionCategory extends Model {

    @Column(name = "label")
    private String label;

    @ManyToMany(mappedBy = "categories")
    private List<Discussion> discussions;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Discussion> getDiscussions() {
        return discussions;
    }

    public void setDiscussions(List<Discussion> discussions) {
        this.discussions = discussions;
    }

    public static List<DiscussionCategory> findAllOrdered() {
        return DiscussionCategory.find("order by id").fetch();
    }



}
