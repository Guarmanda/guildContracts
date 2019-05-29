package fr.black_eyes.guildObjectives;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Material;

public class Objective {
	
	//temps pour le finir, prix, r�compense
	public Integer joursFin;
	public Integer cout;
	public Integer recompense;
	//temps de p�nalit� si non finis et cout
	public Integer joursPenalite;
	public Integer penalite;
	
	//liste des mat�riaux et de leur nombre
	public HashMap<Material, Integer> materiaux = new HashMap<Material, Integer>();
	public ArrayList<String> commandes = new ArrayList<String>();
	
}
