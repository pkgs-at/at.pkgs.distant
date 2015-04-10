package at.pkgs.jna.posix;

import com.sun.jna.Structure;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SolarisFileStat extends BaseNativeFileStat {
    public static class TimeStruct extends Structure {
        public volatile int tv_sec;
        public volatile int tv_nsec;

        @Override
        protected List getFieldOrder() {
            return Arrays.asList(new String[] { "tv_sec", "tv_nsec" });
        }
    }
    public volatile int st_dev;
    public volatile int[] st_pad1;
    public volatile int st_ino;
    public volatile int st_mode;
    public volatile int st_nlink;
    public volatile int st_uid;
    public volatile int st_gid;
    public volatile int st_rdev;
    public volatile int[] st_pad2;
    public volatile int st_size;
    public volatile int st_pad3;
    public volatile TimeStruct st_atim;
    public volatile TimeStruct st_mtim;
    public volatile TimeStruct st_ctim;
    public volatile int st_blksize;
    public volatile int st_blocks;
    public volatile int pad7;
    public volatile int pad8;
    public volatile String st_fstype;
    public volatile int[] st_pad4;

    public SolarisFileStat(POSIX posix) {
        super(posix);
        
        st_pad1 = new int[3];
        st_pad2 = new int[2];
        st_pad4 = new int[8];
    }

    public long atime() {
        return st_atim.tv_sec;
    }

    public long blocks() {
        return st_blocks;
    }

    public long blockSize() {
        return st_blksize;
    }

    public long ctime() {
        return st_ctim.tv_sec;
    }

    public long dev() {
        return st_dev;
    }

    public int gid() {
        return st_gid;
    }

    public long ino() {
        return st_ino;
    }

    public int mode() {
        return st_mode;
    }

    public long mtime() {
        return st_mtim.tv_sec;
    }

    public int nlink() {
        return st_nlink;
    }

    public long rdev() {
        return st_rdev;
    }

    public long st_size() {
        return st_size;
    }

    public int uid() {
        return st_uid;
    }

    @Override
    protected List getFieldOrder() {
        return Arrays.asList(new String[] { 
            "st_dev", "st_pad1", "st_ino", "st_mode", "st_nlink", "st_uid",
            "st_gid", "st_rdev", "st_pad2", "st_size", "st_pad3", "st_atim",
            "st_mtim", "st_ctim", "st_blksize", "st_blocks", "pad7", "pad8",
            "st_fstype", "st_pad4"});
    }
}
