package at.pkgs.jna.posix;

import at.pkgs.jna.posix.util.Platform;
import com.sun.jna.FromNativeContext;
import com.sun.jna.Pointer;
import java.io.FileDescriptor;

public final class LinuxPOSIX extends BaseNativePOSIX {
    private final boolean hasFxstat;
    private final boolean hasLxstat;
    private final boolean hasXstat;
    private final boolean hasFstat;
    private final boolean hasLstat;
    private final boolean hasStat;
    private final int statVersion;
    
    public LinuxPOSIX(String libraryName, LibC libc, POSIXHandler handler) {
        super(libraryName, libc, handler);

        statVersion = Platform.IS_32_BIT ? 3 : 0;

        /*
         * Most linux systems define stat/lstat/fstat as macros which force
         * us to call these weird signature versions.
         */
        hasFxstat = hasMethod("__fxstat64");
        hasLxstat = hasMethod("__lxstat64");
        hasXstat = hasMethod("__xstat64");
        
        /*
         * At least one person is using uLibc on linux which has real 
         * definitions for stat/lstat/fstat.
         */
        hasFstat = !hasFxstat && hasMethod("fstat64");
        hasLstat = !hasLxstat && hasMethod("lstat64");
        hasStat = !hasXstat && hasMethod("stat64");
    }
    
    @Override
    public FileStat allocateStat() {
        if (Platform.IS_32_BIT) {
            return new LinuxHeapFileStat(this);
        } else {
            return new Linux64HeapFileStat(this);
        }
    }

    @Override
    public FileStat fstat(FileDescriptor fileDescriptor) {
        if (!hasFxstat) {
            if (hasFstat) return super.fstat(fileDescriptor);
            
            handler.unimplementedError("fstat");
        }

        FileStat stat = allocateStat();
        int fd = helper.getfd(fileDescriptor);
        
        if (((LinuxLibC) libc).__fxstat64(statVersion, fd, stat) < 0) handler.error(ERRORS.ENOENT, "" + fd);
        
        return stat;
    }

    @Override
    public FileStat lstat(String path) {
        if (!hasLxstat) {
            if (hasLstat) return super.lstat(path);
            
            handler.unimplementedError("lstat");
        }

        FileStat stat = allocateStat();

        if (((LinuxLibC) libc).__lxstat64(statVersion, path, stat) < 0) handler.error(ERRORS.ENOENT, path);
        
        return stat;
    }

    @Override
    public FileStat stat(String path) {
        if (!hasXstat) {
            if (hasStat) return super.stat(path);
            
            handler.unimplementedError("stat");
        }
        
        FileStat stat = allocateStat(); 

        if (((LinuxLibC) libc).__xstat64(statVersion, path, stat) < 0) handler.error(ERRORS.ENOENT, path);
        
        return stat;
    }
    
    public static final PointerConverter PASSWD = new PointerConverter() {
        public Object fromNative(Object arg, FromNativeContext ctx) {
            return arg != null ? new LinuxPasswd((Pointer) arg) : null;
        }
    };
}
