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
import org.saturnine.util.HexCharSequence;

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
        if (args.length <= 0) {
            throw new PbException("Please specify commit message");
        }

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
            if (1 < heads.size()) {
                System.out.println("Too many heads");
                return;
            }
            HexCharSequence primaryParent = heads.isEmpty()? Changeset.NULL : heads.iterator().next().id();

            Dirlog dirlog = repository.getDirlog();
            Dirlog.Builder dirlogBuilder = dirlog.newBuilder();
            dirlogBuilder.oldState(primaryParent);
            for (Map.Entry<String, String> entry : scanResult.getAddedFiles().entrySet()) {
                String addedPath = entry.getKey();
                System.out.println("Checksumming " + addedPath);
                dirlogBuilder.addedFile(workdir.fileInfo(addedPath));
                String origin = entry.getValue();
                if (origin != null) {
                    dirlogBuilder.origin(addedPath, origin);
                }
            }
            for (String modifiedFile : scanResult.getModifiedFiles()) {
                System.out.println("Checksumming " + modifiedFile);
                dirlogBuilder.modifiedFile(workdir.fileInfo(modifiedFile));
            }
            for (String removedFile : scanResult.getRemovedFiles()) {
                dirlogBuilder.removedFile(removedFile);
            }
            DirDiff diff = dirlogBuilder.writeDiff();
            dirlogBuilder.close();

            Changelog.Builder changelogBuilder = changelog.newBuilder();
            changelogBuilder.id(diff.newState());
            changelogBuilder.primaryParent(primaryParent);
            //changelogBuilder.secondaryParent(Changeset.NULL);
            changelogBuilder.author(getAuthor(repository));
            changelogBuilder.comment(getComment(args));
            changelogBuilder.timestamp(new Date().getTime());
            changelogBuilder.writeChangeset();
            changelogBuilder.close();

            workdir.recordFileAttrs(dirlog.state(diff.newState()).keySet(), true);

        } catch (IOException ex) {
            throw new PbException("IOException", ex);
        }
    }

    private static String getAuthor(LocalRepository repository) {
        String author = repository.getProperty(LocalRepository.PROP_USER);
        if (author == null) {
            author = System.getProperty("user.name");
            if (author == null) {
                author = "Anonymous";
            }
        }
        return author;
    }

    private static String getComment(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            if (0 < sb.length()) {
                sb.append(' ');
            }
            sb.append(arg);
        }
        return sb.toString();
    }
}
