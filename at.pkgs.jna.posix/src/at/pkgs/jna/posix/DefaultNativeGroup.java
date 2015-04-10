/***** BEGIN LICENSE BLOCK *****
 * Version: CPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Common Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the CPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the CPL, the GPL or the LGPL.
 ***** END LICENSE BLOCK *****/

package at.pkgs.jna.posix;

import com.sun.jna.Pointer;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * The default native group layout.
 * 
 * <p>
 * This implementation should work on Solaris, Linux and MacOS.
 * </p>
 */
public final class DefaultNativeGroup extends NativeGroup implements Group {
    public String gr_name;   // name
    public String gr_passwd; // group password (encrypted)
    public int gr_gid;       // group id
    public Pointer gr_mem;
    
    DefaultNativeGroup(Pointer memory) {
        useMemory(memory);
        read();
    }
    
    public String getName() {
        return gr_name;
    }
    public String getPassword() {
        return gr_passwd;
    }
    public long getGID() {
        return gr_gid;
    }
    public String[] getMembers() {
        int size = Pointer.SIZE;
        int i=0;
        List<String> lst = new ArrayList<String>();
        while(gr_mem.getPointer(i) != null) {
            lst.add(gr_mem.getPointer(i).getString(0));
            i+=size;
        }
        return lst.toArray(new String[0]);
    }

    @Override
    protected List<?> getFieldOrder() {
        return Arrays.asList(new String[] { 
            "gr_name", "gr_passwd", "gr_gid", "gr_mem"});
    }
}
