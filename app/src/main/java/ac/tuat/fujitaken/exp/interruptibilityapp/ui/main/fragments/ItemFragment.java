package ac.tuat.fujitaken.exp.interruptibilityapp.ui.main.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import ac.tuat.fujitaken.exp.interruptibilityapp.R;
import ac.tuat.fujitaken.exp.interruptibilityapp.ui.main.MainActivity;
import ac.tuat.fujitaken.exp.interruptibilityapp.data.EventCounter;
import ac.tuat.fujitaken.exp.interruptibilityapp.ui.questionnaire.fragments.InputDialogFragment;
import ac.tuat.fujitaken.exp.interruptibilityapp.ui.main.EventAdapter;

import java.util.Map;

/**
 * イベント数をリスト表示するフラグメント
 */
public class ItemFragment extends ListFragment {

    //Bundleからデータを受け取るための定数
    private static final String ARG_POSITION = "param1";
    private static final int INPUT_REQUEST_CODE = 268;

    private EventAdapter adapter;
    private String item;
    private EventCounter counter;

    public ItemFragment() {
    }

    @SuppressWarnings("unused")
    public static ItemFragment newInstance(int position) {
        ItemFragment fragment = new ItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //ドロワーの更新用
        ((MainActivity) getActivity()).onSectionAttached(getArguments().getInt(ARG_POSITION));

        //リストにアダプターを設定
        adapter = new EventAdapter(getActivity().getApplicationContext(), R.layout.item_layout);
        setListAdapter(adapter);

        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        counter = new EventCounter(getActivity().getApplicationContext());
        update();
    }

    /**
     * クリックされたイベント数を変更する．
     * デバッグや，データを消してしまった時のため．
     * @param l リスト
     * @param v クリックされたView
     * @param position クリックされたアイテムの場所
     * @param id アイテムのID
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if(!SettingFragment.isServiceActive(getActivity().getApplicationContext())) {
            //クリックされたアイテムの取得
            item = (String) ((Map.Entry) l.getItemAtPosition(position)).getKey();
            int t = counter.getEvaluations(item);

            //数値入力のためのダイアログ作成
            InputDialogFragment fragment = InputDialogFragment.newInstance(item, String.valueOf(t), InputType.TYPE_CLASS_NUMBER);
            fragment.setTargetFragment(this, INPUT_REQUEST_CODE);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .add(fragment, "Setting")
                    .commit();
        }
    }

    /**
     * ダイアログの結果を受け取る
     * @param requestCode 結果を選択するためのリクエストコード
     * @param resultCode 結果が得られたかどうか
     * @param data データを格納したIntent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle bundle = data.getExtras();
        if(requestCode == INPUT_REQUEST_CODE) {
            String result = bundle.getString(InputDialogFragment.INPUT_TEXT, "0");
            if (!"".equals(result)) {
                //結果が有効な数値なら，変換して置き換える
                try {
                    int t = Integer.parseInt(result);
                    counter.putEvaluation(item, t);
                    update();
                }catch (NumberFormatException e){
                    Log.e("InputFormatError", e.getMessage());
                }
            }
        }
    }

    /**
     * オプションメニューをセットする
     * @param menu メニュー
     * @param inflater Inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.count_menu, menu);
    }

    /**
     * オプションメニューが押されたとき
     * @param item 押されたアイテム
     * @return イベントを消化したらtrue
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(!SettingFragment.isServiceActive(getActivity().getApplicationContext())) {

            //イベント数をクリアする
            if (item.getItemId() == R.id.action_clear) {
                new AlertDialog.Builder(getActivity()).setTitle("Clear")
                        .setMessage("イベントの回数を0にします")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                counter.initialize();
                                update();
                            }
                        })
                        .setCancelable(true)
                        .show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    //リストの更新
    private void update(){
        adapter.clear();
        adapter.addAll(counter.getEvaluations().entrySet());
    }
}
