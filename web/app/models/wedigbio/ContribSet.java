package models.wedigbio;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.math.BigDecimal;

import javax.persistence.Query;

import models.questions.ContributionAnswer;
import play.Logger;
import play.db.jpa.JPA;

/**
 * 
 * Un ensemble de contributions destinées au tableau de bord de https://www.wedigbio.org/ 
 * @see https://github.com/iDigBio/wedigbio-dashboard
 * @author schagnoux 
 *
 */
public class ContribSet {
	private int numFound;
	private int rows;
	private String start;
	
	List<Item> items = new ArrayList<Item>();
	
	/* constructeur */
	public ContribSet(String start, String end, int rowstart) {
		super();
		
		Logger.info("Create ContribSet " + start + " days "+ end + "/" + rowstart);	
		


		
		//Création de la requete
				String where = "where deleted=0 "
				+ "and created_at between "+OracleFromTz(start)+" and "+OracleFromTz(end);
				
		String q="select * from h_contribution_answer "+where+ " order by created_at desc";
		Logger.info("Requete ="+q);
		
		//Comptage
		Logger.info("compte ="+"select count(*) from h_contribution_answer "+where);		
		Query count =JPA.em().createNativeQuery("select count(*) from h_contribution_answer "+where
				);
		this.numFound =	 ((BigDecimal) count.getSingleResult()).intValue();

		//Chargement des contribs
		Query query =JPA.em().createNativeQuery(q,ContributionAnswer.class);
		query.setMaxResults(1000);
		query.setFirstResult(rowstart);
	 
		@SuppressWarnings("unchecked")
		List<ContributionAnswer> freshContribs = query.getResultList();		
		
		//Construction des items
		for (ContributionAnswer c: freshContribs ) {
			items.add(new Item(c));
		}

		this.rows=freshContribs.size();
		this.start=""+rowstart;
	}
	
	/* construction de la chaine oracle pour traduire les dates ISO 
	 * @see https://fr.wikipedia.org/wiki/ISO_8601
	 * @see https://community.oracle.com/thread/666411
	 */
	private String OracleFromTz(String dateIso8601) {
		return "(from_tz(to_timestamp('"+dateIso8601+"', 'yyyy-mm-dd\"T\"hh24:mi:ss\"Z\"'),'+00:00') "
				+ "AT TIME ZONE 'Europe/Paris')";
	}
	
	/* accesseurs */
	public int getNumFound() {
		return numFound;
	}
	public void setNumFound(int numFound) {
		this.numFound = numFound;
	}

	public List<Item> getItems() {
		return items;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

}
