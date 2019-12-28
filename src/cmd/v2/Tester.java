package cmd.cmd2;

//import cmd.global.utilities.Time;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class Tester
{
    public static void main(String[] args)
    {
        Object s = new Object();
        System.out.println("main():before call:s=" + s.hashCode());
        test6(s);
        System.out.println("main():after call:s=" + s.hashCode());
//      System.out.println(s);

//      compileExternalPrograms();
//      testExternalCommand(("cmd/global/utilities/Time ").trim());
    }
    
    public static void test6(Object s)
    {
        System.out.println("test6():before call:s=" + s.hashCode());
        s = new Object();
        System.out.println("test6():after call:s=" + s.hashCode());
    }
    
    public static void compileExternalPrograms()
    {
//      cmd.global.utilities.ExtTest a = new cmd.global.utilities.ExtTest();
//      cmd.global.utilities.Time b = new cmd.global.utilities.Time();
//      System.out.println("ALl external programs compiled successfully!");
    }
    
    public static void testExternalCommand(String fullCmdLine)
    {
        System.out.println("fullCmdLine=" + fullCmdLine);
        int indexFirstSpace = fullCmdLine.indexOf(' ');
        String fullCmdName =
                fullCmdLine
                        .substring(0,(indexFirstSpace==-1)?fullCmdLine.length():indexFirstSpace)
                        .replace('/', '.')
                        .replace('\\', '.');
        String argList = (indexFirstSpace == -1)?"":fullCmdLine.substring(indexFirstSpace+1, fullCmdLine.length());
        System.out.printf("fullCmdName=%s\nargList=%s\n", fullCmdName, argList);
        
        try
        {
            CmdExternalCommands cmd = (CmdExternalCommands)Class.forName(fullCmdName).getDeclaredConstructor().newInstance();
            String cmdName = cmd.getClass().getSimpleName().toLowerCase();
            
            System.out.printf(">> helpdoc %s\n%s\n", cmdName, cmd.getHelpString());
            
            String args[] = argList.split(" +", 0);
            System.out.println("args.length = " + (args.length-1)); // DEBUG
            
            System.out.print(">> " + cmdName);
            for(String s : args)
                System.out.print(" " + s);
            System.out.println();
            
            String[][] op = cmd.exec((args.length==1)?(new String[]{}):args); //(args);
            if(op != null)
            {
                if(op[0] != null)
                    for(String out : op[0])
                        System.out.printf("%s\n", out);
                else
                {
                    System.out.printf("Error: %s: ", cmdName);
                    for(String err : op[1])
                        System.out.printf("%s\n", err);
                }
            }
        }
        catch(Exception e)
        {
            System.err.printf("No such command found! [%s]\n", e.toString());
            e.printStackTrace(System.err); // DEBUG
        }
    }
    
    public static void test5()
    {
        System.out.println(Cmd.UserPriviledge.valueOf("NORMALs"));
    }
    public static void test4()
    {
        System.out.println("user.dir=" + System.getProperty("user.dir"));
        File f = new File("test_dir");
        System.out.println("f=" + f.getAbsolutePath() + ", created?=" + f.mkdir());
    }
    public static void test3()
    {
        File f = new File("cmd/global/utilities");
        System.out.println(f.getAbsolutePath());
        
//      String path = "cmd/global/utilities;cmd/global/system;";
        String path = "F:\\Projects\\Java\\IntelliJ IDEA\\Test_Project_1\\cmd\\global\\utilities;";
        for(String prg : getPrgList(path))
            System.out.println(prg);
    }
    
    private static String[] getPrgList(String pathString)
    {
        ArrayList<String> prgList = new ArrayList<>();
        for(String path : pathString.split(";"))
        {
            System.out.println("list for " + path); // DEBUG
            File dir = new File(path);
            if(dir.exists())
                prgList.addAll( Arrays.asList(dir.list( (File d, String filename) ->  (!filename.contains("$")) )));
            else
                System.out.println("Cannot find path: " + dir.getPath());
        }
        System.out.println("*************"); // DEBUG
        return prgList.toArray(new String[]{});
    }
    
    public static void test2()
    {
//      String line = "echo   ";
        String line = "echo Sourav  Dey";
        int start = line.indexOf(';');
        System.out.println("start=" + start);
        
        System.out.println((start==-1)?"\n":line.substring(start+1));
    }
    public static void test1()
    {
        String dir;
        String currentUsername = "sourav";
        String internalstateFilePath = "usr/"+currentUsername+"/data/InternalState";
        System.out.println("internalstateFilePath="+internalstateFilePath);
        dir=internalstateFilePath.substring(0, internalstateFilePath.lastIndexOf('/'));
        System.out.println("dir="+dir);
        System.out.println("status="+ new File( dir ).mkdirs());
    }
}
