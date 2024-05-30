package gitlet;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.*;

public class RepoEx extends Repo {

    /** field for merge, the split point .*/
    private Commit _split;
    /** field for merge, the head commit.*/
    private Commit _head;
    /** the head commit of the branch .*/
    private Commit _headComm;
    /** Name of a file .*/
    private String file;


    /** Constructor for repoEx which inheritance from Repo.*/
    public RepoEx() {
        super();

    }
    /** init creates file .gitlet and initialze it and make the file repo.*/
    public void init() {
        String message;
        if (!new File(System.getProperty("user.dir"),
                ".gitlet").exists()) {
            get_hiddenGitlet().mkdir();
            get_Branchesss().mkdir();
            get_Blobs().mkdir();
            writeObject(get_currentStage(), get_stagingArea());
            Commit initialCommit = new Commit("initial commit",
                    null, null);
            getStringCommit().put(initialCommit.getSHAcomment(), initialCommit);
            writeObject(get_currCommits(), getStringCommit());
            writeContents(get_masterBranch(), initialCommit.getSHAcomment());
            writeContents(get_Head(), "master");
        } else {
            message = "A Gitlet version-control system already " +
                    "exists in the current directory.";
            message(message);
    return;
}
    }

    /** the add command... creates blobs and stores it in blobs .*/
    public void add(String fileName) {
        if (new File(get_currentWorkingDirec(), fileName).exists()) {

            File target = new File(get_currentWorkingDirec(), fileName);
            Commit toAdd = new Commit(target);
            File file1 = new File(get_Blobs(), toAdd.getSHAblob());
            set_stagingArea(getAreaStage());
            writeContents(file1, toAdd.getContents());
            Commit headCurrent = getHeadCurrent();

            if (!get_stagingArea().getRemove().contains(fileName)) {
            } else {
                get_stagingArea().getRemove().remove(fileName);
                writeObject(get_currentStage(), get_stagingArea());
            }
            if (!headCurrent.getBlob().containsValue(toAdd.getSHAblob())
                    || !headCurrent.getBlob().get(fileName).equals(toAdd.getSHAblob())) {
                get_stagingArea().add(fileName, toAdd.getSHAblob());
                writeObject(get_currentStage(), get_stagingArea());
            } else {
        set_stagingArea(getAreaStage());
        get_stagingArea().unStaged(fileName);
        writeObject(get_currentStage(), get_stagingArea());
        return;
    }
        } else {
            throw new GitletException("File does not exist.");
        }
    }

    /** commit command. its stores all new file in commit file*/
    public void commit(String message, String merg) {
        if (message.length() > 0) {
            set_stagingArea(getAreaStage());
            Commit newCommit = new Commit(message, getHeadCurrent().getSHAcomment(), merg);
            for (String key : getHeadCurrent().getBlob().keySet()) {
                if (!get_stagingArea().getRemove().contains(key)) {
                    newCommit.getBlob().put(key, getHeadCurrent().getBlob().get(key));
                }
            }
            if (get_stagingArea().getAdd().isEmpty() && get_stagingArea().getRemove().isEmpty()) {
                throw new GitletException("No changes added to the commit.");
            }
            for (String key : get_stagingArea().getAdd().keySet()) {
                String shaiD = get_stagingArea().getAdd().get(key);
                if (!newCommit.getBlob().containsValue(shaiD)) {
                    newCommit.getBlob().put(key, shaiD);
                }
            }
            setStringCommit(getStagingArea());
            getStringCommit().put(newCommit.getSHAcomment(), newCommit);
            writeObject(get_currCommits(), getStringCommit());
            ShaIdOfNewHead(newCommit.getSHAcomment());
            get_stagingArea().clean();
            writeObject(get_currentStage(), get_stagingArea());
        } else {
            throw new GitletException("Please enter a commit message.");
        }
    }
    /** Log command . displays commits date, sha1ID.*/
    public void log() {
        setStringCommit(getStagingArea());
        Commit current = getHeadCurrent();
        while (true) {
            if (current == null) {
                break;
            } else {
                SimpleDateFormat formatter = new
                        SimpleDateFormat("EEE MMM d HH:mm:ss Y Z");
                System.out.println("===");
                System.out.println("commit " + current.getSHAcomment());
                System.out.println("Date: " + formatter.format
                        (current.getDate()));
                System.out.println(current.getMessage());
                System.out.print("\n");
                if (current.getParentComment() != null) {
                    current = getStringCommit().get(current.getParentComment());
                } else {
                    break;
                }
            }
        }
    }

