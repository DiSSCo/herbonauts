package controllers;

import models.Mission;
import models.Specimen;
import models.discussions.Discussion;
import models.discussions.Message;
import models.serializer.*;
import models.tags.Tag;

import java.util.List;

/**
 * Created by Jonathan on 26/10/2015.
 */
public class ApiController extends Application {

    public static void redirectDoc() {
        redirect(request.url + "/index.html");
    }
    public static void redirectDoc2() {
        redirect(request.url + "index.html");
    }
    /**
     * @api {get} /api/getSpecimenDatas/:code Request specimen datas
     * @apiName getSpecimenDatas
     * @apiGroup Specimens
     *
     * @apiParam {String} code Specimen code
     *
     * @apiSuccessExample {json} Success-Response:
     *     HTTP/1.1 200 OK
     *     {
     *		"id": 1287609,
     *		"family": "Iris",
     *		"genus": "unguicularis",
     *		"institute": "MHNAIX",
     *		"collection": "AIX",
     *		"code": "AIX015957",
     *		"sonneratURL": "http://dsiphoto.mnhn.fr/sonnerat/AIX/FOUILLOY/08/AIX015957.jpg",
     *		"tw": 429,
     *		"th": 348,
     *	    mission: {
     *           id: 13322,
     *           title: "Mission test",
     *           description: "La description",
     *           specimenCount: 1000°
     *         }
     *     }
     **/
    public static void getSpecimenDatas (String code) {
        Specimen specimen = Specimen.findByCode(code);
        //renderJSON(specimen.getMission());
        renderJSON(specimen, SpecimenSimpleWithMissionJsonSerializer.get(), MissionApiJsonSerializer.get());
    }


    /**
     * @api {get} /api/getDiscussionsBySpecimenCode/:code Request discussion for a specimen
     * @apiName getDiscussionsBySpecimenCode
     * @apiGroup Specimens
     *
     * @apiParam {String} code Specimen code
     * @apiSuccessExample {json} Success-Response:
     *     HTTP/1.1 200 OK
     *     {
     *		[
     *			{
     *				"id": 4065318,
     *				"title": "Anciens commentaires",
     *				"resolved": true,
     *				"author": "DBF",
     *				"messages": [
     *					{
     *					"id": 4085329,
     *					"author": "DBF",
     *					"discussionId": 4065318,
     *					"resolution": false,
     *					"date": 1391635489225,
     *					"imageId": 1442213,
     *					"moderator": "",
     *					"text": "Sp�cimen provenant d'un jardin, donc cultiv�, pas de mention de pays, ni de r�gion, ni de g�olocalisation.",
     *					"first": true
     *					}
     *				],
     *				"tags": [
     *					{
     *						"tagId": 3882074,
     *						"tagLabel": "AIX015957",
     *						"tagType": "SPECIMEN"
     *					}
     *				]
     *			},
     *			{
     *				"id": 4009788,
     *				"resolved": true,
     *				"author": "DBF",
     *				"messages": [
     *					{
     *						"id": 4029799,
     *						"author": "DBF",
     *						"discussionId": 4009788,
     *						"resolution": false,
     *						"date": 1391635489225,
     *						"imageId": 1442213,
     *						"moderator": "",
     *						"text": "Sp�cimen provenant d'un jardin, donc cultiv�, pas de mention de pays, ni de r�gion, ni de g�olocalisation.",
     *						"first": true
     *					}
     *				],
     *				"tags": [
     *					{
     *						"tagId": 3882074,
     *						"tagLabel": "AIX015957",
     *						"tagType": "SPECIMEN"
     *					}
     *				]
     *			}
     *		]
     *     }
     **/
    public static void getDiscussionsBySpecimenCode (String code) {
        List<Discussion> discussions = Discussion.getDiscussionsBySpecimenCode(code);
        renderJSON(discussions, DiscussionSerializer.get());
    }

    /**
     * @api {get} /api/getMessagesBySpecimenCode/:code Request all messages for a specimen
     * @apiName getMessagesBySpecimenCode
     * @apiGroup Specimens
     *
     * @apiParam {String} code Specimen code
     * @apiSuccessExample {json} Success-Response:
     *     HTTP/1.1 200 OK
     *     {
     *			[
     *				{
     *					"id": 4029799,
     *					"author": "DBF",
     *					"discussionId": 4009788,
     *					"resolution": false,
     *					"date": 1391635489225,
     *					"imageId": 1442213,
     *					"moderator": "",
     *					"text": "Sp�cimen provenant d'un jardin, donc cultiv�, pas de mention de pays, ni de r�gion, ni de g�olocalisation.",
     *					"first": true
     *				},
     *				{
     *					"id": 4085329,
     *					"author": "DBF",
     *					"discussionId": 4065318,
     *					"resolution": false,
     *					"date": 1391635489225,
     *					"imageId": 1442213,
     *					"moderator": "",
     *					"text": "Sp�cimen provenant d'un jardin, donc cultiv�, pas de mention de pays, ni de r�gion, ni de g�olocalisation.",
     *					"first": true
     *				}
     *			]
     *     }
     **/
    public static void getMessagesBySpecimenCode (String code) {
        List<Message> messages = Message.getMessagesBySpecimenCode(code);
        renderJSON(messages, MessageSerializer.get());
    }

