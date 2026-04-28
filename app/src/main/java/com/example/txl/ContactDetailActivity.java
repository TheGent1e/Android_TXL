package com.example.txl;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
public class ContactDetailActivity extends AppCompatActivity {
    public static final String EXTRA_CONTACT_ID = "contact_id";
    private static final int CALL_PHONE_REQ = 100;
    private static final int PICK_IMG_REQ = 200;
    private static final int CAM_REQ = 300;
    private ImageView ivAvatar;
    private TextView tvName, tvChangeAvatar, tvNote;
    private LinearLayout llPhones, llNote;
    private Button btnEdit, btnDelete, btnDial;
    private DatabaseHelper dbHelper;
    private long contactId;
    private Contact currentContact;
    private Uri camUri;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);
        dbHelper = new DatabaseHelper(this);
        findViews();
        contactId = getIntent().getLongExtra(EXTRA_CONTACT_ID, -1);
        if (contactId != -1) loadContact(); else finish();
    }
    @Override protected void onResume() { super.onResume(); if (contactId != -1) loadContact(); }
    private void findViews() {
        ivAvatar = findViewById(R.id.iv_avatar_detail);
        tvName = findViewById(R.id.tv_name_detail);
        tvChangeAvatar = findViewById(R.id.tv_change_avatar);
        llPhones = findViewById(R.id.ll_phones_detail);
        llNote = findViewById(R.id.ll_note_detail);
        tvNote = findViewById(R.id.tv_note_detail);
        btnEdit = findViewById(R.id.btn_edit_detail);
        btnDelete = findViewById(R.id.btn_delete_detail);
        btnDial = findViewById(R.id.btn_dial_detail);
        ivAvatar.setOnClickListener(v -> showAvatarDialog());
        tvChangeAvatar.setOnClickListener(v -> showAvatarDialog());
        btnEdit.setOnClickListener(v -> { Intent i = new Intent(ContactDetailActivity.this, AddEditContactActivity.class); i.putExtra(AddEditContactActivity.EXTRA_CONTACT_ID, contactId); startActivity(i); });
        btnDelete.setOnClickListener(v -> showDeleteDialog());
        btnDial.setOnClickListener(v -> {
            if (currentContact != null && !currentContact.getPhones().isEmpty()) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, CALL_PHONE_REQ);
                } else {
                    Intent i = new Intent(Intent.ACTION_CALL);
                    i.setData(Uri.parse("tel:" + currentContact.getMainPhone()));
                    startActivity(i);
                }
            } else {
                Toast.makeText(this, "此联系人没有电话号码", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void loadContact() {
        currentContact = dbHelper.getContactById(contactId);
        if (currentContact == null) { finish(); return; }
        tvName.setText(currentContact.getName());
        if (currentContact.getAvatarPath() != null && !currentContact.getAvatarPath().isEmpty()) { try { ivAvatar.setImageBitmap(BitmapFactory.decodeFile(currentContact.getAvatarPath())); } catch (Exception e) { ivAvatar.setImageResource(R.drawable.circle_avatar); } }
        else ivAvatar.setImageResource(R.drawable.circle_avatar);
        llPhones.removeAllViews();
        for (String phone : currentContact.getPhones()) {
            View pv = LayoutInflater.from(this).inflate(R.layout.item_phone_detail, llPhones, false);
            ((TextView)pv.findViewById(R.id.tv_phone_detail)).setText(phone);
            pv.setOnClickListener(v -> { Intent i = new Intent(ContactDetailActivity.this, DialerActivity.class); i.putExtra("phone_number", phone); startActivity(i); });
            llPhones.addView(pv);
        }
        if (!TextUtils.isEmpty(currentContact.getNote())) { llNote.setVisibility(View.VISIBLE); tvNote.setText(currentContact.getNote()); } else llNote.setVisibility(View.GONE);
    }
    private void showAvatarDialog() { new AlertDialog.Builder(this).setTitle("选择头像").setItems(new String[]{"从相册选择", "拍照"}, (d, w) -> { if (w == 0) pickGallery(); else pickCamera(); }).setNegativeButton("取消", null).show(); }
    private void pickGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMG_REQ);
        else { Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); i.setType("image/*"); startActivityForResult(Intent.createChooser(i, "选择图片"), PICK_IMG_REQ); }
    }
    private void pickCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAM_REQ);
        else { Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); File f = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "temp_avatar.jpg"); camUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", f); i.putExtra(MediaStore.EXTRA_OUTPUT, camUri); startActivityForResult(i, CAM_REQ); }
    }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri uri = null;
            if (requestCode == PICK_IMG_REQ && data != null && data.getData() != null) uri = data.getData();
            else if (requestCode == CAM_REQ) uri = camUri;
            if (uri != null) { try { Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), uri); String p = saveAvatar(cropSquare(bmp)); if (p != null) { currentContact.setAvatarPath(p); dbHelper.updateContact(currentContact); ivAvatar.setImageBitmap(bmp); Toast.makeText(this, "头像设置成功", Toast.LENGTH_SHORT).show(); } } catch (IOException e) { Toast.makeText(this, "头像设置失败", Toast.LENGTH_SHORT).show(); } }
        }
    }
    private Bitmap cropSquare(Bitmap b) { int s = Math.min(b.getWidth(), b.getHeight()); return Bitmap.createBitmap(b, (b.getWidth()-s)/2, (b.getHeight()-s)/2, s, s); }
    private String saveAvatar(Bitmap bmp) { try { File d = new File(getFilesDir(), "avatars"); if (!d.exists()) d.mkdirs(); String n = "avatar_" + contactId + "_" + System.currentTimeMillis() + ".jpg"; File f = new File(d, n); FileOutputStream fos = new FileOutputStream(f); bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos); fos.flush(); fos.close(); delOldAvatar(); return f.getAbsolutePath(); } catch (IOException e) { return null; } }
    private void delOldAvatar() { if (currentContact != null && currentContact.getAvatarPath() != null) { File f = new File(currentContact.getAvatarPath()); if (f.exists()) f.delete(); } }
    @Override public void onRequestPermissionsResult(int requestCode, String[] perms, int[] results) {
        super.onRequestPermissionsResult(requestCode, perms, results);
        if (requestCode == CALL_PHONE_REQ && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) { if (currentContact != null && !currentContact.getPhones().isEmpty()) { Intent i = new Intent(Intent.ACTION_CALL); i.setData(Uri.parse("tel:" + currentContact.getMainPhone())); startActivity(i); } }
        else if (requestCode == PICK_IMG_REQ) { if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) pickGallery(); }
        else if (requestCode == CAM_REQ) { if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) pickCamera(); }
    }
    private void showDeleteDialog() { new AlertDialog.Builder(this).setTitle(R.string.delete).setMessage(R.string.confirm_delete).setPositiveButton(R.string.confirm, (d, w) -> { delOldAvatar(); dbHelper.deleteContact(contactId); Toast.makeText(this, R.string.contact_deleted, Toast.LENGTH_SHORT).show(); finish(); }).setNegativeButton(R.string.cancel, null).show(); }
}