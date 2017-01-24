package ac.tuat.fujitaken.exp.interruptibilityapp.interrupt.decision;

/**
 * Created by hi on 2017/01/20.
 */

public class PC {
    public static final int FROM_PC = 1 << 6;

    private int isPrevFromPC = 0;

    public int judge(String message){
        if (message.equals("")) {
            isPrevFromPC = 0;
            return 0;
        }
        else if(message.equals("null")){
            int ret = isPrevFromPC;
            isPrevFromPC = 0;
            return ret;
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