    /**
     * @api {get} /api/getTagsBySpecimenCode/:code Request all tags for a specimen
     * @apiName getTagsBySpecimenCode
     * @apiGroup Specimens
     *
     * @apiParam {String} code Specimen code
     * @apiSuccessExample {json} Success-Response:
     *     HTTP/1.1 200 OK
     *     {
     *			[
     *				{
     *					"tagId": 3882074,
     *					"tagLabel": "AIX015957",
     *					"tagType": "SPECIMEN"
     *				}
     *			]
     *     }
     **/
    public static void getTagsBySpecimenCode (String code) {
        List<Tag> tags = Tag.getTagsBySpecimenCode(code);
        renderJSON(tags, TagSerializer.get());
    }


    /**
     * @api {get} /api/getAllMissions Request all missions datas
     * @apiName getAllMissions
     * @apiGroup Missions
     * @apiSuccessExample {json} Success-Response:
     *     HTTP/1.1 200 OK
     *     {
     *			[
     *				{
     *					"id": 184987,
     *					"imageId": 302819,
     *					"title": "TST Les Ficus"
     *				},
     *				{
     *					"id": 201766,
     *					"imageId": 211318,
     *					"title": "TST Suivez Benjamin Balansa"
     *				},
     *				{
     *					"id": 70794,
     *					"imageId": 70792,
     *					"title": "TST Mission impossible"
     *				},
     *				{
     *					"id": 574447,
     *					"imageId": 574592,
     *					"title": "TST Juste le pays (�pisode 2)"
     *				},
     *				{
     *					"id": 762,
     *					"imageId": 734,
     *					"title": "TST Orchid�es de France, Le genre Platanthera"
     *				},
     *				{
     *					"id": 1565,
     *					"title": "TST Cypripedium de France"
     *				}
     *			]
     *     }
     **/
    public static void getAllMissions () {
        List<Mission> missions = Mission.findAll();
        renderJSON(missions, MissionSimpleJsonSerializer.get());
    }


    /**
     * @api {get} /api/getMissionById/:missionId Request all missions datas
     * @apiName getMissionById
     * @apiGroup Missions
     *
     * @apiParam {Number} missionId mission Id
     *
     * @apiSuccessExample {json} Success-Response:
     *     HTTP/1.1 200 OK
     *     {
     *			"id": 184987,
     *			"imageId": 302819,
     *			"title": "TST Les Ficus"
     *     }
     **/
    public static void getMissionById (Long missionId) {
        Mission mission = Mission.findById(missionId);
        if(!mission.isProposition() || mission.isPropositionValidated())
            renderJSON(mission, MissionSimpleJsonSerializer.get());
        else
            error();
    }

    /**
     * @api {get} /api/getSpecimensByMissionId/:missionId Request all specimens datas by mission Id
     * @apiName getSpecimensByMissionId
     * @apiGroup Specimens
     *
     * @apiParam {Number} missionId mission Id
     *
     * @apiSuccessExample {json} Success-Response:
     *     HTTP/1.1 200 OK
     *     {
     *			[
     *				{
     *					"id": 194902,
     *					"family": "Ficus",
     *					"genus": "austro-caledonica",
     *					"institute": "MNHN",
     *					"collection": "P",
     *					"code": "P06751015",
     *					"sonneratURL": "http://sonneratphoto.mnhn.fr/2012/11/12/13/P06751015.jpg",
     *					"tw": 207,
     *					"th": 325
     *				},
     *				{
     *					"id": 194903,
     *					"family": "Ficus",
     *					"genus": "nitidifolia",
     *					"institute": "MNHN",
     *					"collection": "P",
     *					"code": "P06753795",
     *					"sonneratURL": "http://sonneratphoto.mnhn.fr/2012/11/09/8/P06753795.jpg",
     *					"tw": 209,
     *					"th": 323
     *				},
     *				{
     *					"id": 194904,
     *					"family": "Ficus",
     *					"genus": "nitidifolia",
     *					"institute": "MNHN",
     *					"collection": "P",
     *					"code": "P06753797",
     *					"sonneratURL": "http://sonneratphoto.mnhn.fr/2012/11/09/8/P06753797.jpg",
     *					"tw": 207,
     *					"th": 327
     *				}
     *			]
     *     }
     **/
    public static void getSpecimensByMissionId (Long missionId) {
        List<Specimen> specimens = Specimen.getSpecimensByMissionId(missionId);
        renderJSON(specimens, SpecimenSimpleJsonSerializer.get());
    }

