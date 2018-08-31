package jobs;

import conf.Herbonautes;
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

import java.util.*;

@NoTransaction
public class CartJob extends Job  {

    private Long missionId;

    private RecolnatSearchClient search = new RecolnatSearchClient();

    public CartJob(Long missionId) {
        super();
        this.missionId = missionId;
    }


    @Override
    public void doJob() throws Exception {
        syncCart();
    }



    public void syncCart() throws Exception {

        Mission mission = null;
        List<MissionCartQuery> queries = null;
        Logger.info("Sync cart...");

        try {

            JPA.startTx("default", false);

            mission = Mission.findById(missionId);
            queries = MissionCartQuery.find("missionId = ?", missionId).fetch();

            Logger.info("Lock mission");
            mission.setLoadingCart(true);
            mission.save();

            JPA.closeTx("default");

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

                JPA.startTx("default", false);
                mission = Mission.findById(missionId);

                mission.setLoadingCart(false);
                mission.save();

                JPA.closeTx("default");
            } catch (Throwable t) {
                Logger.error(t, "Error");
                JPA.rollbackTx("default");
            }
        }
    }

    private boolean processChanges(Mission mission, Long queryId) throws SpecimenLimitException {
        try {
            JPA.startTx("default", false);

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
                        Logger.info("  - " + exploreId);
                        removeSpecimenFromId(mission, exploreId);
                    } else {
                        Logger.info("  + " + exploreId);
                        importSpecimenFromId(mission, exploreId);
                    }
                }
            }

            JPA.closeTx("default");

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
        while ((specimens = search.search(query.getTerms(), query.getNoCollectInfo(), page, pageSize)).size() > 0) {
            for (RecolnatSearchClient.RecolnatSpecimen specimen : specimens) {

                if (!query.getSelectionDraft().contains(specimen._id)) {
                    Logger.info("- %s", specimen.catalogNumber);
                    removeSpecimenFromId(mission, specimen._id);
                } else {
                    Logger.info("- %s in selection, skip remove", specimen.catalogNumber);
                }
            }
            page++;
        }
        Logger.info("Done mapping");

    }

    private void removeSpecimenFromCatalogNumber(Mission mission, String catalogNumber) {

        try {
            JPA.startTx("default", false);

            Specimen specimen = Specimen.find("code = ? and mission.id = ?", catalogNumber, mission.id).first();

            if (specimen != null) {
                specimen.delete();
                Logger.info("  %s supprimé de la mission %s", catalogNumber, mission.getTitle());
            } else {
                Logger.error("  %s n'est pas dans la mission %s", catalogNumber, mission.getTitle());
            }

            JPA.closeTx("default");
        } catch (Throwable t) {

            Logger.error(t, "error specimen");

            JPA.rollbackTx("default");
        }

        //try {
        //    JPA.startTx("default", false);
//
        //    Specimen existing = Specimen.find("code = ?", catalogNumber).first();
//
        //    if (existing == null) {
        //        Logger.error("  %s n'existe pas, pas de suppression !", catalogNumber);
        //        JPA.closeTx("default");
        //        return;
        //    } else if (!existing.getMission().id.equals(mission.id)) {
        //        Logger.error("  %s dans une autre mission, pas de suppression !", catalogNumber);
        //        JPA.closeTx("default");
        //        return;
        //    }
//
        //    Specimen specimen = (Specimen) Specimen.find("code = ?", catalogNumber).fetch(1).get(0);
        //    specimen.delete();
//
        //    Logger.info("  %s supprimé", catalogNumber);
//
        //    JPA.closeTx("default");
        //} catch (Throwable t) {
//
        //    Logger.error(t, "error specimen");
//
        //    JPA.rollbackTx("default");
        //}
    }

    private void removeSpecimenFromId(Mission mission, String exploreId) {
        RecolnatSearchClient.RecolnatSpecimen recolnatSpecimen = this.search.getSpecimen(exploreId);
        removeSpecimenFromCatalogNumber(mission, recolnatSpecimen.catalogNumber);
    }

    private void importAllCatalognumber(Mission mission, MissionCartQuery query) throws SpecimenLimitException {

        HashMap<String, String> terms = new HashMap();


        for (String code : query.getTextFile().getData().split("\n")) {

            code = code.replaceAll("[^a-zA-Z0-9]", "");

            terms.put("catalognumber", code.toLowerCase());
            List<RecolnatSearchClient.RecolnatSpecimen> specimens = search.search(terms, true, 1, 1);

            if (specimens.size() == 0) {
                specimens = search.search(terms, false, 1, 1);
            }
            if (specimens.size() > 0) {
                convertAndSaveSpecimen(mission, specimens.get(0));
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
        while ((specimens = search.search(query.getTerms(), query.getNoCollectInfo(), page, pageSize)).size() > 0) {
            Logger.info("Page %d (%d)", page, pageSize);
            for (RecolnatSearchClient.RecolnatSpecimen specimen : specimens) {
                Logger.info("- %s", specimen.catalogNumber);
                convertAndSaveSpecimen(mission, specimen);
            }
            page++;
        }
        Logger.info("Done mapping");

    }


    private void convertAndSaveSpecimen(Mission mission, RecolnatSearchClient.RecolnatSpecimen recolnatSpecimen) throws SpecimenLimitException {

        try {
            JPA.startTx("default", false);

            checkSpecimenLimit();

            Boolean existing = Specimen.find("code = ? and mission.id = ?", recolnatSpecimen.catalogNumber, mission.id).fetch().size() > 0;

            if (existing) {
                Logger.error("  %s déjà en base !", recolnatSpecimen.catalogNumber);
                JPA.closeTx("default");
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
            specimen.save();
            specimen.refresh();
            Tag tag = Tag.findByLabel(specimen.getCode());
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
            }
            TagLink tagLink = new TagLink();
            tagLink.setLinkType(TagLinkType.SPECIMEN);
            tagLink.setTagId(tag.getId());
            tagLink.setTargetId(specimen.getId());
            tagLink.setPrincipal(true);
            tagLink.create();
            Logger.info("  > %s créé", recolnatSpecimen.catalogNumber);

            JPA.closeTx("default");
        } catch (Throwable t) {

            Logger.error(t, "error specimen");

            JPA.rollbackTx("default");
        }

    }

    private void importSpecimenFromId(Mission mission, String exploreId) throws SpecimenLimitException {
        RecolnatSearchClient.RecolnatSpecimen recolnatSpecimen = this.search.getSpecimen(exploreId);
        convertAndSaveSpecimen(mission, recolnatSpecimen);
    }

    private void markQuerySync(Long queryId) {
        try {
            JPA.startTx("default", false);

            MissionCartQuery query = MissionCartQuery.findById(queryId);
            query.setAllSelected(query.getAllSelectedDraft());
            List<String> selection = new ArrayList<String>();
            selection.addAll(query.getSelectionDraft());
            query.setSelection(selection);
            query.setSync(true);
            query.save();

            JPA.closeTx("default");
        } catch (Throwable t) {

            Logger.error(t, "error specimen");

            JPA.rollbackTx("default");
        }
    }

    public void checkSpecimenLimit() throws SpecimenLimitException {
        long count = Specimen.count("mission.id = ?", missionId);
        if (count >= Herbonautes.get().specimenPerMissionLimit) {
            throw new SpecimenLimitException();
        }
    }

    public static class SpecimenLimitException extends Exception {

    }

}
