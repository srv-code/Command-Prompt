package cmd.cmd2;

public class InternalState
    implements java.io.Serializable
{
    private static class Properties
    {
        private static String fullUserPath;
//      private static String promptString;
        private static String username;
        private static Cmd.UserPriviledge userPriviledge;
        private static String userHomeDirectory;
        private static String currentTraversedDirectory;
        private static String currentProgramDirectory = System.getProperty("user.dir").replace('\\', '/');
    }
    
    InternalState(String username, InternalState prevState)
    {
        Properties.username = username;
        // Default
        Properties.userPriviledge = Cmd.UserPriviledge.NORMAL;
        Properties.userHomeDirectory = "/usr/" + Properties.username;
        Properties.currentTraversedDirectory
                = (prevState==null)?Properties.userHomeDirectory:Properties.currentTraversedDirectory;
        // Setting to default
//      Properties.promptString
//              = (prevState==null)? String.format("[%s@%s]>> ", Properties.currentTraversedDirectory, Properties.username): Properties.promptString;
//              = (prevState==null)? String.format("[%s@%s]>> ", System.getProperty("user.dir"), Properties.username): Properties.promptString;
    }
    
    InternalState(String username) { this(username, null); }
    
    
    public void setUserPath(String path) { Properties.fullUserPath = path; }
    public void setUserPriviledge(Cmd.UserPriviledge priv) { Properties.userPriviledge = priv;}
    public Cmd.UserPriviledge getUserPriviledge() { return Properties.userPriviledge;}
    public String getPromptString() { return String.format("[%s@%s]>> ", Properties.currentTraversedDirectory, Properties.username); }
    public String getUsername() { return Properties.username; }
    public String getUserFullPath() { return Properties.fullUserPath; }
    public String getUserHomeDirectory() { return Properties.userHomeDirectory; }
    public void setCurrentTraversedDirectory(String dirPath) { Properties.currentTraversedDirectory = dirPath; }
    public String getCurrentTraversedDirectory(boolean resolveToAbsolutePath)
    {
        return (resolveToAbsolutePath)?
                (Properties.currentProgramDirectory + "/cmd" + Properties.currentTraversedDirectory) :
                (Properties.currentTraversedDirectory);
    }
    
//  public boolean setPromptString(String p)
//  {
//      p = p.trim();
//      if(p.length()<1)
//          return false;
//      else
//          Properties.promptString = p;
//      return true;
//  }
}
