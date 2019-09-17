package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import conf.Herbonautes;
import forms.SaveTagsForm;
import helpers.GsonUtils;
import models.Mission;
import models.Specimen;
import models.SpecimenMaster;
import models.User;
import models.discussions.Discussion;
import models.serializer.SpecimenSimpleJsonSerializer;
import models.serializer.TagSerializer;
import models.tags.Tag;
import models.tags.TagLink;
import models.tags.TagLinkType;
import models.tags.TagType;
import play.db.jpa.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by Jonathan on 17/06/2015.
 */
public class TagController extends Application {

    public static void getAllTags() {
        renderJSON(Tag.findAllOrdered());
    }

    @Transactional(readOnly = true)
    public static void show(String tagLabel) {
        Tag tag = Tag.findByLabel(tagLabel);
        List<Mission> missions = Mission.getPublishedMissionsByTag(tagLabel);
        boolean subscribedTag = false;
        if(Security.isConnected()) {
            User user = User.findById(Security.connectedUser().getId());
            subscribedTag = user.getTags().contains(tag);
        }
        int nbSpecimens = Specimen.countSpecimensByTag(tagLabel);
        int nbDiscussions = Discussion.getDiscussionsCount(tagLabel, tag.getTagType());
        String tagType = tag.getTagType().toString();

        Long forceDiscussion = params.get("discussion") != null ? Long.valueOf(params.get("discussion")) : null;

        render(tag, missions, subscribedTag, nbSpecimens, nbDiscussions, tagType, forceDiscussion);
    }

    @Transactional()
    public static void subscribeTag(String tagLabel) {
        if(Security.isConnected()) {
            Tag tag = Tag.findByLabel(tagLabel);
            if(tag != null) {
                User user = User.findById(Security.connectedUser().getId());
                user.getTags().add(tag);
                user.save();
            }
        }

    }

    @Transactional()
    public static void unsubscribeTag(String tagLabel) {
        if(Security.isConnected()) {
            Tag tag = Tag.findByLabel(tagLabel);
            if(tag != null) {
                User user = User.findById(Security.connectedUser().getId());
                user.getTags().remove(tag);
                user.save();
            }
        }
    }

    @Transactional(readOnly = true)
    public static void getAllSubscribedTags() {
        if(Security.isConnected()) {
            User user = User.findById(Security.connectedUser().getId());
            GsonBuilder gsonBuilder = GsonUtils.getGsonBuilder();
            gsonBuilder.registerTypeAdapter(Tag.class, TagSerializer.get());
            renderJSON(gsonBuilder.create().toJson(user.getTags()));
        } else {
            error();
        }
    }

    @Transactional(readOnly = true)
    public static void getSpecimensByTag(String tagLabel) {
        List<SpecimenMaster> specimens = Specimen.getSpecimensByTag(tagLabel);
        GsonBuilder gsonBuilder = GsonUtils.getGsonBuilder();
        gsonBuilder.registerTypeAdapter(Specimen.class, SpecimenSimpleJsonSerializer.get());
        renderJSON(gsonBuilder.create().toJson(specimens));
    }


    @Transactional()
    public static void saveElementTags() {
        Gson gson = new Gson();
        if(!Security.isConnected() || Security.connectedUser().getLevel() < Herbonautes.get().saveTagsElementMinLevel && !Security.isAdmin()) {
            error();
        }
        SaveTagsForm form = gson.fromJson(request.params.get("body"), SaveTagsForm.class);
        User user = User.findById(Security.connectedUser().getId());
        boolean subscribeTags = false;
        for(Tag t : form.getTagsToAdd()) {
            if(!form.getElementTags().contains(t.getTagLabel())) {
                Tag tag = Tag.findByLabel(t.getTagLabel());
                if(tag == null) {
                    tag = new Tag();
                    tag.setTagLabel(t.getTagLabel());
                    tag.setTagType(TagType.USER);
                    tag.setPublished(true);
                    tag.create();
                    tag.refresh();
                }
                createTagLink(tag.getId(), TagLinkType.valueOf(form.getTagLinkType()), form.getTargetId(), false);
                if(!user.getTags().contains(tag)) {
                    subscribeTags = true;
                    user.getTags().add(tag);
                }
            }
        }
        if(subscribeTags) {
            user.save();
        }
        if(TagLinkType.valueOf(form.getTagLinkType()).equals(TagLinkType.MISSION)) {
            Mission mission = Mission.findById(form.getTargetId());
            mission.save();
        }else if (TagLinkType.valueOf(form.getTagLinkType()).equals(TagLinkType.SPECIMEN)) {
            SpecimenMaster specimen = SpecimenMaster.findById(form.getTargetId());
            specimen.save();
        }
        ok();
    }

    @Transactional()
    public static void deleteElementTags(Long tagId, String tagLinkType, Long targetId, String principalTagLabel) {
        Tag tag = Tag.findById(tagId);
        if(!principalTagLabel.equals(tag.getTagLabel())) {
            Gson gson = new Gson();
            if(!Security.isConnected() || Security.connectedUser().getLevel() < Herbonautes.get().saveTagsElementMinLevel && !Security.isAdmin()) {
                error();
            }
            TagLink.deleteByTagIdLinkTypeAndTargetId(tagId, TagLinkType.valueOf(tagLinkType), targetId);
            ok();
        } else {
            error();
        }
    }

    private static void createTagLink(Long tagId, TagLinkType linkType, Long targetId, boolean principal) {
        TagLink tagLink = new TagLink();
        tagLink.setLinkType(linkType);
        tagLink.setTagId(tagId);
        tagLink.setTargetId(targetId);
        tagLink.setPrincipal(principal);
        tagLink.create();
    }

    @Transactional(readOnly = true)
    public static void loadTagsByElement(TagLinkType tagLinkType, Long targetId) {
        List<Tag> tags = Tag.findByLinkTypeAndTargetId(tagLinkType, targetId);
        GsonBuilder gsonBuilder = GsonUtils.getGsonBuilder();
        gsonBuilder.registerTypeAdapter(Tag.class, TagSerializer.get());
        renderJSON(gsonBuilder.create().toJson(tags));
    }

}
