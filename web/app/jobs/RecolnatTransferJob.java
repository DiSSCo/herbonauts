package jobs;

import com.fasterxml.jackson.databind.JsonNode;
import conf.Herbonautes;
import libs.Json;
import models.Mission;
import models.Specimen;
import models.questions.ContributionAnswer;
import models.questions.ContributionQuestionStat;
import models.references.ReferenceRecord;

import models.recolnat.*;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.db.jpa.NoTransaction;
import play.jobs.Job;

import java.util.*;


@NoTransaction
public class RecolnatTransferJob extends Job {

    private Long missionId;
    private List<? extends FieldTransfer> transfers = Arrays.asList(
            new CountryFieldTransfer(),
            new RegionFieldTransfer(),
            new DateFieldTransfer(),
            new CollectFieldTransfer(),
            new IdentifiedFieldTransfer(),
            new LocalityFieldTransfer(),
            new GeolocalisationFieldTransfer(),
            new CollectNumberFieldTransfer(),
            new UnusableFieldTransfer()
    );

    //


    public RecolnatTransferJob(Long missionId) {
        this.missionId = missionId;
    }

    //

    private static RecolnatDetermination findValidDetermination(RecolnatSpecimen recolnatSpecimen, Specimen specimen) throws NoTransferException {
        RecolnatDetermination validDetermination = null;
        if (recolnatSpecimen !=null && recolnatSpecimen.determinations != null) {
            for (RecolnatDetermination det : recolnatSpecimen.determinations) {
                if (det.verifStatus == Boolean.TRUE) {
                    if (validDetermination != null) {
                        throw new NoTransferException(specimen, NoTransferCause.MULTIPLE_DETERMINATIONS, "identified");
                    }
                    validDetermination = det;
                }
            }
        }
        return validDetermination;
    }

