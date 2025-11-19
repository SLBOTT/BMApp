package com.example.bmapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private EditText etNumberOfPlayers;
    private EditText etCourtFees;
    private EditText etShuttleFees;
    private Button btnCalculate;
    private Button btnPlayers;
    private TextView tvResult;
    private TextView tvTotalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        initializeViews();

        // Set click listeners
        btnCalculate.setOnClickListener(v -> calculateFeePerPlayer());
        btnPlayers.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PlayersActivity.class);
            startActivity(intent);
        });
    }

    private void initializeViews() {
        etNumberOfPlayers = findViewById(R.id.et_number_of_players);
        etCourtFees = findViewById(R.id.et_court_fees);
        etShuttleFees = findViewById(R.id.et_shuttle_fees);
        btnCalculate = findViewById(R.id.btn_calculate);
        btnPlayers = findViewById(R.id.btn_players);
        tvResult = findViewById(R.id.tv_result);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
    }

    private void calculateFeePerPlayer() {
        String playersStr = etNumberOfPlayers.getText().toString().trim();
        String courtFeesStr = etCourtFees.getText().toString().trim();
        String shuttleFeesStr = etShuttleFees.getText().toString().trim();

        // Validate inputs
        if (playersStr.isEmpty()) {
            showError(getString(R.string.error_empty_players));
            etNumberOfPlayers.requestFocus();
            return;
        }

        if (courtFeesStr.isEmpty()) {
            showError(getString(R.string.error_empty_court_fees));
            etCourtFees.requestFocus();
            return;
        }

        if (shuttleFeesStr.isEmpty()) {
            showError(getString(R.string.error_empty_shuttle_fees));
            etShuttleFees.requestFocus();
            return;
        }

        try {
            int numberOfPlayers = Integer.parseInt(playersStr);
            double courtFees = Double.parseDouble(courtFeesStr);
            double shuttleFees = Double.parseDouble(shuttleFeesStr);

            // Validate number of players
            if (numberOfPlayers <= 0) {
                showError(getString(R.string.error_invalid_players));
                etNumberOfPlayers.requestFocus();
                return;
            }

            // Validate fees
            if (courtFees < 0 || shuttleFees < 0) {
                showError(getString(R.string.error_negative_fees));
                return;
            }

            // Calculate total and per player amount
            double totalAmount = courtFees + shuttleFees;
            double amountPerPlayer = totalAmount / numberOfPlayers;


            // Display results
            tvTotalAmount.setText(getString(R.string.result_total, totalAmount));
            tvResult.setText(getString(R.string.result_per_player, amountPerPlayer));

            // Make result views visible
            tvTotalAmount.setVisibility(View.VISIBLE);
            tvResult.setVisibility(View.VISIBLE);

        } catch (NumberFormatException e) {
            showError(getString(R.string.error_invalid_numbers));
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
