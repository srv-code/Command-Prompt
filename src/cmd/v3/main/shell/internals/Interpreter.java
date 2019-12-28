package cmd.cmd3.main.shell.internals;

import java.io.Console;

public class Interpreter
{
    private final static float appVersion = 0.01f;
    
    public static final class ExitValues
    {
        final static int NORMAL_EXIT = 0;
        final static int NORMAL_ERROR_EXIT = 1;
        final static int FATAL_ERROR_EXIT = 10;
    }
    
    static final class Options
    {
        static boolean verboseEnabled;
        static boolean logEvents;
    }
    
    private static Console sysConsole;
    
    private Interpreter() {}
    
    private static void start()
    {

    }
    
    public static void main(final String[] args)
    {
        // Interprets command line and creates a new Interpreter instance
        
        initialChecks();
        for(String arg : args)
        {
            switch(arg)
            {
                case "-v": Options.verboseEnabled = true; break;
                case "-nl" : Options.logEvents = false; break;
                default:
                    System.err.println("Error: Invalid argument: " + arg);
                    System.exit(ExitValues.NORMAL_ERROR_EXIT);
            }
        }
        
        Interpreter interpreter = new Interpreter();
        interpreter.start();
    }
    
    private static void initialChecks()
    {
        // Check for a valid System console
        sysConsole = System.console();
        if(sysConsole == null)
        {
            System.err.println("Fatal Error: Cannot retrieve system console!");
            System.exit(ExitValues.FATAL_ERROR_EXIT);
        }
    }
    
    private static void showHelpMenu(boolean exitAfterDisplay)
    {
        System.out.printf("Interpreter version %f %n", appVersion);
        System.out.println("  -v : enable verbose mode");
        System.out.println("  -nl : no logging (disable logging events by default)");
        if(exitAfterDisplay)
            System.exit(ExitValues.NORMAL_EXIT);
    }
}
