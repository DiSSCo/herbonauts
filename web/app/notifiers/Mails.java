package notifiers;

import java.util.*;

import javax.mail.internet.InternetAddress;

import models.*;
import models.alerts.Alert;

import models.discussions.Discussion;
import models.discussions.Message;
import models.tags.Tag;
import models.tags.TagType;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;

import conf.Herbonautes;

import play.Logger;
import play.Play;
import play.exceptions.MailException;
import play.exceptions.TemplateNotFoundException;
import play.i18n.Messages;
import play.libs.Mail;
import play.mvc.Mailer;
import play.mvc.Router;
import play.templates.Template;
import play.templates.TemplateLoader;

public class Mails extends Mailer {
	
	public static void welcome(User user) {
		try {
			setSubject(Messages.get("mail.welcome.subject", user.getLogin()));
			addRecipient(user.getEmail());

			setFrom(Herbonautes.get().mailsFrom);
			send(user);
		} catch (Exception e) {
			Logger.error(e, "Impossible d'envoyer le mail à %s", user.getLogin());
		}
	}

	public static void invitation(UserInvitation invitation) {
		try {
			setSubject(Messages.get("mail.invitation.subject", invitation.getFromUser().getLogin()));
			addRecipient(invitation.getToEmail());
			setFrom(Herbonautes.get().mailsFrom);
			send(invitation);
		} catch (Exception e) {
			Logger.error(e, "Impossible d'envoyer le mail invitation à %s", invitation.getToEmail());
		}
	}

	public static void mailChangeConfirmation(EmailConfirmation confirmation) {
		try {
			setSubject(Messages.get("mail.email.change.subject"));
			addRecipient(confirmation.getEmail());
			setFrom(Herbonautes.get().mailsFrom);
			send(confirmation);
		} catch (Exception e) {
			Logger.error(e, "Impossible d'envoyer le mail invitation à %s", confirmation.getEmail());
		}
	}

	public static void goodbye(String login, String email) {
		try {
			setSubject(Messages.get("mail.goodbye.subject", login));
			addRecipient(email);

			setFrom(Herbonautes.get().mailsFrom);
			send(login);
		} catch (Exception e) {
			Logger.error(e, "Impossible d'envoyer le mail good bye à %s", login);
		}
	}

	
	public static void missionAnnouncement(Mission mission, Announcement announcement) {
		final String subject = Messages.get("mail.announcement.subject", mission.getTitle());
		final String from = Herbonautes.get().mailsFrom;
		final String template = "missionAnnouncement";
		
		final Map<String, Object> args = new HashMap<String, Object>();
		args.put("mission", mission);
		args.put("announcement", announcement);
		
		for (User user : mission.getUsers()) {
			if (!user.isReceiveMails()) {
				continue;
			}
			try {
				String to = user.getEmail();
				
				sendUnique(from, to, subject, template, args);
				
				/*setSubject(Messages.get("mail.announcement.subject", mission.getTitle()));
				addRecipient(user.getEmail());
				setFrom(Herbonautes.get().mailsFrom);
				sendAndWait(mission, announcement);*/
			} catch (Exception e) {
				Logger.error(e, "Impossible d'envoyer le mail à %s (%s)", user.getLogin(), user.getEmail());
			}
		}
		
	}
	
	public static void welcomeToMission(User user, Mission mission) {
		if (!user.isReceiveMails()) {
			return;
		}
		
		try {
			setSubject(Messages.get("mail.welcome.mission.subject", user.getLogin()));
			addRecipient(user.getEmail());
			setFrom(Herbonautes.get().mailsFrom);
			send(user, mission);
		} catch (Exception e) {
			Logger.error(e, "Impossible d'envoyer le mail à %s", user.getLogin());
		}
	}
	
	
	public static void alerts(User user, List<Alert> alerts) {
		
		if (!user.isReceiveMails()) {
			Logger.info("No mail for %s", user.getLogin());
			return;
		}
		
		Logger.info("Envoi des notifications par mail pour %s", user.getLogin());
		try {
			setSubject(Messages.get("mail.alerts.subject", user.getLogin()));
			addRecipient(user.getEmail());
			setFrom(Herbonautes.get().mailsFrom);
			sendAndWait(user, alerts);
		} catch (Exception e) {
			Logger.error(e, "Impossible d'envoyer le mail à %s", user.getLogin());
		} 
	}
	
