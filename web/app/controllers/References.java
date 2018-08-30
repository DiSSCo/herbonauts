package controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import libs.Json;
import models.*;
import models.questions.ContributionQuestion;
import models.references.Reference;
import models.references.ReferenceRecord;
import models.references.ReferenceRecordInfo;
import models.serializer.QuestionSimpleJsonSerializer;
import models.serializer.contributions.ReferenceRecordFullJsonSerializer;
import models.serializer.contributions.ReferenceRecordSimpleJsonSerializer;
import org.apache.commons.lang.StringUtils;

import notifiers.Mails;

import models.activities.Activity;
import models.activities.MissionJoinActivity;
import models.comments.MissionComment;
import models.contributions.Contribution;
import models.serializer.BotanistLabelValueJsonSerializer;
import models.serializer.MissionJsonSerializer;
import models.serializer.UserJsonSerializer;
import models.serializer.contributions.BotanistsContributionJsonSerializer;
import play.Logger;
import play.cache.Cache;
import play.db.jpa.JPA;
import services.JPAUtils;
import services.Page;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;


/**
 * Controller regroupant les recherches et champs 
 * autocomplétés :
 * - accés rapide
 * - liste des botanits
 */
public class References extends Application {

    private static ObjectMapper mapper = new ObjectMapper();

	public static void regions1ByCountry(Long countryId) {
		List<RegionLevel1> regions = 
			RegionLevel1.getRegionsForCountry(countryId);
		renderJSON(regions);
	}
	
	public static void regions2ByRegion1(Long region1Id) {
		List<RegionLevel2> regions = 
			RegionLevel2.getRegionsForRegion1(region1Id);
		renderJSON(regions);
	}
	
	public static void searchBotanists(String term) {
		if (StringUtils.isEmpty(term)) {
			badRequest();
		}
		List<Botanist> botanists = Botanist.search(term, 5);
		renderJSON(botanists);
	}

    // ~~~~~~~~~~~~~~~ V2 ~~~~~~~~~~~~~~~

    public static void adminIndex() {
        List<Reference> references = Reference.findAll();
        render(references);
    }

    public static void adminEdit(Long referenceId) {
        Reference reference = Reference.findById(referenceId);
        List<ReferenceRecord> records = Reference.findAllRecords(referenceId);
        render(reference, records);
    }

    public static void allReferences() {
        List<Reference> references = Reference.findAll();
        renderJSON(references);
    }

    public static void saveReference() throws IOException {
        Security.forbiddenIfNotTeam();

        Reference reference = mapper.readValue(request.body, Reference.class);
        Logger.info("Create reference : " + reference.getParent());
        reference.save();
        reference.refresh();
        renderJSON(reference);
    }

    public static void updateReference() throws IOException {
        Security.forbiddenIfNotTeam();

        Reference reference = mapper.readValue(request.body, Reference.class);
        Logger.info("Update reference : " + reference.id + " parent: " + reference.getParent());

        Reference existing = Reference.findById(reference.id);

        if (existing.getParent() != null) {
            if (reference.getParent() != null) {
                if (!existing.getParent().getId().equals(reference.getParent().getId())) {
                    // Changement de ref parent, on supprime les réferences
                    Logger.info("Remove parent links for ref");
                    JPA.em().createNativeQuery("UPDATE H_REFERENCE_RECORD SET PARENT_ID = NULL WHERE REFERENCE_ID = :refId")
                            .setParameter("refId", reference.id)
                            .executeUpdate();
                }
            }
        }

        existing.setLabel(reference.getLabel());
        existing.setParent(reference.getParent());

        existing.save();
        existing.refresh();
        renderJSON(existing);
    }

    public static void deleteReference(Long referenceId) throws IOException {

        Security.forbiddenIfNotTeam();

        List<ContributionQuestion> questions =  ContributionQuestion.findQuestionsUsingReference(referenceId);

        if (questions != null && questions.size() > 0) {
            forbidden();
        }

        Reference ref = Reference.findById(referenceId);
        ref.delete();

        ok();
    }

    public static void getReference(Long referenceId) {
        Reference reference = Reference.findById(referenceId);
        renderJSON(reference);
    }


    public static void deleteRecord(Long referenceId) throws IOException {
        List<ContributionQuestion> list = ContributionQuestion.findQuestionsUsingReference(referenceId);
        if (list.size() > 0) {
            badRequest("Unable to delete records when used in question");
            return;
        }

        // OK to delete unused record
        ReferenceRecord record = Json.parseReferenceRecord(mapper.readTree(request.body));
        if (record.id != null) {
            ReferenceRecord recordToDelete = ReferenceRecord.findById(record.id);
            recordToDelete.delete();
        }
        ok();
    }

