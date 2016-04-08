/**
 * Student Name - Student ID
 */
package comp303project;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class GameController {
	EndpointServer endpoint1, endpoint2;
	final int grids;
	final String p1Name, p2Name;
	
	boolean isP1Black = false;
	private int[][] board;
	boolean isCurrentTurnP1;
	int p1Points, p2Points;
	boolean isRematchRequestedP1, isRematchRequestedP2;
	
//	@EJB 
//	DAOBean daoBean;
	
	class Occupancy{
		public static final int 
			EMPTY = 0, 
			OCCUPIED_BLACK = 1, 
			OCCUPIED_WHITE = 2, 
			CAPTURED_BLACK = 3, 
			CAPTURED_WHITE = 4;
	}
	
	class Point {
		public final int col, row;
		public Point(int col, int row){
			this.col = col;
			this.row = row;
		}
		@Override
		public boolean equals(Object obj){
			if(obj == null || !(obj instanceof Point)){return false;}
			Point p = (Point)obj;
			return p.col == this.col && p.row == this.row;
		}
		@Override
		public int hashCode(){
			return row * grids + col;
		}
		public Point toNorth(){
			return row == 0 ? null : new Point(col, row - 1);
		}
		public Point toSouth(){
			return row == grids-1 ? null : new Point(col, row + 1);
		}
		public Point toEast(){
			return col == grids-1 ? null : new Point(col + 1, row);
		}
		public Point toWest(){
			return col == 0 ? null : new Point(col - 1, row);
		}
	}
	
	public GameController(int g, EndpointServer e1, EndpointServer e2) {
		grids = g;
		endpoint1 = e1;
		endpoint2 = e2;
		
		endpoint1.setController(this);
		endpoint2.setController(this);
		endpoint1.setLookingForOpponent(false);
		endpoint2.setLookingForOpponent(false);
		p1Name = endpoint1.getName();
		p2Name = endpoint2.getName();
		
		newGame();
		
		endpoint1.sendOpponentFound(p2Name, isP1Black);
		endpoint2.sendOpponentFound(p1Name, !isP1Black);
		
	}
	
	private void newGame(){
		board = new int[grids][grids];
		for(int i = 0; i < grids; i++){
			for(int j = 0; j < grids; j++){
				board[i][j] = Occupancy.EMPTY;
			}
		}
		p1Points = 0;
		p2Points = 0;
		isP1Black = !isP1Black;
		
		isRematchRequestedP1 = false;
		isRematchRequestedP2 = false;
	}
	
	public void rematchRequested(EndpointServer ep){
		if(ep == endpoint1){
			isRematchRequestedP1 = true;
		} else {
			isRematchRequestedP2 = true;
		}
		
		if(isRematchRequestedP1 && isRematchRequestedP2){
			newGame();
			endpoint1.sendRematchReady(isP1Black);
			endpoint2.sendRematchReady(!isP1Black);
		} else if(isRematchRequestedP1){
			endpoint1.sendRematchWaitOpponent();
			endpoint2.sendOpponentWantRematch();
		} else{
			endpoint2.sendRematchWaitOpponent();
			endpoint1.sendOpponentWantRematch();
		}
	}
	
	public void beginGame(){
		isCurrentTurnP1 = isP1Black;
		endpoint1.setPlayerTurn(isCurrentTurnP1);
		endpoint2.setPlayerTurn(!isCurrentTurnP1);
	}
	
	public void playerMove(JsonNumber x, JsonNumber y){
		if(isCurrentTurnP1){
			endpoint2.sendOpponentMove(x, y);
		} else {
			endpoint1.sendOpponentMove(x, y);
		}
	}
	
	private boolean isEndGame(){
		if((p1Points + p2Points) < (grids * grids)){
			return false;
		}
		
		// game complete
		boolean isWinnerP1;
		if(p1Points > p2Points){
			//p1 wins
			endpoint1.sendGameWin();
			endpoint2.sendGameLose();
			isWinnerP1 = true;
		} else if(p1Points < p2Points) {
			// p2 wins
			endpoint2.sendGameWin();
			endpoint1.sendGameLose();
			isWinnerP1 = false;
		} else {
			// white wins
			if(isP1Black){
				endpoint2.sendGameWin();
				endpoint1.sendGameLose();
				isWinnerP1 = false;
			} else {
				endpoint1.sendGameWin();
				endpoint2.sendGameLose();
				isWinnerP1 = true;
			}
		}
		// store score to database
//		EntityManagerFactory emf = Persistence.createEntityManagerFactory("GoGame");
//		EntityManager em = emf.createEntityManager();
//		em.setProperty("javax.persistence.cache.storeMode", "REFRESH");
//		em.getTransaction().begin();
		
		DAOBean daoBean = new DAOBean();
		
		Gogame p1GoGame = daoBean.findGogame(p1Name);// em.find(Gogame.class, p1Name);
		Gogame p2GoGame = daoBean.findGogame(p2Name);// em.find(Gogame.class, p2Name);
		
		if(isWinnerP1){
			p1GoGame.setWins(p1GoGame.getWins() + 1);
			p2GoGame.setLosses(p2GoGame.getLosses() + 1);
			p1GoGame.setTotalScore(p1GoGame.getTotalScore() + grids);
		} else {
			p2GoGame.setWins(p2GoGame.getWins() + 1);
			p1GoGame.setLosses(p1GoGame.getLosses() + 1);
			p2GoGame.setTotalScore(p2GoGame.getTotalScore() + grids);
		}
		
		daoBean.updateGogame(p1GoGame);
		daoBean.updateGogame(p2GoGame);
//		em.persist(p1GoGame);
//		em.persist(p2GoGame);
		
		
//		em.getTransaction().commit();
//		em.refresh(p1GoGame);
//		em.refresh(p2GoGame);
//		em.close();
//		emf.close();
		
		return true;
	}
	
	public void playerDisconnect(EndpointServer ep){
		if(ep.equals(endpoint1)){
			if(endpoint2 != null){endpoint2.sendOpponentDisconnect();}
			endpoint1 = null;
			endpoint2 = null;
			
			// store player stats to database
		} else {
			if(endpoint1 != null){endpoint1.sendOpponentDisconnect();}
			endpoint1 = null;
			endpoint2 = null;
			
			// store player stats to database
		}
	}
	
	private void turnComplete(){
		if(!isEndGame()){
			isCurrentTurnP1 = !isCurrentTurnP1;
			endpoint1.setPlayerTurn(isCurrentTurnP1);
			endpoint2.setPlayerTurn(!isCurrentTurnP1);
		}
	}
	
	private boolean isTrapped(Point pos, int wall){
		int row = pos.row, col = pos.col;
		
		if(row == 0 || col == 0 || row == grids-1 || col == grids-1){return false;}
		
		int i;
		// up
		i = row-1;
		while(i>=0 && board[i][col] != wall){
			i--;
		}
		if(i == -1){return false;}
		
		// down
		i = row+1;
		while(i<grids && board[i][col] != wall){
			i++;
		}
		if(i == grids){return false;}
		
		// left
		i = col-1;
		while(i>=0 && board[row][i] != wall){
			i--;
		}
		if(i == -1){return false;}

		// right
		i = col+1;
		while(i<grids && board[row][i] != wall){
			i++;
		}
		if(i == grids){return false;}
		
		return true;
	}
	
	boolean findFreePoints(Point prevPos, Point currPos, int wall, Stack<Point> group){
		group.push(currPos);
		boolean free = !isTrapped(currPos, wall);
		{
			Point point = currPos.toNorth();
			if(point != null && 
					!point.equals(prevPos) && 
					board[point.row][point.col] != wall &&
					!group.contains(point)){
				
				free = findFreePoints(currPos, point, wall, group) || free;
			}
		}
		{
			Point point = currPos.toSouth();
			if(point != null && 
					!point.equals(prevPos) && 
					board[point.row][point.col] != wall &&
					!group.contains(point)){
				
				free = findFreePoints(currPos, point, wall, group) || free;
			}
		}
		{
			Point point = currPos.toEast();
			if(point != null && 
					!point.equals(prevPos) && 
					board[point.row][point.col] != wall &&
					!group.contains(point)){
				
				free = findFreePoints(currPos, point, wall, group) || free;
			}
		}
		{
			Point point = currPos.toWest();
			if(point != null && 
					!point.equals(prevPos) && 
					board[point.row][point.col] != wall &&
					!group.contains(point)){
				
				free = findFreePoints(currPos, point, wall, group) || free;
			}
		}
		
		return free;
	}
	
	List<Integer> captureAndRemove(){
		// magic numbers for temp array
		final int WALL = 1, // player's stone
				FREE = 2, // point NOT enclosed by player's stones
				TRAPPED = 3; // point trapped by player's stones
		
		int wall = (isCurrentTurnP1 == isP1Black) ? Occupancy.OCCUPIED_BLACK : Occupancy.OCCUPIED_WHITE;
		
		int[][] b = new int[grids][grids];
		Stack<Point> stack = new Stack<Point>();
		for(int i=0; i<grids; i++){
			for(int j=0; j<grids; j++){
				if(b[i][j] != 0){continue;}
				if(board[i][j] == wall){
					b[i][j] = WALL;
				} else {
					boolean isFree = findFreePoints(null,new Point(j,i),wall,stack);
					if(isFree){
						while(!stack.empty()){
							Point p = stack.pop();
							b[p.row][p.col] = FREE;
						}
					} else {
						while(!stack.empty()){
							Point p = stack.pop();
							b[p.row][p.col] = TRAPPED;
						}
					}
				}
			}
		}
		
		List<Integer> remove = new ArrayList<Integer>();
		
		for(int i = 0; i<grids; i++){
			for(int j = 0; j<grids; j++){
				if(b[i][j] == TRAPPED){
					if(isCurrentTurnP1 == isP1Black){ // black
						switch(board[i][j]){ // FALL-THROUGH
							case Occupancy.OCCUPIED_WHITE:
								remove.add(j); remove.add(i);
							case Occupancy.CAPTURED_WHITE:
								if(isCurrentTurnP1){p2Points--;} 
								else{p1Points--;}
							case Occupancy.EMPTY:
								board[i][j] = Occupancy.CAPTURED_BLACK;
								if(isCurrentTurnP1){p1Points++;} else{p2Points++;}
								break;
						}
					} else { // white
						switch(board[i][j]){ // FALL-THROUGH
							case Occupancy.OCCUPIED_BLACK:
								remove.add(j); remove.add(i);
							case Occupancy.CAPTURED_BLACK:
								if(isCurrentTurnP1){p2Points--;} 
								else{p1Points--;}
							case Occupancy.EMPTY:
								board[i][j] = Occupancy.CAPTURED_WHITE;
								if(isCurrentTurnP1){p1Points++;} else{p2Points++;}
								break;
						}
					}
				}
				
			}
		}
		return remove;
	}
	
	
	void findEnclosure(Point prevPos, Point currPos, int occupancy, Stack<Point> chain){
		// algorithm NOT finished...
		
		if(chain.contains(currPos)){
			// closure?
			return;
		} 
		if(board[currPos.row][currPos.col] == occupancy){
			chain.push(currPos);
			
			// UP
			Point north = currPos.toNorth();
			if(north != null && !north.equals(prevPos)){
				findEnclosure(currPos, north, occupancy, chain);
			}
			// RIGHT
			Point east = currPos.toEast();
			if(east != null && !east.equals(prevPos)){
				findEnclosure(currPos, east, occupancy, chain);
			}
			// DOWN
			Point south = currPos.toSouth();
			if(south != null && !south.equals(prevPos)){
				findEnclosure(currPos, south, occupancy, chain);
			}
			// LEFT
			Point west = currPos.toWest();
			if(west != null && !west.equals(prevPos)){
				findEnclosure(currPos, west, occupancy, chain);
			}
			
			chain.pop();
		}
		
	}
	
	
	
	public void playerClick(int row, int col){
		switch(board[row][col]){
			case Occupancy.EMPTY:
				// valid set
				//Stack<Point> connectedPoints = new Stack<Point>();
				//Point startPos = new Point(col, row);
				
				if(isCurrentTurnP1){
					p1Points = p1Points + 1;
					if(isP1Black){
						// black, p1
						board[row][col] = Occupancy.OCCUPIED_BLACK;
						//findEnclosure(null, startPos, Occupancy.OCCUPIED_BLACK, connectedPoints);
						
					} else {
						// white, p1
						board[row][col] = Occupancy.OCCUPIED_WHITE;
						//findEnclosure(null, startPos, Occupancy.OCCUPIED_WHITE, connectedPoints);
					}
					
					List<Integer> remove = captureAndRemove();
					JsonArrayBuilder p1Remove = Json.createArrayBuilder();
					JsonArrayBuilder p2Remove = Json.createArrayBuilder();
					for(Integer i : remove){p1Remove.add(i);p2Remove.add(i);}
					
					endpoint1.setYourStone(Json.createObjectBuilder().
							add("col", col).add("row", row).
							add("yourPoints", p1Points).add("opponentPoints", p2Points).
							add("remove", p1Remove));
					endpoint2.setOpponentStone(Json.createObjectBuilder().
							add("col", col).add("row", row).
							add("yourPoints", p2Points).add("opponentPoints", p1Points).
							add("remove", p2Remove));
					
				} else {
					p2Points = p2Points + 1;
					if(isP1Black){
						// white, p2
						board[row][col] = Occupancy.OCCUPIED_WHITE;
						//findEnclosure(null, startPos, Occupancy.OCCUPIED_WHITE, connectedPoints);
					} else {
						// black p2
						board[row][col] = Occupancy.OCCUPIED_BLACK;
						//findEnclosure(null, startPos, Occupancy.OCCUPIED_BLACK, connectedPoints);
					}
					
					List<Integer> remove = captureAndRemove();
					JsonArrayBuilder p1Remove = Json.createArrayBuilder();
					JsonArrayBuilder p2Remove = Json.createArrayBuilder();
					for(Integer i : remove){p1Remove.add(i);p2Remove.add(i);}
					
					endpoint2.setYourStone(Json.createObjectBuilder().
							add("col", col).add("row", row).
							add("yourPoints", p2Points).add("opponentPoints", p1Points).
							add("remove", p2Remove));
					
					endpoint1.setOpponentStone(Json.createObjectBuilder().
							add("col", col).add("row", row).
							add("yourPoints", p1Points).add("opponentPoints", p2Points).
							add("remove", p1Remove));
				}
				turnComplete();
				break;
			case Occupancy.OCCUPIED_BLACK:
			case Occupancy.OCCUPIED_WHITE:
				// invalid set
				if(isCurrentTurnP1){endpoint1.clickNotValid();}
				else{endpoint2.clickNotValid();}
				break;
			case Occupancy.CAPTURED_BLACK:
				if(this.isP1Black == this.isCurrentTurnP1){
					//valid, but 0 pts
					board[row][col] = Occupancy.OCCUPIED_BLACK;
					if(isCurrentTurnP1){
						endpoint1.setYourStone(Json.createObjectBuilder().
								add("col", col).add("row", row).
								add("yourPoints", p1Points).add("opponentPoints", p2Points).
								addNull("remove"));
						endpoint2.setOpponentStone(Json.createObjectBuilder().
								add("col", col).add("row", row).
								add("yourPoints", p2Points).add("opponentPoints", p1Points).
								addNull("remove"));
					} else{
						endpoint2.setYourStone(Json.createObjectBuilder().
								add("col", col).add("row", row).
								add("yourPoints", p2Points).add("opponentPoints", p1Points).
								addNull("remove"));
						endpoint1.setOpponentStone(Json.createObjectBuilder().
								add("col", col).add("row", row).
								add("yourPoints", p1Points).add("opponentPoints", p2Points).
								addNull("remove"));
					}
					
					turnComplete();
				} else {
					// invalid set
					if(isCurrentTurnP1){endpoint1.clickNotValid();}
					else{endpoint2.clickNotValid();}
				}
				break;
			case Occupancy.CAPTURED_WHITE:
				if(this.isP1Black != this.isCurrentTurnP1){
					//valid, but 0 pts
					board[row][col] = Occupancy.OCCUPIED_WHITE;
					if(isCurrentTurnP1){
						endpoint1.setYourStone(Json.createObjectBuilder().
								add("col", col).add("row", row).
								add("yourPoints", p1Points).add("opponentPoints", p2Points).
								addNull("remove"));
						endpoint2.setOpponentStone(Json.createObjectBuilder().
								add("col", col).add("row", row).
								add("yourPoints", p2Points).add("opponentPoints", p1Points).
								addNull("remove"));
					} else{
						endpoint2.setYourStone(Json.createObjectBuilder().
								add("col", col).add("row", row).
								add("yourPoints", p2Points).add("opponentPoints", p1Points).
								addNull("remove"));
						endpoint1.setOpponentStone(Json.createObjectBuilder().
								add("col", col).add("row", row).
								add("yourPoints", p1Points).add("opponentPoints", p2Points).
								addNull("remove"));
					}
					
					turnComplete();
				} else {
					// invalid set
					if(isCurrentTurnP1){endpoint1.clickNotValid();}
					else{endpoint2.clickNotValid();}
				}
				break;
		}
	}

}
