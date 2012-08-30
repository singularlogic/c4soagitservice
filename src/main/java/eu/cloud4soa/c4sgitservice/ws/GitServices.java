package eu.cloud4soa.c4sgitservice.ws;

import eu.cloud4soa.c4sgitservice.dao.*;
import eu.cloud4soa.c4sgitservice.datamodel.*;
import eu.cloud4soa.c4sgitservice.utils.Util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.jws.WebMethod;
import javax.jws.WebService;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;


@WebService
@Component
public class GitServices extends SpringBeanAutowiringSupport {

    protected final Log logger = LogFactory.getLog(getClass());

    @Autowired
    PubKeyRepository   pubkeydao;
    @Autowired
    UserRepository     userdao;
    @Autowired
    GitRepoRepository  repodao;
    @Autowired
    GitProxyRepository proxydao;
    @Autowired
    PaasRepository     paasdao;

    //the first time the file has to be generated by ssh-agent
    private final String PROPERTIES_FILE         = "gitproperties.txt";
    private String AUTHORIZED_KEYS_FILE    = "/home/pgouvas/.ssh/authorized_keys";
    private String C4SOA_SERVER_PUBLIC_KEY = "/home/pgouvas/.ssh/id_rsa.pub";
    private String PROXY_GIT_FILE          = "/home/pgouvas/proxy-git";
    private String SERVER_ACCOUNT_NAME     = "pgouvas";
    private String SERVER_IP_ADDRESS       = "192.168.1.69";


    public GitServices(){
        logger.info("Constructor called");
        File temp = null;
        try {
//            temp = File.createTempFile("i-am-a-temp-file", ".tmp");
//            String absolutePath = temp.getAbsolutePath();
//            logger.info("File path : " + absolutePath);
//            temp.delete();
            Properties properties = new Properties();
            properties.load(new FileInputStream(PROPERTIES_FILE));
            AUTHORIZED_KEYS_FILE    = properties.getProperty("AUTHORIZED_KEYS_FILE","/home/pgouvas/.ssh/authorized_keys").trim();
            C4SOA_SERVER_PUBLIC_KEY = properties.getProperty("C4SOA_SERVER_PUBLIC_KEY","/home/pgouvas/.ssh/id_rsa.pub").trim();
            PROXY_GIT_FILE          = properties.getProperty("PROXY_GIT_FILE","/home/pgouvas/proxy-git").trim();
            SERVER_ACCOUNT_NAME     = properties.getProperty("SERVER_ACCOUNT_NAME","pgouvas").trim();
            SERVER_IP_ADDRESS       = properties.getProperty("SERVER_IP_ADDRESS","192.168.1.69").trim();
            logger.info("Properties loaded successfully");
            logger.info("AUTHORIZED_KEYS_FILE:|"+AUTHORIZED_KEYS_FILE+"|");
            logger.info("C4SOA_SERVER_PUBLIC_KEY:|"+C4SOA_SERVER_PUBLIC_KEY+"|");
            logger.info("PROXY_GIT_FILE:|"+PROXY_GIT_FILE+"|");
            logger.info("SERVER_ACCOUNT_NAME:|"+SERVER_ACCOUNT_NAME+"|");
            logger.info("SERVER_IP_ADDRESS:|"+SERVER_IP_ADDRESS+"|");

        } catch (IOException e) {
            logger.error("Error in Constructor"+e);
        }
    }

    /*
    * Key Handling Methods
    */

    @WebMethod
    public String[] getC4SOAPublicKey(String username, String password) {
        String[] res = {"1","Internal Server Error"};
        if (userdao != null) {
            List userlist =  userdao.findByUsernameAndPassword(username, password);
            if (userlist != null && !userlist.isEmpty()) {
                User user = (User) userlist.get(0);
                try {
                    String pub = Util.readfile(C4SOA_SERVER_PUBLIC_KEY);
                    logger.info("Success-Public Key Fetched " );
                    res = new String[]{"0",pub};
                } catch (Exception e) {
                    logger.error("Error reading public key file");
                    res = new String[]{"1","Error reading public key file"};
                }
            } else {
                logger.error("User with username: "+username+" password: "+password+" does not exist");
                res = new String[]{"1","Error-User with username: "+username+" password: "+password+" does not exist"};
            }
        } else {
            logger.error("SpringBean userdao cannot be instantiated");
            res = new String[]{"1","Error-Internal Server - Spring Bean cannot be initialized"};
        }
        return res;
    }