    /** 3 Check out --- checkout.*/
    public void checkout(String... args) {

        switch (args.length) {
            case 2 -> {
                String branchName = args[1];
                File branch = new File(get_Branchesss(), branchName);
                setStringCommit(getStagingArea());
                if (!new File(get_Branchesss(), branchName).exists()) {
                    throw new GitletException("No such branch exists.");
                }
                Commit branchHead = getStringCommit().get(readContentsAsString(branch));
                Commit currentHead = getHeadCurrent();
                for (String file : plainFilenamesIn(get_currentWorkingDirec())) {
                    if (!currentHead.getBlob().containsKey(file)) {
                        if (branchHead.getBlob().containsKey(file)) {
                            byte[] cwdFileContents =
                                    readContents(new File(get_currentWorkingDirec(), file));
                            byte[] overWriteContents =
                                    readContents(
                                            new File(get_Blobs(), branchHead.getBlob()
                                                    .get(file)));
                            if (!cwdFileContents.equals(overWriteContents)) {
                                throw new GitletException("There is an "
                                        + "untracked file in the way; delete it, "
                                        + "or add and commit it first.");
                            }
                        }
                    }
                }
                if (branchName.equals(readContentsAsString(get_Head()))) {
                    throw new GitletException("No need to checkout "
                            + "the current branch.");
                }
                Iterator<String> iterator = branchHead.getBlob().keySet().iterator();
                while (iterator.hasNext()) {
                    String file = iterator.next();
                    helperForBlobs(file, branchHead);
                }
                Iterator<String> iter = getHeadCurrent().getBlob().keySet().iterator();
                while (iter.hasNext()) {
                    String file = iter.next();
                    if (!branchHead.getBlob().containsKey(file)) {
                        restrictedDelete(file);
                    }
                }
                get_stagingArea().clean();
                writeContents(get_Head(), branchName);
            }
        }


        switch (args.length) {
            case 3 -> {
                String filename = args[2];
                Commit headCommit = getHeadCurrent();
                if (!headCommit.getBlob().containsKey(filename)) {
                    throw new GitletException("File does not "
                            + "exist in that commit.");
                }
                if (headCommit.getBlob().containsKey(filename)) {
                    helperForBlobs(filename, headCommit);
                }
            }
        }
        switch (args.length) {
            case 4 -> {
                String commitID = ShorterUID_LENGTH(args[1]);
                String filename = args[3];
                if (!args[2].equals("--")) {
                    throw new GitletException("Incorrect operands.");
                }
                setStringCommit(getStagingArea());
                Commit c = getStringCommit().get(commitID);
                if (c == null) {
                    throw new GitletException("No commit with that id exists.");
                }
                if (c.getBlob().containsKey(filename)) {
                    helperForBlobs(filename, c);
                } else {
                    throw new GitletException("File does not "
                            + "exist in that commit.");
                }
            }
        }
    }


