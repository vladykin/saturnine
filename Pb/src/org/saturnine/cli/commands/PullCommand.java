package org.saturnine.cli.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.saturnine.api.Changeset;
import org.saturnine.api.DirDiff;
import org.saturnine.api.FileInfo;
import org.saturnine.api.PbException;
import org.saturnine.cli.PbCommand;
import org.saturnine.local.Changelog;
import org.saturnine.local.DirState;
import org.saturnine.local.Dirlog;
import org.saturnine.local.LocalRepository;

/**
 * @author Alexey Vladykin
 */
public class PullCommand implements PbCommand {

    @Override
    public String getName() {
        return "pull";
    }

    @Override
    public String getDescription() {
        return "pull changesets from parent repository";
    }

    @Override
    public void execute(String[] args) throws PbException {
        LocalRepository repository = LocalRepository.find(new File("."));

        String parentPath = repository.getProperty(LocalRepository.PROP_PARENT);
        if (parentPath == null) {
            throw new PbException("No parent repository specified");
        }

        LocalRepository parent = LocalRepository.open(new File(parentPath));

        pull(parent, repository);
    }

    /*package*/ static void pull(LocalRepository parent, LocalRepository child) throws PbException {
        Changelog parentChangelog = parent.getChangelog();
        Dirlog parentDirlog = parent.getDirlog();
        Changelog childChangelog = child.getChangelog();
        Dirlog childDirlog = child.getDirlog();
        List<String> newChangesets = new ArrayList<String>();

        try {
            Changelog.Reader parentChangelogReader = parentChangelog.newReader();
            try {
                for (Changeset changeset = parentChangelogReader.next(); changeset != null; changeset = parentChangelogReader.next()) {
                    if (!childChangelog.hasChangeset(changeset.id())) {
                        newChangesets.add(changeset.id());
                    }
                }
            } finally {
                parentChangelogReader.close();
            }

            if (newChangesets.isEmpty()) {
                System.out.println("No new changesets found");
                return;
            }

            transplantDiffs(parentDirlog, childDirlog, newChangesets);
            transplantChangesets(parentChangelog, childChangelog, newChangesets);

            String tip = newChangesets.get(newChangesets.size() - 1);
            DirState dirstate = child.getDirState();
            DirState.Builder dirstateBuilder = dirstate.newBuilder(false);
            dirstateBuilder.knownFiles(childDirlog.state(tip));
            dirstateBuilder.close();

        } catch (IOException ex) {
            throw new PbException("IOException", ex);
        }
    }

    private static void transplantDiffs(Dirlog src, Dirlog dst, List<String> ids) throws IOException {
        Dirlog.Reader reader = src.newReader();
        Dirlog.Builder builder = dst.newBuilder();
        try {
            int expectedIdPos = 0;
            while (expectedIdPos < ids.size()) {
                DirDiff diff = reader.next();
                if (diff == null) {
                    throw new IOException("Reached eof while looking for " + ids.get(expectedIdPos));
                }
                if (diff.newState().equals(ids.get(expectedIdPos))) {
                    ++expectedIdPos;
                    builder.oldState(diff.oldState());
                    builder.newState(diff.newState());
                    for (FileInfo addedFile : diff.addedFiles().values()) {
                        builder.addedFile(addedFile);
                    }
                    for (FileInfo modifiedFile : diff.modifiedFiles().values()) {
                        builder.modifiedFile(modifiedFile);
                    }
                    for (String removedFile : diff.removedFiles()) {
                        builder.removedFile(removedFile);
                    }
                    builder.writeDiff();
                }
            }
        } finally {
            reader.close();
            builder.close();
        }
    }

    private static void transplantChangesets(Changelog src, Changelog dst, List<String> ids) throws IOException {
        Changelog.Reader reader = src.newReader();
        Changelog.Builder builder = dst.newBuilder();
        try {
            int expectedIdPos = 0;
            while (expectedIdPos < ids.size()) {
                Changeset changeset = reader.next();
                if (changeset == null) {
                    throw new IOException("Reached eof while looking for " + ids.get(expectedIdPos));
                }
                if (changeset.id().equals(ids.get(expectedIdPos))) {
                    ++expectedIdPos;
                    builder.id(changeset.id());
                    builder.primaryParent(changeset.primaryParent());
                    builder.secondaryParent(changeset.secondaryParent());
                    builder.author(changeset.author());
                    builder.comment(changeset.comment());
                    builder.timestamp(changeset.timestamp());
                    builder.writeChangeset();
                }
            }
        } finally {
            reader.close();
            builder.close();
        }
    }
}
