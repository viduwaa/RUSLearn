package com.ruslearn.fot.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ruslearn.fot.R;
import com.ruslearn.fot.api.PaperApi;

import java.io.IOException;

/**
 * Paper request form (FR13): asks the portal for a missing paper through its
 * feature-request endpoint. Limited to 500 characters with a live counter.
 */
public class PaperRequestActivity extends AppCompatActivity {

    private static final int MAX_REQUEST_CHARS = 500;

    private EditText etRequest;
    private TextView tvCounter;
    private TextView tvError;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paper_request);
        setTitle(getString(R.string.request_title));

        etRequest = findViewById(R.id.etRequest);
        tvCounter = findViewById(R.id.tvRequestCounter);
        tvError = findViewById(R.id.tvRequestError);
        btnSubmit = findViewById(R.id.btnSubmitRequest);

        etRequest.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_REQUEST_CHARS)});
        tvCounter.setText(getString(R.string.request_counter_format, 0, MAX_REQUEST_CHARS));
        etRequest.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed — the counter updates on text changes only.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvCounter.setText(getString(R.string.request_counter_format,
                        etRequest.length(), MAX_REQUEST_CHARS));
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed.
            }
        });

        btnSubmit.setOnClickListener(v -> submitRequest());
    }

    private void submitRequest() {
        String text = etRequest.getText().toString().trim();
        if (text.isEmpty()) {
            tvError.setText(R.string.request_empty);
            tvError.setVisibility(android.view.View.VISIBLE);
            return;
        }
        tvError.setVisibility(android.view.View.GONE);
        btnSubmit.setEnabled(false);
        tvCounter.setText(R.string.request_sending);

        new Thread(() -> {
            try {
                PaperApi.sendFeatureRequest(text);
                runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    etRequest.setText("");
                    Toast.makeText(this, R.string.toast_request_sent,
                            Toast.LENGTH_LONG).show();
                    finish();
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    tvCounter.setText(getString(R.string.request_counter_format,
                            etRequest.length(), MAX_REQUEST_CHARS));
                    tvError.setText(e.getMessage());
                    tvError.setVisibility(android.view.View.VISIBLE);
                });
            }
        }).start();
    }
}
