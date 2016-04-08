package comp303project;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 * Session Bean implementation class DAOBean
 */
//@Stateless
//@LocalBean
public class DAOBean {

	private EntityManagerFactory emf;
	private EntityManager em;
	
    public DAOBean() {}
    
    //@PostConstruct
    public void initResurces(){
    	emf = Persistence.createEntityManagerFactory("GoGame");
		em = emf.createEntityManager();
		em.setProperty("javax.persistence.cache.storeMode", "REFRESH");
    }
    
    //@PreDestroy
    public void rleaseResources(){
    	em.close();
    	emf.close();
    }
    
    public User findUser(String username){
    	initResurces();
    	User user = em.find(User.class, username);
    	rleaseResources();
    	return user;
    }
    
    public void insertUser(User user){
    	initResurces();
    	em.getTransaction().begin();
    	em.persist(user);
    	em.getTransaction().commit();
    	rleaseResources();
    }
    
    public void updateUser(User user){
    	initResurces();
    	em.getTransaction().begin();
    	em.merge(user);
    	em.getTransaction().commit();
    	rleaseResources();
    }
    
    public Gogame findGogame(String username){
    	initResurces();
    	Gogame gogame = em.find(Gogame.class, username);
    	rleaseResources();
    	return gogame;
    }
    
    public void insertGogame(Gogame gogame){
    	initResurces();
    	em.getTransaction().begin();
    	em.persist(gogame);
    	em.getTransaction().commit();
    	rleaseResources();
    }
    
    public void updateGogame(Gogame gogame){
    	initResurces();
    	em.getTransaction().begin();
    	em.merge(gogame);
    	em.getTransaction().commit();
    	rleaseResources();
    }
    
    
    public int getRanking(Gogame myStats){
    	initResurces();
    	Query q = em.createNamedQuery("getRanking");
    	q.setParameter("myScore", myStats.getTotalScore());
    	Long L = (Long)q.getSingleResult() + 1;
    	rleaseResources();
    	return L.intValue();
    }
    
    public List<LeaderBoardEntry> getTopPlayers(int maxEntries){
    	String sql = "select new comp303project.LeaderBoardEntry(g.username, g.totalScore, g.wins, g.losses) from Gogame g order by g.totalScore DESC";
    	initResurces();
    	List<LeaderBoardEntry> results = em.createQuery(sql, LeaderBoardEntry.class)
    			.setMaxResults(maxEntries)
    			.getResultList();
    	
    	rleaseResources();
    	return results;
    }
}
