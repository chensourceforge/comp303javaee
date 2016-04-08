package comp303project;

import java.util.concurrent.ArrayBlockingQueue;
import javax.ejb.Singleton;


@Singleton
public class QueueBean {

	private final ArrayBlockingQueue<EndpointServer> waitingQueue9x9 = new ArrayBlockingQueue<EndpointServer>(1);
	private final ArrayBlockingQueue<EndpointServer> waitingQueue13x13 = new ArrayBlockingQueue<EndpointServer>(1);
	private final ArrayBlockingQueue<EndpointServer> waitingQueue19x19 = new ArrayBlockingQueue<EndpointServer>(1);
	
	public void getInLine(int grids, EndpointServer newEndpoint){
		ArrayBlockingQueue<EndpointServer> q;
		switch(grids){
			case 9:
				q = waitingQueue9x9;
				break;
			case 13:
				q = waitingQueue13x13;
				break;
			case 19:
				q = waitingQueue19x19;
				break;
			default:
				q = waitingQueue9x9;
				break;
		}
		
		EndpointServer existingEndpoint = q.poll();
		if(existingEndpoint != null && 
				existingEndpoint != newEndpoint && 
				!existingEndpoint.getName().equals(newEndpoint.getName()) && 
				existingEndpoint.lookingForOpponent()){
			// found an opponent
			new GameController(grids, existingEndpoint, newEndpoint);
			
			
		} else if(!q.offer(newEndpoint)){
			// no opponent AND failed to get on queue
			getInLine(grids, newEndpoint);
		} 
	}
	
	public void leaveLine(int grids, EndpointServer endpoint){
		ArrayBlockingQueue<EndpointServer> q = null;
		switch(grids){
			case 9:
				q = waitingQueue9x9;
				break;
			case 13:
				q = waitingQueue13x13;
				break;
			case 19:
				q = waitingQueue19x19;
				break;
			default:
				break;
		}
		if(q != null && q.contains(endpoint)){
			q.poll();
		}
	}
	
    public QueueBean() {
        // 
    }

}
