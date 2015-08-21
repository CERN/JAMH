package cern.enice.jira.amh.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.osgi.service.cm.ConfigurationException;

public class Utils {

	public static Object getMapFromJsonFile(String filename) {
		return getMapFromJsonFile(filename, "utf-8");
	}
	
	public static Object getMapFromJsonFile(String filename, String encoding) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			String jsonFileContent = IOUtils.toString(
					new FileInputStream(filename), encoding);
			return mapper.readValue(jsonFileContent, Object.class);
		} catch (IOException e) {
			System.out.println(e);
		}
		return null;
	}
	
	public static String updateStringProperty(Dictionary<String, ?> properties, 
			String propertyName) throws ConfigurationException {
		String propertyValue = (String) properties.get(propertyName);
		if (propertyValue == null || propertyValue.isEmpty())
			throw new ConfigurationException(propertyName,
					"property is missing or empty");
		return propertyValue;
	}
	
	public static String updateStringProperty(Dictionary<String, ?> properties, 
			String propertyName, String defaultValue) throws ConfigurationException {
		String propertyValue = (String) properties.get(propertyName);
		if (propertyValue == null || propertyValue.isEmpty())
			return defaultValue;
		return propertyValue;
	}
	
	public static List<String> updateConfigurableListField(Dictionary<String, ?> properties, 
			String propertyName) {
		int index = 0;
		List<String> configurableList = new ArrayList<String>();
		while (properties.get(propertyName + index) != null) {
			String propertyValue = (String) properties.get(propertyName + index++);
			if (propertyValue != null && !propertyValue.isEmpty())
				configurableList.add(propertyValue);
		}
		return configurableList;
	}
	
	private static String[] getPropertyValuePair(String propertyName, String propertyValue, 
			String validFormat) throws ConfigurationException {
		String[] propertyValueParts = propertyValue.split("::");
		if (propertyValueParts.length < 2 || propertyValueParts[0].trim().isEmpty()
				|| propertyValueParts[1].trim().isEmpty())
			throw new ConfigurationException(propertyName,
					"Value format is invalid. Required format: " + validFormat);
		propertyValueParts[0] =	propertyValueParts[0].trim();
		propertyValueParts[1] = propertyValueParts[1].trim();
		return propertyValueParts;
	}
	
	public static Map<String, String> updateConfigurableMapField(
			Dictionary<String, ?> properties, String propertyName, 
			String validFormat) throws ConfigurationException {
		int index = 0;
		Map<String, String> configurableMap = new HashMap<String, String>();
		while (properties.get(propertyName + index) != null) {
			String propertyValue = (String) properties.get(propertyName + index);
			String[] propertyValueParts = getPropertyValuePair(
					propertyName + index, propertyValue, validFormat);
			configurableMap.put(propertyValueParts[0].toLowerCase(), 
					propertyValueParts[1]);
			index++;
		}
		return configurableMap;
	}
	
	public static Map<String, List<String>> updateConfigurableMapOfListsField(
			Dictionary<String, ?> properties, String propertyNamePrefix, 
			String validFormat) throws ConfigurationException {
		Map<String, List<String>> configurableMap = new HashMap<String, List<String>>();
		Enumeration<String> keys = properties.keys();
		while (keys.hasMoreElements()) {
			String propertyName = keys.nextElement();
			if (!propertyName.startsWith(propertyNamePrefix)) continue;
			String propertyValue = (String) properties.get(propertyName);
			String[] propertyValueParts = getPropertyValuePair(
					propertyName, propertyValue, validFormat);
			propertyValueParts[0] = propertyValueParts[0].toLowerCase();
			if (configurableMap.containsKey(propertyValueParts[0])) {
				configurableMap.get(propertyValueParts[0]).add(propertyValueParts[1]);
			} else {
				ArrayList<String> list = new ArrayList<String>();
				list.add(propertyValueParts[1]);
				configurableMap.put(propertyValueParts[0], list);
			}
		}
		return configurableMap;
	}
	
	public static String updateTemplateProperty(Dictionary<String, ?> properties, 
			String propertyName) throws ConfigurationException {
		String pathToNotificationMessageTemplate = 
				Utils.updateStringProperty(properties, propertyName);
		try {
			return IOUtils.toString(
					new FileInputStream(pathToNotificationMessageTemplate), "utf-8");
		} catch (IOException e) {
			throw new ConfigurationException(
					propertyName, "Couldn't find or open the file");
		}
	}
}
