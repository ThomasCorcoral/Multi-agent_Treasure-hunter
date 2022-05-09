package eu.su.mas.dedaleEtu.mas.agents.dummies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

import dataStructures.serializableGraph.SerializableNode;
import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.ExploSoloBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SendPing;
import eu.su.mas.dedaleEtu.mas.behaviours.SendMap;
import eu.su.mas.dedaleEtu.mas.behaviours.CheckMailBox;
import eu.su.mas.dedaleEtu.mas.behaviours.UpdateOwnMap;
import eu.su.mas.dedaleEtu.mas.behaviours.CheckMapReception;
import eu.su.mas.dedaleEtu.mas.behaviours.WaitACK;
import eu.su.mas.dedaleEtu.mas.behaviours.UpdateOtherAgentData;
import eu.su.mas.dedaleEtu.mas.behaviours.SendACK;
import eu.su.mas.dedaleEtu.mas.knowledge.HungarianAlgo;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.SerializableAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import net.sourceforge.plantuml.sequencediagram.AbstractMessage;

public class AgentOptimized extends AbstractDedaleAgent {
	private static final long serialVersionUID = -7969468610241668140L;
	public List<String> list_agentNames;
	public HashMap<AID,int[]> agentsCapacity = new HashMap<AID,int[]>(); 
	public MapRepresentation myMap;
	public SerializableSimpleGraph<String, MapAttribute> MapReceived=null;
	public HashMap<String,Integer> noGo = new HashMap<String,Integer>(); 
	public String placeWantToGo=null;
	public AID senderPing ;
	public AID otherAgent ;
	public int nbTotalNodes=0;
	public int cptRegisteredNodes=0;
	public int nbAgentCrossed=0;
	
	public int WAITINGTIME = 200;
	
	public ArrayList<AID> WaitingForMAp=new ArrayList<AID>();
	//locationGold contient la liste des noeuds contenant de l'or associé au couple l'heure où il a été découvert et la quantité d'or que le noeud contient
	public HashMap<String,Couple>locationGold=new HashMap<String,Couple>();
	//locationGold contient la liste des noeuds contenant de diamant associé au couple l'heure où il a été découvert et la quantité d'or que le noeud contient
	public HashMap<String,Couple>locationDiam=new HashMap<String,Couple>();
	public Observation expertise=Observation.GOLD; //par défaut tout les agents choisissent de se pécialiser dans l'or
	public boolean gotPing=false;
	public Map<String,ArrayList> dico=new HashMap<String,ArrayList>();
	public HashMap<AID,Couple> PersoGold = new HashMap<AID,Couple>();
	public HashMap<AID,Couple> PersoDiam = new HashMap<AID,Couple>();

	public HashMap<String,Couple>localLocationGold;
	public HashMap<String,Couple>localLocationDiam;
	
	public int freeSpaceGold,freeSpaceDiam,freeSpaceGoldPerso,freeSpaceDiamPerso,quantityToPick, backpackGold, backpackDiam;
	public boolean recolte=false;
	public int qteGold=0;
	public int qteDiam=0;
	public int optTreasure=0;
	public long tempsExplo=-1;
	public Couple<ArrayList,ArrayList> treasureHarvested=new Couple<ArrayList,ArrayList>(new ArrayList<String>(),new ArrayList<String>());// Partie droite : trésors Gold récoltés et partie gauche : trésor diamant récoltés
	public long timeout=2*60+1*30;//après au plus 2 minutes d'exploration, on passe l'agent en phase de récolte
	public float difOpt = -1;
	public String harvestObj="-1";
	public boolean finition = false;
	public float objectif = -1;
	public String secondObj = "-1";
	public String[] objHarvest = null;
	
	public boolean interlock = false;
	public List<String> pathlock;
	public MapRepresentation lockMap;
	public String oldHarvestObj = "";
	
	public String finitionObj;
	
	public boolean searchAgents = false;
	
	public List<String> MovesHistory = new ArrayList<String>();
	public int currentMoveHistory = 0;
	
	public int scoreObj = 100000;
	
	public int lock_turn = 0;
	
	private int max_finition = 0;
	private int max_local_finition = 0;
	
	public int count_stop = 0;
	
	private static final String Exploration = "Exploration";
	private static final String SendPing = "SendPing";
	private static final String CheckMailBox = "CheckMailBox";
	private static final String SendMapPing = "SendMapPing";
	private static final String SendMapMap = "SendMapMap";
	private static final String UpdateOwnMap1 = "UpdateOwnMap1";
	private static final String UpdateOwnMap2 = "UpdateOwnMap2";
	private static final String CheckMapReception = "CheckMapReception";
	private static final String WaitACK = "WaitACK";
	private static final String SendACK = "SendACK";
	private static final String UpdateOtherAgentData = "UpdateOtherAgentData";

	
	
