package eu.cloud4soa.c4sgitservice.datamodel;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * User: pgouvas
 * Date: 8/3/12
 * Time: 7:45 AM
 */
@Entity(name = "GITREPO")
public class GitRepo {

    @Id
    @GeneratedValue
    @Column(name = "GITREPOID")
    private Long  gitrepoid;

    @Column(name = "GITURL")
    private String  giturl;

    @Column(name = "GITREPO")
    private String  gitrepo;

    @ManyToOne
    @JoinColumn(name = "USERID")
    private User user;


    @ManyToOne
    @JoinColumn(name = "PAASID")
    private Paas paas;


    public Paas getPaas() {
        return paas;
    }

    public void setPaas(Paas paas) {
        this.paas = paas;
    }

    public Long getGitrepoid() {
        return gitrepoid;
    }

    public void setGitrepoid(Long gitrepoid) {
        this.gitrepoid = gitrepoid;
    }

    public String getGiturl() {
        return giturl;
    }

    public void setGiturl(String giturl) {
        this.giturl = giturl;
    }

    public String getGitrepo() {
        return gitrepo;
    }

    public void setGitrepo(String gitrepo) {
        this.gitrepo = gitrepo;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
