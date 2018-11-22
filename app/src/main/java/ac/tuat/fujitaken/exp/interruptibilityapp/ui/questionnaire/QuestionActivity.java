package ac.tuat.fujitaken.exp.interruptibilityapp.ui.questionnaire;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;

import ac.tuat.fujitaken.exp.interruptibilityapp.interruption.NotificationController;
import ac.tuat.fujitaken.exp.interruptibilityapp.ui.main.MainActivity;
import ac.tuat.fujitaken.exp.interruptibilityapp.ui.questionnaire.fragments.QuestionFragment;
import ac.tuat.fujitaken.exp.interruptibilityapp.ui.questionnaire.fragments.QuestionFragment_Ex;

/**
 * 質問をするときのためのアクティビティ
 * マニフェストで独立起動するように設定
 * 中身は質問ダイアログのフラグメントを呼ぶだけ
 */
public class QuestionActivity extends FragmentActivity {
    public static final String QUESTION_DIALOG = "QUESTION_DIALOG",
            UPDATE_CANCEL = "INTERRUPT_UPDATE_CANCEL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(NotificationController.hasNotification) {

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(UPDATE_CANCEL);
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
            localBroadcastManager.sendBroadcast(broadcastIntent);

            /**
             * フラグメント作成
             * FragmentManagerからフラグメントを参照して存在していなかったら作成
             */
            FragmentManager manager = getSupportFragmentManager();
            if (manager.findFragmentByTag(QUESTION_DIALOG) == null) {
                manager.beginTransaction().add(QuestionFragment_Ex.newInstance(getIntent().getExtras()), QUESTION_DIALOG)  //s 変更：QF_Exに
                        .commit();
            }
        }
        else{
            Intent intent = new Intent(QuestionActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