    @Override
    public void doJob() throws Exception {

        int count = 0;

        Mission mission = null;

        boolean debugNoRecolnat = "true".equals(Play.configuration.getProperty("herbonautes.no.recolnat.transfer.debug", "false"));





        List<Specimen> specimens = null;
        try {

            JPA.startTx("default", true);

            mission = Mission.findById(this.missionId);

            Logger.info("---------------------------------------------------------------------------------");
            Logger.info("Versement Recolnat pour la mission %s", mission.getTitle());
            if (debugNoRecolnat) {
                Logger.info("  DEBUG MODE");
            }
            Logger.info("---------------------------------------------------------------------------------");

            specimens = Specimen.find("mission.id = ?", this.missionId).fetch();

            Logger.info("Lock mission");
            mission.recolnatTransferInProgress = true;
            mission.save();

            JPA.closeTx("default");

        } catch (Throwable t) {
            JPA.rollbackTx("default");
        }


        Logger.info("%d specimens", specimens == null ? -1 : specimens.size());
        Logger.info("---------------------------------------------------------------------------------", specimens.size());

        for (Specimen specimen : specimens) {
            //List<ContributionAnswer> answerForSpecimen = ContributionAnswer.findAllValidAnswersForSpecimen(specimen.id);

            Logger.info("Specimen %s", specimen.getCode());

            RecolnatSpecimen recolnatSpecimen = null;

            boolean forceValuesVsUnusable = true;

            try {

                Long unusableCount = 0l;
                Long usableCount = 0l;

                JPA.startTx("recolnat", false);

                if (debugNoRecolnat) {
                    recolnatSpecimen = new RecolnatSpecimen();
                    recolnatSpecimen.recolte = new RecolnatRecolte();
                    recolnatSpecimen.determinations = new ArrayList<RecolnatDetermination>();

                    RecolnatDetermination det1 = new RecolnatDetermination();
                    det1.verifStatus = true;
                    det1.identifiedBy = "Already";
                    recolnatSpecimen.determinations.add(det1);

                    RecolnatDetermination det2 = new RecolnatDetermination();
                    det2.verifStatus = false;
                    recolnatSpecimen.determinations.add(det2);

                } else {
                    recolnatSpecimen = RecolnatSpecimen.find("catalogNumber = ?", specimen.getCode()).first();
                }


                Logger.info("");

                if (recolnatSpecimen != null) {

                    //Map<String, ContributionAnswer> answersByQuestionName = ContributionAnswer.findAllValidAnswersForSpecimenByName(specimen.id);
                    Map<String, ContributionAnswer> answersByQuestionName = null;

                    try {

                        JPA.startTx("default", true);

                        // Counts
                        List<ContributionQuestionStat> stats = ContributionQuestionStat.find("specimenId = ?", specimen.id).fetch();
                        for (ContributionQuestionStat stat : stats) {
                            if ("unusable".equals(stat.getQuestion().getName())) {
                                unusableCount += stat.getAnswerCount();
                            } else {
                                usableCount += stat.getAnswerCount();
                            }
                        }
                        Logger.info("Counts : inutilisable : %d - utilisable : %s", unusableCount, usableCount);
                        forceValuesVsUnusable = (usableCount >= unusableCount);
                        Logger.info("Forces values : " + forceValuesVsUnusable);

                        answersByQuestionName = ContributionAnswer.findAllTransferableAnswersForSpecimenByName(specimen.id);

                        for (Map.Entry<String, ContributionAnswer> anwser : answersByQuestionName.entrySet()) {
                            Logger.info("- %s : %s", anwser.getKey(), anwser.getValue().getJsonValue());

                            for (Map.Entry<String, String> e : anwser.getValue().toExportableValue().entrySet()) {
                                Logger.info("  - %s : %s", e.getKey(), e.getValue());
                            }


                        }

                        JPA.closeTx("default");

                    } catch (Throwable t) {
                        JPA.rollbackTx("default");
                    }

                    List<RecolnatOperation> operations = new ArrayList<RecolnatOperation>();

                    for (FieldTransfer transfer : transfers) {
                        List<RecolnatOperation> transferOps =null;
                        try {
                            Logger.info("  Do transfer %s (specimen %s)", transfer.getName(), specimen.getCode());

                            if (("unusable".equals(transfer.getName()) && !forceValuesVsUnusable) ||
                                    (!"unusable".equals(transfer.getName()) && forceValuesVsUnusable)) {
                                transferOps = transfer.doTransfer(recolnatSpecimen, specimen, answersByQuestionName);
                            } else {
                                Logger.info("  Transfer %s ignored : forceValuesVsUnusable=" + forceValuesVsUnusable, transfer.getName());
                            }
                        } catch (NoTransferException e) {
                            Logger.info("  No %s transfer : %s", transfer.getName(), e.getNoTransferCause());

                            try {

                                JPA.startTx("default", false);

                                RecolnatTransfer transferFail =
                                        RecolnatTransfer.buildtransferFail(specimen.id, transfer.getName(), e.getNoTransferCause());
                                transferFail.save();

                                JPA.closeTx("default");

                            } catch (Throwable t) {
                                JPA.rollbackTx("default");
                            }


                            continue;
                        } catch (Exception e) {
                            //Logger.error("Unknown exception", e);
                            Logger.error(e, "Unknown exception");
                            continue;
                        }

                        if (transferOps != null && transferOps.size() > 0) {

                            try {

                                JPA.startTx("default", false);

                                RecolnatTransfer transferSuccess =
                                        RecolnatTransfer.buildtransferSuccess(specimen.id, transfer.getName());
                                transferSuccess.save();

                                JPA.closeTx("default");

                            } catch (Throwable t) {
                                JPA.rollbackTx("default");
                            }


                            Logger.info("  %d %s tranfer operations", transferOps.size(), transfer.getName());
                            operations.addAll(transferOps);
                        }


                    }

                    if (operations.size() > 0) {
                        Logger.info("---> %d operations to persist", operations.size());

                        //Logger.info("   new country code : " + recolnatSpecimen.recolte.location.countryCode);

                        //JPA.em("recolnat").persist(recolnatSpecimen);


                        if (!debugNoRecolnat) {

                            for (RecolnatOperation op : operations) {

                                if (RecolnatOperation.TABLE_SPECIMENS.equals(op.tableName)) {
                                    recolnatSpecimen.modified = new Date();
                                } else if (RecolnatOperation.TABLE_RECOLTES.equals(op.tableName)) {
                                    recolnatSpecimen.recolte.modified = new Date();
                                } else if (RecolnatOperation.TABLE_LOCALISATIONS.equals(op.tableName)) {
                                    recolnatSpecimen.recolte.location.modified = new Date();
                                } else if (RecolnatOperation.TABLE_DETERMINATIONS.equals(op.tableName)) {
                                    try {
                                        RecolnatDetermination det = findValidDetermination(recolnatSpecimen, specimen);
                                        //recolnatSpecimen.determination.modified = new Date();
                                        if (det != null) {
                                            det.modified = new Date();
                                        }
                                    } catch (Exception e ) {
                                        Logger.error(e, "");
                                    }
                                }

                                op.save();

                            }

                            recolnatSpecimen.save();

                        } else {
                            Logger.info("Debug, no transfer in db");
                        }


                        //JPA.em("recolnat").flush();
                    }


                } else {
                    Logger.warn("- Recolnat specimen null");
                }

                Logger.info("-----------");

                JPA.closeTx("recolnat");

            } catch (Throwable t) {
                JPA.rollbackTx("recolnat");

                Logger.error(t, "Erreur mise à jour recolnat");

            }


        }



        Logger.info("---------------------------------------------------------------------------------");

        // Mark transfer done


        try {

            JPA.startTx("default", false);

            Logger.info("Update mission");

            mission = Mission.findById(this.missionId);
            mission.recolnatTransferInProgress = false;
            mission.recolnatTransferDone = true;
            mission.recolnatTransferDate = new Date();
            mission.save();

            JPA.closeTx("default");

        } catch (Throwable t) {
            JPA.rollbackTx("default");
        }



        Logger.info("Recolnat transfer done");
        Logger.info("---------------------------------------------------------------------------------");

    }

