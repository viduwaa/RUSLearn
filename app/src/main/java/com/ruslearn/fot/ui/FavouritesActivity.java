package com.ruslearn.fot.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ruslearn.fot.R;
import com.ruslearn.fot.data.DatabaseHelper;
import com.ruslearn.fot.model.Paper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Saved papers (FR06). Tap opens the paper's detail screen; long-press removes
 * after a confirm dialog (the Practical-05 interaction pattern).
 */
public class FavouritesActivity extends AppCompatActivity {

    private final List<Paper> favourites = new ArrayList<>();
    private PaperAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);
        setTitle(getString(R.string.favourites_title));

        tvEmpty = findViewById(R.id.tvFavouritesEmpty);

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
        tvEmpty.setVisibility(saved.isEmpty() ? View.VISIBLE : View.GONE);
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