    /**
     * @api {get} /api/getDiscussionsByMissionId/:missionId Request all discussions by mission Id
     * @apiName getDiscussionsByMissionId
     * @apiGroup Missions
     *
     * @apiParam {Number} missionId mission Id
     *
     * @apiSuccessExample {json} Success-Response:
     *     HTTP/1.1 200 OK
     *     {
     *		[
     *			{
     *				"id": 4056574,
     *				"title": "Anciens commentaires",
     *				"resolved": true,
     *				"author": "MarcP",
     *				"messages": [
     *					{
     *						"id": 4056822,
     *						"author": "MarcP",
     *						"discussionId": 4056574,
     *						"resolution": false,
     *						"date": 1363367451873,
     *						"imageId": 82528,
     *						"moderator": "",
     *						"text": "Chers participants, cette mission vous surprend peut-�tre. On vous promet des Ficus et vous voyez apparaitre des Malva... En fait nous avons utilis� un logiciel de reconnaissance de caract�res. Il suffit que le mot Ficus figure sur l'�tiquette pour qu'elle soit s�lectionn�e!",
     *						"first": true
     *					},
     *					{
     *						"id": 4056848,
     *						"author": "tkoffel",
     *						"discussionId": 4056574,
     *						"resolution": false,
     *						"date": 1364035722843,
     *						"imageId": 399761,
     *						"moderator": "",
     *						"text": "D'une mani�re g�n�rale, lorsque l'on renseigne la localisation, faut-il inclure les pr�cisions sur l'altitude exacte (ou approximative) et l'allure g�n�rale du milieu du pr�l�vement (ceux-ci donnant tous les deux des pr�cisions sur la localisation) ?",
     *						"first": false
     *					}
     *				],
     *				"tags": [
     *					{
     *						"tagId": 3875256,
     *						"tagLabel": "MISSION 184987",
     *						"tagType": "MISSION"
     *					}
     *				]
     *			}
     *		]
     *     }
     **/
    public static void getDiscussionsByMissionId (Long missionId) {
        List<Discussion> discussions = Discussion.getDiscussionsByMissionId(missionId);
        renderJSON(discussions, DiscussionSerializer.get());
    }

    /**
     * @api {get} /api/getMessagesByMissionId/:missionId Request all messages by mission Id
     * @apiName getMessagesByMissionId
     * @apiGroup Missions
     *
     * @apiParam {Number} missionId mission Id
     *
     * @apiSuccessExample {json} Success-Response:
     *     HTTP/1.1 200 OK
     *     {
     *			[
     *				{
     *					"id": 4001505,
     *					"author": "chlorofil",
     *					"discussionId": 4001044,
     *					"resolution": false,
     *					"date": 1381852585487,
     *					"imageId": 137,
     *					"moderator": "",
     *					"text": "La mission s'est termin�e sans que j'y contribue beaucoup ... je me rattraperai sur les suivantes.",
     *					"first": false
     *				},
     *				{
     *					"id": 4057035,
     *					"author": "chlorofil",
     *					"discussionId": 4056574,
     *					"resolution": false,
     *					"date": 1381852585487,
     *					"imageId": 137,
     *					"moderator": "",
     *					"text": "La mission s'est termin�e sans que j'y contribue beaucoup ... je me rattraperai sur les suivantes.",
     *					"first": false
     *				}
     *			]
     *     }
     **/
    public static void getMessagesByMissionId (Long missionId) {
        List<Message> messages = Message.getMessagesByMissionId(missionId);
        renderJSON(messages, MessageSerializer.get());
    }

    /**
     * @api {get} /api/getTagsByMissionId/:missionId Request all tags by mission Id
     * @apiName getTagsByMissionId
     * @apiGroup Missions
     *
     * @apiParam {Number} missionId mission Id
     *
     * @apiSuccessExample {json} Success-Response:
     *     HTTP/1.1 200 OK
     *     {
     *			[
     *				{
     *					"tagId": 3875256,
     *					"tagLabel": "MISSION 184987",
     *					"tagType": "MISSION"
     *				}
     *			]
     *     }
     **/
    public static void getTagsByMissionId (Long missionId) {
        List<Tag> tags = Tag.getTagsByMissionId(missionId);
        renderJSON(tags, TagSerializer.get());
    }

}
