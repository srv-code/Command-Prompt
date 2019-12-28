package cmd.cmd2;

import java.util.ArrayList;
import java.io.File;
import java.util.Arrays;

public class Shell
{
    private static class InternalCommands
    {
        private static void pwd()
        {
            Cmd.printOut(Cmd.internalState.getCurrentTraversedDirectory(false) + "\n");
        }
        
        private static void cd(String toDir)
        {
            File dir = new File(toDir);
            if(!dir.exists() || !dir.isDirectory())
                Cmd.printErr("cd: No such directory exists ('%s')!\n", toDir);
            Cmd.internalState.setCurrentTraversedDirectory(toDir);
        }
    }
    
    private final static float appVersion = 0.1F;
    private static int instanceCount=0;
    private int pid;
    private String[] shellInternalProgramNames
            =   {
                    // Expand later into proper alignment, see Time.getHelpString() for reference
                    "pwd",
                            "Get the current working directory\n",
                    "cd",
                            "Change the current working directory\n",
                    "exit",
                            "Exits current working shell\n",
                    "hi",
                            "Shows current working shell details\n",
                    "pid",
                            "Shows current working shell process id\n",
                    "cls / clear",
                            "Clears the screen!\n" +
                                "Accepts 1 numeric argument as number of blank lines to print for clearing\n" +
                                "Usage: cls [-<number of blank lines to print>]\n",
                    "echo",
                            "Prints the message, provided as arguments, as they are\n",
                    "user / username",
                            "Prints the current name of the user\n",
                    "home / userhome",
                            "Prints the home directory of the current user\n",
                    "priviledge / priv",
                            "Shows the priviledges of the current user\n",
                    "path",
                            "Shows the executable path string for the current user (user + global)\n",
                    "lsprg / prgls",
                            "Prints all the executable command names (shell internal + external)\n",
                    "test",
                            "test program for Dev purpose\n",
                    "helpdoc",
                            "Shows the help message of the command name provided as argument, " +
                                "if no argument is provided shows the help message of the current working shell\n"
                };
    
    Shell()
    {
        pid = instanceCount++;
    }
    
    public static InternalState getInternalState() { return Cmd.internalState; }
    
    private static String[][] cmdLineParser(String cmdLine, int debugLevel)
    {
        final int highestDebugLevel=3;
        debugLevel = Math.abs(debugLevel%(highestDebugLevel+1)); // Levels restricted to 3, 0=debugging info off, lower debug levels = more restricted debugging info
        
        if(debugLevel > 0)
            System.out.printf(  "---- DEBUG START ----\n" +
                            "  highestDebugLevel=%d, debugLevel=%d\n" +
                            "  cmd line='%s'\n\n",
                    highestDebugLevel, debugLevel, cmdLine);
        
        ArrayList<String> lineTokens = new ArrayList<>();
        ArrayList<String[]> lines = new ArrayList<>();
        
        boolean dqOpened = false;
        int idx=-1, dqIdx=-1, ndqIdx=-1, len=cmdLine.length();
        
        while( (idx = cmdLine.indexOf('"', idx+1)) != -1 )
        {
            if (debugLevel == highestDebugLevel)
                System.out.printf("  Dq found at idx=%d\n", idx);
            if (!dqOpened) // Check for opening dq
            {
                if( (idx==0) || Character.isWhitespace(cmdLine.charAt(idx-1)) ) // validate opening dq: idx:0 || left:ws
                {
                    dqOpened = true;
                    dqIdx = idx;
                    
                    if(debugLevel >= 2)
                        System.out.printf("  Valid opening dq found at idx=%d\n", idx);
                }
            }
            else // Check for closing dq
            {
                if( ((idx+1)==len) || (Character.isWhitespace(cmdLine.charAt(idx+1))) || cmdLine.charAt(idx+1)==';' ) // validate closing dq: idx+1:len || right:ws || right:';'
                {
                    splitCmdLine(debugLevel, lineTokens, lines, cmdLine.substring(ndqIdx+1, dqIdx), cmdLine.substring(dqIdx+1, idx));
                    ndqIdx = idx;
                    dqOpened = false;
                    
                    if(debugLevel >= 2)
                        System.out.printf("  Valid closing dq found at idx=%d\n", idx);
                }
            }
        }
        
        if(ndqIdx != len)
            splitCmdLine(debugLevel, lineTokens, lines, cmdLine.substring(ndqIdx+1, len) );
        
        if(lineTokens.size() > 0)
            lines.add( lineTokens.toArray(new String[lineTokens.size()]) ); // Replaced, old version: (new String[]{}) );
        
        if(debugLevel > 0)
        {
            System.out.println("\n  All args:");
            int i=0, j=0;
            for(String[] lnTokens : lines)
            {
                System.out.printf("    Line %d: \n", ++i);
                for (String token : lnTokens)
                    System.out.printf("      [%d] '%s'\n", ++j, token);
                j=0;
            }
            
            System.out.println( "---- DEBUG END ----\n" );
        }

        return lines.toArray(new String[][]{});
    }
    
