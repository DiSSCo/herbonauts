package models.quiz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.data.validation.MaxSize;
import play.db.jpa.Blob;
import play.db.jpa.Model;
import play.templates.JavaExtensions;

@Entity
@Table(name="H_QUESTION")
public class Question extends Model {
	
	@ManyToOne
	private Quiz quiz;
	
	private String text;
	
	private Long imageId;
	
	private boolean hasImage;

	@MaxSize(1000)
	@Column(columnDefinition="VARCHAR2(1000 char)")
	private String answerDetails;
	
	private Integer sortIndex;
	
	@OneToMany(mappedBy="question", cascade={CascadeType.ALL})
	private List<Choice> choices;

	public List<Choice> getChoices() {
		return choices;
	}

	public boolean isAnswerCorrect(List<Long> answerIds) {
		if (choices == null) {
			return true;
		}
		for (Choice choice : choices) {
			
			if ((choice.isAnswer() &&  (answerIds == null || !answerIds.contains(choice.id))) ||
				(!choice.isAnswer()) && answerIds != null && answerIds.contains(choice.id)) {
				return false;
			}
		}
		return true;
	}
	
	public void mergeChoices(List<Choice> inChoices) {
		if (inChoices == null) {
			return;
		}
		
		HashMap<Long, Choice> byId = new HashMap<Long, Choice>();
		if (choices != null) {
			for (Choice choice : choices) {
				byId.put(choice.id, choice);
			}
		}
		
		for (Choice inChoice : inChoices) {
			if (inChoice == null) {
				continue;
			}
			if (inChoice.id != null && byId.get(inChoice.id) != null) {
				Choice merged = byId.get(inChoice.id);
				merged.setText(inChoice.getText());
				merged.setAnswer(inChoice.isAnswer());
				byId.remove(inChoice.id);
			} else {
				choices.add(inChoice);
				inChoice.setQuestion(this);
			}
		}
		
		for (Choice choice : byId.values()) {
			choice.setQuestion(null);
			choice.delete();
			choices.remove(choice);
		}
		
	}

	public void setQuiz(Quiz quiz) {
		this.quiz = quiz;
	}

	public Quiz getQuiz() {
		return quiz;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setImageId(Long imageId) {
		this.imageId = imageId;
	}

	public Long getImageId() {
		return imageId;
	}

	public void setHasImage(boolean hasImage) {
		this.hasImage = hasImage;
	}

	public boolean isHasImage() {
		return hasImage;
	}

	public void setAnswerDetails(String answerDetails) {
		this.answerDetails = answerDetails;
	}

	public String getAnswerDetails() {
		return answerDetails;
	}

	public void setSortIndex(Integer sortIndex) {
		this.sortIndex = sortIndex;
	}

	public Integer getSortIndex() {
		return sortIndex;
	}
	
}
