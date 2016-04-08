/**
 * Student Name - Student ID
 */
package comp303project;

import java.io.IOException;
import java.io.StringReader;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value="/game")
public class EndpointServer {

	Basic playerBasicRemote;
	int grids = 0;
	String playerName;
	private boolean lookingForOpponent = false;
	GameController controller = null;
	Session playerSession;
	
	@Inject
	QueueBean queue;
	/**
	 * messages - action:
	 */
	static final int standby = 1,
	    find_opponent = 2,
	    opponent_found = 3,
	    begin_game = 4,
	    your_turn = 5,
	    opponent_turn = 6,
	    my_move = 7,
	    opponent_move = 8,
	    my_click = 9,
	    invalid_click = 10,
	    set_your_stone = 11,
	    set_opponent_stone = 12,
	    my_stats = 13,
	    you_win = 14,
	    opponent_win = 15,
	    opponent_disconnect = 16,
	    rematch_request = 17,
	    rematch_wait_opponent = 18,
	    rematch_ready = 19,
	    rematch_opponent_requested = 20;
		
	@OnOpen
	public void onOpen(Session session, EndpointConfig cfg){
		playerSession = session;
		playerBasicRemote = session.getBasicRemote();
		
	}
	
	public void sendOpponentFound(String oName, boolean isBlack){
		JsonObject obj = Json.createObjectBuilder().
				add("action", opponent_found).
				add("opponentName", oName).
				add("isBlack", isBlack).
				build();
		
		try {
			playerBasicRemote.sendText(obj.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setController(GameController ctrl){
		controller = ctrl;
	}
	public String getName(){
		return playerName;
	}
	private void findOpponent(){
		this.lookingForOpponent = true;
		queue.getInLine(grids, this);
	}
	
	public void setPlayerTurn(boolean active){
		JsonObject obj = Json.createObjectBuilder().
				add("action", active ? your_turn : opponent_turn).
				build();
		
		try {
			playerBasicRemote.sendText(obj.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendOpponentMove(JsonNumber x, JsonNumber y){
		JsonObject obj = Json.createObjectBuilder().
				add("action", opponent_move).
				add("x", x).add("y", y).
				build();
		
		try {
			playerBasicRemote.sendText(obj.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void clickNotValid(){
		JsonObject obj = Json.createObjectBuilder().
				add("action", invalid_click).
				build();
		
		try {
			playerBasicRemote.sendText(obj.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendGameWin(){
		JsonObject obj = Json.createObjectBuilder().add("action", you_win).build();
		
		try {
			playerBasicRemote.sendText(obj.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void sendGameLose(){
		JsonObject obj = Json.createObjectBuilder().add("action", opponent_win).build();
		
		try {
			playerBasicRemote.sendText(obj.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setYourStone(JsonObjectBuilder builder){
		
		JsonObject obj = builder.add("action", set_your_stone).build();
		
		try {
			playerBasicRemote.sendText(obj.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void setOpponentStone(JsonObjectBuilder builder){
		JsonObject obj = builder.add("action", set_opponent_stone).build();
		
		try {
			playerBasicRemote.sendText(obj.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendRematchWaitOpponent(){
		JsonObject obj = Json.createObjectBuilder().add("action", rematch_wait_opponent).build();
		
		try {
			playerBasicRemote.sendText(obj.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void sendOpponentWantRematch(){
		JsonObject obj = Json.createObjectBuilder().add("action", rematch_opponent_requested).build();
		
		try {
			playerBasicRemote.sendText(obj.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void sendRematchReady(boolean isBlack){
		JsonObject obj = Json.createObjectBuilder().
				add("action", rematch_ready).
				add("isBlack", isBlack).
				build();
		
		try {
			playerBasicRemote.sendText(obj.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@OnMessage
	public void onMessage(String msg, Session session) throws IOException{

		JsonReader reader = Json.createReader(new StringReader(msg));
		JsonObject playerMsg = reader.readObject();
		reader.close();
		
		switch(playerMsg.getInt("action")){
			case my_stats:
				grids = playerMsg.getInt("grids");
				playerName = playerMsg.getString("name");
				playerBasicRemote.sendText(Json.createObjectBuilder().add("action", standby).build().toString());
				break;
			case find_opponent:
				findOpponent();
				break;
			case begin_game:
				controller.beginGame();
				break;
			case my_move:
				controller.playerMove(playerMsg.getJsonNumber("x"), playerMsg.getJsonNumber("y"));
				break;
			case my_click:
				controller.playerClick(playerMsg.getInt("row"), playerMsg.getInt("col"));
				break;
			case rematch_request:
				controller.rematchRequested(this);
				break;
			default:
				break;
		}
	}
	
	@OnClose
	public void onClose(Session session){
		this.lookingForOpponent = false;
		queue.leaveLine(grids, this);
		
		if(controller != null){
			controller.playerDisconnect(this);
		}
	}
	
	public void sendOpponentDisconnect() {
		try {
			playerBasicRemote.sendText(Json.createObjectBuilder().
					add("action", opponent_disconnect).build().toString());
			playerSession.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	@OnError
	public void onError(Session session, Throwable t){
		t.printStackTrace();
	}
	
	public EndpointServer() {}
	
	public boolean lookingForOpponent(){
		return lookingForOpponent;
	}
	public void setLookingForOpponent(boolean yes){
		lookingForOpponent = yes;
	}

}