    public static enum NoTransferCause {
        RECOLNAT_MISSING_ROW,
        HERBONAUTES_NO_DATA,
        CONFLICT_RECOLNAT_HERBONAUTES,
        RECOLNAT_ALREADY_EXISTS,
        MULTIPLE_DETERMINATIONS
    }

    public static interface FieldTransfer {

        List<RecolnatOperation> doTransfer(RecolnatSpecimen recolnatSpecimen, Specimen specimen, Map<String, ContributionAnswer> answersByQuestionName) throws NoTransferException;
        String getName();

    }

    static class NoTransferException extends Exception {

        private Specimen specimen;
        private NoTransferCause noTransferCause;
        private String field;

        public NoTransferException(Specimen specimen, NoTransferCause noTransferCause, String field) {
            super("No transfer");
            this.specimen = specimen;
            this.noTransferCause = noTransferCause;
            this.field = field;
        }

        public Specimen getSpecimen() {
            return specimen;
        }

        public NoTransferCause getNoTransferCause() {
            return noTransferCause;
        }

        public String getField() {
            return field;
        }
    }

    /**
     * COUNTRY TRANSFER
     */

    public static class CountryFieldTransfer implements FieldTransfer {

        private String getCountryCode(ContributionAnswer answer) {
            if (answer == null) {
                return null;
            }
            JsonNode node = Json.parse(answer.getJsonValue());

            if (node.hasNonNull("country")) {
                if (node.get("country").hasNonNull("id")) {
                    Long id = node.get("country").get("id").asLong();
                    ReferenceRecord record = null;



                    try {

                        JPA.startTx("default", true);

                        record = ReferenceRecord.findById(id);

                        JPA.closeTx("default");

                    } catch (Throwable t) {
                        JPA.rollbackTx("default");
                    }

                    if (record != null && record.getValue() != null) {
                        if (record.getValue().toLowerCase().trim().length() > 3) {
                            return record.getValue().toLowerCase().trim().substring(0, 3);
                        } else {
                            return record.getValue().toLowerCase().trim();
                        }
                    }
                }
            }

            return null;
        }

        private String getCountryLabel(ContributionAnswer answer) {
            if (answer == null) {
                return null;
            }
            JsonNode node = Json.parse(answer.getJsonValue());

            if (node.hasNonNull("country")) {
                if (node.get("country").hasNonNull("label")) {
                    String label = node.get("country").get("label").asText();
                    return label;

                }
            }

            return null;
        }

        @Override
        public String getName() {
            return "country";
        }

        @Override
        public List<RecolnatOperation> doTransfer(RecolnatSpecimen recolnatSpecimen,
                                                  Specimen specimen,
                                                  Map<String, ContributionAnswer> answersByQuestionName) throws NoTransferException {

            List<RecolnatOperation> operations = new ArrayList<RecolnatOperation>();


            if (recolnatSpecimen !=null && recolnatSpecimen.recolte != null && recolnatSpecimen.recolte.location != null) {

                String recolnatcountryCode = recolnatSpecimen.recolte.location.countryCode;
                String herbonautesCode = getCountryCode(answersByQuestionName.get("country"));
                String herbonautesLabel = getCountryLabel(answersByQuestionName.get("country"));

                //ContributionAnswer answer = answersByQuestionName.get("country");

                Logger.info("  H: %s   --- R: %s", herbonautesCode, recolnatcountryCode);

                if (recolnatcountryCode != null) {
                    throw new NoTransferException(specimen, NoTransferCause.RECOLNAT_ALREADY_EXISTS, this.getName());
                }

                if (herbonautesCode != null) {
                    recolnatSpecimen.recolte.location.countryCode = herbonautesCode;
                    recolnatSpecimen.recolte.location.country = herbonautesLabel;

                    operations.add(RecolnatOperation.update(specimen, recolnatSpecimen, RecolnatOperation.TABLE_LOCALISATIONS, "COUNTRYCODE"));
                    operations.add(RecolnatOperation.update(specimen, recolnatSpecimen, RecolnatOperation.TABLE_LOCALISATIONS, "COUNTRY"));
                } else {
                    throw new NoTransferException(specimen, NoTransferCause.HERBONAUTES_NO_DATA, this.getName());
                }

            } else {
                throw new NoTransferException(specimen, NoTransferCause.RECOLNAT_MISSING_ROW, this.getName());
            }


            return operations;
        }

    }

