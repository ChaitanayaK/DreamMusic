package com.example.dreammusic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import java.util.List;

public class SongAdapter extends ArrayAdapter<Song> {

    private final Context context;
    private final int resource;
    public static List<Song> songs = null;
    public SongAdapter(Context context, int resource, List<Song> songs) {
        super(context, resource, songs);
        this.context = context;
        this.resource = resource;
        SongAdapter.songs = songs;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);
        }

        ImageView songImage = convertView.findViewById(R.id.songImage);
        TextView songName = convertView.findViewById(R.id.songName);

        Song song = songs.get(position);

        // Load and resize image using Picasso
        Picasso.get().load(song.getImageUrl()).transform(new ResizeTransformation(23, 23)).into(songImage);

        // Set song name
        songName.setText(song.getName());
        convertView.setTag(position);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag(); // Retrieve the position from the tag
                Song clickedSong = songs.get(position);

                if (CreatorActivity.groupPlay){
                    CreatorActivity.reference = CreatorActivity.database.getReference(GroupPlayActivity.uniqueId);
                    CreatorActivity.reference.child("song").setValue(clickedSong);
                    CreatorActivity.reference.child("play").setValue(false);
                    CreatorActivity.reference.child("timeStamp").setValue(0);
                }

                Intent intent = new Intent(context, MusicPlayerActivity.class);

                MainActivity.staticSongName = song.getName();
                MainActivity.staticImageUrl = song.getImageUrl();
                MainActivity.staticSongPosition = position;

                try {
                    if (MainActivity.mediaPlayer.isPlaying()){
                        MainActivity.mediaPlayer.stop();
                    }
                    MainActivity.mediaPlayer = new MediaPlayer();

                    context.startActivity(intent);
                    ((Activity) context).finish();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        return convertView;
    }

    private static class ResizeTransformation implements Transformation {

        private final int targetWidth;
        private final int targetHeight;

        public ResizeTransformation(int targetWidth, int targetHeight) {
            this.targetWidth = targetWidth;
            this.targetHeight = targetHeight;
        }

        @Override
        public Bitmap transform(Bitmap source) {
            double aspectRatio = (double) source.getHeight() / (double) source.getWidth();
            int newWidth = (int) (targetHeight / aspectRatio);
            int newHeight = (int) (targetWidth * aspectRatio);

            Bitmap resizedBitmap;
            if (newWidth < targetWidth) {
                resizedBitmap = Bitmap.createScaledBitmap(source, targetWidth, newHeight, false);
            } else {
                resizedBitmap = Bitmap.createScaledBitmap(source, newWidth, targetHeight, false);
            }

            if (resizedBitmap != source) {
                source.recycle();
            }

            return resizedBitmap;
        }

        @Override
        public String key() {
            return "resize";
        }
    }
}
