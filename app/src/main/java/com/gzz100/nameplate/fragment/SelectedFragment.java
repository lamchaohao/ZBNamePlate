package com.gzz100.nameplate.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gzz100.nameplate.R;
import com.gzz100.nameplate.activity.SelectAccountActivity;
import com.gzz100.nameplate.activity.SortNameActivity;
import com.gzz100.nameplate.adapter.Listener;
import com.gzz100.nameplate.adapter.SortAdapter;
import com.gzz100.nameplate.bean.Account;
import com.gzz100.nameplate.bean.Device;
import com.gzz100.nameplate.dao.AccountDao;
import com.gzz100.nameplate.dao.DeviceDao;
import com.gzz100.nameplate.global.Global;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class SelectedFragment extends Fragment implements Listener.OnUpwardListener,Listener.OnDownListener,Listener.OnItemClickListener{


    private static final int SELECT_ACCOUNT_CODE = 777;
    private View mTipsView;
    private AccountDao mAccountDao;
    private List<Account> mAccountList;
    private SortAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private int mSize;
    private List<Account> mOriginalAccounts;
    private int mPositionClick;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_selected_account, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.rvSelected);
        mTipsView = view.findViewById(R.id.rv_selected_tips);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.VERTICAL));
        mAccountDao = ((SortNameActivity) getActivity()).getAccountDao();
        DeviceDao deviceDao = ((SortNameActivity) getActivity()).getDeviceDao();
        List<Device> deviceList = deviceDao.queryBuilder().list();
        mOriginalAccounts = mAccountDao.queryBuilder().list();
        if (mOriginalAccounts.size()>deviceList.size()){
            mAccountList = mOriginalAccounts.subList(0, deviceList.size());
        }else {
            mAccountList=new ArrayList<>();
            mAccountList.addAll(mOriginalAccounts);
        }

        mSize=mAccountList.size()<deviceList.size() ?mAccountList.size() : deviceList.size();
        mAdapter = new SortAdapter(getActivity(), mAccountList,deviceList.size());
        mAdapter.setOnDownListener(this);
        mAdapter.setOnUpwardListener(this);
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);

        if (deviceList.size()==0) {
            mTipsView.setVisibility(View.VISIBLE);
        }else {
            mTipsView.setVisibility(View.GONE);
        }
    }


    public void addNewAccount(Account account){

        if (mAccountList.size()==0) {
            mTipsView.setVisibility(View.VISIBLE);
        }else {
            mTipsView.setVisibility(View.GONE);
            mAccountList.remove(0);
            mAdapter.notifyItemRemoved(0);
        }
        mAccountList.add(account);
        mAdapter.notifyItemInserted(mAccountList.size());
    }

    @Override
    public void onDownTap(Account account, int position) {
        if (position==mSize-1){
            mAccountList.remove(position);
            mAccountList.add(0,account);
            mAdapter.notifyItemRemoved(position);
            mAdapter.notifyItemInserted(0);
            mRecyclerView.smoothScrollToPosition(0);
        }else {
            Account nextAccount = mAccountList.get(position + 1);
            mAccountList.set(position,nextAccount);
            mAccountList.set(position+1,account);
            mAdapter.notifyItemChanged(position);
            mAdapter.notifyItemChanged(position+1);
        }
        mAdapter.notifyItemRangeChanged(0,mSize);
    }

    @Override
    public void onUpTap(Account account, int position) {
        if (position==0){
            mAccountList.remove(0);
            mAccountList.add(mSize-1,account);
            mAdapter.notifyItemRemoved(position);
            mAdapter.notifyItemInserted(mSize-1);

        }else {
            Account lastAccount = mAccountList.get(position - 1);
            mAccountList.set(position,lastAccount);
            mAccountList.set(position-1,account);
            mAdapter.notifyItemChanged(position);
            mAdapter.notifyItemChanged(position-1);
        }
        mAdapter.notifyItemRangeChanged(0,mSize);
    }

    public void saveSortNum(){

        for (Account originalAccount : mOriginalAccounts) {
            originalAccount.setSortNumber(1000);
        }
        mAccountDao.insertOrReplaceInTx(mOriginalAccounts);

        for (int i = 0; i < mAccountList.size(); i++) {
            if (i<mSize){
                mAccountList.get(i).setSortNumber(i);
            }
        }
        mAccountDao.insertOrReplaceInTx(mAccountList);
    }

    @Override
    public void onItemClick(View view, int position) {
        mPositionClick = position;
        Intent intent = new Intent(getContext(), SelectAccountActivity.class);
        startActivityForResult(intent,SELECT_ACCOUNT_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode==RESULT_OK&&requestCode==SELECT_ACCOUNT_CODE){
            long accountId = data.getLongExtra(Global.EX_ACCOUNT_ID, -1);
            for (Account account : mOriginalAccounts) {
                if (account.getId() == accountId) {
                    mAccountList.set(mPositionClick,account);
                    mAdapter.notifyItemChanged(mPositionClick);
                    break;
                }
            }

        }
    }
}
