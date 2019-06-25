package models;

import conf.Herbonautes;
import helpers.NumberUtils;
import helpers.RecolnatUser;
import models.badges.Badge;
import models.contributions.Contribution;
import models.contributions.DateContribution;
import models.contributions.GeolocalisationContribution;
import models.discussions.Discussion;
import models.discussions.Message;
import models.questions.ContributionAnswer;
import models.tags.Tag;
import org.apache.commons.codec.binary.Base64;
import play.Logger;
import play.cache.Cache;
import play.db.jpa.Blob;
import play.db.jpa.JPA;
import play.db.jpa.NoTransaction;
import services.JPAUtils;
import services.Page;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.math.BigInteger;
import java.util.*;

/**
 * Utilisateur
 */
@Entity
@Table(name="H_USER")
public class User extends DatedModificationsModel<User> {
	
	private String login;
	private String email;
	private String password;
	private String facebookUsername;
	private String facebookId;
	
	@Column(name="CURRENT_LEVEL")
	private Integer level = 1;
	
	@Column(name="PENDING_LEVEL")
	private Integer pendingLevel = 1; // Niveau eligible (quiz)
	
	// Personnal
	private String description;
	private String firstName;
	private String lastName;
	private Country country;
	private String city;
	private String address;
	private Date birthDate;
	
	// Image
	private Blob image;
	private Long imageId;
	private boolean hasImage;
	
	// Configuration
	private boolean receiveMails;
	private boolean publishInformations;
	private Date registrationDate;
	
	// Roles
	private boolean leader;
	private boolean admin;

	// Location
	private Double latitude;

	private Double longitude;

    @Column(name = "TEAM")
    private boolean team;

