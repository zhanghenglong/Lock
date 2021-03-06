package com.anda.smartlock.ui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.anda.smartlock.R;
import com.anda.smartlock.consts.BLEUUID;
import com.anda.smartlock.consts.FP_State;
import com.anda.smartlock.data.FingerPrint;
import com.anda.smartlock.data.FingerPrintRepo;
import com.anda.smartlock.protocol.Lock;
import com.iiseeuu.rootview.RootLayout;

import java.util.ArrayList;
import java.util.UUID;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**==============================================================================**
 * 类名：HomeActivity
 * 类功能：程序界面的主页面activity
 * 范围：public类
 **==============================================================================**/
public class HomeActivity extends AppCompatActivity{
    private static final String TAG = HomeActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 2;
    // Stops scanning after 5 seconds.
    private static final long SCAN_PERIOD = 2000;
    private static final long CONN_PERIOD = 3000;
    private static final long INMODE_PERIOD = 3000;
    private static final long WAITING_DEL_PERIOD = 10000;
    private static short seq = 1;
    private ViewHolder vh;
    private SweetAlertDialog pDialog;
    private String selected_id;
    private String selected_name;
    private int state = FP_State.IDLE;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    //Parameter:======================= Code to manage Service lifecycle.=======================//
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            System.out.println("进入onServiceConnected状态");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder)service).getService();
            if(mBluetoothLeService == null)
                System.out.println("执行连接服务中获取服务失败返回空值");
            System.out.println("执行连接服务");
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
          //  mBluetoothLeService.connect(mDeviceAddress);
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            System.out.println("未连接服务");
            mBluetoothLeService = null;
        }
    };
    //Parameter:======================= Device OnCheckedChangeListener. =======================//
    protected CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!isChecked) {
                mBluetoothLeService.disconnect();
                findViewById(R.id.layout_del).setVisibility(View.INVISIBLE);
                findViewById(R.id.layout_add).setVisibility(View.INVISIBLE);
                buttonView.setText("未连接");
                vh.sw_conn.setClickable(false);
            }
        }
    };
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private boolean mWaitingAdd = false;
    private boolean mWaitingDel = false;
    private BluetoothGattService mBluetoothLeGattaService;
    private boolean mConnected = false;
    //Parameter:======================= Device OnClickListener. =======================//
    public View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mConnected) {
                Toast.makeText(HomeActivity.this, "已有设备连接，请先断开其他设备！", Toast.LENGTH_SHORT).show();
                return;
            }
            int position = v.getId();
            vh = (ViewHolder) v.getTag();
            boolean isChecked = vh.sw_conn.isChecked();
            System.out.println("已经点击了选取item");
            final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
            if (device == null) {
                Log.d(TAG, "device为空");
                return;
            }
            //invoke service to do
            if (mBluetoothLeService != null) {
                mDeviceAddress = device.getAddress();
                Log.d(TAG, "已经选择要连接的设备" + mDeviceAddress);
                mBluetoothLeService.connect(mDeviceAddress);
                pDialog = showProcessDialog("连接智能锁中...");
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pDialog.dismissWithAnimation();
                        if (!mConnected)
                            Toast.makeText(HomeActivity.this, "无法连接选择的设备，请检查设备！", Toast.LENGTH_SHORT).show();
                    }
                }, CONN_PERIOD);

            } else {
                Log.d(TAG, "BluetoothLeService为空");
            }
        }
    };
    //Parameter:===================== Handles various events fired by the Service.=====================//
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                //  mConnected = true;
             //   updateConnectionState(R.string.connected);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
            //    updateConnectionState(R.string.disconnected);
            }
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                mBluetoothLeGattaService = mBluetoothLeService.getSupportedGattServices(UUID.fromString(BLEUUID.SERVICE));
                if(mBluetoothLeGattaService == null) {
                    Toast.makeText(HomeActivity.this, "无法识别服务，请确认连接的是否是智能锁设备", Toast.LENGTH_SHORT).show();
                    // LogWriter.showLog("无法识别服务，请确认连接的是否是智能锁设备");
                } else {
                    mNotifyCharacteristic = mBluetoothLeGattaService.getCharacteristic(UUID.fromString(BLEUUID.CHARACTER));
                    if (mNotifyCharacteristic == null) {
                        Toast.makeText(HomeActivity.this, "无法获取FFE1的读写通知特性！", Toast.LENGTH_SHORT).show();
                        //   LogWriter.showLog("无法获取FFE1的读写通知特性！");
                    } else {
                        //set the readUnblockCharacteristic as true
                        mConnected = true;
                        mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, true);
                        updateConnectionState(R.string.connected);
                    }
                }
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                if(data == null)
                    Log.d(TAG, "ACTION_DATA_AVAILABLE的广播数据返回的为空值，请检查回调函数是否有数据返回");
                Log.d(TAG, "指纹流程已经收到数据：" + new String(data));
                //处理收到的数据
                handleMsg(data);
            }
        }
    };
    //Parameter:======================= Device scan callback. =======================//
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };
    /**==============================================================================**
     * 函数名：makeGattUpdateIntentFilter
     * 函数功能：过滤
     * 全局变量：
     * 输入参数：null
     * 返回值：IntentFilter
     **==============================================================================**/
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
    /**==============================================================================**
     * 函数名：onCreate
     * 函数功能：activity的onCreat流程
     * 全局变量：
     * 输入参数：savedInstanceState -- Bundle
     * 返回值：void
     **==============================================================================**/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        RootLayout.getInstance(this).setTitleBar("SMART LOCK");
        findViewById(R.id.layout_del).setVisibility(View.INVISIBLE);
        findViewById(R.id.layout_add).setVisibility(View.INVISIBLE);
        //判断系统是否支持BLE或者是否开启蓝牙
        initBLE();

        mHandler = new Handler();
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        Intent gattServiceIntent = new Intent(HomeActivity.this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        findViewById(R.id.btn_scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (state != FP_State.IDLE)
                    return;
                if (!mConnected) {
                    mLeDeviceListAdapter.clear();
                    mLeDeviceListAdapter.notifyDataSetChanged();
                    scanLeDevice(true);
                } else {
                    Toast.makeText(HomeActivity.this, "已连接设备，请先断开设备后再执行扫描！", Toast.LENGTH_SHORT).show();
                }
            }
        });
        findViewById(R.id.btn_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (state != FP_State.IDLE)
                    return;
                sendMsg2Lock(Lock.CmdId.sendAddNotificationToLock, null, seq++, (short) 0);
                mWaitingAdd = true;
                pDialog = showProcessDialog("开启录指纹模式...");
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mWaitingAdd = false;
                        if (state == FP_State.IDLE) {
                            pDialog.dismissWithAnimation();
                            Toast.makeText(HomeActivity.this, "设备开启录指纹模式失败，请检查设备！", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, INMODE_PERIOD);
            }
        });
        findViewById(R.id.btn_del).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (state != FP_State.IDLE)
                    return;
                FingerPrintRepo fp_repo = new FingerPrintRepo(HomeActivity.this);
                if (fp_repo.getFingerPrintList().size() != 0) {
                    sendMsg2Lock(Lock.CmdId.sendDelNotificationToLock, null, seq++, (short) 0);
                    mWaitingDel = true;
                    pDialog = showProcessDialog("开启删指纹模式...");
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mWaitingDel = false;
                            if (state == FP_State.IDLE) {
                                pDialog.dismissWithAnimation();
                                Toast.makeText(HomeActivity.this, "设备开启删指纹模式失败，请检查设备！", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, INMODE_PERIOD);
                } else {
                    Toast.makeText(HomeActivity.this, "无法进入删除指纹模式，指纹库为空，请添加指纹！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ListView list_dev = (ListView)findViewById(R.id.list_dev);
        list_dev.setAdapter(mLeDeviceListAdapter);
    }
    /**==============================================================================**
     * 函数名：initBLE
     * 函数功能：初始化BLUETOOTH
     * 全局变量：
     * 输入参数：
     * 返回值：void
     **==============================================================================**/
    private void initBLE() {
        // Use this check to determine whether BLE is supported on the device.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        // Initializes a Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }
    /**==============================================================================**
     * 函数名：onResume
     * 函数功能：activity的onResume流程
     * 全局变量：
     * 输入参数：
     * 返回值：void
     **==============================================================================**/
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }
    /**==============================================================================**
     * 函数名：onPause
     * 函数功能：activity的onResume流程
     * 全局变量：
     * 输入参数：
     * 返回值：void
     **==============================================================================**/
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }
    /**==============================================================================**
     * 函数名：onDestroy
     * 函数功能：activity的onDestroy流程
     * 全局变量：
     * 输入参数：
     * 返回值：void
     **==============================================================================**/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }
    /**==============================================================================**
     * 函数名：onActivityResult
     * 函数功能：activity的onDestroy流程
     * 全局变量：
     * 输入参数：
     * 返回值：void
     **==============================================================================**/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        if (requestCode == 0 && resultCode == Activity.RESULT_CANCELED) {
            state = FP_State.IDLE;
        }
        if (requestCode == 1 && resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(HomeActivity.this, "退出删指纹模式！", Toast.LENGTH_SHORT).show();
            state = FP_State.IDLE;
            sendMsg2Lock(Lock.CmdId.pushToLock, null, (short) 0, (short) 8);
        }
        if(requestCode == 0 && resultCode == Activity.RESULT_OK){
            state = FP_State.IDLE;
            System.out.println(data.getStringExtra(AddProActivity.EXTRAS_FingerPrint_NAME) + "的指纹录取成功！");
            Toast.makeText(this, data.getStringExtra(AddProActivity.EXTRAS_FingerPrint_NAME) + "的指纹录取成功！", Toast.LENGTH_SHORT).show();
        }
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            state = FP_State.DEL;
            selected_id = data.getStringExtra(DelProActivity.EXTRAS_FingerPrint_ID);
            selected_name = data.getStringExtra(DelProActivity.EXTRAS_FingerPrint_NAME);
            System.out.println(selected_id + "已被选择要删除！");
            new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("确定要删除吗?")
                    .setContentText(selected_name + "的指纹将从设备和数据库中删除！")
                    .setConfirmText("删除")
                    .setCancelText("返回")
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sendMsg2Lock(Lock.CmdId.pushToLock, null, (short) 0, (short) 8);
                            state = FP_State.IDLE;
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    })
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            //将选择删除的ID进行数据库查询并将地址发往设备
                            FingerPrintRepo fp_repo = new FingerPrintRepo(HomeActivity.this);
                            int id = Integer.valueOf(selected_id);
                            FingerPrint fingerPrint = fp_repo.getFingerPrintById(id);
                            System.out.println("要删除的指纹地址是：" + fingerPrint.address);
                            sendMsg2Lock(Lock.CmdId.sendDataToLock, fingerPrint.address, seq++, (short) 0);
                            sDialog.dismissWithAnimation();
                            showProcessDialog("已将删除命令发往设备，等待设备返回！");
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (state != FP_State.IDLE) {
                                        state = FP_State.IDLE;
                                        pDialog.dismissWithAnimation();
                                        Toast.makeText(HomeActivity.this, "删除失败，删除命令处理超时退出！", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, WAITING_DEL_PERIOD);
                        }
                    }).show();
        }
    }
    /**==============================================================================**
     * 函数名：scanDevice
     * 函数功能：搜索周围蓝牙设备
     * 全局变量：
     * 输入参数：enable -- boolean
     * 返回值：void
     **==============================================================================**/
    private void scanLeDevice(final boolean enable) {
        //增加蓝牙开启判断
        if (enable) {
            // Stops scanning after a pre-defined scan period.延时SCAN_PERIOD调用停止扫描
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    pDialog.dismissWithAnimation();
                    mScanning = false;
                    try {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    } catch (NullPointerException exception) {
                        //    LogWriter.showLog("Can't stop scan. Unexpected NullPointerException");
                    }
                    if (mLeDeviceListAdapter.getCount() == 0)
                        Toast.makeText(HomeActivity.this, "抱歉，附近没有找到设备！", Toast.LENGTH_SHORT).show();
                }
            }, SCAN_PERIOD);
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            pDialog = showProcessDialog("扫描设备中，请稍后...");
        } else {
            mScanning = false;
            try {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } catch (NullPointerException exception) {
                //   LogWriter.showLog("Can't stop scan. Unexpected NullPointerException");
            }
        }
        //
    }
    /**==============================================================================**
     * 函数名：updateConnectionState
     * 函数功能：搜索周围蓝牙设备
     * 全局变量：
     * 输入参数：null
     * 返回值：void
     **==============================================================================**/
    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //mConnectionState.setText(resourceId);
                pDialog.dismissWithAnimation();
                if (mConnected) {
                    findViewById(R.id.layout_del).setVisibility(View.VISIBLE);
                    findViewById(R.id.layout_add).setVisibility(View.VISIBLE);
                    vh.sw_conn.setText("已连接");
                    vh.sw_conn.setClickable(true);
                    vh.sw_conn.setChecked(true);
                } else {
                    Toast.makeText(HomeActivity.this, "设备连接失败，请检查智能锁设备", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    /**==============================================================================**
     * 函数名：handleMsg
     * 函数功能：处理收到蓝牙设备发过来的数据
     * 全局变量：
     * 输入参数：null
     * 返回值：void
     **==============================================================================**/
    private void handleMsg(byte[] data) {
        Lock msg = Lock.parse(data);
        // 获得包头和包体
        String packetBody = msg.body;
        Lock.Head packetHead = msg.head;
        switch (state) {
            case FP_State.IDLE:
                if (packetHead.cmdId == 0x1003 && mWaitingAdd) {
                    mWaitingAdd = false;
                    if (packetHead.errorCode == 0) {
                        state = FP_State.ADD;
                        pDialog.dismissWithAnimation();
                        Intent intent = new Intent(HomeActivity.this, AddProActivity.class);
                        intent.putExtra(AddProActivity.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
                        startActivityForResult(intent, 0);
                        System.out.println("智能锁已经进入录指纹模式，请在指纹模块上按下指纹！");
                    }
                    if (packetHead.errorCode == 2) {
                        pDialog.dismissWithAnimation();
                        state = FP_State.IDLE;
                        System.out.println("智能锁开始录指纹模式失败，请检查指纹模块是否就绪！");
                        Toast.makeText(HomeActivity.this, "设备开启录指纹模式失败！", Toast.LENGTH_SHORT).show();
                    }
                } else if (packetHead.cmdId == 0x1004 && mWaitingDel) {
                    mWaitingDel = false;
                    if (packetHead.errorCode == 0) {
                        state = FP_State.DEL;
                        System.out.println("智能锁已经进入删指纹模式");
                        pDialog.dismissWithAnimation();
                        Intent intent1 = new Intent(HomeActivity.this, DelProActivity.class);
                        startActivityForResult(intent1, 1);
                    } else {
                        pDialog.dismissWithAnimation();
                        state = FP_State.IDLE;
                        Toast.makeText(this, "设备开启删指纹模式失败！", Toast.LENGTH_SHORT).show();
                        System.out.println("智能锁开始录指纹模式失败，请检查设备是否就绪！" + packetHead.errorCode);
                    }
                }
                break;
            case FP_State.DEL:
                if (packetHead.cmdId == 0x1002) {
                    if (packetHead.errorCode == 0) {
                        state = FP_State.IDLE;
                        pDialog.dismissWithAnimation();
                        System.out.println("收到设备返回的删除指纹成功");
                        FingerPrintRepo fp_repo = new FingerPrintRepo(this);
                        fp_repo.delete(Integer.valueOf(selected_id));
                        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("已删除!")
                                .setContentText(selected_name + "的指纹从设备和数据库中删除!")
                                .setConfirmText("好的")
                                .setConfirmClickListener(null).show();
                    }
                    if (packetHead.errorCode == 5) {
                        state = FP_State.IDLE;
                        Toast.makeText(this, "设备删除指纹失败！", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }
    /**==============================================================================**
     * 函数名：sendMsg2Lock
     * 函数功能：发送数据给蓝牙锁
     * 全局变量：
     * 输入参数：null
     * 返回值：void
     **==============================================================================**/
    private void sendMsg2Lock(Lock.CmdId cmdId, String respText, short seq, short errorCode) {
        if (mBluetoothLeGattaService == null || mNotifyCharacteristic == null) {
            Log.d(TAG, "BluetoothLeGattaService or NotifyCharacteristic can not get");
            mBluetoothLeService.connect(mDeviceAddress);
        }
        Lock lockResp = Lock.build(cmdId, respText, seq, errorCode);
        // 序列化
        byte[] respRaw = lockResp.toBytes();
        System.out.println("对象序列化之后的长度为：" + respRaw.length);
        // Base64编码
        // final String respContent = Base64.encodeBase64String(respRaw);
        //final String respContent = Base64.encodeToString(respRaw, Base64.DEFAULT);
        //  System.out.println("要挟输入的数据是：" + respContent);
        // 推送消息给设备
        mNotifyCharacteristic.setValue(respRaw);
        mNotifyCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mBluetoothLeService.writeCharacteristic(mNotifyCharacteristic);
    }
    /**==============================================================================**
     * 函数名：showProcessDialog
     * 函数功能：显示进程对话框
     * 全局变量：
     * 输入参数：null
     * 返回值：void
     **==============================================================================**/
    public SweetAlertDialog showProcessDialog(String content) {
        if(pDialog != null)
            pDialog = null;
        pDialog = new SweetAlertDialog(HomeActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText(content);
        pDialog.setCancelable(false);
        pDialog.show();

        return pDialog;
    }
    /**
     * ==============================================================================**
     * 类名：ViewHolder
     * 类功能：ViewHolder
     * 范围：static inner类
     * *==============================================================================
     **/
    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        Switch sw_conn;
    }
    /**
     * ==============================================================================**
     * 类名：LeDeviceListAdapter
     * 类功能：Adapter for holding devices found through scanning.
     * 范围：inner类
     * *==============================================================================
     **/
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<>();
            mInflator = HomeActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.layout_list_item_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.txt_item_mac);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.txt_item_name);
                viewHolder.sw_conn = (Switch) view.findViewById(R.id.sw_conn);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());
            viewHolder.sw_conn.setText("未连接");
            viewHolder.sw_conn.setClickable(false);

            viewHolder.sw_conn.setOnCheckedChangeListener(onCheckedChangeListener);
            view.setClickable(true);
            view.setId(i);
            view.setOnClickListener(onClickListener);
            return view;
        }
    }
}


