package eu.su.mas.dedaleEtu.mas.behaviours;


import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedaleEtu.mas.agents.dummies.AgentOptimized;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class CheckMapReception extends OneShotBehaviour{
	private AgentOptimized a;
	private float timer=0;//temps en seconde d'attente de la reception d'une map
	private long t1,t2;
	private int response;
	private final float stopTimer =(float) 3;
	private boolean receivedMap=false;
	public CheckMapReception(AgentOptimized a) {
		super(a);
		this.a=a;

	}
	@Override
	public void action() {
		MessageTemplate shareTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("SHARE-TOPO"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived=this.myAgent.receive(shareTemplate);
		t1=System.currentTimeMillis();
		while(timer<stopTimer && !receivedMap) {
			if (msgReceived!=null) {
				SerializableSimpleGraph<String, MapAttribute> sgreceived=null;
				try {
					a.otherAgent=msgReceived.getSender();
					a.MapReceived = (SerializableSimpleGraph<String, MapAttribute>)msgReceived.getContentObject();
					receivedMap=true;
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			t2=System.currentTimeMillis();
			timer=timer+(t2-t1)/1000;
			t1=t2;
					
		}
		if(!receivedMap) {
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
