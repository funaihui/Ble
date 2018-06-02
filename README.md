> 与普通蓝牙相比，低功耗蓝牙显著降低了能量消耗，允许Android应用程序与具有更严格电源要求的BLE设备进行通信，如接近传感器、心率传感器等低功耗设备。

### 声明蓝牙权限

&emsp;&emsp;和使用普通蓝牙一样，在使用低功耗蓝牙时也需要在`AndroidManifest`声明蓝牙权限，声明蓝牙权限的代码如下

```xml
<uses-permission android:name="android.permission.BLUETOOTH"/>
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
```

如果想让你的App只适用于BLE设备，可以在`AndroidManifest`中继续添加以下代码

```xml
<uses-feature android:name="android.hardware.bluetooth_le" android:required="true"/>
```

如果想让你的App适用于不支持BLE的设备，只需要将`required="true"`改为`required="false"`然后在代码中通过以下方法来判断设备是否支持BLE，

```java
 private void checkIsSupportBLE() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
    }
```

<font color='red' size = '5'>注：</font>

> LE信标往往与位置有关,如果想要通过``BluetoothLeScanner` 方法来获取正确的扫描结果，需要在`AndroidManifest`中声明位置权限，声明位置权限可以使用`ACCESS_COARSE_LOCATION`或则 `ACCESS_FINE_LOCATION` ，如过不声明位置权限，将不会返回蓝牙的扫描结果。

### 开启BLE

&emsp;&emsp;开启BLE只需要以下两步

1. 拿到[BluetoothAdapter](https://developer.android.google.cn/reference/android/bluetooth/BluetoothAdapter.html)。

   在Android系统中只有一个`BluetoothAdapter`，可以通过以下方法来获取

   ```java
   private void obtainBluetoothAdapter() {
           final BluetoothManager bluetoothManager =
                   (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
           mBluetoothAdapter = bluetoothManager.getAdapter();
       }
   ```

2. 开启蓝牙。

   通过[isEnabled()](https://developer.android.google.cn/reference/android/bluetooth/BluetoothAdapter.html#isEnabled())可以判断是否已经开启BLE，如果没有开启则可以通过以下方法来开启

   ```java
    private void checkIsEnable() {
           if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
               Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
               startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
           }
       }
   ```

   <font color='red' size = '5'>注：</font>`REQUEST_ENABLE_BT`必须大于0。然后我们便可以在`onActivityResult(int, int, android.content.Intent)`方法中获取BLE开启的结果。

通过Intent方法开启蓝牙，系统会弹出是否确认开启蓝牙的对话框，对话框如下

![](https://user-gold-cdn.xitu.io/2018/3/31/1627a292382a3c62?w=540&h=262&f=png&s=27624)

这时，可以发现上面的提示语为“某个应用想要开启蓝牙”，如果我们想要把“某个”换成具体的应用名称，则需要换一种打开蓝牙的方式，代码如下

```java
if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();

        }
```

这时弹出是否打开蓝牙的弹出框如下图

![](https://user-gold-cdn.xitu.io/2018/5/27/163a034b682dcdd9?w=1079&h=589&f=png&s=32956)

红框标识处，就是当前应用的名称。

<font color='red' size = '5'>注：</font>这种打开蓝牙的方式，在某些机型上不会有弹出框，会直接打开蓝牙。

如果通过这种方式打开蓝牙，并且想知道蓝牙是否成功打开，则需要一个广播接收者来监听蓝牙状态的变化。代码如下

```java
//注册广播接受者
private void initReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
    }
//新建监听蓝牙状态变化的广播
 private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        mBluetoothState.setText("Bluetooth off");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        mBluetoothState.setText("Turning Bluetooth off...");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        mBluetoothState.setText("Bluetooth on");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        mBluetoothState.setText("Turning Bluetooth on...");
                        break;
                }
            }
        }
    };
