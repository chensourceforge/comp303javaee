package comp303project;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the users database table.
 * 
 */
@Entity
@Table(name="users")
@NamedQuery(name="User.findAll", query="SELECT u FROM User u")
public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String username;

	private String password;

	//bi-directional one-to-one association to Gogame
	@OneToOne(mappedBy="user")
	private Gogame gogame;

	public User() {}
	
	public User(String username, String password){
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Gogame getGogame() {
		return this.gogame;
	}

	public void setGogame(Gogame gogame) {
		this.gogame = gogame;
	}

}