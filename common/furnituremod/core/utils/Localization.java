package furnituremod.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Properties;

import furnituremod.core.proxy.CoreProxy;

public class Localization {


	private static class modInfo {

		final String modName, defaultLanguage;

		public modInfo(String modName, String defaultLanguage) {
			this.modName = modName;
			this.defaultLanguage = defaultLanguage;
		}
	}

	private static String loadedLanguage = getCurrentLanguage();
	private static Properties defaultMappings = new Properties();
	private static Properties mappings = new Properties();
	private static LinkedList<modInfo> mods = new LinkedList<modInfo>();


	public static void addLocalization(String path, String defaultLanguage) {
		mods.add(new modInfo(path, defaultLanguage));
		load(path, defaultLanguage);
	}


	public static synchronized String get(String key) {
        if (getCurrentLanguage() == null) {
            return key;
        }
		if (!getCurrentLanguage().equals(loadedLanguage)) {
			defaultMappings.clear();
			mappings.clear();
			for (modInfo mInfo : mods) {
				load(mInfo.modName, mInfo.defaultLanguage);
			}
			loadedLanguage = getCurrentLanguage();
		}

		return mappings.getProperty(key, defaultMappings.getProperty(key, key));
	}
	
	public static synchronized boolean hasKey(String key) {
        if (getCurrentLanguage() == null) {
            return false;
        }
		if (!getCurrentLanguage().equals(loadedLanguage)) {
			defaultMappings.clear();
			mappings.clear();
			for (modInfo mInfo : mods) {
				load(mInfo.modName, mInfo.defaultLanguage);
			}
			loadedLanguage = getCurrentLanguage();
		}

		return mappings.getProperty(key, defaultMappings.getProperty(key)) != null;
	}

	private static void load(String path, String default_language) {
		InputStream langStream = null;
		Properties modMappings = new Properties();

		try {
			// Load the default language mappings
			langStream = Localization.class.getResourceAsStream(path + default_language + ".properties");
			modMappings.load(langStream);
			defaultMappings.putAll(modMappings);{
			langStream.close();
		
		}
			// Try to load the current language mappings.
			// If the file doesn't exist use the default mappings.
			langStream = Localization.class.getResourceAsStream(path + getCurrentLanguage() + ".properties");
			if (langStream != null) {
				modMappings.clear();
				modMappings.load(langStream);
			}

			// If the selected language inherits mappings from another language
			// we use those first and then we overwrite the common ones with
			// those in the selected language
			if (modMappings.containsKey("language.parent")) {
				langStream = Localization.class.getResourceAsStream(path + modMappings.get("language.parent") + ".properties");

				if (langStream != null) {
					Properties parentModMappings = new Properties();

					parentModMappings.load(langStream);
					mappings.putAll(parentModMappings);
				}
			}

			mappings.putAll(modMappings);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (langStream != null) {
					langStream.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private static String getCurrentLanguage() {
		return CoreProxy.proxy.getCurrentLanguage();
	}
}

