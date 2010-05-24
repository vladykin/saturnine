package org.saturnine.cli.commands;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import org.saturnine.api.Changeset;
import org.saturnine.api.DirDiff;
import org.saturnine.api.PbException;
import org.saturnine.cli.PbCommand;
import org.saturnine.local.Changelog;
import org.saturnine.local.DirScanResult;
import org.saturnine.local.Dirlog;
import org.saturnine.local.LocalRepository;
import org.saturnine.local.WorkDir;

/**
 * @author Alexey Vladykin
 */
public class CommitCommand implements PbCommand {

    @Override
    public String getName() {
        return "commit";
    }

    @Override
    public String getDescription() {
        return "commit changes";
    }

    @Override
    public void execute(String[] args) throws PbException {
        LocalRepository repository = LocalRepository.find(new File("."));
        try {
            WorkDir workdir = repository.getWorkDir();

            DirScanResult scanResult = workdir.scan();
            if (scanResult.isClean()) {
                System.out.println("No changes to commit");
                return;
            }

            Changelog changelog = repository.getChangelog();
            Collection<Changeset> heads = changelog.getHeads();
            if (heads.size() != 1) {
                System.out.println("Too many heads");
                return;
            }
            Changeset primaryParent = heads.iterator().next();

            Dirlog dirlog = repository.getDirlog();
            Dirlog.Builder dirlogBuilder = dirlog.newBuilder();
            dirlogBuilder.oldState(primaryParent.id());
            for (Map.Entry<String, String> entry : scanResult.getAddedFiles().entrySet()) {
                String addedPath = entry.getKey();
                dirlogBuilder.addedFile(workdir.fileInfo(addedPath));
                String origin = entry.getValue();
                if (origin != null) {
                    dirlogBuilder.origin(addedPath, origin);
                }
            }
            for (String modifiedFile : scanResult.getModifiedFiles()) {
                dirlogBuilder.modifiedFile(workdir.fileInfo(modifiedFile));
            }
            for (String removedFile : scanResult.getRemovedFiles()) {
                dirlogBuilder.removedFile(removedFile);
            }
            DirDiff diff = dirlogBuilder.writeDiff();
            dirlogBuilder.close();

            Changelog.Builder changelogBuilder = changelog.newBuilder();
            changelogBuilder.id(diff.newState());
            changelogBuilder.primaryParent(primaryParent.id());
            changelogBuilder.secondaryParent(Changeset.NULL);
            changelogBuilder.author(System.getProperty("user.name"));
            changelogBuilder.comment("no comment");
            changelogBuilder.timestamp(new Date().getTime());
            changelogBuilder.writeChangeset();
            changelogBuilder.close();

            workdir.recordFileAttrs(dirlog.state(diff.newState()).keySet());

        } catch (IOException ex) {
            throw new PbException("IOException", ex);
        }
    }
}
