package controllers;

import inspectors.Event;
import inspectors.InspectorChain;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import libs.Images;
import models.Image;
import models.Mission;
import models.User;
import models.quiz.Choice;
import models.quiz.Question;
import models.quiz.Quiz;

import models.serializer.contributions.SimpleQuizJsonSerializer;
import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.data.validation.*;
import play.data.validation.Error;
import play.db.jpa.JPA;
import play.i18n.Messages;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import play.mvc.Http;

/**
 * Gestion des quiz
 */
public class Quizzes extends Application {

	private static final String CURRENT_QUESTION_KEY = "q";
	private static final String CURRENT_ERROR_COUNT_KEY = "ec";

	/**
	 * Redirection vers le début du quiz
	 */
	public static void show(String name) {
		start(name);
	}
	
	public static void saveSort(String name, Long[] sort) {
		int index = 1;
		for (Long id : sort) {
			Question question = Question.findById(id);
			question.setSortIndex(index++);
			question.save();
		}
		ok();
	}

	// TODO Reprise si déjà commencé ?
	public static void start(String name) {
		resetErrorsCount();
		session.remove(CURRENT_QUESTION_KEY);
		Quiz quiz = Quiz.find("name = ?", name).first();
		notFoundIfNull(quiz);
 
		// Quiz level 0 = quiz hors mission
		if (quiz.getUnlockingLevel() > 0) {
			
			User user = Security.connected();
	
			// Le quiz n'est pas adapté, il y a un d'autres niveaux
			// à débloquer avant
			if (user.getLevel() + 1 < quiz.getUnlockingLevel()) {
				Quiz prioritaryQuiz = Quiz.quizForLevel(user.getLevel() + 1);
	
				if (prioritaryQuiz != null) {
					flash.error(Messages.get("take.quiz.before", prioritaryQuiz.getTitle(), quiz.getTitle()));
					start(prioritaryQuiz.getName());
				}
			}
		} 
		render(quiz);
	}

	/**
	 * Affichage de l'image d'une question
	 * @param id
	 */
	public static void image(Long id) {
		final Question question = Question.findById(id);
		notFoundIfNull(question);

		if (!question.isHasImage()) {
			notFound();
		}

		renderImage(question.getImageId());
	}

	/**
	 * Envoie le quiz necessaire pour un niveau au format JSON
	 * (utilisé sur la page de contribution pour afficher les quiz à 
	 * passer)
	 */
	public static void forLevel(Integer level) {
		Quiz quiz = Quiz.quizForLevel(level);

		notFoundIfNull(quiz);

		Gson gson = new Gson();
		JsonObject jsonQuiz = new JsonObject();
		jsonQuiz.addProperty("title", quiz.getTitle());
		jsonQuiz.addProperty("name", quiz.getName());

		renderJSON(gson.toJson(jsonQuiz));
	}

	/**
	 * Fin du quiz
	 */
	public static void end(String name) {
		Quiz quiz = Quiz.find("name = ?", name).first();
		notFoundIfNull(quiz);

		// Bilan
		Integer errorsCount = getErrorsCount();
		boolean passed = (errorsCount == 0);

		// Lancement de l'évenement
		if (passed) {
			User user = Security.connected();
			InspectorChain.get().inspect(Event.QUIZ_PASSED, quiz, user);
		}


		Long lastMissionId = session.get(Missions.CURRENT_MISSION_KEY) != null ? Long
				.valueOf(session.get(Missions.CURRENT_MISSION_KEY)) : null;
		Long lastSpecimenId = session.get(Missions.CURRENT_SPECIMEN_KEY) != null ? Long
				.valueOf(session.get(Missions.CURRENT_SPECIMEN_KEY)) : null;

		render(quiz, passed, errorsCount, lastMissionId, lastSpecimenId);
	}

	/**
	 * Affichage d'une question
	 */
	public static void showQuestion(String name, Integer number) {
		if (number != 1) {
			if (session.get(CURRENT_QUESTION_KEY) == null) {
				session.put(CURRENT_QUESTION_KEY, 1);
			}
			if (number != Integer.valueOf(session.get(CURRENT_QUESTION_KEY))) {
				// On ne triche pas
				showQuestion(name,
						Integer.valueOf(session.get(CURRENT_QUESTION_KEY)));
			}
		}
		session.put(CURRENT_QUESTION_KEY, number);

		Quiz quiz = Quiz.find("name = ?", name).first();
		Integer totalNumber = quiz.questions.size();
		Question question = quiz.getQuestion(number);

		render(quiz, question, number, totalNumber);
	}