    public static void saveRecord(Long referenceId) throws IOException {

        //ReferenceRecord record = mapper.readValue(request.body, ReferenceRecord.class);

        ReferenceRecord record = Json.parseReferenceRecord(mapper.readTree(request.body));

        ReferenceRecord recordToUpdate = null;
        List<ReferenceRecordInfo> infoToUpdate = null;
        if (record.id != null) {

            recordToUpdate = ReferenceRecord.findById(record.id);
            infoToUpdate = record.getInfo();

            if (recordToUpdate != null) {
                recordToUpdate.setLabel(record.getLabel());
                //recordToUpdate.setInfo(record.getInfo());
                //recordToUpdate.setInfo(record.getInfo());
                if (record.getParent() != null) {
                    recordToUpdate.setParent(record.getParent());
                }
                recordToUpdate.save();
                recordToUpdate.refresh();
            }



        } else {

            infoToUpdate = record.getInfo();

            Reference ref = Reference.findById(referenceId);
            record.setReference(ref);
            record.setInfo(null);
            record.save();
            record.refresh();

            recordToUpdate = record;
            //renderJSON(record, ReferenceRecordFullJsonSerializer.get());

        }


        // Update info
        ReferenceRecordInfo.delete("recordId = ?", recordToUpdate.id);
        if (infoToUpdate != null) {
            for (ReferenceRecordInfo i : infoToUpdate) {
                i.setRecordId(recordToUpdate.id);
                i.save();
            }
        }
        recordToUpdate.setInfo(infoToUpdate);

        if (recordToUpdate.getReference() != null && "synonym".equals(recordToUpdate.getReference().getName())) {
            Cache.delete("ref_synonyms");
        }

        renderJSON(recordToUpdate, ReferenceRecordFullJsonSerializer.get());
    }

    public static void searchRecord(Long referenceId, String term, Long parentId) {
        Logger.info("Term : " + term);
        List<ReferenceRecord> records = ReferenceRecord.search(referenceId, term, parentId, 10);
        renderJSON(records);
    }

    public static void questionsUsingReference(Long referenceId) {
        List<ContributionQuestion> list = ContributionQuestion.findQuestionsUsingReference(referenceId);
        renderJSON(list, QuestionSimpleJsonSerializer.get());
    }

    public static void getRecords(final Long referenceId, Integer page, Integer pageSize, String sortBy, String order, final String filter) {

        if (page == null) {
            page = 1;
        }
        if (pageSize == null) {
            pageSize = 10;
        }

        Logger.info("Records page %d", page);



        Page<ReferenceRecord> records = JPAUtils.getPage(ReferenceRecord.class, page, pageSize, new JPAUtils.PredicateBuilder() {

            @Override
            public Predicate buildPredicate(CriteriaBuilder cb, Root root) {

                String filterExpression = null;
                if (filter != null && filter.trim().length() > 0) {
                    filterExpression = "%" + filter.trim().toLowerCase() + "%";
                }

                if (filterExpression == null) {
                    return cb.and(
                            cb.equal(root.get("reference").get("id"), referenceId)
                    );
                } else {

                    return cb.and(
                            cb.equal(root.get("reference").get("id"), referenceId),
                            cb.like(cb.lower(root.get("label")), filterExpression)
                    );
                }
            }
        }, sortBy, order);

        renderJSON(records, ReferenceRecordFullJsonSerializer.get());
    }

    public static void allRecords(Long referenceId, Long parent, Boolean sample) {
        final List<ReferenceRecord> records;
        if (parent != null) {
            records = Reference.findAllRecords(referenceId, parent);
        } else {

            Reference reference = Reference.findById(referenceId);
            if (reference.getParent() != null) {
                // Référence avec parent sans parentID => liste vide

                if (Boolean.TRUE.equals(sample)) {
                    records = Reference.sampleRecords(referenceId);
                    ReferenceRecord etc = new ReferenceRecord();
                    etc.setLabel("...");
                    etc.id = 0l;
                    etc.setValue("...");
                    records.add(etc);
                } else {
                    records = new ArrayList<ReferenceRecord>();
                }
            } else {
                records = Reference.findAllRecords(referenceId);
            }
        }
        renderJSON(records, ReferenceRecordSimpleJsonSerializer.get());
    }

}
