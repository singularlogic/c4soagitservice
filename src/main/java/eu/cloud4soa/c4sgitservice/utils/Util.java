package eu.cloud4soa.c4sgitservice.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: pgouvas
 * Date: 8/21/12
 * Time: 12:54 PM
 * To change this template use File | Settings | File Templates.
 */
@Component
public class Util {

    protected final Log logger = LogFactory.getLog(getClass());

    public static String readfile(String filename) throws Exception {
        String ret="";
        File file = new File(filename);
        int ch;
        StringBuffer strContent = new StringBuffer("");
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);
            while ((ch = fin.read()) != -1) strContent.append((char) ch);
            fin.close();
        } catch (Exception e) {
            throw e;
        }
        ret=strContent.toString();
        return ret;
    }

    public static String createAuthorizedKeysSegment(String proxyexecutable, String key, String pubkey){
        //TODO add this command="/home/ubitech/proxy-git username",no-X11-forwarding,no-pty,no-port-forwarding,no-agent-forwarding
        String ret="";
        String command = " command=\""+proxyexecutable+" username\",no-X11-forwarding,no-pty,no-port-forwarding,no-agent-forwarding ";
        ret="#"+key+"\n"+command+" "+pubkey+"\n"+"#"+key+"end"+"\n";
        return ret;
    }

    public static String createProxyGitSegment(String proxyid, String proxyname, String url, String reponame){
        String ret="";
        ret="#proxy"+proxyid+"                                                                                             \n" +
             "REPO=\""+reponame+"\"                                                                                         \n" +
             "if [ \"$repo\" == \"'"+proxyname+"'\" ]; then                                                                 \n" +
             "        exec /usr/bin/ssh -oBatchMode=yes -i \"$C4SSERVER_KEY\" "+url+" \"$cmd\" \"'$REPO'\"                  \n" +
             "fi                                                                                                            \n" +
             "#proxy"+proxyid+"end                                                                                         \n";
        return ret;
    }


    public static synchronized void appendToFile(String filename, String text){
        FileWriter fWriter = null;
        BufferedWriter writer = null;
        try {
            fWriter = new FileWriter(filename,true);
            writer = new BufferedWriter(fWriter);
            writer.append(text);
            writer.newLine();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String createSedBlockForAuthorizedKeys(String pubkeyid){
        String ret="";
        ret= " /^#"+pubkeyid+"/,/"+"#"+pubkeyid+"end"+"/d ";
        return ret;
    }


    public static String createSedBlockForProxyGit(String tag){
        String ret="";
        ret= " /^#proxy"+tag+"/,/"+"#proxy"+tag+"end"+"/d ";
        return ret;
    }

    public static synchronized int replaceBlockWithSedForAuthorizedKeys(String filename, String pubkeyid){
        int retvalue = 1; //1=ERROR 0=OK

        try {
            String sedblock = createSedBlockForAuthorizedKeys(pubkeyid);
            System.out.println(" sed -e "+sedblock +" "+filename );
            String[] command = new String[] {"sed" ,
                    "-i"        ,  // -i is for replace // -e is used for piping
                    sedblock    ,
                    filename
            };
            System.out.println(command.toString());
            Process child = Runtime.getRuntime().exec(command);
            child.waitFor();
            //Get the input stream and read from it
            InputStream in = child.getInputStream();
            int c;
            while ((c = in.read()) != -1) {
                System.out.print((char)c);
            }
            in.close();
            retvalue = child.exitValue();
            System.out.println("Replace file returned: "+retvalue);
        } catch (Exception ex){
              ex.printStackTrace();
        }
        return retvalue;
    }

    public static synchronized int replaceBlockWithSedForProxyGit(String filename, String tag){
        int retvalue = 1; //1=ERROR 0=OK

        try {
            String sedblock = createSedBlockForProxyGit(tag);
            System.out.println(" sed -e "+sedblock +" "+filename );
            String[] command = new String[] {"sed" ,
                    "-i"        ,  // -i is for replace // -e is used for piping
                    sedblock    ,
                    filename
            };
            System.out.println(command.toString());
            Process child = Runtime.getRuntime().exec(command);
            child.waitFor();
            //Get the input stream and read from it
            InputStream in = child.getInputStream();
            int c;
            while ((c = in.read()) != -1) {
                System.out.print((char)c);
            }
            in.close();
            retvalue = child.exitValue();
            System.out.println("Replace file returned: "+retvalue);
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return retvalue;
    }


}
