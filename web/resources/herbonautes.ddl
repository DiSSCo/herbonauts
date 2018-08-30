
    drop table H_ACTIVITY cascade constraints;

    drop table H_ALERT cascade constraints;

    drop table H_ANNOUNCEMENT cascade constraints;

    drop table H_BADGE cascade constraints;

    drop table H_BOTANIST cascade constraints;

    drop table H_CHOICE cascade constraints;

    drop table H_COMMENT cascade constraints;

    drop table H_CONTENT cascade constraints;

    drop table H_CONTRIBUTION cascade constraints;

    drop table H_CONTRIBUTION_REPORT cascade constraints;

    drop table H_COUNTRY cascade constraints;

    drop table H_IMAGE cascade constraints;

    drop table H_LINK cascade constraints;

    drop table H_MISSION cascade constraints;

    drop table H_QUESTION cascade constraints;

    drop table H_QUIZ cascade constraints;

    drop table H_REGION_LEVEL_1 cascade constraints;

    drop table H_REGION_LEVEL_2 cascade constraints;

    drop table H_SPECIMEN cascade constraints;

    drop table H_USER cascade constraints;

    drop table contribution_collectors cascade constraints;

    drop table mission_banned_user cascade constraints;

    drop table mission_specimen cascade constraints;

    drop table mission_user cascade constraints;

    drop sequence hibernate_sequence;

    create table H_ACTIVITY (
        type varchar2(31 char) not null,
        id number(19,0) not null,
        CREATION_DATE timestamp,
        mission_id number(19,0),
        user_id number(19,0),
        contribution_id number(19,0),
        specimen_id number(19,0),
        badge_id number(19,0),
        primary key (id)
    );

    create table H_ALERT (
        type varchar2(31 char) not null,
        id number(19,0) not null,
        CREATION_DATE timestamp,
        emailSent number(1,0) not null,
        userRead number(1,0) not null,
        user_id number(19,0),
        mission_id number(19,0),
        specimenComment_id number(19,0),
        contribution_id number(19,0),
        missionComment_id number(19,0),
        primary key (id)
    );

    create table H_ANNOUNCEMENT (
        id number(19,0) not null,
        PUBLICATION_DATE timestamp,
        published number(1,0) not null,
        text varchar2(255 char),
        title varchar2(255 char),
        mission_id number(19,0),
        primary key (id)
    );

    create table H_BADGE (
        type varchar2(31 char) not null,
        id number(19,0) not null,
        user_id number(19,0),
        primary key (id)
    );

    create table H_BOTANIST (
        id number(19,0) not null,
        countries CLOB,
        createdByUser number(1,0) not null,
        harvardId number(19,0),
        hasImage number(1,0) not null,
        herborariumIndex varchar2(255 char),
        imageId number(19,0),
        name varchar2(255 char),
        nameInv varchar2(255 char),
        period varchar2(255 char),
        speciality CLOB,
        primary key (id)
    );

    create table H_CHOICE (
        id number(19,0) not null,
        answer number(1,0) not null,
        text varchar2(255 char),
        question_id number(19,0),
        primary key (id)
    );

    create table H_COMMENT (
        type varchar2(31 char) not null,
        id number(19,0) not null,
        CREATION_DATE timestamp,
        text VARCHAR2(1000 char),
        user_id number(19,0),
        specimen_id number(19,0),
        mission_id number(19,0),
        primary key (id)
    );

    create table H_CONTENT (
        id number(19,0) not null,
        lang varchar2(255 char),
        name varchar2(255 char),
        text CLOB,
        title varchar2(255 char),
        url varchar2(255 char),
        primary key (id)
    );

    create table H_CONTRIBUTION (
        type varchar2(31 char) not null,
        id number(19,0) not null,
        canceled number(1,0) not null,
        CREATION_DATE timestamp,
        deducted number(1,0) not null,
        notPresent number(1,0) not null,
        notReadable number(1,0) not null,
        notSure number(1,0) not null,
        report number(1,0) not null,
        validatedFromOther number(1,0) not null,
        collectorNotPresent number(1,0),
        collectorNotSure number(1,0),
        determinerNotPresent number(1,0),
        determinerNotSure number(1,0),
        collectDate timestamp,
        collectEndDate timestamp,
        collectStartDate timestamp,
        period number(1,0),
        locality varchar2(255 char),
        cause number(10,0),
        latitude float,
        longitude float,
        mission_id number(19,0),
        specimen_id number(19,0),
        user_id number(19,0),
        regionLevel1_id number(19,0),
        collector_id number(19,0),
        determiner_id number(19,0),
        regionLevel2_id number(19,0),
        country_id number(19,0),
        H_PRECISION varchar2(255 char),
        primary key (id)
    );

    create table H_CONTRIBUTION_REPORT (
        type varchar2(31 char) not null,
        id number(19,0) not null,
        complete number(1,0) not null,
        conflicts number(1,0) not null,
        count number(10,0),
        lastModified timestamp,
        specimen_id number(19,0),
        validatedContribution_id number(19,0),
        primary key (id)
    );

    create table H_COUNTRY (
        id number(19,0) not null,
        iso varchar2(255 char),
        name varchar2(255 char),
        primary key (id)
    );

    create table H_IMAGE (
        id number(19,0) not null,
        data blob,
        mimeType varchar2(255 char),
        primary key (id)
    );

    create table H_LINK (
        id number(19,0) not null,
        lang varchar2(255 char),
        title varchar2(255 char),
        url varchar2(255 char),
        primary key (id)
    );

    create table H_MISSION (
        id number(19,0) not null,
        bigImageId number(19,0),
        closed number(1,0) not null,
        goal number(10,0),
        hasBigImage number(1,0) not null,
        hasImage number(1,0) not null,
        imageId number(19,0),
        lang varchar2(255 char),
        loading number(1,0) not null,
        openingDate timestamp,
        presentation CLOB,
        priority number(10,0),
        published number(1,0) not null,
        report CLOB,
        reportPublished number(1,0) not null,
        shortDescription VARCHAR(1000 char),
        title VARCHAR(1000),
        country_id number(19,0),
        leader_id number(19,0),
        primary key (id)
    );

    create table H_QUESTION (
        id number(19,0) not null,
        answerDetails VARCHAR2(1000 char),
        hasImage number(1,0) not null,
        imageId number(19,0),
        sortIndex number(10,0),
        text varchar2(255 char),
        quiz_id number(19,0),
        primary key (id)
    );

    create table H_QUIZ (
        id number(19,0) not null,
        description CLOB,
        congratulations CLOB,
        lang varchar2(255 char),
        name varchar2(255 char),
        title varchar2(255 char),
        unlockingLevel number(10,0) not null,
        primary key (id)
    );


    CREATE TABLE H_QUIZ_QUESTION