	protected void setup() {
		super.setup();
		final Object[] args = getArguments();
		
		list_agentNames=new ArrayList<String>();
		
		if(args.length==0){
			System.err.println("Error while creating the agent, names of agent to contact expected");
			System.exit(-1);
		}else{
			int i=2;// WARNING YOU SHOULD ALWAYS START AT 2. This will be corrected in the next release.
			while (i<args.length) {
				list_agentNames.add((String)args[i]);
				i++;
			}
		}
		List<Couple<Observation, Integer>> backpack = this.getBackPackFreeSpace();
		Couple<Observation, Integer> backpack1 = backpack.get(0);
		Couple<Observation, Integer> backpack2 = backpack.get(1);
		if(backpack1.getLeft().equals(Observation.GOLD)) {
			this.freeSpaceGold = backpack1.getRight();
			this.freeSpaceGoldPerso = backpack1.getRight();
			this.backpackGold = this.freeSpaceGoldPerso;
			
			this.freeSpaceDiam = backpack2.getRight();
			this.freeSpaceDiamPerso = backpack2.getRight();
			this.backpackDiam = this.freeSpaceDiamPerso;

		}
		else {
			if(backpack2.getLeft().equals(Observation.GOLD)){
				this.freeSpaceGold = backpack2.getRight();
				this.freeSpaceGoldPerso = backpack2.getRight();

				this.freeSpaceDiam = backpack1.getRight();
				this.freeSpaceDiamPerso = backpack1.getRight();
				}
		}
		
		this.backpackDiam = this.freeSpaceDiamPerso;
		this.backpackGold = this.freeSpaceGoldPerso;
		
		this.PersoGold.put(this.getAID(),new Couple<Long,Integer>(System.currentTimeMillis(), freeSpaceGoldPerso));
		
		int[] capa = {this.freeSpaceGoldPerso, this.freeSpaceDiamPerso};
		this.agentsCapacity.put(this.getAID(), capa);
		
		
		FSMBehaviour fsm = new FSMBehaviour(this);
		List<Behaviour> lb=new ArrayList<Behaviour>();
		
		// Define the different states and behaviours
		fsm. registerFirstState (new ExploSoloBehaviour(this,myMap), Exploration);
		fsm. registerState (new SendPing(this,list_agentNames), SendPing);
		fsm. registerState (new SendMap(this,true), SendMapPing);
		fsm. registerState (new SendMap(this,false), SendMapMap);
		fsm. registerState (new CheckMailBox(this), CheckMailBox);
		fsm. registerState (new UpdateOwnMap(this), UpdateOwnMap1);
		fsm. registerState (new UpdateOwnMap(this), UpdateOwnMap2);
		fsm. registerState (new CheckMapReception(this), CheckMapReception);
		fsm. registerState (new WaitACK(this), WaitACK);
		fsm. registerState (new UpdateOtherAgentData(this), UpdateOtherAgentData);
		fsm. registerState (new SendACK(this), SendACK);
		
		// Register the transitions
		fsm. registerTransition (Exploration,CheckMailBox,0);
		fsm. registerDefaultTransition (SendPing,Exploration);
		fsm. registerTransition (CheckMailBox,SendPing,0);
		fsm. registerTransition (CheckMailBox,SendMapPing,2);
		fsm. registerTransition (CheckMailBox,SendMapMap,1);
		fsm. registerTransition (CheckMailBox,UpdateOwnMap1,3);
		fsm. registerTransition (CheckMailBox,UpdateOtherAgentData,4);
		fsm. registerDefaultTransition (SendMapMap,UpdateOwnMap2);
		fsm. registerDefaultTransition (UpdateOwnMap2,WaitACK);
		fsm. registerTransition (WaitACK,Exploration, 1) ;
		fsm. registerTransition (WaitACK,UpdateOtherAgentData, 2) ;
		fsm. registerDefaultTransition(SendMapPing,CheckMapReception) ;
		fsm. registerTransition (CheckMapReception,Exploration, 1) ;
		fsm. registerTransition (CheckMapReception,UpdateOwnMap1, 2) ;
		fsm. registerDefaultTransition(UpdateOwnMap1,SendACK) ;
		fsm. registerDefaultTransition(SendACK,UpdateOtherAgentData) ;
		fsm. registerDefaultTransition(UpdateOtherAgentData,Exploration) ;

		lb.add(fsm);
		
		addBehaviour(new startMyBehaviours(this,lb));
		
		
		System.out.println("the agent "+this.getLocalName()+ " is started");
		

	}
	
	protected void takeDown(){
		super.takeDown();
	}
	
