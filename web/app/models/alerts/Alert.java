package models.alerts;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import play.db.jpa.Model;

import models.User;
import models.activities.Activity;

@Entity
@Table(name="H_ALERT")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE) 
@DiscriminatorColumn(name="type")
public abstract class Alert extends Model {

	@Column(insertable = false, updatable = false)
	private String type;
	
	@Column(name="CREATION_DATE")
	private Date date;
	
	@ManyToOne
	private User user;
	
	private boolean userRead;
	private boolean emailSent = false;
	
	
	@PrePersist
	public void blank() {
		this.setDate(new Date());
		this.setUserRead(false);
		//this.setEmailSent(false);
	}


	public void setUserRead(boolean userRead) {
		this.userRead = userRead;
	}


	public boolean isUserRead() {
		return userRead;
	}


	public void setEmailSent(boolean emailSent) {
		this.emailSent = emailSent;
	}


	public boolean isEmailSent() {
		return emailSent;
	}


	public void setType(String type) {
		this.type = type;
	}


	public String getType() {
		return type;
	}


	public void setDate(Date date) {
		this.date = date;
	}


	public Date getDate() {
		return date;
	}


	public void setUser(User user) {
		this.user = user;
	}


	public User getUser() {
		return user;
	}
	
}
