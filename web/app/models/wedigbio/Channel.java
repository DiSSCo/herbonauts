package models.wedigbio;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import play.Logger;
import conf.Herbonautes;


public class Channel {
    
	

	String title, 
    link, description, language, webMaster, pubDate;
   


	List<ItemRss> items = new ArrayList();
    
    public  Channel() {
    	this.language="en";
    	this.title="LesHerbonautes activity";
    	this.description="Each item if this channel is a transcribing or geoferencing activity "
    			+ "done by LesHerbonautes community";
    	Calendar cal = Calendar.getInstance();
    	SimpleDateFormat sdf= new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z",Locale.ENGLISH);
    	this.pubDate = sdf.format(cal.getTime());
    	this.link=Herbonautes.get().baseUrl;
    }

	public String getTitle() {
		return title;
	}
	
    public List<ItemRss> getItems() {
		return items;
	}
    
    public void addItem( ItemRss i)    {
    	items.add(i);
    }

    /**
     * Complete la description avec le nb de jour d'ancienneté du flux RSS
     */
    public void setNbDay(int n) {
    	if (this.description!=null)
    		description += " during the last "+n+" days";
    }
    
    /**
     * Parcours des Items pour que seul la dernière contribution d'un même spécimen
     * ait labelcompleted à true
     */

    public void removeDuplicateLabelCompleted() {
    	if (items.size()==0) return;

    	ListIterator i=items.listIterator(items.size()-1);
    	for (ItemRss it=(ItemRss) i.next();
    			i.hasPrevious();
    			it=(ItemRss)i.previous()) {

    		if (it.labelCompleted)
    			it.removeMultipleLabelCompleted(this);
    	}
    }
}