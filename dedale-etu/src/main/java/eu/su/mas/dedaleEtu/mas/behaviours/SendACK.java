package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.AgentOptimized;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class SendACK extends OneShotBehaviour{
	
	private AgentOptimized a;
	public SendACK(AgentOptimized a) {
		super(a);
		this.a=a;

	}
	@Override
	public void action() {
		//System.out.println("SEND ACK "+this.a.getLocalName());
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("ACK");
		msg.setSender(this.myAgent.getAID());
		msg.addReceiver(this.a.otherAgent);
		
		((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		
	}

}
