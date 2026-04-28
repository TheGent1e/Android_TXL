package com.example.txl;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class AddEditContactActivity extends BaseActivity {
  public static final String EXTRA_CONTACT_ID = "contact_id";

  private EditText etName;
  private EditText etNote;
  private LinearLayout llPhones;
  private Button btnAddPhone;
  private Button btnSave;
  private Button btnCancel;

  private DatabaseHelper dbHelper;
  private long contactId = -1;
  private List<View> phoneViews = new ArrayList<>();

  private static final Pattern PHONE_PATTERN = Pattern.compile("^[1][3-9]\\d{9}$|^\\d{3,4}-\\d{7,8}$");

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add_edit_contact);

    dbHelper = new DatabaseHelper(this);

    initViews();
    contactId = getIntent().getLongExtra(EXTRA_CONTACT_ID, -1);

    if (contactId != -1) {
      setTitle(R.string.edit_contact);
      loadContactData();
    } else {
      setTitle(R.string.add_new_contact);
      addPhoneInput(null);
    }
  }

  private void initViews() {
    etName = findViewById(R.id.et_name);
    etNote = findViewById(R.id.et_note);
    llPhones = findViewById(R.id.ll_phones);
    btnAddPhone = findViewById(R.id.btn_add_phone);
    btnSave = findViewById(R.id.btn_save);
    btnCancel = findViewById(R.id.btn_cancel);

    btnAddPhone.setOnClickListener(v -> addPhoneInput(null));
    btnSave.setOnClickListener(v -> saveContact());
    btnCancel.setOnClickListener(v -> finish());
  }

  private void addPhoneInput(String phone) {
    View view = LayoutInflater.from(this).inflate(R.layout.item_phone_input, llPhones, false);
    EditText etPhone = view.findViewById(R.id.et_phone_item);
    ImageButton btnRemove = view.findViewById(R.id.btn_remove_phone);

    if (phone != null) {
      etPhone.setText(phone);
    }

    btnRemove.setOnClickListener(v -> {
      if (phoneViews.size() > 1) {
        llPhones.removeView(view);
        phoneViews.remove(view);
      } else {
        Toast.makeText(this, R.string.phone_required, Toast.LENGTH_SHORT).show();
      }
    });

    llPhones.addView(view);
    phoneViews.add(view);
  }

  private void loadContactData() {
    Contact contact = dbHelper.getContactById(contactId);
    if (contact != null) {
      etName.setText(contact.getName());
      etNote.setText(contact.getNote());

      List<String> phones = contact.getPhones();
      if (phones != null && !phones.isEmpty()) {
        for (String phone : phones) {
          addPhoneInput(phone);
        }
      } else {
        addPhoneInput(null);
      }
    }
  }

  private void saveContact() {
    String name = etName.getText().toString().trim();
    String note = etNote.getText().toString().trim();
    List<String> phones = new ArrayList<>();

    for (View view : phoneViews) {
      EditText etPhone = view.findViewById(R.id.et_phone_item);
      String phone = etPhone.getText().toString().trim();
      if (!TextUtils.isEmpty(phone)) {
        phones.add(phone);
      }
    }

    if (TextUtils.isEmpty(name)) {
      Toast.makeText(this, R.string.name_required, Toast.LENGTH_SHORT).show();
      return;
    }

    if (phones.isEmpty()) {
      Toast.makeText(this, R.string.phone_required, Toast.LENGTH_SHORT).show();
      return;
    }

    for (String phone : phones) {
      if (!isValidPhone(phone)) {
        Toast.makeText(this, R.string.invalid_phone, Toast.LENGTH_SHORT).show();
        return;
      }
    }

    Contact contact = new Contact(name, phones, note);

    if (contactId != -1) {
      contact.setId(contactId);
      dbHelper.updateContact(contact);
    } else {
      dbHelper.insertContact(contact);
    }

    Toast.makeText(this, R.string.contact_saved, Toast.LENGTH_SHORT).show();
    finish();
  }

  private boolean isValidPhone(String phone) {
    return phone != null && phone.length() >= 7 && phone.length() <= 15;
  }

}
