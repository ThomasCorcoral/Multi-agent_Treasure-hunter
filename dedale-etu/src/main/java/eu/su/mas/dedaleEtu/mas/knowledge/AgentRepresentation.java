package eu.su.mas.dedaleEtu.mas.knowledge;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import dataStructures.serializableGraph.SerializableNode;
import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedaleEtu.mas.agents.dummies.AgentOptimized;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;


/**
 * 
 * @author tc
 */
public class AgentRepresentation implements Serializable {

	private static final long serialVersionUID = 2162879882687321972L;

	/*********************************
	 * Parameters for an agent
	 ********************************/
	
	public MapRepresentation agentMap;
	private Observation ressourceType;
	private SerializableAgent sa;
	private AgentOptimized agent;
	//locationGold contient la liste des noeuds contenant de l'or associé au couple l'heure où il a été découvert et la quantité d'or que le noeud contient
	private HashMap<String,Couple> locationGold;
	//locationDiam contient la liste des noeuds contenant de diamant associé au couple l'heure où il a été découvert et la quantité d'or que le noeud contient
	private HashMap<String,Couple> locationDiam;
	ArrayList<String> PersoGold,PersoDiam;
	
	public AgentRepresentation(AgentOptimized agent) {
		this.ressourceType = this.agent.expertise;
		this.agent=agent;
		this.agentMap = this.agent.getMyMap();
		this.locationGold=this.agent.locationGold;
		this.locationDiam=this.agent.locationDiam;
		this.PersoGold=this.agent.PersoGold;
		this.PersoDiam=this.agent.PersoDiam;

	}
	
	public SerializableSimpleGraph<String, MapAttribute> updateSendMap(SerializableSimpleGraph<String, MapAttribute> ser) {
		SerializableSimpleGraph<String, MapAttribute> res=new SerializableSimpleGraph<String, MapAttribute>();
		ArrayList<String> noeudsOther = this.agent.dico.get(this.agent.senderPing);
		ArrayList<String> allNodes=new ArrayList<String>();
		boolean nothingToSay=true;
		
		for(SerializableNode<String, MapAttribute> noeudO : ser.getAllNodes()) {
			if(noeudsOther!=null && !noeudsOther.contains(noeudO.getNodeId())) {
				res.addNode(noeudO.getNodeId(), noeudO.getNodeContent());
				allNodes.add(noeudO.getNodeId());
				nothingToSay=false;
			}
		}
		
		if(nothingToSay) {
			return null;
		}
		
		ArrayList<String> allEdge=new ArrayList<String>();
		for(SerializableNode<String, MapAttribute> noeudO:res.getAllNodes()) {
			for(String edgeO : ser.getEdges(noeudO.getNodeId())) {
				if(!allEdge.contains(edgeO)) {
					for(SerializableNode<String, MapAttribute> noeudO2 : ser.getAllNodes()) {
						for(String edgeO2 : ser.getEdges(noeudO.getNodeId())) {
							if(edgeO2.equals(edgeO) && !noeudO.getNodeId().equals(noeudO2.getNodeId())) {
								res.addEdge(edgeO, noeudO.getNodeId(), noeudO2.getNodeId());
							}
							
						}
					}
					allEdge.add(edgeO);
				}
			}
		}
		
		return res;
		
	}

	public synchronized SerializableAgent getAgentRepresentation(){
		this.sa = null;
		this.sa = new SerializableAgent(this);
		return this.sa;
	}
	
	
	/*******************************************
	 * Getters / Setters
	 *******************************************/
	

	public MapRepresentation getAgentMap() {
		return agentMap;
	}
	public HashMap<String,Couple> getLocationGold() {
		return locationGold;
		
	}
	public HashMap<String,Couple> getLocationDiam() {
		return locationDiam;
		
	}

	public Observation getRessourceType() {
		return ressourceType;
	}
	public ArrayList<String> getPersoGold(){
		return PersoGold;
	}
	public ArrayList<String> getPersoDiam(){
		return PersoDiam;
	}


}