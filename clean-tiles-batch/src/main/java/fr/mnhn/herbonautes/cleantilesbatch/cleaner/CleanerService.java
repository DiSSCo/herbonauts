package fr.mnhn.herbonautes.cleantilesbatch.cleaner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Date;

@Service
public class CleanerService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private CleanerConfiguration cleanerConfiguration;

    @Autowired
    private CleanerTask cleanerTask;



    public CleanerService() {
    }

    @Async
    @Transactional
    public void clean() {

        if (cleanerTask.getRunning()) {
            return;
        }

        try {
            cleanerTask.run();
        } catch (Exception e) {
            e.printStackTrace();;
        } finally {

        }

        //Runnable task = new Runnable() {
        //    @Override
        //    public void run() {
        //        System.out.println("Start cleaner task");
        //        CleanerTask cleanerTask = new CleanerTask(entityManager, cleanerConfiguration);
        //        cleanerTask.run();
        //        System.out.println("Done");
        //    }
        //};
//
        //try {
        //    executorService.submit(task);
        //    System.out.println("Submitted task");
        //} catch (Exception e) {
        //    e.printStackTrace();
        //}

    }
}
