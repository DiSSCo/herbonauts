package models.wedigbio;

import java.util.ArrayList;
import java.util.List;

import models.contributions.Contribution;
import models.questions.ContributionAnswer;
import play.Logger;

public class Rss {
Version version;
Channel channel;
/**
 * Generation du flux RSS des contributions des n derniers jours
 * @param pubDate
 */
public Rss(int n) {
	super();
	this.setVersion(new Version("2.0"));
	Channel ch = new Channel();
	ch.setNbDay(n);
	setChannel(ch);
	List <ContributionAnswer> freshContribs = ContributionAnswer.find("deleted=false"
			+ " and createdAt >= sysdate-"+n+" order by id").fetch();

	Logger.info("Create RSS " + n + " days");

	for (ContributionAnswer c: freshContribs ) {
		ch.addItem(new ItemRss(c));
	}
	ch.removeDuplicateLabelCompleted();
}
/**
 * Generation du flux RSS des contributions égale ou postérieure à
 * celle dont l'identifiant est fourni en paramètre
 * @param pubDate
 */
public Rss(Long id) {
	super();
	this.setVersion(new Version("2.0"));
	Channel ch = new Channel();
	setChannel(ch);
	List <Contribution> freshContribs = Contribution.find("canceled=0 and report=0 and id >="+id).fetch(100);
	for (Contribution c: freshContribs ) {
		ch.addItem(new ItemRss(c));
	}
	ch.removeDuplicateLabelCompleted();
}

public Version getVersion() {
		return version;
	}

	public void setVersion(Version version) {
		this.version = version;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	

}
