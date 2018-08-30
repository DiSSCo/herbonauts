package inspectors;

import models.User;

/**
 * Inspector de base pour ajouter la méthode d'extraction 
 * d'un objet selon son type
 */
public abstract class BaseInspector implements Inspector {

	protected<E> E extract(Class<E> type, Object... objects) {
		
		for (Object obj :objects) {
			if (type.isAssignableFrom(obj.getClass())) {
				return (E) obj;
			}
		}
		
		return null;
	}
	
}