    /**
     * Region TRANSFER
     */

    public static class RegionFieldTransfer implements FieldTransfer {

        private String getRegionLabel(ContributionAnswer answer) {
            if (answer == null) {
                return null;
            }
            JsonNode node = Json.parse(answer.getJsonValue());

            if (node.hasNonNull("region")) {
                if (node.get("region").hasNonNull("label")) {
                    String label = node.get("region").get("label").asText();
                    return label;

                }
            }

            return null;
        }

        @Override
        public String getName() {
            return "region";
        }

        @Override
        public List<RecolnatOperation> doTransfer(RecolnatSpecimen recolnatSpecimen,
                                                  Specimen specimen,
                                                  Map<String, ContributionAnswer> answersByQuestionName) throws NoTransferException {

            List<RecolnatOperation> operations = new ArrayList<RecolnatOperation>();


            if (recolnatSpecimen !=null && recolnatSpecimen.recolte != null && recolnatSpecimen.recolte.location != null) {

                String recolnatstateProvince = recolnatSpecimen.recolte.location.stateProvince;
                String herbonautesLabel = getRegionLabel(answersByQuestionName.get("region1"));

                //ContributionAnswer answer = answersByQuestionName.get("country");

                Logger.info("  H: %s   --- R: %s", herbonautesLabel, recolnatstateProvince);

                if (recolnatstateProvince != null) {
                    throw new NoTransferException(specimen, NoTransferCause.RECOLNAT_ALREADY_EXISTS, this.getName());
                }

                if (herbonautesLabel != null) {
                    recolnatSpecimen.recolte.location.stateProvince = herbonautesLabel;

                    operations.add(RecolnatOperation.update(specimen, recolnatSpecimen, RecolnatOperation.TABLE_LOCALISATIONS, "STATEPROVINCE"));
                } else {
                    throw new NoTransferException(specimen, NoTransferCause.HERBONAUTES_NO_DATA, this.getName());
                }

            } else {
                throw new NoTransferException(specimen, NoTransferCause.RECOLNAT_MISSING_ROW, this.getName());
            }

            return operations;
        }

    }

    /**
     * COLLECT DATE TRANSFER
     */

    public static class DateFieldTransfer implements FieldTransfer {


        private static Date offsetDate(Date date) {
            long offset = Herbonautes.get().timezoneOffset.longValue() * 60 * 1000;
            date.setTime(date.getTime() - offset);

            return date;
        }

        private LocalDate getDate(ContributionAnswer answer, String field) {
            if (answer == null) {
                return null;
            }
            JsonNode node = Json.parse(answer.getJsonValue());

            if (node.hasNonNull("collect_date")) {
                if (node.get("collect_date").hasNonNull(field)) {
                    Long ts = node.get("collect_date").get(field).get("ts").asLong();

                    Date d = offsetDate(new Date(ts));

                    return new LocalDate(d);
                }
            }

            return null;
        }

        private boolean isSingleDay(LocalDate start, LocalDate end) {
            return start.equals(end);
        }
        private boolean isMonthPeriod(LocalDate start, LocalDate end) {
            return start.getDayOfMonth() == 1 && start.plusMonths(1).minusDays(1).equals(end);
        }
        private boolean isYearPeriod(LocalDate start, LocalDate end) {
            return start.getDayOfMonth() == 1  && start.getMonthOfYear() == 1 && start.plusYears(1).minusDays(1).equals(end);
        }


        private RecolnatRecolte toRecolte(ContributionAnswer answer) {
            LocalDate startDate = getDate(answer, "start");
            LocalDate endDate = getDate(answer, "end");

            Logger.debug("to recolte %s %s", startDate, endDate);

            if (startDate != null && endDate != null) {

                RecolnatRecolte recolte = new RecolnatRecolte();

                if (isSingleDay(startDate, endDate)) {
                    recolte.startDay = startDate.getDayOfMonth();
                    recolte.startMonth = startDate.getMonthOfYear();
                    recolte.startYear = startDate.getYear();
                } else if (isMonthPeriod(startDate, endDate)) {
                    recolte.startMonth = startDate.getMonthOfYear();
                    recolte.startYear = startDate.getYear();
                } else if (isYearPeriod(startDate, endDate)) {
                    recolte.startYear = startDate.getYear();
                } else {
                    recolte.startDay = startDate.getDayOfMonth();
                    recolte.startMonth = startDate.getMonthOfYear();
                    recolte.startYear = startDate.getYear();
                    recolte.endDay = endDate.getDayOfMonth();
                    recolte.endMonth = endDate.getMonthOfYear();
                    recolte.endYear = endDate.getYear();
                }


                DateTimeFormatter fmt = ISODateTimeFormat.date();

                DateTimeFormatter fmt2 = null;

                //if (startDate.getDayOfMonth() != endDate.getDayOfMonth()) {
                //    fmt2 = DateTimeFormat.forPattern("dd");
                //} else if (startDate.getMonthOfYear() != endDate.getMonthOfYear()) {
                //    fmt2 = DateTimeFormat.forPattern("MM-dd");
                //} else if (startDate.getYear() != endDate.getYear()) {
                //    fmt2 = ISODateTimeFormat.date();
                //}
                // FIX #339



                fmt2 = fmt;

                recolte.eventDate = fmt.print(startDate) + (fmt2 != null ? "/" + fmt2.print(endDate) : "");

                //

                recolte.startEventDate = startDate.toDate();
                recolte.endEventDate = endDate.toDate();

                //

                //Logger.info("Event date : %s", recolte.eventDate);


                return recolte;
            }

            return null;
        }


