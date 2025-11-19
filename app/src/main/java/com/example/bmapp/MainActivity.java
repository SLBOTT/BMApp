package com.example.bmapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "BadmintonMainPrefs";
    private static final String KEY_COURT_FEE = "court_fee";
    private static final String KEY_NUM_MEMBERS = "num_members";

    private EditText etCourtFee;
    private EditText etNumOfMembers;
    private EditText etShuttleFees;
    private EditText etNumOfNonMembers;
    private Button btnCalculate;
    private Button btnPlayers;
    private TextView tvPaymentForMember;
    private TextView tvPaymentForNonMember;
    private View cardResults;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize views
        initializeViews();

        // Load saved persistent data
        loadPersistentData();

        // Set up text watchers for persistent fields
        setupPersistentFieldWatchers();

        // Set click listeners
        btnCalculate.setOnClickListener(v -> calculateFeePerPlayer());
        btnPlayers.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PlayersActivity.class);
            startActivity(intent);
        });
    }

    private void initializeViews() {
        etCourtFee = findViewById(R.id.et_court_fee);
        etNumOfMembers = findViewById(R.id.et_num_of_members);
        etShuttleFees = findViewById(R.id.et_shuttle_fees);
        etNumOfNonMembers = findViewById(R.id.et_num_of_non_members);
        btnCalculate = findViewById(R.id.btn_calculate);
        btnPlayers = findViewById(R.id.btn_players);
        tvPaymentForMember = findViewById(R.id.tv_payment_for_member);
        tvPaymentForNonMember = findViewById(R.id.tv_payment_for_non_member);
        cardResults = findViewById(R.id.card_results);
    }

    private void loadPersistentData() {
        // Load saved court fee
        String savedCourtFee = sharedPreferences.getString(KEY_COURT_FEE, "");
        if (!savedCourtFee.isEmpty()) {
            etCourtFee.setText(savedCourtFee);
        }

        // Load saved number of members
        String savedNumMembers = sharedPreferences.getString(KEY_NUM_MEMBERS, "");
        if (!savedNumMembers.isEmpty()) {
            etNumOfMembers.setText(savedNumMembers);
        }
    }

    private void setupPersistentFieldWatchers() {
        // Watch for court fee changes
        etCourtFee.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                savePersistentData(KEY_COURT_FEE, s.toString());
            }
        });

        // Watch for number of members changes
        etNumOfMembers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                savePersistentData(KEY_NUM_MEMBERS, s.toString());
            }
        });
    }

    private void savePersistentData(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private void calculateFeePerPlayer() {
        String courtFeeStr = etCourtFee.getText().toString().trim();
        String membersStr = etNumOfMembers.getText().toString().trim();
        String shuttleFeesStr = etShuttleFees.getText().toString().trim();
        String nonMembersStr = etNumOfNonMembers.getText().toString().trim();

        // Validate inputs
        if (courtFeeStr.isEmpty()) {
            showError("කරුණාකර කෝට් ගාස්තුව ඇතුළත් කරන්න");
            etCourtFee.requestFocus();
            return;
        }

        if (membersStr.isEmpty()) {
            showError("කරුණාකර සාමාජිකයන් ගණන ඇතුළත් කරන්න");
            etNumOfMembers.requestFocus();
            return;
        }

        if (shuttleFeesStr.isEmpty()) {
            showError("කරුණාකර ෂටල් ගාස්තුව ඇතුළත් කරන්න");
            etShuttleFees.requestFocus();
            return;
        }

        if (nonMembersStr.isEmpty()) {
            showError("කරුණාකර සාමාජික නොවන අය ගණන ඇතුළත් කරන්න");
            etNumOfNonMembers.requestFocus();
            return;
        }

        try {
            double courtFee = Double.parseDouble(courtFeeStr);
            int numMembers = Integer.parseInt(membersStr);
            double shuttleFees = Double.parseDouble(shuttleFeesStr);
            int numNonMembers = Integer.parseInt(nonMembersStr);

            // Validate inputs
            if (courtFee < 0 || shuttleFees < 0) {
                showError("ගාස්තුව සෘණ අගයක් විය නොහැක");
                return;
            }

            if (numMembers < 0 || numNonMembers < 0) {
                showError("සංඛ්‍යා සෘණ අගයක් විය නොහැක");
                return;
            }

            int totalPlayers = numMembers + numNonMembers;
            if (totalPlayers == 0) {
                showError("අවම වශයෙන් එක් ක්‍රීඩකයෙකු සිටිය යුතුය");
                return;
            }

            // Calculate payments - total fees divided equally among all players
            double courtFeesPerPlayer = courtFee / totalPlayers;
            double shuttleFeesPerPlayer = shuttleFees / totalPlayers;
            double paymentForNonMember = courtFeesPerPlayer + shuttleFeesPerPlayer;
            double paymentForMember = shuttleFeesPerPlayer - (courtFee / numMembers) + courtFeesPerPlayer; // Members get court fee discount


            // Display results (same amount for both members and non-members in this version)
            tvPaymentForMember.setText(String.format(Locale.getDefault(), "රු %.2f", paymentForMember));
            tvPaymentForNonMember.setText(String.format(Locale.getDefault(), "රු %.2f", paymentForNonMember));

            // Make results card visible
            cardResults.setVisibility(View.VISIBLE);

        } catch (NumberFormatException e) {
            showError("කරුණාකර වලංගු සංඛ්‍යා ඇතුළත් කරන්න");
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
