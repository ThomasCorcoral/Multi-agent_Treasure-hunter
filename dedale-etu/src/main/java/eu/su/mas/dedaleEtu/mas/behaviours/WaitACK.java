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
	private long t1;
	private final float stopTimer;
	private boolean receivedACK;
	private int response;
	public WaitACK(AgentOptimized a) {
		super(a);
		this.a=a;
		this.stopTimer =(float) this.a.WAITINGTIME*6; 

	}

	@Override
	public void action() {
		response=1;
		receivedACK=false;
		MessageTemplate shareTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("ACK"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		t1=System.currentTimeMillis();
		while(System.currentTimeMillis()-t1<stopTimer && !receivedACK) {
			ACLMessage msgReceived=this.myAgent.receive(shareTemplate);
			if (msgReceived!=null) {
				receivedACK=true;
			}
		}
		if(receivedACK) {
			//System.out.println("RECEIVED ACK "+this.a.getLocalName());
			response=2;
		}
		
	}
	public int onEnd() {
		return response;
	}
}
