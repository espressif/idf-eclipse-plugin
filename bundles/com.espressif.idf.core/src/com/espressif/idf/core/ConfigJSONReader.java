//package com.espressif.idf.core;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.IOException;
//import java.net.URL;
//import java.util.Iterator;
//
//import org.eclipse.core.runtime.FileLocator;
//import org.eclipse.core.runtime.Path;
//import org.eclipse.core.runtime.Platform;
//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;
//import org.osgi.framework.Bundle;
//
//public class ConfigJSONReader {
//
//	
//	public KConfigMenuItem reader()
//	{
//		JSONParser parser = new JSONParser();
//		
//		
//		String file = "resources/kconfig_menus.json"; //$NON-NLS-1$
//		String pluginlocation = null;
//		  try {
//		    Bundle bundle = Platform.getBundle("com.espressif.idf.confserver.ui"); //my class pkg
//		    URL pLocationUrl = FileLocator.find(bundle, new Path("/"), null);
//		    URL pFileUrl = FileLocator.toFileURL(pLocationUrl);
//		    pluginlocation = pFileUrl.getFile();
//		   } catch (IOException e) {
//		    //log error
//		   }
//		   
//		  KConfigMenuItem root = new KConfigMenuItem(null);
//		   
//		String completePath = pluginlocation.concat(File.separator).concat(file);
//		
//		try {
//
//            Object obj = parser.parse(new FileReader(completePath));
//            read(obj, root);
//            
//            System.out.println(root);
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//		return root;
//	}
//	private void read(Object obj, KConfigMenuItem menuItem) {
//		JSONArray jsonArray = (JSONArray) obj;
//		Iterator<JSONObject> iterator = jsonArray.iterator();
//		while (iterator.hasNext()) {
//		    
//			KConfigMenuItem childMenu = new KConfigMenuItem(menuItem);
//			
//			JSONObject jsonObject = (JSONObject) iterator.next();
//			
//			childMenu.setName((String) jsonObject.get("name"));
//			childMenu.setType((String) jsonObject.get("type"));
//			childMenu.setHelp((String) jsonObject.get("help"));
//			childMenu.setDepends_on((String) jsonObject.get("depends_on"));
//			
//			String title = (String) jsonObject.get("title");
//			childMenu.setTitle(title);
//			
//			if (title != null)
//			{
//				menuItem.addChild(childMenu);
//			}
//		   
//		    // loop array
//		    JSONArray children = (JSONArray) jsonObject.get("children");
//		    read(children, childMenu);
//		    
//		    
//		}
//	}
//}
