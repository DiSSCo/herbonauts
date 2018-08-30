package controllers;

import controllers.modules.cas.SecureCAS;
import inspectors.Event;
import inspectors.InspectorChain;
import models.UserInvitation;
import models.User;

import play.data.validation.Validation;
import play.db.jpa.Transactional;


import java.util.Arrays;
import java.util.List;

/**
 * Parrainage
 */
public class Invitation extends Application {

	public static void form() {
		render();
	}

    @Transactional
    public static void send(String emails) {

        List<String> emailList = Arrays.asList(emails.split("\n"));

        for (String email : emailList) {
            if (!Validation.email("email", email.trim()).ok) {
                flash.error(play.i18n.Messages.get("message.email.invalid", email));
                flash.put("emails", email);
                Invitation.form();
            }
        }

        User from = Security.connectedUser();

        for (String email : emailList) {
            UserInvitation.createAndSend(from, email.trim());
        }

        flash.success(play.i18n.Messages.get("message.invitation.sent"));
        Application.index();

    }

    public static void validate(String token) {

        UserInvitation invitation = UserInvitation.findByToken(token);

        if (invitation == null) {
            renderTemplate("Invitation/invalidToken.html");
        }

        response.setCookie("invitation", invitation.getToken());

        render(invitation);
    }

    public static void sign(String token) throws Throwable {
        // redirect to CAS & CREATE BADGE
        UserInvitation invitation = UserInvitation.findByToken(token);

        if (invitation == null) {
            renderTemplate("Invitation/invalidToken.html");
        }

        InspectorChain.get().inspect(Event.USER_INVITATION, invitation);

        SecureCAS.login();
    }


}