        @Override
        public String getName() {
            return "date";
        }

        @Override
        public List<RecolnatOperation> doTransfer(RecolnatSpecimen recolnatSpecimen,
                                                  Specimen specimen,
                                                  Map<String, ContributionAnswer> answersByQuestionName) throws NoTransferException {

            List<RecolnatOperation> operations = new ArrayList<RecolnatOperation>();


            if (recolnatSpecimen !=null && recolnatSpecimen.recolte != null) {

                boolean exists =
                        recolnatSpecimen.recolte.startDay != null ||
                        recolnatSpecimen.recolte.startMonth != null ||
                        recolnatSpecimen.recolte.startYear != null ||
                        recolnatSpecimen.recolte.endDay != null ||
                        recolnatSpecimen.recolte.endMonth != null ||
                        recolnatSpecimen.recolte.endYear != null;



                if (exists) {
                    throw new NoTransferException(specimen, NoTransferCause.RECOLNAT_ALREADY_EXISTS, this.getName());
                }

                ContributionAnswer answer = answersByQuestionName.get("collect_date");

                RecolnatRecolte herbonautesRecolteDate = toRecolte(answer);


                if (herbonautesRecolteDate != null) {

                    operations.add(RecolnatOperation.update(specimen, recolnatSpecimen, RecolnatOperation.TABLE_RECOLTES, "STARTEVENTDATE"));
                    operations.add(RecolnatOperation.update(specimen, recolnatSpecimen, RecolnatOperation.TABLE_RECOLTES, "ENDEVENTDATE"));
                    operations.add(RecolnatOperation.update(specimen, recolnatSpecimen, RecolnatOperation.TABLE_RECOLTES, "EVENTDATE"));

                    Logger.info("  H : %d %d %d - %d %d %d",
                        herbonautesRecolteDate.startDay  ,
                        herbonautesRecolteDate.startMonth,
                        herbonautesRecolteDate.startYear ,
                        herbonautesRecolteDate.endDay    ,
                        herbonautesRecolteDate.endMonth  ,
                        herbonautesRecolteDate.endYear   );

                    if ( herbonautesRecolteDate.startDay  != null) {
                        recolnatSpecimen.recolte.startDay = herbonautesRecolteDate.startDay;
                        operations.add(RecolnatOperation.update(specimen, recolnatSpecimen, RecolnatOperation.TABLE_RECOLTES, "SDAY"));
                    }
                    if ( herbonautesRecolteDate.startMonth  != null) {
                        recolnatSpecimen.recolte.startMonth = herbonautesRecolteDate.startMonth;
                        operations.add(RecolnatOperation.update(specimen, recolnatSpecimen, RecolnatOperation.TABLE_RECOLTES, "SMONTH"));
                    }
                    if ( herbonautesRecolteDate.startYear  != null) {
                        recolnatSpecimen.recolte.startYear = herbonautesRecolteDate.startYear;
                        operations.add(RecolnatOperation.update(specimen, recolnatSpecimen, RecolnatOperation.TABLE_RECOLTES, "SYEAR"));
                    }
                    if ( herbonautesRecolteDate.endDay  != null) {
                        recolnatSpecimen.recolte.endDay = herbonautesRecolteDate.endDay;
                        operations.add(RecolnatOperation.update(specimen, recolnatSpecimen, RecolnatOperation.TABLE_RECOLTES, "EDAY"));
                    }
                    if ( herbonautesRecolteDate.endMonth  != null) {
                        recolnatSpecimen.recolte.endMonth = herbonautesRecolteDate.endMonth;
                        operations.add(RecolnatOperation.update(specimen, recolnatSpecimen, RecolnatOperation.TABLE_RECOLTES, "EMONTH"));
                    }
                    if ( herbonautesRecolteDate.endYear  != null) {
                        recolnatSpecimen.recolte.endYear = herbonautesRecolteDate.endYear;
                        operations.add(RecolnatOperation.update(specimen, recolnatSpecimen, RecolnatOperation.TABLE_RECOLTES, "EYEAR"));
                    }

                    recolnatSpecimen.recolte.startEventDate = herbonautesRecolteDate.startEventDate;
                    recolnatSpecimen.recolte.endEventDate = herbonautesRecolteDate.endEventDate;
                    recolnatSpecimen.recolte.eventDate = herbonautesRecolteDate.eventDate;

                } else {
                    throw new NoTransferException(specimen, NoTransferCause.HERBONAUTES_NO_DATA, this.getName());
                }

            } else {
                throw new NoTransferException(specimen, NoTransferCause.RECOLNAT_MISSING_ROW, this.getName());
            }


            return operations;
        }



    }

