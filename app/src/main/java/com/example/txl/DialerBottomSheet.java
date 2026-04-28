package com.example.txl;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.Locale;

public class DialerBottomSheet extends BottomSheetDialogFragment {

    private static final int CALL_PHONE_REQ = 100;

    private TextView tvNumber;
    private ImageButton btnBackspace;
    private GridView gridDialpad;
    private LinearLayout btnCallTelecom;
    private LinearLayout btnCallUnicom;

    private final String[][] keys = {
            {"1", ""}, {"2", "ABC"}, {"3", "DEF"},
            {"4", "GHI"}, {"5", "JKL"}, {"6", "MNO"},
            {"7", "PQRS"}, {"8", "TUV"}, {"9", "WXYZ"},
            {"*", ""}, {"0", "+"}, {"#", ""}
    };

    public static DialerBottomSheet newInstance(String phoneNumber) {
        DialerBottomSheet f = new DialerBottomSheet();
        Bundle args = new Bundle();
        args.putString("phone_number", phoneNumber);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_dialer, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog != null) {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.getLayoutParams().height = (int) (getResources().getDisplayMetrics().heightPixels * 0.55);
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setPeekHeight(getResources().getDisplayMetrics().heightPixels / 2, false);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvNumber = view.findViewById(R.id.tv_dialer_number);
        btnBackspace = view.findViewById(R.id.btn_dialer_backspace);
        gridDialpad = view.findViewById(R.id.grid_dialpad);
        btnCallTelecom = view.findViewById(R.id.btn_call_telecom);
        btnCallUnicom = view.findViewById(R.id.btn_call_unicom);

        if (getArguments() != null) {
            String phone = getArguments().getString("phone_number");
            if (phone != null) tvNumber.setText(phone);
        }

        btnBackspace.setOnClickListener(v -> {
            String cur = tvNumber.getText().toString();
            if (!TextUtils.isEmpty(cur)) tvNumber.setText(cur.substring(0, cur.length() - 1));
        });
        btnBackspace.setOnLongClickListener(v -> { tvNumber.setText(""); return true; });

        gridDialpad.setAdapter(new BaseAdapter() {
            @Override public int getCount() { return keys.length; }
            @Override public Object getItem(int p) { return keys[p]; }
            @Override public long getItemId(int p) { return p; }
            @Override public View getView(int p, View cv, ViewGroup pg) {
                if (cv == null) cv = LayoutInflater.from(requireContext()).inflate(R.layout.item_dialpad_key, pg, false);
                TextView tvN = cv.findViewById(R.id.tv_key_num);
                TextView tvL = cv.findViewById(R.id.tv_key_letters);
                tvN.setText(keys[p][0]);
                tvL.setText(keys[p][1]);
                cv.setOnClickListener(v -> tvNumber.append(keys[p][0]));
                return cv;
            }
        });

        View.OnClickListener callListener = v -> {
            String num = tvNumber.getText().toString().trim();
            if (!TextUtils.isEmpty(num)) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CALL_PHONE}, CALL_PHONE_REQ);
                } else {
                    doCall(num);
                }
            } else {
                Toast.makeText(requireContext(), "请输入电话号码", Toast.LENGTH_SHORT).show();
            }
        };
        btnCallTelecom.setOnClickListener(callListener);
        btnCallUnicom.setOnClickListener(callListener);
    }

    private void doCall(String number) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));
        startActivity(intent);
        dismiss();
    }
}
