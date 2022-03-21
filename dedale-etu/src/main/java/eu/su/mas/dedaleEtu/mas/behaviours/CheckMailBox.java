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
	private int response=0;
	private AgentOptimized a;
	public CheckMailBox(AgentOptimized a) {
		super(a);
		this.a=a;

	}
	@Override
	public void action() {
		MessageTemplate pingTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("PING"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		MessageTemplate shareTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("SHARE-TOPO"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
			
		ACLMessage pingReceived=this.myAgent.receive(pingTemplate);
		ACLMessage MapTemplate=this.myAgent.receive(shareTemplate);
		/* we check if we received a map*/
		if(MapTemplate != null) {
			a.otherAgent=MapTemplate.getSender();
			try {
				this.a.MapReceived = (SerializableSimpleGraph<String, MapAttribute>)MapTemplate.getContentObject();
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
			response=2;
		}
		else {
			/* otherwise we check if we received a ping*/

			if(pingReceived != null) {
				this.a.senderPing=pingReceived.getSender();
				response=1;
			}
		}
		
	}
	public int onEnd() {
		return response;
	}

}
