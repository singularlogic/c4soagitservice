package eu.cloud4soa.c4sgitservice.datamodel;

import java.io.Serializable;
import java.util.Set;
import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * User: pgouvas
 * Date: 8/2/12
 * Time: 12:30 PM
 */

@Entity(name="C4sUser")
public class User implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue
    @Column(name = "USERID")
    private Long userId;

    @Column(name = "USERNAME")
    private String username;
    
    @Column(name = "PASSWORD")
    private String password;

    //If the comment below is activated then a JOIN USER_PUBKEY is created
    //@OneToMany(fetch = FetchType.LAZY)
    //private Set<PubKey> pubkeys;

    public User() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

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

//    public Set<PubKey> getPubkeys() {
//        return pubkeys;
//    }
//
//    public void setPubkeys(Set<PubKey> pubkeys) {
//        this.pubkeys = pubkeys;
//    }


    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}  //EoClass
