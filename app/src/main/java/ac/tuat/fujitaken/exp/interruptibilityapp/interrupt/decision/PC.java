package ac.tuat.fujitaken.exp.interruptibilityapp.interrupt.decision;

/**
 * Created by hi on 2017/01/20.
 */

public class PC {
    public static final int FROM_PC = 1 << 6;

    private int isPrevFromPC = 0;

    public int judge(String message){
        if (message.equals("")) {
            return 0;
        }
        else if(message.equals("null")){
            return isPrevFromPC;
        }
        String[] params = message.split(":");
        long clickInterval, keyInterval;
        try {
            clickInterval = Long.parseLong(params[params.length - 1]);
            keyInterval = Long.parseLong(params[params.length - 2]);
        }
        catch (NumberFormatException e){
            return 0;
        }

        long pcInterval = clickInterval < keyInterval ? clickInterval : keyInterval;
        isPrevFromPC = (pcInterval < 60 * 1000) ? FROM_PC : 0;
        return isPrevFromPC;
    }
}
