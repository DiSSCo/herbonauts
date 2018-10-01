package models;


import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="H_SPECIMEN_MEDIA")
public class SpecimenMedia extends Model {

    @Column(name = "SPECIMEN_ID")
    private Long specimenId;

    @Column(name = "MEDIA_NUMBER")
    private Long mediaNumber;

    @Column(name = "MEDIA_ID")
    private String mediaId;

    @Column(name = "URL")
    private String url;

    private Integer tileWidth = 200;
    private Integer tileHeight = 300;
    private boolean tiled = false;
    private boolean tilingError = false; // Pb rencontré par le batch

    public static void deleteMediaBySpecimenId(Long id) {
        SpecimenMedia.delete("specimenId = ?", id);
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

    public Integer getTileWidth() {
        return tileWidth;
    }

    public void setTileWidth(Integer tileWidth) {
        this.tileWidth = tileWidth;
    }

    public Integer getTileHeight() {
        return tileHeight;
    }

    public void setTileHeight(Integer tileHeight) {
        this.tileHeight = tileHeight;
    }

    public boolean isTiled() {
        return tiled;
    }

    public void setTiled(boolean tiled) {
        this.tiled = tiled;
    }

    public boolean isTilingError() {
        return tilingError;
    }

    public void setTilingError(boolean tilingError) {
        this.tilingError = tilingError;
    }
}
