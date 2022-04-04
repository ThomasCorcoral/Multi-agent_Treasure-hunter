package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;

import dataStructures.serializableGraph.SerializableNode;
import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.AgentOptimized;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class SendMap extends OneShotBehaviour{
	private AgentOptimized a;
	private MapRepresentation myMap;
	public SendMap(AgentOptimized a) {
		super(a);
		this.a=a;

	}
	public SerializableSimpleGraph<String, MapAttribute> updateSendMap(SerializableSimpleGraph<String, MapAttribute> ser) {
		SerializableSimpleGraph<String, MapAttribute> res=new SerializableSimpleGraph<String, MapAttribute>();
		ArrayList<String> noeudsOther = this.a.dico.get(this.a.senderPing);
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
	@Override
	public void action() {
		System.out.println("SendMap "+this.a.getLocalName());

		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("SHARE-TOPO");
		msg.setSender(this.myAgent.getAID());
		msg.addReceiver(this.a.senderPing);
		SerializableSimpleGraph<String, MapAttribute> sg=updateSendMap(this.a.getMyMap().getSerializableGraph());
		try {					
			msg.setContentObject(sg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(sg!=null) {
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		}
		
		
	}
	public int onEnd() {
		if(a.gotPing) {
			return 1;		
		}
		return 2;
		}

}