    @WebMethod
    public String[] generateKeyPairForUser(String username, String password) {
        String[] res = {"1","Internal Server Error"};
        int retvalue=0;
        if (userdao != null) {
            List userlist =  userdao.findByUsernameAndPassword(username, password);
            if (userlist != null && !userlist.isEmpty()) {
                //Step 1 - Execute command
                try {
                    Process child = Runtime.getRuntime().exec(new String[] {"ssh-keygen", //
                            "-q"            , // quiet
                            "-t", "rsa"     , //
                            "-P", ""      , // Zero password
                            "-C", username  , //
                            "-f", username    //
                    });
                    child.waitFor();

                    //Get the input stream and read from it
                    InputStream in = child.getInputStream();
                    int c;
                    while ((c = in.read()) != -1) {
                        //System.out.print((char)c);
                    }
                    in.close();
                    retvalue = child.exitValue();
                    logger.info("ssh-keygen returned: "+ retvalue);
                    //Step 2 - read file
                    if (retvalue == 0){ //Everything OK
                        try{
                            String pub,priv="";
                            pub = Util.readfile(username + ".pub");
                            priv = Util.readfile(username);
                            //cleanup
                            try {
                                String command1 = "rm "+username+".pub";
                                Process child1 = Runtime.getRuntime().exec(command1);
                                String command2 = "rm "+username;
                                Process child2 = Runtime.getRuntime().exec(command2);
                                child1.waitFor();
                                child2.waitFor();
                                logger.info("Success-Key-Pair created for User with username: " );
                                res = new String[]{"0",pub,priv};
                            } catch (Exception ex) {
                                logger.error("Error-Cleaning temporary files");
                                res = new String[]{"1","Error-Cleaning temporary files"};
                            }
                        } catch (Exception ex){
                            logger.error("Error-Reading temporary files");
                            res = new String[]{"1","Error-Reading temporary files"};
                        }
                    } else {
                        logger.error("Error-Internal ssh-keygen error");
                        res = new String[]{"1","Internal ssh-keygen error"};
                    }
                } catch (Exception ex) {
                    logger.error("Error-Internal Server Error while executing ssh-keygen");
                    res = new String[]{"1","Error-Internal Server Error while executing ssh-keygen"};
                }

            } else {
                logger.error("User with username: "+username+" password: "+password+" does not exist");
                res = new String[]{"1","Error-User with username: "+username+" password: "+password+" does not exist"};
            }
        } else {
            logger.error("SpringBean userdao cannot be instantiated");
            res = new String[]{"1","Error-Internal Server - Spring Bean cannot be initialized"};
        }

        return res;
    }

  /*
   * Key Handling Methods
   */
    @WebMethod
    public String[] registerPublicKeyForUser(String username, String password, String rsa_pub_key) {
        String[] res = {"1","Internal Server Error"};
        if (userdao != null) {
            List userlist =  userdao.findByUsernameAndPassword(username, password);
            if (userlist != null && !userlist.isEmpty()) {
                User user = (User) userlist.get(0);
                List<PubKey> pubkeys = pubkeydao.findByPubkey(rsa_pub_key);
                if (pubkeys.isEmpty()){
                    PubKey pubKey = new PubKey();
                    pubKey.setPubkey(rsa_pub_key);
                    pubKey.setUser(user);
                    pubkeydao.save(pubKey);
                    //write to file
                    String segment = Util.createAuthorizedKeysSegment(PROXY_GIT_FILE,pubKey.getPubkeyid()+"",rsa_pub_key);
                    Util.appendToFile(AUTHORIZED_KEYS_FILE,segment);
                    logger.info("Success-User with username: " + username + " registered pubkey");
                    res = new String[]{"0","OK"};

                } else {
                    logger.error("User with username: "+username+" already contains the key");
                    res = new String[]{"1","Error-User with username: "+username+" already contains the key"};
                }
            } else {
                logger.error("User with username: "+username+" password: "+password+" does not exist");
                res = new String[]{"1","Error-User with username: "+username+" password: "+password+" does not exist"};
            }
        } else {
            logger.error("SpringBean userdao cannot be instantiated");
            res = new String[]{"1","Error-Internal Server - Spring Bean cannot be initialized"};
        }
    return res;
    }

