package ac.tuat.fujitaken.exp.interruptibilityapp.data.save;

import ac.tuat.fujitaken.exp.interruptibilityapp.data.base.Data;

/**
 *
 * Created by hi on 2015/11/20.
 */
public class EvaluationData extends RowData {
    public static String EVALUATION_DATA = "EVALUATION_DATA";

    public long answerTime = 0;
    public String task = "",
            location = "",
            usePurpose = "",  //s 追加：スマホ使用目的
            comment = "";
    public int evaluation = 0,
            event = 0;

    public EvaluationData(){
        super();
    }

    @Override
    public EvaluationData clone(){
        return (EvaluationData) super.clone();
    }

    @Override
    public String getLine() {
        StringBuilder builder = new StringBuilder();
        builder.append(DATE_FORMAT.format(time));
        builder.append(",");
        builder.append(answerTime == 0 ? "" : (answerTime- time)/1000);
        builder.append(",");
        builder.append(evaluation == 0? "": evaluation);
        builder.append(",");
        builder.append(task.replaceAll(",", "，"));
        builder.append(",");
        builder.append(location.replaceAll(",", "，"));
        builder.append(",");
        builder.append(usePurpose.replaceAll(",", "，"));  //s 追加
        builder.append(",");  //s 追加
        builder.append(comment.replaceAll(",", "，"));
        builder.append(",");
        builder.append(event == 0? "": event);
        builder.append(",");
        for(Data d: data){
            builder.append(d.getString());
            builder.append(",");
        }

        return builder.substring(0, builder.length()-1);
    }

    public void setValue(RowData setData){
        this.time = setData.time;
        this.data = setData.data;
    }

    public void setValue(EvaluationData setData){
        this.event = setData.event;
        setValue((RowData)setData);
    }

    public void setAnswer(EvaluationData setData){
        this.answerTime = setData.answerTime;
        this.task = setData.task;
        this.location = setData.location;
        this.usePurpose = setData.usePurpose;  //s 追加
        this.comment = setData.comment;
        this.evaluation = setData.evaluation;
    }
}