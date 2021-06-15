package org.inventivetalent.pluginannotations.config;

import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.pluginannotations.utils.DataConfigFile;

/**
 * Created by shell on 2018/1/23.
 * <p>
 * Github: https://github.com/shellljx
 */
public class ConfigAnnotations {

    /**
     * load config
     *
     * @param plugin
     * @param classToLoad
     * @param <T>
     * @return
     */
    public <T> T loadValues(JavaPlugin plugin, Class<T> classToLoad) {
        return loadValues(plugin, null, classToLoad);
    }

    /**
     * load config
     *
     * @param plugin
     * @param configFilePath
     * @param classToLoad
     * @param <T>
     * @return
     */
    public <T> T loadValues(JavaPlugin plugin, String configFilePath, Class<T> classToLoad) {
        AnnotationInterpreter interpreter = new BeanInterpreter();
        return interpreter.decodeFromYml(initConfig(plugin, configFilePath, classToLoad).getConfig(), classToLoad);
    }

    /**
     * save object to config
     *
     * @param plugin
     * @param targetToSave
     */
    public void saveValues(JavaPlugin plugin, Object targetToSave) {
        saveValues(plugin, null, targetToSave);
    }

    /**
     * save object to config
     *
     * @param plugin
     * @param configFilePath
     * @param targetTosave
     */
    public void saveValues(JavaPlugin plugin, String configFilePath, Object targetTosave) {
        AnnotationInterpreter interpreter = new BeanInterpreter();
        DataConfigFile configuration = initConfig(plugin,configFilePath,targetTosave.getClass());
        interpreter.encodeToYml(configuration.getConfig(),targetTosave);
        configuration.saveConfig();
    }

    private <T> DataConfigFile initConfig(JavaPlugin plugin, String configFilePath, Class<T> classz) {
        String configFileName;
        if (configFilePath != null) {
            configFileName = configFilePath;
        } else {
            ConfigBean configBean = classz.getAnnotation(ConfigBean.class);
            configFileName = configBean.file();
        }
        return new DataConfigFile(plugin, configFileName);
    }
}
