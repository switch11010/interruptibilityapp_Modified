package ac.tuat.fujitaken.kk.test.testapplication.data;

/**
 *
 * Created by hi on 2015/11/20.
 */
public class EvaluationData extends RowData {
    public static String EVALUATION_DATA = "EVALUATION_DATA";

    public long answerTime = 0;
    public String task = "",
            location = "",
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
        builder.append(DATE_FORMAT.format(time)).append(",")
                .append(answerTime == 0 ? "" : (answerTime- time)/1000).append(",")
                .append(evaluation == 0? "": evaluation).append(",")
                .append(task.replaceAll(",", "，")).append(",")
                .append(location.replaceAll(",", "，")).append(",")
                .append(comment.replaceAll(",", "，")).append(",")
                .append(event == 0? "": event).append(",");
        for(Data d: data){
            builder.append(d.getString())
                    .append(",");
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
        this.comment = setData.comment;
        this.evaluation = setData.evaluation;
        this.event = setData.event;
    }
}