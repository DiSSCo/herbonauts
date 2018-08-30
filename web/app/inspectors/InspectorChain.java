package inspectors;

import java.util.LinkedList;

import play.Logger;
import play.db.Model;

/**
 * Centralisation de l'inspection des évenements
 * 
 * Un appel à InspectorChain.get().inspect() permet de déclencher la chaine
 * de traitement des évenement (activites, badge, level, alert) et creer
 * les éléments au besoin
 */
public class InspectorChain {

	private LinkedList<Inspector> chain;
	
	private static final InspectorChain INSTANCE = new InspectorChain();
	
	private InspectorChain() {
		chain = new LinkedList<Inspector>();
		chain.add(new ActivityInspector());
		chain.add(new BadgeInspector());
		chain.add(new LevelInspector());
		chain.add(new AlertInspector());
	}
	
	public static InspectorChain get() {
		return INSTANCE;
	}
	
	public void inspect(Event event, Object... objects) {
		Logger.info("[EVENT]  %s", event);
		
		for (Inspector inspector : chain) {
			inspector.inspect(event, objects);
		}
		
	}
	
	
}
