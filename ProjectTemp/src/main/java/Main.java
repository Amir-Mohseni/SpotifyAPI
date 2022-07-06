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
    public static boolean prep = false;
    public static final String PASSWORD_PATTERN =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,20}$";
    public static final String purple = "\033[0;35m";
    public static final String red = "\033[0;31m";
    public static final String blue = "\033[0;34m";
    public static final String greenbold = "\033[1;32m";
    public static final String white = "\u001B[0m";

    public static boolean isValidPassword(String password) {
        return password.matches(PASSWORD_PATTERN);
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
        System.out.println(greenbold + "Enter username");
        String username = input.next();
        System.out.println(greenbold + "Enter password" + white);
        String password = input.next();

        if(!isValidPassword(password)) {
            System.out.println(red + "Password must be at least 8 characters, contain at least one number, one uppercase letter, one lowercase letter, and no spaces." + white);
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
            System.out.println(greenbold + "You logged in successfully" + white);
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
                System.out.println(red + "Invalid Username or Password" + white);
            else
                System.out.println(red + "No Username/Password provided" + white);
            loginProcess();
        }
    }

    public static void signupProcess() {
        Scanner input = new Scanner(System.in);
        System.out.println(greenbold + "Please enter a Username" + white);
        String username = input.next();
        System.out.println(greenbold + "Please enter a Password" + white);
        String password = input.next();

        if(!isValidPassword(password)) {
            System.out.println(red + "Password must be at least 8 characters, contain at least one number, one uppercase letter, one lowercase letter, and no spaces." + white);
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
            System.out.println(greenbold + "You signed up successfully" + white);
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
                System.out.println(red + "Username already exists" + white);
            else
                System.out.println(red + "No Username/Password provided" + white);
            signupProcess();
        }
    }

    public static void getProfileInfo() {
        long currentTime = System.currentTimeMillis() / 1000;
        if (prep || currentTime - start > 20) {
            try {
                InlineResponse2003 usersApiProfileInfo = usersApi.getProfileInfo();
                String username = usersApiProfileInfo.getUsername();
                String premiumUntil = usersApiProfileInfo.getPremiumUntil();
                if(premiumUntil == null)
                    premiumUntil = "null";
                System.out.println(blue + "Username: " + username + white);
                currentUser.premiumUntil = premiumUntil;
                if(premiumUntil.contains("null")) {
                    System.out.println(red + "You are not premium" + white);
                    currentUser.isPremium = false;
                } else {
                    System.out.println(greenbold + "You are premium until " + premiumUntil + white);
                    currentUser.isPremium = true;
                }
                start = System.currentTimeMillis() / 1000;
            } catch (ApiException apiException) {
                System.out.println(red + apiException.getResponseBody() + white);
            }
        } else {
            System.out.println(blue + "Username: " + currentUser.username + white);
            if(currentUser.premiumUntil.equals("null")) {
                System.out.println(red + "You are not premium" + white);
            } else {
                System.out.println(greenbold + "You are premium until " + currentUser.premiumUntil + white);
            }
        }
    }

    public static void getTracksInfo() {
        long currentTime = System.currentTimeMillis() / 1000;
        if (prep || currentTime - start > 20) {
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
                System.out.println(red + apiException.getResponseBody() + white);
            }
        } else {
            System.out.println("Moshkel az taraf mane");
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
        if(prep || currentTime - start > 20) {
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
                System.out.println(red + apiException.getResponseBody() + white);
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
        System.out.println(greenbold + "Please enter a playlist name" + white);
        String playlistName = input.nextLine();
        PlaylistsBody playlistsBody = new PlaylistsBody();
        playlistsBody.setName(playlistName);
        try {
            Integer id = usersApi.createPlaylist(playlistsBody).getId();
            System.out.println(greenbold + "Playlist created successfully with ID: " + id + white);
            Playlist playlist = new Playlist();
            playlist.setId(id.toString());
            playlist.setName(playlistName);
            currentUser.addPlaylist(playlist);
        } catch (ApiException apiException) {
            String response = apiException.getResponseBody();
            if(response.contains("no name provided"))
                System.out.println(red + "No name provided" + white);
            else
                System.out.println(red + "Playlist already exists" + white);
        }
        userMenuProcess();
    }

    public static void deletePlaylistProcess() {
        Scanner input = new Scanner(System.in);
        System.out.println(greenbold + "Please enter a playlist ID" + white);
        int playlistId;
        try {
            playlistId = input.nextInt();
        }catch (Exception e) {
            System.out.println(red + "Invalid ID" + white);
            userMenuProcess();
            return;
        }
        try {
            usersApi.deletePlaylist(playlistId);
            System.out.println(greenbold + "Playlist deleted successfully" + white);
            for (Playlist p : currentUser.playlists) {
                if(p.getId().equals(playlistId)) {
                    currentUser.deletePlaylist(p);
                    break;
                }
            }
        } catch (ApiException apiException) {
            String response = apiException.getResponseBody();
            if(response.contains("no playlist_id provided"))
                System.out.println(red + "No playlist found with this ID" + white);
            else
                System.out.println(red + "Playlist was not found" + white);
        }
        userMenuProcess();
    }

    public static void addTrackProcess() {
        Scanner input = new Scanner(System.in);
        System.out.println(greenbold + "Please enter a playlist ID" + white);
        int playlistID = input.nextInt();
        System.out.println(greenbold + "Please enter a track ID" + white);
        String trackID = input.next();
        try {
            usersApi.addTrackToPlaylist(playlistID, trackID);
            System.out.println(greenbold + "Track successfully added to playlist" + white);
            Playlist playlist = currentUser.getPlaylist(String.valueOf(playlistID));
            Track track = currentUser.getTrack(trackID);
            List<Track> tracks = playlist.getTracks();
            tracks.add(track);
            playlist.setTracks(tracks);
        } catch (ApiException apiException) {
            String response = apiException.getResponseBody();
            if(response.contains("no playlist_id provided"))
                System.out.println(red + "No playlist found with this ID" + white);
            else if(response.contains("track not found"))
                System.out.println(red + "No track found with this ID" + white);
            else
                System.out.println(red + "Track already exists in playlist" + white);
        }
    }

    public static void removeTrackProcess() {
        Scanner input = new Scanner(System.in);
        System.out.println(greenbold + "Please enter a playlist ID" + white);
        int playlistID = input.nextInt();
        System.out.println(greenbold + "Please enter a track ID" + white);
        String trackID = input.next();
        try {
            usersApi.removeTrackFromPlaylist(playlistID, trackID);
            System.out.println(greenbold + "Track successfully removed from playlist" + white);
            Playlist playlist = currentUser.getPlaylist(String.valueOf(playlistID));
            Track track = currentUser.getTrack(trackID);
            List<Track> tracks = playlist.getTracks();
            if(tracks.contains(track))
                tracks.remove(track);
            playlist.setTracks(tracks);
        } catch (ApiException apiException) {
            String response = apiException.getResponseBody();
            if(response.contains("no playlist_id provided"))
                System.out.println(red + "No playlist found with this ID" + white);
            else
                System.out.println(red + "Track not found in playlist" + white);
        }
    }

    public static void upgradeToPremium() {
        try {
            InlineResponse2005 result = usersApi.upgradeToPremium();
            System.out.println(greenbold + "Successfully upgraded to premium" + white);
            System.out.println("Premium until: " + result.getPremiumUntil());
            currentUser.isPremium = true;
            currentUser.premiumUntil = result.getPremiumUntil();
        }catch (ApiException apiException) {
            System.out.println(red + "Error upgrading to premium\nPlease try again" + white);
        }
    }

    public static void upgradeToPremiumTest() {
        try {
            InlineResponse2005 result = usersApi.upgradeToPremiumTest();
            System.out.println(greenbold + "Successfully upgraded to premium" + white);
            System.out.println("Premium until: " + result.getPremiumUntil());
            currentUser.isPremium = true;
            currentUser.premiumUntil = result.getPremiumUntil();
        } catch (ApiException apiException) {
            System.out.println(red + "Error upgrading to premium\nPlease try again" + white);
        }
    }

    //Premium feature from here

    public static void getFriendsInfo() {
        long currentTime = System.currentTimeMillis() / 1000;
        if(prep || currentTime - start > 20) {
            try {
                List<String> friendsList = premiumUsersApi.getFriends();
                currentUser.friendsList = friendsList;
                System.out.println(blue + "Friends: " + white);
                for (String friend : friendsList) {
                    System.out.println("Friend's username: " + friend);
                }
                start = System.currentTimeMillis() / 1000;
            } catch (ApiException apiException) {
                System.out.println(red + "Error getting friends info\nPlease try again" + white);
            }
        } else {
            List<String> friendsList = currentUser.friendsList;
            System.out.println(blue + "Friends: " + white);
            for (String friend : friendsList) {
                System.out.println("Friend's username: " + friend);
            }
        }
    }

    public static void getFriendRequests() {
        long currentTime = System.currentTimeMillis() / 1000;
        if(prep || currentTime - start > 20) {
            try {
                List<String> friendRequests = premiumUsersApi.getFriendRequests();
                currentUser.friendRequests = friendRequests;
                System.out.println(blue + "Friend requests: " + white);
                for (String friendRequest : friendRequests) {
                    System.out.println("Requesters username: " + friendRequest);
                }
                start = System.currentTimeMillis() / 1000;
            } catch (ApiException apiException) {
                System.out.println(red + "Error getting friend requests\nPlease try again" + white);
            }
        } else {
            List<String> friendRequests = currentUser.friendRequests;
            System.out.println(blue + "Friend requests: " + white);
            for (String friendRequest : friendRequests) {
                System.out.println("Requesters username: " + friendRequest);
            }
        }
    }

    public static void addFriendProcess() {
        Scanner input = new Scanner(System.in);
        System.out.println(greenbold + "Please enter a username to add as a friend" + white);
        String username = input.next();
        try {
            InlineResponse2006 response2006 = premiumUsersApi.addFriend(username);
            String message = response2006.getMessage();
            if(message.contains("already")) {
                System.out.println(purple + "User is already your friend" + white);
            }
            else if(message.contains("sent")) {
                System.out.println(greenbold + "Friend request sent" + white);
            }
            else if(message.contains("now")) {
                System.out.println(greenbold + "Friend successfully added" + white);
                if(currentUser.friendRequests.contains(username))
                    currentUser.friendRequests.remove(username);
                currentUser.friendsList.add(username);
            }
            else {
                System.out.println(purple + "You have already sent a friend request to this user" + white);
            }
        } catch (ApiException apiException) {
            System.out.println(red + "Invalid username" + white);
        }
        userMenuProcess();
    }

    public static void getFriendPlaylist() {
        long currentTime = System.currentTimeMillis() / 1000;
        Scanner input = new Scanner(System.in);
        System.out.println(greenbold + "Please enter a username" + white);
        String username = input.next();
        if(prep || currentTime - start > 20) {
            try {
                Playlists friendsPlaylists = premiumUsersApi.getFriendPlaylists(username);
                if (friendsMap.containsKey(username))
                    friendsMap.remove(username);
                friendsMap.put(username, friendsPlaylists);
                System.out.println(blue + "Friends' playlists: " + white);
                for (Playlist friendPlaylist : friendsPlaylists) {
                    System.out.println("Friend's playlist name: " + friendPlaylist.getName() + " with ID: " + friendPlaylist.getId());
                    for (Track track : friendPlaylist.getTracks()) {
                        System.out.println("Track name: " + track.getName() + " with ID: " + track.getId() + " and artist: " + track.getArtist());
                    }
                    System.out.println("---------------------------------");
                }
                start = System.currentTimeMillis() / 1000;
            } catch (ApiException apiException) {
                String response = apiException.getResponseBody();
                if (response.contains("invalid friend_username"))
                    System.out.println(red + "Invalid username" + white);
                else
                    System.out.println(red + "Friend not found" + white);
            }
        } else {
            if(friendsMap.containsKey(username)) {
                Playlists friendsPlaylists = friendsMap.get(username);
                System.out.println(blue + "Friends' playlists: " + white);
                for (Playlist friendPlaylist : friendsPlaylists) {
                    System.out.println("Friend's playlist name: " + friendPlaylist.getName() + " with ID: " + friendPlaylist.getId());
                    for (Track track : friendPlaylist.getTracks()) {
                        System.out.println("Track name: " + track.getName() + " with ID: " + track.getId() + " and artist: " + track.getArtist());
                    }
                    System.out.println("---------------------------------");
                }
            }
            else {
                System.out.println(red + "Friend's playlist not found" + white);
            }
        }
        userMenuProcess();
    }

    //Premium feature to here

    public static void signOut() {
        friendsMap.clear();
        authAPIKey();
        mainMenu();
    }

    public static void checkPremium(int choice) {
        if(!currentUser.isPremium) {
            clearConsole();
            System.out.println(red + "You discovered a premium feature" + white);
            userMenuProcess();
            return;
        }
        else {
            clearConsole();
            switch (choice) {
                case 10:
                    getFriendsInfo();
                    break;
                case 11:
                    getFriendRequests();
                    break;
                case 12:
                    getFriendPlaylist();
                    break;
                case 13:
                    addFriendProcess();
                    break;
                default:
                    userMenuProcess();
                    break;
            }
        }
    }

    public static void userMenuProcess() {
        Scanner input = new Scanner(System.in);
        String cur = (currentUser.premiumUntil.contains("null"))? red : blue;
        System.out.println(blue + "1-Profile\n2-Tracks\n3-Get playlist\n4-Create a playlist\n" +
                "5-Delete a playlist\n6-Add track to a playlist\n7-Remove track from a playlist\n" +
                "8-Upgrade to premium\n9-Upgrade to premium test\n" + cur +
                "10-Get friends info\n11-Get friend requests\n12-Get friend's playlists\n" +
                "13-Add friend\n" + blue + "14-Sign out\n15-Exit" + white);
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
            checkPremium(10);
        } else if(choice == 11) {
            clearConsole();
            checkPremium(11);
        } else if(choice == 12) {
            clearConsole();
            checkPremium(12);
        } else if(choice == 13) {
            clearConsole();
            checkPremium(13);
        } else if(choice == 14) {
            clearConsole();
            signOut();
        } else if(choice == 15) {
            clearConsole();
            System.out.println(purple + "Goodbye" + white);
            System.exit(0);
        } else {
            clearConsole();
            System.out.println(red + "Invalid choice" + white);
        }
        userMenuProcess();
    }

    public static void mainMenu() {
        clearConsole();
        Scanner input = new Scanner(System.in);
        System.out.println(blue + "Welcome\n1-Login\n2-Signup\n3-Exit" + white);

        int choice = input.nextInt();
        if (choice == 1)
            loginProcess();
        else if (choice == 2)
            signupProcess();
        else if (choice == 3)
            System.exit(0);
        else
            System.out.println(red + "Invalid choice" + white);
    }

    public static void preProcess() {
        prep = true;
        getProfileInfo();
        getPlaylistInfo();
        getFriendPlaylist();
        getTracksInfo();
        getFriendsInfo();
        getFriendRequests();
        prep = false;
        start = System.currentTimeMillis() / 1000;
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
