package com.anda.smartlock.ui;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.anda.smartlock.R;
import com.anda.smartlock.consts.BLEUUID;
import com.anda.smartlock.consts.FP_State;
import com.anda.smartlock.data.FingerPrint;
import com.anda.smartlock.data.FingerPrintRepo;
import com.anda.smartlock.protocol.Lock;

import java.util.ArrayList;
import java.util.UUID;

import me.drozdzynski.library.steppers.OnCancelAction;
import me.drozdzynski.library.steppers.OnFinishAction;
import me.drozdzynski.library.steppers.SteppersItem;
import me.drozdzynski.library.steppers.SteppersView;
/**==============================================================================**
 * 类名：AddProActivity
 * 类功能：实现指纹录流程activity
 * 范围：public类
 **==============================================================================**/
public class AddProActivity extends AppCompatActivity {
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_FingerPrint_NAME = "NAME";
    private static final String TAG = AddProActivity.class.getSimpleName();
    private static short seq = 1;
    FingerPrint fingerPrint = new FingerPrint();
    FingerPrintRepo fp_repo = new FingerPrintRepo(this);
    private BluetoothLeService mBluetoothLeService;
    private BluetoothGattService mBluetoothLeGattaService;
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private String mDeviceAddress;
    //Parameter:======================= Code to manage Service lifecycle.=======================//
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder)service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
             mBluetoothLeService.connect(mDeviceAddress);
            //获取服务和特性
            mBluetoothLeGattaService = mBluetoothLeService.getSupportedGattServices(UUID.fromString(BLEUUID.SERVICE));
            mNotifyCharacteristic = mBluetoothLeGattaService.getCharacteristic(UUID.fromString(BLEUUID.CHARACTER));
            ///进入界面后直接发送消息开启录指纹模式
           // sendMsg2Lock(Lock.CmdId.sendAddNotificationToLock, null, (short)seq);
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    private int state = FP_State.FIRSTSTEP;
    private ArrayList<SteppersItem> steps;
    //Parameter:===================== Handles various events fired by the Service.=====================//
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
             //   updateConnectionState(R.string.connected);
                //   invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
              //  updateConnectionState(R.string.disconnected);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                mBluetoothLeGattaService = mBluetoothLeService.getSupportedGattServices(UUID.fromString(BLEUUID.SERVICE));
                mNotifyCharacteristic = mBluetoothLeGattaService.getCharacteristic(UUID.fromString(BLEUUID.CHARACTER));
                //set the readUnblockCharacteristic as true
                mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic,true);
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                if(data == null)
                    Log.d(TAG, "ACTION_DATA_AVAILABLE的广播数据返回的为空值，请检查回调函数是否有数据返回");
                Log.d(TAG, "增加指纹流程界面已经收到数据：" + new String(data));
                handleMsg(data);
            }
        }
    };

    /**==============================================================================**
     * 函数名：makeGattUpdateIntentFilter
     * 函数功能：确定过滤service回调的事件
     * 全局变量：
     * 输入参数：null
     * 返回值：IntentFilter
     **==============================================================================**/
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
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
        setContentView(R.layout.activity_main);

        final Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        //绑定service
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        SteppersView.Config steppersViewConfig = new SteppersView.Config();
        steppersViewConfig.setOnFinishAction(new OnFinishAction() {
            @Override
            public void onFinish() {
                EditText txt_name = (EditText) findViewById(R.id.txt_name);
                String name = txt_name.getText().toString();
                if(name.equals("")) {
                    Toast.makeText(getApplicationContext(), "请输入指纹主人的名字！", Toast.LENGTH_SHORT).show();
                } else {
                    fingerPrint.name = name;
                    fingerPrint.device_mac = mDeviceAddress;
                    int id = fp_repo.insert(fingerPrint);
                    if(id == -1)
                        Toast.makeText(getApplicationContext(), "数据存储失败，请重试！", Toast.LENGTH_SHORT).show();
                    else {
                        Log.d(TAG, "指纹地址数据存储成功！");
                        Intent tent = new Intent(AddProActivity.this, HomeActivity.class);
                        tent.putExtra(EXTRAS_FingerPrint_NAME, name);
                        setResult(RESULT_OK, tent);
                        AddProActivity.this.finish();
                    }
                }
            }
        });
        steppersViewConfig.setOnCancelAction(new OnCancelAction() {
            @Override
            public void onCancel() {
                if (state == FP_State.RECIEVED_DATA) {
                    Toast.makeText(getApplicationContext(), "已收到指纹数据，请输入指纹主人的名字！", Toast.LENGTH_SHORT).show();
                } else {
                    Intent tent = new Intent(AddProActivity.this, HomeActivity.class);
                    setResult(RESULT_CANCELED, tent);
                    AddProActivity.this.finish();
                }
            }
        });
        steppersViewConfig.setFragmentManager(getSupportFragmentManager());
        steps = new ArrayList<>();
        int i = 0;
        while (i < 2) {
            final SteppersItem item = new SteppersItem();
            item.setLabel("指纹录入步骤:(" + (i+1) +"/2)");
            item.setPositiveButtonEnable(false);
            if(i == 0) {
                FirstStepFragment firstStepFragment = new FirstStepFragment();
                item.setSubLabel("");
                item.setFragment(firstStepFragment);
            } else if(i == 1) {
                SecStepFragment secStepFragment = new SecStepFragment();
                item.setSubLabel("");
                item.setFragment(secStepFragment);
            }
            steps.add(item);
            i++;
        }
        SteppersView steppersView = (SteppersView) findViewById(R.id.steppersView);
        steppersView.setConfig(steppersViewConfig);
        steppersView.setItems(steps);
        steppersView.build();
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
    }

    /**==============================================================================**
     * 函数名：onStop
     * 函数功能：activity的onStop流程
     * 全局变量：
     * 输入参数：
     * 返回值：void
     **==============================================================================**/
    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    /**==============================================================================**
     * 函数名：onStart
     * 函数功能：activity的onStart流程
     * 全局变量：
     * 输入参数：
     * 返回值：void
     **==============================================================================**/
    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    /**==============================================================================**
     * 函数名：scanDevice
     * 函数功能：搜索周围蓝牙设备
     * 全局变量：
     * 输入参数：null
     * 返回值：void
     **==============================================================================**/
    private void updateTxtState(final int resourceId, final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView txt = (TextView) findViewById(resourceId);
                txt.setText(msg);
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
            case FP_State.FIRSTSTEP:
                if(packetHead.cmdId == 0x2001) {
                    if (packetHead.errorCode == 0) {
                        /// 这里将来考虑提示两次成功后启动timer定时器，如果在超时还没有收到指纹地址的话就给设备和微信提示
                        state = FP_State.SECSTEP;
                        System.out.println("智能锁第一次录取指纹成功！请再次按下指纹！");
                        updateTxtState(R.id.txt_fir, "第一次录指纹成功，请点击Continue继续第二次录指纹");
                        steps.get(0).setSubLabel("智能锁第一次录取指纹成功！");
                        steps.get(0).setPositiveButtonEnable(true);
                    } else {
                        state = FP_State.IDLE;
                        Toast.makeText(AddProActivity.this, "指纹录取失败，请点击取消退出", Toast.LENGTH_SHORT);
                        System.out.println("智能锁录取指纹失败！失败代码为：" + packetHead.errorCode);
                    }
                }
                break;
            case FP_State.SECSTEP:
                if(packetHead.cmdId == 0x2001) {
                    if (packetHead.errorCode == 0) {
                        state = FP_State.RECIEVED_DATA;
                        System.out.println("智能锁第二次录取指纹成功！录指纹成功！");
                        updateTxtState(R.id.txt_sec, "第二次录指纹成功，等待接受指纹数据...");
                        /// 这里将来考虑提示两次成功后启动timer定时器，如果在超时还没有收到指纹地址的话就给设备和微信提示
                    } else {
                        state = FP_State.IDLE;
                        Toast.makeText(AddProActivity.this, "指纹录取失败，请点击取消退出", Toast.LENGTH_SHORT);
                        System.out.println("智能锁录取指纹失败！失败代码为：" + packetHead.errorCode);
                    }
                }
                break;
            case FP_State.RECIEVED_DATA:
                if(packetHead.cmdId == 0x01) {
                    System.out.println("收到智能锁成功录取的指纹:" + packetBody+"长度为："+packetBody.length());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView txt_sec = (TextView) findViewById(R.id.txt_sec);
                            txt_sec.setText("收到指纹数据，请给指纹命名并点击Finish存储指纹数据");
                            EditText txt_name = (EditText) findViewById(R.id.txt_name);
                            txt_name.setVisibility(View.VISIBLE);
                        }
                    });
                    sendMsg2Lock(Lock.CmdId.sendDataToServACK , packetBody, packetHead.seq);
                    fingerPrint.address = packetBody;
                    steps.get(1).setPositiveButtonEnable(true);
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
    private void sendMsg2Lock(Lock.CmdId cmdId, String respText, short seq) {
        if (mBluetoothLeGattaService == null || mNotifyCharacteristic == null) {
            Log.d(TAG, "BluetoothLeGattaService or NotifyCharacteristic can not get");
            mBluetoothLeService.connect(mDeviceAddress);
        }
        Lock lockResp = Lock.build(cmdId , respText, seq);
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
      //  Toast.makeText(getApplicationContext(), "写入成功！", Toast.LENGTH_SHORT).show();
        seq++;
    }
}
