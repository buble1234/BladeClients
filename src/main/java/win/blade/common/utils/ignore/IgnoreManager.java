package win.blade.common.utils.ignore;

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

@SuppressWarnings("all")
public class IgnoreManager {
    public static IgnoreManager instance;

    public List<Ignore> ignoreList = new ArrayList<>();
    public Path directory;
    public Gson gson;

    public IgnoreManager(){
        instance = this;
        gson = new GsonBuilder().setPrettyPrinting().create();
        directory = Paths.get(MinecraftInstance.mc.runDirectory.getPath(), "blade", "ignore");
        try {
            Files.createDirectories(directory);
        } catch (Exception e){
            e.printStackTrace();
        }

        ignoreList.addAll(loadIgnored());
    }

    public Result<Boolean, String> add(String name){
        Ignore ignore;
        if(!hasIgnored(name)){
            ignore = new Ignore(name, System.currentTimeMillis());
        } else {
            return Result.failure("Игрок: %s уже в списке игнорируемых".formatted(name));
        }

        ignoreList.add(ignore);

        var result = save();

        if(result.isFailure() || result.value() == false){
            return Result.failure(result.error());
        }

        return Result.success(true);
    }

    public Result<Boolean, String> save(){
        return _save(ignoreList);
    }

    public Result<Boolean, String> _save(List<Ignore> ignores){
        if(ignoreList.isEmpty()){
            return Result.failure("Список игнорируемых пуст, сохранение отменено!");
        }

        JsonObject root = new JsonObject();
        JsonObject ignoreObjects = new JsonObject();

        for(var ignore : ignores){
            JsonObject ignoreObject = new JsonObject();

            ignoreObject.addProperty("creationTime", ignore.creationTime());

            ignoreObjects.add(ignore.name(), ignoreObject);
        }

        root.add("ignored", ignoreObjects);

        File ignoreFile = directory.resolve("ignore.json").toFile();
        try (FileWriter writer = new FileWriter(ignoreFile)){
            gson.toJson(root, writer);
        } catch (Exception e){
            e.printStackTrace();

            return Result.failure("Не удалось сохранить игнорируемых!");
        }

        return Result.success(true);
    }
    
    public List<Ignore> loadIgnored() {
        File file = directory.resolve("ignore.json").toFile();

        List<Ignore> loaded = new ArrayList<>();

        if(!file.exists()){
            return loaded;
        }

        try (FileReader reader = new FileReader(file)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            if (root.has("ignored")) {
                JsonObject ignoreObjects = root.getAsJsonObject("ignored");

                for (Map.Entry<String, JsonElement> ignore : ignoreObjects.entrySet()) {
                    String ignoredName = ignore.getKey();

                    JsonObject ignoreObject = ignore.getValue().getAsJsonObject();

                    long creationTime = ignoreObject.get("creationTime").getAsLong();

                    loaded.add(new Ignore(ignoredName, creationTime));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return loaded;
    }

    public Result<Boolean, String> removeIgnored(String name){
        var ignore = findIgnored(name);

        if(ignore == null) return Result.failure("Игрока с именем %s нет в списке игнорируемых".formatted(name));

        boolean remove = ignoreList.remove(ignore);
        Result<Boolean, String> result;

        if(!remove){
            return Result.failure("Не удалось удалить игрока с именем %s из игнора".formatted(name));
        } else {
            result = save();
        }

        return result.isSuccess() ? Result.success(true) : Result.failure(result.error());
    }

    public boolean hasIgnored(String name){
        return findIgnored(name) != null;
    }

    public Ignore findIgnored(String name){
        return ignoreList.stream()
                .filter(ignore -> ignore.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public boolean shouldHideMessage(String message) {
        if (message == null || message.isEmpty()) return false;

        for (Ignore ignore : ignoreList) {
            if (message.toLowerCase().contains(ignore.name().toLowerCase())) {
                return true;
            }
        }

        return false;
    }
}