/**
 * Student Name - Student ID
 */
package comp303project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.validation.constraints.Size;
import javax.websocket.server.ServerEndpointConfig;


//@ConversationScoped
//@SessionScoped
@Named("player")
@SessionScoped
public class PlayerBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	
	@Size(min=3, max=16, message="username length: 3-16")
	private String username;
	
	@Size(min=3, max=16, message="password length: 3-16")
	private String password;
	
	private String loginError;
	//private Gogame playerStats;
	private boolean isLoggedIn = false;
	//private int rank;
	
	//@Inject
	DAOBean daoBean;
	
	public List<LeaderBoardEntry> getLeaderBoard(){
		List<LeaderBoardEntry> topPlayers = daoBean.getTopPlayers(10);
		int len = topPlayers.size();
		if(len > 0){
			int prevScore = topPlayers.get(0).getTotalScore();
			int prevRank = 1;
			topPlayers.get(0).setRank(prevRank);
			for(int i = 1; i < len; i++){
				int score = topPlayers.get(i).getTotalScore();
				if(prevScore == score){
					topPlayers.get(i).setRank(prevRank);
				} else {
					prevRank = i + 1;
					prevScore = score;
					topPlayers.get(i).setRank(prevRank);
				}
			}
		}
		if(topPlayers == null){topPlayers = new ArrayList<LeaderBoardEntry>();}
		return topPlayers;
	}
	
	public void login(){
		User user = daoBean.findUser(username);
		if(user != null && !password.equals(user.getPassword())){
			loginError = "Invalid password";
			return;
		}
		
		if(user == null){
			// create username/password
			user = new User(username, password);
			daoBean.insertUser(user);
			
			// create user stats
			//playerStats = new Gogame(username);
			daoBean.insertGogame(new Gogame(username));
		} 
//		else {
//			// login success
//			//playerStats = daoBean.findGogame(username);
//		}
		
		isLoggedIn = true;
	}
	
	public String logout(){

		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/home.xhtml?faces-redirect=true";

	}

	public String playGame(){
		if(isLoggedIn){
			return "go";
		} else {
			return "";
		}
	}

	
	
	public PlayerBean() {daoBean = new DAOBean();}
	

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getLoginError() {
		return loginError;
	}
	public void setLoginError(String loginError) {
		this.loginError = loginError;
	}
	
//	public Gogame getPlayerStats(){return playerStats;}
//	public void setPlayerStats(Gogame playerStats){this.playerStats = playerStats;}

	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	public void setLoggedIn(boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
	}

	public int getRank() {
		if(isLoggedIn){
			return daoBean.getRanking(daoBean.findGogame(username));
		} else {
			return 0;
		}
//		if(playerStats != null){
//			return daoBean.getRanking(playerStats);
//		} else {
//			return 0;
//		}
	}

	public void setRank(int rank) {
		//this.rank = rank;
	}
	
	public int getTotalScore(){
		if(isLoggedIn){
			return daoBean.findGogame(username).getTotalScore();
		} else {
			return 0;
		}
	}
	
	public int getWins(){
		if(isLoggedIn){
			return daoBean.findGogame(username).getWins();
		} else {
			return 0;
		}
	}
	
	public int getLosses(){
		if(isLoggedIn){
			return daoBean.findGogame(username).getLosses();
		} else {
			return 0;
		}
	}
}

