package com.example.bmapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PlayersActivity extends AppCompatActivity {

    private LinearLayout playersContainer;
    private Button btnBack, btnAdd, btnClear;
    private TextView tvEmptyState;
    private List<Player> playersList;
    private DecimalFormat df;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_players);

        // Initialize variables
        playersList = new ArrayList<>();
        df = new DecimalFormat("#0.00");

        // Initialize views
        initializeViews();

        // Set click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        playersContainer = findViewById(R.id.players_container);
        btnBack = findViewById(R.id.btn_back);
        btnAdd = findViewById(R.id.btn_add);
        btnClear = findViewById(R.id.btn_clear);
        tvEmptyState = findViewById(R.id.tv_empty_state);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnAdd.setOnClickListener(v -> showAddPlayerDialog());

        btnClear.setOnClickListener(v -> showClearConfirmDialog());
    }

    private void showAddPlayerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_player, null);

        TextInputEditText etPlayerName = dialogView.findViewById(R.id.et_player_name);
        TextInputEditText etPlayerAmount = dialogView.findViewById(R.id.et_player_amount);

        builder.setView(dialogView)
                .setTitle(getString(R.string.dialog_add_player_title))
                .setPositiveButton(getString(R.string.btn_add), (dialog, which) -> {
                    String name = etPlayerName.getText().toString().trim();
                    String amountStr = etPlayerAmount.getText().toString().trim();

                    if (validatePlayerInput(name, amountStr)) {
                        double amount = Double.parseDouble(amountStr);
                        Player player = new Player(name, amount);
                        playersList.add(player);
                        addPlayerView(player);
                        updateEmptyState();
                    }
                })
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .show();
    }

    private boolean validatePlayerInput(String name, String amountStr) {
        if (name.isEmpty()) {
            showError(getString(R.string.error_empty_player_name));
            return false;
        }

        if (amountStr.isEmpty()) {
            showError(getString(R.string.error_empty_player_amount));
            return false;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount < 0) {
                showError(getString(R.string.error_negative_amount));
                return false;
            }
        } catch (NumberFormatException e) {
            showError(getString(R.string.error_invalid_amount));
            return false;
        }

        // Check for duplicate names
        for (Player player : playersList) {
            if (player.getName().equalsIgnoreCase(name)) {
                showError(getString(R.string.error_duplicate_player));
                return false;
            }
        }

        return true;
    }

    private void addPlayerView(Player player) {
        View playerView = LayoutInflater.from(this).inflate(R.layout.item_player, null);

        TextView tvPlayerName = playerView.findViewById(R.id.tv_player_name);
        TextView tvPlayerAmount = playerView.findViewById(R.id.tv_player_amount);
        MaterialButton btnDelete = playerView.findViewById(R.id.btn_delete_player);

        // Add null checks
        if (tvPlayerName == null || tvPlayerAmount == null || btnDelete == null) {
            showError("Error loading player view");
            return;
        }

        tvPlayerName.setText(player.getName());
        tvPlayerAmount.setText(getString(R.string.amount_format, df.format(player.getAmount())));

        // Use WeakReference to prevent memory leaks
        WeakReference<View> playerViewRef = new WeakReference<>(playerView);
        btnDelete.setOnClickListener(v -> {
            View view = playerViewRef.get();
            if (view != null) {
                showDeleteConfirmDialog(player, view);
            }
        });

        playersContainer.addView(playerView);
    }

    private void showDeleteConfirmDialog(Player player, View playerView) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_delete_title))
                .setMessage(getString(R.string.confirm_delete_message, player.getName()))
                .setPositiveButton(getString(R.string.btn_delete), (dialog, which) -> {
                    playersList.remove(player);
                    playersContainer.removeView(playerView);
                    updateEmptyState();
                    showMessage(getString(R.string.player_deleted, player.getName()));
                })
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .show();
    }

    private void showClearConfirmDialog() {
        if (playersList.isEmpty()) {
            showMessage(getString(R.string.no_players_to_clear));
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_clear_title))
                .setMessage(getString(R.string.confirm_clear_message))
                .setPositiveButton(getString(R.string.btn_clear), (dialog, which) -> {
                    playersList.clear();
                    playersContainer.removeAllViews();
                    updateEmptyState();
                    showMessage(getString(R.string.all_players_cleared));
                })
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .show();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateEmptyState() {
        if (playersList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
        }
    }

    // Player model class
    public static class Player {
        private String name;
        private double amount;

        public Player(String name, double amount) {
            this.name = name;
            this.amount = amount;
        }

        public String getName() {
            return name;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }
    }
}
