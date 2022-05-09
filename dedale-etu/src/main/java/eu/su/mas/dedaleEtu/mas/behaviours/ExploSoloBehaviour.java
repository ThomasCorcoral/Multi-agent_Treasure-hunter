package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.Arrays;
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
import eu.su.mas.dedaleEtu.mas.knowledge.HungarianAlgo;
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
		
		//System.out.println("ExploSoloBehaviour "+this.ag.getLocalName());
		//System.out.println(this.ag.getLocalName()+" :qteDiam "+this.ag.qteDiam+" qteGold "+this.ag.qteGold+" nb persoGold "+this.ag.PersoGold.size()+" nb PersoDiam "+this.ag.PersoDiam.size());
		//System.out.println("qte vide backpack gold : "+this.ag.freeSpaceGold+" qte vide backpack diamond : "+this.ag.freeSpaceDiam);
		//System.out.println("expertise : "+this.ag.expertise);
		
		if(this.myMap==null) {
			this.myMap= new MapRepresentation();
		}
		this.response=0;
		boolean wait = false; // Passage à la phase Harvest
		for(int k=0;k<3;k++) {
			//0) Retrieve the current position
			wait = false;
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
					this.myAgent.doWait(this.ag.WAITINGTIME);
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
								//System.out.println("AJOUT DIAMOND DEPUIS EXPLO "+this.ag.qteDiam);
							}
						}
						if(o2.getLeft()== Observation.GOLD) {
							if(!this.ag.locationGold.containsKey(o1.getLeft())) {
								this.ag.locationGold.put(o1.getLeft(), new Couple<Long,Integer>(System.currentTimeMillis(),o2.getRight()));
								this.ag.qteGold+=o2.getRight();
								//System.out.println("AJOUT GOLD DEPUIS EXPLO "+this.ag.qteGold);

							}
						}
						
						
					}
				}
				
				
				
				
				
				/********************************************
				 * SELECTION D'UN NOUVEL OBJECTIF (RECOLTE) *
				 ********************************************/
				if(this.ag.recolte) {
					if(this.ag.objHarvest == null) {
						if(this.ag.expertise.equals(Observation.DIAMOND)) {
							this.ag.objHarvest = new String[this.ag.locationDiam.size()];
						}else{
							this.ag.objHarvest = new String[this.ag.locationGold.size()];
						}
					}
					// Partie récolte si l'agent est arrivé sur l'objectif
					if(this.ag.harvestObj == null) {
						this.ag.TreasureHarvested();
					}
					// Partie définition du prochain objectif
					if(this.ag.harvestObj.startsWith("-") && this.ag.recolte) {
						// System.out.println("Définition d'un nouvel objectif de récolte !");
						this.ag.UpdateHarvest(myPosition);
					}
				}
				
				/*
				if(this.ag.recolte) {
					if(!this.ag.checkObjectiveHarvest()) {
						System.out.print("problème loc ");
						this.ag.UpdateHarvest(myPosition);
					}
					if(this.ag.expertise.equals(Observation.DIAMOND)) {
						if(this.ag.locationDiam.keySet().size() == 0) {
							this.ag.recolte = false;
							this.ag.finition = true;
						}
					}else {
						if(this.ag.locationGold.keySet().size() == 0) {
							this.ag.recolte = false;
							this.ag.finition = true;
						}
					}
				}*/
				
				/********************************************
				 *  *
				 ********************************************/
				if(this.ag.finition || this.ag.searchAgents) {
					//System.out.println(this.ag.dico.size());
					//System.out.println(this.ag.list_agentNames.size());
					nextNode = this.ag.defineRandomObjective(myPosition, nextNode);
					if(this.ag.finition) {
						this.ag.count_stop++;
						if(this.ag.count_stop == 20) {
							//response = -1;
							this.ag.printScore();

						}
					}
				}else if(this.ag.recolte) {
					
					if(!this.ag.placeWantToGo.equals(myPosition)) {
						this.ag.lock_turn++;
					}
					
					if(this.ag.lock_turn > 3) {
						this.ag.randomMove(10, nextNode);
						this.ag.lock_turn = 0;
						myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
					}
					
					if(this.ag.harvestObj == null) {
						this.ag.TreasureHarvested();
					}
					if(this.ag.harvestObj.startsWith("-") && this.ag.recolte) {
						this.ag.UpdateHarvest(myPosition);
					}
					
					if(!this.ag.harvestObj.equals(myPosition)) {
						nextNode=this.myMap.getShortestPath(myPosition, this.ag.harvestObj).get(0);
					}else {
						//System.out.println("----------------------------------------------------");
						//System.out.println(this.ag.getLocalName() + " : ancien obj : " + this.ag.oldHarvestObj + " | nvl obj : " + this.ag.harvestObj);
						this.ag.oldHarvestObj= this.ag.harvestObj; 
						this.ag.harvestObj = null;
					}
				}else if(this.openNodes.isEmpty()){
					//Explo fini
					wait = true;
				}else{
					if (nextNode==null){
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
				
				if(this.ag.recolte) {
					if(!(this.ag.harvestObj == null)) { // objectif non atteint
						this.ag.placeWantToGo=nextNode;
						((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
						this.ag.MovesHistory.add(nextNode);
					}
				}else if(!wait){
					this.ag.placeWantToGo=nextNode;
					((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
					this.ag.MovesHistory.add(nextNode);
				}
			}
			this.ag.updateMap(this.myMap);
			if(wait) {
				break;
			}
		}
		
		
		Map<String, ArrayList> dicoExplo = this.ag.dico;
		//System.out.println("Nombre d'agents croisés : "+dicoExplo.size()+" pour "+this.ag.getLocalName());
		//System.out.println("Nombre de noeuds ouverts : "+this.openNodes.size()+" pour "+this.ag.getLocalName());
        if((System.currentTimeMillis()-this.ag.tempsExplo>this.ag.timeout*1000 && !this.ag.recolte && !this.ag.finition) || wait || (!this.ag.finition && !this.ag.recolte && ((this.openNodes.size()<6 && dicoExplo.size()==this.ag.list_agentNames.size() && System.currentTimeMillis()-this.ag.tempsExplo>this.ag.timeout*1000) /*|| System.currentTimeMillis()-this.ag.tempsExplo>this.ag.timeout*1000 */))) {
        	if(wait || !(dicoExplo.size()==this.ag.list_agentNames.size())) {
        		this.ag.searchAgents = true;
        	}else {
        		this.ag.searchAgents = false;
        		this.ag.finitionObj = null;
        		this.ag.defineBestComposition();
        		this.ag.transisitonHarvest();
        	}
    	}
	}
	public int onEnd() {
		if(response == -1) {
			this.ag.doDelete();
		}
		return response;
	}


}
