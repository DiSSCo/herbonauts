package forms;

import models.tags.Tag;

import java.util.List;

/**
 * Created by Jonathan on 25/06/2015.
 */
public class SaveTagsForm {

    private List<Tag> tagsToAdd;

    private List<String> elementTags;

    private String tagLinkType;

    private String principalTagLabel;

    private Long targetId;

    public List<String> getElementTags() {
        return elementTags;
    }

    public void setElementTags(List<String> elementTags) {
        this.elementTags = elementTags;
    }

    public List<Tag> getTagsToAdd() {
        return tagsToAdd;
    }

    public void setTagsToAdd(List<Tag> tagsToAdd) {
        this.tagsToAdd = tagsToAdd;
    }

    public String getTagLinkType() {
        return tagLinkType;
    }

    public void setTagLinkType(String tagLinkType) {
        this.tagLinkType = tagLinkType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getPrincipalTagLabel() {
        return principalTagLabel;
    }

    public void setPrincipalTagLabel(String principalTagLabel) {
        this.principalTagLabel = principalTagLabel;
    }

}
