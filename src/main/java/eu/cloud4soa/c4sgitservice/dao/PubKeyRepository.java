package eu.cloud4soa.c4sgitservice.dao;

import eu.cloud4soa.c4sgitservice.datamodel.PubKey;
import eu.cloud4soa.c4sgitservice.datamodel.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: pgouvas
 * Date: 8/2/12
 * Time: 3:17 PM
 */
public interface PubKeyRepository extends JpaRepository<PubKey,Long> {

    /*
    * Will produce autogenerated DAO Impl
    */
    public PubKey findByPubkeyid(Long pubkeyid);

    public List<PubKey> findByUser(User user);

    public List<PubKey> findByPubkey(String pubkey);

    public List<PubKey> findByUserAndPubkey(User user,String pubkey);

    public List<PubKey> findByUserAndPubkeyid(User user,Long pubkeyid);

}
