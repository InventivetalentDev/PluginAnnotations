package org.inventivetalent.pluginannotations.utils;

import com.google.common.base.Charsets;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by shell on 2017/12/2.
 * <p>
 * Github: https://github.com/shellljx
 */
public class DataConfigFile {

    private JavaPlugin plugin;
    private FileConfiguration newDataConfig = null;
    private File dataConfigFile = null;
    private String fileName;

    public DataConfigFile(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        this.dataConfigFile = new File(plugin.getDataFolder(), fileName);
    }

    public void reloadConfig() {
        newDataConfig = YamlConfiguration.loadConfiguration(dataConfigFile);

        InputStream defInputStream = plugin.getResource(fileName);
        if (defInputStream == null) {
            return;
        }

        InputStreamReader streamReader = new InputStreamReader(defInputStream, Charsets.UTF_8);

        newDataConfig.setDefaults(YamlConfiguration.loadConfiguration(streamReader));

        if (defInputStream != null) {
            try {
                defInputStream.close();
            } catch (IOException e) {
                throw new RuntimeException("Faild close DataConfigFile inputStream!");
            }
        }

        if (streamReader != null) {
            try {
                streamReader.close();
            } catch (IOException e) {
                throw new RuntimeException("Faild close DataConfigFile streamReader!");
            }
        }
    }

    public FileConfiguration getConfig() {
        if (newDataConfig == null) {
            this.reloadConfig();
        }
        return newDataConfig;
    }

    public void saveConfig() {
        if (newDataConfig == null || dataConfigFile == null) {
            return;
        }
        try {
            getConfig().save(dataConfigFile);
        } catch (IOException ex) {
            throw new RuntimeException("Could not save config to " + dataConfigFile + ex);
        }
    }

    public void saveDefaultConfig() {
        if (!dataConfigFile.exists()) {
            plugin.saveResource(fileName, false);
        }
    }
}
