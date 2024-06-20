package com.example.dreammusic;

public class Song implements Comparable<Song>{

    private final String name;
    private final String imageUrl;
    private String audioUrl;
    private String songName;

    public Song(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.audioUrl = audioUrl;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    @Override
    public int compareTo(Song otherSong) {
        return this.songName.compareTo(otherSong.getName());
    }
}


