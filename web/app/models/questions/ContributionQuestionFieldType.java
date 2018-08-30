package models.questions;

import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

public enum ContributionQuestionFieldType {

    REFERENCE,
    TEXT,
    CHECKBOX,
    LIST,
    DATE,
    PERIOD,
    GEO;



}
