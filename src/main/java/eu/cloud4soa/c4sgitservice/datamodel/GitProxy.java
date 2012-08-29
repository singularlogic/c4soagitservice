package eu.cloud4soa.c4sgitservice.datamodel;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * User: pgouvas
 * Date: 8/28/12
 * Time: 4:44 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity(name = "GITPROXY")
public class GitProxy {


    @Id
    @GeneratedValue
    @Column(name = "GITPROXYID")
    private Long  gitproxyid;


    @Column(name = "PROXYNAME")
    private String  proxyname;

    @OneToOne //(cascade =CascadeType.ALL )
    @JoinColumn(name = "REPOID")
    private GitRepo repo;

    @ManyToOne
    @JoinColumn(name = "USERID")
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getGitproxyid() {
        return gitproxyid;
    }

    public void setGitproxyid(Long gitproxyid) {
        this.gitproxyid = gitproxyid;
    }

    public String getProxyname() {
        return proxyname;
    }

    public void setProxyname(String proxyname) {
        this.proxyname = proxyname;
    }

    public GitRepo getRepo() {
        return repo;
    }

    public void setRepo(GitRepo repo) {
        this.repo = repo;
    }

}