    private static void splitCmdLine(
            int debugLevel,
            ArrayList<String> lineTokens,
            ArrayList<String[]> lines,
            String cmdLineSegment,
            String... extra)
    {
        cmdLineSegment = cmdLineSegment.trim();
        int idx = -1, lastIdx = 0;
        String lineSeg;
        
        while( (idx=cmdLineSegment.indexOf(';', idx+1)) != -1 )
        {
            lineSeg = cmdLineSegment.substring(lastIdx, idx).trim();
            if(lineSeg.length()>0)
                lineTokens.addAll( Arrays.asList( lineSeg.split(" +") ) );
            lines.add( lineTokens.toArray(new String[]{}) );
            lineTokens.clear();
            lastIdx = idx+1;
        }
        
        if( (idx != (cmdLineSegment.length()-1)) && (lineSeg = cmdLineSegment.substring(lastIdx, cmdLineSegment.length()).trim()).length()>0 )
            lineTokens.addAll( Arrays.asList( lineSeg.split(" +") ) );
        
        for(String dqArg : extra)
            lineTokens.add(dqArg);
        
        if( debugLevel > 0 )
        {
            System.out.printf("  splitCmdLine:\n    cmdLineSegment='%s', extra='%s'\n    Added tokens:\n",
                    cmdLineSegment,
                    (extra.length>0)?extra[0]:null);
            for (String[] line : lines)
            {
                System.out.printf("      [ ");
                for (String token : line)
                    System.out.printf("'%s', ", token);
                System.out.println("\b\b ]");
            }
        }
    }
    
