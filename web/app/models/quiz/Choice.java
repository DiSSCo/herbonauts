package models.quiz;

import javax.persistence.*;

import play.db.jpa.Model;

@Entity
@Table(name="H_CHOICE")
public class Choice extends Model {

	@ManyToOne(cascade = CascadeType.ALL)
	private Question question;
	
	private String text;
	
	private boolean answer;

	public void setQuestion(Question question) {
		this.question = question;
	}

	public Question getQuestion() {
		return question;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setAnswer(boolean answer) {
		this.answer = answer;
	}

	public boolean isAnswer() {
		return answer;
	}
	
}
