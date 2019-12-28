package cmd.cmd2;

import java.io.File;
import java.io.Console;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Cmd
{
    private static final float appVersion = 0.11F;
    private static final String preferenceFilename = "data/Preferences";
    private static final String internalStateFilename = "data/InternalState";
    private static final String userDirPathname = "cmd/usr/";
    private static final String globalDirPath = "cmd/global/";
    private static final String pathFilename = "data/Path";
    private static final String utilitiesDirName = "utilities";
    private static final String systemDirName = "system";
    private static final String globalPathString
            = String.format("%1$s%2$s;%1$s%3$s;", globalDirPath, utilitiesDirName, systemDirName);
    private static final String[] illegalUserNames = {"system", "global", "root"};
    private static String userPreferenceFilePath = null;
    private static String userInternalStateFilePath = null;
    
    static Console console = null;
    private static Shell mainShell = null;
    static InternalState internalState = null;
    private static String currentUsername = null;
    private static PrintWriter pwOut = new PrintWriter(System.out);
    private static PrintWriter pwErr = new PrintWriter(System.err);
    
    static enum UserPriviledge { ADMIN, NORMAL }
    
    public static void main(String[] args)
    {
        printOut("Welcome to command interpreter [version %.2f]\n\n", appVersion);
        boolean errOccured = false;
        boolean exitShell = false;
        try
        {
            if(processCommandLineArgs(args)) // true when help menu is not asked for
            {
                if (Options.verboseModeActivated) printOut("[Loading initial objects...]\n");
                init();
                while (!exitShell)
                    exitShell = mainShell.processCommand();
            }
        }
        catch(IllegalStateException e) // For all kinds of errors
        {
            printErr("Error: %s\n", e.getMessage());
            errOccured = true;
        }
        finally
        {
            printOut("\nCommand interpreter terminating...\n");
            System.exit(errOccured?1:0);
        }
    }
    
    private static void showHelpMenu()
    {
        printOut("Usage: Cmd [-option1[ -option2...]]\n");
        printOut("Options:\n");
        printOut("\t-v: activate verbose mode\n");
        printOut("\t-h: this help message\n");
    }
    
    // returns true when help option is not asked for
    private static boolean processCommandLineArgs(String[] args)
        throws IllegalStateException // if any invalid option is encountered
    {
        for(String arg : args)
        {
            switch(arg)
            {
                case "-v" :
                    Options.verboseModeActivated = true;
                    if(Options.verboseModeActivated)
                        printOut("[Processing command line arguments...]\n");
                    break;
                case "-h" : showHelpMenu(); return false;
                default: throw new IllegalArgumentException("Invalid command line argument: "+arg+"\nProvide -h for help menu\n");
            }
        }
        return true;
    }
    
    private static class Options
    {
        // -v : verbose mode
        
        private static boolean verboseModeActivated = false;
    }
    
    static void printErr(String msg, Object... args) { print0(true, msg, args); }
    static void printOut(String msg, Object... args) { print0(false, msg, args); }
    
    private static void print0(boolean err, String msg, Object... args)
    {
        if(err)
        {
//          System.err.printf(msg, args);
            pwErr.printf(msg, args);
            pwErr.flush();
        }
        else
        {
//          System.out.printf(msg, args);
            pwOut.printf(msg, args);
            pwOut.flush();
        }
    }
    
    private static void init()
    {
        if(Options.verboseModeActivated) printOut("[Loading system console...]\n");
        console = System.console();
        if(console == null)
            throw new IllegalStateException("Cannot retrieve system console!\n");
        
        if(Options.verboseModeActivated) printOut("[Loading main shell...]\n");
        mainShell = new Shell();
        
        getCurrentUserName();
        
        userInternalStateFilePath = String.format("%s%s/%s", userDirPathname, currentUsername, internalStateFilename);
        userPreferenceFilePath = String.format("%s%s/%s", userDirPathname, currentUsername, preferenceFilename);
        
        if(Options.verboseModeActivated) printOut("[Initializing internal state...]\n");
        internalState = getInternalState();
        
        if(Options.verboseModeActivated) printOut("[Initializing preferences...]\n");
        loadPreferencesFile();
        
        if(Options.verboseModeActivated) printOut("[Loading user path...]\n");
        internalState.setUserPath(loadPath("global") + loadPath("user"));
        
        if(Options.verboseModeActivated) printOut("[Checking other global directories and files...]\n");
        checkGlobalDirsAndFiles();
    }
    
    public static void checkGlobalDirsAndFiles()
    {
        String[] miscDirs = { utilitiesDirName, systemDirName };
        
        for(String path : miscDirs)
        {
            if(Options.verboseModeActivated) printOut("[Checking directory '%s']\n", path);
            File dir = new File(globalDirPath+path);
            if(!dir.exists())
            {
                if(Options.verboseModeActivated) printOut("[Directory '%s' not found! Creating...]\n", path);
                dir.mkdirs();
            }
        }
    }
    
    public static String loadPath(String whose)
    {
        String pathString = null;
        if(whose.equals("user") || whose.equals("global"))
        {
            boolean loadingForUser = whose.equals("user");
            // Load user path
            File f = null;
            String pathFilePath;
            if(loadingForUser)
                pathFilePath = String.format("%s%s/%s", userDirPathname, currentUsername, pathFilename);
            else
                pathFilePath = String.format("%s%s", globalDirPath, pathFilename);
            f = new File( pathFilePath );
            if(f.exists())
            {
                if(Options.verboseModeActivated)
                    printOut("[Loading %s path file (%s)...]\n", whose, pathFilePath);
                try(BufferedReader r = new BufferedReader(new FileReader( f )))
                {
                    pathString = r.readLine();
                }
                catch(IOException e)
                {
                    throw new IllegalStateException("Cannot load "+whose
                            +" path file ("+f.getPath()+") ["+e.getMessage()+"]");
                }
            }
            else
            {
                if(Options.verboseModeActivated)
                    printOut("[Path file not found for %s%s! Creating file (%s)...]\n",
                            whose,
                            loadingForUser?(" ("+currentUsername+")"):"",
                            f.getPath());
                File dir = new File(pathFilePath.substring(0, pathFilePath.lastIndexOf('/')));
                if(!dir.exists())
                    if(!dir.mkdirs())
                        throw new IllegalStateException("Cannot create path file directory ("
                                +dir.getPath()+") for "
                                +whose
                                +(loadingForUser?(" ("+currentUsername+")"):"")
                                +" ("+currentUsername+")");
                try
                {
                    if(!f.createNewFile())
                        throw new IllegalStateException();
                    if(!loadingForUser)
                    {
                        FileWriter w = new FileWriter(f);
                        w.write(globalPathString);
                        pathString = globalPathString;
                        w.close();
                    }
                }
                catch(Exception e)
                {
                    throw new IllegalStateException("Cannot create path file ("
                            +f.getPath()+") for "
                            +whose
                            +(loadingForUser?(" ("+currentUsername+")"):""));
                }
            }
        }
        else
        {
            // Invalid whose value
            printErr("Internal Error: Cmd::loadPath(String whose): Invalid parameter value passed=%s\n", whose);
        }
        return (pathString == null)?"":pathString;
    }
    
    private static void getCurrentUserName()
    {
        boolean continueLooping = false;
        String res;
        do
        {
            currentUsername = console.readLine("Enter username: ").trim();
            if(currentUsername == null ||  currentUsername.length()<1)
            {
                continueLooping = true;
                printOut("Wrong input! Please try again!\n");
            }
            else if(!new File(String.format("%s%s", userDirPathname, currentUsername)).exists())
            {
                res = console.readLine("Create new user '%s'? [Y/N]: ", currentUsername);
                continueLooping = (!res.equals("Y"));
            }
            else
            {
                boolean illegalUsernameFound = false;
                for(String name : illegalUserNames)
                    if(illegalUsernameFound  = (currentUsername.toLowerCase().equals(name)))
                    {
                        printErr("Cannot assign this name to any normal user!\nPlease try some other name!\n");
                        break;
                    }
                if(!illegalUsernameFound)
                    continueLooping = false;
            }
        } while(continueLooping);
    }
    
    private static InternalState getInternalState()
    {
        InternalState state = null;
        File filePath = new File(userInternalStateFilePath);
        File dirPath = new File( userInternalStateFilePath.substring(0, userInternalStateFilePath.lastIndexOf('/')) );
        
        try
        {
            if (!dirPath.exists())
            {
                if(Options.verboseModeActivated)
                    printOut("[Creating internal state directory (%s) for new user (%s)...]\n", dirPath.getPath(), currentUsername);
                if(!dirPath.mkdirs())
                    throw new IllegalStateException("Cannot create new internal state directory ("+dirPath.getPath()
                            +") for new user ("+currentUsername+")!");
                state = new InternalState(currentUsername);
            }
            else if(filePath.exists())
            {
                if(Options.verboseModeActivated)
                    printOut("[Restoring previous internal state (%s) for existing user (%s)...]\n", filePath.getPath(), currentUsername);
                state = new InternalState(currentUsername, loadInternalStateFromFile());
            }
            else
            {
                if(Options.verboseModeActivated)
                    printOut("[No previous internal state (%s) was found for existing user (%s)]\n", filePath.getPath(), currentUsername);
                state = new InternalState(currentUsername);
            }
        }
        catch(IOException e)
        {
            throw new IllegalStateException("Cannot create new internal state directory ("+filePath.getPath()
                    +") for new user ("+currentUsername+")! ["+e.getMessage()+"]");
        }
        
        return state;
    }
    
    private static InternalState loadInternalStateFromFile()
            throws IOException
    {
        System.out.println("Cmd::loadInternalStateFromFile(): Stub!");
        return null;
        //      try( ObjectInputStream objOS = new ObjectInputStream(new FileInputStream( internalstateFilePath )) )
//      {
//
//      }
//      catch(IOException e)
//      {
//
//      }
    }
    
    private static void writeUserPreferences()
    {
        try (FileWriter w = new FileWriter( userPreferenceFilePath ))
        {
            w.write("user priviledge=" + internalState.getUserPriviledge() + "\n");
        }
//      catch(NullPointerException|IOException e)
        catch(IOException e)
        {
            throw new IllegalStateException("Cannot write to preferences file ("
                    +userPreferenceFilePath
                    +") ["+e.getMessage()+"]");
        }
    }
    private static void loadPreferencesFile()
    {
        File path = new File( userPreferenceFilePath );
        boolean operationSuccessful = true;
        if(!path.exists())
        {
            if(Options.verboseModeActivated)
                printOut("[Creating preferences file (%s) for new user (%s)...]\n",
                        userPreferenceFilePath,
                        currentUsername);
            try
            {
                File dirPath = new File( userPreferenceFilePath.substring(0, userPreferenceFilePath.lastIndexOf('/')) );
                if(!dirPath.exists())
                    if(!dirPath.mkdirs())
                        throw new IllegalStateException("Cannot create new preferences directory ("+dirPath.getPath()
                                +") for new user ("+currentUsername+")!");
                if(!path.createNewFile())
                    throw new IllegalStateException("Cannot create new preferences file ("
                            +userPreferenceFilePath
                            +") for new user ("+currentUsername+")!");
                writeUserPreferences();
            }
            catch(IOException e)
            {
                throw new IllegalStateException("Cannot create new preferences directory ("
                        +userPreferenceFilePath
                        +") for new user ("+currentUsername+")! ["+e.getMessage()+"]");
            }
        }
        else
        {
            if(Options.verboseModeActivated)
                printOut("[Loading preferences file (%s) for existing user (%s)...]\n", userPreferenceFilePath, currentUsername);
            try (BufferedReader r = new BufferedReader(new FileReader(path)))
            {
                String line;
                while ((line = r.readLine()) != null)
                    try { loadPreferenceProperty(line); }
                    catch(IllegalArgumentException e)
                    {
                        if (Options.verboseModeActivated)
                            printErr("\t[Error loading preference data: %s\n", e.getMessage());
                    }
            }
            catch (IOException e)
            {
                throw new IllegalStateException("Cannot load preference file ("+userPreferenceFilePath+"): I/O error occured ("+e.getMessage()+")");
            }
        }
    }
    
    private static void loadPreferenceProperty(String pref)
        throws  IllegalArgumentException // if any invalid property found
//              ArrayIndexOutOfBoundsException // If splitting isn't successful
    {
        String[] keyValPair = pref.split("=");
        if(keyValPair.length != 2)
            throw new IllegalArgumentException("Invalid preference string ("+pref+")");
        switch (keyValPair[0])
        {
            case "user priviledge":
                try
                { internalState.setUserPriviledge(UserPriviledge.valueOf(keyValPair[1])); }
                catch(IllegalArgumentException e)
                { throw new IllegalArgumentException("[Invalid value: {key: user priviledge, val="+keyValPair[1]+"}]"); }
                break;
            default:
                throw new IllegalArgumentException("[Invalid key: {key: "+keyValPair[0]+", val="+keyValPair[1]+"}");
        }
    }
}