	public static void sendPassword(User user) {
		Logger.info("Envoi des notifications par mail pour %s", user.getLogin());
		try {
			setSubject(Messages.get("mail.password.subject", user.getLogin()));
			addRecipient(user.getEmail());
			setFrom(Herbonautes.get().mailsFrom);
			sendAndWait(user);
		} catch (Exception e) {
			Logger.error(e, "Immpossible d'envoyer le mail à %s", user.getLogin());
		}
	}
	
	public static void message(User from, User to, String message) {
		Logger.info("Envoi d'un message de %s pour %s", from.getLogin(), to.getLogin());
		try {
			setSubject(Messages.get("mail.message.subject", from.getLogin()));
			addRecipient(to.getEmail());
			setFrom(Herbonautes.get().mailsFrom);
			setReplyTo(from.getEmail());
			send(from, to, message);
		} catch (Exception e) {
			Logger.error(e, "Immpossible d'envoyer le mail à %s <%s>", to.getLogin(), to.getEmail());
		}
	}
	
	/**
	 * Envoi sans thread local (pour mail unique)
	 * @throws EmailException 
	 */
	private static void sendUnique(String from, String to, String subject, String template, Map<String, Object> args) 
	throws EmailException {
        
		Template templateHtml = TemplateLoader.load("Mails/" + template + ".html");
		String bodyHtml = templateHtml.render(args);

		HtmlEmail email = new HtmlEmail();
		email.setHtmlMsg(bodyHtml);
		
		email.setCharset("utf-8");

        if (from != null) {
            try {
                InternetAddress iAddress = new InternetAddress(from.toString());
                email.setFrom(iAddress.getAddress(), iAddress.getPersonal());
            } catch (Exception e) {
                email.setFrom(from.toString());
            }
        }

        try {
            InternetAddress iAddress = new InternetAddress(to.toString());
            email.addTo(iAddress.getAddress(), iAddress.getPersonal());
        } catch (Exception e) {
            email.addTo(to.toString());
        }
        
        email.setSubject(subject);
        email.updateContentType("text/html");

        Mail.send(email);
	}

