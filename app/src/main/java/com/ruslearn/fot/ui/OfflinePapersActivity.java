package com.ruslearn.fot.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.ruslearn.fot.R;
import com.ruslearn.fot.data.DatabaseHelper;
import com.ruslearn.fot.model.Paper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Downloaded papers (FR14). Tap opens the local PDF with the device's viewer via
 * FileProvider; long-press removes the copy (file + row) after confirming.
 */
public class OfflinePapersActivity extends ToolbarActivity {

    private static final String FILE_PROVIDER = "com.ruslearn.fot.fileprovider";
    private static final String PDF_MIME = "application/pdf";
    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);

    private final List<DatabaseHelper.OfflinePaper> offlinePapers = new ArrayList<>();
    private final List<String> displayRows = new ArrayList<>();
    private ArrayAdapter<String> listAdapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_papers);
        bindToolbar(R.string.offline_title, true);

        tvEmpty = findViewById(R.id.tvOfflineEmpty);

        ListView lvOffline = findViewById(R.id.lvOffline);
        listAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, displayRows);
        lvOffline.setAdapter(listAdapter);
        lvOffline.setOnItemClickListener((parent, view, position, id) ->
                openOfflineCopy(position));
        lvOffline.setOnItemLongClickListener((parent, view, position, id) -> {
            promptRemove(position);
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        reload();
    }

    private void reload() {
        DatabaseHelper db = new DatabaseHelper(this);
        offlinePapers.clear();
        offlinePapers.addAll(db.getOfflinePapers());
        db.close();

        displayRows.clear();
        for (DatabaseHelper.OfflinePaper row : offlinePapers) {
            Paper paper = row.paper;
            displayRows.add(getString(R.string.offline_row_format,
                    paper.getCourseCode(), paper.getCourseName(),
                    DATE_FORMAT.format(new Date(row.savedAt))));
        }
        listAdapter.notifyDataSetChanged();
        tvEmpty.setVisibility(offlinePapers.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void openOfflineCopy(int position) {
        File file = new File(offlinePapers.get(position).filePath);
        Uri uri = FileProvider.getUriForFile(this, FILE_PROVIDER, file);
        Intent view = new Intent(Intent.ACTION_VIEW);
        view.setDataAndType(uri, PDF_MIME);
        view.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(view);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.toast_no_pdf_viewer, Toast.LENGTH_LONG).show();
        }
    }

    private void promptRemove(int position) {
        DatabaseHelper.OfflinePaper row = offlinePapers.get(position);
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_remove_offline_title)
                .setMessage(getString(R.string.dialog_remove_offline_message,
                        row.paper.getCourseCode()))
                .setPositiveButton(R.string.action_remove, (dialog, which) -> {
                    new File(row.filePath).delete();
                    DatabaseHelper db = new DatabaseHelper(this);
                    db.removeOfflinePaper(row.paper.getDriveFileId());
                    db.close();
                    reload();
                    Toast.makeText(this, R.string.toast_offline_removed,
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
