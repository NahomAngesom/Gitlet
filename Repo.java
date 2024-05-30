package gitlet;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import static gitlet.Utils.*;
import static java.util.Collections.reverse;

/** Repo and  classes need for the repo to work.
 * @author Nahom Ghebreselasie */
public class Repo {

    /** Current Working Directory.*/
    private File _currentWorkingDirec = new
            File(System.getProperty("user.dir"));
    /** Hidden .gitlet.*/
    private File _hiddenGitlet = new
            File(_currentWorkingDirec, ".gitlet");
    /** current state of the staging area in the repo. */
    private File _currentStage = new
            File(_hiddenGitlet, "staging" + ".txt");
    /** StagingArea object.*/
    private Commit _stagingArea;
    /** The current state of the
     * _commits is stored here. */
    private File _currCommits =
            new File(_hiddenGitlet, "commits");
    /** Commit objects are mapped to sha1 IDs.*/
    private TreeMap<String, Commit>
            stringCommit;
    /** branch's head commits are stored in a directory.*/
    private File _Branchesss =
            new File(_hiddenGitlet, "branches");
    /** stores the sha1 of the commit. */
    private  File _Head =
            new File(_Branchesss, "HEAD");
    /** master branch. */
    private File _masterBranch =
            new File(_Branchesss, "master");
    /** Blobs are saved in this file. */
    private File _Blobs =
            new File(_hiddenGitlet, "blobs");

    /** Usage: java gitlet.Main ARGS, where ARGS contains*/
    public static void input(String... args) {
        RepoEx Git = new RepoEx();

        if (args.length >= 1) {
            if (!args[0].equals("init") && !Git.getRepo().exists()) {
                throw new
                        GitletException("Not in an initialized Gitlet directory.");
            }

            if ("init".equals(args[0])) {
                Git.init();

            } else if ("add".equals(args[0])) {
                Git.add(args[1]);

            } else if ("commit".equals(args[0])) {
                Git.commit(args[1], null);

            } else if ("log".equals(args[0])) {
                Git.log();

            } else if ("global-log".equals(args[0])) {
                Git.globalLog();

            } else if ("find".equals(args[0])) {
                Git.find(args[1]);

            } else if ("status".equals(args[0])) {
                Git.status();

            } else if ("checkout".equals(args[0])) {
                if (args.length != 3
                        && args.length != 4 && args.length != 2) {
                    throw new GitletException("Incorrect operands.");
                }
                Git.checkout(args);

            } else if ("branch".equals(args[0])) {
                Git.branch(args[1]);

            } else if ("rm-branch".equals(args[0])) {
                Git.rmBranch(args[1]);

            } else if ("rm".equals(args[0])) {
                Git.rm(args[1]);

            } else if ("reset".equals(args[0])) {
                Git.reset(args[1]);

            } else if ("merge".equals(args[0])) {
                Git.merge(args[1]);
            } else {
                throw new GitletException("No command with that name exists.");
            }
        } else {
            throw new GitletException("Please enter a command.");
        }
    }

    /**Constructor for a repo. */
    public Repo() {

        _stagingArea = new Commit();
        stringCommit = new TreeMap<>();
    }



    /** Rm command.... removes file from current working direc.*/
    public void rm(String filename) {
        _stagingArea = getAreaStage();
        boolean staged = false;
        if (!_stagingArea.getAdd().containsKey(filename)) {
        } else {
            staged = true;
            _stagingArea.unStaged(filename);
            writeObject(_currentStage, _stagingArea);
        }
        _stagingArea = getAreaStage();
        boolean tracked = false;
        if (!getHeadCurrent().getBlob().containsKey(filename)) {
        } else {
            tracked = true;
            _stagingArea.remove(filename);
            _stagingArea.unStaged(filename);
            restrictedDelete(filename);
            writeObject(_currentStage, _stagingArea);
        }
        if (staged || tracked) {
            return;
        }
        throw new GitletException("No reason to remove the file.");
    }

