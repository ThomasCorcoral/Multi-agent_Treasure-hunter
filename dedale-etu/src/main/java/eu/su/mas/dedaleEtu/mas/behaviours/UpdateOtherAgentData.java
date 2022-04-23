package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import dataStructures.serializableGraph.SerializableNode;
import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedaleEtu.mas.agents.dummies.AgentOptimized;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;

public class UpdateOtherAgentData extends OneShotBehaviour{
	
	private AgentOptimized a;
	private AID otherAgent;
	private SerializableSimpleGraph<String, MapAttribute> mapOtherAgent;
	
	public UpdateOtherAgentData(AgentOptimized a) {
		super(a);
		this.a=a;
	}
	@Override
	public void action() {
		this.otherAgent=a.otherAgent;
		this.mapOtherAgent=a.getMapReceived();
		ArrayList<String> noeuds= new ArrayList<String>();
		Set<SerializableNode<String, MapAttribute>> allNodes = mapOtherAgent.getAllNodes();
		for(SerializableNode<String, MapAttribute> node:allNodes) {
				noeuds.add(node.getNodeId());
		}
		a.dico.put(otherAgent.getLocalName(), noeuds);
		a.myMap.mergeMap(a.MapReceived);
	}

}
