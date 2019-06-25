package fr.mnhn.herbonautes.cleantilesbatch.cleaner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class DiskUsageReport {

    private Long totalSpaceInBytes = 0L;
    private Long freeSpaceInBytes = 0L;

    private Integer totalSpecimenCount = 0;
    private Integer completeSpecimenCount = 0;
    private Integer minimalSpecimenCount = 0;

    public static DiskUsageReport build(CleanerConfiguration configuration) throws IOException {
        final DiskUsageReport report = new DiskUsageReport();

        report.totalSpaceInBytes = new File("/").getTotalSpace();
        report.freeSpaceInBytes = new File("/").getFreeSpace();

        long completeTotalSpaceUsageInBytes = 0;

        for (File instituteDir : new File(configuration.getTilesRootDir()).listFiles()) {
            if (!instituteDir.isDirectory()) { continue; }

            for (File collectionDir : instituteDir.listFiles()) {
                if (!collectionDir.isDirectory()) { continue; }

                for (File codeDir : collectionDir.listFiles()) {
                    if (!codeDir.isDirectory()) { continue; }

                    report.totalSpecimenCount += 1;
                    if (new File(codeDir, configuration.getTestFiles().get(0)).exists()) {
                        report.completeSpecimenCount += 1;
                    } else {
                        report.minimalSpecimenCount += 1;
                    }


                    //CleanerTask.cleanTilesForSpecimen(codeDir.getAbsolutePath(), Arrays.asList("tile_0_0_0.jpg"));

                }

            }
        }


        return report;
    }

    public Long getTotalSpaceInBytes() {
        return totalSpaceInBytes;
    }

    public void setTotalSpaceInBytes(Long totalSpaceInBytes) {
        this.totalSpaceInBytes = totalSpaceInBytes;
    }

    public Long getFreeSpaceInBytes() {
        return freeSpaceInBytes;
    }

    public void setFreeSpaceInBytes(Long freeSpaceInBytes) {
        this.freeSpaceInBytes = freeSpaceInBytes;
    }

    public Integer getTotalSpecimenCount() {
        return totalSpecimenCount;
    }

    public void setTotalSpecimenCount(Integer totalSpecimenCount) {
        this.totalSpecimenCount = totalSpecimenCount;
    }

    public Integer getCompleteSpecimenCount() {
        return completeSpecimenCount;
    }

    public void setCompleteSpecimenCount(Integer completeSpecimenCount) {
        this.completeSpecimenCount = completeSpecimenCount;
    }

    public Integer getMinimalSpecimenCount() {
        return minimalSpecimenCount;
    }

    public void setMinimalSpecimenCount(Integer minimalSpecimenCount) {
        this.minimalSpecimenCount = minimalSpecimenCount;
    }


}

