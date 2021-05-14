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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

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

    //s 変更：接尾辞 Button を追加
    private Button interruptibilityButton,
//            timingButton, //ny 追加：タイミング
//            newsButton, //ny　追加：ニュース
            taskButton,
            locationButton,
            usePurposeButton,  //s 追加：スマホ使用目的
            noteButton,  //s 追加：ロック画面で通知を確認したことによる開始か
            commentButton;  //s 押されると ListDialogFragment とかのフラグメントの表示に移る
    private Button[] interruptibilityButtonEx = new Button[5];  //s 追加：5つのボタン

    private Button[] timingButtonEx = new Button[5];  //ny 追加：タイミング用5つのボタン
    private Button[] newsButtonEx = new Button[5];  //ny 追加：ニュース用5つのボタン

    private boolean mode,  //s 割込み通知が一定時間無反応だった（回答時間が過ぎました）なら false …？（それ以外にも普通に用途がある気がする）
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

        //ny 追加：タイミングとニュース
//        timingButton = (Button) rootLayout.findViewById(R.id.interrupt);
//        newsButton = (Button) rootLayout.findViewById(R.id.interrupt);

        interruptibilityButton = (Button) rootLayout.findViewById(R.id.interrupt);
        taskButton = (Button) rootLayout.findViewById(R.id.task);
        locationButton = (Button) rootLayout.findViewById(R.id.location);
        usePurposeButton = (Button) rootLayout.findViewById(R.id.usePurpose);  //s 追加：スマホ使用目的
        commentButton = (Button) rootLayout.findViewById(R.id.comment);

        //ny　追加：タイミングとニュースへの興味への評価
        timingButtonEx[0] = (Button) rootLayout.findViewById(R.id.timing1);
        timingButtonEx[1] = (Button) rootLayout.findViewById(R.id.timing2);
        timingButtonEx[2] = (Button) rootLayout.findViewById(R.id.timing3);
        timingButtonEx[3] = (Button) rootLayout.findViewById(R.id.timing4);
        timingButtonEx[4] = (Button) rootLayout.findViewById(R.id.timing5);

        newsButtonEx[0] = (Button) rootLayout.findViewById(R.id.news1);
        newsButtonEx[1] = (Button) rootLayout.findViewById(R.id.news2);
        newsButtonEx[2] = (Button) rootLayout.findViewById(R.id.news3);
        newsButtonEx[3] = (Button) rootLayout.findViewById(R.id.news4);
        newsButtonEx[4] = (Button) rootLayout.findViewById(R.id.news5);

        //s 追加：interruptibilityButton を5つの押しボタンにバラしたver
        interruptibilityButtonEx[0] = (Button) rootLayout.findViewById(R.id.interrupt1);
        interruptibilityButtonEx[1] = (Button) rootLayout.findViewById(R.id.interrupt2);
        interruptibilityButtonEx[2] = (Button) rootLayout.findViewById(R.id.interrupt3);
        interruptibilityButtonEx[3] = (Button) rootLayout.findViewById(R.id.interrupt4);
        interruptibilityButtonEx[4] = (Button) rootLayout.findViewById(R.id.interrupt5);



        commentButton.setText(evaluationData.comment);
        commentButton.setOnClickListener(new View.OnClickListener() {
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
            //ny 使われていない
            interruptibilityButton.setText(String.valueOf(evaluationData.evaluation));
            interruptibilityButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String title = "現在の通知許容度は？\n1（問題なし）～5（嫌）";  //s 追加ここから
                    String[] selection = {
                            "1（忙しい）", "2", "3", "4", "5（大丈夫）"  //s 最初の1文字は半角数字（そのまま数値に変換される）
                    };  //s 追加ここまで
                    ListDialogFragment fragment = ListDialogFragment.newInstance(title, selection);  //s 変更；引数を上に分離
                    fragment.setTargetFragment(selfFragment, INTERRUPTIBILITY_REQUEST_CODE);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .add(fragment, INTERRUPTIBILITY)
                            .commit();
                }
            });

            //s 追加：5つのボタンにバラしたver
            //ny 追加：タイミングとニュースの評価
            int index_t = evaluationData.timing - 1;
            if (index_t < 0 || 4 < index_t) {
                index_t = 0;
            }
            timingButtonEx[index_t].setBackgroundResource(R.drawable.button_background_highlight);
            for (int i=0; i<5; i++) {
               timingButtonEx[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int index_t = 0;
                        for (int i=0; i<5; i++) {
                            int resource = R.drawable.button_background;
                            timingButtonEx[i].setBackgroundResource(resource);
                            if (view == timingButtonEx[i]) {
                                index_t = i;
                            }
                        }
                        view.setBackgroundResource(R.drawable.button_background_highlight);  //s 押されたやつをハイライト

                        evaluationData.timing = (index_t + 1);
                       //timingButton.setText(String.valueOf(index_t + 1));
                    }
                });
            }

            int index_n = evaluationData.news - 1;
            if (index_n < 0 || 4 < index_n) {
                index_n = 0;
            }
            newsButtonEx[index_n].setBackgroundResource(R.drawable.button_background_highlight);
            for (int i=0; i<5; i++) {
                newsButtonEx[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int index_n = 0;
                        for (int i=0; i<5; i++) {
                            int resource = R.drawable.button_background;
                            newsButtonEx[i].setBackgroundResource(resource);
                            if (view == newsButtonEx[i]) {
                                index_n = i;
                            }
                        }
                        view.setBackgroundResource(R.drawable.button_background_highlight);  //s 押されたやつをハイライト

                        evaluationData.news = (index_n + 1);
                        //interruptibilityButton.setText(String.valueOf(index + 1));
                    }
                });
            }

            int index = evaluationData.evaluation - 1;
            if (index < 0 || 4 < index) {
                index = 0;
            }
            interruptibilityButtonEx[index].setBackgroundResource(R.drawable.button_background_highlight);
            for (int i=0; i<5; i++) {
                interruptibilityButtonEx[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int index = 0;
                        for (int i=0; i<5; i++) {
                            int resource = R.drawable.button_background;
                            interruptibilityButtonEx[i].setBackgroundResource(resource);
                            if (view == interruptibilityButtonEx[i]) {
                                index = i;
                            }
                        }
                        view.setBackgroundResource(R.drawable.button_background_highlight);  //s 押されたやつをハイライト

                        evaluationData.evaluation = (index + 1);
                        //interruptibilityButton.setText(String.valueOf(index + 1));
                    }
                });
            }


            taskButton.setText(evaluationData.task);
            taskButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String title = "情報提供直前の活動内容は？";  //s 追加ここから
                    String[] selection = {
                            "テレビ", "PC", "スマホ", "家事", "移動", "会話", "休憩",  "自由記述"
                    };  //s 追加ここまで
                    ListDialogFragment fragment = ListDialogFragment.newInstance(title, selection);  //s 変更；引数を上に分離
                    fragment.setTargetFragment(selfFragment, TASK_REQUEST_CODE);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .add(fragment, TASK)
                            .commit();
                }
            });

            locationButton.setText(evaluationData.location);
            locationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String title = "現在の場所は？";  //s 追加ここから
                    String[] selection = {
                            "テーブル", "机", "ベッド", "キッチン",  "自由記述"
                    };  //s 追加ここまで
                    ListDialogFragment fragment = ListDialogFragment.newInstance(title, selection);  //s 変更；引数を上に分離
                    fragment.setTargetFragment(selfFragment, LOCATION_REQUEST_CODE);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .add(fragment, LOCATION)
                            .commit();
                }
            });

            //s 追加ここから：スマホ使用目的
            usePurposeButton.setText(evaluationData.usePurpose);
            usePurposeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String title = "スマホの使用目的は？\n（なるべく始めの下記から選択）";
                    String[] selection = {
                            "時間・通知の確認","情報消費","ブラウジング","コミュニケーション","ゲーム","記録・マネジメント","ショッピング・予約","設定","目的なし", "それ以外"
                    };
                    ListDialogFragment fragment = ListDialogFragment.newInstance(title, selection);
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
            TextView interruptibilitySubTextL = (TextView)rootLayout.findViewById(R.id.interruptibilitySubTextL);  //s 追加
            TextView interruptibilitySubTextR = (TextView)rootLayout.findViewById(R.id.interruptibilitySubTextR);  //s 追加
            TextView taskText = (TextView)rootLayout.findViewById(R.id.taskText);
            TextView locationText = (TextView)rootLayout.findViewById(R.id.locationText);
            TextView usePurposeText = (TextView)rootLayout.findViewById(R.id.usePurposeText);  //s 追加
            TextView commentText = (TextView)rootLayout.findViewById(R.id.commentText);

            rootLayout.removeView(interruptibilityText);
            rootLayout.removeView(interruptibilitySubTextL);  //s 追加
            rootLayout.removeView(interruptibilitySubTextR);  //s 追加
            rootLayout.removeView(taskText);
            rootLayout.removeView(locationText);
            rootLayout.removeView(usePurposeText);  //s 追加

            rootLayout.removeView(interruptibilityButton);
            for (int i=0; i<5; i++) {  //s 追加ここから
                rootLayout.removeView(interruptibilityButtonEx[i]);  //s なぜかこれで消えない（非常にバカっぽいコメント）
            }  //s 追加ここまで
            rootLayout.removeView(taskButton);
            rootLayout.removeView(locationButton);
            rootLayout.removeView(usePurposeButton);  //s 追加

            interruptibilityButton.setVisibility(View.INVISIBLE);
            for (int i=0; i<5; i++) {  //s 追加ここから
                interruptibilityButtonEx[i].setVisibility(View.GONE);  //s なぜか消えないので GONE を指定
            }  //s 追加ここまで
            taskButton.setVisibility(View.INVISIBLE);
            locationButton.setVisibility(View.INVISIBLE);
            usePurposeButton.setVisibility(View.INVISIBLE);  //s 追加

            interruptibilitySubTextL.setVisibility(View.GONE);  //s 追加（なぜか消えないので GONE を指定）
            interruptibilitySubTextR.setVisibility(View.GONE);  //s 追加（同上）

            commentText.setText("遅れた理由は？");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(rootLayout);
        if(mode){
            builder.setTitle("評価アンケート");//"話しかけを受け、\n5分間の作業中断発生");
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
                this.evaluationData.evaluation = Integer.parseInt(txt.substring(0, 1));  //s 変更：最初の1文字だけを数値に変換
                interruptibilityButton.setText(txt.substring(0, 1));  //s 変更：同上
                break;
            case TASK_REQUEST_CODE:
                this.evaluationData.task = txt;
                taskButton.setText(txt);
                break;
            case LOCATION_REQUEST_CODE:
                this.evaluationData.location = txt;
                locationButton.setText(txt);
                break;
            case USE_PURPOSE_REQUEST_CODE:  //s 追加ここから：スマホ使用目的
                this.evaluationData.usePurpose = txt;
                usePurposeButton.setText(txt);
                break;  //s 追加ここまで
            case COMMENT_REQUEST_CODE:
                this.evaluationData.comment = bundle.getString(InputDialogFragment.INPUT_TEXT, "");
                commentButton.setText(this.evaluationData.comment);
                break;
            default:
                break;
        }
    }
}