	/**
	 * Valide la réponse à une question
	 */
	public static void validateQuestion(String name, Integer number,
			List<Long> answers) {
		if (number != 1) {
			if (number != Integer.valueOf(session.get(CURRENT_QUESTION_KEY))) {
				// On ne triche pas
				showQuestion(name,
						Integer.valueOf(session.get(CURRENT_QUESTION_KEY)));
			}
		}

		if (answers != null) {
			for (Long answer : answers) {
				Logger.info(">%s", answer);
			}
		}

		Quiz quiz = Quiz.find("name = ?", name).first();
		Question question = quiz.getQuestion(number);
		Integer totalNumber = quiz.questions.size();
		Integer nextNumber = number + 1;
		session.put(CURRENT_QUESTION_KEY, nextNumber);

		boolean correct = question.isAnswerCorrect(answers);

		if (!correct) {
			incrementErrorsCount();
		}

		boolean finished = false;
		if (quiz.questions.size() == number) {
			finished = true;
		}

		render(quiz, question, number, nextNumber, totalNumber, finished,
				correct, answers);
	}


	private static Integer getErrorsCount() {
		return session.get(CURRENT_ERROR_COUNT_KEY) == null ? 0 : Integer
				.valueOf(session.get(CURRENT_ERROR_COUNT_KEY));
	}

	private static void incrementErrorsCount() {
		session.put(CURRENT_ERROR_COUNT_KEY, getErrorsCount() + 1);
	}

	private static void resetErrorsCount() {
		session.remove(CURRENT_ERROR_COUNT_KEY);
	}
	
	/**
	 * Modifie la question
	 */
	public static void edit(String name) {

		Security.forbiddenIfNotTeam();

		Quiz quiz = Quiz.find("name = ?", name).first();
		notFoundIfNull(quiz);

		render(quiz);
	}

	public static void editQuestion(String name, Long id) {

		Security.forbiddenIfNotTeam();

		Quiz quiz = Quiz.find("name = ?", name).first();
		notFoundIfNull(quiz);

		Question question = Question.findById(id);

		render(quiz, question);
	}

	public static void list() {

		Security.forbiddenIfNotTeam();

		List<Quiz> quizzes = Quiz.findAll();

		render(quizzes);
	}

	public static void blankQuiz() {
		Security.forbiddenIfNotTeam();
		render();
	}

	public static void blankQuestion(String name) {
		Security.forbiddenIfNotTeam();
		Quiz quiz = Quiz.find("name = ?", name).first();
		notFoundIfNull(quiz);

		render(quiz);
	}

	public static void createQuestion(String name, @Valid Question question,
			File image, List<Choice> choices) {

		Security.forbiddenIfNotTeam();

		Quiz quiz = Quiz.find("name = ?", name).first();
		notFoundIfNull(quiz);

		if (image != null) {
			Image imageDB = Image.createImageFromBytes(Images.compress(image));
			question.setImageId(imageDB.id);
			question.setHasImage(true);
		}

		question.setQuiz(quiz);

		question.save();

		if (choices != null) {
			for (Choice choice : choices) {
				if (choice != null && StringUtils.isNotBlank(choice.getText())) {
					choice.setQuestion(question);
					choice.save();
				}
			}
		}

		edit(quiz.getName());
	}

	public static void createQuiz(@Valid Quiz quiz) {

		Security.forbiddenIfNotTeam();


        if(validation.hasErrors()) {
            params.flash(); // add http parameters to the flash scope
            validation.keep(); // keep the errors for the next request
            blankQuiz();
        }

		quiz.save();

		list();
	}

	public static void saveQuiz(String name, Quiz quiz) {

		Security.forbiddenIfNotTeam();

		Quiz quizToUpdate = Quiz.find("name = ?", name).first();

		quizToUpdate.setName(quiz.getName());
		quizToUpdate.setDescription(quiz.getDescription());
		quizToUpdate.setUnlockingLevel(quiz.getUnlockingLevel());
		quizToUpdate.setTitle(quiz.getTitle());
		quizToUpdate.setCongratulations(quiz.getCongratulations());

		quizToUpdate.save();

		list();
	}

	public static void saveQuestion(String name, Long id, @Valid Question question,
			File image, List<Choice> choices) {

		Security.forbiddenIfNotTeam();

		Question questionToUpdate = Question.findById(id);

		questionToUpdate.setText(question.getText());
		questionToUpdate.setAnswerDetails(question.getAnswerDetails());

		if (image != null) {
			Image imageDB = Image.createImageFromFile(image);
			questionToUpdate.setImageId(imageDB.id);
			questionToUpdate.setHasImage(true);
		}

		questionToUpdate.mergeChoices(choices);

		questionToUpdate.save();
		questionToUpdate.refresh();

		edit(name);
	}

	public static void deleteQuestion(String name, Long id) {

		Security.forbiddenIfNotTeam();

		Question question = Question.findById(id);
		question.delete();
		edit(name);
	}


    // API

    public static void allQuizzes() {
        List<Quiz> quizzes = Quiz.findAll();
        renderJSON(quizzes, SimpleQuizJsonSerializer.get());
    }
}
