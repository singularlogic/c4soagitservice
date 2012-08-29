package eu.cloud4soa.c4sgitservice.dao;

import eu.cloud4soa.c4sgitservice.datamodel.Paas;
import eu.cloud4soa.c4sgitservice.datamodel.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created with IntelliJ IDEA.
 * User: pgouvas
 * Date: 8/29/12
 * Time: 9:44 AM
 * To change this template use File | Settings | File Templates.
 */
public interface PaasRepository extends JpaRepository<Paas,Long> {

}
