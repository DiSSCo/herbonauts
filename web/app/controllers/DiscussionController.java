package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import conf.Herbonautes;
import forms.DiscussionForm;
import forms.NewMessageForm;
import forms.SaveTagsForm;
import helpers.GsonUtils;
import inspectors.Event;
import inspectors.InspectorChain;
import models.Mission;
import models.Specimen;
import models.User;
import models.discussions.Discussion;
import models.discussions.DiscussionCategory;

import models.discussions.Message;
import models.serializer.DiscussionCategorySerializer;
import models.serializer.DiscussionSerializer;
import models.serializer.MessageSerializer;
import models.tags.Tag;
import models.tags.TagLink;
import models.tags.TagLinkType;
import models.tags.TagType;
import notifiers.Mails;
import play.db.jpa.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jonathan on 17/06/2015.
 */
public class DiscussionController extends Application {

    @Transactional(readOnly = true)
    public static void show(Long id) {
        Discussion discussion = Discussion.findById(id);
        render(discussion);
    }

    @Transactional(readOnly = true)
    public static void showAll() {
        render();
    }

    public static void getServerTime() {
        renderJSON(new Date().getTime());
    }

    public static void getDiscussionsCategories() {
        renderJSON(DiscussionCategory.findAllOrdered(), DiscussionCategorySerializer.get());
    }

    @Transactional(readOnly = true)
    public static void getDiscussionsFilteredByCategories(String tagLabel, TagType tagType, String categories, String loadedDiscussions) {
        if(categories != null && !categories.trim().isEmpty()) {
            Pattern pattern = Pattern.compile("[^0-9,]");
            Matcher matcher = pattern.matcher(categories);
            if(matcher.find()) {
                error();
            };
        }
        int maxResults = Herbonautes.get().nbDiscussionsToLoadPerCall + 1;
        List<Discussion> discussionList = Discussion.getDiscussionsFilteredByCategories(tagLabel, tagType, categories, loadedDiscussions, maxResults);
        renderJSON(discussionList, DiscussionSerializer.get());
    }

    @Transactional(readOnly = true)
    public static void getDiscussionById(Long id) {
        List<Discussion> discussionList = new ArrayList<Discussion>();
        Discussion discussion = Discussion.findById(id);
        discussionList.add(discussion);
        renderJSON(discussionList, DiscussionSerializer.get());
    }

    @Transactional(readOnly = true)
    public static void getDiscussionsCount(String tagLabel, TagType tagType) {
        Integer count = Discussion.getDiscussionsCount(tagLabel, tagType);
        renderText(count);
    }


    @Transactional(readOnly = true)
    public static void getLastMessages(String tagLabel, TagType tagType) {
        List<Message> messageList = Message.getLastMessages(tagLabel, tagType, Herbonautes.get().lastMessagesMaxResults);
        GsonBuilder gsonBuilder = GsonUtils.getGsonBuilder();
        gsonBuilder.registerTypeAdapter(Message.class, MessageSerializer.get());
        renderJSON(gsonBuilder.create().toJson(messageList));
    }

    @Transactional(readOnly = true)
     public static void getNewMessages(String tagLabel, TagType tagType, Long lastMessageId) {
        List<Message> messageList = Message.getNewMessages(tagLabel, tagType, lastMessageId);
        GsonBuilder gsonBuilder = GsonUtils.getGsonBuilder();
        gsonBuilder.registerTypeAdapter(Message.class, MessageSerializer.get());
        renderJSON(gsonBuilder.create().toJson(messageList));
    }

    @Transactional(readOnly = true)
    public static void getNewMessagesByDiscussionId(Long discussionId, Long lastMessageId) {
        List<Message> messageList = Message.getNewMessages(discussionId, lastMessageId);
        GsonBuilder gsonBuilder = GsonUtils.getGsonBuilder();
        gsonBuilder.registerTypeAdapter(Message.class, MessageSerializer.get());
        renderJSON(gsonBuilder.create().toJson(messageList));
    }