    @WebMethod
    public String[] deletePublicKeyFromUser(String username, String password, String rsa_pub_key_id) {
        String[] res = {"1","Internal Server Error"};
        if (userdao != null) {
            List userlist =  userdao.findByUsernameAndPassword(username, password);
            if (userlist != null && !userlist.isEmpty()) {
                User user = (User) userlist.get(0);
                try{
                    List<PubKey> pubkeys = pubkeydao.findByUserAndPubkeyid(user,new Long(rsa_pub_key_id) );
                    if (!pubkeys.isEmpty()){
                        //delete them from file first
                        for (int i = 0; i < pubkeys.size(); i++) {
                            PubKey pubKey =  pubkeys.get(i);
                            Util.replaceBlockWithSedForAuthorizedKeys(AUTHORIZED_KEYS_FILE, rsa_pub_key_id);
                        }
                        //delete them from the database
                        pubkeydao.delete(pubkeys);
                        logger.info("Success-Deleted pubkey for user with username: " + username );
                        res = new String[]{"0","OK"};
                    } else {
                        logger.error("User with username: "+username+" does not contain the specific pubkey");
                        res = new String[]{"1","User with username: "+username+" does not contain the specific pubkey"};
                    }
                }  catch(Exception ex){
                    logger.error("Error-Internal Server Error "+ex);
                    res = new String[]{"1","Error-Internal Server Error "+ex};
                }
            } else {
                logger.error("User with username: "+username+" password: "+password+" does not exist");
                res = new String[]{"1","Error-User with username: "+username+" password: "+password+" does not exist"};
            }
        } else {
            logger.error("SpringBean userdao cannot be instantiated");
            res = new String[]{"1","Error-Internal Server - Spring Bean cannot be initialized"};
        }
        return res;
    }

    @WebMethod
    public String[] getPublicKeysForUser(String username, String password) {
        String[] res = {"1","Internal Server Error"};
        if (userdao != null) {
            List userlist =  userdao.findByUsernameAndPassword(username, password);
            if (userlist != null && !userlist.isEmpty()) {
                User user = (User) userlist.get(0);
                List<PubKey> pubkeys = pubkeydao.findByUser(user);
                    logger.info("Success-User with username: " + username + " has "+pubkeys.size()+" keys");
                    String ret="0";
                    for (int i = 0; i < pubkeys.size(); i++) {
                        PubKey pubKey = pubkeys.get(i);
                        //in the first one we have to append a comma in after the "0"
                        ret+=","+pubKey.getPubkeyid()+","+pubKey.getPubkey();
                    }
                    res = ret.split(",");
            } else {
                logger.error("User with username: "+username+" password: "+password+" does not exist");
                res = new String[]{"1","Error-User with username: "+username+" password: "+password+" does not exist"};
            }
        } else {
            logger.error("SpringBean userdao cannot be instantiated");
            res = new String[]{"1","Error-Internal Server - Spring Bean cannot be initialized"};
        }
        return res;
    }


   /*
    * Repo Handling Methods
    */

    @WebMethod
    public String[] registerGitRepository(String username, String password, String giturl, String reponame, String paasid) {
        String[] res = {"1","Internal Server Error"};
        if (userdao != null) {
            List userlist =  userdao.findByUsernameAndPassword(username, password);
            if (userlist != null && !userlist.isEmpty()) {
                User user = (User) userlist.get(0);
                logger.info("userid:"+user.getUserId());
                Paas paas = paasdao.findOne(new Long(paasid));
                logger.info("paas:"+paasid);
                //check double
                List<GitRepo> repos = repodao.findByGitrepo(reponame);
                if (repos.isEmpty()){
                    GitRepo gitrepo = new GitRepo();
                    gitrepo.setGiturl(giturl);
                    gitrepo.setGitrepo(reponame);
                    //gitrepo.setUser(user);
                    gitrepo.setUser(user);
                    //gitrepo.setPaas(paas);
                    gitrepo.setPaas(paas);
                    repodao.save(gitrepo);
                    logger.info("Success-Repo registered" );
                    res = new String[]{"0","OK"};
                } else {
                    logger.error("Error- This git-repository already exists");
                    res = new String[]{"1","Error- This git-repository already exists"};
                }
            } else {
                logger.error("User with username: "+username+" password: "+password+" does not exist");
                res = new String[]{"1","Error-User with username: "+username+" password: "+password+" does not exist"};
            }
        } else {
            logger.error("SpringBean userdao cannot be instantiated");
            res = new String[]{"1","Error-Internal Server - Spring Bean cannot be initialized"};
        }
        return res;
    }