    /**
     * Collect TRANSFER
     */

    public static class CollectFieldTransfer implements FieldTransfer {



        @Override
        public String getName() {
            return "collect";
        }

        @Override
        public List<RecolnatOperation> doTransfer(RecolnatSpecimen recolnatSpecimen,
                                                  Specimen specimen,
                                                  Map<String, ContributionAnswer> answersByQuestionName) throws NoTransferException {

            List<RecolnatOperation> operations = new ArrayList<RecolnatOperation>();


            if (recolnatSpecimen !=null && recolnatSpecimen.recolte != null) {

                String recolnatRecordedBy = recolnatSpecimen.recolte.recordedBy;
                String herbonautesRecordedBy = getRecordedBy(answersByQuestionName.get("collector"));

                //ContributionAnswer answer = answersByQuestionName.get("country");

                Logger.info("  H: %s   --- R: %s", herbonautesRecordedBy, recolnatRecordedBy);

                if (recolnatRecordedBy != null) {
                    throw new NoTransferException(specimen, NoTransferCause.RECOLNAT_ALREADY_EXISTS, this.getName());
                }

                if (herbonautesRecordedBy != null) {
                    recolnatSpecimen.recolte.recordedBy = herbonautesRecordedBy;

                    operations.add(RecolnatOperation.update(specimen, recolnatSpecimen, RecolnatOperation.TABLE_RECOLTES, "RECORDEDBY"));
                } else {
                    throw new NoTransferException(specimen, NoTransferCause.HERBONAUTES_NO_DATA, this.getName());
                }

            } else {
                throw new NoTransferException(specimen, NoTransferCause.RECOLNAT_MISSING_ROW, this.getName());
            }

            return operations;
        }

        private String getRecordedBy(ContributionAnswer answer) {

            if (answer == null) {
                return null;
            }
            StringBuffer recordedBy = new StringBuffer();


            JsonNode node = Json.parse(answer.getJsonValue());

            if (node.hasNonNull("collector")) {
                recordedBy.append(node.get("collector").get("label").asText());
            }

            if (node.hasNonNull("other_collectors")) {

                for (JsonNode c : node.get("other_collectors").get("values")) {
                    if (recordedBy.length() > 0) {
                        recordedBy.append("|");
                    }
                    recordedBy.append(c.get("label").asText());
                }

            }

            if(recordedBy.length() > 0) {
                return recordedBy.toString();
            }

            return null;
        }

    }

    /**
     * Identified TRANSFER
     */

    public static class IdentifiedFieldTransfer implements FieldTransfer {



        @Override
        public String getName() {
            return "identified";
        }




        @Override
        public List<RecolnatOperation> doTransfer(RecolnatSpecimen recolnatSpecimen,
                                                  Specimen specimen,
                                                  Map<String, ContributionAnswer> answersByQuestionName) throws NoTransferException {

            List<RecolnatOperation> operations = new ArrayList<RecolnatOperation>();


            if (recolnatSpecimen !=null && recolnatSpecimen.determinations != null) {

                RecolnatDetermination validDetermination = findValidDetermination(recolnatSpecimen, specimen);

                String recolnatIdentifiedBy = (validDetermination != null) ? validDetermination.identifiedBy : null;
                String herbonautesIdentifieddBy = getIdentifiedBy(answersByQuestionName.get("identifier"));

                //ContributionAnswer answer = answersByQuestionName.get("country");

                Logger.info("  H: %s   --- R: %s", herbonautesIdentifieddBy, recolnatIdentifiedBy);

                if (recolnatIdentifiedBy != null) {
                    throw new NoTransferException(specimen, NoTransferCause.RECOLNAT_ALREADY_EXISTS, this.getName());
                }

                if (herbonautesIdentifieddBy != null) {
                    validDetermination.identifiedBy = herbonautesIdentifieddBy;

                    operations.add(RecolnatOperation.update(specimen, recolnatSpecimen, RecolnatOperation.TABLE_DETERMINATIONS, "IDENTIFIEDBY"));
                } else {
                    throw new NoTransferException(specimen, NoTransferCause.HERBONAUTES_NO_DATA, this.getName());
                }

            } else {
                throw new NoTransferException(specimen, NoTransferCause.RECOLNAT_MISSING_ROW, this.getName());
            }

            return operations;
        }

