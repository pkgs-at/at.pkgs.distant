/*
 * POSIXFunctionMapper.java
 */

package at.pkgs.jna.posix;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.sun.jna.FunctionMapper;
import com.sun.jna.NativeLibrary;

public class WindowsLibCFunctionMapper implements FunctionMapper {
    private Map<String, String> methodNameMap;

    public WindowsLibCFunctionMapper() {
        methodNameMap = new HashMap<String, String>();
        
        methodNameMap.put("getpid", "_getpid");
        methodNameMap.put("chmod", "_chmod");
        methodNameMap.put("fstat", "_fstat");
        methodNameMap.put("stat", "_stat64");
        methodNameMap.put("mkdir", "_mkdir");
        methodNameMap.put("umask", "_umask");
	methodNameMap.put("isatty", "_isatty");
    }
  
    public String getFunctionName(NativeLibrary library, Method method) {
        String originalName = method.getName();
        String name = methodNameMap.get(originalName);
        
        return name != null ? name : originalName; 
    }
}
