package eu.su.mas.dedaleEtu.mas.behaviours;


import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedaleEtu.mas.agents.dummies.AgentOptimized;
import eu.su.mas.dedaleEtu.mas.knowledge.SerializableAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class CheckMapReception extends OneShotBehaviour{
	private AgentOptimized a;
	private long t1;
	private int response;
	private final float stopTimer =(float) 3;
	private boolean receivedMap=false;
	public CheckMapReception(AgentOptimized a) {
		super(a);
		this.a=a;

	}
	@Override
	public void action() {
		response=1;
		receivedMap=false;
		//System.out.println("CheckMap Reception");
		MessageTemplate shareTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("SHARE-TOPO"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		
		t1=System.currentTimeMillis();
		while(System.currentTimeMillis()-t1<stopTimer && !receivedMap) {
			ACLMessage msgReceived=this.myAgent.receive(shareTemplate);
			if (msgReceived!=null) {
				try {
					//System.out.println("DATA RECEIVED CHECK "+this.a.getLocalName());
					a.otherAgent=msgReceived.getSender();
					
					SerializableAgent sAg = (SerializableAgent) msgReceived.getContentObject();
					this.a.MapReceived = sAg.getSg();
					this.a.fusionData(sAg);
					
					receivedMap=true;
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
					
		}
		if(receivedMap) {
			response=2;
		}
		else {
			this.a.WaitingForMAp.add(this.a.senderPing);
		}
		
		
	}
	public int onEnd() {
		return response;
	}
}
