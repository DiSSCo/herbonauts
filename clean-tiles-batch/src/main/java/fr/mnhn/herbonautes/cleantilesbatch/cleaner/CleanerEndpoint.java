package fr.mnhn.herbonautes.cleantilesbatch.cleaner;

import fr.mnhn.herbonautes.cleantilesbatch.specimen.SpecimenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;

@RestController
@RequestMapping("/cleaner")
public class CleanerEndpoint {

    @Autowired
    private CleanerConfiguration configuration;

    @Autowired
    private SpecimenRepository specimenRepository;

    @Autowired
    private CleanerTask cleanerTask;

    @Autowired
    private CleanerService cleanerService;

    @GetMapping("/configuration")
    public CleanerConfiguration configuration() {
        return this.configuration;
    }

    @GetMapping("/usage")
    public DiskUsageReport diskUsage() throws IOException {
        return DiskUsageReport.build(configuration);
    }

    @PostMapping("/run")
    public ResponseEntity<Status> run(@RequestHeader("apiKey") String apiKey) {

        if (!configuration.apiTokens.contains(apiKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        long specimenCount = specimenRepository.count();

        cleanerService.clean();
        //cleanerTask.run();

        Status status = Status.build(cleanerTask);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/status")
    public Status status() {
        Status status = Status.build(cleanerTask);
        return status;
    }

    public static class Status {

        private Boolean running = false;

        private Date lastStart;

        private Boolean lastRunError = false;

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

        public static Status build(CleanerTask task) {
            Status status = new Status();

            status.setRunning(task.getRunning());
            status.setLastRunError(task.getLastRunError());
            status.setLastStart(task.getLastStart());

            return status;
        }
    }
}
