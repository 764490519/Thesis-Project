package com.example.datacollector.presentation.MoveSense;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;

import com.example.datacollector.R;
import com.example.datacollector.presentation.MoveSense.bluetooth.MdsRx;
import com.example.datacollector.presentation.MoveSense.model.ImuModel;
import com.example.datacollector.presentation.MoveSense.model.InfoResponse;
import com.example.datacollector.presentation.MoveSense.utils.FormatHelper;
import com.example.datacollector.presentation.util.CsvLogger;
import com.example.datacollector.presentation.util.TimeUtil;
import com.google.gson.Gson;
import com.movesense.mds.Mds;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsNotificationListener;
import com.movesense.mds.MdsResponseListener;
import com.movesense.mds.MdsSubscription;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.movesense.mds.internal.connectivity.MovesenseDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class ImuActivity extends ComponentActivity {

    private final String TAG = "ImuActivityUserDebug";
    public static final String URI_EVENTLISTENER = "suunto://MDS/EventListener";

    private final String IMU6_PATH = "Meas/IMU6/";
    private final String IMU9_PATH = "Meas/IMU9/";
    private String SELECTED_PATH = IMU9_PATH;

    private final String IMU_INFO_PATH = "/Meas/IMU/Info";
    private MdsSubscription mMdsSubscription;
    private final List<String> spinnerRates = new ArrayList<>();
    private String rate = "13"; // 13, 26, 52, 104, 208, 416, 833, 1666
    long prevUpdateTimestamp = 0;
    private TextView mImuTimeStampTextView;
    private TextView mLinearaccXAxisTextView;
    private TextView mLinearaccYAxisTextView;
    private TextView mLinearaccZAxisTextView;
    private TextView mGyroXAxisTextView;
    private TextView mGyroYAxisTextView;
    private TextView mGyroZAxisTextView;
    private TextView mMagnXAxisTextView;
    private TextView mMagnYAxisTextView;
    private TextView mMagnZAxisTextView;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imu);
        mImuTimeStampTextView = findViewById(R.id.imu_timestamp_textview);
        mLinearaccXAxisTextView = findViewById(R.id.linearacc_x_axis_textView);
        mLinearaccYAxisTextView = findViewById(R.id.linearacc_y_axis_textView);
        mLinearaccZAxisTextView = findViewById(R.id.linearacc_z_axis_textView);
        mGyroXAxisTextView = findViewById(R.id.gyro_x_axis_textView);
        mGyroYAxisTextView = findViewById(R.id.gyro_y_axis_textView);
        mGyroZAxisTextView = findViewById(R.id.gyro_z_axis_textView);
        mMagnXAxisTextView = findViewById(R.id.magn_x_axis_textView);
        mMagnYAxisTextView = findViewById(R.id.magn_y_axis_textView);
        mMagnZAxisTextView = findViewById(R.id.magn_z_axis_textView);

    }

    @Override
    protected void onStart() {
        super.onStart();

        MovesenseDevice device = MovesenseConnectedDevices.getConnectedDevice(0);
        Log.d(TAG,"Name: " + device.getName() + " MacAddress: " + device.getMacAddress());

        Mds.builder().build(this).get(MdsRx.SCHEME_PREFIX + device.getSerial() + IMU_INFO_PATH,
                null, new MdsResponseListener() {
                    @Override
                    public void onSuccess(String data) {
                        Log.d(TAG, "onSuccess(): " + data);
                        InfoResponse infoResponse = new Gson().fromJson(data, InfoResponse.class);
                        // Get sample rates
                        for (Integer inforate : infoResponse.content.sampleRates) {
                            spinnerRates.add(String.valueOf(inforate));
                            // Set first rate as default
                            if (rate == null) {
                                rate = String.valueOf(inforate);
                            }
                        }

                        subscribeImuData(device);
                    }

                    @Override
                    public void onError(MdsException error) {
                        Log.e(TAG, "onError(): ", error);
                    }
                });

    }

    private void subscribeImuData(MovesenseDevice device){

        CsvLogger csvLogger = new CsvLogger(this, "imu_data.csv");
        csvLogger.logData("Timestamp,Date,LinearAccX,LinearAccY,LinearAccZ,GyroX,GyroY,GyroZ,MagnX,MagnY,MagnZ");


        mMdsSubscription = Mds.builder().build(this).subscribe(URI_EVENTLISTENER,
                FormatHelper.formatContractToJson(device.getSerial(), SELECTED_PATH + rate), new MdsNotificationListener() {
                    @Override
                    public void onNotification(String data) {
                        Log.d(TAG, "IMU data: " + data);
                        final int initialMsPerSample = 1000/Integer.valueOf(rate);
                        ImuModel imuModel = new Gson().fromJson(data, ImuModel.class);
                        long diffTs = imuModel.getBody().getTimestamp()-prevUpdateTimestamp;
                        // Use guess when starting or if timestamp loops
                        if (prevUpdateTimestamp == 0 || diffTs < 0)
                        {
                            diffTs = initialMsPerSample * imuModel.getBody().getArrayAcc().length;
                        }

                        mImuTimeStampTextView.setText("Time Stamp: " + imuModel.getBody().getTimestamp());

                        mLinearaccXAxisTextView.setText(String.format(Locale.getDefault(), "x: %.6f", imuModel.getBody().getArrayAcc()[0].getX()));
                        mLinearaccYAxisTextView.setText(String.format(Locale.getDefault(), "y: %.6f", imuModel.getBody().getArrayAcc()[0].getY()));
                        mLinearaccZAxisTextView.setText(String.format(Locale.getDefault(), "z: %.6f", imuModel.getBody().getArrayAcc()[0].getZ()));

                        mGyroXAxisTextView.setText(String.format(Locale.getDefault(), "x: %.6f", imuModel.getBody().getArrayGyro()[0].getX()));
                        mGyroYAxisTextView.setText(String.format(Locale.getDefault(), "y: %.6f", imuModel.getBody().getArrayGyro()[0].getY()));
                        mGyroZAxisTextView.setText(String.format(Locale.getDefault(), "z: %.6f", imuModel.getBody().getArrayGyro()[0].getZ()));

                        if (imuModel.getBody().getArrayMagnl() != null) {
                            mMagnXAxisTextView.setText(String.format(Locale.getDefault(), "x: %.6f", imuModel.getBody().getArrayMagnl()[0].getX()));
                            mMagnYAxisTextView.setText(String.format(Locale.getDefault(), "y: %.6f", imuModel.getBody().getArrayMagnl()[0].getY()));
                            mMagnZAxisTextView.setText(String.format(Locale.getDefault(), "z: %.6f", imuModel.getBody().getArrayMagnl()[0].getZ()));
                        }
//                        StringBuilder sb = new StringBuilder();
//                        sb.append(imuModel.getBody().getTimestamp() + ",");
//                        sb.append(TimeUtil.formatTime(imuModel.getBody().getTimestamp()) + ",");
//                        sb.append(imuModel.getBody().getArrayAcc()[0].getX() + ",");
//                        sb.append(imuModel.getBody().getArrayAcc()[0].getY() + ",");
//                        sb.append(imuModel.getBody().getArrayAcc()[0].getZ() + ",");
//                        sb.append(imuModel.getBody().getArrayGyro()[0].getX() + ",");
//                        sb.append(imuModel.getBody().getArrayGyro()[0].getY() + ",");
//                        sb.append(imuModel.getBody().getArrayGyro()[0].getZ() + ",");
//                        sb.append(imuModel.getBody().getArrayMagnl()[0].getX() + ",");
//                        sb.append(imuModel.getBody().getArrayMagnl()[0].getY() + ",");
//                        sb.append(imuModel.getBody().getArrayMagnl()[0].getZ() + ",");
//                        csvLogger.logData(sb.toString());

                    }

                    @Override
                    public void onError(MdsException error) {
                        Log.e(TAG, "onError(): ", error);
                    }
                });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