	@SuppressWarnings("unchecked")
	public void fusionData(SerializableAgent sAg) {
		
		int[] capa = {sAg.freeSpaceGoldPerso, sAg.freeSpaceDiamPerso};
		this.agentsCapacity.put(sAg.agentId, capa);
		
		this.updateLocation(sAg.getLocationDiam(), sAg.getLocationGold());
		// this.updatePersoTreasure(sAg.getPersoDiam(), sAg.getPersoGold());
		// this.updateFreeSpaces();
		String myPosition=((AbstractDedaleAgent)this).getCurrentPosition();
		
		//sAg.getTreasuresHarvested()

		if(!this.recolte) {
			UpdateExpertise();
		}else if(this.recolte){
			
			// Objectif de récolte égaux
			if(sAg.harvestObj == this.harvestObj) {
				if(this.scoreObj < sAg.scoreObj) {
					if(this.expertise.equals(Observation.DIAMOND)) {
						this.treasureHarvested.getLeft().add(this.harvestObj);
					}else {
						this.treasureHarvested.getRight().add(this.harvestObj);
					}
					this.updateLocalLocation();
					UpdateHarvest(myPosition);
				}
			}
			
			// System.out.println(sAg.placeWantToGo + " vs " + myPosition);
			// L'autre agent veut accéder à la case sur laquelle est notre agent
			if(sAg.placeWantToGo.equals(myPosition)) {
				this.commonObjective(sAg.agentPosition, myPosition);
			}
			
			
			// Mise à jour les trésors ramassées par l'autre agent
			this.updateHarvestedOtherAgent(sAg.getLocationDiam(), sAg.getLocationGold(), sAg.getTreasuresHarvested());
			
			this.updateLocalLocation();
		}else if(this.finition) {
			if(sAg.getRecolte() && sAg.placeWantToGo.equals(myPosition)) {
				List<String> cpy = this.MovesHistory;
				for(int i = 0; i < 10; i++) {
					this.placeWantToGo=this.MovesHistory.get(this.MovesHistory.size()-1-i);
					((AbstractDedaleAgent)this).moveTo(this.placeWantToGo);
					cpy.add(this.placeWantToGo);
				}
				this.MovesHistory = cpy;
			}
		}
	}
	
	public void updateRepartition() {
		
		
		
	}
	
	/**
	 * OK màj des trésors
	 * 
	 * @param locDiamsAg : Position des diamants pour l'autre agent
	 * @param locGoldsAg : Position des golds pour l'autre agent
	 */
	public void updateLocation(HashMap<String, Couple> locDiamsAg, HashMap<String, Couple> locGoldsAg) {
		
		
		
		
		
		for(String loc : locDiamsAg.keySet()) {
			if(!this.locationDiam.containsKey(loc)) {
				if(!this.treasureHarvested.getLeft().contains(loc)) {
					this.locationDiam.put(loc, locDiamsAg.get(loc));
					this.qteDiam+=(int)locDiamsAg.get(loc).getRight();
				}
			}else if((Long)locDiamsAg.get(loc).getLeft() < (Long)this.locationDiam.get(loc).getLeft()) {
				this.qteDiam -= (int)this.locationDiam.get(loc).getRight();
				this.qteDiam += (int)this.locationDiam.get(loc).getRight();
				this.locationDiam.replace(loc, locDiamsAg.get(loc));
			}
		}
		
		for(String loc : locGoldsAg.keySet()) {
			if(!this.locationGold.containsKey(loc)) {
				if(!this.treasureHarvested.getRight().contains(loc)) {
					this.locationGold.put(loc, locGoldsAg.get(loc));
					this.qteGold+=(int)locGoldsAg.get(loc).getRight();
				}
			}else if((Long)locGoldsAg.get(loc).getLeft() < (Long)this.locationGold.get(loc).getLeft()) {
				this.qteGold -= (int)this.locationGold.get(loc).getRight();
				this.qteGold += (int)this.locationGold.get(loc).getRight();
				this.locationGold.replace(loc, locGoldsAg.get(loc));
			}
		}
	}
	
	/**
	 * OK màj les capacités des agents
	 * 
	 * @param persoDiamsAg
	 * @param persoGoldsAg
	 */
	public void updatePersoTreasure(HashMap<AID, Couple> persoDiamsAg, HashMap<AID, Couple> persoGoldsAg) {
		for(AID persG : persoGoldsAg.keySet()) {
			if(!this.PersoGold.containsKey(persG) || ((Long)this.PersoGold.get(persG).getLeft() > (Long)persoGoldsAg.get(persG).getLeft() && (Long)persoGoldsAg.get(persG).getLeft() >= 0)) {
				this.PersoGold.put(persG,persoGoldsAg.get(persG));
			}
			if(this.PersoDiam.containsKey(persG)) {
				this.PersoDiam.remove(persG);
			}
		}
		for(AID persD : persoDiamsAg.keySet()) {
			if(!this.PersoDiam.containsKey(persD) || ((Long)this.PersoDiam.get(persD).getLeft() > (Long)persoDiamsAg.get(persD).getLeft() &&  (Long)persoDiamsAg.get(persD).getLeft() >= 0)) {
				this.PersoDiam.put(persD,persoDiamsAg.get(persD));
			}
			if(this.PersoGold.containsKey(persD)) {
				this.PersoGold.remove(persD);
			}
		}
	}
	
