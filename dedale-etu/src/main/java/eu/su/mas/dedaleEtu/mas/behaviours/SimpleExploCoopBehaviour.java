package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import eu.su.mas.dedaleEtu.mas.knowledge.*;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;


import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;


/**
 * <pre>
 * This behaviour allows an agent to explore the environment and learn the associated topological map.
 * The algorithm is a pseudo - DFS computationally consuming because its not optimised at all.
 * 
 * When all the nodes around him are visited, the agent randomly select an open node and go there to restart its dfs. 
 * This (non optimal) behaviour is done until all nodes are explored. 
 * 
 * Warning, this behaviour does not save the content of visited nodes, only the topology.
 * Warning, the sub-behaviour ShareMap periodically share the whole map
 * </pre>
 * @author hc
 *
 */
public class SimpleExploCoopBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;
	private int _WAITING_TIME = 1000;
	private short _STOP_WAITING_TIME = 3;
	private short _WAITING_TIME_SHARE_AGAIN = 5;

	private boolean finished = false;
	
	private short stop_counter = 0;
	private int turn_counter = 0;
	

	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private AgentRepresentation myAgentData;
	private Map<String, AgentRepresentation> mapAgents;

	private List<String> list_agentNames;
	
	String[] array = {"foo", "bar"};
	private List<String> list_waitingMap = new ArrayList<String>();
	private List<String> list_waitingAck = new ArrayList<String>();
	
	private Map<String, Integer> map_meeting = new HashMap<>(); 

