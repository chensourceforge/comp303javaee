package comp303project;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the gogame database table.
 * 
 */
@Entity
@NamedQuery(name="getRanking", query="select COUNT(g) from Gogame g where g.totalScore > :myScore")
public class Gogame implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String username;

	@Column(name="total_score")
	private int totalScore;

	private int wins;
	private int losses;

	//bi-directional one-to-one association to User
	@OneToOne
	@PrimaryKeyJoinColumn(name="username", referencedColumnName="username")
	private User user;

	public Gogame() {}
	
	public Gogame(String username){
		this.username = username;
		this.totalScore = 0;
		this.wins = 0;
		this.losses = 0;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getLosses() {
		return this.losses;
	}

	public void setLosses(int losses) {
		this.losses = losses;
	}

	public int getTotalScore() {
		return this.totalScore;
	}

	public void setTotalScore(int totalScore) {
		this.totalScore = totalScore;
	}

	public int getWins() {
		return this.wins;
	}

	public void setWins(int wins) {
		this.wins = wins;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}