```

> 此广播包含额外字段 `EXTRA_STATE` 和 `EXTRA_PREVIOUS_STATE`，二者分别包含新的和旧的蓝牙状态。 这些额外字段可能的值包括 `STATE_TURNING_ON`、`STATE_ON`、`STATE_TURNING_OFF` 和 `STATE_OFF`。侦听此广播适用于检测在您的应用运行期间对蓝牙状态所做的更改。

### 扫描BLE设备

&emsp;&emsp;扫描蓝牙设备可以通过 `startLeScan(BluetoothAdapter.LeScanCallback)`和`startLeScan(UUID[], BluetoothAdapter.LeScanCallback)`方法，这两种扫描BLE设备的区别如下。

- `startLeScan(BluetoothAdapter.LeScanCallback)`方法扫描的是周围所有的BLE设备。
- `startLeScan(UUID[], BluetoothAdapter.LeScanCallback)`只扫描和UUID相匹配的设备。

可以发现无论通过哪种方式扫描蓝牙都必须要实现`LeScanCallback`回调方法，此方法是用来获取蓝牙扫描结果。

&emsp;&emsp;通过以上两种方式实现扫描BLE设备的代码如下：

1. 通过`startLeScan(BluetoothAdapter.LeScanCallback)`方法扫描蓝牙的代码如下

   ```java
    //扫描BLE设备
   private void scanLeDevice(final boolean enable) {
           if (enable) {
               // Stops scanning after a pre-defined scan period.
               mHandler.postDelayed(new Runnable() {
                   @Override
                   public void run() {
                       mScanning = false;
                       mBluetoothAdapter.stopLeScan(mLeScanCallback);
                   }
               }, SCAN_PERIOD);
   
               mScanning = true;
               mBluetoothAdapter.startLeScan(mLeScanCallback);
           } else {
               mScanning = false;
               mBluetoothAdapter.stopLeScan(mLeScanCallback);
           }
       }
   
   //扫描结果回调
       private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                   @Override
                   public void onLeScan(final BluetoothDevice device, int rssi,
                                        byte[] scanRecord) {
                       runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               mLeDeviceListAdapter.addDevice(device);
                               mLeDeviceListAdapter.notifyDataSetChanged();
                           }
                       });
                   }
               };
   
   ```

2. 通过`startLeScan(UUID[], BluetoothAdapter.LeScanCallback)`方法扫描蓝牙的代码如下

   ```java
   private void scanLeDeviceByUUID() {
           mBluetoothAdapter.startLeScan(new UUID[]{RX_SERVICE_UUID}, mLeScanCallback);
       }
   
   ```

   <font color='red' size = '5'>再次声明：</font>

   > LE信标往往与位置有关,如果想要通过``BluetoothLeScanner` 方法来获取正确的扫描结果，需要在`AndroidManifest`中声明位置权限，声明位置权限可以使用`ACCESS_COARSE_LOCATION`或则 `ACCESS_FINE_LOCATION` ，如过不声明位置权限，将不会返回蓝牙的扫描结果。

### 连接BLE设备

   &emsp;&emsp;&emsp;要进行BLE设备之间的通讯，首先应该进行设备之间的连接，可以通过`device.connectGatt (Context context,boolean autoConnect,                 BluetoothGattCallback callback)`方法来连接设备。

   - `autoConnect`是设置当BLE设备可用时是否自动进行连接。
   - `device`就是通过扫描BLE设备获得的。
   - `callback`则是连接指定设备后的回掉，可以在回掉中知道是否建立连接、连接断开、以及获取设备之间传输的数据。

   以下是`BluetoothGattCallback`类中具体的方法![](https://user-gold-cdn.xitu.io/2018/5/30/163b0e9d09b9cb2d?w=663&h=308&f=png&s=87783)

下面我会介绍几个比较常用的方法：

- `onConnectionStateChange`此方法的作用是可以获得设备连接的状态，如“成功连接”、“断开连接”。
- `onServicesDiscovered`当远程设备的服务，特性和描述符列表已更新时（即发现新服务），调用此方法。
- `onCharacteristicChanged`远程特征变化会调用此方法，即BLE设备的状态发生了变化会调用此方法。

> `onCharacteristicChanged`方法就是相当于BLE设备对你操作的回应，如打开BLE设备成功等，然后在此方法回掉之后，便可以继续下一步操作了。

### 操作BLE设备

&emsp;&emsp;在设备连接之后，就可以向BLE设备发送命令来操作BLE设备了，向BLE写数据的代码如下

```java
public boolean writeByteToBleDevice(byte[] data) {
        BluetoothGattService mBluetoothGattService = mBluetoothGatt.getService(RX_SERVICE_UUID);
        if (mBluetoothGattService == null) {
            return false;
        }

        BluetoothGattCharacteristic characteristic = mBluetoothGattService.getCharacteristic(RX_SERVICE_UUID);
        if (characteristic == null) {

            return false;
        }

        boolean b = characteristic.setValue(data);

        return b && mBluetoothGatt.writeCharacteristic(characteristic);
    }
```

在向BLE设备发送数据之后，就可以在`onCharacteristicChanged`方法中获得BLE设备的状态了。

### 结束语

&emsp;&emsp;这篇文章拖了这么久，总算写完了，本来打算在上一篇蓝牙文章写完后就写这篇，结果中间公司忙着上线新的项目，加班加点将近一个月，就没时间写这篇文章了，在项目上线之后就立马开始写这篇文章，希望这篇文章能帮到想要学习蓝牙的朋友。
