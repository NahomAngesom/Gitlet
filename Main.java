package gitlet;


/** Driver class for Gitlet, the tiny stupid version-control system.
 **  @author Nahom Ghebreselasie
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        try {
            Repo.input(args);
            return;
        } catch (GitletException error) {
            System.out.println(error.getMessage());
        }
        System.exit(0);
    }
}



