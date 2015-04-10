
package at.pkgs.jna.posix;

import com.sun.jna.Pointer;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class SolarisPasswd extends NativePasswd implements Passwd {
    public String pw_name;   // user name
    public String pw_passwd; // password (encrypted)
    public int pw_uid;       // user id
    public int pw_gid;       // user id
    public Pointer pw_age;   // unused
    public Pointer pw_comment;// unused
    public String pw_gecos;  // login info
    public String pw_dir;    // home directory
    public String pw_shell;  // default shell
    
    SolarisPasswd(Pointer memory) {
        useMemory(memory);
        read();
    }
    
    public String getAccessClass() {
        return "unknown";
    }
    public String getGECOS() {
        return pw_gecos;
    }
    public long getGID() {
        return pw_gid;
    }
    public String getHome() {
        return pw_dir;
    }
    public String getLoginName() {
        return pw_name;
    }
    public int getPasswdChangeTime() {
        return 0;
    }
    public String getPassword() {
        return pw_passwd;
    }
    public String getShell() {
        return pw_shell;
    }
    public long getUID() {
        return pw_uid;
    }
    public int getExpire() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected List<?> getFieldOrder() {
        return Arrays.asList(new String[] { 
            "pw_name", "pw_passwd", "pw_uid", "pw_gid",
            "pw_age", "pw_comment", "pw_gecos", "pw_dir",
            "pw_shell"});
    }
}
