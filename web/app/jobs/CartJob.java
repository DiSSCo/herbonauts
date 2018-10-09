package jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import conf.Herbonautes;
import libs.Json;
import models.*;
import models.tags.Tag;
import models.tags.TagLink;
import models.tags.TagLinkType;
import models.tags.TagType;
import org.apache.commons.collections.CollectionUtils;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.NoTransaction;
import play.jobs.Job;
import play.libs.Codec;
import services.RecolnatSearchClient;
import sun.rmi.runtime.Log;

import java.util.*;

@NoTransaction
public class CartJob extends Job  {

    private Long missionId;

    private RecolnatSearchClient search = new RecolnatSearchClient();

    private List<MissionImportException> exceptions;

    public CartJob(Long missionId) {
        super();
        this.missionId = missionId;
    }


    @Override
    public void doJob() throws Exception {
        syncCart();
    }



    public void syncCart() throws Exception {

        Logger.info("Sync cart");


        Mission mission = null;
        List<MissionCartQuery> queries = null;
        Logger.info("Sync cart...");

        try {

            JPA.startTx("default", false);Logger.warn("===> START TX syncCart");

            // Load exceptions
            this.exceptions = MissionImportException.findImportExceptionByMissionId(missionId);

            mission = Mission.findById(missionId);
            queries = MissionCartQuery.find("missionId = ?", missionId).fetch();

            Logger.info("Lock mission");
            mission.setLoadingCart(true);
            mission.save();

            JPA.closeTx("default");Logger.warn("===> CLOSE TX");

        } catch (Throwable t) {
            JPA.rollbackTx("default");
        }


        if (mission == null) {
            return;
        }


        try {

            for (MissionCartQuery query : queries) {
                if (!query.getSync()) {
                    Logger.info("- Sync query %d", query.id);

                    try {
                        boolean done = processChanges(mission, query.getId());
                        if (done) {
                            markQuerySync(query.id);
                        }
                    } catch (SpecimenLimitException se) {
                        Logger.error(se, "Specimen limit " + Herbonautes.get().specimenPerMissionLimit + " reached");
                    } finally {
                        //markQuerySync(query.id);
                    }

                } else {
                    Logger.info("- No change on query %d", query.id);
                }
            }


        }  catch (Exception e) {
            Logger.error(e, "Unable to load cart");

        } finally {

            Logger.info("Unlock mission");

            try {

                JPA.startTx("default", false);Logger.warn("===> START TX syncCart finnaly");
                mission = Mission.findById(missionId);

                mission.setLoadingCart(false);
                mission.save();

                JPA.closeTx("default");Logger.warn("===> CLOSE TX");
            } catch (Throwable t) {
                Logger.error(t, "Error");
                JPA.rollbackTx("default");
            }
        }
    }

    private boolean processChanges(Mission mission, Long queryId) throws SpecimenLimitException {


        try {
            JPA.startTx("default", false);Logger.warn("===> START TX processChanges");


            MissionCartQuery query = MissionCartQuery.findById(queryId);



            if (query.getTextFile() != null) {
                Logger.info("  Import file");
                importAllCatalognumber(mission, query);
            } else if (query.getAllSelectedDraft()) {
                // Selection complete, on importe tout
                Logger.info("  All selected, import all");
                importAllSpecimens(mission, query);
            } else if (query.getAllSelected()) {
                // Ils étaient tous selectionnés, il faut tous les annuler sauf
                // la selection
                Logger.info("  Selection mode selected");
                removeAllSpecimens(mission, query);
            } else {
                // Changement dans la selection seulement, on ajoute et on supprime les differences
                Logger.info("  Change in selection");
                Collection<String> diff = CollectionUtils.disjunction(query.getSelection(), query.getSelectionDraft());
                for (String exploreId : diff) {
                    if (query.getSelection().contains(exploreId)) {
                        Logger.info("  - " + exploreId + " (" + query.getIndexName() + ")");
                        removeSpecimenFromId(mission, exploreId, query.getIndexName());
                    } else {
                        Logger.info("  + " + exploreId + " (" + query.getIndexName() + ")");
                        importSpecimenFromId(mission, exploreId, query.getIndexName());
                    }
                }

                if (diff.size() == 0) {
                    Logger.info("  - no diff found, force import all selection (probably exception change)");
                    for (String exploreId : query.getSelection()) {
                        Logger.info("    - Import specimen (explore id: %s)", exploreId);
                        importSpecimenFromId(mission, exploreId, query.getIndexName());
                    }
                }

            }

            JPA.closeTx("default");Logger.warn("===> CLOSE TX");

            return true;


        } catch(SpecimenLimitException sle) {
            throw sle;
        } catch (Throwable t) {

            Logger.error(t, "error specimen");

            JPA.rollbackTx("default");

            return false;
        }
    }

