package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedaleEtu.mas.agents.dummies.AgentOptimized;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class WaitACK  extends OneShotBehaviour{
	private AgentOptimized a;
	private long t1,t2;
	private float timer;
	private final float stopTimer=(float) 3;
	private boolean receivedACK=false;
	private int response;
	public WaitACK(AgentOptimized a) {
		super(a);
		this.a=a;

	}

	@Override
	public void action() {
		MessageTemplate shareTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("ACK"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived=this.myAgent.receive(shareTemplate);
		t1=System.currentTimeMillis();
		while(timer<stopTimer && !receivedACK) {
			if (msgReceived!=null) {
				receivedACK=true;
			}
			t2=System.currentTimeMillis();
			timer=timer+(t2-t1)/1000;
			t1=t2;
		}
		if(!receivedACK) {
			response=1;
		}
		else {
			response=2;
		}
		
	}
	public int onEnd() {
		return response;
	}
}