	/**
	 * OK màj des capacités totales des agents
	 */
	public void updateFreeSpaces() {
		this.freeSpaceGold=0;
		this.freeSpaceDiam=0;
		for(Couple<Long,Integer> persG : this.PersoGold.values()) {
			this.freeSpaceGold+=persG.getRight();
		}
		for(Couple<Long,Integer> persD : this.PersoDiam.values()) {
			this.freeSpaceDiam+=persD.getRight();
		}
	}
	
	
	public void commonObjective(String otherAgentPosition, String myPosition) {
		//System.out.println("INTERBLOCAGE");
		if(this.placeWantToGo != myPosition) {
			this.lockMap = this.myMap;
			this.interlock = true;
			this.lockMap.removeNode(this.placeWantToGo);
			this.myMap.getGraph().edges().forEach(edge -> {
				if(edge.getNode0().getId().equals(this.placeWantToGo) || edge.getNode1().getId().equals(this.placeWantToGo)) {
					this.lockMap.getGraph().removeEdge(edge.getNode0(), edge.getNode1());
				}
			});
			try {
				//System.out.println("LockMap : " + myPosition + " | " + this.harvestObj);
				myPosition=((AbstractDedaleAgent)this).getCurrentPosition();
				this.pathlock = this.lockMap.getShortestPath(myPosition, this.harvestObj);
				if(this.pathlock == null) {
					//System.out.println("PAS ITINERAIRE BIS");
					/*this.interlock = false;
					for(int i = 0; i < 5; i++) {
						this.placeWantToGo=this.MovesHistory.get(this.MovesHistory.size()-1-i);
						((AbstractDedaleAgent)this).moveTo(this.placeWantToGo);
					}*/
				}else {
					//System.out.println("ITINERAIRE BIS");
					int go = Math.min(5, this.pathlock.size());
					for(int i = 0; i < go; i++) {
						myPosition=((AbstractDedaleAgent)this).getCurrentPosition();
						this.placeWantToGo=this.lockMap.getShortestPath(myPosition, this.harvestObj).get(0);
						((AbstractDedaleAgent)this).moveTo(this.placeWantToGo);
					}
				}
			}catch(Exception e) {
				//System.out.println("PAS ITINERAIRE BIS");
				this.interlock = false;
				//this.randomMove(5, this.placeWantToGo);
				
				/*for(int i = 0; i < 5; i++) {
					
					
					
					this.placeWantToGo=this.MovesHistory.get(this.MovesHistory.size()-1-i);
					this.placeWantToGo=this.lockMap.getShortestPath(myPosition, this.harvestObj).get(0);
					((AbstractDedaleAgent)this).moveTo(this.placeWantToGo);
				}*/
			}
		}
	}
	
	public void updateHarvestedOtherAgent(HashMap<String, Couple> locDiamsAg, HashMap<String, Couple> locGoldsAg, Couple<ArrayList, ArrayList> harvestedSAg) {
		
		// Parcoure tous trésors de moi. SI L'AUTRE a HARVEST. SI L'AUTRE A PAS DANS SES LOCATION - SUPPRIME
		

		if(this.expertise.equals(Observation.DIAMOND)) {
			HashMap<String, Couple> cpy = (HashMap<String, Couple>) this.locationDiam.clone();
			for( String locD : this.locationDiam.keySet() ) {
				if(harvestedSAg.getLeft().contains(locD)) {
					if(!locDiamsAg.containsKey(locD)) {
						cpy.remove(locD);
						this.treasureHarvested.getLeft().add(locD);
					}
				}
			}
			this.locationDiam = cpy;
		}else {
			HashMap<String, Couple> cpy = (HashMap<String, Couple>) this.locationGold.clone();
			for( String locG : this.locationGold.keySet() ) {
				if(harvestedSAg.getLeft().contains(locG)) {
					if(!locGoldsAg.containsKey(locG)) {
						cpy.remove(locG);
						this.treasureHarvested.getRight().add(locG);
					}
				}
			}
			this.locationGold = cpy;
		}
	}
	
	/**
	 * OK, met à jour les locations non récoltées par l'agent.
	 */
	public void updateLocalLocation() {
		this.localLocationDiam = (HashMap<String, Couple>) this.locationDiam.clone();
		this.localLocationGold = (HashMap<String, Couple>) this.locationGold.clone();
		
		for(String locD : this.locationDiam.keySet()) {
			if(this.treasureHarvested.getLeft().contains(locD)) {
				localLocationDiam.remove(locD);
			}
		}
		
		for(String locG : this.locationGold.keySet()) {
			if(this.treasureHarvested.getRight().contains(locG)) {
				localLocationGold.remove(locG);
			}
		}
		
	}
	
	public void printLocation(HashMap<String, Couple> location, String type) {
		System.out.println(type + " :");
		for(String loc : location.keySet()) {
			System.out.print(loc);
			System.out.print(" ");
		}
		System.out.println("");
	}
	
