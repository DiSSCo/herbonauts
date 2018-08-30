package jobs;

import com.fasterxml.jackson.databind.JsonNode;
import helpers.SpecimensCVSLoader;
import libs.Json;
import models.Cart;
import models.Mission;
import models.Specimen;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.db.jpa.NoTransaction;
import play.jobs.Job;
import services.RecolnatSearchClient;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Date;
import java.util.List;
import java.util.Map;

@NoTransaction
public class CartJobDeprecated extends Job  {

    public static enum Action {
        ADD,
        REMOVE
    }

	private Long missionId;
    private Cart cart;
    private Action action;

    private RecolnatSearchClient search = new RecolnatSearchClient();

    public CartJobDeprecated(Long missionId, Cart cart, Action action) {
        super();
        this.missionId = missionId;
        this.cart = cart;
        this.action = action;
    }


	@Override
	public void doJob() throws Exception {

        switch (this.action) {
            case ADD: addCart(); break;
            case REMOVE: removeCart(); break;
            default: break;
        }

    }

    public void removeCart() throws Exception {
        Logger.info("Removing cart queries...");
        Mission mission = null;

        try {

            JPA.startTx("default", false);

            mission = Mission.findById(missionId);

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

            for (Cart.CartQuery q : cart.queries) {

                if (q.loaded == null || !q.loaded) {
                    Logger.info("- Query non versée");
                    continue;
                }

                Logger.info("- All selected ? %s", q.selectedAll);
                if (q.selectedAll) {
                    // Selection de sous les specimens de la query
                    removeAllSpecimens(mission, q.query);
                } else {
                    // Selection un par un
                    for (String id : q.selected) {
                        Logger.info("- Get specimen (id:%s)", id);
                        RecolnatSearchClient.RecolnatSpecimen specimen = this.search.getSpecimen(id);
                        Logger.info("- %s", specimen._id);
                        removeSpecimen(mission, specimen);
                    }
                }
                //markQueryLoaded(mission.id, cart, q);
            }
        } catch (Exception e) {
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

    public void addCart() throws Exception {

        Mission mission = null;

        Logger.info("Loading cart...");

        try {

            JPA.startTx("default", false);

            mission = Mission.findById(missionId);

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

            for (Cart.CartQuery q : cart.queries) {

                if (q.loaded != null && q.loaded) {
                    Logger.info("- Query déjà versée");
                    continue;
                }

                Logger.info("- All selected ? %s", q.selectedAll);
                if (q.selectedAll) {
                    // Selection de sous les specimens de la query
                    importAllSpecimens(mission, q.query);
                } else {
                    // Selection un par un
                    for (String id : q.selected) {
                        Logger.info("- Get specimen (id:%s)", id);
                        RecolnatSearchClient.RecolnatSpecimen specimen = this.search.getSpecimen(id);
                        Logger.info("- %s", specimen._id);
                        importSpecimen(mission, specimen);
                    }
                }
                markQueryLoaded(mission.id, cart, q);
            }
        } catch (Exception e) {
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

    private void markQueryLoaded(Long missionId, Cart cart, Cart.CartQuery query) {
        try {
            JPA.startTx("default", false);

            Mission mission = Mission.findById(missionId);

            query.loaded = true;
            //mission.setCartJson(Json.toJson(cart));
            mission.save();

            JPA.closeTx("default");
        } catch (Throwable t) {

            Logger.error(t, "error specimen");

            JPA.rollbackTx("default");
        }
    }

    private void removeAllSpecimens(Mission mission, Map<String, String> query) {
        Logger.info("mapping all %s", query);
        int page = 1;
        int pageSize = 50;
        List<RecolnatSearchClient.RecolnatSpecimen> specimens = null;
        /*while ((specimens = search.search(query, page, pageSize)).size() > 0) {
            for (RecolnatSearchClient.RecolnatSpecimen specimen : specimens) {
                Logger.info("- %s", specimen.catalogNumber);
                removeSpecimen(mission, specimen);
            }
            page++;
        }*/
        Logger.info("Done mapping");

    }

    private void removeSpecimen(Mission mission, RecolnatSearchClient.RecolnatSpecimen recolnatSpecimen) {
        try {
            JPA.startTx("default", false);

            Boolean existing = Specimen.find("code = ?", recolnatSpecimen.catalogNumber).fetch().size() > 0;

            if (!existing) {
                Logger.error("- %s n'existe pas, pas de suppression !", recolnatSpecimen.catalogNumber);
                JPA.closeTx("default");
                return;
            }

            Specimen specimen = (Specimen) Specimen.find("code = ?", recolnatSpecimen.catalogNumber).fetch(1).get(0);
            specimen.delete();

            Logger.info("- %s supprimé", recolnatSpecimen.catalogNumber);

            JPA.closeTx("default");
        } catch (Throwable t) {

            Logger.error(t, "error specimen");

            JPA.rollbackTx("default");
        }
    }

    private void importAllSpecimens(Mission mission, Map<String, String> query) {
        Logger.info("mapping all %s", query);
        int page = 1;
        int pageSize = 50;
        List<RecolnatSearchClient.RecolnatSpecimen> specimens = null;
        /*while ((specimens = search.search(query, page, pageSize)).size() > 0) {
            for (RecolnatSearchClient.RecolnatSpecimen specimen : specimens) {
                Logger.info("- %s", specimen.catalogNumber);
                importSpecimen(mission, specimen);
            }
            page++;
        }*/
        Logger.info("Done mapping");

    }

    private void importSpecimen(Mission mission, RecolnatSearchClient.RecolnatSpecimen recolnatSpecimen) {


        try {
            JPA.startTx("default", false);

            Boolean existing = Specimen.find("code = ?", recolnatSpecimen.catalogNumber).fetch().size() > 0;

            if (existing) {
                Logger.error("- %s déjà en base !", recolnatSpecimen.catalogNumber);
                JPA.closeTx("default");
                return;
            }

            Specimen specimen = new Specimen();
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
            specimen.save();

            Logger.info("- %s créé", recolnatSpecimen.catalogNumber);

            JPA.closeTx("default");
        } catch (Throwable t) {

            Logger.error(t, "error specimen");

            JPA.rollbackTx("default");
        }

    }
	
}
