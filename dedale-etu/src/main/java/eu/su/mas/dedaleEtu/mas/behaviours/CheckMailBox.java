package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.HashMap;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedaleEtu.mas.agents.dummies.AgentOptimized;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.SerializableAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class CheckMailBox extends OneShotBehaviour{
	private int response;
	private AgentOptimized a;
	private boolean receivedAnything=false;
	private final float stopTimer =(float) 2;
	private SerializableAgent sAg;
	public CheckMailBox(AgentOptimized a) {
		super(a);
		this.a=a;

	}
	
	@Override
	public void action() {
		receivedAnything=false;
		response=0;
		//System.out.println("CheckMailBox "+this.a.getLocalName());

		MessageTemplate pingTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("PING"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		
		MessageTemplate shareTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("SHARE-TOPO"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		
		MessageTemplate ACKTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("ACK"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		
		long t1=System.currentTimeMillis();
		while(System.currentTimeMillis()-t1<stopTimer && !receivedAnything) {	
			ACLMessage pingReceived=this.myAgent.receive(pingTemplate);
			ACLMessage MapTemplate=this.myAgent.receive(shareTemplate);
			ACLMessage ACKReceived=this.myAgent.receive(ACKTemplate);
			/* we check if we received a map*/
			if(MapTemplate != null) {
				receivedAnything=true;
				a.otherAgent=MapTemplate.getSender();
				try {
					sAg=(SerializableAgent) MapTemplate.getContentObject();
					this.a.MapReceived = sAg.getSg();
					
					if(!this.a.dico.containsKey(this.a.otherAgent.getLocalName())) {
						this.a.dico.put(this.a.otherAgent.getLocalName(), null);
					}
					this.a.fusionData(sAg);
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
				if(this.a.WaitingForMAp.contains(a.otherAgent)) {
					//System.out.println("MAP RECEIVED CHECK2 "+this.a.getLocalName());
					this.a.WaitingForMAp.remove(a.otherAgent);
					response=3;
				}
				else {
					//System.out.println("MAP RECEIVED MAILBOX "+this.a.getLocalName());
					response=1;
				}
				
			}
			else {
				/* otherwise we check if we received a ping*/
	
				if(pingReceived != null) {
					
					//System.out.println("PING RECEIVED MAILBOX "+this.a.getLocalName());
					receivedAnything=true;
					this.a.senderPing=pingReceived.getSender();
					if(!this.a.dico.containsKey(this.a.senderPing.getLocalName())) {
						this.a.dico.put(this.a.senderPing.getLocalName(), null);
					}
					response=2;
				}
			}
			if(ACKReceived!=null && this.a.otherAgent!=null && ACKReceived.getSender().getLocalName().equals(this.a.otherAgent.getLocalName())) {
				//System.out.println("RECEIVED ACK2 "+this.a.getLocalName());
				receivedAnything=true;
				if(!this.a.dico.containsKey(ACKReceived.getSender().getLocalName())) {
					this.a.dico.put(ACKReceived.getSender().getLocalName(), null);
				}
				response=4;
			}
		}
		
	}
	public int onEnd() {
		return response;
	}

}
