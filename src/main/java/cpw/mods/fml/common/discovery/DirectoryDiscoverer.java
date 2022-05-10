package cpw.mods.fml.common.discovery;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import cpw.mods.fml.common.*;
import cpw.mods.fml.common.discovery.asm.ASMModParser;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;

public class DirectoryDiscoverer implements ITypeDiscoverer {
    private ASMDataTable table;

    public DirectoryDiscoverer() {
    }

    public List<ModContainer> discover(ModCandidate candidate, ASMDataTable table) {
        this.table = table;
        List<ModContainer> found = Lists.newArrayList();
        FMLLog.fine("Examining directory %s for potential mods", candidate.getModContainer().getName());
        exploreFileSystem("", candidate.getModContainer(), found, candidate, null);
        for (ModContainer mc : found)
        {
            table.addContainer(mc);
        }
        return found;
    }

    public void exploreFileSystem(String path, File modDir, List<ModContainer> harvestedMods, ModCandidate candidate, MetadataCollection mc) {
        if (path.length() == 0)
        {
            File metadata = new File(modDir, "mcmod.info");
            try
            {
                FileInputStream fis = new FileInputStream(metadata);
                mc = MetadataCollection.from(fis,modDir.getName());
                fis.close();
                FMLLog.fine("Found an mcmod.info file in directory %s", modDir.getName());
            }
            catch (Exception e)
            {
                mc = MetadataCollection.from(null,"");
                FMLLog.fine("No mcmod.info file found in directory %s", modDir.getName());
            }
        }

        File[] content = modDir.listFiles(new ClassFilter());

        // Always sort our content
        Arrays.sort(content);
        for (File file : content)
        {
            if (file.isDirectory())
            {
                FMLLog.finest("Recursing into package %s", path + file.getName());
                exploreFileSystem(path + file.getName() + ".", file, harvestedMods, candidate, mc);
                continue;
            }
            Matcher match = classFile.matcher(file.getName());

            if (match.matches())
            {
                ASMModParser modParser = null;
                try
                {
                    FileInputStream fis = new FileInputStream(file);
                    modParser = new ASMModParser(fis);
                    fis.close();
                }
                catch (LoaderException e)
                {
                    FMLLog.log(Level.SEVERE, e, "There was a problem reading the file %s - probably this is a corrupt file", file.getPath());
                    throw e;
                }
                catch (Exception e)
                {
                    Throwables.propagate(e);
                }

                modParser.validate();
                modParser.sendToTable(table, candidate);
                ModContainer container = ModContainerFactory.instance().build(modParser, candidate.getModContainer(), candidate);
                if (container!=null)
                {
                    harvestedMods.add(container);
                    container.bindMetadata(mc);
                }
            }


        }
    }

    private class ClassFilter implements FileFilter {
        private ClassFilter() {
        }

        public boolean accept(File file) {
            return file.isFile() && ITypeDiscoverer.classFile.matcher(file.getName()).find() || file.isDirectory();
        }
    }
}