    @Transactional()
    public static void createDiscussion() {
        Gson gson = new Gson();
        List<Tag> tags = new ArrayList<Tag>();
        DiscussionForm form = gson.fromJson(request.params.get("body"), DiscussionForm.class);
        if(!Security.isConnected()
                || form.getText() == null
                || form.getText().trim().isEmpty()) {
            error();
        }
        if(Security.connectedUser().getLevel() < Herbonautes.get().discussionTagsMinLevel
                && (form.getTags().size() > 1
                || !form.getTags().get(0).getTagLabel().equals(form.getTagLabel()))) {
            error();
        }
        User user = User.findById(Security.connectedUser().getId());
        Discussion d = new Discussion();
        Message m = new Message();
        Date now = new Date();
        d.setTitle(form.getTitle());
        d.setCreationDate(now);
        d.setMessages(new ArrayList<Message>());
        d.setResolved(false);
        d.setAuthor(user);
        m.setText(form.getText());
        m.setAuthor(user);
        m.setCreationDate(now);
        m.setResolution(false);
        m.setDiscussion(d);
        m.setFirst(true);
        d.getMessages().add(m);
        if(form.getCategories() != null && !form.getCategories().isEmpty()) {
            d.setCategories(new ArrayList<DiscussionCategory>());
            for(Long id : form.getCategories()) {
                DiscussionCategory cat = DiscussionCategory.findById(id);
                d.getCategories().add(cat);
            }
        }
        d.create();
        boolean isDefaultTagExists = false;
        for(Tag t : form.getTags()) {
            if(t.getTagLabel().equals(form.getTagLabel())) {
                isDefaultTagExists = true;
            }
        }
        if(!isDefaultTagExists) {
            Tag tag = Tag.findByLabel(form.getTagLabel());
            tags.add(tag);
            createTagLink(tag.getId(), TagLinkType.DISCUSSION, d.getId(), true);
        }
        boolean subscribeTags = false;
        for(Tag t : form.getTags()) {
            Tag tag = Tag.findByLabel(t.getTagLabel());
            if(tag == null) {
                tag = new Tag();
                tag.setTagLabel(t.getTagLabel());
                tag.setTagType(TagType.USER);
                tag.setPublished(true);
                tag.create();
                tag.refresh();
            }
            tags.add(tag);
            createTagLink(tag.getId(), TagLinkType.DISCUSSION, d.getId(), t.getTagLabel().equals(form.getTagLabel()));
            if(!user.getTags().contains(tag)) {
                subscribeTags = true;
                user.getTags().add(tag);
            }
        }
        if(subscribeTags) {
            user.save();
        }
        Mails.mailAfterPostedMessage(d, Security.connectedUser());
        InspectorChain.get().inspect(Event.DISCUSSION_CREATED, Security.connectedUser());
        for(Tag tag : tags) {
            if(tag.getTagType().equals(TagType.SPECIMEN)) {
                InspectorChain.get().inspect(Event.COMMENT_SPECIMEN_ADDED, m, Specimen.findByCode(tag.getTagLabel()));
            } else if (tag.getTagType().equals(TagType.MISSION)) {
                InspectorChain.get().inspect(Event.COMMENT_MISSION_ADDED, m, Mission.findByPrincipalTag(tag.getTagLabel()));
            }
        }
        renderJSON(d, DiscussionSerializer.get());
    }

    private static void createTagLink(Long tagId, TagLinkType linkType, Long targetId, boolean principal) {
        TagLink tagLink = new TagLink();
        tagLink.setLinkType(linkType);
        tagLink.setTagId(tagId);
        tagLink.setTargetId(targetId);
        tagLink.setPrincipal(principal);
        tagLink.create();
    }

    @Transactional()
    public static void postMessage() {
        if(!Security.isConnected()) {
            error();
        }
        Gson gson = new Gson();
        NewMessageForm form = gson.fromJson(request.params.get("body"), NewMessageForm.class);
        Discussion discussion = Discussion.findById(form.getDiscussionId());
        if (!discussion.isResolved()) {
            Date now = new Date();
            Message message = new Message();
            message.setDiscussion(discussion);
            message.setText(form.getText().replaceAll("\\r?\\n", "<br />"));
            message.setCreationDate(now);
            message.setAuthor(Security.connectedUser());
            message.setResolution(false);
            message.setFirst(false);
            message.create();
            discussion.getMessages().add(message);
            discussion.save();
            Mails.mailAfterPostedMessage(discussion, Security.connectedUser());
            InspectorChain.get().inspect(Event.MESSAGE_POSTED, Security.connectedUser());
            List<Tag> tags = Tag.findByDiscussionId(discussion.getId());
            for(Tag tag : tags) {
                if(tag.getTagType().equals(TagType.SPECIMEN)) {
                    InspectorChain.get().inspect(Event.COMMENT_SPECIMEN_ADDED, message, Specimen.findByCode(tag.getTagLabel()));
                } else if (tag.getTagType().equals(TagType.MISSION)) {
                    InspectorChain.get().inspect(Event.COMMENT_MISSION_ADDED, message, Mission.findByPrincipalTag(tag.getTagLabel()));
                }
            }
            /*if(TagType.valueOf(form.getTagType()).equals(TagType.SPECIMEN)) {
                InspectorChain.get().inspect(Event.COMMENT_SPECIMEN_ADDED, message, Specimen.findByCode(form.getTagLabel()));
            } else if (TagType.valueOf(form.getTagType()).equals(TagType.MISSION)) {
                InspectorChain.get().inspect(Event.COMMENT_MISSION_ADDED, message, Mission.findByPrincipalTag(form.getTagLabel()));
            }*/
            GsonBuilder gsonBuilder = GsonUtils.getGsonBuilder();
            gsonBuilder.registerTypeAdapter(Message.class, MessageSerializer.get());
            renderJSON(gsonBuilder.create().toJsonTree(message));
        }
    }


