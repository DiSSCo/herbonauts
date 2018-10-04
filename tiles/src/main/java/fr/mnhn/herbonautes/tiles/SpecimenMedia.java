package fr.mnhn.herbonautes.tiles;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SpecimenMedia {

    private Long id;

    private Long specimenId;

    private Long mediaNumber;

    private String mediaId;

    private String url;

    public static SpecimenMedia create(ResultSet rs) {
        if (rs == null) return null;
        try {
            SpecimenMedia media = new SpecimenMedia();
            media.id = rs.getLong("ID");
            media.specimenId = rs.getLong("SPECIMEN_ID");
            media.mediaNumber = rs.getLong("MEDIA_NUMBER");
            media.mediaId = rs.getString("MEDIA_ID");
            media.url = rs.getString("URL");
            return media;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSpecimenId() {
        return specimenId;
    }

    public void setSpecimenId(Long specimenId) {
        this.specimenId = specimenId;
    }

    public Long getMediaNumber() {
        return mediaNumber;
    }

    public void setMediaNumber(Long mediaNumber) {
        this.mediaNumber = mediaNumber;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