	/**
	 * Récolte d'un trésor lorsque l'agent arrive sur place
	 * 
	 * @param myPosition : position de l'agent
	 */
	public void TreasureHarvested() {
		
		// List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this).observe();
		
		// Partie récolte si l'agent est arrivé sur l'objectif
		String myPosition=((AbstractDedaleAgent)this).getCurrentPosition();
//		System.out.println(this.getLocalName() + " RECOLTE en " + myPosition);
		if(this.expertise.equals(Observation.DIAMOND)) {
//			this.printLocation(this.locationDiam, "Diam");
			try {
				// int qteD = (int) this.locationDiam.get(myPosition).getRight();
				boolean t = this.openLock(Observation.DIAMOND);
				if(t) {
//					System.out.println(this.getLocalName() + " COFFRE OUVERT EN " + myPosition);
				}else {
//					System.out.println(this.getLocalName() + " COFFRE PAS OUVERT EN " + myPosition);
				}
				int qteD = this.pick();
				if(qteD <=this.freeSpaceDiamPerso) {
//					System.out.println("Diamants récoltés qt : " + qteD + " | freespace : " + this.freeSpaceDiamPerso);
					this.freeSpaceDiamPerso-=qteD;
					this.locationDiam.remove(myPosition); // Trésor entierement ramassé
				}
				else {
//					System.out.println("Diamants récoltés qt : " + this.freeSpaceDiamPerso + " | freespace : " + this.freeSpaceDiamPerso + " | qt dispo : " + qteD);
					this.locationDiam.put(myPosition, new Couple<Long,Integer>(System.currentTimeMillis(),qteD-this.freeSpaceDiamPerso));
					this.freeSpaceDiamPerso=0;
					this.finition = true;
					this.recolte = false;
				}
				this.treasureHarvested.getRight().add(myPosition);
		    } catch (Exception e) {
//			      System.out.println("Pas de diamants ici.");
//			      System.out.println(this.getLocalName() + " : myposition : " + myPosition + "| obj : " + this.harvestObj);
			      this.locationDiam.remove(myPosition); // Trésor entierement ramassé
		    }
//			this.printLocation(this.locationDiam, "Diam");
		}else {
			try {
//				this.printLocation(this.locationGold, "Gold");
				// int qteG = (int) this.locationGold.get(myPosition).getRight();
				boolean t = this.openLock(Observation.GOLD);
				if(t) {
//					System.out.println(this.getLocalName() + " COFFRE OUVERT EN " + myPosition);
				}else {
//					System.out.println(this.getLocalName() + " COFFRE PAS OUVERT EN " + myPosition);
				}
				int qteG = this.pick();
				if(qteG <=this.freeSpaceGoldPerso) {
//					System.out.println("Gold récoltés qt : " + qteG + " | freespace : " + this.freeSpaceGoldPerso);
					this.freeSpaceGoldPerso-=qteG;
					this.locationGold.remove(myPosition); // Trésor entierement ramassé
				}
				else {
//					System.out.println("FINITION - Gold récoltés qt : " + this.freeSpaceGoldPerso + " | freespace : " + this.freeSpaceGoldPerso + " | qt dispo : " + qteG);
					this.locationGold.put(myPosition, new Couple<Long,Integer>(System.currentTimeMillis(),qteG-this.freeSpaceGoldPerso));
					this.freeSpaceGoldPerso=0;
					this.finition = true;
					this.recolte = false;
				}
				this.treasureHarvested.getLeft().add(myPosition);
		    } catch (Exception e) {
//			      System.out.println("Pas de gold ici.");
//			      System.out.println(this.getLocalName() + " : myposition : " + myPosition + "| obj : " + this.harvestObj);
			      this.locationGold.remove(myPosition); // Trésor entierement ramassé
		    }
//			this.printLocation(this.locationGold, "Gold");
//			System.out.println("----------------------------------------------------");
		}
		if(this.expertise.equals(Observation.DIAMOND)) {
			if(this.freeSpaceDiamPerso < this.objectif) {
				this.finition = true;
			}
		}else {
			if(this.freeSpaceGoldPerso < this.objectif) {
				this.finition = true;
			}
		}
		this.harvestObj = "-1";
	}
	
	/**
	 * Préparation du plan de récolte seul
	 * 
	 * @param location : emplacement des trésors
	 * @param myPosition : position de l'agent
	 */
	public void aloneHarvest(HashMap<String, Couple> location, String myPosition) {
		int minDistDiams = -1;
		for(String loc : location.keySet()) {
			int currMin = (int) this.myMap.getShortestPath(myPosition, loc).size();
			if(minDistDiams == -1 || currMin < minDistDiams) {
				minDistDiams = currMin;
				this.harvestObj = loc;
			}
		}
	}
	
