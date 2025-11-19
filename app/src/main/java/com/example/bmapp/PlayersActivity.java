package com.example.bmapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PlayersActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "BadmintonPlayersPrefs";
    private static final String PLAYERS_KEY = "players_list";

    private LinearLayout playersContainer;
    private Button btnBack, btnAdd, btnClear;
    private List<Player> playersList;
    private DecimalFormat df;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_players);

        // Initialize variables
        playersList = new ArrayList<>();
        df = new DecimalFormat("#0.00");
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        gson = new Gson();

        // Initialize views
        initializeViews();

        // Load saved players data
        loadPlayersData();

        // Set click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        playersContainer = findViewById(R.id.players_table_container);
        btnBack = findViewById(R.id.btn_back);
        btnAdd = findViewById(R.id.btn_add);
        btnClear = findViewById(R.id.btn_clear);
        // tvEmptyState = findViewById(R.id.tv_empty_state); // Remove this for now since layout changed
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnAdd.setOnClickListener(v -> showAddPlayerDialog());

        btnClear.setOnClickListener(v -> showClearConfirmDialog());
    }

    private void savePlayersData() {
        String json = gson.toJson(playersList);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PLAYERS_KEY, json);
        editor.apply();
    }

    private void loadPlayersData() {
        String json = sharedPreferences.getString(PLAYERS_KEY, null);
        if (json != null) {
            Type type = new TypeToken<List<Player>>(){}.getType();
            List<Player> savedPlayers = gson.fromJson(json, type);
            if (savedPlayers != null) {
                playersList.clear();
                playersList.addAll(savedPlayers);

                // Display loaded players
                for (Player player : playersList) {
                    addPlayerView(player);
                }
            }
        }
    }

    private void showAddPlayerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_player, null);

        TextInputEditText etPlayerName = dialogView.findViewById(R.id.et_player_name);
        TextInputEditText etPlayerAmount = dialogView.findViewById(R.id.et_player_amount);

        builder.setView(dialogView)
                .setTitle(getString(R.string.dialog_add_player_title))
                .setPositiveButton(getString(R.string.btn_add), (dialog, which) -> {
                    String name = etPlayerName.getText() != null ? etPlayerName.getText().toString().trim() : "";
                    String amountStr = etPlayerAmount.getText() != null ? etPlayerAmount.getText().toString().trim() : "";

                    if (validatePlayerInput(name, amountStr)) {
                        double amount = Double.parseDouble(amountStr);
                        Player player = new Player(name, amount);
                        playersList.add(player);
                        addPlayerView(player);
                        savePlayersData();
                    }
                })
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .show();
    }

    private void showEditPlayerDialog(Player player, View playerView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_player, null);

        TextInputEditText etPlayerName = dialogView.findViewById(R.id.et_edit_player_name);
        TextInputEditText etPlayerAmount = dialogView.findViewById(R.id.et_edit_player_amount);

        // Pre-fill with current values
        etPlayerName.setText(player.getName());
        etPlayerAmount.setText(String.valueOf(player.getAmount()));

        builder.setView(dialogView)
                .setTitle(getString(R.string.dialog_edit_player_title))
                .setPositiveButton(getString(R.string.btn_update), (dialog, which) -> {
                    String name = etPlayerName.getText() != null ? etPlayerName.getText().toString().trim() : "";
                    String amountStr = etPlayerAmount.getText() != null ? etPlayerAmount.getText().toString().trim() : "";

                    if (validatePlayerEditInput(name, amountStr, player)) {
                        double amount = Double.parseDouble(amountStr);

                        // Update player data
                        player.setName(name);
                        player.setAmount(amount);

                        // Update the view
                        updatePlayerView(playerView, player);

                        // Save the updated data
                        savePlayersData();

                        showMessage(getString(R.string.player_updated, name));
                    }
                })
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .show();
    }

    private boolean validatePlayerEditInput(String name, String amountStr, Player currentPlayer) {
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

        // Check for duplicate names (excluding current player)
        for (Player player : playersList) {
            if (player != currentPlayer && player.getName().equalsIgnoreCase(name)) {
                showError(getString(R.string.error_duplicate_player));
                return false;
            }
        }

        return true;
    }

    private void updatePlayerView(View playerView, Player player) {
        TextView tvPlayerName = playerView.findViewById(R.id.tv_player_name);
        TextView tvPlayerAmount = playerView.findViewById(R.id.tv_player_amount);

        if (tvPlayerName != null && tvPlayerAmount != null) {
            tvPlayerName.setText(player.getName());
            tvPlayerAmount.setText(getString(R.string.amount_format, df.format(player.getAmount())));
        }
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
        View playerView = LayoutInflater.from(this).inflate(R.layout.item_player_row, playersContainer, false);

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

        // Make the entire row clickable for editing
        playerView.setOnClickListener(v -> {
            View view = playerViewRef.get();
            if (view != null) {
                showEditPlayerDialog(player, view);
            }
        });

        // Delete button functionality
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
                    savePlayersData();
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
                    savePlayersData();
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

        public void setName(String name) {
            this.name = name;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }
    }
}
