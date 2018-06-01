package com.funaihui.ble;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class LeDeviceListAdapter extends BaseAdapter {

    private List<BluetoothDevice> deviceList;
    private Context mContext;

    public LeDeviceListAdapter(Context context) {
        mContext = context;
        deviceList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public BluetoothDevice getItem(int i) {
        return deviceList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_ble_device, null);

        }
        TextView tvMainDeviceName = view.findViewById(R.id.tvMainDeviceName);
        TextView tvMainDeviceNum = view.findViewById(R.id.tvMainDeviceNum);

        tvMainDeviceName.setText(getItem(position).getName());
        tvMainDeviceNum.setText(getItem(position).getAddress());

        return view;
    }

    public void addDevice(BluetoothDevice bluetoothDevice) {
        if (deviceList != null) {
            deviceList.add(bluetoothDevice);
        }
    }
}
