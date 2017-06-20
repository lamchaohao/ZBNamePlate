package com.gzz100.nameplate.fragment;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gzz100.nameplate.App;
import com.gzz100.nameplate.R;
import com.gzz100.nameplate.activity.AddFromFileActivity;
import com.gzz100.nameplate.activity.EditActivity;
import com.gzz100.nameplate.activity.MultiSendActivity;
import com.gzz100.nameplate.adapter.AccountAdapter;
import com.gzz100.nameplate.adapter.Listener;
import com.gzz100.nameplate.bean.Account;
import com.gzz100.nameplate.dao.AccountDao;
import com.gzz100.nameplate.global.Global;
import com.gzz100.nameplate.utils.connect.SendDataUtil;
import com.gzz100.nameplate.utils.genfile.DrawBitmapUtil;
import com.gzz100.nameplate.utils.genfile.GenFileUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.gzz100.nameplate.global.Global.CONNECT_NORESPONE;
import static com.gzz100.nameplate.global.Global.CONN_ERRO;
import static com.gzz100.nameplate.global.Global.GENFILE_DONE;
import static com.gzz100.nameplate.global.Global.UPDATE_PROGRESS;
import static com.gzz100.nameplate.global.Global.WIFI_ERRO;

public class AccountFragment extends Fragment implements Listener.OnItemClickListener,
        Listener.OnItemLongClickListener,
        Listener.OnEditClickListener,
        Listener.OnSendClickListener, View.OnClickListener ,View.OnLongClickListener{


    private static final int EDIT_ACCOUNT_CODE = 201;
    private static final int REQUEST_ADD_CODE = 230;
    @BindView(R.id.fab_account_add)
    FloatingActionButton mFabAccountAdd;
    @BindView(R.id.fab_account_send)
    FloatingActionButton mFabAccountSend;
    @BindView(R.id.fab_account_delete)
    FloatingActionButton mFabAccountDelete;
    @BindView(R.id.fab_account_addFromfile)
    FloatingActionButton mFabAccountAddFromfile;
    private AccountDao mAccountDao;
    private List<Account> mAccountList;
    private AccountAdapter mAdapter;
    private int mPosition;
    private boolean isSending;
    private RecyclerView mRecyclerView;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadData();
    }

    private void loadData() {
        mAccountDao = ((App) getActivity().getApplication()).getDaoSession().getAccountDao();
        mAccountList = mAccountDao.queryBuilder().list();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accout, container, false);
        ButterKnife.bind(this, view);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.rcv_account);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new AccountAdapter(getContext(), mAccountList);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);
        mAdapter.setOnItemLongClickListener(this);
        mAdapter.setOnEditClickListener(this);
        mAdapter.setOnSendClickListener(this);
        mFabAccountDelete.setOnLongClickListener(this);
    }

    @OnClick({R.id.fab_account_add, R.id.fab_account_send, R.id.fab_account_delete,R.id.fab_account_addFromfile})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_account_add:
                if (!mAdapter.isMutilChoiceMode()) {
                    Account account = new Account();
                    account.setId(System.currentTimeMillis());
                    account.setAccountName(getString(R.string.newName));
                    mAccountList.add(account);
                    mAccountDao.insert(account);
                    mAdapter.notifyItemInserted(mAccountList.size());
                    mRecyclerView.smoothScrollToPosition(mAccountList.size());
                }
                break;
            case R.id.fab_account_send:
                startActivity(new Intent(getActivity(), MultiSendActivity.class));
                break;
            case R.id.fab_account_addFromfile:
                Intent intent = new Intent(getActivity(), AddFromFileActivity.class);
                startActivityForResult(intent,REQUEST_ADD_CODE);
                break;
            case R.id.fab_account_delete:
                if (mAdapter.isMutilChoiceMode()) {
                    boolean[] isSelected = mAdapter.getIsSelected();
                    List<Account> toDelete = new ArrayList<>();
                    for (int i = 0; i < isSelected.length; i++) {
                        if (isSelected[i]) {
                            toDelete.add(mAccountList.get(i));
                        }
                    }
                    for (Account tode : toDelete) {
                        mAccountDao.delete(tode);
                        mAccountList.remove(tode);
                    }
                    if (toDelete.size() != 0)
                        Snackbar.make(mRecyclerView, R.string.deletedSelect, Snackbar.LENGTH_SHORT).show();
                    mAdapter.setMutilChoiceMode(false);
                    mAdapter.notifyDataSetChanged();
                } else {
                    boolean[] selected = new boolean[mAccountList.size()];
                    mAdapter.setIsSelected(selected);
                    mAdapter.setMutilChoiceMode(true);
                    mAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    @Override
    public void onEditClick(View v, final int position) {

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext());
        builder.title(R.string.editContent)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(getString(R.string.inputHint), mAccountList.get(position).getAccountName(), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        String newName = input.toString();
                        Account account = mAccountList.get(position);
                        account.setAccountName(newName);
                        mAccountDao.insertOrReplace(account);
                        mAdapter.notifyItemChanged(position);
                    }
                })
                .positiveText(R.string.comfirm)
                .onPositive(null)
                .negativeText(R.string.cancle)
                .onNegative(null)
                .show();
    }

    @Override
    public void onItemClick(View v, int position) {
        mPosition = position;
        Intent intent = new Intent(getContext(), EditActivity.class);
        intent.putExtra(Global.EX_ACCOUNT_ID, mAccountList.get(position).getId());
        startActivityForResult(intent, EDIT_ACCOUNT_CODE);
    }

    @Override
    public void onLongClick(View v, final int position) {
        final Account account = mAccountList.get(position);
        new AlertDialog.Builder(getContext())
                .setMessage(getString(R.string.delete)+"\" " + account.getAccountName() + "\"?")
                .setPositiveButton(R.string.comfirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String accountName = account.getAccountName();
                        mAccountDao.delete(account);
                        mAccountList.remove(position);
                        mAdapter.notifyItemRemoved(position);
                        Snackbar.make(mRecyclerView, accountName + getString(R.string.deleted), Snackbar.LENGTH_SHORT).show();
                        mAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton(R.string.cancle, null)
                .show();

    }

    @Override
    public void onSendClick(View v, int position) {
        Account account = mAccountList.get(position);
        DrawBitmapUtil drawBitmapUtil = new DrawBitmapUtil(getActivity(),account);
        Bitmap bitmap = drawBitmapUtil.drawBitmap();
        if (isSending) {
            Snackbar.make(mRecyclerView, R.string.tos_alreadySend, Snackbar.LENGTH_SHORT).show();
        } else {
            GenFileUtil genFileUtil = new GenFileUtil(getContext(), bitmap, mHandler);
            genFileUtil.startGenFile();
            isSending = true;
            Snackbar.make(mRecyclerView, R.string.startSend, Snackbar.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == getActivity().RESULT_OK && requestCode == EDIT_ACCOUNT_CODE) {
            String newName = data.getStringExtra(Global.EX_NEW_NAME);
            Account account = mAccountList.get(mPosition);
            account.setAccountName(newName);
            mAccountDao.insertOrReplace(account);
            mAdapter.notifyItemChanged(mPosition);
        }else if (resultCode == getActivity().RESULT_OK && requestCode == REQUEST_ADD_CODE){
            mAccountList.clear();
            List<Account> list = mAccountDao.queryBuilder().list();
            mAccountList.addAll(list);
            mAdapter.notifyDataSetChanged();
        }
    }

    public boolean isDeleteMode() {
        return mAdapter.isMutilChoiceMode();
    }

    public void cancleDeleteMode() {
        mAdapter.setMutilChoiceMode(false);
        mAdapter.notifyDataSetChanged();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GENFILE_DONE:
                    SendDataUtil sendDataUtil = new SendDataUtil(getContext(), this);
                    sendDataUtil.send();
                    break;
                case UPDATE_PROGRESS:
                    int arg1 = msg.arg1;
                    if (arg1 == 100) {
                        isSending = false;
                        Snackbar.make(mRecyclerView, R.string.sent, Snackbar.LENGTH_SHORT).show();
                    }

                    break;
                case WIFI_ERRO:
                    isSending = false;
                    Toast.makeText(getContext(), R.string.tos_wifiErro, Toast.LENGTH_LONG).show();
                    getContext().startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    break;
                case CONN_ERRO:
                    isSending = false;
                    Toast.makeText(getContext(), R.string.tos_connectTimeout, Toast.LENGTH_LONG).show();
                    break;
                case CONNECT_NORESPONE:
                    isSending = false;
                    Snackbar.make(mRecyclerView, R.string.tos_connectTimeout, Snackbar.LENGTH_LONG).show();
                    break;
            }
        }
    };

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.fab_account_delete:
                new AlertDialog.Builder(getContext())
                        .setMessage(R.string.deletd_all_comfirm)
                        .setPositiveButton(R.string.delete_all, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mAccountDao.deleteInTx(mAccountList);
                                mAccountList.clear();
                                mAdapter.notifyDataSetChanged();
                                Snackbar.make(mRecyclerView, R.string.delete_all_success, Snackbar.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(R.string.cancle, null)
                        .show();
                break;

        }
        return true;
    }
}
