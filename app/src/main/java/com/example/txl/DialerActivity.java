package com.example.txl;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.CallLog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class DialerActivity extends BaseActivity {
    private static final int CALL_PHONE_PERMISSION_REQUEST = 101;

    private EditText etPhoneNumber;
    private ImageButton btnBackspace;
    private GridView dialpadGrid;
    private View btnDial;
    private TextView tvCallStatus;

    private DatabaseHelper dbHelper;
    private Handler timerHandler;
    private Runnable timerRunnable;
    private int callSeconds;
    private boolean isCalling;

    private final String[][] dialpadKeys = {
            { "1", "" }, { "2", "ABC" }, { "3", "DEF" },
            { "4", "GHI" }, { "5", "JKL" }, { "6", "MNO" },
            { "7", "PQRS" }, { "8", "TUV" }, { "9", "WXYZ" },
            { "*", "" }, { "0", "+" }, { "#", "" }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialer);

        dbHelper = new DatabaseHelper(this);
        timerHandler = new Handler(Looper.getMainLooper());

        initViews();
        setupDialpad();

        String phoneNumber = getIntent().getStringExtra("phone_number");
        if (phoneNumber != null) {
            etPhoneNumber.setText(phoneNumber);
        }
    }

    private void initViews() {
        etPhoneNumber = findViewById(R.id.et_phone_number);
        btnBackspace = findViewById(R.id.btn_backspace);
        dialpadGrid = findViewById(R.id.dialpad_grid);
        btnDial = findViewById(R.id.btn_dial);
        tvCallStatus = findViewById(R.id.tv_call_status);

        btnBackspace.setOnClickListener(v -> {
            String currentText = etPhoneNumber.getText().toString();
            if (!TextUtils.isEmpty(currentText)) {
                etPhoneNumber.setText(currentText.substring(0, currentText.length() - 1));
                etPhoneNumber.setSelection(etPhoneNumber.getText().length());
            }
        });

        btnBackspace.setOnLongClickListener(v -> {
            etPhoneNumber.setText("");
            return true;
        });

        btnDial.setOnClickListener(v -> {
            String number = etPhoneNumber.getText().toString().trim();
            if (!TextUtils.isEmpty(number)) {
                if (isCalling) {
                    endCall();
                } else {
                    makePhoneCall(number);
                }
            } else {
                Toast.makeText(this, "请输入电话号码", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupDialpad() {
        dialpadGrid.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return dialpadKeys.length;
            }

            @Override
            public Object getItem(int position) {
                return dialpadKeys[position];
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    view = LayoutInflater.from(DialerActivity.this)
                            .inflate(R.layout.item_dialpad_key, parent, false);
                }

                TextView tvNumber = view.findViewById(R.id.tv_key_num);
                TextView tvLetters = view.findViewById(R.id.tv_key_letters);

                tvNumber.setText(dialpadKeys[position][0]);
                tvLetters.setText(dialpadKeys[position][1]);

                view.setOnClickListener(v -> {
                    String key = dialpadKeys[position][0];
                    etPhoneNumber.append(key);
                });

                return view;
            }
        });
    }

    private void makePhoneCall(String phoneNumber) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.CALL_PHONE },
                    CALL_PHONE_PERMISSION_REQUEST);
            return;
        }

        isCalling = true;
        callSeconds = 0;
        startCallTimer();

        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);

        tvCallStatus.setText("正在呼叫 " + phoneNumber);
        tvCallStatus.setVisibility(View.VISIBLE);
        saveCallLog(phoneNumber, CallLog.Calls.OUTGOING_TYPE);
    }

    private void startCallTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                callSeconds++;
                int minutes = callSeconds / 60;
                int seconds = callSeconds % 60;
                String timeStr = String.format("%02d:%02d", minutes, seconds);
                tvCallStatus.setText("通话中 " + timeStr);
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void endCall() {
        isCalling = false;
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
        tvCallStatus.setVisibility(View.GONE);
        Toast.makeText(this, "通话结束", Toast.LENGTH_SHORT).show();
    }

    private void saveCallLog(String phoneNumber, int type) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("phone_number", phoneNumber);
        values.put("call_type", type);
        values.put("call_time", System.currentTimeMillis());
        values.put("duration", callSeconds);
        try {
            db.insert("call_logs", null, values);
        } catch (Exception e) {
            String createTable = "CREATE TABLE IF NOT EXISTS call_logs ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "phone_number TEXT,"
                    + "call_type INTEGER,"
                    + "call_time INTEGER,"
                    + "duration INTEGER)";
            db.execSQL(createTable);
            db.insert("call_logs", null, values);
        }
        db.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CALL_PHONE_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                String number = etPhoneNumber.getText().toString().trim();
                if (!TextUtils.isEmpty(number)) {
                    makePhoneCall(number);
                }
            } else {
                Toast.makeText(this, "需要拨号权限", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