    private void removeAllSpecimens(Mission mission, MissionCartQuery query) {
        Logger.info("mapping all %s", query);
        int page = 1;
        int pageSize = 100;
        List<RecolnatSearchClient.RecolnatSpecimen> specimens = null;
        while ((specimens = search.search(query.getIndexName(), query.getTerms(), query.getNoCollectInfo(), page, pageSize)).size() > 0) {
            for (RecolnatSearchClient.RecolnatSpecimen specimen : specimens) {

                if (!query.getSelectionDraft().contains(specimen._id)) {
                    Logger.info("- %s", specimen.catalogNumber);
                    removeSpecimenFromId(mission, specimen._id, query.getIndexName());
                } else {
                    Logger.info("- %s in selection, skip remove", specimen.catalogNumber);
                }
            }
            page++;
        }
        Logger.info("Done mapping");

    }

    private void removeSpecimenFromCatalogNumberNoTx(Mission mission, String catalogNumber) {

        try {

            Specimen specimen = Specimen.find("code = ? and mission.id = ?", catalogNumber, mission.id).first();

            if (specimen != null) {
                SpecimenMedia.deleteMediaBySpecimenId(specimen.getId());
                specimen.delete();

                Logger.info("  %s supprimé de la mission %s", catalogNumber, mission.getTitle());
            } else {
                Logger.error("  %s n'est pas dans la mission %s", catalogNumber, mission.getTitle());
            }

        } catch (Throwable t) {

            Logger.error(t, "error specimen");

        }

        //try {
        //    JPA.startTx("default", false);Logger.warn("===> START TX");
//
        //    Specimen existing = Specimen.find("code = ?", catalogNumber).first();
//
        //    if (existing == null) {
        //        Logger.error("  %s n'existe pas, pas de suppression !", catalogNumber);
        //        JPA.closeTx("default");Logger.warn("===> CLOSE TX");
        //        return;
        //    } else if (!existing.getMission().id.equals(mission.id)) {
        //        Logger.error("  %s dans une autre mission, pas de suppression !", catalogNumber);
        //        JPA.closeTx("default");Logger.warn("===> CLOSE TX");
        //        return;
        //    }
//
        //    Specimen specimen = (Specimen) Specimen.find("code = ?", catalogNumber).fetch(1).get(0);
        //    specimen.delete();
//
        //    Logger.info("  %s supprimé", catalogNumber);
//
        //    JPA.closeTx("default");Logger.warn("===> CLOSE TX");
        //} catch (Throwable t) {
//
        //    Logger.error(t, "error specimen");
//
        //    JPA.rollbackTx("default");
        //}
    }

    private void removeSpecimenFromCatalogNumber(Mission mission, String catalogNumber) {

        try {
            //JPA.startTx("default", false);Logger.warn("===> START TX removeSpecimenFromCatalogNumber");

            Specimen specimen = Specimen.find("code = ? and mission.id = ?", catalogNumber, mission.id).first();

            if (specimen != null) {
                SpecimenMedia.deleteMediaBySpecimenId(specimen.getId());
                specimen.delete();
                Logger.info("  %s supprimé de la mission %s", catalogNumber, mission.getTitle());
            } else {
                Logger.error("  %s n'est pas dans la mission %s", catalogNumber, mission.getTitle());
            }

            //JPA.closeTx("default");Logger.warn("===> CLOSE TX removeSpecimenFromCatalogNumber");
        } catch (Throwable t) {

            Logger.error(t, "error specimen");

            //JPA.rollbackTx("default");
        }

        //try {
        //    JPA.startTx("default", false);Logger.warn("===> START TX");
//
        //    Specimen existing = Specimen.find("code = ?", catalogNumber).first();
//
        //    if (existing == null) {
        //        Logger.error("  %s n'existe pas, pas de suppression !", catalogNumber);
        //        JPA.closeTx("default");Logger.warn("===> CLOSE TX");
        //        return;
        //    } else if (!existing.getMission().id.equals(mission.id)) {
        //        Logger.error("  %s dans une autre mission, pas de suppression !", catalogNumber);
        //        JPA.closeTx("default");Logger.warn("===> CLOSE TX");
        //        return;
        //    }
//
        //    Specimen specimen = (Specimen) Specimen.find("code = ?", catalogNumber).fetch(1).get(0);
        //    specimen.delete();
//
        //    Logger.info("  %s supprimé", catalogNumber);
//
        //    JPA.closeTx("default");Logger.warn("===> CLOSE TX");
        //} catch (Throwable t) {
//
        //    Logger.error(t, "error specimen");
//
        //    JPA.rollbackTx("default");
        //}
    }

