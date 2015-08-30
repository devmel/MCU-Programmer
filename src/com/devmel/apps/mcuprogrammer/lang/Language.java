package com.devmel.apps.mcuprogrammer.lang;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Language {
	private static final String DEFAULT_PACKAGE = "com.devmel.apps.mcuprogrammer.lang.";
	private static final String DEFAULT_NAME = DEFAULT_PACKAGE+"en";

	private static ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(DEFAULT_NAME);

	private Language() {}

	
	public static boolean setLanguage(String lang) {
		boolean ret = false;
		try {
			ResourceBundle res = ResourceBundle.getBundle(DEFAULT_PACKAGE+lang);
			if(res!=null){
				String code = res.getString("Code");
				if(code!=null && code.equals(lang)){
					RESOURCE_BUNDLE = res;
					ret = true;
				}
			}
		} catch (MissingResourceException e) {
		}
		return ret;
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