        private String getIdentifiedBy(ContributionAnswer answer) {

            if (answer == null) {
                return null;
            }
            StringBuffer recordedBy = new StringBuffer();

            JsonNode node = Json.parse(answer.getJsonValue());

            if (node.hasNonNull("identifier")) {
                recordedBy.append(node.get("identifier").get("label").asText());
            }

            if(recordedBy.length() > 0) {
                return recordedBy.toString();
            }

            return null;
        }



    }

    /**
     * Locality TRANSFER
     */

    public static class LocalityFieldTransfer implements FieldTransfer {


        @Override
        public String getName() {
            return "locality";
        }

        @Override
        public List<RecolnatOperation> doTransfer(RecolnatSpecimen recolnatSpecimen,
                                                  Specimen specimen,
                                                  Map<String, ContributionAnswer> answersByQuestionName) throws NoTransferException {

            List<RecolnatOperation> operations = new ArrayList<RecolnatOperation>();


            if (recolnatSpecimen !=null && recolnatSpecimen.recolte != null && recolnatSpecimen.recolte.location != null) {

                String recolnatstateProvince = recolnatSpecimen.recolte.location.verbatimLocality;
                String herbonautesLabel = getLocality(answersByQuestionName.get("locality"));

                //ContributionAnswer answer = answersByQuestionName.get("country");

                Logger.info("  H: %s   --- R: %s", herbonautesLabel, recolnatstateProvince);

                if (recolnatstateProvince != null) {
                    throw new NoTransferException(specimen, NoTransferCause.RECOLNAT_ALREADY_EXISTS, this.getName());
                }

                if (herbonautesLabel != null) {
                    recolnatSpecimen.recolte.location.verbatimLocality = herbonautesLabel;

                    operations.add(RecolnatOperation.update(specimen, recolnatSpecimen, RecolnatOperation.TABLE_LOCALISATIONS, "VERBATIMLOCALITY"));
                } else {
                    throw new NoTransferException(specimen, NoTransferCause.HERBONAUTES_NO_DATA, this.getName());
                }

            } else {
                throw new NoTransferException(specimen, NoTransferCause.RECOLNAT_MISSING_ROW, this.getName());
            }

            return operations;
        }

        private String getLocality(ContributionAnswer answer) {
            if (answer == null) {
                return null;
            }
            StringBuffer value = new StringBuffer();


            JsonNode node = Json.parse(answer.getJsonValue());

            if (node.hasNonNull("locality")) {
                value.append(node.get("locality").asText());
            }

            if(value.length() > 0) {
                return value.toString();
            }

            return null;
        }

    }

    /**
     * Geolocalisation TRANSFER
     */

    public static class GeolocalisationFieldTransfer implements FieldTransfer {


        @Override
        public String getName() {
            return "geo";
        }

        @Override
        public List<RecolnatOperation> doTransfer(RecolnatSpecimen recolnatSpecimen,
                                                  Specimen specimen,
                                                  Map<String, ContributionAnswer> answersByQuestionName) throws NoTransferException {

            List<RecolnatOperation> operations = new ArrayList<RecolnatOperation>();


            if (recolnatSpecimen !=null && recolnatSpecimen.recolte != null && recolnatSpecimen.recolte.location != null) {

                Double recolnatLat = recolnatSpecimen.recolte.location.decimalLatitude;
                Double recolnatLng = recolnatSpecimen.recolte.location.decimalLongitude;

                Double[] herbonautePosition = getPosition(answersByQuestionName.get("geo"));

                //ContributionAnswer answer = answersByQuestionName.get("country");

                if (herbonautePosition != null) {
                    Logger.info("  H: %s %s   --- R: %s %s", herbonautePosition[0], herbonautePosition[1], recolnatLat, recolnatLng);
                }

                if (recolnatLat != null && recolnatLng != null) {
                    throw new NoTransferException(specimen, NoTransferCause.RECOLNAT_ALREADY_EXISTS, this.getName());
                }

                if (herbonautePosition != null) {
                    recolnatSpecimen.recolte.location.decimalLatitude = herbonautePosition[0];
                    recolnatSpecimen.recolte.location.decimalLongitude = herbonautePosition[1];

                    operations.add(RecolnatOperation.update(specimen, recolnatSpecimen, RecolnatOperation.TABLE_LOCALISATIONS, "DECIMALLATITUDE"));
                    operations.add(RecolnatOperation.update(specimen, recolnatSpecimen, RecolnatOperation.TABLE_LOCALISATIONS, "DECIMALLONGITUDE"));
                } else {
                    throw new NoTransferException(specimen, NoTransferCause.HERBONAUTES_NO_DATA, this.getName());
                }

            } else {
                throw new NoTransferException(specimen, NoTransferCause.RECOLNAT_MISSING_ROW, this.getName());
            }

            return operations;
        }

