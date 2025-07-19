package win.blade.common.utils.friends;

import com.google.gson.*;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.other.Result;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Автор Ieo117
 * Дата создания: 16.07.2025, в 16:35:02
 */
@SuppressWarnings("all")
public class FriendManager {
    public static FriendManager instance;

    public List<Friend> friendList = new ArrayList<>();
    public Path directory;
    public Gson gson;

    public FriendManager(){
        gson = new GsonBuilder().setPrettyPrinting().create();
        directory = Paths.get(MinecraftInstance.mc.runDirectory.getPath(), "blade", "friends");
        try {
            Files.createDirectories(directory);
        } catch (Exception e){
            e.printStackTrace();
        }


        friendList.addAll(loadFriends());
    }

    public Result<Boolean, String> add(String name){
        Friend friend;
        if(!hasFriend(name)){
            friend = new Friend(name, System.currentTimeMillis());
        }else {
            return Result.failure("Игрок: %s уже в списке друзей".formatted(name));
        }

        friendList.add(friend);

        var result = save();

        if(result.isFailure() || result.value() == false){
            return Result.failure(result.error());
        }

        return Result.success(true);
    }

    public Result<Boolean, String> save(){
        return _save(friendList);
    }

    public Result<Boolean, String> _save(List<Friend> friends){
        if(friendList.isEmpty()){
            return Result.failure("Список друзей пуст, сохранение отменено!");
        }

        JsonObject root = new JsonObject();
        JsonObject friendObjects = new JsonObject();

        for(var friend : friends){
            JsonObject friendObject = new JsonObject();

            friendObject.addProperty("creationTime", friend.creationTime());

            friendObjects.add(friend.name(), friendObject);
        }

        root.add("friends", friendObjects);

        File friendFile = directory.resolve("friend.json").toFile();
        try (FileWriter writer = new FileWriter(friendFile)){
            gson.toJson(root, writer);
        } catch (Exception e){
            e.printStackTrace();

            return Result.failure("Не удалось сохранить друзей!");
        }

        return Result.success(true);
    }

    public List<Friend> loadFriends() {
        File file = directory.resolve("friend.json").toFile();

        List<Friend> loaded = new ArrayList<>();

        try (FileReader reader = new FileReader(file)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            if (root.has("friends")) {
                JsonObject friendObjects = root.getAsJsonObject("friends");

                for (Map.Entry<String, JsonElement> friend : friendObjects.entrySet()) {
                    String friendName = friend.getKey();

                    JsonObject friendObject = friend.getValue().getAsJsonObject();

                    long creationTime = friendObject.get("creationTime").getAsLong();

                    loaded.add(new Friend(friendName, creationTime));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return loaded;
    }


    public Result<Boolean, String> removeFriend(String name){
        var friend = findFriend(name);

        if(friend == null) Result.failure("Друга с именем %s не существует".formatted(name));

        boolean remove =friendList.remove(friend);
        Result<Boolean, String> result;

        if(!remove){
            return Result.failure("Не удалось удалить друга с именем %s".formatted(name));
        } else {
            result = save();
        }

        return result.isSuccess() ? Result.success(true) : Result.failure(result.error());
    }


    public boolean hasFriend(String name){
        return findFriend(name) != null;
    }


    public Friend findFriend(String name){
        return friendList.stream()
                .filter(friend -> friend.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

}