    /** status -- displays out all brances, staged, unstaged and untracked files*/
    public void status() {
        ArrayList<String> branches = new ArrayList<>();
        List<String> plainFilenamesIn = plainFilenamesIn(get_Branchesss());
        {
            int i = 0;
            while (i < plainFilenamesIn.size()) {
                String branch = plainFilenamesIn.get(i);
                if (!branch.equals("HEAD")) {
                    if (branch.equals(readContentsAsString(get_Head()))) {
                        branches.add("*" + branch);
                    } else {
                        branches.add(branch);
                    }
                }
                i++;
            }
        }
        ArrayList<String> staged = new ArrayList<>();
        set_stagingArea(getAreaStage());
        {
            Iterator<String> iterator = get_stagingArea().getAdd().keySet().iterator();
            while (iterator.hasNext()) {
                String filename = iterator.next();
                staged.add(filename);
            }
        }
        ArrayList<String> removed = get_stagingArea().getRemove();
        ArrayList<String> unstaged = new ArrayList<>();
        List<String> filenamesIn = plainFilenamesIn(get_currentWorkingDirec());
        {
            int i = 0;
            while (i < filenamesIn.size()) {
                String file = filenamesIn.get(i);
                byte[] cwdContents = readContents(new File(get_currentWorkingDirec(), file));
                if (getHeadCurrent().getBlob().containsKey(file) && new File(get_Blobs(),
                        getHeadCurrent().getBlob().get(file)).exists()) {
                    byte[] commitContents = readContents(new File(get_Blobs(),
                            getHeadCurrent().getBlob().get(file)));
                    if (!Arrays.equals(cwdContents, commitContents)
                            && !get_stagingArea().getAdd().containsKey(file)) {
                        unstaged.add(file + " (modified)");
                    }
                }
                if (get_stagingArea().getAdd().containsKey(file) && !staged.contains(file)
                        && !cwdContents.equals(readContents(new File(get_Blobs(),
                        get_stagingArea().getAdd().get(file))))) {
                    unstaged.add(file + " (modified)");
                }
                i++;
            }
        }
        Iterator<String> iterator = get_stagingArea().getAdd().keySet().iterator();
        while (iterator.hasNext()) {
            String file = iterator.next();
            if (!plainFilenamesIn(get_currentWorkingDirec()).contains(file)) {
                unstaged.add(file + " (deleted)");
            }
        }
        Iterator<String> iter = getHeadCurrent().getBlob().keySet().iterator();
        while (iter.hasNext()) {
            String file = iter.next();
            if (!plainFilenamesIn(get_currentWorkingDirec()).contains(file)
                    && !get_stagingArea().getRemove().contains(file)) {
                unstaged.add(file + " (deleted)");
            }
        }

        ArrayList<String> untracked = new ArrayList<>();
        plainFilenamesIn = plainFilenamesIn(get_currentWorkingDirec());
        int i = 0;
        while (i < plainFilenamesIn.size()) {
            String file = plainFilenamesIn.get(i);
            if (!getHeadCurrent().getBlob().containsKey(file)
                    && !get_stagingArea().getAdd().containsKey(file)) {
                untracked.add(file);
            }
            i++;
        }
        System.out.println("=== Branches ===");
        int j = 0;
        while (j < branches.size()) {
            String branch = branches.get(j);
            System.out.println(branch);
            j++;
        }
        System.out.print("\n");
        System.out.println("=== Staged Files ===");
        int k = 0;
        while (k < staged.size()) {
            String file = staged.get(k);
            System.out.println(file);
            k++;
        }
        System.out.print("\n");
        System.out.println("=== Removed Files ===");
        int i1 = 0;
        while (i1 < removed.size()) {
            String file = removed.get(i1);
            System.out.println(file);
            i1++;
        }
        System.out.print("\n");
        System.out.println("=== Modifications Not Staged For Commit ===");
        int i2 = 0;
        while (i2 < unstaged.size()) {
            String file = unstaged.get(i2);
            System.out.println(file);
            i2++;
        }
        System.out.print("\n");
        System.out.println("=== Untracked Files ===");
        int i3 = 0;
        while (i3 < untracked.size()) {
            System.out.println(file);
            i3++;
        }
        System.out.print("\n");
    }


