package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedaleEtu.mas.agents.dummies.AgentOptimized;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.OneShotBehaviour;

public class UpdateOwnMap extends OneShotBehaviour{
	private AgentOptimized a;
	public UpdateOwnMap(AgentOptimized a) {
		super(a);
		this.a=a;

	}
	@Override
	public void action() {
		a.myMap.mergeMap(a.MapReceived);
		
	}

}
