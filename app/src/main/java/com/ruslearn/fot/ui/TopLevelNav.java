package com.ruslearn.fot.ui;

import android.content.Intent;
import android.view.View;
import android.widget.ListView;
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
     * The ⚙ settings sheet — the six actions in a ListView inside a
     * BottomSheetDialog. ListView row taps are framework-owned (the same mechanism
     * as the papers/favourites lists), so the whole row is one reliable touch
     * target; the BottomSheet adds native drag-to-dismiss.
     */
    private void showSettingsSheet() {
        View sheet = activity.getLayoutInflater()
                .inflate(R.layout.view_settings_sheet, null);
        ListView list = sheet.findViewById(R.id.lvSettings);

        String[] titles = {
                activity.getString(R.string.settings_favourites),
                activity.getString(R.string.settings_offline),
                activity.getString(R.string.settings_request),
                activity.getString(R.string.settings_theme),
                activity.getString(R.string.settings_developer),
                activity.getString(R.string.settings_sign_out)
        };
        String[] captions = {
                activity.getString(R.string.settings_favourites_caption),
                activity.getString(R.string.settings_offline_caption),
                activity.getString(R.string.settings_request_caption),
                themeCaption(),
                activity.getString(R.string.settings_developer_caption),
                ""
        };
        int[] icons = {
                R.drawable.ic_star_nav,
                R.drawable.ic_download,
                R.drawable.ic_edit,
                R.drawable.ic_theme,
                R.drawable.ic_info,
                R.drawable.ic_power
        };

        android.widget.SimpleAdapter rows = new android.widget.SimpleAdapter(
                activity, buildRows(titles, captions), R.layout.item_settings_row,
                new String[]{"icon", "title", "caption"},
                new int[]{R.id.ivRowIcon, R.id.tvRowTitle, R.id.tvRowCaption});
        // SimpleAdapter can't set drawables — bind icons manually per row.
        rows.setViewBinder(new android.widget.SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view.getId() == R.id.ivRowIcon) {
                    ((android.widget.ImageView) view).setBackgroundResource(
                            icons[((Number) data).intValue()]);
                    ((android.widget.ImageView) view).setColorFilter(themeIconTint());
                    return true;
                }
                if (view.getId() == R.id.tvRowTitle
                        && activity.getString(R.string.settings_sign_out).equals(data)) {
                    ((TextView) view).setTextColor(activity.getColor(R.color.colorStampRed));
                }
                return false;
            }
        });
        list.setAdapter(rows);

        com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(activity);
        dialog.setContentView(sheet);
        list.setOnItemClickListener((parent, view, position, id) -> {
            dialog.dismiss();
            if (position == 0) {
                activity.startActivity(new Intent(activity, FavouritesActivity.class));
            } else if (position == 1) {
                activity.startActivity(new Intent(activity, OfflinePapersActivity.class));
            } else if (position == 2) {
                activity.startActivity(new Intent(activity, PaperRequestActivity.class));
            } else if (position == 3) {
                cycleTheme();
            } else if (position == 4) {
                showDeveloperDetails();
            } else if (position == 5) {
                signOutHost.onSignOut();
            }
        });

        dialog.show();
    }

    /** SimpleAdapter rows: icon index, title, caption per action. */
    private static java.util.List<java.util.HashMap<String, Object>> buildRows(
            String[] titles, String[] captions) {
        java.util.List<java.util.HashMap<String, Object>> rows = new java.util.ArrayList<>();
        for (int i = 0; i < titles.length; i++) {
            java.util.HashMap<String, Object> row = new java.util.HashMap<>();
            row.put("icon", i);
            row.put("title", titles[i]);
            row.put("caption", captions[i]);
            rows.add(row);
        }
        return rows;
    }

    private String themeCaption() {
        String mode = new ThemePrefs(activity).load();
        if (ThemePrefs.PAPER.equals(mode)) {
            return activity.getString(R.string.theme_paper);
        }
        if (ThemePrefs.INK.equals(mode)) {
            return activity.getString(R.string.theme_ink);
        }
        return activity.getString(R.string.theme_system);
    }

    /** Icon tint follows the theme so glyphs stay visible in Paper and Ink. */
    private int themeIconTint() {
        return activity.getColor(R.color.colorFaculty);
    }

    /**
     * Paper → Ink → System → Paper; saves, then applies. The activity rebuild
     * (AppCompatDelegate contract for night-mode changes) dismisses the sheet;
     * the fresh sheet shows the new caption.
     */
    private void cycleTheme() {
        ThemePrefs prefs = new ThemePrefs(activity);
        String mode = prefs.load();
        String next;
        switch (mode) {
            case ThemePrefs.PAPER:
                next = ThemePrefs.INK;
                break;
            case ThemePrefs.INK:
                next = ThemePrefs.SYSTEM;
                break;
            default:
                next = ThemePrefs.PAPER;
        }
        prefs.save(next);
        ThemeSwitcher.apply(prefs);
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
