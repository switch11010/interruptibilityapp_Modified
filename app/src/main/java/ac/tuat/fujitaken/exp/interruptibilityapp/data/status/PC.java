package ac.tuat.fujitaken.exp.interruptibilityapp.data.status;

import ac.tuat.fujitaken.exp.interruptibilityapp.Constants;

/**
 * PCの状態を保存
 * Created by hi on 2017/01/20.
 */

public class PC {
    public static final int FROM_PC = 1 << 6;
    private static final String COMM_FAILED = "",
                                PREV_STATE = "null";

    private int isPrevFromPC = 0;

    public int judge(String message){
        if (COMM_FAILED.equals(message)) {
            isPrevFromPC = 0;
            return 0;
        }
        else if(PREV_STATE.equals(message)){
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
        isPrevFromPC = (pcInterval < Constants.PC_OPERATION_LIMITATION) ? FROM_PC : 0;
        return isPrevFromPC;
    }
}
