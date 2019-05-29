package fr.black_eyes.guildObjectives;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Material;

public class Objective {
	
	//temps pour le finir, prix, récompense
	public Integer joursFin;
	public Integer cout;
	public Integer recompense;
	//temps de pénalité si non finis et cout
	public Integer joursPenalite;
	public Integer penalite;
	
	//liste des matériaux et de leur nombre
	public HashMap<Material, Integer> materiaux = new HashMap<Material, Integer>();
	public ArrayList<String> commandes = new ArrayList<String>();
	
}
