package com.gzz100.nameplate.fragment;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gzz100.nameplate.R;
import com.gzz100.nameplate.activity.SortNameActivity;
import com.gzz100.nameplate.adapter.Listener;
import com.gzz100.nameplate.adapter.SortAdapter;
import com.gzz100.nameplate.bean.Account;
import com.gzz100.nameplate.dao.AccountDao;

import java.util.List;


public class CandidateAccountFragment extends Fragment implements Listener.OnItemClickListener{

    private SortAdapter mAdapter;
    private View mTipsView;
    private AccountDao mAccountDao;
    private List<Account> mAccountList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_candidate_account, container, false);
        loadData();
        initView(view);
        return view;
    }

    private void initView(View view) {
        RecyclerView rvAvail = (RecyclerView) view.findViewById(R.id.rvCandidate);
        mTipsView = view.findViewById(R.id.rv_candidate_tips);
        rvAvail.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.VERTICAL));
        mAdapter =new SortAdapter(getActivity(),mAccountList,0);
        mAdapter.setOnItemClickListener(this);

        rvAvail.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvAvail.setAdapter(mAdapter);

        if (mAccountList.size()==0) {
            mTipsView.setVisibility(View.VISIBLE);
        }else {
            mTipsView.setVisibility(View.GONE);
        }

    }
    private void loadData() {
        mAccountDao = ((SortNameActivity) getActivity()).getAccountDao();
        mAccountList = mAccountDao.queryBuilder().list();

    }




    @Override
    public void onItemClick(View view, int position) {
        Account account = mAccountList.get(position);
        ((SortNameActivity)getActivity()).addNewAccount(account);
        Snackbar.make(mTipsView,R.string.addedToSelected,Snackbar.LENGTH_SHORT).show();
    }

}
