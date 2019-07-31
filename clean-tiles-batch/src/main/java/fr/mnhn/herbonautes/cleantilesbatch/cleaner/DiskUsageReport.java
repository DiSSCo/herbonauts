package fr.mnhn.herbonautes.cleantilesbatch.cleaner;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DiskUsageReport {

    private Long totalSpaceInBytes = 0L;
    private Long freeSpaceInBytes = 0L;

    private Integer totalSpecimenCount = 0;
    private Integer completeSpecimenCount = 0;
    private Integer minimalSpecimenCount = 0;

    public static DiskUsageReport build(CleanerConfiguration configuration) throws IOException {
        return buildV2(configuration);
    }

    public static DiskUsageReport buildSimple(CleanerConfiguration configuration) throws IOException {
        final DiskUsageReport report = new DiskUsageReport();

        report.totalSpaceInBytes = new File(configuration.getTilesRootDir()).getTotalSpace();
        report.freeSpaceInBytes = new File(configuration.getTilesRootDir()).getFreeSpace();

        return report;
    }

    public static DiskUsageReport buildV2(CleanerConfiguration configuration) throws IOException {
        final DiskUsageReport report = new DiskUsageReport();

        report.totalSpaceInBytes = new File(configuration.getTilesRootDir()).getTotalSpace();
        report.freeSpaceInBytes = new File(configuration.getTilesRootDir()).getFreeSpace();

        String testFileName = configuration.getTestFiles().get(0);

        Path root = Paths.get(configuration.getTilesRootDir());

        try (Stream<Path> walk = Files.walk(root, 3)) {

            walk
                    .parallel()
                    .filter(Files::isDirectory)
                    .filter(p -> p.getParent().getParent().getParent().equals(root)) // test ../../.. == root
                    .forEach(codeDir -> {

                report.totalSpecimenCount += 1;

                Path testFile = codeDir.resolve(testFileName);
                if (Files.exists(testFile)) {
                    report.completeSpecimenCount += 1;
                } else {
                    report.minimalSpecimenCount += 1;
                }

            });

        }

        return report;
    }

    @Deprecated
    public static DiskUsageReport buildV1(CleanerConfiguration configuration) throws IOException {
        final DiskUsageReport report = new DiskUsageReport();

        report.totalSpaceInBytes = new File(configuration.getTilesRootDir()).getTotalSpace();
        report.freeSpaceInBytes = new File(configuration.getTilesRootDir()).getFreeSpace();

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


    @Override
    public String toString() {
        return "DiskUsageReport{" +
                "totalSpaceInBytes=" + totalSpaceInBytes +
                ", freeSpaceInBytes=" + freeSpaceInBytes +
                ", totalSpecimenCount=" + totalSpecimenCount +
                ", completeSpecimenCount=" + completeSpecimenCount +
                ", minimalSpecimenCount=" + minimalSpecimenCount +
                '}';
    }
}

