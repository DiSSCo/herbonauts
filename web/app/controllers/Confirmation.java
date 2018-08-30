package controllers;

import controllers.modules.cas.SecureCAS;
import inspectors.Event;
import inspectors.InspectorChain;
import models.EmailConfirmation;
import models.User;
import models.UserInvitation;
import play.data.validation.Validation;
import play.db.jpa.Transactional;
import play.i18n.Messages;

import java.util.Arrays;
import java.util.List;

/**
 * Confirmation email
 */
public class Confirmation extends Application {

    public static void validate(String token) {

        EmailConfirmation confirmation = EmailConfirmation.findByToken(token);

        if (confirmation == null) {
            if (!Security.isConnected() || Security.connectedUser().id != confirmation.getUser().id) {
                renderTemplate("Invitation/invalidToken.html");
            }
        }


        // Change email
        User savedUser = User.findByLogin(confirmation.getUser().getLogin(), false);
        savedUser.setEmail(confirmation.getEmail());
        savedUser.save();

        flash.success(Messages.get("message.email.change.success"));



        Users.show(confirmation.getUser().getLogin());
    }



}