package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dataStructures.serializableGraph.SerializableSimpleGraph;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * The agent periodically share its map.
 * It blindly tries to send all its graph to its friend(s)  	
 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

 * @author hc
 *
 */
public class SendMapBehaviour extends TickerBehaviour{
	
	private MapRepresentation myMap;
	private boolean finished=true;
	private AID canceler,potential_requester;
	private Set<AID> requesters;
	private HashMap<AID,Float> list_TimerAgent;
	private long period;

	/**
	 * The agent periodically share its map.
	 * It blindly tries to send all its graph to its friend(s)  	
	 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

	 * @param a the agent
	 * @param period the periodicity of the behaviour (in ms)
	 * @param mymap (the map to share)
	 * @param receivers the list of agents to send the map to
	 */
	public SendMapBehaviour(Agent a, long period,MapRepresentation mymap, List<String> receivers) {
		super(a, period);
		this.myMap=mymap;
		this.period=period;
		requesters = new HashSet<AID>();
		list_TimerAgent=new HashMap<AID,Float>();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -568863390879327961L;

	@Override
	protected void onTick() {
		final MessageTemplate msgTemplate1 = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);			
		final MessageTemplate msgTemplate2 = MessageTemplate.MatchPerformative(ACLMessage.CANCEL);			

		final ACLMessage msg1 = this.myAgent.receive(msgTemplate1);
		final ACLMessage msg2 = this.myAgent.receive(msgTemplate2);
		for (AID key : list_TimerAgent.keySet()) {
			list_TimerAgent.put(key,list_TimerAgent.get(key)+((float) period)/1000);
		}
		if (msg2 != null) {
			//System.out.println(this.myAgent.getName()+"a recu un message CANCEL ou TIMEOUT");
			
			canceler=msg2.getSender();
			list_TimerAgent.put(canceler, (float) 0);
			if(requesters.contains(canceler)) {
				requesters.remove(canceler);
			}
			if(requesters.isEmpty()) {
				finished=true;
			}
			
		}else{
			if(msg1 != null) {
				potential_requester=msg1.getSender();
				if(!list_TimerAgent.containsKey(potential_requester) || list_TimerAgent.get(potential_requester)>=3) {
					requesters.add(potential_requester);
					finished=false;
				}
				
			}
		}
		if(!finished) {
			sendMap();
		}
		
	}

	private void sendMap() {
		//System.out.println(this.myAgent.getName()+"a envoyé sa Map");
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("SHARE-TOPO");
		msg.setSender(this.myAgent.getAID());
		for(AID requester : requesters) {
			msg.addReceiver(requester);
		}
			
		SerializableSimpleGraph<String, MapAttribute> sg=this.myMap.getSerializableGraph();
		try {					
			msg.setContentObject(sg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		
	}

}