    /** Merge
     * @param branchs -
     */
    public void merge(String branchs) {

        if (new File(get_Branchesss(), branchs).exists()) {
            if (!branchs.equals(readContentsAsString(get_Head()))) {
                set_stagingArea(getAreaStage());
                if (get_stagingArea().getAdd().keySet().isEmpty()
                        && get_stagingArea().getRemove().isEmpty()) {
                    setStringCommit(getStagingArea());
                    Commit headL = getHeadCurrent();
                    Commit mergeHead = getStringCommit().get(readContentsAsString
                            (new File(get_Branchesss(), branchs)));
                    Commit common = getStringCommit().get(split(headL, mergeHead));
                    if (!common.getSHAcomment().equals(mergeHead.getSHAcomment())) {
                        if (helpers(headL).contains(mergeHead.getSHAcomment())
                                || helpers(mergeHead).contains(headL.getSHAcomment())) {
                            checkout("checkout", branchs);
                            System.out.println("Current branch fast-forwarded.");
                            return;
                        }
                        List<String> plainFilenamesIn = plainFilenamesIn(get_currentWorkingDirec());
                        {
                            int i = 0;
                            while (i < plainFilenamesIn.size()) {
                                String file = plainFilenamesIn.get(i);
                                if (!getHeadCurrent().getBlob().containsKey(file)
                                        && mergeHead.getBlob().containsKey(file)) {
                                    throw new GitletException("There is an untracked file in "
                                            + "the way; delete it, or add and commit it first.");
                                }
                                i++;
                            }
                        }
                        Set<String> allFiles = new TreeSet<>(headL.getBlob().keySet());
                        allFiles.addAll(mergeHead.getBlob().keySet());
                        allFiles.addAll(common.getBlob().keySet());
                        int i = 0;
                        Iterator<String> iterator = allFiles.iterator();
                        while (iterator.hasNext()) {
                            String file = iterator.next();
                            this._split = common;
                            this._head = headL;
                            this._headComm = mergeHead;
                            this.file = file;

                            boolean occured = merge2();
                            if (occured) {
                                i++;
                            }
                        }
                        boolean conflict = i > 0;
                        if (!get_stagingArea().getAdd().isEmpty() || !get_stagingArea().getRemove().isEmpty()) {
                            commit("Merged " + branchs + " into "
                                    + readContentsAsString(get_Head()) + ".", mergeHead.getSHAcomment());
                            if (conflict) {
                                System.out.println("Encountered a merge conflict.");
                            }
                        } else {
                            throw new GitletException("No changes added to the commit.");
                        }
                    } else {
                        throw new GitletException("Given branch is an "
                                + "ancestor of the current branch.");
                    }
                } else {
            throw new GitletException("You have uncommitted changes.");
        }
            } else {
                throw new GitletException("Cannot merge a branch with itself.");
            }
        } else {
            throw new GitletException("A branch with that "
                    + "name does not exist.");
        }
    }
        /**  MERGE */
    public boolean merge2() {
        set_stagingArea(getAreaStage());
        if (!spl().containsKey(file)) {
            boolean st = true;
        } else {
            if (!spl().get(file).equals(hea().get(file)) || spl().get(file)
                    .equals(oth().get(file)) || !oth().containsKey(file)) {
                if (spl().get(file).equals(oth().get(file))
                        && !spl().get(file).equals(hea().get(file))
                        && hea().containsKey(file)) {
                    return false;
                }
                if ((!hea().containsKey(file) || !oth().containsKey(file)
                        || !hea().get(file).equals(oth().get(file)))
                        && (hea().containsKey(file) || oth()
                        .containsKey(file))) {
                    if (spl().get(file).equals(hea().get(file)) && !oth()
                            .containsKey(file)) {
                        rm(file);
                        writeObject(get_currentStage(), get_stagingArea());
                        return false;
                    }
                    if (spl().get(file).equals(oth().get(file)) && !hea()
                            .containsKey(file)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                checkout("checkout", _headComm.getSHAcomment(), "--", file);
                get_stagingArea().add(file, oth().get(file));
                writeObject(get_currentStage(), get_stagingArea());
                return false;
            }
        }
        if (spl().containsKey(file)) {
            boolean st = true;
        } else {
            if (hea().containsKey(file) && !oth().containsKey(file)) {
                return false;
            }
            if (hea().containsKey(file) || !oth().containsKey(file)) {
                boolean st = true;
            } else {
                checkout("checkout", _headComm.getSHAcomment(), "--", file);
                get_stagingArea().add(file, oth().get(file));
                writeObject(get_currentStage(), get_stagingArea());
                return false;
            }
        }
        if ((!spl().containsKey(file) || spl().get(file)
                .equals(hea().get(file))
                || spl().get(file).equals(oth().get(file))
                || !hea().containsKey(file)
                || !oth().containsKey(file) || hea().get(file)
                .equals(oth().get(file)))
                && (!spl().containsKey(file) || !hea().containsKey(file)
                || spl().get(file).equals(hea().get(file))
                || oth().containsKey(file)) && (!spl().containsKey(file)
                || !oth().containsKey(file)
                || spl().get(file).equals(oth().get(file))
                || hea().containsKey(file))
                && (spl().containsKey(file)
                || hea().get(file).equals(oth().get(file)))) {
            writeObject(get_currentStage(), get_stagingArea());
            return false;
        } else {
            String headC, otherC;
            headC = hea().containsKey(file) ? readContentsAsString
                    (new File(get_Blobs(), hea().get(file))) : "";
            otherC = oth().containsKey(file) ? readContentsAsString
                    (new File(get_Blobs(), oth().get(file))) : "";
            File conflicted = new File(get_currentWorkingDirec(), file);
            String contents = "<<<<<<< HEAD" + "\n" + headC
                + "=======" + "\n" + otherC + ">>>>>>>" + "\n";
            writeContents(conflicted, contents);
            Commit newBlob = new Commit(conflicted);
            get_stagingArea().add(file, newBlob.getSHAblob());
            writeObject(get_currentStage(), get_stagingArea());
            return true;
        }
    }

    /**
     * helper method for merge.
     * return - spl
     */
    public HashMap<String, String> spl() {
        HashMap<String, String> spl;
        spl = _split.getBlob();
        return spl;
    }
    /**
     * helper method for merge.
     * @return - hea
     */
    public HashMap<String, String> hea() {
        HashMap<String, String> hea;
        hea = _head.getBlob();
        return hea;
    }
    /**
     * helper method for merge.
     * @return - _headcomm.getblob().
     */
    public HashMap<String, String> oth() {
        HashMap<String, String> oth;
        oth = _headComm.getBlob();
        return oth;
    }
    /** Staging Area.
     * @return - */
    public Commit getAreaStage() {
        return readObject(get_currentStage(), Commit.class);
    }

    /**
     * @return - hiddengitlet field
     */
    public File getRepo() {
        return get_hiddenGitlet();
    }
}