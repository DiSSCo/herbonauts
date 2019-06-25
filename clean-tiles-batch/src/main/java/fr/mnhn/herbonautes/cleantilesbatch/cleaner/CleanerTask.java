package fr.mnhn.herbonautes.cleantilesbatch.cleaner;

import fr.mnhn.herbonautes.cleantilesbatch.specimen.Specimen;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Component
public class CleanerTask {

    private static final Log logger = LogFactory.getLog(CleanerTask.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private CleanerConfiguration configuration;

    private Boolean running = false;

    private Date lastStart;

    private Boolean lastRunError = false;

    private static String ONLY_CLOSED_MISSIONS_QUERY =
            "SELECT \n" +
            "  sm.id as ID,\n" +
            "  sm.institute as INSTITUTE,\n" +
            "  sm.collection as COLLECTION,\n" +
            "  sm.code as CODE\n" +
            "FROM \n" +
            "  h_specimen_master sm\n" +
            "  INNER JOIN \n" +
            "    h_specimen s ON s.master_id = sm.id\n" +
            "  INNER JOIN\n" +
            "    h_mission m ON s.mission_id = m.id\n" +
            "GROUP BY\n" +
            "  sm.id,\n" +
            "  sm.institute,\n" +
            "  sm.collection,\n" +
            "  sm.code\n" +
            "HAVING\n" +
            "  count(*) = SUM(m.closed) AND\n" +
            "  count(*) != SUM(s.deleted_tiles)";

    private static String MARK_DELETED_TILES_QUERY = "UPDATE H_SPECIMEN \n" +
            "SET deleted_tiles = 1\n" +
            "WHERE MASTER_ID IN (\n" +
            "  SELECT \n" +
            "    sm.id as ID\n" +
            "  FROM \n" +
            "    h_specimen_master sm\n" +
            "    INNER JOIN \n" +
            "      h_specimen s ON s.master_id = sm.id\n" +
            "    INNER JOIN\n" +
            "      h_mission m ON s.mission_id = m.id\n" +
            "  GROUP BY\n" +
            "    sm.id\n" +
            "  HAVING\n" +
            "    count(*) = SUM(m.closed) AND\n" +
            "    count(*) != SUM(s.deleted_tiles)\n" +
            ")";

    public void run() {

        running = true;
        lastStart = new Date();

        try {

            long startInMillis = System.currentTimeMillis();

            logger.info("Clean task");
            logger.info("- start");

            int specimenCount = 0;
            int deletedCount = 0;

            Session unwrappedSession = entityManager.unwrap(Session.class);
            SessionFactory sessionFactory = unwrappedSession.getSessionFactory();

            StatelessSession session = sessionFactory.openStatelessSession();
            Transaction tx = session.beginTransaction();

            ScrollableResults closedSpecimens = session.createNativeQuery(ONLY_CLOSED_MISSIONS_QUERY, Specimen.class)
                    .setReadOnly(true)
                    .scroll(ScrollMode.FORWARD_ONLY);

            while (closedSpecimens.next()) {

                specimenCount++;

                try {
                    Specimen specimen = (Specimen) closedSpecimens.get(0);

                    logger.info("- delete images for specimen " + specimen.getCode() + " (#" + specimenCount + ")");

                    File rootSpecimenDir = new File(configuration.getTilesRootDir(), specimen.getInstitute() + "/" + specimen.getCollection() + "/" + specimen.getCode());
                    cleanTilesForSpecimen(rootSpecimenDir, configuration.keepFiles);

                } catch (Exception e) {
                    e.printStackTrace();
                }


                //session.update(customer);
            }


            logger.info("- mark tiles as deleted");
            session.createNativeQuery(MARK_DELETED_TILES_QUERY).executeUpdate();



            long durationInSeconds = (System.currentTimeMillis() - startInMillis) / 1000;
            logger.info("- done in " + durationInSeconds + " seconds");
            logger.info("- " + specimenCount + " specimen deleted");

            tx.commit();
            session.close();

            lastRunError = false;

        } catch (Exception e) {

            lastRunError = true;
            e.printStackTrace();

        } finally {
            running = false;
            lastStart = null;
        }

    }


    public void cleanTilesForSpecimen(File rootSpecimenDir, List<String> keepFiles) {

        logger.info("Clean " + rootSpecimenDir);

        if (!rootSpecimenDir.exists()) {
            logger.info("-  dir does not exist >> skip");
            return;
        }

        Set<File> keepFilesSet = new HashSet<>();
        for (String keepFile : keepFiles) {
            keepFilesSet.add(new File(rootSpecimenDir, keepFile));
        }

        for (File f : rootSpecimenDir.listFiles()) {

            if (f.isDirectory()) {
                // Media directory, remove
                logger.info("- delete dir : " + f.getAbsolutePath());
                FileSystemUtils.deleteRecursively(f);
            } else {
                if (keepFilesSet.contains(f)) {
                    logger.info("- keep file: " + f.getAbsolutePath());
                } else {
                    //logger.info("- delete file: " + f.getAbsolutePath());
                    FileSystemUtils.deleteRecursively(f);
                }
            }

        }

    }

    public Boolean getRunning() {
        return running;
    }

    public void setRunning(Boolean running) {
        this.running = running;
    }

    public Date getLastStart() {
        return lastStart;
    }

    public void setLastStart(Date lastStart) {
        this.lastStart = lastStart;
    }

    public Boolean getLastRunError() {
        return lastRunError;
    }

    public void setLastRunError(Boolean lastRunError) {
        this.lastRunError = lastRunError;
    }

}
