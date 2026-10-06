package com.ruslearn.fot.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ruslearn.fot.R;
import com.ruslearn.fot.data.DatabaseHelper;
import com.ruslearn.fot.data.DrivePaperFetcher;
import com.ruslearn.fot.model.Paper;

import java.io.File;

/**
 * One past paper's actions (FR05 open, FR06 favourite, FR14 offline copy).
 * Papers open through the device browser/Drive viewer, where the student's
 * university session can grant access to domain-restricted files.
 */
public class PaperDetailActivity extends ToolbarActivity {

    public static final String EXTRA_PAPER = "paper";

    private Paper paper;
    private DatabaseHelper db;

    private Button btnDownload;
    private Button btnFavourite;
    private TextView tvOfflineBadge;

    private boolean isFavourite;
    private boolean isOffline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paper_detail);

        paper = (Paper) getIntent().getSerializableExtra(EXTRA_PAPER);
        if (paper == null) {
            finish();
            return;
        }
        bindToolbar(getString(R.string.detail_title_format, paper.getCourseCode()), true);
        db = new DatabaseHelper(this);
        String driveId = paper.getDriveFileId();
        isFavourite = driveId != null && db.isFavourite(driveId);
        isOffline = driveId != null && db.isOffline(driveId);

        TextView tvCode = findViewById(R.id.tvDetailCode);
        TextView tvName = findViewById(R.id.tvDetailName);
        tvCode.setText(paper.getCourseCode());
        tvName.setText(paper.getCourseName());

        ((TextView) findViewById(R.id.tvDetailDepartment)).setText(paper.getDepartmentName());
        ((TextView) findViewById(R.id.tvDetailStudyYear)).setText(paper.getAcademicYear());
        ((TextView) findViewById(R.id.tvDetailSemester)).setText(paper.getSemester());
        ((TextView) findViewById(R.id.tvDetailExamYear)).setText(paper.getExamYear());

        tvOfflineBadge = findViewById(R.id.tvOfflineBadge);
        btnDownload = findViewById(R.id.btnDownloadPaper);
        btnFavourite = findViewById(R.id.btnFavourite);

        findViewById(R.id.btnOpenPaper).setOnClickListener(v -> openOnline());
        btnDownload.setOnClickListener(v -> toggleOffline());
        btnFavourite.setOnClickListener(v -> toggleFavourite());

        boolean hasDriveFile = driveId != null;
        btnDownload.setVisibility(hasDriveFile ? View.VISIBLE : View.GONE);
        btnFavourite.setVisibility(hasDriveFile ? View.VISIBLE : View.GONE);
        refreshActionButtons();
    }

    private void openOnline() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(paper.getViewUrl())));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.toast_no_browser, Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleFavourite() {
        String driveId = paper.getDriveFileId();
        if (driveId == null) {
            Toast.makeText(this, R.string.toast_no_drive_file, Toast.LENGTH_SHORT).show();
            return;
        }
        if (isFavourite) {
            db.removeFavourite(driveId);
            isFavourite = false;
            Toast.makeText(this, R.string.toast_fav_removed, Toast.LENGTH_SHORT).show();
        } else {
            db.addFavourite(paper);
            isFavourite = true;
            Toast.makeText(this, R.string.toast_fav_saved, Toast.LENGTH_SHORT).show();
        }
        refreshActionButtons();
    }

    private void toggleOffline() {
        String driveId = paper.getDriveFileId();
        if (driveId == null) {
            Toast.makeText(this, R.string.toast_no_drive_file, Toast.LENGTH_SHORT).show();
            return;
        }
        if (isOffline) {
            deleteOfflineCopy(driveId);
            isOffline = false;
            Toast.makeText(this, R.string.toast_offline_removed, Toast.LENGTH_SHORT).show();
            refreshActionButtons();
            return;
        }

        btnDownload.setEnabled(false);
        Toast.makeText(this, R.string.toast_downloading, Toast.LENGTH_SHORT).show();
        new Thread(() -> new DrivePaperFetcher(this).download(paper, new DrivePaperFetcher.Callback() {
            @Override
            public void onDownloaded(File file) {
                runOnUiThread(() -> {
                    db.addOfflinePaper(paper, file.getAbsolutePath());
                    isOffline = true;
                    btnDownload.setEnabled(true);
                    refreshActionButtons();
                    Toast.makeText(PaperDetailActivity.this,
                            R.string.toast_offline_saved, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailed(String message) {
                runOnUiThread(() -> {
                    android.util.Log.i("PaperDownload", "not saved: " + message);
                    btnDownload.setEnabled(true);
                    Toast.makeText(PaperDetailActivity.this, message, Toast.LENGTH_LONG).show();
                });
            }
        })).start();
    }

    /** Deletes the offline PDF file if present, then its metadata row. */
    private void deleteOfflineCopy(String driveId) {
        for (DatabaseHelper.OfflinePaper row : db.getOfflinePapers()) {
            if (driveId.equals(row.paper.getDriveFileId())) {
                new File(row.filePath).delete();
                break;
            }
        }
        db.removeOfflinePaper(driveId);
    }

    private void refreshActionButtons() {
        btnFavourite.setText(isFavourite
                ? R.string.action_favourite_remove
                : R.string.action_favourite_save);
        btnDownload.setText(isOffline
                ? R.string.action_remove_offline
                : R.string.action_download);
        tvOfflineBadge.setVisibility(isOffline ? View.VISIBLE : View.GONE);
    }
}
