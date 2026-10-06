package com.ruslearn.fot.ui;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.ruslearn.fot.R;
import com.ruslearn.fot.model.GradePoint;

/**
 * Band → stamp colour mapping (audit S12, spec §3.2). The single place that turns
 * {@link GradePoint#bandFor(double)} into a visible colour; call
 * {@link #apply(TextView, double)} after setting the stamp text.
 */
public final class Stamp {

    private static final int TINT_ALPHA = 36; // ~14% background under the text colour

    private Stamp() {
        // Static helpers only.
    }

    /** Colours a stamp view for the given GPA using the band mapping. */
    public static void apply(TextView stamp, double gpa) {
        int colour = colorFor(stamp, gpa);
        stamp.setTextColor(colour);
        GradientDrawable background = (GradientDrawable) stamp.getBackground();
        background.setTint(tintOverCard(colour,
                ContextCompat.getColor(stamp.getContext(), R.color.colorCard)));
    }

    /** Source colour at ~14% alpha, alpha-composited over the card colour. */
    private static int tintOverCard(int source, int card) {
        double fraction = TINT_ALPHA / 255.0;
        int r = (int) (Color.red(source) * fraction + Color.red(card) * (1 - fraction));
        int g = (int) (Color.green(source) * fraction + Color.green(card) * (1 - fraction));
        int b = (int) (Color.blue(source) * fraction + Color.blue(card) * (1 - fraction));
        return Color.argb(255, r, g, b);
    }

    private static int colorFor(TextView stamp, double gpa) {
        String band = GradePoint.bandFor(gpa);
        if (band.startsWith("First")) {
            return ContextCompat.getColor(stamp.getContext(), R.color.colorStampGreen);
        }
        if (band.startsWith("Second Class (Upper)")) {
            return ContextCompat.getColor(stamp.getContext(), R.color.colorFaculty);
        }
        if (band.startsWith("Second Class (Lower)")) {
            return ContextCompat.getColor(stamp.getContext(), R.color.colorStampBlue);
        }
        if (band.startsWith("General")) {
            return ContextCompat.getColor(stamp.getContext(), R.color.colorStampAmber);
        }
        return ContextCompat.getColor(stamp.getContext(), R.color.colorStampRed);
    }
}