    private void removeSpecimenFromId(Mission mission, String exploreId, String index) {
        RecolnatSearchClient.RecolnatSpecimen recolnatSpecimen = this.search.getSpecimen(index, exploreId);
        removeSpecimenFromCatalogNumber(mission, recolnatSpecimen.catalogNumber);
    }

    private void importAllCatalognumber(Mission mission, MissionCartQuery query) throws SpecimenLimitException {

        HashMap<String, String> terms = new HashMap();


        for (String code : query.getTextFile().getData().split("\n")) {

            code = code.replaceAll("[^a-zA-Z0-9]", "");

            terms.put("catalognumber", code.toLowerCase());
            List<RecolnatSearchClient.RecolnatSpecimen> specimens = search.search(query.getIndexName(), terms, true, 1, 1);

            if (specimens.size() == 0) {
                specimens = search.search(query.getIndexName(), terms, false, 1, 1);
            }
            if (specimens.size() > 0) {
                convertAndSaveSpecimen(mission, specimens.get(0), true);
            } else {
                Logger.info("No specimen found : %s", code);
            }
        }
    }

    private void importAllSpecimens(Mission mission, MissionCartQuery query) throws SpecimenLimitException {
        Logger.info("mapping all %s (collect: %s)", query.getTerms(), query.getNoCollectInfo());
        int page = 1;
        int pageSize = 100;
        List<RecolnatSearchClient.RecolnatSpecimen> specimens = null;
        while ((specimens = search.search(query.getIndexName(), query.getTerms(), query.getNoCollectInfo(), page, pageSize)).size() > 0) {
            Logger.info("Page %d (%d)", page, pageSize);
            for (RecolnatSearchClient.RecolnatSpecimen specimen : specimens) {
                Logger.info("- %s", specimen.catalogNumber);
                convertAndSaveSpecimen(mission, specimen, true);
            }
            page++;
        }
        Logger.info("Done mapping");

    }


    private void convertAndSaveSpecimen(Mission mission, RecolnatSearchClient.RecolnatSpecimen recolnatSpecimen) throws SpecimenLimitException {
        convertAndSaveSpecimen(mission, recolnatSpecimen, false);
    }

