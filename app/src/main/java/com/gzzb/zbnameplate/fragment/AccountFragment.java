package com.gzzb.zbnameplate.fragment;


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
import com.gzzb.zbnameplate.App;
import com.gzzb.zbnameplate.R;
import com.gzzb.zbnameplate.activity.EditActivity;
import com.gzzb.zbnameplate.adapter.AccountAdapter;
import com.gzzb.zbnameplate.bean.Account;
import com.gzzb.zbnameplate.dao.AccountDao;
import com.gzzb.zbnameplate.global.Global;
import com.gzzb.zbnameplate.utils.DrawBitmapUtil;
import com.gzzb.zbnameplate.utils.GenFileUtil;
import com.gzzb.zbnameplate.utils.SendDataUtil;

import java.util.List;

import static com.gzzb.zbnameplate.utils.GenFileUtil.START_SEND_DATA;
import static com.gzzb.zbnameplate.utils.SendDataUtil.CONN_ERRO;
import static com.gzzb.zbnameplate.utils.SendDataUtil.UPDATE_PROGRESS;
import static com.gzzb.zbnameplate.utils.SendDataUtil.WIFI_ERRO;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment implements AccountAdapter.OnItemClickListener,
        AccountAdapter.OnItemLongClickListener,
        AccountAdapter.OnEditClickListener,
        AccountAdapter.OnSendClickListener ,View.OnClickListener{


    private static final int EDIT_ACCOUNT_CODE = 201;
    private FloatingActionButton mFab;
    private AccountDao mAccountDao;
    private List<Account> mAccountList;
    private AccountAdapter mAdapter;
    private int mPosition;
    private boolean isSending;

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
        initView(view);
        return view;
    }

    private void initView(View view) {
        mFab = (FloatingActionButton) view.findViewById(R.id.fab_account);
        mFab.setOnClickListener(this);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rcv_account);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new AccountAdapter(getContext(),mAccountList);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);
        mAdapter.setOnItemLongClickListener(this);
        mAdapter.setOnEditClickListener(this);
        mAdapter.setOnSendClickListener(this);
        View tbTips = view.findViewById(R.id.tv_account_tips);
        if (mAccountList.size()==0){
            tbTips.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onEditClick(View v, final int position) {

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext());
        builder.title("修改名字")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("请输入名字", mAccountList.get(position).getAccountName(), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        String newName = input.toString();
                        Account account = mAccountList.get(position);
                        account.setAccountName(newName);
                        mAccountDao.insertOrReplace(account);
                        mAdapter.notifyItemChanged(position);
                    }
                })
                .positiveText("确定")
                .onPositive(null)
                .negativeText("取消")
                .onNegative(null)
                .show();
    }

    @Override
    public void onClick(View v, int position) {
        mPosition = position;
        Intent intent = new Intent(getContext(), EditActivity.class);
        intent.putExtra(Global.EX_ACCOUNT_ID,mAccountList.get(position).getId());
        startActivityForResult(intent,EDIT_ACCOUNT_CODE);
    }

    @Override
    public void onLongClick(View v, final int position) {
        final Account account = mAccountList.get(position);
        new AlertDialog.Builder(getContext())
                .setMessage("确定删除 \" " + account.getAccountName() + "\"?")
                .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String accountName = account.getAccountName();
                        mAccountDao.delete(account);
                        mAccountList.remove(position);
                        mAdapter.notifyDataSetChanged();
                        Snackbar.make(mFab,accountName+"已删除",Snackbar.LENGTH_SHORT).show();
                    }})
                .setNegativeButton("取消", null)
                .show();

    }

    @Override
    public void onSendClick(View v, int position) {
        Account account = mAccountList.get(position);
        String name = account.getAccountName();
        DrawBitmapUtil drawBitmapUtil = new DrawBitmapUtil(name);
        drawBitmapUtil.setItalic(account.getIsItalic());
        drawBitmapUtil.setBold(account.getIsBold());
        drawBitmapUtil.setUnderline(account.getIsUnderline());
        Bitmap bitmap = drawBitmapUtil.drawBitmap();
        if (isSending) {
            Snackbar.make(mFab,"已经在发送",Snackbar.LENGTH_SHORT).show();
        }else {
            GenFileUtil genFileUtil = new GenFileUtil(getContext(),bitmap,mHandler);
            genFileUtil.startGenFile();
            isSending = true;
            Snackbar.make(mFab,"开始发送 ",Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        Account account=new Account();
        account.setId(System.currentTimeMillis());
        account.setAccountName("新建");
        mAccountList.add(account);
        mAccountDao.insert(account);
        mAdapter.notifyItemChanged(mAccountList.size());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode==getActivity().RESULT_OK&&requestCode==EDIT_ACCOUNT_CODE){
            String newName = data.getStringExtra(Global.EX_NEW_NAME);
            Account account = mAccountList.get(mPosition);
            account.setAccountName(newName);
            mAccountDao.insertOrReplace(account);
            mAdapter.notifyItemChanged(mPosition);
        }
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_SEND_DATA:
                    SendDataUtil sendDataUtil = new SendDataUtil(getContext(),this);
                    sendDataUtil.send();
                    break;
                case UPDATE_PROGRESS:
                    int arg1 = msg.arg1;
                    if (arg1==100){
                        isSending = false;
                        Snackbar.make(mFab,"已发送 ",Snackbar.LENGTH_SHORT).show();
                    }

                    break;
                case WIFI_ERRO:
                    isSending = false;
                    Toast.makeText(getContext(),"所连接WiFi非本公司产品，请切换WiFi",Toast.LENGTH_LONG).show();
                    getContext().startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    break;
                case CONN_ERRO:
                    isSending = false;
                    Toast.makeText(getContext(),"连接超时，请重试",Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

}
