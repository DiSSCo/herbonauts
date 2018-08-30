package models.quiz;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import controllers.Quizzes;
import play.Logger;
import play.data.validation.Check;
import play.data.validation.CheckWith;
import play.data.validation.MaxSize;
import play.db.jpa.Model;

@Entity
@Table(name="H_QUIZ")
public class Quiz extends Model {

	private int unlockingLevel;

    @CheckWith(value = NewQuizValidator.class, message = "quiz.name.already.exists")
	private String name; // (ID)
	
	private String lang;
	
	private String title;

	@Lob
	@Column(columnDefinition="TEXT")
	private String description;
	
	@Lob
	@Column(columnDefinition="TEXT")
	private String congratulations;
	
	@OneToMany(fetch=FetchType.LAZY, mappedBy="quiz")
	public List<Question> questions;

    public static class NewQuizValidator extends Check {
        @Override
        public boolean isSatisfied(Object quizObj, Object nameObj) {
            Quiz quiz = (Quiz) quizObj;
            String name = (String) nameObj;
            if (quiz.id == null && Quiz.count("name = ?", name) > 0) {
                return false;
            }

            return true;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    public Quiz() {
        super();
    }

    public Quiz(Long id) {
        super();
        this.id = id;
    }

    public List<Question> getQuestions() {
		return Question
			.find("quiz.id = ? order by sortIndex", this.id)
			.fetch();
	}
	
	public Question getQuestion(int number) {
		return Question
					.find("quiz.id = ? order by sortIndex", this.id)
					.from(number - 1)
					.first();
	}
	
	public static Quiz quizForLevel(int level) {
		return Quiz.find("unlockingLevel = ?", level).first();
	}

	public void setUnlockingLevel(int unlockingLevel) {
		this.unlockingLevel = unlockingLevel;
	}

	public int getUnlockingLevel() {
		return unlockingLevel;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getLang() {
		return lang;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setCongratulations(String congratulations) {
		this.congratulations = congratulations;
	}

	public String getCongratulations() {
		return congratulations;
	}
	
}