    @WebMethod
    public String[] registerGitProxy(String username, String password, String proxyname ) {
        String[] res = {"1","Internal Server Error"};
        if (userdao != null) {
            List userlist =  userdao.findByUsernameAndPassword(username, password);
            if (userlist != null && !userlist.isEmpty()) {
                User user = (User) userlist.get(0);
                //check double
                List<GitProxy> repos = proxydao.findByProxyname(proxyname);
                if (repos.isEmpty()){
                    GitProxy gitproxy = new GitProxy();
                    gitproxy.setProxyname(proxyname);
                    gitproxy.setUser(user);
                    proxydao.save(gitproxy);
                    res = new String[]{"0","OK"};
                } else {
                    logger.error("Error- This git-proxy already exists");
                    res = new String[]{"1","Error- This git-proxy already exists"};
                }
            } else {
                logger.error("User with username: "+username+" password: "+password+" does not exist");
                res = new String[]{"1","Error-User with username: "+username+" password: "+password+" does not exist"};
            }
        } else {
            logger.error("SpringBean userdao cannot be instantiated");
            res = new String[]{"1","Error-Internal Server - Spring Bean cannot be initialized"};
        }
        return res;
    }

    @WebMethod
    public String[] bindProxyToGit(String username, String password, String proxyid, String gitid  ) {
        String[] res = {"1","Internal Server Error"};
        if (userdao != null) {
            List userlist =  userdao.findByUsernameAndPassword(username, password);
            if (userlist != null && !userlist.isEmpty()) {
                User user = (User) userlist.get(0);
                try {   //TODO add findby
                    //GitRepo
                    List<GitRepo> repos = repodao.findByUserAndGitrepoid(user, new Long(gitid));
                    //GitProxy
                    List<GitProxy> proxies = proxydao.findByUserAndGitproxyid(user,new Long(proxyid));
                    if (!repos.isEmpty()){
                         if (!proxies.isEmpty()){
                             GitRepo repo = repos.get(0);
                             GitProxy proxy = proxies.get(0);
                             if (repo!=null && proxy!=null) {
                                 //delete old from file
                                 Util.replaceBlockWithSedForProxyGit(PROXY_GIT_FILE, ""+proxy.getGitproxyid());
                                 //change the database
                                 proxy.setRepo(repo);
                                 proxydao.save(proxy);
                                 //create new file
                                 String segment = Util.createProxyGitSegment( ""+proxy.getGitproxyid() , proxy.getProxyname() , repo.getGiturl() , repo.getGitrepo() );
                                 logger.info("writing to PROXY_GIT_FILE:|"+PROXY_GIT_FILE+"|");
                                 Util.appendToFile(PROXY_GIT_FILE,segment);
                                 logger.info("Success-Repo created" );
                                 //return
                                 String gitcommand = "git remote add origin "+SERVER_ACCOUNT_NAME+"@"+SERVER_IP_ADDRESS+":"+proxy.getProxyname();
                                 res = new String[]{"0","OK - You should execute "+gitcommand};
                             }   else {
                                 throw new Exception("Repo or Proxy does not exist");
                             }

                         }   else {
                             logger.error("Proxy with this id does not exist for the specific user");
                             res = new String[]{"1","Proxy with this id does not exist for the specific user"};
                         }
                    }   else {
                        logger.error("Repository with this id does not exist for the specific user");
                        res = new String[]{"1","Repository with this id does not exist for the specific user"};
                    }
                } catch(Exception ex){
                    logger.error("Git Or Proxy with this id does not exist");
                    res = new String[]{"1","Error-Git Or Proxy with this id does not exist"};
                }

            } else {
                logger.error("User with username: "+username+" password: "+password+" does not exist");
                res = new String[]{"1","Error-User with username: "+username+" password: "+password+" does not exist"};
            }
        } else {
            logger.error("SpringBean userdao cannot be instantiated");
            res = new String[]{"1","Error-Internal Server - Spring Bean cannot be initialized"};
        }
        return res;
    }