    // returns true only when exit command is issued
    boolean processCommand()
    {
        printPrompt();
        String line = Cmd.console.readLine();
        // Insert cmdLineParser here -- TO DO
        
        for(String[] cmds : cmdLineParser( line.trim(), 0 ))
        {
            switch(cmds[0])
            {
                // Search shell internal commands
                case "": break;
                case "exit":
                    if(checkPrgArgCount(cmds[0], ((cmds.length-1)==0), 0, cmds.length-1))
                        return true;
                    break;
                case "hi":
                    if(checkPrgArgCount(cmds[0], ((cmds.length-1)==0), 0, cmds.length-1))
                        Cmd.printOut("Shell [version=%.2f, pid=%d]\n", appVersion, pid);
                    break;
                case "pid":
                    if(checkPrgArgCount(cmds[0], ((cmds.length-1)==0), 0, cmds.length-1))
                        Cmd.printOut("%d\n", pid);
                    break;
                case "cls":
                case "clear":
                    if(checkPrgArgCount(cmds[0], ((cmds.length-1)<2), -1, -1, "Usage: clear [-(No of lines)]"))
                    {
                        try
                        {
                            int lineCount = 10;
                            if (cmds.length > 1)
                            {
                                if (cmds[1].charAt(0) == '-')
                                    lineCount = Integer.parseInt(cmds[1].substring(1));
                                else
                                    throw new IllegalArgumentException();
                            }
                            for (int i = 0; i < lineCount; i++)
                                Cmd.printOut("\n");
                        } catch (IllegalArgumentException | IndexOutOfBoundsException e)
                        { Cmd.printErr("Invalid option: '%s'\n", cmds[1]); }
                    }
                    break;
                case "echo":
                    for(int i=1; i<cmds.length; i++)
                        Cmd.printOut(cmds[i]);
                    Cmd.printOut("\n");
                    break;
                case "pwd":
                    if(checkPrgArgCount(cmds[0], ((cmds.length-1)==0), 0, cmds.length-1))
                        InternalCommands.pwd();
                    break;
                case "cd":
                    if(checkPrgArgCount(cmds[0], ((cmds.length-1)==1), 1, cmds.length-1, "Usage: cd <directory path to visit>"))
                        InternalCommands.cd(cmds[1]);
                    break;
                case "user":
                case "username":
                    if(checkPrgArgCount(cmds[0], ((cmds.length-1)==0), 0, cmds.length-1))
                        Cmd.printOut(Cmd.internalState.getUsername() + "\n");
                    break;
                case "userhome":
                case "home":
                    if(checkPrgArgCount(cmds[0], ((cmds.length-1)==0), 0, cmds.length-1))
                        Cmd.printOut(Cmd.internalState.getUserHomeDirectory() + "\n");
                    break;
                case "priviledge":
                case "priv":
                    if(checkPrgArgCount(cmds[0], ((cmds.length-1)==0), 0, cmds.length-1))
                        Cmd.printOut(Cmd.internalState.getUserPriviledge().toString() + "\n");
                    break;
                case "path":
                    if(checkPrgArgCount(cmds[0], ((cmds.length-1)==0), 0, cmds.length-1))
                        Cmd.printOut(Cmd.internalState.getUserFullPath() + "\n");
                    break;
                case "lsprg":
                case "prgls":
                    if(checkPrgArgCount(cmds[0], ((cmds.length-1)<=1), -1, -1,
                            "Usage: prgls [-e|-i]\n",
                            "  -e: Show external programs\n",
                            "  -i: Show shell internal programs\n",
                            "  default: Show both"))
                    {
                        if(cmds.length==1)
                            showProgramList(true, true);
                        else if(cmds[1].equals("-e"))
                            showProgramList(false, true);
                        else if(cmds[1].equals("-i"))
                            showProgramList(true, false);
                        else
                            Cmd.printErr("Error: %s: Invalid option: '%s'\n", cmds[0], cmds[1]);
                    }
                    break;
                case "helpdoc":
                    // Default: if no arg : showHelpDoc("Shell");
                    if(checkPrgArgCount(cmds[0], ((cmds.length-1)<=1), -1, -1,
                            "Usage: helpdoc [-<command name>], default: Shell (current program operating on)\n"))
                        showHelpDoc(((cmds.length-1)==0)?null:cmds[1]);
                    break;
                case "test":
                    test(Cmd.internalState.getUserFullPath());
                    Cmd.printOut("\n");
                    break;
                default:
                    // Search for external commands
                    CmdExternalCommands extcmd = getExternalCommand(cmds[0]);
                    if (extcmd == null)
                        Cmd.printErr("Cannot recognize command: '%s'\n", cmds[0]);
                    else
                    {
                        String[] args = new String[cmds.length-1];
                        if(cmds.length>1)
                            System.arraycopy(cmds, 1, args, 0, cmds.length-1);
                        
                        String[][] op = extcmd.exec(args);
                        if(op != null)
                        {
                            if(op[0] != null)
                                for(String out : op[0])
                                    Cmd.printOut("%s\n", out) ;
                            else
                            {
                                Cmd.printErr("Error: %s: ", cmds[0]);
                                for(String err : op[1])
                                    Cmd.printErr("%s\n", err);
                            }
                        }
                    }
            }
        }
        return false;
    }
    
    public static boolean checkPrgArgCount(
            String prgName,
            boolean cond,
            int req,
            int provided,
            String... additionalMsg)
    {
        if(!cond)
        {
            Cmd.printErr("Error: '%s': Invalid number of arguments!" +
                            ((req != -1)?" (Requires: %d, Provided: %d)":"") + "\n",
                            prgName,
                            req,
                            provided);
            if(additionalMsg.length>0)
            {
                
                for (String m : additionalMsg)
                    Cmd.printErr("%s", m);
                Cmd.printErr("\n");
            }
        }
        return cond;
    }
    