	/**
	 * Préparation du plan de récolte à plusieurs
	 * 
	 * @param location : emplacement des trésors
	 * @param Perso : agents récoltant ce trésor
	 * @param myPosition : position de l'agent 
	 * @param backpack : capacité du sac de l'agent
	 * @param freeSpace : espace disponible dans le sac de l'agent
	 * @param harvested : liste des trésors récoltés
	 */
	public void coopHarvest(HashMap<String, Couple> location, HashMap<AID, Couple> Perso, String myPosition, int backpack, int freeSpace, ArrayList harvested) {
		float dMoy = 0;
		for(String loc : location.keySet()) {
			dMoy += this.myMap.getShortestPath(myPosition, loc).size();
		}
		dMoy /= location.size();

		int currentAgentId = -1;
		int size = Math.max(location.size(), Perso.size());
		int[][] matrix = new int[size][size];
		for(int i = 0; i<size;i++){
	        Arrays.fill(matrix[i], 0);
        }
		int i = 0;
		int j = 0;
		
		for(String loc : location.keySet()) {
			j=0;
			int valTreasure = (int) location.get(loc).getRight();
			for(AID pers : Perso.keySet()) {
				int dist = this.myMap.getShortestPath(myPosition, loc).size();
				// System.out.print(pers + " ");
				if(pers.equals(this.getAID())) {
					// System.out.print("OK ");
					currentAgentId = j;
					float missOpt = (this.optTreasure*backpack) - (backpack - freeSpace);
					int difOpt = (int) Math.abs(missOpt - valTreasure);
					/*if(harvested.contains(loc)) {
						difOpt += 10000;
					}*/
					matrix[i][j] = (int) dist + difOpt;
				}else {
					int difOpt = 0;
					if(valTreasure > (int) Perso.get(pers).getRight()) {
						difOpt = Math.abs(this.optTreasure - (int) Perso.get(pers).getRight());
					}else {
						difOpt = Math.abs(this.optTreasure - valTreasure);
					}
					if(dist > dMoy) {
						matrix[i][j] = (int) (1.5 * dist +difOpt);
					}else {
						matrix[i][j] = (int) (0.75 * dist +difOpt);
					}
				}
				j+=1;
			}
			i+=1;
		}
		
		if(matrix.length == 0) {
			this.recolte = false;
			this.finition = true;
		}else {
		
			HungarianAlgo ha = new HungarianAlgo(matrix);
			int[][] assignment = ha.findOptimalAssignment();
			
			int col = 0;
			for(int ii=0; ii < assignment.length; ii++) {
				if(assignment[ii][1] == currentAgentId) {
					col = ii;
					break;
				}
			}
			try {
				this.scoreObj = matrix[col][currentAgentId];
			}catch(Exception e) {
				this.scoreObj = matrix[col][0];
			}
			
			if(location.keySet().toArray().length == 0) {
				if(this.expertise.equals(Observation.GOLD)) {
					if(this.backpackGold == this.freeSpaceGold) {
						this.expertise = Observation.DIAMOND;
						this.harvestObj = "-1";
					}else {
						this.finition = true;
						this.recolte = false;
					}
				}else {
					if(this.backpackDiam == this.freeSpaceDiam) {
						this.expertise = Observation.GOLD;
						this.harvestObj = "-1";
					}else {
						this.finition = true;
						this.recolte = false;
					}
				}
			}else {
				try {
					this.harvestObj = (String) location.keySet().toArray()[col];
				}catch (Exception e) {
					this.harvestObj = (String) location.keySet().toArray()[0];
				}
			}
		}
	}
	
	public void UpdateHarvest(String myPosition) {
		if(this.expertise.equals(Observation.DIAMOND)) {
			if(this.PersoDiam.size() == 1) { // L'agent est le seul à récolter des diamants
				this.aloneHarvest(this.locationDiam, myPosition);
			}else { // Il y a plusieurs agents qui veulent la même ressource	
				this.coopHarvest(this.locationDiam, this.PersoDiam, myPosition, this.backpackDiam, this.freeSpaceDiamPerso, this.treasureHarvested.getLeft());
			}
		}else{ // Partie Gold
			// System.out.println("Récolte des golds seul !");
			if(this.PersoGold.size() == 1) { // L'agent est le seul à récolter du gold
				this.aloneHarvest(this.locationGold, myPosition);
			}else { // Il y a plusieurs agents qui veulent la même ressource
				this.coopHarvest(this.locationGold, this.PersoGold, myPosition, this.backpackGold, this.freeSpaceGoldPerso, this.treasureHarvested.getRight());
			}	
		}
		//System.out.println(this.getLocalName() + " : Nvl objectif - " + this.harvestObj);
	}
	
	public void UpdateExpertise() {
		int goldApp1 = Math.min(qteGold,freeSpaceGold);
		int diamApp1 = Math.min(qteDiam,freeSpaceDiam);
		
		int goldApp2,diamApp2;
		if(expertise.equals(Observation.GOLD)) {
			goldApp2 = Math.min(qteGold,this.freeSpaceGold-this.freeSpaceGoldPerso);
			diamApp2 = Math.min(qteDiam,this.freeSpaceDiam+this.freeSpaceDiamPerso);
			
			if(goldApp2+diamApp2>goldApp1+diamApp1) {
				expertise=Observation.DIAMOND;
				this.PersoGold.remove(this.getAID());
				this.PersoDiam.put(this.getAID(),new Couple<Long,Integer>(System.currentTimeMillis(), this.freeSpaceDiamPerso));
				this.freeSpaceGold-=this.freeSpaceGoldPerso;
				this.freeSpaceDiam+=this.freeSpaceDiamPerso;	
			}
		}
		else {
			goldApp2 = Math.min(qteGold,this.freeSpaceGold+this.freeSpaceGoldPerso);
			diamApp2 = Math.min(qteDiam,this.freeSpaceDiam-this.freeSpaceDiamPerso);
			
			if(goldApp2+diamApp2>goldApp1+diamApp1) {
				expertise=Observation.GOLD;
				this.PersoDiam.remove(this.getAID());
				this.PersoGold.put(this.getAID(),new Couple<Long,Integer>(System.currentTimeMillis(), this.freeSpaceGoldPerso));
				this.freeSpaceGold+=this.freeSpaceGoldPerso;
				this.freeSpaceDiam-=this.freeSpaceDiamPerso;
			}
		}
		
		
		if(expertise.equals(Observation.GOLD) && freeSpaceGold!=0) {
			this.quantityToPick=Math.min(freeSpaceGoldPerso,(freeSpaceGoldPerso/freeSpaceGold)*qteGold);

		}
		else if(expertise.equals(Observation.DIAMOND) && freeSpaceDiam!=0){
			this.quantityToPick=Math.min(freeSpaceDiamPerso,(freeSpaceDiamPerso/freeSpaceDiam)*qteDiam);
		}
	}
	
