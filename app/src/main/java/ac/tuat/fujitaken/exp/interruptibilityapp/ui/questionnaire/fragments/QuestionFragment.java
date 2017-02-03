package ac.tuat.fujitaken.exp.interruptibilityapp.ui.questionnaire.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import ac.tuat.fujitaken.exp.interruptibilityapp.R;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.save.EvaluationData;
import ac.tuat.fujitaken.exp.interruptibilityapp.ui.questionnaire.InterruptionNotification;

/**
 * ダイアログ表示用フラグメント
 *
 */
public class QuestionFragment extends DialogFragment{

    public static final int INTERRUPTIBILITY_REQUEST_CODE = 147,
            TASK_REQUEST_CODE = 258,
            LOCATION_REQUEST_CODE = 369,
            COMMENT_REQUEST_CODE = 684;

    public static final String INTERRUPTIBILITY = "INTERRUPTIBILITY",
            TASK = "TASK",
            LOCATION = "LOCATION",
            COMMENT = "COMMENT",
            BROADCAST_ANSWER_ACTION = "BROADCAST_ANSWER_ACTION",
            BROADCAST_ASK_ACTION = "BROADCAST_ASK_ACTION",
            BROADCAST_CANCEL_ACTION = "BROADCAST_cancel_ACTION";

    private Fragment selfFragment = this;

    private Button interruptibility,
            task,
            location,
            comment;

    private boolean mode,
            answered = false;

    private EvaluationData evaluationData;

    public static QuestionFragment newInstance(Bundle bundle) {
        QuestionFragment fragment = new QuestionFragment();
        EvaluationData data = (EvaluationData)bundle.getSerializable(EvaluationData.EVALUATION_DATA);
        if (data != null) {
            data.answerTime = System.currentTimeMillis();
        }
        fragment.setArguments(bundle);
        return fragment;
    }

    public QuestionFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        evaluationData = (EvaluationData)bundle.getSerializable(EvaluationData.EVALUATION_DATA);
        mode = bundle.getBoolean(InterruptionNotification.TYPE, true);
        answered = false;
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final LinearLayout rootLayout;
        rootLayout = (LinearLayout)inflater.inflate(R.layout.dialog_layout, null, false);

        interruptibility = (Button) rootLayout.findViewById(R.id.interrupt);
        task = (Button) rootLayout.findViewById(R.id.task);
        location = (Button) rootLayout.findViewById(R.id.location);
        comment = (Button) rootLayout.findViewById(R.id.comment);


        comment.setText(evaluationData.comment);
        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputDialogFragment fragment = InputDialogFragment.newInstance("コメント");
                fragment.setTargetFragment(selfFragment, COMMENT_REQUEST_CODE);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .add(fragment, COMMENT)
                        .commit();
            }
        });

        if(mode) {
            interruptibility.setText(String.valueOf(evaluationData.evaluation));
            interruptibility.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ListDialogFragment fragment = ListDialogFragment.newInstance("現在の割り込み拒否度は？\n1（問題なし）～5（嫌）", new String[]{"1", "2", "3", "4", "5"});
                    fragment.setTargetFragment(selfFragment, INTERRUPTIBILITY_REQUEST_CODE);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .add(fragment, INTERRUPTIBILITY)
                            .commit();
                }
            });

            task.setText(evaluationData.task);
            task.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ListDialogFragment fragment = ListDialogFragment.newInstance("現在の作業内容は？", new String[]{"PC作業", "デスクワーク", "机外作業", "移動", "会話", "休憩",  "自由記述"});
                    fragment.setTargetFragment(selfFragment, TASK_REQUEST_CODE);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .add(fragment, TASK)
                            .commit();
                }
            });

            location.setText(evaluationData.location);
            location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ListDialogFragment fragment = ListDialogFragment.newInstance("現在の場所は？", new String[]{"414", "419", "4S", "4Q", "S4", "自由記述"});
                    fragment.setTargetFragment(selfFragment, LOCATION_REQUEST_CODE);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .add(fragment, LOCATION)
                            .commit();
                }
            });
        }
        else {
            TextView interruptibilityText = (TextView)rootLayout.findViewById(R.id.interruptibilityText);
            TextView taskText = (TextView)rootLayout.findViewById(R.id.taskText);
            TextView locationText = (TextView)rootLayout.findViewById(R.id.locationText);
            TextView commentText = (TextView)rootLayout.findViewById(R.id.commentText);

            rootLayout.removeView(interruptibilityText);
            rootLayout.removeView(taskText);
            rootLayout.removeView(locationText);

            rootLayout.removeView(interruptibility);
            rootLayout.removeView(task);
            rootLayout.removeView(location);

            interruptibility.setVisibility(View.INVISIBLE);
            task.setVisibility(View.INVISIBLE);
            location.setVisibility(View.INVISIBLE);

            commentText.setText("遅れた理由は？");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(rootLayout);
        if(mode){
            builder.setTitle("話しかけを受け、\n5分間の作業中断発生");
        } else{
            builder.setTitle("回答時間を過ぎました\n" + String.valueOf((evaluationData.answerTime - evaluationData.time) / 1000) + "秒経過");
        }
        builder.setPositiveButton(getString(R.string.positive_dialog_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent broadcastIntent = new Intent();
                        broadcastIntent.putExtras(getArguments());
                        if(mode) {
                            broadcastIntent.setAction(BROADCAST_ANSWER_ACTION);
                        }
                        else{
                            broadcastIntent.setAction(BROADCAST_ASK_ACTION);
                        }
                        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity().getApplicationContext());
                        localBroadcastManager.sendBroadcast(broadcastIntent);
                        answered = true;
                    }
                });
        setCancelable(false);

        return builder.create();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(!answered){
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(BROADCAST_CANCEL_ACTION);
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity().getApplicationContext());
            localBroadcastManager.sendBroadcast(broadcastIntent);
        }
        getActivity().finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle bundle = data.getExtras();
        String txt = bundle.getString(ListDialogFragment.TEXT_RESULT, "");
        switch (requestCode){
            case INTERRUPTIBILITY_REQUEST_CODE:
                this.evaluationData.evaluation = Integer.parseInt(txt);
                interruptibility.setText(txt);
                break;
            case TASK_REQUEST_CODE:
                this.evaluationData.task = txt;
                task.setText(txt);
                break;
            case LOCATION_REQUEST_CODE:
                this.evaluationData.location = txt;
                location.setText(txt);
                break;
            case COMMENT_REQUEST_CODE:
                this.evaluationData.comment = bundle.getString(InputDialogFragment.INPUT_TEXT, "");
                comment.setText(this.evaluationData.comment);
                break;
            default:
                break;
        }
    }
}
