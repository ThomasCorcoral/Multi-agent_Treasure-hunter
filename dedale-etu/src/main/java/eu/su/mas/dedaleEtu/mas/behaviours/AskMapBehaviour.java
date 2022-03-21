package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
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
public class AskMapBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	private boolean finished = false;
	private boolean hasDetectedNeighbour = false;

	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;

	private List<String> list_agentNames;
	

/**
 * 
 * @param myagent
 * @param myMap known map of the world the agent is living in
 * @param agentNames name of the agents to share the map with
 */
	public AskMapBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap,List<String> agentNames) {
		super(myagent);
		this.myMap=myMap;
		this.list_agentNames=agentNames;
		
		
	}

	@Override
	public void action() {

		//0) Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition

			/**
			 * Just added here to let you see what the agent is doing, otherwise he will be too quick
			 */
			try {
				this.myAgent.doWait(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}

			//1) remove the current node from openlist and add it to closedNodes.
			this.myMap.addNode(myPosition, MapAttribute.closed);

			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			List<Couple<Observation, Integer>> observations=null;
			String nextNode=null;
			Observation obs;
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			Couple<String, List<Couple<Observation, Integer>>> elem;
			while(iter.hasNext()){
				elem=iter.next();
				String nodeId=elem.getLeft();
				boolean isNewNode=this.myMap.addNewNode(nodeId);
				//the node may exist, but not necessarily the edge
				if (myPosition!=nodeId) {
					this.myMap.addEdge(myPosition, nodeId);
					if (nextNode==null && isNewNode) nextNode=nodeId;
				}
				if(elem.getRight()!=null) {
					observations=elem.getRight();
					for(int k=0;k<observations.size();k++) {
						obs=observations.get(k).getLeft();
						if(obs.getName().equals("Stench") && !hasDetectedNeighbour ) {
							sendRequest();
							hasDetectedNeighbour=true;	
						}
							
					}
				}
			}

			//3) while openNodes is not empty, continues.
			if (!this.myMap.hasOpenNode()){
				//Explo finished
				finished=true;
				System.out.println(this.myAgent.getLocalName()+" - Exploration successufully done, behaviour removed.");
			}else{
				//4) select next move.
				//4.1 If there exist one open node directly reachable, go for it,
				//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
				if (nextNode==null){
					//no directly accessible openNode
					//chose one, compute the path and take the first step.
					nextNode=this.myMap.getShortestPathToClosestOpenNode(myPosition).get(0);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
					//System.out.println(this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"| nextNode: "+nextNode);
				}else {
					System.out.println("nextNode notNUll - "+this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"\n -- nextNode: "+nextNode);
				}
				//5) At each time step, the agent check if he received a graph from a teammate. 	
				// If it was written properly, this sharing action should be in a dedicated behaviour set.
				if(hasDetectedNeighbour) {
					hasDetectedNeighbour=false;
					MessageTemplate msgTemplate=MessageTemplate.and(
							MessageTemplate.MatchProtocol("SHARE-TOPO"),
							MessageTemplate.MatchPerformative(ACLMessage.INFORM));
					ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
					if (msgReceived!=null) {
						SerializableSimpleGraph<String, MapAttribute> sgreceived=null;
						try {
							sgreceived = (SerializableSimpleGraph<String, MapAttribute>)msgReceived.getContentObject();
						} catch (UnreadableException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						this.myMap.mergeMap(sgreceived);
						sendReceived(msgReceived.getSender());
					}
					else {
						sendRequest();
					}
				}
				((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			}
			
		}
	}

	private void sendRequest() {
		System.out.println(this.myAgent.getName()+"a envoyé une Requête");
		final ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setSender(this.myAgent.getAID());
		for(String agentName : list_agentNames) {
			msg.addReceiver(new AID(agentName, AID.ISLOCALNAME));  
		}
			
		this.myAgent.send(msg);
		System.out.println(this.myAgent.getLocalName()+ "Request Message to share their Map sent to Agents");
	}
	private void sendReceived(AID receiver) {
		System.out.println(this.myAgent.getLocalName()+"A RECU UNE MAP");
		final ACLMessage msg = new ACLMessage(ACLMessage.CANCEL);
		msg.setSender(this.myAgent.getAID());
		msg.addReceiver(receiver);  
			
		this.myAgent.send(msg);
		
	}

	@Override
	public boolean done() {
		return finished;
	}

}
