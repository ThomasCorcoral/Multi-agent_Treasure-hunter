package eu.su.mas.dedaleEtu.mas.knowledge;

import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedaleEtu.mas.knowledge.AgentRepresentation.Ressource;
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
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
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
	
	private Observation ressourceType;
	private Integer capacity;
	private SerializableSimpleGraph<String, MapAttribute> sg;

	private HashMap<String, Couple> locationGold;

	private HashMap<String, Couple> locationDiam;

	private Observation pref;

	
	public SerializableAgent(AgentRepresentation a) {
		this.ressourceType = a.getRessourceType();
		this.sg = a.updateSendMap(a.getAgentMap().getSerializableGraph());
		this.locationGold=a.getLocationGold();
		this.locationDiam=a.getLocationDiam();
		this.persoGold=a.getPersoGold();
		this.persoDiam=a.getPersoDiam();
	}
	
	


	public Observation getRessourceType() {
		return ressourceType;
	}


	public Integer getCapacity() {
		return capacity;
	}


	public void setCapacity(Integer capacity) {
		this.capacity = capacity;
	}


	public SerializableSimpleGraph<String, MapAttribute> getSg() {
		return sg;
	}


	public void setSg(SerializableSimpleGraph<String, MapAttribute> sg) {
		this.sg = sg;
	}

}