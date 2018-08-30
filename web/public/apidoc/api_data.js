define({ "api": [
  {
    "type": "get",
    "url": "/api/getAllMissions",
    "title": "Request all missions datas",
    "name": "getAllMissions",
    "group": "Missions",
    "success": {
      "examples": [
        {
          "title": "Success-Response:",
          "content": "    HTTP/1.1 200 OK\n    {\n\t\t\t[\n\t\t\t\t{\n\t\t\t\t\t\"id\": 184987,\n\t\t\t\t\t\"imageId\": 302819,\n\t\t\t\t\t\"title\": \"TST Les Ficus\"\n\t\t\t\t},\n\t\t\t\t{\n\t\t\t\t\t\"id\": 201766,\n\t\t\t\t\t\"imageId\": 211318,\n\t\t\t\t\t\"title\": \"TST Suivez Benjamin Balansa\"\n\t\t\t\t},\n\t\t\t\t{\n\t\t\t\t\t\"id\": 70794,\n\t\t\t\t\t\"imageId\": 70792,\n\t\t\t\t\t\"title\": \"TST Mission impossible\"\n\t\t\t\t},\n\t\t\t\t{\n\t\t\t\t\t\"id\": 574447,\n\t\t\t\t\t\"imageId\": 574592,\n\t\t\t\t\t\"title\": \"TST Juste le pays (�pisode 2)\"\n\t\t\t\t},\n\t\t\t\t{\n\t\t\t\t\t\"id\": 762,\n\t\t\t\t\t\"imageId\": 734,\n\t\t\t\t\t\"title\": \"TST Orchid�es de France, Le genre Platanthera\"\n\t\t\t\t},\n\t\t\t\t{\n\t\t\t\t\t\"id\": 1565,\n\t\t\t\t\t\"title\": \"TST Cypripedium de France\"\n\t\t\t\t}\n\t\t\t]\n    }",
          "type": "json"
        }
      ]
    },
    "version": "0.0.0",
    "filename": "./ApiController.java",
    "groupTitle": "Missions"
  },
  {
    "type": "get",
    "url": "/api/getDiscussionsByMissionId/:missionId",
    "title": "Request all discussions by mission Id",
    "name": "getDiscussionsByMissionId",
    "group": "Missions",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "<p>Number</p> ",
            "optional": false,
            "field": "missionId",
            "description": "<p>mission Id</p> "
          }
        ]
      }
    },
    "success": {
      "examples": [
        {
          "title": "Success-Response:",
          "content": "    HTTP/1.1 200 OK\n    {\n\t\t[\n\t\t\t{\n\t\t\t\t\"id\": 4056574,\n\t\t\t\t\"title\": \"Anciens commentaires\",\n\t\t\t\t\"resolved\": true,\n\t\t\t\t\"author\": \"MarcP\",\n\t\t\t\t\"messages\": [\n\t\t\t\t\t{\n\t\t\t\t\t\t\"id\": 4056822,\n\t\t\t\t\t\t\"author\": \"MarcP\",\n\t\t\t\t\t\t\"discussionId\": 4056574,\n\t\t\t\t\t\t\"resolution\": false,\n\t\t\t\t\t\t\"date\": 1363367451873,\n\t\t\t\t\t\t\"imageId\": 82528,\n\t\t\t\t\t\t\"moderator\": \"\",\n\t\t\t\t\t\t\"text\": \"Chers participants, cette mission vous surprend peut-�tre. On vous promet des Ficus et vous voyez apparaitre des Malva... En fait nous avons utilis� un logiciel de reconnaissance de caract�res. Il suffit que le mot Ficus figure sur l'�tiquette pour qu'elle soit s�lectionn�e!\",\n\t\t\t\t\t\t\"first\": true\n\t\t\t\t\t},\n\t\t\t\t\t{\n\t\t\t\t\t\t\"id\": 4056848,\n\t\t\t\t\t\t\"author\": \"tkoffel\",\n\t\t\t\t\t\t\"discussionId\": 4056574,\n\t\t\t\t\t\t\"resolution\": false,\n\t\t\t\t\t\t\"date\": 1364035722843,\n\t\t\t\t\t\t\"imageId\": 399761,\n\t\t\t\t\t\t\"moderator\": \"\",\n\t\t\t\t\t\t\"text\": \"D'une mani�re g�n�rale, lorsque l'on renseigne la localisation, faut-il inclure les pr�cisions sur l'altitude exacte (ou approximative) et l'allure g�n�rale du milieu du pr�l�vement (ceux-ci donnant tous les deux des pr�cisions sur la localisation) ?\",\n\t\t\t\t\t\t\"first\": false\n\t\t\t\t\t}\n\t\t\t\t],\n\t\t\t\t\"tags\": [\n\t\t\t\t\t{\n\t\t\t\t\t\t\"tagId\": 3875256,\n\t\t\t\t\t\t\"tagLabel\": \"MISSION 184987\",\n\t\t\t\t\t\t\"tagType\": \"MISSION\"\n\t\t\t\t\t}\n\t\t\t\t]\n\t\t\t}\n\t\t]\n    }",
          "type": "json"
        }
      ]
    },
    "version": "0.0.0",
    "filename": "./ApiController.java",
    "groupTitle": "Missions"
  },
  {
    "type": "get",
    "url": "/api/getMessagesByMissionId/:missionId",
    "title": "Request all messages by mission Id",
    "name": "getMessagesByMissionId",
    "group": "Missions",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "<p>Number</p> ",
            "optional": false,
            "field": "missionId",
            "description": "<p>mission Id</p> "
          }
        ]
      }
    },
    "success": {
      "examples": [
        {
          "title": "Success-Response:",
          "content": "    HTTP/1.1 200 OK\n    {\n\t\t\t[\n\t\t\t\t{\n\t\t\t\t\t\"id\": 4001505,\n\t\t\t\t\t\"author\": \"chlorofil\",\n\t\t\t\t\t\"discussionId\": 4001044,\n\t\t\t\t\t\"resolution\": false,\n\t\t\t\t\t\"date\": 1381852585487,\n\t\t\t\t\t\"imageId\": 137,\n\t\t\t\t\t\"moderator\": \"\",\n\t\t\t\t\t\"text\": \"La mission s'est termin�e sans que j'y contribue beaucoup ... je me rattraperai sur les suivantes.\",\n\t\t\t\t\t\"first\": false\n\t\t\t\t},\n\t\t\t\t{\n\t\t\t\t\t\"id\": 4057035,\n\t\t\t\t\t\"author\": \"chlorofil\",\n\t\t\t\t\t\"discussionId\": 4056574,\n\t\t\t\t\t\"resolution\": false,\n\t\t\t\t\t\"date\": 1381852585487,\n\t\t\t\t\t\"imageId\": 137,\n\t\t\t\t\t\"moderator\": \"\",\n\t\t\t\t\t\"text\": \"La mission s'est termin�e sans que j'y contribue beaucoup ... je me rattraperai sur les suivantes.\",\n\t\t\t\t\t\"first\": false\n\t\t\t\t}\n\t\t\t]\n    }",
          "type": "json"
        }
      ]
    },
    "version": "0.0.0",
    "filename": "./ApiController.java",
    "groupTitle": "Missions"
  },
  {
    "type": "get",
    "url": "/api/getMissionById/:missionId",
    "title": "Request all missions datas",
    "name": "getMissionById",
    "group": "Missions",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "<p>Number</p> ",
            "optional": false,
            "field": "missionId",
            "description": "<p>mission Id</p> "
          }
        ]
      }
    },
    "success": {
      "examples": [
        {
          "title": "Success-Response:",
          "content": "    HTTP/1.1 200 OK\n    {\n\t\t\t\"id\": 184987,\n\t\t\t\"imageId\": 302819,\n\t\t\t\"title\": \"TST Les Ficus\"\n    }",
          "type": "json"
        }
      ]
    },
    "version": "0.0.0",
    "filename": "./ApiController.java",
    "groupTitle": "Missions"
  },
  {
    "type": "get",
    "url": "/api/getTagsByMissionId/:missionId",
    "title": "Request all tags by mission Id",
    "name": "getTagsByMissionId",
    "group": "Missions",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "<p>Number</p> ",
            "optional": false,
            "field": "missionId",
            "description": "<p>mission Id</p> "
          }
        ]
      }
    },
    "success": {
      "examples": [
        {
          "title": "Success-Response:",
          "content": "    HTTP/1.1 200 OK\n    {\n\t\t\t[\n\t\t\t\t{\n\t\t\t\t\t\"tagId\": 3875256,\n\t\t\t\t\t\"tagLabel\": \"MISSION 184987\",\n\t\t\t\t\t\"tagType\": \"MISSION\"\n\t\t\t\t}\n\t\t\t]\n    }",
          "type": "json"
        }
      ]
    },
    "version": "0.0.0",
    "filename": "./ApiController.java",
    "groupTitle": "Missions"
  },
  {
    "type": "get",
    "url": "/api/getDiscussionsBySpecimenCode/:code",
    "title": "Request discussion for a specimen",
    "name": "getDiscussionsBySpecimenCode",
    "group": "Specimens",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "<p>String</p> ",
            "optional": false,
            "field": "code",
            "description": "<p>Specimen code</p> "
          }
        ]
      }
    },
    "success": {
      "examples": [
        {
          "title": "Success-Response:",
          "content": "    HTTP/1.1 200 OK\n    {\n\t\t[\n\t\t\t{\n\t\t\t\t\"id\": 4065318,\n\t\t\t\t\"title\": \"Anciens commentaires\",\n\t\t\t\t\"resolved\": true,\n\t\t\t\t\"author\": \"DBF\",\n\t\t\t\t\"messages\": [\n\t\t\t\t\t{\n\t\t\t\t\t\"id\": 4085329,\n\t\t\t\t\t\"author\": \"DBF\",\n\t\t\t\t\t\"discussionId\": 4065318,\n\t\t\t\t\t\"resolution\": false,\n\t\t\t\t\t\"date\": 1391635489225,\n\t\t\t\t\t\"imageId\": 1442213,\n\t\t\t\t\t\"moderator\": \"\",\n\t\t\t\t\t\"text\": \"Sp�cimen provenant d'un jardin, donc cultiv�, pas de mention de pays, ni de r�gion, ni de g�olocalisation.\",\n\t\t\t\t\t\"first\": true\n\t\t\t\t\t}\n\t\t\t\t],\n\t\t\t\t\"tags\": [\n\t\t\t\t\t{\n\t\t\t\t\t\t\"tagId\": 3882074,\n\t\t\t\t\t\t\"tagLabel\": \"AIX015957\",\n\t\t\t\t\t\t\"tagType\": \"SPECIMEN\"\n\t\t\t\t\t}\n\t\t\t\t]\n\t\t\t},\n\t\t\t{\n\t\t\t\t\"id\": 4009788,\n\t\t\t\t\"resolved\": true,\n\t\t\t\t\"author\": \"DBF\",\n\t\t\t\t\"messages\": [\n\t\t\t\t\t{\n\t\t\t\t\t\t\"id\": 4029799,\n\t\t\t\t\t\t\"author\": \"DBF\",\n\t\t\t\t\t\t\"discussionId\": 4009788,\n\t\t\t\t\t\t\"resolution\": false,\n\t\t\t\t\t\t\"date\": 1391635489225,\n\t\t\t\t\t\t\"imageId\": 1442213,\n\t\t\t\t\t\t\"moderator\": \"\",\n\t\t\t\t\t\t\"text\": \"Sp�cimen provenant d'un jardin, donc cultiv�, pas de mention de pays, ni de r�gion, ni de g�olocalisation.\",\n\t\t\t\t\t\t\"first\": true\n\t\t\t\t\t}\n\t\t\t\t],\n\t\t\t\t\"tags\": [\n\t\t\t\t\t{\n\t\t\t\t\t\t\"tagId\": 3882074,\n\t\t\t\t\t\t\"tagLabel\": \"AIX015957\",\n\t\t\t\t\t\t\"tagType\": \"SPECIMEN\"\n\t\t\t\t\t}\n\t\t\t\t]\n\t\t\t}\n\t\t]\n    }",
          "type": "json"
        }
      ]
    },
    "version": "0.0.0",
    "filename": "./ApiController.java",
    "groupTitle": "Specimens"
  },
  {
    "type": "get",
    "url": "/api/getMessagesBySpecimenCode/:code",
    "title": "Request all messages for a specimen",
    "name": "getMessagesBySpecimenCode",
    "group": "Specimens",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "<p>String</p> ",
            "optional": false,
            "field": "code",
            "description": "<p>Specimen code</p> "
          }
        ]
      }
    },
    "success": {
      "examples": [
        {
          "title": "Success-Response:",
          "content": "    HTTP/1.1 200 OK\n    {\n\t\t\t[\n\t\t\t\t{\n\t\t\t\t\t\"id\": 4029799,\n\t\t\t\t\t\"author\": \"DBF\",\n\t\t\t\t\t\"discussionId\": 4009788,\n\t\t\t\t\t\"resolution\": false,\n\t\t\t\t\t\"date\": 1391635489225,\n\t\t\t\t\t\"imageId\": 1442213,\n\t\t\t\t\t\"moderator\": \"\",\n\t\t\t\t\t\"text\": \"Sp�cimen provenant d'un jardin, donc cultiv�, pas de mention de pays, ni de r�gion, ni de g�olocalisation.\",\n\t\t\t\t\t\"first\": true\n\t\t\t\t},\n\t\t\t\t{\n\t\t\t\t\t\"id\": 4085329,\n\t\t\t\t\t\"author\": \"DBF\",\n\t\t\t\t\t\"discussionId\": 4065318,\n\t\t\t\t\t\"resolution\": false,\n\t\t\t\t\t\"date\": 1391635489225,\n\t\t\t\t\t\"imageId\": 1442213,\n\t\t\t\t\t\"moderator\": \"\",\n\t\t\t\t\t\"text\": \"Sp�cimen provenant d'un jardin, donc cultiv�, pas de mention de pays, ni de r�gion, ni de g�olocalisation.\",\n\t\t\t\t\t\"first\": true\n\t\t\t\t}\n\t\t\t]\n    }",
          "type": "json"
        }
      ]
    },
    "version": "0.0.0",
    "filename": "./ApiController.java",
    "groupTitle": "Specimens"
  },
  {
    "type": "get",
    "url": "/api/getSpecimenDatas/:code",
    "title": "Request specimen datas",
    "name": "getSpecimenDatas",
    "group": "Specimens",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "<p>String</p> ",
            "optional": false,
            "field": "code",
            "description": "<p>Specimen code</p> "
          }
        ]
      }
    },
    "success": {
      "examples": [
        {
          "title": "Success-Response:",
          "content": "    HTTP/1.1 200 OK\n    {\n\t\t\"id\": 1287609,\n\t\t\"family\": \"Iris\",\n\t\t\"genus\": \"unguicularis\",\n\t\t\"institute\": \"MHNAIX\",\n\t\t\"collection\": \"AIX\",\n\t\t\"code\": \"AIX015957\",\n\t\t\"sonneratURL\": \"http://dsiphoto.mnhn.fr/sonnerat/AIX/FOUILLOY/08/AIX015957.jpg\",\n\t\t\"tw\": 429,\n\t\t\"th\": 348\n    }",
          "type": "json"
        }
      ]
    },
    "version": "0.0.0",
    "filename": "./ApiController.java",
    "groupTitle": "Specimens"
  },
  {
    "type": "get",
    "url": "/api/getSpecimensByMissionId/:missionId",
    "title": "Request all specimens datas by mission Id",
    "name": "getSpecimensByMissionId",
    "group": "Specimens",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "<p>Number</p> ",
            "optional": false,
            "field": "missionId",
            "description": "<p>mission Id</p> "
          }
        ]
      }
    },
    "success": {
      "examples": [
        {
          "title": "Success-Response:",
          "content": "    HTTP/1.1 200 OK\n    {\n\t\t\t[\n\t\t\t\t{\n\t\t\t\t\t\"id\": 194902,\n\t\t\t\t\t\"family\": \"Ficus\",\n\t\t\t\t\t\"genus\": \"austro-caledonica\",\n\t\t\t\t\t\"institute\": \"MNHN\",\n\t\t\t\t\t\"collection\": \"P\",\n\t\t\t\t\t\"code\": \"P06751015\",\n\t\t\t\t\t\"sonneratURL\": \"http://sonneratphoto.mnhn.fr/2012/11/12/13/P06751015.jpg\",\n\t\t\t\t\t\"tw\": 207,\n\t\t\t\t\t\"th\": 325\n\t\t\t\t},\n\t\t\t\t{\n\t\t\t\t\t\"id\": 194903,\n\t\t\t\t\t\"family\": \"Ficus\",\n\t\t\t\t\t\"genus\": \"nitidifolia\",\n\t\t\t\t\t\"institute\": \"MNHN\",\n\t\t\t\t\t\"collection\": \"P\",\n\t\t\t\t\t\"code\": \"P06753795\",\n\t\t\t\t\t\"sonneratURL\": \"http://sonneratphoto.mnhn.fr/2012/11/09/8/P06753795.jpg\",\n\t\t\t\t\t\"tw\": 209,\n\t\t\t\t\t\"th\": 323\n\t\t\t\t},\n\t\t\t\t{\n\t\t\t\t\t\"id\": 194904,\n\t\t\t\t\t\"family\": \"Ficus\",\n\t\t\t\t\t\"genus\": \"nitidifolia\",\n\t\t\t\t\t\"institute\": \"MNHN\",\n\t\t\t\t\t\"collection\": \"P\",\n\t\t\t\t\t\"code\": \"P06753797\",\n\t\t\t\t\t\"sonneratURL\": \"http://sonneratphoto.mnhn.fr/2012/11/09/8/P06753797.jpg\",\n\t\t\t\t\t\"tw\": 207,\n\t\t\t\t\t\"th\": 327\n\t\t\t\t}\n\t\t\t]\n    }",
          "type": "json"
        }
      ]
    },
    "version": "0.0.0",
    "filename": "./ApiController.java",
    "groupTitle": "Specimens"
  },
  {
    "type": "get",
    "url": "/api/getTagsBySpecimenCode/:code",
    "title": "Request all tags for a specimen",
    "name": "getTagsBySpecimenCode",
    "group": "Specimens",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "<p>String</p> ",
            "optional": false,
            "field": "code",
            "description": "<p>Specimen code</p> "
          }
        ]
      }
    },
    "success": {
      "examples": [
        {
          "title": "Success-Response:",
          "content": "    HTTP/1.1 200 OK\n    {\n\t\t\t[\n\t\t\t\t{\n\t\t\t\t\t\"tagId\": 3882074,\n\t\t\t\t\t\"tagLabel\": \"AIX015957\",\n\t\t\t\t\t\"tagType\": \"SPECIMEN\"\n\t\t\t\t}\n\t\t\t]\n    }",
          "type": "json"
        }
      ]
    },
    "version": "0.0.0",
    "filename": "./ApiController.java",
    "groupTitle": "Specimens"
  }
] });