package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.AgentOptimized;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;


/**
 * This behaviour allows an agent to explore the environment and learn the associated topological map.
 * The algorithm is a pseudo - DFS computationally consuming because its not optimised at all.</br>
 * 
 * When all the nodes around him are visited, the agent randomly select an open node and go there to restart its dfs.</br> 
 * This (non optimal) behaviour is done until all nodes are explored. </br> 
 * 
 * Warning, this behaviour does not save the content of visited nodes, only the topology.</br> 
 * Warning, this behaviour is a solo exploration and does not take into account the presence of other agents (or well) and indefinitely tries to reach its target node
 * @author hc
 *
 */
public class ExploSoloBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;

	/**
	 * Nodes known but not yet visited
	 */
	private ArrayList<String> openNodes;
	/**
	 * Visited nodes
	 */
	private Set<String> closedNodes;
	private AgentOptimized ag;
	private HashMap<String,Integer> noGo;

	private int response;


	public ExploSoloBehaviour(final AgentOptimized myagent, MapRepresentation myMap) {
		super(myagent);
		ag=myagent;
		this.myMap=myMap;
		this.openNodes=new ArrayList<String>();
		this.closedNodes=new HashSet<String>();
		this.noGo=ag.getNoGo();
	}

	@Override
	public void action() {
		if(this.ag.tempsExplo==-1) {
			this.ag.tempsExplo=System.currentTimeMillis();
		}
		System.out.println("ExploSoloBehaviour "+this.ag.getLocalName());
		System.out.println(this.ag.getLocalName()+" :qteDiam "+this.ag.qteDiam+" qteGold "+this.ag.qteGold+" nb persoGold "+this.ag.PersoGold.size()+" nb PersoDiam "+this.ag.PersoDiam.size());
		System.out.println("qte vide backpack gold : "+this.ag.freeSpaceGold+" qte vide backpack diamond : "+this.ag.freeSpaceDiam);
		System.out.println("expertise : "+this.ag.expertise);
		if(this.myMap==null) {
			this.myMap= new MapRepresentation();
		}
		this.response=0;
		for(int k=0;k<3;k++) {
			//0) Retrieve the current position
			String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
			ArrayList<String> aEliminer=new ArrayList<String>();
			for(String cle:noGo.keySet()) {
				Integer val=noGo.get(cle);
				if(val==0) {
					aEliminer.add(cle);
				}
				else {
					noGo.put(cle, val-1);
				}
				
			}
			for(String cle : aEliminer) {
				noGo.remove(cle);
			}
			this.ag.setNoGo(noGo);
			
			if(this.ag.placeWantToGo!=null && this.ag.placeWantToGo!=myPosition) {
				noGo.put(this.ag.placeWantToGo, 3);
			}
			
			if (myPosition!=null){
				List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
				//List of observable from the agent's current position
	
				/**
				 * Just added here to let you see what the agent is doing, otherwise he will be too quick
				 */
				try {
					this.myAgent.doWait(500);
				} catch (Exception e) {
					e.printStackTrace();
				}
	
				//1) remove the current node from openlist and add it to closedNodes.
				this.closedNodes.add(myPosition);
				this.openNodes.remove(myPosition);
	
				this.myMap.addNode(myPosition,MapAttribute.closed);
	
				//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
				String nextNode=null;
				Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
				while(iter.hasNext()){
					String nodeId=iter.next().getLeft();
					if (!this.closedNodes.contains(nodeId)){
						if (!this.openNodes.contains(nodeId)){
							this.openNodes.add(nodeId);
							this.myMap.addNode(nodeId, MapAttribute.open);
							this.myMap.addEdge(myPosition, nodeId);	
						}else{
							//the node exist, but not necessarily the edge
							this.myMap.addEdge(myPosition, nodeId);
						}
						if (nextNode==null && !noGo.containsKey(nodeId)) nextNode=nodeId;
					}
				}
				//2 bis) observe if there are treasures
				for(Couple<String, List<Couple<Observation, Integer>>> o1:lobs){
					for(Couple<Observation, Integer> o2:o1.getRight()) {
						
						if(o2.getLeft()== Observation.DIAMOND) {
							if(!this.ag.locationDiam.containsKey(o1.getLeft())) {
								this.ag.locationDiam.put(o1.getLeft(), new Couple<Long,Integer>(System.currentTimeMillis(),o2.getRight()));
								this.ag.qteDiam+=o2.getRight();
								System.out.println("AJOUT DIAMOND DEPUIS EXPLO "+this.ag.qteDiam);
							}
						}
						if(o2.getLeft()== Observation.GOLD) {
							if(!this.ag.locationGold.containsKey(o1.getLeft())) {
								this.ag.locationGold.put(o1.getLeft(), new Couple<Long,Integer>(System.currentTimeMillis(),o2.getRight()));
								this.ag.qteGold+=o2.getRight();
								System.out.println("AJOUT GOLD DEPUIS EXPLO "+this.ag.qteGold);

							}
						}
						
						
					}
				}
				
				
				
				if(this.ag.recolte) {
					//PARTIE RECOLTE ICI
					if(this.ag.expertise.equals(Observation.DIAMOND)) {
						for(String locD : this.ag.locationDiam.keySet()) {
							if(locD.equals(myPosition) && !this.ag.treasureHarvested.getRight().contains(myPosition)) {
								this.ag.openLock(Observation.DIAMOND);
								this.ag.pick();
								System.out.println(this.getAgent().getLocalName()+" pick a diamond treasure");
								
								int qteD = (int) this.ag.locationDiam.get(myPosition).getRight();
								if(qteD <=this.ag.freeSpaceDiamPerso) {
									this.ag.treasureHarvested.getRight().add(myPosition);
									this.ag.freeSpaceDiamPerso-=qteD;
								}
								else {
									this.ag.locationDiam.put(myPosition, new Couple<Long,Integer>(System.currentTimeMillis(),qteD-this.ag.freeSpaceDiamPerso));
									this.ag.freeSpaceDiamPerso=0;
								}
							}
						}
					}
					else if(this.ag.expertise.equals(Observation.GOLD)){
						for(String locG : this.ag.locationGold.keySet()) {
							if(locG.equals(myPosition) &&  !this.ag.treasureHarvested.getLeft().contains(myPosition)) {
								this.ag.openLock(Observation.GOLD);
								this.ag.pick();
								System.out.println(this.getAgent().getLocalName()+" pick a gold treasure");
								
								int qteG = (int) this.ag.locationGold.get(myPosition).getRight();
								if(qteG <=this.ag.freeSpaceGoldPerso) {
									this.ag.treasureHarvested.getLeft().add(myPosition);
									this.ag.freeSpaceGoldPerso-=qteG;
								}
								else {
									this.ag.locationGold.put(myPosition, new Couple<Long,Integer>(System.currentTimeMillis(),qteG-this.ag.freeSpaceGoldPerso));
									this.ag.freeSpaceGoldPerso=0;
								}
							}
						}
					}
					

				}
				if (this.openNodes.isEmpty()){
					//Explo finished
					System.out.println("Exploration successufully done, behaviour removed.");
					this.ag.recolte=true;
				}else{
					//4) select next move.
					//4.1 If there exist one open node directly reachable, go for it,
					//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
					if (nextNode==null){
						//no directly accessible openNode
						//chose one, compute the path and take the first step.
						
						//ArrayList<String> listeNodesToGo = ordonnOpenNodes(myPosition);
						nextNode=this.myMap.getShortestPath(myPosition, this.openNodes.get(0)).get(0);
						int ii=1;
						while(ii<this.openNodes.size() && noGo.containsKey(nextNode)) {
							String elem = this.openNodes.get(0);
							this.openNodes.add(elem);
							this.openNodes.remove(0);
							
							nextNode=this.myMap.getShortestPath(myPosition, this.openNodes.get(0)).get(0);
							ii+=1;
						}
						if(noGo.containsKey(nextNode)) {
							nextNode=myPosition;
						}
						
					}
			}
			
				
				
				
				
				
				/***************************************************
				** 		ADDING the API CALL to illustrate their use **
				*****************************************************/
				/*
				//list of observations associated to the currentPosition
				List<Couple<Observation,Integer>> lObservations= lobs.get(0).getRight();
				System.out.println(this.myAgent.getLocalName()+" - State of the observations : "+lobs);
				
				//example related to the use of the backpack for the treasure hun
				Boolean b=false;
				for(Couple<Observation,Integer> o:lObservations){
					switch (o.getLeft()) {
					case DIAMOND:case GOLD:

						System.out.println(this.myAgent.getLocalName()+" - My treasure type is : "+((AbstractDedaleAgent) this.myAgent).getMyTreasureType());
						System.out.println(this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
						System.out.println(this.myAgent.getLocalName()+" - My expertise is: "+((AbstractDedaleAgent) this.myAgent).getMyExpertise());
						System.out.println(this.myAgent.getLocalName()+" - I try to open the safe: "+((AbstractDedaleAgent) this.myAgent).openLock(Observation.GOLD));
						System.out.println(this.myAgent.getLocalName()+" - Value of the treasure on the current position: "+o.getLeft() +": "+ o.getRight());
						System.out.println(this.myAgent.getLocalName()+" - The agent grabbed : "+((AbstractDedaleAgent) this.myAgent).pick());
						System.out.println(this.myAgent.getLocalName()+" - the remaining backpack capacity is: "+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
						b=true;
						break;
					default:
						break;
					}
				}

				//If the agent picked (part of) the treasure
				if (b){
					List<Couple<String,List<Couple<Observation,Integer>>>> lobs2=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
					System.out.println(this.myAgent.getLocalName()+" - State of the observations after picking "+lobs2);
					
					//Trying to store everything in the tanker
					System.out.println(this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent)this.myAgent).getBackPackFreeSpace());
					System.out.println(this.myAgent.getLocalName()+" - The agent tries to transfer is load into the Silo (if reachable); succes ? : "+((AbstractDedaleAgent)this.myAgent).emptyMyBackPack("Silo"));
					System.out.println(this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent)this.myAgent).getBackPackFreeSpace());
					
				}
				
				//Trying to store everything in the tanker
				//System.out.println(this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent)this.myAgent).getBackPackFreeSpace());
				//System.out.println(this.myAgent.getLocalName()+" - The agent tries to transfer is load into the Silo (if reachable); succes ? : "+((AbstractDedaleAgent)this.myAgent).emptyMyBackPack("Silo"));
				//System.out.println(this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent)this.myAgent).getBackPackFreeSpace());

				*/
				/************************************************
				 * 				END API CALL ILUSTRATION
				 *************************************************/
				
				this.ag.placeWantToGo=nextNode;
				((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
				
				
	
			}
			this.ag.updateMap(this.myMap);
		}
		
		
		Map<String, ArrayList> dicoExplo = this.ag.dico;
		System.out.println("Nombre d'agents croisés : "+dicoExplo.size()+" pour "+this.ag.getLocalName());
		System.out.println("Nombre de noeuds ouverts : "+this.openNodes.size()+" pour "+this.ag.getLocalName());
        if((this.openNodes.size()<6 && dicoExplo.size()>=0.9*this.ag.list_agentNames.size() && System.currentTimeMillis()-this.ag.tempsExplo>60*1000) || System.currentTimeMillis()-this.ag.tempsExplo>this.ag.timeout*1000 ) {
            System.out.println("HARVEST "+this.ag.getLocalName());
        	this.ag.recolte=true;
        }
	}
	public int onEnd() {
		return response;
	}


}