	public void updateMap(MapRepresentation map) {
		this.myMap=map;
	}
	
	public MapRepresentation getMyMap() {
		return myMap;
	}
	
	public HashMap<String,Integer> getNoGo() {
		return noGo;
	}
	
	public void setNoGo(HashMap<String,Integer> ng) {
		this.noGo=ng;
	}
	
	public SerializableSimpleGraph<String, MapAttribute> getMapReceived() {
		return this.MapReceived;
	}
	
	public void transisitonHarvest() {
		System.out.println("START HARVEST "+this.getLocalName());
    	this.recolte=true;
    	
    	this.localLocationDiam = this.locationDiam;
    	this.localLocationGold = this.locationGold;
    	
    	this.optTreasure = (this.qteGold + this.qteDiam) /  (this.freeSpaceGold+this.freeSpaceDiam);
    	if(this.optTreasure > 1) {
    		this.optTreasure = 1;
		}
    	
		if(this.expertise.equals(Observation.DIAMOND)) {
			this.objectif = (float) (this.freeSpaceDiamPerso - this.optTreasure*0.95*this.freeSpaceDiamPerso);
		}else {
			this.objectif = (float) (this.freeSpaceGoldPerso - this.optTreasure*0.95*this.freeSpaceGoldPerso);
		}

//		System.out.println("Agent : " + this.getLocalName());
//		System.out.println("Expertise : " + this.expertise);
//		System.out.print("Diam : ");
		for(AID persD : this.PersoDiam.keySet()) {
//			System.out.print(persD.getLocalName() + " ");
			if(persD.equals(this.getAID())) {
				this.expertise = Observation.DIAMOND;
			}
		}
//		System.out.println("");
//		System.out.print("Gold : ");
		for(AID persG : this.PersoGold.keySet()) {
			if(persG.equals(this.getAID())) {
				this.expertise = Observation.GOLD;
			}
//			System.out.print(persG.getLocalName() + " ");
		}
//		System.out.println("");
//		System.out.println("Expertise : " + this.expertise);
//		System.out.println("");
	}
	
	/**
	 * Random 
	 */
	public void defineFinitionObjective() {
		
		// Random objective
		
		int nb = (int) this.myMap.getGraph().nodes().count();
		int i = (int) (Math.random() * (nb-1)) ;
		this.finitionObj = this.myMap.getGraph().getNode(i).getId();
		//System.out.println(this.finitionObj);
		/*
		
		this.myMap.getGraph().nodes().forEach(node ->{
			this.max_local_finition = 0;
			this.myMap.getGraph().edges().forEach(edge -> {
				if(edge.getNode0().getId() == node.getId() || edge.getNode1().getId() == node.getId()) {
					this.max_local_finition += 1;
				}
			});
			if(this.max_local_finition > this.max_finition) {
				this.max_finition = this.max_local_finition;
				this.finitionObj = node.toString();
			}
		});*/
	}

	
	public String defineRandomObjective(String myPosition, String nextNode) {
		this.recolte = false;
		if(this.finitionObj == null || myPosition.equals(this.finitionObj)) {
			this.defineFinitionObjective();
		}
		
		List<String> nextMove = this.myMap.getShortestPath(myPosition, this.finitionObj);
		int cnt = 0;
		if(!myPosition.equals(this.finitionObj) && nextMove.size() > 0) {
			nextNode = nextMove.get(0);
			while(this.placeWantToGo.equals(nextNode) && cnt < 10) {
				do {
					this.defineFinitionObjective();
					nextMove = this.myMap.getShortestPath(myPosition, this.finitionObj);
					cnt++;
				}while(nextMove.size() < 1);
				nextNode = nextMove.get(0);
			}
		}else {
			while(myPosition.equals(this.finitionObj) && cnt < 10) {
				do {
					this.defineFinitionObjective();
					nextMove = this.myMap.getShortestPath(myPosition, this.finitionObj);
					cnt++;
				}while(nextMove.size() < 1);
				nextNode = nextMove.get(0);
			}
		}
		return nextNode;
	}
	

