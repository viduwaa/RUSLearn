package com.ruslearn.fot.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ruslearn.fot.R;
import com.ruslearn.fot.model.Paper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Row renderer for the papers list (FR02–FR04). The star reflects the favourite
 * state; row bodies show code, subject title and the filterable context line.
 */
public class PaperAdapter extends ArrayAdapter<Paper> {

    private final Set<String> favouriteIds = new HashSet<>();

    public PaperAdapter(@NonNull Context context, @NonNull List<Paper> papers) {
        super(context, 0, papers);
    }

    /** Refreshes the remembered favourite set (called after DB-backed changes). */
    public void setFavouriteIds(Set<String> ids) {
        favouriteIds.clear();
        favouriteIds.addAll(ids);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_paper, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Paper paper = getItem(position);
        if (paper == null) {
            return convertView;
        }
        holder.code.setText(paper.getCourseCode());
        holder.name.setText(paper.getCourseName());
        holder.meta.setText(getContext().getString(R.string.paper_meta_format,
                paper.getDepartmentCode(), paper.getAcademicYear(),
                paper.getSemester(), paper.getExamYear()));
        holder.star.setImageResource(favouriteIds.contains(paper.getDriveFileId())
                ? R.drawable.ic_star_filled
                : R.drawable.ic_star_outline);
        return convertView;
    }

    /** Cached row views so scrolling stays smooth. */
    private static final class ViewHolder {
        final TextView code;
        final TextView name;
        final TextView meta;
        final ImageView star;

        ViewHolder(View row) {
            code = row.findViewById(R.id.tvPaperCode);
            name = row.findViewById(R.id.tvPaperName);
            meta = row.findViewById(R.id.tvPaperMeta);
            star = row.findViewById(R.id.ivFavouriteStar);
        }
    }
}
