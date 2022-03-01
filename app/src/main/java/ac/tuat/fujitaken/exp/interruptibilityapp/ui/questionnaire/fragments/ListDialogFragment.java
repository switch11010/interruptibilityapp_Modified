package ac.tuat.fujitaken.exp.interruptibilityapp.ui.questionnaire.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import ac.tuat.fujitaken.exp.interruptibilityapp.LogEx; //s 自作Log

/**
 * 選択ダイアログ
 */
//s QuestionFragment, _Ex 内で呼ばれる っぽい
//s 選択式のリスト を表示して選んでもらうフラグメント
public class ListDialogFragment extends DialogFragment {

    public static final String TITLE = "TITLE_TEXT",
            CONTENTS = "CONTENTS_TEXT",
            TEXT_RESULT = "TEXT_RESULT";

    public static final int INPUT_REQUEST_CODE = 951;

    private Fragment selffragment = this;
    private String title;
    private String[] contents;
    boolean mode;

    //s Fragment のインスタンスを用意して返す
    //s QuestionFragment, _Ex から呼ばれる
    //s title：ダイアログのてっぺんに表示する 説明文
    //s contents：選択式リストの 内容物
    public static ListDialogFragment newInstance(String title, String[] contents) {
        ListDialogFragment fragment = new ListDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TITLE, title);
        bundle.putStringArray(CONTENTS, contents);
        fragment.setArguments(bundle);
        return fragment;
    }

    public ListDialogFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selffragment = this;
        Bundle bundle = getArguments();
        title = bundle.getString(TITLE, "");
        contents = bundle.getStringArray(CONTENTS);
        mode = getTargetRequestCode() != QuestionFragment_Ex.INTERRUPTIBILITY_REQUEST_CODE;

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setItems(contents, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mode && which == contents.length - 1) {
                            InputDialogFragment fragment = InputDialogFragment.newInstance(getArguments().getString(TITLE, ""));
                            fragment.setTargetFragment(selffragment, INPUT_REQUEST_CODE);
                            getActivity().getSupportFragmentManager().beginTransaction()
                                    .add(fragment, InputDialogFragment.INPUT_TEXT)
                                    .commit();
                        } else {
                            Intent result = new Intent();
                            result.putExtra(TEXT_RESULT, contents[which]);
                            onPositiveButtonClicked(result);
                        }
                    }
                });
        return builder.create();
    }

    //s すぐ上の ラムダ式内の else の onClick() と すぐ下の onActivityResult() から呼ばれる
    private void onPositiveButtonClicked(Intent result){
        if (getTargetFragment() != null) {
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, result);
        } else {
            PendingIntent pi = getActivity().createPendingResult(getTargetRequestCode(), result,
                    PendingIntent.FLAG_ONE_SHOT);
            try {
                pi.send(Activity.RESULT_OK);
            } catch (PendingIntent.CanceledException ex) {
                LogEx.e("Error", ex.getMessage());
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle bundle = data.getExtras();
        if(requestCode == INPUT_REQUEST_CODE){
            Intent result = new Intent();
            result.putExtra(TEXT_RESULT, bundle.getString(InputDialogFragment.INPUT_TEXT, ""));
            onPositiveButtonClicked(result);
        }
    }
}
