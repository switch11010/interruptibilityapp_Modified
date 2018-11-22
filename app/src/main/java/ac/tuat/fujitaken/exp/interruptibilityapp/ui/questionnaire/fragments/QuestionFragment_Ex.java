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

import ac.tuat.fujitaken.exp.interruptibilityapp.LogEx;
import ac.tuat.fujitaken.exp.interruptibilityapp.R;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.save.EvaluationData;
import ac.tuat.fujitaken.exp.interruptibilityapp.ui.questionnaire.InterruptionNotification;

/**
 * ダイアログ表示用フラグメント（QuestionFragment のコピペ）
 *
 */
public class QuestionFragment_Ex extends DialogFragment{

    public static final int INTERRUPTIBILITY_REQUEST_CODE = 147,
            TASK_REQUEST_CODE = 258,
            LOCATION_REQUEST_CODE = 369,
            USE_PURPOSE_REQUEST_CODE = 13579,  //s 追加：スマホ使用目的
            COMMENT_REQUEST_CODE = 684;  //s 入力が終わった後に onActivityResult() 内でどのフラグメントによる入力だったのかの識別に利用…？

    public static final String INTERRUPTIBILITY = "INTERRUPTIBILITY",
            TASK = "TASK",
            LOCATION = "LOCATION",
            USE_PURPOSE = "USE_PURPOSE",  //s 追加：スマホ使用目的
            COMMENT = "COMMENT",
            BROADCAST_ANSWER_ACTION = "BROADCAST_ANSWER_ACTION",
            BROADCAST_ASK_ACTION = "BROADCAST_ASK_ACTION",
            BROADCAST_CANCEL_ACTION = "BROADCAST_cancel_ACTION";

    private Fragment selfFragment = this;

    private Button interruptibility,
            task,
            location,
            usePurpose,  //s 追加：スマホ使用目的
            note,  //s 追加：ロック画面で通知を確認したことによる開始か
            comment;  //s 押されると ListDialogFragment とかのフラグメントの表示に移る

    private boolean mode,  //s 割込み通知が一定時間無反応だった（回答時間が過ぎました）なら false
            answered = false;  //s 割込み通知に回答があったら true ？

    private EvaluationData evaluationData;

    public static QuestionFragment_Ex newInstance(Bundle bundle) {
        QuestionFragment_Ex fragment = new QuestionFragment_Ex();
        EvaluationData data = (EvaluationData)bundle.getSerializable(EvaluationData.EVALUATION_DATA);
        if (data != null) {
            data.answerTime = System.currentTimeMillis();
        }
        fragment.setArguments(bundle);
        return fragment;
    }

    public QuestionFragment_Ex(){}

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
        rootLayout = (LinearLayout)inflater.inflate(R.layout.dialog_layout_ex, null, false);

        interruptibility = (Button) rootLayout.findViewById(R.id.interrupt);
        task = (Button) rootLayout.findViewById(R.id.task);
        location = (Button) rootLayout.findViewById(R.id.location);
        usePurpose = (Button) rootLayout.findViewById(R.id.usePurpose);  //s 追加：スマホ使用目的
        comment = (Button) rootLayout.findViewById(R.id.comment);


        comment.setText(evaluationData.comment);
        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputDialogFragment fragment = InputDialogFragment.newInstance("気になったことなど");
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
                    ListDialogFragment fragment = ListDialogFragment.newInstance("割り込み直前の作業内容は？", new String[]{"PC作業", "デスクワーク", "机外作業", "移動", "会話", "休憩",  "自由記述"});
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

            //s 追加ここから：スマホ使用目的
            usePurpose.setText(evaluationData.usePurpose);
            usePurpose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] selection = {
                            "時間・通知の確認", "サブPC的利用", "情報アクセス", "私用", "休憩",  "自由記述"
                    };
                    ListDialogFragment fragment = ListDialogFragment.newInstance("スマホの使用目的は？", selection);
                    fragment.setTargetFragment(selfFragment, USE_PURPOSE_REQUEST_CODE);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .add(fragment, USE_PURPOSE)
                            .commit();
                }
            });
            //s 追加ここまで
        }
        else {  //s 時間内に回答をしなかった場合
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
            builder.setTitle("★割込みシミュレーション★");//"話しかけを受け、\n5分間の作業中断発生");
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
            case USE_PURPOSE_REQUEST_CODE:  //s 追加ここから：スマホ使用目的
                this.evaluationData.usePurpose = txt;
                usePurpose.setText(txt);
                break;  //s 追加ここまで
            case COMMENT_REQUEST_CODE:
                this.evaluationData.comment = bundle.getString(InputDialogFragment.INPUT_TEXT, "");
                comment.setText(this.evaluationData.comment);
                break;
            default:
                break;
        }
    }
}
