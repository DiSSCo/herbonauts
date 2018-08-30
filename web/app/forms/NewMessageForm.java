package forms;

import models.tags.Tag;

import java.util.List;

/**
 * Created by Jonathan on 30/06/2015.
 */
public class NewMessageForm {

    private String text;

    private String tagType;

    private String tagLabel;

    private Long discussionId;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getDiscussionId() {
        return discussionId;
    }

    public void setDiscussionId(Long discussionId) {
        this.discussionId = discussionId;
    }

    public String getTagType() {
        return tagType;
    }

    public void setTagType(String tagType) {
        this.tagType = tagType;
    }

    public String getTagLabel() {
        return tagLabel;
    }

    public void setTagLabel(String tagLabel) {
        this.tagLabel = tagLabel;
    }
}
