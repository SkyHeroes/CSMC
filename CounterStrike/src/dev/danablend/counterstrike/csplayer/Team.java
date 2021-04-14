package dev.danablend.counterstrike.csplayer;

import java.util.Collection;

public class Team {
	
	private TeamEnum team;
	private Collection<CSPlayer> csplayers;
	private int wins;
	private int losses;
	
	public Team(TeamEnum team, Collection<CSPlayer> csplayers) {
		this.team = team;
		this.csplayers = csplayers;
		this.wins = 0;
		this.losses = 0;
	}
	
	public int getWins() {
		return wins;
	}
	
	public void setWins(int wins) {
		this.wins = wins;
	}
	
	public void addVictory() {
		wins += 1;
	}
	
	public int getLosses() {
		return losses;
	}
	
	public void setLosses(int losses) {
		this.losses = losses;
	}
	
	public void addLoss() {
		losses += 1;
	}
	
	public Collection<CSPlayer> getCsPlayers() {
		return csplayers;
	}
	
	public TeamEnum getTeam() {
		return team;
	}

}
