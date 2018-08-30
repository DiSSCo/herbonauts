package models;

import org.apache.commons.collections.CollectionUtils;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "H_MISSION_CART_QUERY")
public class MissionCartQuery extends Model {

    @Column(name="MISSION_ID")
    private Long missionId;

    @ManyToOne
    @JoinColumn(name="text_file_id")
    private TextFile textFile;

    @Column(name="HITS")
    private Long hits;

    @Column(name="SYNC")
    private Boolean sync = false;

    @Column(name="DELETED")
    private Boolean deleted = false;

    @Column(name="NO_COLLECT_INFO")
    private Boolean noCollectInfo = true;

    @Column(name="ALL_SELECTED")
    private Boolean allSelected = false;

    @Column(name="ALL_SELECTED_D")
    private Boolean allSelectedDraft = false;

    @ElementCollection
    @Column(name="SPECIMEN_ID")
    @CollectionTable(name="H_MISSION_CART_QUERY_SEL", joinColumns={
            @JoinColumn(name="CART_QUERY_ID")
    })
    private List<String> selection = Arrays.asList();

    @ElementCollection
    @Column(name="SPECIMEN_ID")
    @CollectionTable(name="H_MISSION_CART_QUERY_SEL_D", joinColumns={
            @JoinColumn(name="CART_QUERY_ID")
    })
    private List<String> selectionDraft = Arrays.asList();

    @ElementCollection
    @MapKeyColumn(name="FIELD_NAME")
    @Column(name="FIELD_VALUE")
    @CollectionTable(name="H_MISSION_CART_QUERY_TERM", joinColumns={
            @JoinColumn(name="CART_QUERY_ID")
    })
    private Map<String, String> terms = new HashMap<String, String>();

    public static MissionCartQuery findExistingSimilar(MissionCartQuery model) {
        MissionCartQuery existing = null;
        if (model.id != null) {
            existing = findById(model.id);
            if (existing != null) {
                return existing;
            }
        }

        List<MissionCartQuery> sameMission =
                MissionCartQuery.find("missionId = ?", model.getMissionId()).fetch();

        for (MissionCartQuery e : sameMission) {

            if (e.textFile != null) {
                // ignore text
                continue;
            }

            if (e.isSimilar(model)) {
                existing = e;
                break;
            }
        }


        return existing;
    }

    public Long getMissionId() {
        return missionId;
    }

    public void setMissionId(Long missionId) {
        this.missionId = missionId;
    }

    public Boolean getSync() {
        return sync;
    }

    public void setSync(Boolean sync) {
        this.sync = sync;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Boolean getNoCollectInfo() {
        return noCollectInfo;
    }

    public void setNoCollectInfo(Boolean noCollectInfo) {
        this.noCollectInfo = noCollectInfo;
    }

    public Boolean getAllSelected() {
        return allSelected;
    }

    public void setAllSelected(Boolean allSelected) {
        this.allSelected = allSelected;
    }

    public List<String> getSelection() {
        return selection;
    }

    public void setSelection(List<String> selection) {
        this.selection = selection;
    }

    public Map<String, String> getTerms() {
        return terms;
    }

    public void setTerms(Map<String, String> terms) {
        this.terms = terms;
    }

    public boolean isSimilar(MissionCartQuery model) {
        if (model.id != null) {
            return this.id.equals(model.id);
        }

        return
            (this.noCollectInfo == model.noCollectInfo) &&
            CollectionUtils.isEqualCollection(this.terms.entrySet(), model.terms.entrySet());
    }

    public Long getHits() {
        return hits;
    }

    public void setHits(Long hits) {
        this.hits = hits;
    }

    public Boolean getAllSelectedDraft() {
        return allSelectedDraft;
    }

    public void setAllSelectedDraft(Boolean allSelectedDraft) {
        this.allSelectedDraft = allSelectedDraft;
    }

    public List<String> getSelectionDraft() {
        return selectionDraft;
    }

    public void setSelectionDraft(List<String> selectionDraft) {
        this.selectionDraft = selectionDraft;
    }

    public TextFile getTextFile() {
        return textFile;
    }

    public void setTextFile(TextFile textFile) {
        this.textFile = textFile;
    }
}