    @WebMethod
    public String[] getGitRepositoriesForUser(String username, String password) {
        String[] res = {"1","Internal Server Error"};
        if (userdao != null) {
            List userlist =  userdao.findByUsernameAndPassword(username, password);
            if (userlist != null && !userlist.isEmpty()) {
                User user = (User) userlist.get(0);
                List<GitRepo> repos = repodao.findByUser(user);
                logger.info("Success-User with username: " + username + " has "+repos.size()+" repos");
                String ret="0";
                for (int i = 0; i < repos.size(); i++) {
                    GitRepo gitrepo = repos.get(i);
                    //in the first one we have to append a comma in after the "0"
                    ret+=","+gitrepo.getGitrepoid()+","+gitrepo.getGiturl()+","+gitrepo.getGitrepo();
                }
                res = ret.split(",");
            } else {
                logger.error("User with username: "+username+" password: "+password+" does not exist");
                res = new String[]{"1","Error-User with username: "+username+" password: "+password+" does not exist"};
            }
        } else {
            logger.error("SpringBean userdao cannot be instantiated");
            res = new String[]{"1","Error-Internal Server - Spring Bean cannot be initialized"};
        }
        return res;
    }

    @WebMethod
    public String[] getGitProxiesForUser(String username, String password) {
        String[] res = {"1","Internal Server Error"};
        if (userdao != null) {
            List userlist =  userdao.findByUsernameAndPassword(username, password);
            if (userlist != null && !userlist.isEmpty()) {
                User user = (User) userlist.get(0);
                List<GitProxy> proxies = proxydao.findByUser(user);
                logger.info("Success-User with username: " + username + " has "+proxies.size()+" proxies");
                String ret="0";
                for (int i = 0; i < proxies.size(); i++) {
                    GitProxy gitproxy = proxies.get(i);
                    String binding = "unbound";
                    //in the first one we have to append a comma in after the "0"
                    GitRepo bindrepo = gitproxy.getRepo();
                    if (bindrepo!=null) binding=bindrepo.getGitrepoid()+"";
                    ret+=","+gitproxy.getGitproxyid()+","+gitproxy.getProxyname()+","+binding;
                }
                res = ret.split(",");
            } else {
                logger.error("User with username: "+username+" password: "+password+" does not exist");
                res = new String[]{"1","Error-User with username: "+username+" password: "+password+" does not exist"};
            }
        } else {
            logger.error("SpringBean userdao cannot be instantiated");
            res = new String[]{"1","Error-Internal Server - Spring Bean cannot be initialized"};
        }
        return res;
    }

    @WebMethod
    public String[] deleteGitRepositoryFromUserByRepoid(String username, String password, String repoid) {
        String[] res = {"1","Internal Server Error"};
        if (userdao != null) {
            List userlist =  userdao.findByUsernameAndPassword(username, password);
            if (userlist != null && !userlist.isEmpty()) {
                User user = (User) userlist.get(0);
                try{
                    List<GitRepo> repos = repodao.findByUserAndGitrepoid(user, new Long(repoid));
                    if (!repos.isEmpty()){
                        //delete them from the database
                        repodao.delete(repos);
                        logger.info("Success-Deleted repository for user with username: " + username );
                        res = new String[]{"0","OK"};
                    } else {
                        logger.error("User with username: "+username+" does not contain the specific repository");
                        res = new String[]{"1","User with username: "+username+" does not contain the specific repository"};
                    }
                }   catch (Exception ex) {
                    logger.error("Error-Internal Server Error "+ex);
                    res = new String[]{"1","Error-Internal Server Error "+ex};
                }
            } else {
                logger.error("User with username: "+username+" password: "+password+" does not exist");
                res = new String[]{"1","Error-User with username: "+username+" password: "+password+" does not exist"};
            }
        } else {
            logger.error("SpringBean userdao cannot be instantiated");
            res = new String[]{"1","Error-Internal Server - Spring Bean cannot be initialized"};
        }
        return res;
    }

