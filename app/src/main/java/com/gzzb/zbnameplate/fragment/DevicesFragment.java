package com.gzzb.zbnameplate.fragment;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gzzb.zbnameplate.App;
import com.gzzb.zbnameplate.R;
import com.gzzb.zbnameplate.adapter.DeviceAdapter;
import com.gzzb.zbnameplate.adapter.Listener;
import com.gzzb.zbnameplate.bean.Device;
import com.gzzb.zbnameplate.dao.DeviceDao;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class DevicesFragment extends Fragment implements Listener.OnItemClickListener,Listener.OnEditClickListener,Listener.OnItemLongClickListener{


    private DeviceDao mDeviceDao;
    private DeviceAdapter mAdapter;
    private List<Device> mDeviceList;
    private View mTipsView;

    public DevicesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_devices, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rvDevices);
        mTipsView = view.findViewById(R.id.rv_devices_tips);
        mDeviceDao = ((App) getActivity().getApplication()).getDaoSession().getDeviceDao();
        mDeviceList = mDeviceDao.queryBuilder().list();
        if (mDeviceList.size()==0) {
            mTipsView.setVisibility(View.VISIBLE);
        }else {
            mTipsView.setVisibility(View.GONE);
        }
        mAdapter = new DeviceAdapter(mDeviceList,getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mAdapter);
        recyclerView.setHasFixedSize(true);//设定高度固定,可提高效率
        mAdapter.setEditListener(this);
        mAdapter.setItemListener(this);
        mAdapter.setLongClickListener(this);
    }

    @Override
    public void onItemClick(View v, int pos) {
        onEditClick(v,pos);
    }

    @Override
    public void onEditClick(View v, final int pos) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext());
        builder.title("修改设备名称")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("请输入设备名称", mDeviceList.get(pos).getDeviceName(), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        String newName = input.toString();
                        Device device = mDeviceList.get(pos);
                        device.setDeviceName(newName);
                        mDeviceDao.insertOrReplace(device);
                        mAdapter.notifyItemChanged(pos);
                    }
                })
                .positiveText("确定")
                .onPositive(null)
                .negativeText("取消")
                .onNegative(null)
                .show();
    }

    @Override
    public void onLongClick(View v, final int pos) {
        final Device device = mDeviceList.get(pos);
        new AlertDialog.Builder(getActivity())
                .setTitle("删除设备")
                .setMessage("确定要删除"+device.getDeviceName())
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDeviceList.remove(device);
                        mDeviceDao.delete(device);
                        mAdapter.notifyItemRemoved(pos);
                    }
                })
                .setNegativeButton("取消",null)
                .show();
        if (mDeviceList.size()==0) {
            mTipsView.setVisibility(View.VISIBLE);
        }else {
            mTipsView.setVisibility(View.GONE);
        }
    }

    public void addNewDevice(Device device){
        mDeviceList.add(device);
        if (mDeviceList.size()==0) {
            mTipsView.setVisibility(View.VISIBLE);
        }else {
            mTipsView.setVisibility(View.GONE);
        }
        mAdapter.notifyItemInserted(mDeviceList.size());
    }
}
