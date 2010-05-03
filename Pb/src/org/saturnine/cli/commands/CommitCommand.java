package org.saturnine.cli.commands;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import org.saturnine.api.DirDiff;
import org.saturnine.api.PbException;
import org.saturnine.cli.PbCommand;
import org.saturnine.local.Changelog;
import org.saturnine.local.DirScanResult;
import org.saturnine.local.DirState;
import org.saturnine.local.Dirlog;
import org.saturnine.local.LocalRepository;

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
            DirState dirstate = repository.getDirState();
            DirState.Snapshot snapshot = dirstate.snapshot();

            DirScanResult scanResult = snapshot.scanDir();
            if (scanResult.isClean()) {
                System.out.println("No changes to commit");
                return;
            }

            Dirlog dirlog = repository.getDirlog();
            Dirlog.Builder dirlogBuilder = dirlog.newBuilder();
            dirlogBuilder.oldState(snapshot.primaryParent());
            for (String addedPath : scanResult.getAddedFiles()) {
                dirlogBuilder.addedFile(repository.fileInfo(addedPath));
                String origin = scanResult.getCopyOf(addedPath);
                if (origin != null) {
                    dirlogBuilder.origin(addedPath, origin);
                }
            }
            for (String modifiedFile : scanResult.getModifiedFiles()) {
                dirlogBuilder.modifiedFile(repository.fileInfo(modifiedFile));
            }
            for (String removedFile : scanResult.getRemovedFiles()) {
                dirlogBuilder.removedFile(removedFile);
            }
            DirDiff diff = dirlogBuilder.closeDiff();
            dirlogBuilder.close();

            Changelog changelog = repository.getChangelog();
            Changelog.Builder changelogBuilder = changelog.newBuilder();
            changelogBuilder.id(diff.newState());
            changelogBuilder.primaryParent(snapshot.primaryParent());
            changelogBuilder.secondaryParent(snapshot.secondaryParent());
            changelogBuilder.author(System.getProperty("user.name"));
            changelogBuilder.comment("no comment");
            changelogBuilder.timestamp(new Date().getTime());
            changelogBuilder.closeChangeset();
            changelogBuilder.close();

        } catch (IOException ex) {
            throw new PbException("IOException", ex);
        }
    }
}
