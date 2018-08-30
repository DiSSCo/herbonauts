package controllers;

import helpers.RecolnatAPIClient;
import helpers.RecolnatUser;
import inspectors.Event;
import inspectors.InspectorChain;
import models.Image;
import models.User;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.cache.Cache;
import play.modules.cas.models.CASUser;
import play.mvc.Scope;

public class SecurityCas extends controllers.modules.cas.Security  {

    public static boolean check(String profile) {
        Logger.debug("[MySecurity]: check :" + profile);
        return profile.equals("role1");
    }

    public static void onAuthenticated(CASUser user) {
        Logger.debug("[MySecurity]: onAutenticated method");

        Logger.info("CAS USER username = %s (update info)", user.getUsername());
        RecolnatUser recolnatUser = RecolnatAPIClient.getUserByLogin(user.getUsername());

        Logger.info("Recolnat user : " + recolnatUser.login);

        // Connect
        User huser = User.findByLogin(user.getUsername(), false);


        if (huser == null) {
            Logger.info("Nouvel utilisateur ! %s", user.getUsername());
            huser = User.createFromRecolnat(recolnatUser);
        } else {


            if (!StringUtils.equals(huser.getFirstName(), recolnatUser.firstname) ||
                    !StringUtils.equals(huser.getLastName(), recolnatUser.lastname)) {
                huser.setFirstName(recolnatUser.firstname);
                huser.setLastName(recolnatUser.lastname);
                huser.save();
                huser.refresh();
                Logger.info("Mise à jour utilisateur %s", user.getUsername());
            }

        }


        if (recolnatUser != null) {
            Logger.info("Infos Recolnat");
            Logger.info("  name: %s %s", recolnatUser.firstname, recolnatUser.lastname);
            Logger.info("  avatar : %s %s", recolnatUser.login, recolnatUser.avatar);
        } else {
            Logger.info("Recolnat user null");

        }
        if (recolnatUser.avatar != null) {
            Logger.info("  - avatar id : " + recolnatUser.avatar.id);

            // Update Avatar

            String base64Avatar = recolnatUser.avatar.data;

            byte[] decoded = Base64.decodeBase64(base64Avatar);
            Image imageDB = Image.createImageFromBytes(decoded);

            //user.image = image;
            huser.setImageId(imageDB.id);
            huser.setHasImage(true);
            huser.save();
            huser.refresh();
        }

        Security.connect(huser);
        InspectorChain.get().inspect(Event.USER_CONNECT, Security.connectedUser());
        Cache.set("user_" + Scope.Session.current().get("username"), recolnatUser);
    }

    public static void onDisconnected() {
        Logger.debug("[MySecurity]: onAutenticated method");
        Cache.delete(Scope.Session.current().get("username"));
        Scope.Session.current().clear();
    }

    public static RecolnatUser connected() {
        Logger.debug("[MySecurity]: onAutenticated method");
        return (RecolnatUser) Cache.get("user_" + Scope.Session.current().get("username"));
    }

}