package eu.cloud4soa.c4sgitservice.dao;

import eu.cloud4soa.c4sgitservice.datamodel.GitRepo;
import eu.cloud4soa.c4sgitservice.datamodel.PubKey;
import eu.cloud4soa.c4sgitservice.datamodel.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: pgouvas
 * Date: 8/3/12
 * Time: 7:55 AM
 */
public interface GitRepoRepository extends JpaRepository<GitRepo,Long> {

    public List<GitRepo> findByUser(User user);

    public List<GitRepo> findByGitrepo(String gitrepo);

    public List<GitRepo> findByProxyname(String proxyname);

    public List<GitRepo> findByUserAndProxyname(User user, String proxyname);

    public List<GitRepo> findByUserAndGitrepoid(User user, Long gitrepoid);

}