package win.blade.common.gui.impl.screen.account;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorOutputStream;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Автор Ieo117
 * Дата создания: 05.07.2025, в 22:21:14
 */
@SuppressWarnings("all")
public class AccountSaver {
    private static final Gson gson = new Gson();
    private static final Type token = new TypeToken<List<Account>>(){}.getType();
    private static final String path = "blade/";
    private static final String fileName = "accounts.json";
    public static void save(List<Account> accounts){
        File file = new File(path + fileName);
        File path = new File(AccountSaver.path);

        try {
            file.getParentFile().mkdirs();

            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(accounts, writer);
            }

            System.out.println("Saved: " + accounts.size());
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    public static List<Account> loadAccounts(){
        List<Account> accountList = new ArrayList<>();

        File file = new File(path + fileName);

        if(file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                List<Account> loadedList = gson.fromJson(reader, token);
                if(loadedList != null && !loadedList.isEmpty()){
                    accountList = loadedList;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        accountList.removeIf(Objects::isNull);
        return accountList;
    }
}
