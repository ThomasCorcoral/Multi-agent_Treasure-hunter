package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import dataStructures.serializableGraph.SerializableNode;
import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedaleEtu.mas.agents.dummies.AgentOptimized;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;

public class Harvest extends OneShotBehaviour{
	
	private AgentOptimized a;
	private HashMap<String,Couple> locationTreasure=null;
	private int optTreasure=0;
	private int response;
	
	public Harvest(AgentOptimized a) {
		super(a);
		this.a=a;

	}
	@Override
	public void action() {
		response=0;
		if(this.a.optTreasure==0) {
			optTreasure=0;
			int qteTreasure;
			int qteGlobalTreasure=0;
			int freeSpace;
			if(this.a.expertise==Observation.GOLD) {
				locationTreasure=this.a.locationGold;
				qteTreasure=this.a.qteGold;
				freeSpace = this.a.freeSpaceGoldPerso;
			}
			else {
				locationTreasure=this.a.locationDiam;
				qteTreasure=this.a.qteDiam;
				freeSpace = this.a.freeSpaceDiamPerso;
			}
			optTreasure=(this.a.qteGold+this.a.qteDiam)/(this.a.freeSpaceGold+this.a.freeSpaceDiam);
			optTreasure*=freeSpace;
			this.a.optTreasure=optTreasure;
		}
		
		
		
		
		
	}
	public int OnEnd() {
		return response;
	}

}
