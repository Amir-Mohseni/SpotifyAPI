import io.swagger.client.model.Playlist;
import io.swagger.client.model.Playlists;
import io.swagger.client.model.Track;
import io.swagger.client.model.Tracks;

import java.util.ArrayList;
import java.util.List;

public class User {
    public String username;
    public String password;
    public Playlists playlists = new Playlists();
    public Tracks tracks = new Tracks();
    public String token;
    public List<String> friendsList = new ArrayList<>();
    public List<String> friendRequests = new ArrayList<>();
    public String premiumUntil = "null";
    public boolean isPremium = false;




    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void addPlaylist(Playlist playlist) {
        this.playlists.add(playlist);
    }

    public void deletePlaylist(Playlist playlist) {
        this.playlists.remove(playlist);
    }

    public Playlist getPlaylist(String id) {
        for (Playlist playlist : playlists) {
            if(playlist.getId().equals(id))
                return playlist;
        }
        return null;
    }

    public void addTrack(Track track) {
        this.tracks.add(track);
    }

    public void deleteTrack(Track track) {
        this.tracks.remove(track);
    }

    public Track getTrack(String id) {
        for (Track track : tracks) {
            if(track.getId().equals(id))
                return track;
        }
        return null;
    }
}
