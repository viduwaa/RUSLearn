package com.ruslearn.fot.ui;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ruslearn.fot.R;

/**
 * Base activity for sub-screens with the shared toolbar include (audit S4).
 * Subclasses call {@link #bindToolbar(int, boolean)} after setContentView; the
 * toolbar title replaces the invisible setTitle(...) calls this app used to make.
 */
public abstract class ToolbarActivity extends AppCompatActivity {

    /**
     * Binds the view_toolbar.xml include.
     *
     * @param showUp true on stacked pages (arrow = finish()); false on top-level
     *               screens that wear the bottom nav instead.
     */
    protected void bindToolbar(int titleResId, boolean showUp) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar == null) {
            return;
        }
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(titleResId);
            getSupportActionBar().setDisplayHomeAsUpEnabled(showUp);
        }
    }

    /** Binds the toolbar with a literal string title (detail screen formats one). */
    protected void bindToolbar(CharSequence title, boolean showUp) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar == null) {
            return;
        }
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(showUp);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Practical-notes pattern: the up arrow simply closes the screen.
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
