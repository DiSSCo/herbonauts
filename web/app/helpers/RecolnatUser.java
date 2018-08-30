package helpers;

import java.io.Serializable;

public class RecolnatUser implements Serializable {

    public Long id;
    public String user_uuid;
    public String login;
    public String email;
    public String firstname;
    public String lastname;
    public Boolean has_avatar;
    public Avatar avatar;
    public String password;
    public String currentpassword;

    public static class Avatar implements Serializable  {
        public Long id;
        public String data;
    }

}


