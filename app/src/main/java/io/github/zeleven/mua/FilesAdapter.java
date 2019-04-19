package io.github.zeleven.mua;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.github.zeleven.mua.activity.MainActivity;
import io.github.zeleven.mua.fragment.EditorFragment;
import io.github.zeleven.mua.utils.FileUtils;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.ViewHolder> {
    private List<FileEntity> dataSet;
    private AppCompatActivity context;

    public FilesAdapter(List<FileEntity> entityList) {
        dataSet = (entityList == null) ? new ArrayList<FileEntity>() : entityList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (context == null) {
            context = (MainActivity) parent.getContext();
        }
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.file_list_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final FileEntity entity = dataSet.get(position);
        String fileName = entity.getName();
        holder.fileName.setText(fileName);

        String content = FileUtils.readContentFromFile(new File(entity.getAbsolutePath()), false);
        if (content.length() == 0) {
            holder.fileContent.setVisibility(View.GONE);
        } else {
            content = content.length() > 500 ? content.substring(0, 500) : content;
            holder.fileContent.setText(content);
        }

        holder.fileDate.setText(DateUtils.getRelativeTimeSpanString(entity.getLastModified(),
                System.currentTimeMillis(), DateUtils.FORMAT_ABBREV_ALL));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new EditorFragment();

                Bundle args = new Bundle();
                args.putBoolean(Constants.BUNDLE_KEY_SAVED, true);
                args.putBoolean(Constants.BUNDLE_KEY_FROM_FILE, true);
                args.putString(Constants.BUNDLE_KEY_FILE_NAME,
                        FileUtils.stripExtension(entity.getName()));
                args.putString(Constants.BUNDLE_KEY_FILE_PATH, entity.getAbsolutePath());

                fragment.setArguments(args);
                context.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView fileName, fileContent, fileDate;

        public ViewHolder(View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.file_name);
            fileContent = itemView.findViewById(R.id.file_content);
            fileDate = itemView.findViewById(R.id.file_date);
        }
    }
}