        private Double[] getPosition(ContributionAnswer answer) {
            if (answer == null) {
                return null;
            }
            Double[] value = new Double[2];


            JsonNode node = Json.parse(answer.getJsonValue());

            if (node.hasNonNull("position")) {
                value[0] = node.get("position").get("lat").asDouble();
                value[1] = node.get("position").get("lng").asDouble();
                return value;
            }



            return null;
        }


    }

    /**
     * Collect number TRANSFER
     */

    public static class CollectNumberFieldTransfer implements FieldTransfer {


        @Override
        public String getName() {
            return "collect_number";
        }

        @Override
        public List<RecolnatOperation> doTransfer(RecolnatSpecimen recolnatSpecimen,
                                                  Specimen specimen,
                                                  Map<String, ContributionAnswer> answersByQuestionName) throws NoTransferException {

            List<RecolnatOperation> operations = new ArrayList<RecolnatOperation>();


            if (recolnatSpecimen !=null && recolnatSpecimen.recolte != null) {

                String fieldNumber = getFieldNumber(answersByQuestionName.get("collect_number"));

                String recolnatFieldNumber = recolnatSpecimen.recolte.fieldNumber;
                //ContributionAnswer answer = answersByQuestionName.get("country");

                Logger.info("  H: %s   --- R: %s", fieldNumber, recolnatFieldNumber);

                if (recolnatFieldNumber != null) {
                    throw new NoTransferException(specimen, NoTransferCause.RECOLNAT_ALREADY_EXISTS, this.getName());
                }

                if (fieldNumber != null) {
                    recolnatSpecimen.recolte.fieldNumber = fieldNumber;

                    operations.add(RecolnatOperation.update(specimen, recolnatSpecimen, RecolnatOperation.TABLE_RECOLTES, "FIELDNUMBER"));
                } else {
                    throw new NoTransferException(specimen, NoTransferCause.HERBONAUTES_NO_DATA, this.getName());
                }

            } else {
                throw new NoTransferException(specimen, NoTransferCause.RECOLNAT_MISSING_ROW, this.getName());
            }

            return operations;
        }

        private String getFieldNumber(ContributionAnswer answer) {
            if (answer == null) {
                return null;
            }
            StringBuffer value = new StringBuffer();


            JsonNode node = Json.parse(answer.getJsonValue());

            if (node.hasNonNull("")) {
                value.append(node.get("").asText());
            }
            if (node.hasNonNull("number")) {
                value.append(node.get("number").asText());
            }
            if (node.hasNonNull("numero")) {
                value.append(node.get("numero").asText());
            }

            if(value.length() > 0) {
                return value.toString();
            }

            return null;
        }

    }

    /**
     * Unusable TRANSFER
     */

    public static class UnusableFieldTransfer implements FieldTransfer {


        @Override
        public String getName() {
            return "unusable";
        }

        @Override
        public List<RecolnatOperation> doTransfer(RecolnatSpecimen recolnatSpecimen,
                                                  Specimen specimen,
                                                  Map<String, ContributionAnswer> answersByQuestionName) throws NoTransferException {

            List<RecolnatOperation> operations = new ArrayList<RecolnatOperation>();


            if (recolnatSpecimen !=null) {

                String recolnatRemarks = recolnatSpecimen.occurrenceRemarks;
                String herbonautesCause = getUnusableCause(answersByQuestionName.get("unusable"));

                //ContributionAnswer answer = answersByQuestionName.get("country");

                Logger.info("  H: %s   --- R: %s", herbonautesCause, recolnatRemarks);

                if (recolnatRemarks != null) {
                    throw new NoTransferException(specimen, NoTransferCause.RECOLNAT_ALREADY_EXISTS, this.getName());
                }

                if (herbonautesCause != null) {
                    recolnatSpecimen.occurrenceRemarks = String.format("Photo inutilisable (%s)", herbonautesCause);

                    operations.add(RecolnatOperation.update(specimen, recolnatSpecimen, RecolnatOperation.TABLE_LOCALISATIONS, "OCCURRENCEREMARKS"));
                } else {
                    throw new NoTransferException(specimen, NoTransferCause.HERBONAUTES_NO_DATA, this.getName());
                }

            } else {
                throw new NoTransferException(specimen, NoTransferCause.RECOLNAT_MISSING_ROW, this.getName());
            }

            return operations;
        }

        private String getUnusableCause(ContributionAnswer answer) {
            if (answer == null) {
                return null;
            }
            StringBuffer value = new StringBuffer();


            JsonNode node = Json.parse(answer.getJsonValue());

            if (node.hasNonNull("cause")) {
                value.append(node.get("cause").asText());
            }

            if(value.length() > 0) {
                return value.toString();
            }

            return null;
        }

    }

}