	public static void mailAfterPostedMessage(Discussion d, User author) {
		String subject = null;
		if(d.getTitle() == null) {
			subject = Messages.get("mail.discussion.new.message.subject.without.title");
		} else {
			subject = Messages.get("mail.discussion.new.message.subject.with.title", d.getTitle());
		}

		final String from = Herbonautes.get().mailsFrom;
		final String template = "discussionResponse";

		final Map<String, Object> args = new HashMap<String, Object>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", d.getId());
		String url = Play.configuration.getProperty("application.mailUrl");
		if(url.endsWith("/"))
			url = url.substring(0, url.length() - 1);
		url += Router.reverse("DiscussionController.show", map).url;

		args.put("discussion", d);


		List<Message> messagesByDiscussionId = Message.getMessagesByDiscussionId(d.id);

		int nMessagesMax = Herbonautes.get().lastMessagesMaxResults;
		args.put("nMessagesMax", nMessagesMax);
		args.put("nMessages", messagesByDiscussionId.size());

		if(null != messagesByDiscussionId && ! messagesByDiscussionId.isEmpty()){
			List<Message> discussionmessages= new ArrayList<Message>();
			if(messagesByDiscussionId.size()>nMessagesMax){
				discussionmessages= messagesByDiscussionId.subList(messagesByDiscussionId.size() - nMessagesMax, messagesByDiscussionId.size());
				discussionmessages.add(0, messagesByDiscussionId.get(0));
			}
			else{
				discussionmessages= messagesByDiscussionId;
			}

			args.put("discussionMessages", discussionmessages);

			for(Message m : discussionmessages){
				System.out.println(m.getText()+ " : "+m.isFirst());
			}
		}
		
		args.put("url", url);

		Set<User> users = new HashSet<User>();
		users.addAll(User.getUsersByDiscussionId(d.getId()));
		List<Tag> tags = Tag.findByDiscussionId(d.getId());
		users.addAll(User.getUsersBySubscribedTags(tags));

		args.put("tags", tags);


		Tag uniqueContextTag = null;
		for (Tag tag : tags) {
			if (tag.getTagType() == TagType.SPECIMEN || tag.getTagType() == TagType.MISSION) {
				if (uniqueContextTag == null) {
					uniqueContextTag = tag;
				} else {
					// On a trouvé 2, on annule et on sort
					uniqueContextTag = null;
					break;
				}
			}
		}

		Logger.info("Unique context tag : " + (uniqueContextTag != null ? uniqueContextTag.getTagLabel() : "<null>"));

		args.put("uniqueContextTag", uniqueContextTag);

		if (uniqueContextTag != null && uniqueContextTag.getTagType() == TagType.SPECIMEN) {
			Specimen specimen = Specimen.findById(uniqueContextTag.getTargetId());
			args.put("specimen", specimen);
		} else if (uniqueContextTag != null && uniqueContextTag.getTagType() == TagType.MISSION) {
			Mission mission = Mission.findById(uniqueContextTag.getTargetId());
			args.put("mission", mission);
		}

		for (User user : users) {
			if (!user.isReceiveMails() || author.getId().equals(user.getId())) {
				continue;
			}
			try {
				String to = user.getEmail();
				sendUnique(from, to, subject, template, args);
			} catch (Exception e) {
				Logger.error(e, "Impossible d'envoyer le mail à %s (%s)", user.getLogin(), user.getEmail());
			}
		}
	}

	public static void mailAfterProposedMission(Mission m) {
		final String subject = Messages.get("mail.mission.proposition.subject");
		final String from = Herbonautes.get().mailsFrom;
		final String template = "missionProposition";

		final Map<String, Object> args = new HashMap<String, Object>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", m.getId());
		String url = Play.configuration.getProperty("application.mailUrl");
		if(url.endsWith("/"))
			url = url.substring(0, url.length() - 1);
		url += Router.reverse("Missions.displayPropositionValidationForm", map).url;
		args.put("login", m.getLeader().getLogin());
		args.put("url", url);
		List<User> users = User.findTeamUsers();

		for (User user : users) {
			try {
				String to = user.getEmail();
				sendUnique(from, to, subject, template, args);
			} catch (Exception e) {
				Logger.error(e, "Impossible d'envoyer le mail à %s (%s)", user.getLogin(), user.getEmail());
			}
		}
	}

	public static void mailAfterProposedMissionValidation(User leader, Mission m) {
		final String subject = Messages.get("mail.mission.proposition.validated", m.getTitle());
		final String from = Herbonautes.get().mailsFrom;
		final String template = "missionValidation";

		final Map<String, Object> args = new HashMap<String, Object>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", m.getId());
		String url = Play.configuration.getProperty("application.mailUrl");
		if(url.endsWith("/"))
			url = url.substring(0, url.length() - 1);
		url += Router.reverse("Missions.show", map).url;
		args.put("login", m.getValidator().getLogin());
		args.put("url", url);
		args.put("mission", m);
		try {
			String to = leader.getEmail();
			sendUnique(from, to, subject, template, args);
		} catch (Exception e) {
			Logger.error(e, "Impossible d'envoyer le mail à %s (%s)", leader.getLogin(), leader.getEmail());
		}

	}

}
