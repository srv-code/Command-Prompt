package cmd.cmd2;

import javax.naming.OperationNotSupportedException;

public interface CmdExternalCommands
{
    String getHelpString();
    String[][] exec(String[] args);
}