    private void convertAndSaveSpecimen(Mission mission, RecolnatSearchClient.RecolnatSpecimen recolnatSpecimen, boolean noTx) throws SpecimenLimitException {

        try {
            if (!noTx) {
                JPA.startTx("default", false);
                Logger.warn("===> START TX");
            }

            checkSpecimenLimit();

            Logger.info("---------");
            Logger.info("CONVERT AND SAVE %s", recolnatSpecimen.catalogNumber);


            // Is ignored if exist is other missions

            List<Long> exceptionsMissionIdList = new ArrayList<Long>();
            for (MissionImportException exception : this.exceptions) {
                exceptionsMissionIdList.add(exception.getIgnoreMissionId());
            }

            Logger.info("  Exception mission id list:");
            for (Long id : exceptionsMissionIdList) {
                Logger.info("  - %s", id);
            }

            boolean isIgnored = (exceptionsMissionIdList.size() > 0) && Specimen.find("code = :code and mission.id IN (:idList)")
                    .setParameter("code", recolnatSpecimen.catalogNumber)
                    .setParameter("idList", exceptionsMissionIdList)
                    .fetch().size() > 0;


            Boolean existing = Specimen.find("code = ? and mission.id = ?", recolnatSpecimen.catalogNumber, mission.id).fetch().size() > 0;

            if (existing) {
                Logger.error("  %s déjà en base !", recolnatSpecimen.catalogNumber);


                if (isIgnored) {
                    // Should be deleted
                    Logger.info("  Exists but ignored -> Force remove");
                    removeSpecimenFromCatalogNumberNoTx(mission, recolnatSpecimen.catalogNumber);
                }

                if (!noTx) {
                    JPA.closeTx("default");
                    Logger.warn("===> CLOSE TX");
                }
                return;
            }



            if (isIgnored) {
                Logger.info("  %s ignored from exceptions", recolnatSpecimen.catalogNumber);
                if (!noTx) {
                    JPA.closeTx("default");
                    Logger.warn("===> CLOSE TX");
                }
                return;
            }


            Boolean masterExisting = SpecimenMaster.count("code = ?", recolnatSpecimen.catalogNumber) > 0;

            SpecimenMaster master = null;

            Logger.info("Master exists (%s) : %s", recolnatSpecimen.catalogNumber, masterExisting);

            // Create master
            if (!masterExisting) {
                master = new SpecimenMaster();
                master.setCode(recolnatSpecimen.catalogNumber);
                master.setInstitute(recolnatSpecimen.institution);
                master.setCollection(recolnatSpecimen.collection);
                if (recolnatSpecimen.media != null && recolnatSpecimen.media.size() > 0) {
                    master.setSonneratURL(recolnatSpecimen.media.get(0).url);
                }
                master.setFamily(recolnatSpecimen.family);
                master.setGenus(recolnatSpecimen.genus);
                master.setSpecificEpithet(recolnatSpecimen.specificEpithet);
                master.save();
                Logger.info("Master created (id = %d)", master.id);
            } else {
                master = SpecimenMaster.find("code = ?", recolnatSpecimen.catalogNumber).first();
            }

            Logger.info("Build specimen");

            Specimen specimen = new Specimen();
            specimen.setMaster(master);
            specimen.setCode(recolnatSpecimen.catalogNumber);
            specimen.setInstitute(recolnatSpecimen.institution);
            specimen.setCollection(recolnatSpecimen.collection);
            specimen.setMission(mission);
            if (recolnatSpecimen.media != null && recolnatSpecimen.media.size() > 0) {
                specimen.setSonneratURL(recolnatSpecimen.media.get(0).url);
            }
            specimen.setFamily(recolnatSpecimen.family);
            specimen.setGenus(recolnatSpecimen.genus);
            specimen.setSpecificEpithet(recolnatSpecimen.specificEpithet);
            specimen.setLastModified(new Date());
            specimen.setAlea(Codec.hexMD5(recolnatSpecimen.catalogNumber));

            Logger.info("Save specimen");

            specimen.save();

            // Create media list

            Long number = 1L;
            HashSet<String> addedMediaId = new HashSet<String>();
            for (RecolnatSearchClient.RecolnatSpecimenMedia media :recolnatSpecimen.media) {

                if (addedMediaId.contains(media.id)) {
                    continue;
                }
                addedMediaId.add(media.id);

                SpecimenMedia m = new SpecimenMedia();
                m.setSpecimenId(specimen.getId());
                m.setMediaNumber(number);
                m.setMediaId(media.id);
                m.setUrl(media.url);
                m.save();
                number++;
            }


            Logger.info("Build tag for target " + master.getId());
            Tag tag = Tag.findByLabel(specimen.getCode());
            Logger.info("Tag already exists for code %s" + specimen.getCode());
            if(tag == null) {
                tag = new Tag();
                tag.setTagLabel(specimen.getCode());
                tag.setTagType(TagType.SPECIMEN);
                if(mission.isProposition() && !mission.isPropositionValidated()) {
                    tag.setPublished(false);
                } else {
                    tag.setPublished(true);
                }
                tag.create();
                tag.refresh();

                TagLink tagLink = new TagLink();
                tagLink.setLinkType(TagLinkType.SPECIMEN);
                tagLink.setTagId(tag.getId());
                tagLink.setTargetId(master.getId());
                tagLink.setPrincipal(true);
                tagLink.create();
            }


            Logger.info("  > %s créé", recolnatSpecimen.catalogNumber);

            if (!noTx) {
                JPA.closeTx("default");
                Logger.warn("===> CLOSE TX");
            }
        } catch (Throwable t) {

            Logger.error(t, "error specimen");

            if (!noTx) JPA.rollbackTx("default");
        }

    }

    private void importSpecimenFromId(Mission mission, String exploreId, String index) throws SpecimenLimitException {
        RecolnatSearchClient.RecolnatSpecimen recolnatSpecimen = this.search.getSpecimen(index, exploreId);
        convertAndSaveSpecimen(mission, recolnatSpecimen, true);
    }

    private void markQuerySync(Long queryId) {
        try {
            JPA.startTx("default", false);Logger.warn("===> START TX markQuerySync");

            MissionCartQuery query = MissionCartQuery.findById(queryId);
            query.setAllSelected(query.getAllSelectedDraft());
            List<String> selection = new ArrayList<String>();
            selection.addAll(query.getSelectionDraft());
            query.setSelection(selection);
            query.setSync(true);
            query.save();

            JPA.closeTx("default");Logger.warn("===> CLOSE TX");
        } catch (Throwable t) {

            Logger.error(t, "error specimen");

            JPA.rollbackTx("default");
        }
    }

    public void checkSpecimenLimit() throws SpecimenLimitException {
        Logger.info("Check specimen limit");
        long count = Specimen.count("mission.id = ?", missionId);
        if (count >= Herbonautes.get().specimenPerMissionLimit) {
            throw new SpecimenLimitException();
        }
    }

    public static class SpecimenLimitException extends Exception {

    }

}
