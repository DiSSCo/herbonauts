package inspectors;

/**
 * Interface à implémenter pour le objets d'inspection d'évément
 */
public interface Inspector {

	/**
	 * 
	 */
	void inspect(Event event, Object... objects);
	
}