    @Transactional()
    public static void solveDiscussion(Long messageId) {
        Message m = Message.findById(messageId);
        if(m != null && null == m.getModeratorLogin() && Security.isConnected() && (Security.connectedLogin().equals(m.getDiscussion().getAuthor().getLogin())
                || Security.isAdmin() || Security.isTeam() || Security.isLeader())) {
            m.setResolution(true);
            m.save();
            m.getDiscussion().setResolved(true);
            m.getDiscussion().save();
            GsonBuilder gsonBuilder = GsonUtils.getGsonBuilder();
            gsonBuilder.registerTypeAdapter(Message.class, MessageSerializer.get());
            InspectorChain.get().inspect(Event.DISCUSSION_RESOLVED, Security.connectedUser(), m);
            renderJSON(gsonBuilder.create().toJsonTree(m));
        } else {
            error();
        }
    }

    @Transactional()
    public static void deleteMessage(Long messageId) {
        Message m = Message.findById(messageId);
        if(m != null && m.getModeratorLogin() == null && Security.isConnected()) {
            GsonBuilder gsonBuilder = GsonUtils.getGsonBuilder();
            gsonBuilder.registerTypeAdapter(Message.class, MessageSerializer.get());
            if(Security.connectedLogin().equals(m.getAuthor().getLogin())) {
                if(m.isFirst()) {
                    m.getDiscussion().delete();
                } else {
                    m.delete();
                }
            } else if (Security.isAdmin() || Security.connectedLogin().equals(m.getDiscussion().getAuthor().getLogin())) {
                if(m.isFirst()) {
                     m.getDiscussion().delete();
                } else {
                    m.setModeratorLogin(Security.connectedUser().getLogin());
                    m.save();
                }
            }
            renderJSON(gsonBuilder.create().toJsonTree(m));
        } else {
            error();
        }
    }

    @Transactional()
    public static void saveDiscussionTags() {
        Gson gson = new Gson();
        if(!Security.isConnected() || Security.connectedUser().getLevel() < Herbonautes.get().discussionTagsMinLevel && !Security.isAdmin()) {
            error();
        }
        SaveTagsForm form = gson.fromJson(request.params.get("body"), SaveTagsForm.class);
        User user = User.findById(Security.connectedUser().getId());
        List<Tag> tags = new ArrayList<Tag>();
        TagLink.deleteByLinkTypeAndTargetId(TagLinkType.DISCUSSION, form.getTargetId());
        boolean isDefaultTagExists = false;
        for(Tag t : form.getTagsToAdd()) {
            if(t.getTagLabel().equals(form.getPrincipalTagLabel())) {
                isDefaultTagExists = true;
            }
        }
        if(!isDefaultTagExists) {
            Tag tag = Tag.findByLabel(form.getPrincipalTagLabel());
            tags.add(tag);
            createTagLink(tag.getId(), TagLinkType.DISCUSSION, form.getTargetId(), true);
        }
        boolean subscribeTags = false;
        for(Tag t : form.getTagsToAdd()) {
            Tag tag = Tag.findByLabel(t.getTagLabel());
            if(tag == null) {
                tag = new Tag();
                tag.setTagLabel(t.getTagLabel());
                tag.setTagType(TagType.USER);
                tag.setPublished(true);
                tag.create();
                tag.refresh();
            }
            tags.add(tag);
            createTagLink(tag.getId(), TagLinkType.DISCUSSION, form.getTargetId(), t.getTagLabel().equals(form.getPrincipalTagLabel()));
            if(!user.getTags().contains(tag)) {
                subscribeTags = true;
                user.getTags().add(tag);
            }
        }
        if(subscribeTags) {
            user.save();
        }
        Discussion discussion = Discussion.findById(form.getTargetId());
        discussion.save();
        ok();
    }

}
