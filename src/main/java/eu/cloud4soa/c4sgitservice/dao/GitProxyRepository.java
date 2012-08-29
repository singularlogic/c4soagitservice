package eu.cloud4soa.c4sgitservice.dao;

import eu.cloud4soa.c4sgitservice.datamodel.GitProxy;
import eu.cloud4soa.c4sgitservice.datamodel.GitRepo;
import eu.cloud4soa.c4sgitservice.datamodel.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: pgouvas
 * Date: 8/28/12
 * Time: 5:26 PM
 * To change this template use File | Settings | File Templates.
 */
public interface GitProxyRepository extends JpaRepository<GitProxy,Long> {

    public List<GitProxy> findByProxyname(String proxyname);

    public List<GitProxy> findByUser(User user);

    public List<GitProxy> findByUserAndGitproxyid(User user, Long proxyid);

}
