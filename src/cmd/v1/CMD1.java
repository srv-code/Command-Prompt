package Apps.Java.cmd;

import java.io.Console;


// Command Interpreter
public class CMD1
{
    private Console console;
    private String cmdPrompt;
    public static float appVersion = 0.11F;
    
    private static class Options
    {
        // -v : verbose mode
        // -c0 : show compilation error count only
        // -c : show compilation error messages only
        
        private static boolean verboseModeActivated = false;
        private static boolean showOnlyCompilationErrorCount = false;
        private static boolean showOnlyCompilationErrorMessages = false;
    }
    
    public CMD1(String[] cmdLineArgs)
            throws  IllegalArgumentException, // If any invalid argument passed
                    IllegalStateException // If the system console cannot be retrieved
    {
        console = System.console();
        if(console==null)
            throw new IllegalStateException();
        
        for(String arg : cmdLineArgs)
        {
            switch(arg)
            {
                case "-v": Options.verboseModeActivated = true; break;
                case "-c": Options.showOnlyCompilationErrorMessages = true; break;
                case "-c0": Options.showOnlyCompilationErrorCount = true; break;
                default: throw new IllegalArgumentException(arg);
            }
        }
        printOut("Welcome to command interpreter [version %.2f]\n", appVersion);
        cmdPrompt = String.format("[%s]$ ", System.getProperty("user.dir"));
    }
    
    private static void printErr(String msg, Object... args) { print0(true, msg, args); }
    private static void printOut(String msg, Object... args) { print0(false, msg, args); }
    
    private static void print0(boolean err, String msg, Object... args)
    {
        if(err)
            System.err.printf(msg, args);
        else
            System.out.printf(msg, args);
    }
    
    public static void main(String[] args)
    {
        int exitCode = 0;
        try
        {
            CMD1 cmd = new CMD1(args);
            cmd.receiveCommand();
        }
        catch(IllegalStateException e)
        {
            printErr("Error: Cannot retrieve system console!\n");
            exitCode = 1;
        }
        catch(IllegalArgumentException e)
        {
            printErr("Error: Invalid argument (%s)\nProvide -h for help menu\n", e.getMessage());
            exitCode = 1;
        }
        finally
        {
            System.exit(exitCode);
        }
    }
    
    boolean processCommand(String lines)
    {
        for(String line : lines.split(";"))
        {
            line = line.trim();
            switch (line.toLowerCase())
            {
                case "exit": printOut("[Exiting command interpreter...]\n"); return true;
                default: printOut(line + " : Not designed!\n");
            }
        }
        return false;
    }
    
    void receiveCommand()
    {
//      System.out.println("[DEBUG]: Inside receiveCommand()...");
        boolean exitLoop = false;
        for(int successfulCommandsCount=0, totalCommandsCount=0; (!exitLoop); totalCommandsCount++)
        {
            printOut(cmdPrompt);
            exitLoop = processCommand(console.readLine());
        }
    }
}