    @WebMethod
    public String[] deleteGitProxyFromUserByProxyid(String username, String password, String proxyid) {
        String[] res = {"1","Internal Server Error"};
        if (userdao != null) {
            List userlist =  userdao.findByUsernameAndPassword(username, password);
            if (userlist != null && !userlist.isEmpty()) {
                User user = (User) userlist.get(0);
                try{
                    List<GitProxy> proxies = proxydao.findByUserAndGitproxyid(user, new Long(proxyid));
                    if (!proxies.isEmpty()){
                        //delete them from file
                        for (int i = 0; i < proxies.size(); i++) {
                            GitProxy proxy =  proxies.get(i);
                            Util.replaceBlockWithSedForProxyGit(PROXY_GIT_FILE, proxy.getGitproxyid()+"");
                            //delete them from the database
                            proxydao.delete(proxy);
                        }
                        logger.info("Success-Deleted proxyname for user with username: " + username );
                        res = new String[]{"0","OK"};
                    } else {
                        logger.error("User with username: "+username+" does not contain the specific proxy");
                        res = new String[]{"1","User with username: "+username+" does not contain the specific proxy"};
                    }
                }   catch (Exception ex) {
                    logger.error("Error-Internal Server Error "+ex);
                    res = new String[]{"1","Error-Internal Server Error "+ex};
                }
            } else {
                logger.error("User with username: "+username+" password: "+password+" does not exist");
                res = new String[]{"1","Error-User with username: "+username+" password: "+password+" does not exist"};
            }
        } else {
            logger.error("SpringBean userdao cannot be instantiated");
            res = new String[]{"1","Error-Internal Server - Spring Bean cannot be initialized"};
        }
        return res;
    }

//    @WebMethod
//    public String[] updateGitRepositoryFromUserByProxyname(String username, String password, String proxyname, String giturl , String reponame) {
//        String[] res = {"1","Internal Server Error"};
//        if (userdao != null) {
//            List userlist =  userdao.findByUsernameAndPassword(username, password);
//            if (userlist != null && !userlist.isEmpty()) {
//                User user = (User) userlist.get(0);
//                try{
//                    List<GitRepo> repos = repodao.findByUserAndProxyname(user, proxyname);
//                    if (!repos.isEmpty()){
//                        //delete old from file
//                        Util.replaceBlockWithSedForProxyGit(PROXY_GIT_FILE, proxyname);
//                        //replace db entry
//                        for (int i = 0; i < repos.size(); i++) {
//                            GitRepo gitrepo =  repos.get(i);
//                            gitrepo.setGiturl(giturl);
//                            gitrepo.setGitrepo(reponame);
//                            repodao.save(gitrepo);
//                        }
//                        //create new file segment
//                        String segment = Util.createProxyGitSegment(proxyname,giturl,reponame);
//                        Util.appendToFile(PROXY_GIT_FILE,segment);
//                        logger.info("Success-Updated proxyname for user with username: " + username );
//                        res = new String[]{"0","OK"};
//                    } else {
//                        logger.error("User with username: "+username+" does not contain the specific repository");
//                        res = new String[]{"1","User with username: "+username+" does not contain the specific repository"};
//                    }
//                }   catch (Exception ex) {
//                    logger.error("Error-Internal Server Error "+ex);
//                    res = new String[]{"1","Error-Internal Server Error "+ex};
//                }
//            } else {
//                logger.error("User with username: "+username+" password: "+password+" does not exist");
//                res = new String[]{"1","Error-User with username: "+username+" password: "+password+" does not exist"};
//            }
//        } else {
//            logger.error("SpringBean userdao cannot be instantiated");
//            res = new String[]{"1","Error-Internal Server - Spring Bean cannot be initialized"};
//        }
//        return res;
//    }


} //EoC