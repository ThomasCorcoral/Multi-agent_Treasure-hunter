package eu.su.mas.dedaleEtu.mas.agents.dummies;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.ExploSoloBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;

public class AgentCommunicationOptimized extends Agent {
	private MapRepresentation myMap=new MapRepresentation();
	private static final String Exploration = "Exploration";
	private static final String SendPing = "SendPing";
	private static final String CheckMailBox = "CheckMailBox";
	private static final String SendMapPing = "SendMapPing";
	private static final String SendMapMap = "SendMapMap";
	private static final String UpdateOwnMap = "UpdateOwnMap";
	private static final String CheckMapReception = "CheckMapReception";
	private static final String WaitACK = "WaitACK";
	private static final String SendACK = "SendACK";
	private static final String UpdateOtherAgentData = "UpdateOtherAgentData";
	
	
	protected void setup() {
		FSMBehaviour fsm = new FSMBehaviour(this);
		// Define the different states and behaviours
		fsm. registerFirstState (new Exploration(), Exploration);
		fsm. registerState (new SendPing(), SendPing);
		fsm. registerState (new SendMapPing(), SendMapPing);
		fsm. registerState (new SendMapMap(), SendMapMap);
		fsm. registerState (new CheckMailBox(), CheckMailBox);
		fsm. registerState (new UpdateOwnMap(), UpdateOwnMap);
		fsm. registerState (new CheckMapReception(), CheckMapReception);
		fsm. registerState (new WaitACK(), WaitACK);
		fsm. registerState (new UpdateOtherAgentData(), UpdateOtherAgentData);
		fsm. registerState (new SendACK(), SendACK);
		
		// Register the transitions
		fsm. registerDefaultTransition (Exploration,CheckMailBox);//Default
		fsm. registerTransition (CheckMailBox,SendPing,1);
		fsm. registerTransition (CheckMailBox,SendMapPing,2);
		fsm. registerTransition (CheckMailBox,SendMapMap,3);
		fsm. registerDefaultTransition (SendMapMap,UpdateOwnMap);
		fsm. registerDefaultTransition (UpdateOwnMap,WaitACK);
		fsm. registerTransition (WaitACK,Exploration, 1) ;
		fsm. registerTransition (WaitACK,UpdateOtherAgentData, 2) ;
		fsm. registerDefaultTransition(SendMapPing,CheckMapReception) ;
		fsm. registerTransition (CheckMapReception,Exploration, 1) ;
		fsm. registerTransition (CheckMapReception,UpdateOwnMap, 2) ;
		fsm. registerDefaultTransition(UpdateOwnMap,SendACK) ;
		fsm. registerDefaultTransition(SendACK,UpdateOtherAgentData) ;
		fsm. registerDefaultTransition(UpdateOtherAgentData,Exploration) ;
		addBehaviour(fsm);

	}
}
