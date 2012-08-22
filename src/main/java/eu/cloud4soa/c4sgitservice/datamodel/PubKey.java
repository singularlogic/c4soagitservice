package eu.cloud4soa.c4sgitservice.datamodel;

import org.hibernate.annotations.Table;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * User: pgouvas
 * Date: 8/2/12
 * Time: 12:30 PM
 */

@Entity(name = "PUBKEY")
public class PubKey {

    @Id
    @GeneratedValue
    @Column(name = "PUBKEYID")
    private Long pubkeyid;

    @Column(name = "PUBKEY",length = 500)
    private String pubkey;

    @ManyToOne
    @JoinColumn(name = "USERID")
    private User user;

    public Long getPubkeyid() {
        return pubkeyid;
    }

    public void setPubkeyid(Long pubkeyid) {
        this.pubkeyid = pubkeyid;
    }

    public String getPubkey() {
        return pubkey;
    }

    public void setPubkey(String pubkey) {
        this.pubkey = pubkey;
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "PubKey{" +
                "pubkeyid=" + pubkeyid +
                ", pubkey='" + pubkey + '\'' +
                ", user=" + user +
                '}';
    }
}
