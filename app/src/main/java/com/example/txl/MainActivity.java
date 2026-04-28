package com.example.txl;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MainActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private ContactAdapter adapter;
    private DatabaseHelper dbHelper;
    private EditText etSearch;
    private ImageButton btnAdd;
    private ImageButton btnThemeToggle;
    private SideIndexView sideIndex;
    private TextView tvEmpty;
    private LinearLayout tabDialer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new DatabaseHelper(this);
        initViews();
        loadContacts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadContacts();
        updateThemeIcon();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        etSearch = findViewById(R.id.et_search);
        btnAdd = findViewById(R.id.btn_add);
        btnThemeToggle = findViewById(R.id.btn_theme_toggle);
        sideIndex = findViewById(R.id.side_index);
        tvEmpty = findViewById(R.id.tv_empty);
        tabDialer = findViewById(R.id.tab_dialer);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditContactActivity.class);
            startActivity(intent);
        });

        btnThemeToggle.setOnClickListener(v -> ThemeManager.toggleTheme(this));

        tabDialer.setOnClickListener(v -> {
            DialerBottomSheet bottomSheet = DialerBottomSheet.newInstance(null);
            bottomSheet.show(getSupportFragmentManager(), "dialer");
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) { adapter.filter(s.toString()); updateEmptyState(); }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        sideIndex.setOnIndexSelectedListener(letter -> {
            if (adapter != null) {
                int position = adapter.getPositionForLetter(letter);
                if (position != -1) recyclerView.scrollToPosition(position);
            }
        });
    }

    private void updateThemeIcon() {
        if (ThemeManager.isDarkTheme(this)) {
            btnThemeToggle.setImageResource(R.drawable.ic_theme_light);
        } else {
            btnThemeToggle.setImageResource(R.drawable.ic_theme_dark);
        }
    }

    private void loadContacts() {
        List<Contact> contactList = dbHelper.getAllContacts();
        adapter = new ContactAdapter(contactList);
        adapter.setOnItemClickListener(new ContactAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Contact contact) {
                Intent intent = new Intent(MainActivity.this, ContactDetailActivity.class);
                intent.putExtra(ContactDetailActivity.EXTRA_CONTACT_ID, contact.getId());
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(Contact contact) {
            }
        });
        recyclerView.setAdapter(adapter);
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (adapter != null && adapter.getItemCount() == 0) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }
}
