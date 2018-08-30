/**
 * 
 */
package models.wedigbio;

/**
 * Le sujet de la transcription 
 * ... c'est à dire le specimen 
 * @author schagnoux
 *
 */
public class Subject {
	public Subject(String link, String thumbnailUri) {
		super();
		this.link = link;
		this.thumbnailUri = thumbnailUri;
	}
	String link; 			//URI de la page specimen
	String thumbnailUri;	//URI de la vignette
	public String getLink() {
		return link;
	}
	public String getThumbnailUri() {
		return thumbnailUri;
	}

}