    @Column(name = "RECOLNAT_UUID")
    private String recolnatUUID;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "H_TAGS_SUBSCRIPTION", joinColumns = {@JoinColumn(name = "user_id", nullable = false)},
			inverseJoinColumns = {@JoinColumn(name = "tag_id", nullable = false)})
	private List<Tag> tags;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_UPDATE_DATE")
	public Date lastUpdateDate;

	@Column(name = "deleted")
	private Boolean deleted = false;

	@Column(name = "ALERT_MISSION")
	private boolean alertMission;

	@Column(name = "ALERT_SPECIMEN")
	private boolean alertSpecimen;

	public Date getLastUpdateDate() {
		return lastUpdateDate;
	}

	@Override
	@PreUpdate
	public void setLastUpdateDate() {
		this.lastUpdateDate = new Date();
	}
	
	public String getFullName() {
		if (getFirstName() != null && getLastName() != null) {
			return getFirstName() + " " + getLastName();
		} else {
			return "";
		}
	}
	
	@ManyToMany(fetch=FetchType.LAZY)
	@JoinTable(name="mission_user", 
			joinColumns = { @JoinColumn(name = "user_id", nullable = false) },
			inverseJoinColumns = { @JoinColumn(name ="mission_id", nullable = false) })
	private List<Mission> missions;

    @OneToMany
    @JoinColumn(name = "USER_ID")
    private List<PassedQuiz> passedQuizList;

	@OneToMany(fetch=FetchType.LAZY, mappedBy="user")
	private List<Badge> badges;
	
	public List<Mission> getMissions() {
		return Mission.find("select u.missions from User u where u.id = ? order by priority", this.id).fetch();
	}

	public List<MissionSimple> getSimpleMissions() {

		return JPA.em().createNativeQuery("select M.* from HV_MISSION_SIMPLE M INNER JOIN MISSION_USER J ON J.MISSION_ID = M.ID " +
				"WHERE J.USER_ID = :userId", MissionSimple.class)
				.setParameter("userId", this.id)
				.getResultList();

		//return Mission.find("select u.missions from User u where u.id = ? order by priority", this.id).fetch();
	}

    public List<Mission> getCurrentMissions() {
        return Mission.find("select u.missions from User u where u.id = ? and published = true and closed = false order by priority", this.id).fetch();
    }

	public List<MissionSimple> getAllMissions() {

		return (List<MissionSimple>) JPA.em().createNativeQuery("select m.* from hv_mission_simple m inner join mission_user mu " +
				"on mu.mission_id = m.id where mu.user_id = :userId", MissionSimple.class)
				.setParameter("userId", this.id)
				.getResultList();

		//return Mission.find("select u.missions from User u where u.id = ? order by priority", this.id).fetch();
	}

	/*public List<Mission> getAllMissionTitles() {

		return (List<Mission>) JPA.em().createNativeQuery("select m.* from h_mission m inner join mission_user mu " +
				"on mu.mission_id = m.id where mu.user_id = :userId", Mission.class)
				.setParameter("userId", this.id)
				.getResultList();

		//return Mission.find("select u.missions from User u where u.id = ? order by priority", this.id).fetch();
	}*/
	
	public List<Specimen> getLastContributedSpecimens(int page, int length) {
		int start = (page - 1) * length;
		/*return JPA.em()
			.createNativeQuery(
				"SELECT DISTINCT S.*" +
				"FROM H_SPECIMEN S " +
				"INNER JOIN " +
				"    (SELECT S.ID S_ID, C.ID C_ID " +
				"     FROM H_CONTRIBUTION C " +
				"     INNER JOIN H_SPECIMEN S ON C.SPECIMEN_ID = S.ID " +
				"     WHERE C.USER_ID = :userId AND C.CANCELED = 0 " +
				"     ORDER BY C.CREATION_DATE DESC) T " + 
				"ON S.ID = T.S_ID", Specimen.class)
			.setParameter("userId", this.id)
			.setFirstResult(start)
			.setMaxResults(length)
			.getResultList();*/
		
		List<Object[]> ids = JPA.em()
			.createNativeQuery(
					"SELECT S.ID, T.C_DATE " +
					"FROM H_SPECIMEN S " +
					"INNER JOIN " +
					  "  (SELECT S.ID S_ID, MAX(C.CREATION_DATE) C_DATE " +
					   "  FROM H_CONTRIBUTION C " +
					    " INNER JOIN H_SPECIMEN S ON C.SPECIMEN_ID = S.ID  "+
					    " WHERE C.USER_ID = :userId AND C.CANCELED = 0 "+
	            " GROUP BY S.ID) T "+
					"ON S.ID = T.S_ID "+
	        "ORDER BY T.C_DATE DESC")
	       .setParameter("userId", this.id)
	       .setFirstResult(start)
			.setMaxResults(length)
			.getResultList();
		
		List<Specimen> specimens = new ArrayList<Specimen>();
		for (Object[] id : ids) {
			long speId = NumberUtils.toBigInteger(id[0]).longValue();
			specimens.add((Specimen) Specimen.findById(speId));
		}
		return specimens;
	
	}
	
	
	public Map<Specimen, Map<String, Contribution>> getLastContributionsBySpecimenAndType(int page, int length) {
		List<Specimen> specimens = getLastContributedSpecimens(page, length);
		
		LinkedHashMap<Specimen, Map<String, Contribution>> result = new LinkedHashMap<Specimen, Map<String,Contribution>>();
		if (specimens == null) {
			return result;
		}
		for (Specimen specimen : specimens) {
			List<Contribution> contributions = specimen.getContributionsByLogin(this.getLogin());
			if (contributions == null) {
				continue;
			}
			HashMap<String, Contribution> contributionsByType = new HashMap<String, Contribution>();
			for (Contribution contribution : contributions) {
				contributionsByType.put(contribution.getType(), contribution);
			}
			result.put(specimen, contributionsByType);
		}
		
		return result;
	}
	

	public List<Mission> getLeadMissions() {
		if (!isLeader()) {
			return null;
		}

		List<Mission> missions = JPA.em().createNativeQuery("SELECT * FROM H_MISSION M INNER JOIN H_MISSION_LEADER ML ON M.ID = ML.MISSION_ID WHERE ML.USER_ID = :userId and proposition = 0 and closed = 0", Mission.class)
				.setParameter("userId", this.id)
				.getResultList();

		return missions;


		//return Mission.find("leader.id = ? and proposition = false and closed = false ", this.id).fetch();
	}
	
	// COUNTS
	
	public static User findByLogin(String login) {
		User user = Cache.get("huser_" + login, User.class);
		if (user == null) {
			Logger.debug("User %s not in cache", login);
			user = User.find("login = ?", login).first();
			if (user != null) {
				Cache.set("huser_" + login, user);
			}
		} else {
			Logger.debug("User %s from cache", login);
		}
		return user;
	}
	
	public static User findByLogin(String login, boolean fromCache) {
		if (!fromCache) {
			User user =  User.find("login = ?", login).first();
			Cache.replace("user_" + login, user);
			return user;
		} else {
			return findByLogin(login);
		}
	}
		
	@NoTransaction
	public Long getContributionsCount() {
		return User.getContributionsCount(this.id);
	}

	public static Long getContributionsCount(Long id) {
		Long count =
				count("select count(c) from ContributionAnswer c where c.deleted != true and c.userId = ?", id);
		return count;
	}
	
	@NoTransaction
	public static Long getContributionsCount(String login) {
		Long count = 
			count("select count(c) from Contribution c join c.user u " +
					"where c.canceled = false and u.login = ?", login);
		return count;
	}
	
	
	@NoTransaction
	public Long getSpecimensCount() {
		return User.getSpecimensCount(this.id);
	}
	
	@NoTransaction
	public static Long getSpecimensCount(String login) {
		Long count = 
			count("select count(distinct c.specimen) from ContributionAnswer c join c.user u " +
					"where c.canceled = false and u.login = ?", login);
		return count;
	}

	@NoTransaction
	public static Long getSpecimensCount(Long id) {
		Long count =
				count("select count(distinct c.specimenId) from ContributionAnswer c  " +
						"where c.deleted != true and c.userId = ?", id);
		return count;
	}
	
	// LEVELS
	public static Integer getLevel(String login) {
		User user = User.findByLogin(login);
		return user.level;
	}
	
	public static Integer getPendingLevel(String login) {
		User user = User.findByLogin(login);
		return user.pendingLevel;
	}
	
	public Integer levelUp() {
		this.setLevel(this.getLevel() + 1);
		this.save();
		uncache();
		return this.getLevel();
	}
	
	public Integer pendingLevelUp() {
		if (getPendingLevel() <= Herbonautes.get().maxLevel) {
			this.setPendingLevel(this.getPendingLevel() + 1);
			this.save();
			uncache();
		}
		return this.getPendingLevel();
	}
	
	public Integer pendingLevelUp(int newPending) {
		if (getPendingLevel() <= Herbonautes.get().maxLevel) {
			this.setPendingLevel(newPending);
			this.save();
			uncache();
		}
		return this.getPendingLevel();
	}
	
	public boolean hasBadge(Badge.Type type) {
		/*
		if (getBadges() == null) {
			return false;
		}
		for (Badge badge : getBadges()) {
			if (type.equals(Badge.Type.valueOf(badge.getType()))) {
				return true;
			}
		}
		*/

		return JPA.em().createNativeQuery("select * from H_BADGE where user_id = :userId and type = :type")
				.setParameter("userId", this.id)
				.setParameter("type", type.toString())
				.getResultList()
				.size() > 0;

		//return false;
	}
	
	private void uncache() {
		Cache.delete("huser_" + getLogin());
	}
	
	public UserContributionReport getContributionReport(Long missionId) {
		List<Object[]> dirtyReport = JPA.em().createNativeQuery("select " +
			"c.type as type ," +
			"count(c.type) as count " +
			"from H_CONTRIBUTION c " +
			"inner join H_MISSION m on c.mission_id = m.id " +
			"where c.user_id = ? and c.canceled = 0 and c.mission_id = ? " +
			"group by c.type")
			.setParameter(1, this.id)
			.setParameter(2, missionId)
			.getResultList();
		
		UserContributionReport report = new UserContributionReport();
		report.user = this;
		report.total = BigInteger.ZERO;
		
		report.countByType = new HashMap<String, BigInteger>();
		
		if (dirtyReport != null) {
			for (Object[] obj : dirtyReport) {
				report.countByType.put((String) obj[0], NumberUtils.toBigInteger(obj[1]));
				report.total = report.total.add(NumberUtils.toBigInteger(obj[1]));
			}
		}
			
		return report;
	}
	
	public static List<GeolocalisationContribution> getGeolocalisations(String login) {
		return GeolocalisationContribution.find(
				"canceled = false and " +
				"user.login = ? and " +
				"notPresent = false", login).fetch();
	}
	
	public static List<DateContribution> getDateContributions(String login) {
		return DateContribution.find(
				"canceled = false and " +
				"user.login = ? and " +
				"notPresent = false", login).fetch();
	}
	
	public static List<User> search(String q, int max) {
		return search(q, max, null);
	}
	
	public static List<User> search(String q, int max, Integer page) {
		if (q == null) {
			return new ArrayList<User>();
		}
		
		String likeValue = q.toLowerCase().replace(' ', '%');
		
		Integer realPage = (page == null ? 1 : page);
		
		List<User> users = User
			.find("lower(login) like ?", "%" + likeValue + "%")
			.fetch(realPage, max);
		
		return users;
	}
 	
	@PrePersist
	public void onCreate() {
		this.setReceiveMails(true);
		this.setAlertMission(true);
		this.setAlertSpecimen(true);
		this.setRegistrationDate(new Date());
		this.lastUpdateDate = new Date();
	}

    @PostUpdate
    public void clearCache() {
        this.uncache();
    }
	
	
	public void setLogin(String login) {
		this.login = login;
	}

	public String getLogin() {
		return login;
	}


	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setFacebookUsername(String facebookUsername) {
		this.facebookUsername = facebookUsername;
	}

	public String getFacebookUsername() {
		return facebookUsername;
	}

	public void setFacebookId(String facebookId) {
		this.facebookId = facebookId;
	}

	public String getFacebookId() {
		return facebookId;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public Integer getLevel() {
		return level;
	}

	public void setPendingLevel(Integer pendingLevel) {
		this.pendingLevel = pendingLevel;
	}

	public Integer getPendingLevel() {
		return pendingLevel;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public Country getCountry() {
		return country;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCity() {
		return city;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddress() {
		return address;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setImage(Blob image) {
		this.image = image;
	}

	public Blob getImage() {
		return image;
	}

	public void setImageId(Long imageId) {
		this.imageId = imageId;
	}

	public Long getImageId() {
		return imageId;
	}

	public void setHasImage(boolean hasImage) {
		this.hasImage = hasImage;
	}

	public boolean isHasImage() {
		return hasImage;
	}

	public void setReceiveMails(boolean receiveMails) {
		this.receiveMails = receiveMails;
	}

	public boolean isReceiveMails() {
		return receiveMails;
	}

	public void setPublishInformations(boolean publishInformations) {
		this.publishInformations = publishInformations;
	}

	public boolean isPublishInformations() {
		return publishInformations;
	}

	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}

	public Date getRegistrationDate() {
		return registrationDate;
	}

	public void setLeader(boolean leader) {
		this.leader = leader;
	}

	public boolean isLeader() {
		return leader;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setMissions(List<Mission> missions) {
		this.missions = missions;
	}

	public void setBadges(List<Badge> badges) {
		this.badges = badges;
	}

	public List<Badge> getBadges() {
		return badges;
	}

	public static class UserContributionReport {
		
		public Integer rank;
		
		public User user;
		
		public BigInteger total;
		
		public Map<String, BigInteger> countByType;
		
	}

    public String getRecolnatUUID() {
        return recolnatUUID;
    }

    public void setRecolnatUUID(String recolnatUUID) {
        this.recolnatUUID = recolnatUUID;
    }

    public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

    public boolean isTeam() {
        return team;
    }

    public void setTeam(boolean team) {
        this.team = team;
    }

    public List<PassedQuiz> getPassedQuizList() {
        return passedQuizList;
    }

    public void setPassedQuizList(List<PassedQuiz> passedQuizList) {
        this.passedQuizList = passedQuizList;
    }

    // Admin

    public static Page<User> getUsers(Integer page, Integer pageSize, String sortBy, String order) {

        if(page < 1) page = 1;

        /*JPAUtils.PredicateBuilder predicateBuilder = new JPAUtils.PredicateBuilder() {
            @Override
            public Predicate buildPredicate(CriteriaBuilder cb, Root root) {
                return cb.notEqual(root.get("deleted"), Boolean.TRUE);
            }
        };*/

        return JPAUtils.getPage(User.class, page, pageSize, sortBy, order);
        //return JPAUtils.get
    }

    public static Page<User> findUsers(String filter, Integer page, Integer pageSize, String sortBy, String order) {

        if (filter == null || filter.trim().length() == 0) {
            return getUsers(page, pageSize, sortBy, order);
        }

        final String filterParam = "%" + filter.trim().toLowerCase() + "%";

        if(page < 1) page = 1;


        JPAUtils.PredicateBuilder predicateBuilder = new JPAUtils.PredicateBuilder() {
            @Override
            public Predicate buildPredicate(CriteriaBuilder cb, Root root) {
                //cb.or()
                //root.
                return cb.and(
                        cb.or(
                                cb.like(cb.lower(root.get("login")), filterParam),
                                cb.like(cb.lower(root.get("firstName")), filterParam),
                                cb.like(cb.lower(root.get("lastName")), filterParam)
                        )
                        // ,cb.notEqual(root.get("deleted"), Boolean.TRUE)
                );
                //root.get("firstname"),
                //root.get("lastname");
                //return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };

        Logger.debug("Find users {} - {}", sortBy, order);

        return JPAUtils.getPage(User.class, page, pageSize, predicateBuilder, sortBy, order);

    }

	public static List<User> getUsersByDiscussionId(Long discussionId) {
		try {
			CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
			CriteriaQuery<User> query = cb.createQuery(User.class);
			Root<Discussion> from = query.from(Discussion.class);
			Join<Discussion, Message> messageJoin = from.join("messages");
			Join<Message, User> userJoin = messageJoin.join("author");
			List<Predicate> predicates = new ArrayList<Predicate>();
			predicates.add(cb.equal(from.get("id"), discussionId));
			query.select(userJoin).distinct(true);
			query.where(predicates.toArray(new Predicate[]{}));
			TypedQuery<User> createQuery = JPA.em().createQuery(query);
			return createQuery.getResultList();
		} catch (RuntimeException re) {
			throw re;
		}
	}

	public static List<User> getUsersBySubscribedTags(List<Tag> tags) {
		try {
			CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
			CriteriaQuery<User> query = cb.createQuery(User.class);
			Root<User> from = query.from(User.class);
			Join<User, Tag> tagJoin = from.join("tags");
			List<Predicate> predicates = new ArrayList<Predicate>();
			predicates.add(tagJoin.in(tags));
			query.select(from);
			query.where(predicates.toArray(new Predicate[]{}));
			TypedQuery<User> createQuery = JPA.em().createQuery(query);
			return createQuery.getResultList();
		} catch (RuntimeException re) {
			throw re;
		}
	}

	public static boolean isEligibleUsersForCandleBadge(Date date, User user) {
		try {
			boolean result = false;
			if(user.getRegistrationDate().before(date)) {
				CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
				CriteriaQuery<Long> query = cb.createQuery(Long.class);
				Root<ContributionAnswer> from = query.from(ContributionAnswer.class);
				List<Predicate> predicates = new ArrayList<Predicate>();
				predicates.add(cb.equal(from.get("userId"), user.getId()));
				query.select(cb.count(from)).distinct(true);
				query.where(predicates.toArray(new Predicate[]{}));
				TypedQuery<Long> createQuery = JPA.em().createQuery(query);
				Long count = createQuery.getSingleResult();
				if(count > 0L) {
					result = true;
				}
			}
			return result;
		} catch (RuntimeException re) {
			throw re;
		}
	}

	public static List<User> findTeamUsers() {
		return User.find("team = true").fetch();
	}

    public static User createFromRecolnat(RecolnatUser ruser) {
        User huser = new User();

        huser.setLogin(ruser.login);
        huser.setAdmin(false);
        huser.setTeam(false);
        huser.setLeader(false);
        huser.setEmail(ruser.email);

		Logger.info("Create user from recolnat : avatar %s", ruser.avatar);

		if (ruser.avatar != null) {

			Image avatar = Image.createImageFromBytes(Base64.decodeBase64(ruser.avatar.data));
			huser.imageId = avatar.id;
			huser.hasImage = true;
		}


        huser.setFirstName(ruser.firstname);
        huser.setLastName(ruser.lastname);
        huser.setRecolnatUUID(ruser.user_uuid);

		huser.save();
        huser.refresh();

        return huser;
    }

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public Boolean isAlertMission() {
		return alertMission;
	}

	public void setAlertMission(Boolean alertMission) {
		this.alertMission = alertMission;
	}

	public Boolean isAlertSpecimen() {
		return alertSpecimen;
	}

	public void setAlertSpecimen(Boolean alertSpecimen) {
		this.alertSpecimen = alertSpecimen;
	}
}
