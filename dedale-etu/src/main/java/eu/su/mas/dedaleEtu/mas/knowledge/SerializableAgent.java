package eu.su.mas.dedaleEtu.mas.knowledge;

import eu.su.mas.dedale.env.Observation;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import dataStructures.serializableGraph.*;
import dataStructures.tuple.Couple;
import eu.su.mas.dedaleEtu.mas.agents.dummies.AgentOptimized;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.Agent;
import javafx.application.Platform;

/**
 * 
 * @author tc
 */
public class SerializableAgent implements Serializable {

	private static final long serialVersionUID = 2162879882687321972L;




	/*********************************
	 * Parameters for an agent
	 ********************************/
	private HashMap<AID, Integer> persoGold,persoDiam;

	private SerializableSimpleGraph<String, MapAttribute> sg;

	private HashMap<String, Couple> locationGold;

	private HashMap<String, Couple> locationDiam;

	private Observation pref;

	
	public SerializableAgent(AgentOptimized a,SerializableSimpleGraph<String, MapAttribute> sg) {
		this.sg = sg;
		this.locationGold=a.locationGold;
		this.locationDiam=a.locationDiam;
		this.persoGold=a.PersoGold;
		this.persoDiam=a.PersoDiam;
	}


	public SerializableSimpleGraph<String, MapAttribute> getSg() {
		return sg;
	}
	
	public HashMap<String,Couple> getLocationGold() {
		return locationGold;
		
	}
	public HashMap<String,Couple> getLocationDiam() {
		return locationDiam;
		
	}

	public HashMap<AID, Integer> getPersoGold(){
		return persoGold;
	}
	public HashMap<AID, Integer> getPersoDiam(){
		return persoDiam;
	}


}