package com.ruslearn.fot.ui;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ruslearn.fot.R;
import com.ruslearn.fot.data.ThemePrefs;

/**
 * Shared bottom-nav + settings-sheet behaviour for the four top-level screens
 * (ticket 02b). Owns the 4-slot bar (Home · Papers · GPA · Tracking), the
 * back-to-dashboard route on Papers/GPA/Tracking, and the Home ⚙ settings sheet
 * (Favourites, Offline, Request, Developer details, Sign out). Top-level screens
 * extend {@link ToolbarActivity} (or plain AppCompatActivity) and call
 * {@link #attach(int)} after the content view is set.
 */
public final class TopLevelNav {

    /** The Home screen's sign-out (Dashboard keeps owning the ADR-0001 revoke flow). */
    interface SignOutHost {
        void onSignOut();
    }

    private final AppCompatActivity activity;
    private final SignOutHost signOutHost;

    public TopLevelNav(AppCompatActivity activity, SignOutHost signOutHost) {
        this.activity = activity;
        this.signOutHost = signOutHost;
    }

    /**
     * Wires the bar and, on Home, the ⚙ settings button. Call after bindToolbar(...)
     * once the content view is set.
     */
    public void attach(int activeItemId) {
        BottomNavigationView bar = activity.findViewById(R.id.bottomNav);
        if (bar != null) {
            bar.setSelectedItemId(activeItemId);
            bar.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == activeItemId) {
                    return true; // re-tap on the current slot: nothing to do
                }
                if (id == R.id.nav_home) {
                    openTopLevel(DashboardActivity.class);
                } else if (id == R.id.nav_papers) {
                    openTopLevel(PapersActivity.class);
                } else if (id == R.id.nav_gpa) {
                    openTopLevel(GpaCalculatorActivity.class);
                } else if (id == R.id.nav_tracking) {
                    openTopLevel(SubjectTrackingActivity.class);
                }
                // No finish(): SINGLE_TOP keeps each screen's state alive so the
                // student hops between tabs without losing filters or picks.
                return true;
            });

            // System back on Papers/GPA/Tracking routes to the nav root (Dashboard);
            // on Home no callback is added so back exits the app normally.
            if (activeItemId != R.id.nav_home) {
                activity.getOnBackPressedDispatcher().addCallback(activity,
                        new OnBackPressedCallback(true) {
                            @Override
                            public void handleOnBackPressed() {
                                openTopLevel(DashboardActivity.class);
                            }
                        });
            }
        }

        if (activeItemId == R.id.nav_home) {
            View settings = activity.findViewById(R.id.btnSettings);
            if (settings != null) {
                settings.setOnClickListener(v -> showSettingsSheet());
            }
        }
    }

    private void openTopLevel(Class<?> target) {
        Intent intent = new Intent(activity, target);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
    }

    /**
     * The ⚙ settings sheet — icon-tile rows matching the mockup (preview section 06).
     * Built as an AlertDialog with a custom view (transparent frame) so row clicks
     * route through the standard dialog window; every row dismisses, then acts.
     */
    private void showSettingsSheet() {
        View sheet = activity.getLayoutInflater()
                .inflate(R.layout.view_settings_sheet, null);
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setView(sheet)
                .create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        sheet.findViewById(R.id.rowFavourites).setOnClickListener(v -> {
            dialog.dismiss();
            activity.startActivity(new Intent(activity, FavouritesActivity.class));
        });
        sheet.findViewById(R.id.rowOffline).setOnClickListener(v -> {
            dialog.dismiss();
            activity.startActivity(new Intent(activity, OfflinePapersActivity.class));
        });
        sheet.findViewById(R.id.rowRequest).setOnClickListener(v -> {
            dialog.dismiss();
            activity.startActivity(new Intent(activity, PaperRequestActivity.class));
        });
        sheet.findViewById(R.id.rowTheme).setOnClickListener(v ->
                cycleTheme(dialog));
        sheet.findViewById(R.id.rowDeveloper).setOnClickListener(v -> {
            dialog.dismiss();
            showDeveloperDetails();
        });
        sheet.findViewById(R.id.rowSignOut).setOnClickListener(v -> {
            dialog.dismiss();
            signOutHost.onSignOut();
        });

        dialog.show();
    }

    /** Paper → Ink → System → Paper; saves, re-applies, updates the row caption. */
    private void cycleTheme(AlertDialog dialog) {
        ThemePrefs prefs = new ThemePrefs(activity);
        String mode = prefs.load();
        String next;
        int label;
        switch (mode) {
            case ThemePrefs.PAPER:
                next = ThemePrefs.INK;
                label = R.string.theme_ink;
                break;
            case ThemePrefs.INK:
                next = ThemePrefs.SYSTEM;
                label = R.string.theme_system;
                break;
            default:
                next = ThemePrefs.PAPER;
                label = R.string.theme_paper;
        }
        prefs.save(next);
        ThemeSwitcher.apply(prefs);

        TextView caption = dialog.findViewById(R.id.tvThemeValue);
        if (caption != null) {
            caption.setText(label);
        }
    }

    private void showDeveloperDetails() {
        String version = "1.0";
        try {
            version = activity.getPackageManager()
                    .getPackageInfo(activity.getPackageName(), 0).versionName;
        } catch (Exception ignored) {
            // Fall back to the hardcoded version string.
        }
        new AlertDialog.Builder(activity)
                .setTitle(R.string.settings_developer)
                .setMessage(activity.getString(R.string.developer_details_format, version))
                .setPositiveButton(R.string.action_close, null)
                .show();
    }
}