(
    ID number(19,0) NOT NULL,
    ANSWERDETAILS VARCHAR(1000),
    HASIMAGE number(1,0) NOT NULL,
    IMAGEID number(19,0),
    SORTINDEX number(19,0),
    TEXT VARCHAR(255),
    QUIZ_ID number(19,0),
    primary key (id)
);


    create table H_REGION_LEVEL_1 (
        id number(19,0) not null,
        name varchar2(255 char),
        country_id number(19,0),
        primary key (id)
    );

    create table H_REGION_LEVEL_2 (
        id number(19,0) not null,
        name varchar2(255 char),
        region1_id number(19,0),
        primary key (id)
    );

    create table H_SPECIMEN (
        id number(19,0) not null,
        code varchar2(255 char),
        collection varchar2(255 char),
        complete number(1,0) not null,
        displayed number(1,0) not null,
        family varchar2(255 char),
        firstDiplayed timestamp,
        genus varchar2(255 char),
        institute varchar2(255 char),
        lastModified timestamp,
        sonneratURL varchar2(255 char),
        tileHeight number(10,0),
        tileWidth number(10,0),
        tiled number(1,0) not null,
        tilesBaseURL varchar2(255 char),
        tilingError number(1,0) not null,
        tropicosURL varchar2(255 char),
        unusable number(1,0) not null,
        primary key (id)
    );

    create table H_USER (
        id number(19,0) not null,
        address varchar2(255 char),
        admin number(1,0) not null,
        birthDate timestamp,
        city varchar2(255 char),
        country raw(255),
        description varchar2(255 char),
        email varchar2(255 char),
        facebookId varchar2(255 char),
        facebookUsername varchar2(255 char),
        firstName varchar2(255 char),
        hasImage number(1,0) not null,
        image varchar2(255 char),
        imageId number(19,0),
        lastName varchar2(255 char),
        leader number(1,0) not null,
        CURRENT_LEVEL number(10,0),
        login varchar2(255 char),
        password varchar2(255 char),
        PENDING_LEVEL number(10,0),
        publishInformations number(1,0) not null,
        receiveMails number(1,0) not null,
        registrationDate timestamp,
        primary key (id)
    );

    create table contribution_collectors (
        contribution_id number(19,0) not null,
        botanist_id number(19,0) not null
    );

    create table mission_banned_user (
        mission_id number(19,0) not null,
        user_id number(19,0) not null
    );

    create table mission_specimen (
        specimen_id number(19,0) not null,
        mission_id number(19,0) not null
    );

    create table mission_user (
        user_id number(19,0) not null,
        mission_id number(19,0) not null
    );

    alter table H_ACTIVITY 
        add constraint FK85FEE5268AD45980 
        foreign key (badge_id) 
        references H_BADGE;

    alter table H_ACTIVITY 
        add constraint FK85FEE526FB39F789 
        foreign key (contribution_id) 
        references H_CONTRIBUTION;

    alter table H_ACTIVITY 
        add constraint FK85FEE526FC7D37DE 
        foreign key (specimen_id) 
        references H_SPECIMEN;

    alter table H_ACTIVITY 
        add constraint FK85FEE52647140EFE 
        foreign key (user_id) 
        references H_USER;

    alter table H_ACTIVITY 
        add constraint FK85FEE526476B7E96 
        foreign key (mission_id) 
        references H_MISSION;

    alter table H_ALERT 
        add constraint FK86949E85FB39F789 
        foreign key (contribution_id) 
        references H_CONTRIBUTION;

    alter table H_ALERT 
        add constraint FK86949E85D7DF3C90 
        foreign key (missionComment_id) 
        references H_COMMENT;

    alter table H_ALERT 
        add constraint FK86949E85923C7924 
        foreign key (specimenComment_id) 
        references H_COMMENT;

    alter table H_ALERT 
        add constraint FK86949E8547140EFE 
        foreign key (user_id) 
        references H_USER;

    alter table H_ALERT 
        add constraint FK86949E85476B7E96 
        foreign key (mission_id) 
        references H_MISSION;

    alter table H_ANNOUNCEMENT 
        add constraint FK9C7753DE476B7E96 
        foreign key (mission_id) 
        references H_MISSION;

    alter table H_BADGE 
        add constraint FK869DB0CC47140EFE 
        foreign key (user_id) 
        references H_USER;

    alter table H_CHOICE 
        add constraint FK4F34ED786AB4470F 
        foreign key (question_id) 
        references H_QUESTION;

    alter table H_COMMENT 
        add constraint FKA3405948FC7D37DE 
        foreign key (specimen_id) 
        references H_SPECIMEN;

    alter table H_COMMENT 
        add constraint FKA340594847140EFE 
        foreign key (user_id) 
        references H_USER;

    alter table H_COMMENT 
        add constraint FKA3405948476B7E96 
        foreign key (mission_id) 
        references H_MISSION;

    alter table H_CONTRIBUTION 
        add constraint FKE522AAA77CD8C907 
        foreign key (collector_id) 
        references H_BOTANIST;

    alter table H_CONTRIBUTION 
        add constraint FKE522AAA7FC7D37DE 
        foreign key (specimen_id) 
        references H_SPECIMEN;

    alter table H_CONTRIBUTION 
        add constraint FKE522AAA79B84A56 
        foreign key (country_id) 
        references H_COUNTRY;

    alter table H_CONTRIBUTION 
        add constraint FKE522AAA786F283BE 
        foreign key (regionLevel2_id) 
        references H_REGION_LEVEL_2;

    alter table H_CONTRIBUTION 
        add constraint FKE522AAA7DD1F69D5 
        foreign key (determiner_id) 
        references H_BOTANIST;

    alter table H_CONTRIBUTION 
        add constraint FKE522AAA747140EFE 
        foreign key (user_id) 
        references H_USER;

    alter table H_CONTRIBUTION 
        add constraint FKE522AAA7476B7E96 
        foreign key (mission_id) 
        references H_MISSION;

    alter table H_CONTRIBUTION 
        add constraint FKE522AAA786F20F5E 
        foreign key (regionLevel1_id) 
        references H_REGION_LEVEL_1;

    alter table H_CONTRIBUTION_REPORT 
        add constraint FKED9DA1CCFC7D37DE 
        foreign key (specimen_id) 
        references H_SPECIMEN;

    alter table H_MISSION 
        add constraint FKAA58EC7594344500 
        foreign key (leader_id) 
        references H_USER;

    alter table H_MISSION 
        add constraint FKAA58EC759B84A56 
        foreign key (country_id) 
        references H_COUNTRY;

    alter table H_QUESTION 
        add constraint FKA3352DFDF525CF2F 
        foreign key (quiz_id) 
        references H_QUIZ;

    alter table H_REGION_LEVEL_1 
        add constraint FK4E51C8A29B84A56 
        foreign key (country_id) 
        references H_COUNTRY;

    alter table H_REGION_LEVEL_2 
        add constraint FK4E51C8A3AD82F742 
        foreign key (region1_id) 
        references H_REGION_LEVEL_1;

    alter table contribution_collectors 
        add constraint FK803C4D156B1974C8 
        foreign key (contribution_id) 
        references H_CONTRIBUTION;

    alter table contribution_collectors 
        add constraint FK803C4D1565C5D1E 
        foreign key (botanist_id) 
        references H_BOTANIST;

    alter table mission_banned_user 
        add constraint FK5A0DD61947140EFE 
        foreign key (user_id) 
        references H_USER;

    alter table mission_banned_user 
        add constraint FK5A0DD619476B7E96 
        foreign key (mission_id) 
        references H_MISSION;

    alter table mission_specimen 
        add constraint FKB670B05BFC7D37DE 
        foreign key (specimen_id) 
        references H_SPECIMEN;

    alter table mission_specimen 
        add constraint FKB670B05B476B7E96 
        foreign key (mission_id) 
        references H_MISSION;

    alter table mission_user 
        add constraint FKFC823A9E47140EFE 
        foreign key (user_id) 
        references H_USER;

    alter table mission_user 
        add constraint FKFC823A9E476B7E96 
        foreign key (mission_id) 
        references H_MISSION;

    create sequence hibernate_sequence;