    private void showHelpDoc(String prgName)
    {
        // Check for default value first
        if(prgName == null)
        {
            Cmd.printOut("helpdoc: shell\nShell [version=%.2f, pid=%d]\n", appVersion, pid);
            return;
        }
        
        // Checking shell internal prorams first
        int idx = findShellInternalCommands(prgName);
        if(idx != -1)
        {
            Cmd.printOut("helpdoc: %s:\n%s\n", shellInternalProgramNames[idx], shellInternalProgramNames[idx+1]);
            return;
        }
        
        // Checking external programs now
        CmdExternalCommands extcmd = getExternalCommand(prgName);
        if(extcmd != null)
            Cmd.printOut("helpdoc: %s:\n%s\n", prgName, extcmd.getHelpString());
    }
    
    private void showProgramList(boolean showIntPrgs, boolean showExtProgs)
    {
        Cmd.printOut("List of executable programs:\n");
        int count = 0;
        
        if(showIntPrgs)
        {
            Cmd.printOut("  Shell internals:\n");
            for(int i=0; i<shellInternalProgramNames.length; i+=2, count++)
                Cmd.printOut("    [%3d]  %s\n", count+1, shellInternalProgramNames[i]);
        }
        
        if(showExtProgs)
        {
            Cmd.printOut("  Externals:\n");
            count = 0;
            for(String prg : getExternalProgramList()[0])
                Cmd.printOut("    [%3d]  %s\n", ++count, prg);
        }
    }
    
    private int findShellInternalCommands(String cmdName)
    {
        String nm = cmdName.toLowerCase();
        int idx = -1;
        for(int i=0; i<shellInternalProgramNames.length; i+=2)
            if(nm.equals(shellInternalProgramNames[i]))
            { idx = i; break; }
        return idx;
    }
    
    private String[][] getExternalProgramList()
    {
        String fileSeparatorString = System.getProperty("file.separator");
        ArrayList<File> ls = new ArrayList<>();
        for(String path : (Cmd.internalState.getUserFullPath()).split(";"))
            ls.addAll(Arrays.asList((new File(path)).listFiles((File f) ->
                    (   f.getPath().endsWith(".class") &&
                        !f.getPath().contains("$")
                    ))));
        
        ArrayList<String> nameLs = new ArrayList<>();
        ArrayList<String> pathLs = new ArrayList<>();
        String tempName, tempPath;
        for(int i=0; i<ls.size(); i++)
        {
            tempPath = (tempPath=ls.get(i).getPath()).substring(0, tempPath.length()-".class".length());
            tempName =
                    tempPath
                        .toLowerCase()
                        .substring(tempPath.lastIndexOf(fileSeparatorString)+1);
            if(findShellInternalCommands(tempName) == -1)
            { nameLs.add(tempName); pathLs.add(tempPath); }
        }
        return new String[][]{ nameLs.toArray(new String[]{}), pathLs.toArray(new String[]{}) };
    }
    
    private void test(String path)
    {
        System.out.println("user.dir=" + System.getProperty("user.dir"));
        for(String p : Cmd.internalState.getUserFullPath().split(";"))
            System.out.printf("\t%s = %s\n", p, new File(p).getAbsolutePath());
    }
    
    private CmdExternalCommands getExternalCommand(String cmdName)
    {
        CmdExternalCommands extcmd = null;
        boolean extcmdFound = false;
        String fullCmdPathName = null;
        
        String[][] extcmdList = getExternalProgramList();
        for(int i=0; i<extcmdList[0].length; i++)
            if(extcmdList[0][i].toLowerCase().equals(cmdName.toLowerCase()))
            {
                extcmdFound = true;
                fullCmdPathName = extcmdList[1][i];
                break;
            }
        if(!extcmdFound)
            return null;
        
        String fileSeparatorString = System.getProperty("file.separator");
        String fullyQualifiedCmdName = fullCmdPathName.replace(fileSeparatorString, ".");
        
        try { extcmd = (CmdExternalCommands)Class.forName(fullyQualifiedCmdName).getDeclaredConstructor().newInstance(); }
        catch(Exception e) { return null; }
        
        return extcmd;
    }
    
    private void printPrompt()
    {
        Cmd.printOut(Cmd.internalState.getPromptString());
    }
}
