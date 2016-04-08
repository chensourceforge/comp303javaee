/**
 * Student Name - Student ID
 */
package comp303project;

public class LeaderBoardEntry {

	private String username;
	private int totalScore;
	private int wins;
	private int losses;
	private int rank;
	
	public LeaderBoardEntry(String username, int totalScore, int wins, int losses) {
		this.setUsername(username);
		this.setTotalScore(totalScore);
		this.setWins(wins);
		this.setLosses(losses);
		setRank(0);
	}
	
	public LeaderBoardEntry(){}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getTotalScore() {
		return totalScore;
	}

	public void setTotalScore(int totalScore) {
		this.totalScore = totalScore;
	}

	public int getWins() {
		return wins;
	}

	public void setWins(int wins) {
		this.wins = wins;
	}

	public int getLosses() {
		return losses;
	}

	public void setLosses(int losses) {
		this.losses = losses;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

}
