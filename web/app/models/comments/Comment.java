package models.comments;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import net.sf.oval.constraint.MaxLength;
import models.User;
import play.data.validation.Max;
import play.data.validation.MaxSize;
import play.db.jpa.Model;
import play.templates.JavaExtensions;

@Entity
@Table(name="H_COMMENT")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE) 
@DiscriminatorColumn(name="type")
public class Comment extends Model {
  
  @ManyToOne
  private User user;
  
  @Column(name="CREATION_DATE")
  private Date date;

  @MaxSize(1000)
  @Column(columnDefinition="VARCHAR2(1000 char)")
  private String text;
  
  @PrePersist
  public void markDate() {
    this.setDate(new Date());
  }

  public void setUser(User user) {
    this.user = user;
  }

  public User getUser() {
    return user;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public Date getDate() {
    return date;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }
  
   public Long getCommentsCount(Long idMission) {
      return count("select count(comment) from H_COMMENT comment where comment.MISSION_ID = ?", idMission);
    }
  
}