    /**Global Log. displays all commits. */
    public void globalLog() {
        List<String> keys = new ArrayList<>(getStagingArea().keySet());
        reverse(keys);
        int i = 0;
        while (i < keys.size()) {
            String id = keys.get(i);
            Commit commit = getStagingArea().get(id);
            SimpleDateFormat dateFormat = new
                    SimpleDateFormat("EEE MMM d HH:mm:ss Y Z");
            System.out.println("===");
            System.out.println("commit " + commit.getSHAcomment());
            System.out.println("Date: " + dateFormat.format
                    (commit.getDate()));
            System.out.println(commit.getMessage());
            System.out.print("\n");
            i++;
        }
    }

    /** Find ..... displays sha1 of commits or gives error. */
    public void find(String message) {
        stringCommit = getStagingArea();
        boolean holds = false;
        Iterator<String> iterator = stringCommit.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (stringCommit.get(key).getMessage().equals(message)) {
                System.out.println(stringCommit.get(key).getSHAcomment());
                holds = true;
            }
        }
        if (holds) {
            return;
        }
        throw new GitletException("Found no commit with that message.");
    }
    /** Branch... creates a branch files at head commit.*/
    public void branch(String branchN) {
        List<String> plainFilenamesIn = plainFilenamesIn(_Branchesss);
        int i = 0;
        while (i < plainFilenamesIn.size()) {
            String branch = plainFilenamesIn.get(i);
            if (branch.equals(branchN)) {
                throw new GitletException("A branch with "
                        + "that name already exists.");
            }
            i++;
        }
        File newBranch = new File(_Branchesss, branchN);
        writeContents(newBranch, getHeadCurrent().getSHAcomment());
    }

    /** rmBranch.....removes selected branch. */
    public void rmBranch(String branchName) {
        if (!branchName.equalsIgnoreCase(readContentsAsString(_Head))) {
            if (new File(_Branchesss, branchName).exists()) {
                new File(_Branchesss, branchName).delete();
            } else {
                throw new GitletException("A branch with that "
                        + "name does not exist.");
            }
        } else {
            throw new GitletException("Cannot remove the current branch.");
        }
    }

    /** Reset ...... checksout a commit checksout commit*/
    public void reset(String id) {
        String current = readContentsAsString(_Head);
        stringCommit = getStagingArea();
        _stagingArea = getAreaStage();
        if (stringCommit.containsKey(id)) {
            Commit newHead = stringCommit.get(id);
            List<String> plainFilenamesIn = plainFilenamesIn(_currentWorkingDirec);
            int i = 0;
            while (i < plainFilenamesIn.size()) {
                String file = plainFilenamesIn.get(i);
                if (getHeadCurrent().getBlob().containsKey(file)
                        || !newHead.getBlob().containsKey(file)) {
                    i++;
                } else {
            throw new GitletException("There is an untracked "
                    + "file in the way; delete it, or "
                    + "add and commit it first.");
        }
            }
            for (String file : newHead.getBlob().keySet()) {
                helperForBlobs(file, newHead);
            }
            List<String> filenamesIn = plainFilenamesIn(_currentWorkingDirec);
            int j = 0;
            while (j < filenamesIn.size()) {
                String file = filenamesIn.get(j);
                if (!newHead.getBlob().containsKey(file)) {
                    restrictedDelete(file);
                }
                j++;
            }
            _stagingArea.clean();
            writeObject(_currentStage, _stagingArea);
            writeContents(new File(_Branchesss, current),
                    newHead.getSHAcomment());
            writeContents(_Head, current);
        } else {
            throw new GitletException("No commit with that id exists.");
        }
    }

    /** helperForMerge1.*/
    public String split(Commit head, Commit other) {
        stringCommit = getStagingArea();
        ArrayList<String> headFir = helpers(head);
        ArrayList<String> otherFir = helpers(other);
        int i = 0;
        while (i < headFir.size()) {
            String ancestor = headFir.get(i);
            if (otherFir.contains(ancestor)) {
                return ancestor;
            }
            i++;
        }
        return null;
    }
    /** helper for helperForMerge1 which returns.*/
    public ArrayList<String> helpers(Commit helper) {
        ArrayList<String> previous = new ArrayList<>();
        previous.add(helper.getSHAcomment());
        while (true) {
            if (helper != null) {
                if (helper.getMergePare() != null) {
                    previous.add(helper.getMergePare());
                }
                previous.add(helper.getParentComment());
                if (helper.getParentComment() != null) {
                    helper = stringCommit.get(helper.getParentComment());
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        return previous;
    }

    /** Returns the current head branch.*/
    @SuppressWarnings("unchecked")
    public Commit getHeadCurrent() {
        String headName;
        headName = readContentsAsString(_Head);
        File headFile;
        headFile = new File(_Branchesss, headName);
        String headID;
        headID = readContentsAsString(headFile);
        stringCommit = readObject(_currCommits, TreeMap.class);
        return stringCommit.get(headID);
    }

    /** Staging Area .*/
    @SuppressWarnings("unchecked")
    public TreeMap<String, Commit> getStagingArea() {
        return readObject(_currCommits, TreeMap.class);
    }

    /** Staging Area .*/
    public Commit getAreaStage() {
        return readObject(_currentStage, Commit.class);
    }


    /** ShaId of brand new branches.*/
    public void ShaIdOfNewHead(String newss) {
        String currentBranch;
        currentBranch = readContentsAsString(_Head);
        File activeFile;
        activeFile = new File(_Branchesss, currentBranch);
        writeContents(activeFile, newss);
    }

    /** Helper Method for blobs .*/
    public void helperForBlobs(String filename, Commit main) {
        String blobSha;
        blobSha = main.getBlob().get(filename);
        File FilePath;
        FilePath = new File(_Blobs, blobSha);
        byte[] thisq;
        thisq = readContents(FilePath);
        File thatq;
        thatq = new File(_currentWorkingDirec, filename);
        writeContents(thatq, thisq);
    }

    /** ShorterUID_LENGTH .*/
    public String ShorterUID_LENGTH(String shortUID) {
        if (shortUID.length() == UID_LENGTH) {
            return shortUID;
        }
        stringCommit = getStagingArea();
        Iterator<String> iterator = stringCommit.keySet().iterator();
        while (iterator.hasNext()) {
            String num = iterator.next();
            if (num.startsWith(shortUID)) {
                return num;
            }
        }
        throw new GitletException("No commit with that id exists.");
    }
    /** getter method for repo .*/
    public File getRepo() {
        return _hiddenGitlet;
    }

    /** getter method to access _currentWorkingDirec field.
     * @return - _currentWorkingDirec
     */
    public File get_currentWorkingDirec() {
        return _currentWorkingDirec;
    }


    /** getter method to access _hiddenGitlet.
     * @return - _hiddenGitlet
     */
    public File get_hiddenGitlet() {
        return _hiddenGitlet;
    }


    /** getter method _currentStage
     * @return _currentStage
     */
    public File get_currentStage() {
        return _currentStage;
    }


    /** getter method _stagingArea
     * @return _stagingArea
     */
    public Commit get_stagingArea() {
        return _stagingArea;
    }


    /** setter method _stagingArea
     * @param _stagingArea - _stagingArea
     */
    public void set_stagingArea(Commit _stagingArea) {
        this._stagingArea = _stagingArea;
    }

    /** getter method for _currCommits
     * @return _currCommits
     */
    public File get_currCommits() {
        return _currCommits;
    }

    /** getter method for stringCommit
     * @return stringCommit
     */
    public TreeMap<String, Commit> getStringCommit() {
        return stringCommit;
    }


    /** setter method for stringCommit
     * @param stringCommit -string commit
     */
    public void setStringCommit(TreeMap<String, Commit> stringCommit) {
        this.stringCommit = stringCommit;
    }

    /** getter method for _branchess
     * @return - _branchess
     */
    public File get_Branchesss() {
        return _Branchesss;
    }


    /** getter method for _head
     * @return _head
     */
    public File get_Head() {
        return _Head;
    }


    /** getter Method _masterBranch
     * @return _masterBranch
     */
    public File get_masterBranch() {
        return _masterBranch;
    }


    /** getter Method for blobs
     * @return blobs
     */
    public File get_Blobs() {
        return _Blobs;
    }


    }



