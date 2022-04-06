package eu.su.mas.dedaleEtu.mas.agents.dummies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;

public class AgentOptimized extends AbstractDedaleAgent {
	private static final long serialVersionUID = -7969468610241668140L;
	private List<String> list_agentNames;
	public MapRepresentation myMap;
	public SerializableSimpleGraph<String, MapAttribute> MapReceived=null;
	public HashMap<String,Integer> noGo = new HashMap<String,Integer>(); 
	public String placeWantToGo=null;
	public AID senderPing ;
	public AID otherAgent ;
	//locationGold contient la liste des noeuds contenant de l'or associé au couple l'heure où il a été découvert et la quantité d'or que le noeud contient
	public HashMap<String,Couple>locationGold=new HashMap<String,Couple>();
	//locationGold contient la liste des noeuds contenant de diamant associé au couple l'heure où il a été découvert et la quantité d'or que le noeud contient
	public HashMap<String,Couple>locationDiam=new HashMap<String,Couple>();
	public int goldQte,diamQte,nbAgentGold,nbAgentDiam;
	public Observation expertise=Observation.GOLD; //par défaut tout les agents choisissent de se pécialiser dans l'or
	public boolean gotPing=false;
	public Map<AID,ArrayList> dico=new HashMap<AID,ArrayList>();
	
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
	public ArrayList<String> PersoGold = new ArrayList<String>();
	public ArrayList<String> PersoDiam = new ArrayList<String>();
	
	
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
		fsm. registerDefaultTransition (Exploration,CheckMailBox);//Default
		fsm. registerTransition (CheckMailBox,SendPing,0);
		fsm. registerDefaultTransition (SendPing,Exploration);
		fsm. registerTransition (CheckMailBox,SendMapPing,2);
		fsm. registerTransition (CheckMailBox,SendMapMap,1);
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
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");

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
}
