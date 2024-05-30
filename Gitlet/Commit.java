package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class Commit implements Serializable {

    /** The message of the commit is stored here. */
    private String _mes;
    /** Filenames are mapped to the
     * SHA-1 of a blob
     * that contains ints contents. */
    private final HashMap<String, String> _blobsSnap = new HashMap<>();
    /** The SHA-1 of the Commit parent is stored here.*/
    private String _parentSha;
    /**If the commit has a merge parent,
     * it stores the SHA-1 of that parent.  */
    private String _mergePar;
    /** The correct time at which a commit is initiated
     * is saved in this variable. */
    private Date _times;
    /** Contents of Blobs.*/
    private byte[] _contentsBlob;
    /**  file name. */
    private String _fileName;
    /** Maps staged file to blob sha1s using a hashmap. */
    private HashMap<String, String> _addSha = new HashMap<>();
    /** stores files that have been marked for removal.*/
    private ArrayList<String> _remove;


    /** Constructor for a commit object.
     * @param message - message.
     * @param parent - parent.
     * @param merge - merge.
     * */

    public Commit(String message, String parent, String merge) {
        _mes = message;
        _times = new Date();
        _parentSha = parent;
        _mergePar = merge;

        if (!Objects.equals(message, "initial commit")) {
            return;
        } else {
            _times = new Date(0);
            _parentSha = null;
        }

    }

    /** SHA-1 code for a commit is generated with this method.
     * @return sha1 of commit */
    public String getSHAcomment() {
        return Utils.sha1(Utils.serialize(this), "commit");
    }

    /** commit message's getter method.
     * @return - message */
    public String getMessage() {
        return _mes;
    }

    /** A date object's getter method.
     * @return - dateStamp*/
    public Date getDate() {
        return _times;
    }

    /** A commits blobs references getter method.
     * @return - blob snapshot.*/
    public HashMap<String, String> getBlob() {
        return _blobsSnap;
    }

    /** getter method for a commit's parent.
     * @return - parent shai in commit.*/
    public String getParentComment() {
        return _parentSha;
    }

    /** merge parent of a commit can be retrieved using this method.
     * @return - merger parent */
    public String getMergePare() {
        return _mergePar;
    }

    /** Constructor for blobs object.
     * @param file - file */
    public Commit(File file) {
        _contentsBlob = Utils.readContents(file);
        _fileName = file.getName();
    }

    /** Getter method blob contents.
     * @return - contents of blobs */
    public byte[] getContents() {
        return _contentsBlob;
    }

    /** SHA-1 code for a blobs is generated with this method.
     * @return - sha1 of blob */
    public String getSHAblob() {
        return Utils.sha1(Utils.serialize(this), "blob");
    }

    /** Constructor . */
    public Commit() {
        _remove = new ArrayList<>();
    }

    /** clean method. */
    public void clean() {
        _addSha = new HashMap<>();
        _remove = new ArrayList<>();
    }

    /** a file that is about to be added.
     * @param filename - filename
     * @param sha1 - sha1 */
    public void add(String filename, String sha1) {
        _addSha.put(filename, sha1);
    }

    /** getter method a file that is about to be deleted.
     * @param filename - filename*/
    public void remove(String filename) {
        _remove.add(filename);
    }

    /** files ready to be added.
     * @return add */
    public HashMap<String, String> getAdd() {
        return _addSha;
    }

    /** Getter method that returns all files staged for removal.
     * @return remove */
    public ArrayList<String> getRemove() {
        return _remove;
    }

    /** Removes a file that has been staged for addition.
     * @param filename - filename*/
    public void unStaged(String filename) {
        _addSha.remove(filename);
    }
}
