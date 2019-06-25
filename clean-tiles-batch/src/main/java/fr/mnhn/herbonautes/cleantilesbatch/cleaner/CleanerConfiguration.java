package fr.mnhn.herbonautes.cleantilesbatch.cleaner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CleanerConfiguration {

    @Value("${cleaner.tilesRootDir}")
    private String tilesRootDir;

    @Value("${cleaner.apiTokens}")
    public List<String> apiTokens;

    @Value("${cleaner.keepFiles:tile_0_0_0.jpg}")
    public List<String> keepFiles;

    @Value("${cleaner.testFiles:original.jpg}")
    public List<String> testFiles;

    public String getTilesRootDir() {
        return tilesRootDir;
    }

    public void setTilesRootDir(String tilesRootDir) {
        this.tilesRootDir = tilesRootDir;
    }

    public List<String> getApiTokens() {
        return apiTokens;
    }

    public void setApiTokens(List<String> apiTokens) {
        this.apiTokens = apiTokens;
    }

    public List<String> getKeepFiles() {
        return keepFiles;
    }

    public void setKeepFiles(List<String> keepFiles) {
        this.keepFiles = keepFiles;
    }

    public List<String> getTestFiles() {
        return testFiles;
    }

    public void setTestFiles(List<String> testFiles) {
        this.testFiles = testFiles;
    }
}