	public static <T> List<Collection<T>> product(Collection<T> a, int r) {
        List<Collection<T>> result = Collections.nCopies(1, Collections.emptyList());
        for (Collection<T> pool : Collections.nCopies(r, new LinkedHashSet<>(a))) {
            List<Collection<T>> temp = new ArrayList<>();
            for (Collection<T> x : result) {
                for (T y : pool) {
                    Collection<T> z = new ArrayList<>(x);
                    z.add(y);
                    temp.add(z);
                }
            }
            result = temp;
        }
        return result;
    }
	
	private int max_score = 0;
	private Collection<String> best_comb;
	private int count = 0;
	
	public void defineBestComposition() {
		
		product(List.of("g", "d"), this.agentsCapacity.size()).forEach(compo ->{
			//System.out.println(compo);
			int curr_d = 0;
			int curr_g = 0;
			int curr_max = 0;
			int i = 0;
			Object[] capaKeys = this.agentsCapacity.keySet().toArray();
			for(String treasureType : compo) {
				Object currentKey = capaKeys[i];
				if(treasureType.equals("g")) { // gold case
					curr_g += this.agentsCapacity.get(currentKey)[0];
				}else { // diamond case
					curr_d += this.agentsCapacity.get(currentKey)[1];
				}
				i++;
				curr_max = Math.min(curr_d, this.qteDiam) + Math.min(curr_g, this.qteGold);
		        if(curr_max > max_score) {
		            this.max_score = curr_max;
		            this.best_comb = compo;
		        }
			}
		});
		//System.out.println(this.best_comb);
		//System.out.println("max score : " + this.max_score + " | qt diam : " + this.qteDiam + " | qt gold : " + this.qteGold);
		
		this.PersoGold = new HashMap<AID,Couple>();
		this.PersoDiam = new HashMap<AID,Couple>();
		this.count = 0;
		this.best_comb.forEach(treasureType ->{
			AID currentKey = (AID) this.agentsCapacity.keySet().toArray()[this.count];
			if(treasureType.equals("g")) {
				//System.out.println(currentKey.getLocalName() + " : " + treasureType + " | qt : " + this.agentsCapacity.get(currentKey)[0]);
				this.PersoGold.put(currentKey, new Couple<Long,Integer>(System.currentTimeMillis(), this.agentsCapacity.get(currentKey)[0]));
			}else {
				//System.out.println(currentKey.getLocalName() + " : " + treasureType + " | qt : " + this.agentsCapacity.get(currentKey)[1]);
				this.PersoDiam.put(currentKey, new Couple<Long,Integer>(System.currentTimeMillis(), this.agentsCapacity.get(currentKey)[1]));
			}
			this.count++;
		});
		
	}
	
	
	public String defineRandomObj(String nextNode) {
		String tempObj = this.placeWantToGo;
		List<String> nextMove = null;
		String nextNodecpy = nextNode;
		int cnt = 0;
		do {
			do {
				// Node random
				try {
					int nb = (int) this.myMap.getGraph().nodes().count();
					int i = (int) (Math.random() * (nb-1)) ;
					tempObj = this.myMap.getGraph().getNode(i).getId();
					String myPosition=((AbstractDedaleAgent)this).getCurrentPosition();
					nextMove = this.myMap.getShortestPath(myPosition, tempObj);
					cnt++;
				}catch(Exception e) {
					continue;
				}
			}while(((nextMove == null) || (nextMove.size() < 1)));
			nextNodecpy = nextMove.get(0);
		}while(this.placeWantToGo.equals(nextNodecpy) && cnt < 10);
		return tempObj;
	}
	
	
	public void randomMove(int time, String nextNode) {
		
		String tempObj = this.defineRandomObj(nextNode);
		String myPosition=((AbstractDedaleAgent)this).getCurrentPosition();
		String nextNodecpy;
		
		for(int i = 0; i < time; i++) {
			nextNodecpy = this.myMap.getShortestPath(myPosition, tempObj).get(0);
			this.placeWantToGo=nextNodecpy;
			try {
				this.doWait(WAITINGTIME);
			} catch (Exception e) {
				e.printStackTrace();
			}
			((AbstractDedaleAgent)this).moveTo(nextNodecpy);
			myPosition=((AbstractDedaleAgent)this).getCurrentPosition();
			if(this.myMap.getShortestPath(myPosition, tempObj).size() == 0 || !myPosition.equals(nextNodecpy)) {
				tempObj = this.defineRandomObj(nextNodecpy);
			}
		}
	}
	
	public void printScore() {
		System.out.println("Score agent \"" + this.getLocalName() + "\" : " + (this.backpackDiam - this.freeSpaceDiamPerso) + " Diamond(s)" );
		System.out.println("Score agent \"" + this.getLocalName() + "\" : " + (this.backpackGold - this.freeSpaceGoldPerso) + " Gold(s)" );
	}
	

	public boolean checkObjectiveHarvest() {
		if(this.harvestObj == null) {
			return false;
		}
		if(this.expertise.equals(Observation.GOLD)) {
			for(String check : this.locationDiam.keySet()) {
				if(this.harvestObj.equals(check)) {
					return true;
				}
			}
			return false;
		}else {
			for(String check : this.locationGold.keySet()) {
				if(this.harvestObj.equals(check)) {
					return true;
				}
			}
			return false;
		}
	}
	
}
