package org.saturnine.cli.commands;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.saturnine.api.Changeset;
import org.saturnine.api.FileInfo;
import org.saturnine.api.PbException;
import org.saturnine.cli.PbCommand;
import org.saturnine.local.DirScanResult;
import org.saturnine.local.LocalRepository;
import org.saturnine.local.WorkDir;
import org.saturnine.util.HexCharSequence;

/**
 * @author Alexey Vladykin
 */
public class StatusCommand implements PbCommand {

    @Override
    public String getName() {
        return "status";
    }

    @Override
    public String getDescription() {
        return "show uncommitted changes";
    }

    @Override
    public void execute(String[] args) throws PbException {
        LocalRepository repository = LocalRepository.find(new File("."));
        try {
            final WorkDir workDir = repository.getWorkDir();
            DirScanResult scanResult = workDir.scan();

            if (!scanResult.getUncertainFiles().isEmpty()) {
                Collection<Changeset> heads = repository.getChangelog().getHeads();
                if (1 < heads.size()) {
                    throw new PbException("Too many heads: " + heads);
                }

                HexCharSequence headId = heads.isEmpty()? Changeset.NULL : heads.iterator().next().id();
                Map<String, FileInfo> state = repository.getDirlog().state(headId);
                classifyUncertain(scanResult, workDir, state);
            }

            for (String path : sort(scanResult.getAddedFiles().keySet())) {
                System.out.println("A " + path);
            }
            for (String path : sort(scanResult.getRemovedFiles())) {
                System.out.println("R " + path);
            }
            for (String path : sort(scanResult.getMissingFiles())) {
                System.out.println("! " + path);
            }
            for (String path : sort(scanResult.getUntrackedFiles())) {
                System.out.println("? " + path);
            }
            for (String path : sort(scanResult.getModifiedFiles())) {
                System.out.println("M " + path);
            }
        } catch (IOException ex) {
            throw new PbException("IOException", ex);
        }
    }

    private static Collection<String> sort(Collection<String> orig) {
        return new TreeSet<String>(orig);
    }

    /*package*/ static void classifyUncertain(DirScanResult scanResult, WorkDir workDir, Map<String, FileInfo> expectedState) throws IOException {
        Set<String> cleanFiles = new TreeSet<String>();
        for (String path : scanResult.getUncertainFiles()) {
            System.out.println("Verifying " + path);
            FileInfo fileInfo = workDir.fileInfo(path);
            if (fileInfo.equals(expectedState.get(path))) {
                cleanFiles.add(path);
            } else {
                scanResult.getModifiedFiles().add(path);
            }
        }
        if (!cleanFiles.isEmpty() && workDir.isWritable()) {
            workDir.recordFileAttrs(cleanFiles, false);
        }
        scanResult.getUncertainFiles().clear();
    }
}
