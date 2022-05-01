package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class SendPing extends OneShotBehaviour{
	private List<String> receivers;
	private ACLMessage msg;
	public SendPing(Agent a, List<String> receivers) {
		super(a);
		this.receivers=receivers;

	}

	@Override
	public void action() {
		msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("PING");
		msg.setSender(this.myAgent.getAID());
		for(String a : this.receivers) {
			//System.out.println("Agent " + this.myAgent.getLocalName() + " ping agent " + a);
			msg.addReceiver(new AID(a, false));
		}
		((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		
	}


}
