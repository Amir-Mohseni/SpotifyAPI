import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.Configuration;
import io.swagger.client.api.AuthApi;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.api.PremiumUsersApi;
import io.swagger.client.api.UsersApi;
import io.swagger.client.auth.ApiKeyAuth;
import io.swagger.client.auth.OAuth;
import io.swagger.client.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static final String MY_API_KEY = "e5af7a49bdc5884cf0bba14ad5822809c6fbf019";
    public static ApiClient defaultClient;
    public static DefaultApi defaultApi;
    public static AuthApi authApi;
    public static UsersApi usersApi;
    public static PremiumUsersApi premiumUsersApi;
    public static HashMap<String, Playlists> friendsMap = new HashMap<>();
    public static User currentUser;
    public static long start;
    public static final String PASSWORD_PATTERN =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,20}$";

    public static boolean isValidPassword(String password) {
        return password.matches(PASSWORD_PATTERN);
    }

    public static long getStart() {
        return start;
    }

    public static void authAPIKey() {
        defaultClient = Configuration.getDefaultApiClient();
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey(MY_API_KEY);
        defaultApi = new DefaultApi();
        authApi = new AuthApi(defaultClient);
        usersApi = new UsersApi(defaultClient);
        premiumUsersApi = new PremiumUsersApi(defaultClient);
    }

    public static void loginProcess() {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter username");
        String username = input.next();
        System.out.println("Enter password");
        String password = input.next();

        if(!isValidPassword(password)) {
            System.out.println("Password must be at least 8 characters, contain at least one number, one uppercase letter, one lowercase letter, and no spaces.");
            loginProcess();
            return;
        }

        String token = "";
        try {
            AuthLoginBody authLoginBody = new AuthLoginBody();
            authLoginBody.setUsername(username);
            authLoginBody.setPassword(password);
            token = (authApi.login(authLoginBody).getToken());
            currentUser = new User(username, password);
            System.out.println("You logged in successfully");
            currentUser.token = token;
            defaultClient.setAccessToken(currentUser.token);
            OAuth bearerAuth = (OAuth) defaultClient.getAuthentication("bearerAuth");
            bearerAuth.setAccessToken(currentUser.token);
            clearConsole();
            preProcess();
            userMenuProcess();
        } catch (ApiException apiException) {
            String errorResponse = apiException.getResponseBody();
            if (errorResponse.contains("invalid username or password"))
                System.out.println("Invalid Username or Password");
            else
                System.out.println("No Username/Password provided");
            loginProcess();
        }
    }

    public static void signupProcess() {
        Scanner input = new Scanner(System.in);
        System.out.println("Please enter a Username");
        String username = input.next();
        System.out.println("Please enter a Password");
        String password = input.next();

        if(!isValidPassword(password)) {
            System.out.println("Password must be at least 8 characters, contain at least one number, one uppercase letter, one lowercase letter, and no spaces.");
            signupProcess();
            return;
        }

        String token = "";
        try {
            AuthSignupBody authSignupBody = new AuthSignupBody();
            authSignupBody.setUsername(username);
            authSignupBody.setPassword(password);
            token = (authApi.signUp(authSignupBody).getToken());
            currentUser = new User(username, password);
            System.out.println("You signed up successfully");
            currentUser.token = token;
            defaultClient.setAccessToken(currentUser.token);
            OAuth bearerAuth = (OAuth) defaultClient.getAuthentication("bearerAuth");
            bearerAuth.setAccessToken(currentUser.token);
            clearConsole();
            preProcess();
            userMenuProcess();
        } catch (ApiException apiException) {
            String errorResponse = apiException.getResponseBody();
            if (errorResponse.contains("username already taken"))
                System.out.println("Username already exists");
            else
                System.out.println("No Username/Password provided");
            signupProcess();
        }
    }

    public static void getProfileInfo() {
        long currentTime = System.currentTimeMillis() / 1000;
        if (currentTime - getStart() > 20) {
            try {
                InlineResponse2003 usersApiProfileInfo = usersApi.getProfileInfo();
                String username = usersApiProfileInfo.getUsername();
                String premiumUntil = usersApiProfileInfo.getPremiumUntil();
                System.out.println("Username: " + username);
                System.out.println("Premium Until: " + premiumUntil);
                currentUser.premiumUntil = premiumUntil;
                start = System.currentTimeMillis() / 1000;
            } catch (ApiException apiException) {
                System.out.println(apiException.getResponseBody());
            }
        } else {
            System.out.println("Username: " + currentUser.username);
            System.out.println("Premium Until: " + currentUser.premiumUntil);
        }
    }

    public static void getTracksInfo() {
        long currentTime = System.currentTimeMillis() / 1000;
        if (currentTime - getStart() > 20) {
            try {
                Tracks tracks = usersApi.getTracksInfo();
                currentUser.tracks = tracks;
                for (Track track : tracks) {
                    if(track.isIsPremium() && currentUser.isPremium)
                        System.out.println("Track: " + track.getName() + " - " + "Artist: " + track.getArtist() + " - " + "ID: " + track.getId());
                    else if(!track.isIsPremium())
                        System.out.println("Track: " + track.getName() + " - " + "Artist: " + track.getArtist() + " - " + "ID: " + track.getId());
                }
                start = System.currentTimeMillis() / 1000;
            } catch (ApiException apiException) {
                System.out.println(apiException.getResponseBody());
            }
        } else {
            Tracks tracks = currentUser.tracks;
            for (Track track : tracks) {
                if(track.isIsPremium() && currentUser.isPremium)
                    System.out.println("Track: " + track.getName() + " - " + "Artist: " + track.getArtist() + " - " + "ID: " + track.getId());
                else if(!track.isIsPremium())
                    System.out.println("Track: " + track.getName() + " - " + "Artist: " + track.getArtist() + " - " + "ID: " + track.getId());
            }
        }
        userMenuProcess();
    }

    public static void getPlaylistInfo() {
        long currentTime = System.currentTimeMillis() / 1000;
        if(currentTime - getStart() > 20) {
            try {
                Playlists playlists = usersApi.getPlaylistsInfo();
                currentUser.playlists = playlists;
                for (Playlist playlist : playlists) {
                    System.out.println("Name: " + playlist.getName() + " - " + "ID: " + playlist.getId());
                    System.out.println("Tracks: ");
                    for (Track track : playlist.getTracks()) {
                        System.out.println("-- Track: " + track.getName() + " - " + "Artist: " + track.getArtist() + " - " + "ID: " + track.getId());
                    }
                    System.out.println("---------------------");
                }
                start = System.currentTimeMillis() / 1000;
            } catch (ApiException apiException) {
                System.out.println(apiException.getResponseBody());
            }
        }
        else {
            Playlists playlists = currentUser.playlists;
            for (Playlist playlist : playlists) {
                System.out.println("Name: " + playlist.getName() + " - " + "ID: " + playlist.getId());
                System.out.println("Tracks: ");
                for (Track track : playlist.getTracks()) {
                    System.out.println("-- Track: " + track.getName() + " - " + "Artist: " + track.getArtist() + " - " + "ID: " + track.getId());
                }
                System.out.println("---------------------");
            }
        }
        userMenuProcess();
    }

    public static void createPlaylistProcess() {
        Scanner input = new Scanner(System.in);
        System.out.println("Please enter a playlist name");
        String playlistName = input.nextLine();
        PlaylistsBody playlistsBody = new PlaylistsBody();
        playlistsBody.setName(playlistName);
        try {
            Integer id = usersApi.createPlaylist(playlistsBody).getId();
            System.out.println("Playlist created successfully with ID: " + id);
            Playlist playlist = new Playlist();
            playlist.setId(id.toString());
            playlist.setName(playlistName);
            currentUser.addPlaylist(playlist);
        } catch (ApiException apiException) {
            String response = apiException.getResponseBody();
            if(response.contains("no name provided"))
                System.out.println("No name provided");
            else
                System.out.println("Playlist already exists");
        }
        userMenuProcess();
    }

    public static void deletePlaylistProcess() {
        Scanner input = new Scanner(System.in);
        System.out.println("Please enter a playlist ID");
        int playlistId;
        try {
            playlistId = input.nextInt();
        }catch (Exception e) {
            System.out.println("Invalid ID");
            userMenuProcess();
            return;
        }
        try {
            usersApi.deletePlaylist(playlistId);
            System.out.println("Playlist deleted successfully");
            for (Playlist p : currentUser.playlists) {
                if(p.getId().equals(playlistId)) {
                    currentUser.deletePlaylist(p);
                    break;
                }
            }
        } catch (ApiException apiException) {
            String response = apiException.getResponseBody();
            if(response.contains("no playlist_id provided"))
                System.out.println("No playlist found with this ID");
            else
                System.out.println("Playlist was not found");
        }
        userMenuProcess();
    }

    public static void addTrackProcess() {
        Scanner input = new Scanner(System.in);
        System.out.println("Please enter a playlist ID");
        int playlistID = input.nextInt();
        System.out.println("Please enter a track ID");
        String trackID = input.next();
        try {
            usersApi.addTrackToPlaylist(playlistID, trackID);
            System.out.println("Track successfully added to playlist");
            Playlist playlist = currentUser.getPlaylist(String.valueOf(playlistID));
            Track track = currentUser.getTrack(trackID);
            List<Track> tracks = playlist.getTracks();
            tracks.add(track);
            playlist.setTracks(tracks);
        } catch (ApiException apiException) {
            String response = apiException.getResponseBody();
            if(response.contains("no playlist_id provided"))
                System.out.println("No playlist found with this ID");
            else if(response.contains("track not found"))
                System.out.println("No track found with this ID");
            else
                System.out.println("Track already exists in playlist");
        }
    }

    public static void removeTrackProcess() {
        Scanner input = new Scanner(System.in);
        System.out.println("Please enter a playlist ID");
        int playlistID = input.nextInt();
        System.out.println("Please enter a track ID");
        String trackID = input.next();
        try {
            usersApi.removeTrackFromPlaylist(playlistID, trackID);
            System.out.println("Track successfully removed from playlist");
            Playlist playlist = currentUser.getPlaylist(String.valueOf(playlistID));
            Track track = currentUser.getTrack(trackID);
            List<Track> tracks = playlist.getTracks();
            if(tracks.contains(track))
                tracks.remove(track);
            playlist.setTracks(tracks);
        } catch (ApiException apiException) {
            String response = apiException.getResponseBody();
            if(response.contains("no playlist_id provided"))
                System.out.println("No playlist found with this ID");
            else
                System.out.println("Track not found in playlist");
        }
    }

    public static void upgradeToPremium() {
        try {
            InlineResponse2005 result = usersApi.upgradeToPremium();
            System.out.println("Successfully upgraded to premium");
            System.out.println("Premium until: " + result.getPremiumUntil());
            currentUser.isPremium = true;
            currentUser.premiumUntil = result.getPremiumUntil();
        }catch (ApiException apiException) {
            System.out.println("Error upgrading to premium\nPlease try again");
        }
    }

    public static void upgradeToPremiumTest() {
        try {
            InlineResponse2005 result = usersApi.upgradeToPremiumTest();
            System.out.println("Successfully upgraded to premium");
            System.out.println("Premium until: " + result.getPremiumUntil());
            currentUser.isPremium = true;
            currentUser.premiumUntil = result.getPremiumUntil();
        } catch (ApiException apiException) {
            System.out.println("Error upgrading to premium\nPlease try again");
        }
    }

    //Premium feature from here

    public static void getFriendsInfo() {
        try {
            List <String> friendsList = premiumUsersApi.getFriends();
            currentUser.friendsList = friendsList;
            System.out.println("Friends: ");
            for (String friend : friendsList) {
                System.out.println("Friend's username: " + friend);
            }
        } catch (ApiException apiException) {
            System.out.println("Error getting friends info\nPlease try again");
        }
    }

    public static void getFriendRequests() {
        try {
            List <String> friendRequests = premiumUsersApi.getFriendRequests();
            currentUser.friendRequests = friendRequests;
            System.out.println("Friend requests: ");
            for (String friendRequest : friendRequests) {
                System.out.println("Requesters username: " + friendRequest);
            }
        } catch (ApiException apiException) {
            System.out.println("Error getting friend requests\nPlease try again");
        }
    }

    public static void addFriendProcess() {
        Scanner input = new Scanner(System.in);
        System.out.println("Please enter a username to add as a friend");
        String username = input.next();
        try {
            InlineResponse2006 response2006 = premiumUsersApi.addFriend(username);
            String message = response2006.getMessage();
            if(message.contains("already")) {
                System.out.println("User is already your friend");
            }
            else if(message.contains("sent")) {
                System.out.println("Friend request sent");
            }
            else if(message.contains("now")) {
                System.out.println("Friend successfully added");
                if(currentUser.friendRequests.contains(username))
                    currentUser.friendRequests.remove(username);
                currentUser.friendsList.add(username);
            }
            else {
                System.out.println("You have already sent a friend request to this user");
            }
        } catch (ApiException apiException) {
            System.out.println("Invalid username");
        }
        userMenuProcess();
    }

    public static void getFriendPlaylist() {
        Scanner input = new Scanner(System.in);
        System.out.println("Please enter a username");
        String username = input.next();
        try {
            Playlists friendsPlaylists = premiumUsersApi.getFriendPlaylists(username);
            if(friendsMap.containsKey(username))
                friendsMap.remove(username);
            friendsMap.put(username, friendsPlaylists);
            System.out.println("Friends' playlists: ");
            for (Playlist friendPlaylist : friendsPlaylists) {
                System.out.println("Friend's playlist name: " + friendPlaylist.getName() + " with ID: " + friendPlaylist.getId());
                for (Track track : friendPlaylist.getTracks()) {
                    System.out.println("Track name: " + track.getName() + " with ID: " + track.getId() + " and artist: " + track.getArtist());
                }
                System.out.println("---------------------------------");
            }
        } catch (ApiException apiException) {
            String response = apiException.getResponseBody();
            if(response.contains("invalid friend_username"))
                System.out.println("Invalid username");
            else
                System.out.println("Friend not found");
        }
        userMenuProcess();
    }

    //Premium feature to here

    public static void signOut() {
        currentUser = null;
        premiumUsersApi = null;
        friendsMap.clear();
        authAPIKey();
        mainMenu();
    }

    public static void userMenuProcess() {
        Scanner input = new Scanner(System.in);
        System.out.println("1-Profile\n2-Tracks\n3-Get playlist\n4-Create a playlist\n" +
                "5-Delete a playlist\n6-Add track to a playlist\n7-Remove track from a playlist\n" +
                "8-Upgrade to premium\n9-Upgrade to premium test\n" +
                "10-Get friends info\n11-Get friend requests\n12-Get friend's playlists\n" +
                "13-Add friend\n14-Sign out\n15-Exit");
        int choice = input.nextInt();
        if (choice == 1) {
            clearConsole();
            getProfileInfo();
        } else if (choice == 2) {
            clearConsole();
            getTracksInfo();
        } else if(choice == 3) {
            clearConsole();
            getPlaylistInfo();
        } else if(choice == 4) {
            clearConsole();
            createPlaylistProcess();
        } else if(choice == 5) {
            clearConsole();
            deletePlaylistProcess();
        }else if(choice == 6) {
            clearConsole();
            addTrackProcess();
        } else if(choice == 7) {
            clearConsole();
            removeTrackProcess();
        } else if(choice == 8) {
            clearConsole();
            upgradeToPremium();
        } else if(choice == 9) {
            clearConsole();
            upgradeToPremiumTest();
        } else if(choice == 10) {
            clearConsole();
            getFriendsInfo();
        } else if(choice == 11) {
            clearConsole();
            getFriendRequests();
        } else if(choice == 12) {
            clearConsole();
            getFriendPlaylist();
        } else if(choice == 13) {
            clearConsole();
            addFriendProcess();
        } else if(choice == 14) {
            clearConsole();
            signOut();
        } else if(choice == 15) {
            clearConsole();
            System.out.println("Goodbye");
            System.exit(0);
        } else {
            clearConsole();
            System.out.println("Invalid choice");
        }
        userMenuProcess();
    }

    public static void mainMenu() {
        clearConsole();
        Scanner input = new Scanner(System.in);
        System.out.println("Welcome\n1-Login\n2-Signup\n3-Exit");

        int choice = input.nextInt();
        if (choice == 1)
            loginProcess();
        else if (choice == 2)
            signupProcess();
        else if (choice == 3)
            System.exit(0);
        else
            System.out.println("Invalid choice");
    }

    public static void preProcess() {
        start = System.currentTimeMillis() / 1000;
        start -= 30;
        getProfileInfo();
        start -= 30;
        getPlaylistInfo();
        start -= 30;
        getFriendPlaylist();
        start -= 30;
        getTracksInfo();
        start -= 30;
        getFriendsInfo();
        start -= 30;
        getFriendRequests();
    }

    public static void main(String[] args) {
        authAPIKey();
        mainMenu();
    }

    public static void clearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
