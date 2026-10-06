package com.ruslearn.fot.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.ruslearn.fot.R;
import com.ruslearn.fot.data.DatabaseHelper;
import com.ruslearn.fot.model.Paper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Saved papers (FR06). Tap opens the paper's detail screen; long-press removes
 * after a confirm dialog (the Practical-05 interaction pattern). The empty state
 * is the shared view_empty_state include with a "Browse papers" CTA.
 */
public class FavouritesActivity extends ToolbarActivity {

    private final List<Paper> favourites = new ArrayList<>();
    private PaperAdapter adapter;
    private TextView tvEmpty;
    private Button btnBrowse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);
        bindToolbar(R.string.favourites_title, true);

        tvEmpty = findViewById(R.id.tvEmptyTitle);
        tvEmpty.setText(R.string.favourites_empty);
        btnBrowse = findViewById(R.id.btnEmptyAction);
        btnBrowse.setText(R.string.dashboard_browse_papers);
        btnBrowse.setOnClickListener(v ->
                startActivity(new Intent(this, PapersActivity.class)));

        ListView lvFavourites = findViewById(R.id.lvFavourites);
        adapter = new PaperAdapter(this, favourites);
        lvFavourites.setAdapter(adapter);
        lvFavourites.setOnItemClickListener((parent, view, position, id) -> {
            Paper paper = favourites.get(position);
            Intent intent = new Intent(this, PaperDetailActivity.class);
            intent.putExtra(PaperDetailActivity.EXTRA_PAPER, paper);
            startActivity(intent);
        });
        lvFavourites.setOnItemLongClickListener((parent, view, position, id) -> {
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
        List<Paper> saved = db.getFavourites();
        Set<String> favouriteIds = new HashSet<>();
        for (Paper paper : saved) {
            favouriteIds.add(paper.getDriveFileId());
        }
        db.close();

        favourites.clear();
        favourites.addAll(saved);
        adapter.setFavouriteIds(favouriteIds);
        adapter.notifyDataSetChanged();
        boolean empty = saved.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        btnBrowse.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void promptRemove(int position) {
        Paper paper = favourites.get(position);
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_remove_favourite_title)
                .setMessage(getString(R.string.dialog_remove_favourite_message,
                        paper.getCourseCode()))
                .setPositiveButton(R.string.action_remove, (dialog, which) -> {
                    DatabaseHelper db = new DatabaseHelper(this);
                    db.removeFavourite(paper.getDriveFileId());
                    db.close();
                    reload();
                    Toast.makeText(this, R.string.toast_fav_removed,
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
