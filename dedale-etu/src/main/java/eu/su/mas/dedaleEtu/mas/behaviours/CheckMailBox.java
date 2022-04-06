package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedaleEtu.mas.agents.dummies.AgentOptimized;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class CheckMailBox extends OneShotBehaviour{
	private int response;
	private AgentOptimized a;
	private boolean receivedAnything=false;
	private final float stopTimer =(float) 1;
	public CheckMailBox(AgentOptimized a) {
		super(a);
		this.a=a;

	}
	@Override
	public void action() {
		receivedAnything=false;
		response=0;
		System.out.println("CheckMailBox "+this.a.getLocalName());

		MessageTemplate pingTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("PING"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		
		MessageTemplate shareTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("SHARE-TOPO"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		
		long t1=System.currentTimeMillis();
		while(System.currentTimeMillis()-t1<stopTimer && !receivedAnything) {	
			ACLMessage pingReceived=this.myAgent.receive(pingTemplate);
			ACLMessage MapTemplate=this.myAgent.receive(shareTemplate);
			/* we check if we received a map*/
			if(MapTemplate != null) {
				receivedAnything=true;
				a.otherAgent=MapTemplate.getSender();
				try {
					
					this.a.MapReceived = (SerializableSimpleGraph<String, MapAttribute>)MapTemplate.getContentObject();
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
				System.out.println("MAP RECEIVED MAILBOX "+this.a.getLocalName());
				response=1;
			}
			else {
				/* otherwise we check if we received a ping*/
	
				if(pingReceived != null) {
					System.out.println("PING RECEIVED MAILBOX "+this.a.getLocalName());
					receivedAnything=true;
					this.a.senderPing=pingReceived.getSender();
					response=2;
				}
			}
		}
		
	}
	public int onEnd() {
		return response;
	}

}
