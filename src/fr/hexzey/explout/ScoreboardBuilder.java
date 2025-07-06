package fr.hexzey.explout;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class ScoreboardBuilder
{
	// attributs
	
	private String objectiveName;
	private String criteria;
	private String displayName;
	private ArrayList<String> lines;
	
	// constructeurs
	
	public ScoreboardBuilder(String _objectiveName, String _criteria, String _displayName)
	{
		this.objectiveName = _objectiveName;
		this.criteria = _criteria;
		this.displayName = _displayName;
		
		this.lines = new ArrayList<String>();
	}
	
	// méthodes
	
	public void AddLine(String lineContent)
	{
		this.lines.add(lineContent);
	}
	
	public Scoreboard build()
	{
		ScoreboardManager manager;
		Scoreboard scoreboard;
		
		manager = Bukkit.getScoreboardManager();
		scoreboard = manager.getNewScoreboard();
		
		Objective objective = scoreboard.registerNewObjective(this.objectiveName, this.criteria, this.displayName);
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		int i = this.lines.size();
		for(String line : this.lines)
		{
			Score score = objective.getScore(line);
			score.setScore(i);
			i--;
		}
		
		return scoreboard;
	}
}
