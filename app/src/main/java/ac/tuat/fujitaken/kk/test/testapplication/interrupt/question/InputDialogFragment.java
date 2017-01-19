package ac.tuat.fujitaken.kk.test.testapplication.interrupt.question;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import ac.tuat.fujitaken.kk.test.testapplication.R;

/**
 * 記述のためのダイアログ表示クラス
 */
public class InputDialogFragment extends DialogFragment {

    public static final String INPUT_TEXT = "INPUT_TEXT",
            TITLE = "TITLE_TEXT",
            INPUT_MODE = "inputMode",
            INITIAL_TEXT = "initialText";

    private EditText inputText;
    private int inputMode;
    private String title,
            initialText;

    public static InputDialogFragment newInstance(String title) {
        InputDialogFragment fragment = new InputDialogFragment();
        Bundle b = new Bundle();
        b.putString(TITLE, title);
        fragment.setArguments(b);
        return fragment;
    }

    public static InputDialogFragment newInstance(String title, int inputMode) {
        InputDialogFragment fragment = new InputDialogFragment();
        Bundle b = new Bundle();
        b.putInt(INPUT_MODE, inputMode);
        b.putString(TITLE, title);
        fragment.setArguments(b);
        return fragment;
    }

    public static InputDialogFragment newInstance(String title, String initialText, int inputMode) {
        InputDialogFragment fragment = new InputDialogFragment();
        Bundle b = new Bundle();
        b.putInt(INPUT_MODE, inputMode);
        b.putString(INITIAL_TEXT, initialText);
        b.putString(TITLE, title);
        fragment.setArguments(b);
        return fragment;
    }

    public InputDialogFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        title = getArguments().getString(TITLE, "");
        initialText = getArguments().getString(INITIAL_TEXT, "");
        inputMode = getArguments().getInt(INPUT_MODE, InputType.TYPE_CLASS_TEXT);
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View rootLayout;
        rootLayout = inflater.inflate(R.layout.input_dialog, null, false);
        inputText = (EditText)rootLayout.findViewById(R.id.inputText);

        inputText.setInputType(inputMode);
        inputText.setText(initialText);

        inputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return false;
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(rootLayout)
                .setTitle(title)
                .setPositiveButton(getString(R.string.positive_dialog_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(inputText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        onPositiveButtonClicked();
                    }
                });

        return builder.create();
    }

    private void onPositiveButtonClicked(){
        Intent result = new Intent();
        result.putExtra(INPUT_TEXT, inputText.getText().toString());

        if (getTargetFragment() != null) {
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, result);
        } else {
            PendingIntent pi = getActivity().createPendingResult(getTargetRequestCode(), result,
                    PendingIntent.FLAG_ONE_SHOT);
            try {
                pi.send(Activity.RESULT_OK);
            } catch (PendingIntent.CanceledException ex) {
                Log.e("Error", ex.getMessage());
            }
        }
    }
}
