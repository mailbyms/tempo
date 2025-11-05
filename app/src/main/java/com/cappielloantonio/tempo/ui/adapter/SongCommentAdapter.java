package com.cappielloantonio.tempo.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cappielloantonio.tempo.databinding.ItemSongCommentBinding;
import com.cappielloantonio.tempo.subsonic.models.SongComment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SongCommentAdapter extends ListAdapter<SongComment, SongCommentAdapter.CommentViewHolder> {

    public SongCommentAdapter(List<SongComment> comments) {
        super(new DiffUtil.ItemCallback<SongComment>() {
            @Override
            public boolean areItemsTheSame(@NonNull SongComment oldItem, @NonNull SongComment newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull SongComment oldItem, @NonNull SongComment newItem) {
                return oldItem.getContent().equals(newItem.getContent()) &&
                       oldItem.getUser().equals(newItem.getUser()) &&
                       oldItem.getTimestamp() == newItem.getTimestamp();
            }
        });
        submitList(comments);
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSongCommentBinding binding = ItemSongCommentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CommentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        SongComment comment = getItem(position);
        holder.bind(comment);
    }

    public void updateComments(List<SongComment> newComments) {
        submitList(newComments);
    }

    public void addComments(List<SongComment> newComments) {
        List<SongComment> currentList = getCurrentList();
        if (currentList != null) {
            // 创建新列表包含现有评论和新评论
            List<SongComment> updatedList = new ArrayList<>(currentList);
            updatedList.addAll(newComments);
            submitList(updatedList);
        } else {
            submitList(newComments);
        }
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        private final ItemSongCommentBinding binding;

        public CommentViewHolder(ItemSongCommentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(SongComment comment) {
            // 设置用户头像
            if (comment.getAvatarUrl() != null && !comment.getAvatarUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(comment.getAvatarUrl())
                        .placeholder(binding.userAvatar.getDrawable())
                        .into(binding.userAvatar);
            }

            // 设置用户名
            binding.userName.setText(comment.getUser() != null ? comment.getUser() : "匿名用户");

            // 设置评论内容
            binding.commentContent.setText(comment.getContent());

            // 设置评论时间
            String timeText = formatTime(comment.getTimestamp());
            binding.commentTime.setText(timeText);

            // 设置点赞数（如果有）
            if (comment.getLikedCount() > 0) {
                binding.likedCount.setText(String.valueOf(comment.getLikedCount()));
                binding.likedCount.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.likedCount.setVisibility(android.view.View.GONE);
            }
        }

        private String formatTime(long timestamp) {
            try {
                Date date = new Date(timestamp);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                return sdf.format(date);
            } catch (Exception e) {
                return "";
            }
        }
    }
}