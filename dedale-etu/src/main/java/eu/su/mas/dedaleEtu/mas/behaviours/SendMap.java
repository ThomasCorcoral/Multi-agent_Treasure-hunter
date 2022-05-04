package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import dataStructures.serializableGraph.SerializableNode;
import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.AgentOptimized;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.SerializableAgent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class SendMap extends OneShotBehaviour{
	private AgentOptimized a;
	private MapRepresentation myMap;
	private boolean afterPing;
	SerializableAgent sAgent;
	public SendMap(AgentOptimized a,boolean afterPing) {
		super(a);
		this.a=a;
		this.afterPing=afterPing;
		

	}
	public SerializableSimpleGraph<String, MapAttribute> updateSendMap(SerializableSimpleGraph<String, MapAttribute> ser) {
		
		SerializableSimpleGraph<String, MapAttribute> res=new SerializableSimpleGraph<String, MapAttribute>();
		ArrayList<String> noeudsOther = this.a.dico.get(this.a.senderPing);
		ArrayList<String> allNodes=new ArrayList<String>();
		boolean nothingToSay=true;
		
		if(noeudsOther==null) {
			return ser;
		}
		for(SerializableNode<String, MapAttribute> noeudO : ser.getAllNodes()) {
			if(!noeudsOther.contains(noeudO.getNodeId())) {
				res.addNode(noeudO.getNodeId(), noeudO.getNodeContent());
				allNodes.add(noeudO.getNodeId());
				nothingToSay=false;
			}
		}
		
		if(nothingToSay) {
			return null;
		}
		
		ArrayList<String> allEdge=new ArrayList<String>();
		for(String noeudO:allNodes) {
			for(String edgeO : ser.getEdges(noeudO)) {
				if(!allEdge.contains(edgeO)) {
					for(SerializableNode<String, MapAttribute> noeudO2 : ser.getAllNodes()) {
						for(String edgeO2 : ser.getEdges(noeudO)) {
							if(edgeO2.equals(edgeO) && !noeudO.equals(noeudO2.getNodeId())) {
								if(!allNodes.contains(noeudO2.getNodeId())) {
									res.addNode(noeudO2.getNodeId(), noeudO2.getNodeContent());
								}
								res.addEdge(edgeO, noeudO2.getNodeId(),noeudO);
							}
							
						}
					}
					allEdge.add(edgeO);
				}
			}
		}
		
		return res;
		
	}
	
	@Override
	public void action() {
		myMap=this.a.getMyMap();
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("SHARE-TOPO");
		msg.setSender(this.myAgent.getAID());
		if(this.afterPing) {
			msg.addReceiver(this.a.senderPing);
		}
		else {
			msg.addReceiver(this.a.otherAgent);
		}
		//SerializableAgent sa=new SerializableAgent(new AgentRepresentation(this.a));
		SerializableSimpleGraph<String, MapAttribute> sa = updateSendMap(myMap.getSerializableGraph());
		String myPosition=((AbstractDedaleAgent)this.a).getCurrentPosition();
		sAgent=new SerializableAgent(this.a,sa, myPosition);
		try {					
			msg.setContentObject(sAgent);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(sa!=null) {
			if(this.afterPing) {
				//System.out.println("SENDMAP "+this.a.getLocalName()+" after Ping");
			}
			else {
				//System.out.println("SENDMAP "+this.a.getLocalName()+" after Other Map Reception");
			}
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		}
		else {
			//System.out.println("SENDMAP "+this.a.getLocalName()+" EST NULL");
		}
		
		
	}

}