/**
 * 
 * @param myagent
 * @param myMap known map of the world the agent is living in
 * @param agentNames name of the agents to share the map with
 */
	public SimpleExploCoopBehaviour(final AbstractDedaleAgent myagent, AgentRepresentation myAgentData, Map<String, AgentRepresentation> mapAgents, List<String> agentNames) {
		super(myagent);
		this.myAgentData=myAgentData;
		this.mapAgents=mapAgents;
		this.list_agentNames=agentNames;
		this.list_agentNames.remove(0);
		//System.out.println(this.list_agentNames);
		for(String a : this.list_agentNames) {
			this.map_meeting.put(a, 0);
			this.mapAgents.put(a, new AgentRepresentation(a));
		}
	}
	
	private int containsIgnoreCase(String toCheck, boolean map) {
		int i = 0;
		if(map) {
			for(String s : this.list_waitingMap) {
				if(toCheck.equalsIgnoreCase(s)) {
					return i;
				}
				i++;
			}
		}else {
			for(String s : this.list_waitingAck) {
				if(toCheck.equalsIgnoreCase(s)) {
					return i;
				}
				i++;
			}
		}
		return -1;
	}
	
	private void updateAgentsMeet() {
		for(AgentRepresentation a : this.mapAgents.values()) {
			a.updateMeet();
		}
	}
	
	@Override
	public void action() {
		String nextNode=null;
		
		if( this.myAgentData.getAgentMap() == null ) {
			this.myAgentData.agentMap = new MapRepresentation();
		}

		//0) Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
			try { // wait some time
				this.myAgent.doWait(_WAITING_TIME);
			} catch (Exception e) {
				e.printStackTrace();
			}
			//1) remove the current node from openlist and add it to closedNodes.
			this.myAgentData.getAgentMap().addNode(myPosition, MapAttribute.closed);
			if(this.stop_counter == 0) {
				//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
				Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
				while(iter.hasNext()){
					String nodeId=iter.next().getLeft();
					boolean isNewNode=this.myAgentData.getAgentMap().addNewNode(nodeId);
					if (myPosition!=nodeId) { //the node may exist, but not necessarily the edge
						this.myAgentData.getAgentMap().addEdge(myPosition, nodeId);
						if (nextNode==null && isNewNode) nextNode=nodeId;
					}
				}
				//3) while openNodes is not empty, continues.
				if (!this.myAgentData.getAgentMap().hasOpenNode()){
					finished=true; // End of exploration
					System.out.println(this.myAgent.getLocalName()+" - Exploration successufully done, behaviour removed.");
				}else{
					if (nextNode==null){
						nextNode=this.myAgentData.getAgentMap().getShortestPathToClosestOpenNode(myPosition).get(0);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
					}else {
						//System.out.println("nextNode notNUll - "+this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"\n -- nextNode: "+nextNode);
					}
				}
			}

			/************************
			 * Template definitions *
			 ************************/
			
			MessageTemplate pingTemplate=MessageTemplate.and(
					MessageTemplate.MatchProtocol("PING"),
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
			MessageTemplate shareTemplate=MessageTemplate.and(
					MessageTemplate.MatchProtocol("SHARE-TOPO"),
					MessageTemplate.MatchPerformative(ACLMessage.INFORM));
			MessageTemplate ackTemplate=MessageTemplate.and(
					MessageTemplate.MatchProtocol("ACKNOWLEDGE"),
					MessageTemplate.MatchPerformative(ACLMessage.CANCEL));
			
			
			/*************
			 * Ack check *
			 *************/
			
			if(this.stop_counter != 0 & this.list_waitingAck.size() > 0) {
				ACLMessage ackReceived=this.myAgent.receive(ackTemplate);
				if(ackReceived != null) { // A message is received
					String senderName = ackReceived.getSender().getLocalName();
					int ind = containsIgnoreCase(senderName, true);
					if(ind != -1) { // was waiting for this ack
						// Remove ack waiting
						this.list_waitingAck.remove(ind);
						// Update the map of the agent
						this.mapAgents.get(senderName).setAgentMap(this.myAgentData.getAgentMap());
					}
				}
			}
			
			/*************************
			 * Communication process *
			 *************************/
				
			ACLMessage pingReceived=this.myAgent.receive(pingTemplate);
			
			boolean ping = (pingReceived != null);
			
			if(ping) { // Ping received
				String senderName = pingReceived.getSender().getLocalName();
				System.out.println("Ping reveived from " + senderName + " for :");
				jade.util.leap.Iterator it = pingReceived.getAllReceiver();
				while(it.hasNext()) {
					System.out.println(it.next());
				}
				
				if( this.mapAgents.get(senderName).getLastMeet() > _WAITING_TIME_SHARE_AGAIN ) {
					// ping received so stop the agent for 3 turns (if he do not receive a map).
					this.stop_counter = _STOP_WAITING_TIME;
					// Send agent map to agent who ping
					ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
					msg.setProtocol("SHARE-TOPO");
					msg.setSender(this.myAgent.getAID());
					msg.addReceiver(pingReceived.getSender());
					// TODO : Optimize this shit
					SerializableSimpleGraph<String, MapAttribute> sg=this.myAgentData.getAgentMap().getSerializableGraph();
					try {					
						msg.setContentObject(sg);
					} catch (IOException e) {
						e.printStackTrace();
					}
					((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
					
					list_waitingMap.add(pingReceived.getSender().getLocalName());
				} else {
					ping = false;
				}
			}
			
			if(!ping){ // No ping so maybe a map ?
				ACLMessage shareAgentContent=this.myAgent.receive(shareTemplate);
				if (shareAgentContent!=null){ // Map received !
					String senderName = shareAgentContent.getSender().getLocalName();
					int ind = containsIgnoreCase(senderName, true);
					this.mapAgents.get(senderName).setLastMeet(0);
					
					// System.out.println("Map received from " + msgReceived.getSender().getLocalName() + " for :");
					// jade.util.leap.Iterator it = msgReceived.getAllReceiver();
					// while(it.hasNext()) {
					// 	System.out.println(it.next());
					// }
					
					SerializableAgent sareceived = null;

					try {
						sareceived = (SerializableAgent)shareAgentContent.getContentObject();
					} catch (UnreadableException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					// Merge the map with optimized data
					this.myAgentData.getAgentMap().mergeMap(sareceived.getSg());
					
					if(ind != -1) { // If the agent was waiting for the map
						// Remove user, communication finished
						this.list_waitingMap.remove(ind);
						
						//Update agent info
						this.mapAgents.get(senderName).setCapacity(sareceived.getCapacity());
						this.mapAgents.get(senderName).setRessourceType(sareceived.getRessourceType());
						

						ACLMessage ack = new ACLMessage(ACLMessage.CANCEL);
						ack.setProtocol("ACKNOWLEDGE");
						ack.setSender(this.myAgent.getAID());
						ack.addReceiver(new AID(shareAgentContent.getSender().getLocalName(), false));
						((AbstractDedaleAgent)this.myAgent).sendMessage(ack);
						this.stop_counter = 0;
					}else {
						// Send map and wait ack
						this.list_waitingAck.add(shareAgentContent.getSender().getLocalName());
						stop_counter = _STOP_WAITING_TIME;
					}
				}else { // No Ping / Map received
					// Send a ping
					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
					msg.setProtocol("PING");
					msg.setSender(this.myAgent.getAID());
					for(String a : this.list_agentNames) {
						if(!a.equalsIgnoreCase(this.myAgent.getLocalName())) {
							// System.out.println("Agent " + this.myAgent.getLocalName() + " ping agent " + a);
							msg.addReceiver(new AID(a, false));
						}
					}
					((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				}
			}
			if(this.stop_counter > 0){
				this.stop_counter--;
				if(this.stop_counter == 0) {
					// Clear the waiting lists
					this.list_waitingAck.clear();
					this.list_waitingMap.clear();
				}
			}else {
				((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			}
		}
		this.turn_counter += 1;
		this.updateAgentsMeet();
	}

	@Override
	public boolean done() {
		return finished;
	}

}
