package com.espressif.idf.core.gpt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@SuppressWarnings("nls")
public class GptApi
{
	private static final String API_KEY = "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"; //$NON-NLS-1$
	private static final String GPT_API_COMPLETION_URL = "https://api.openai.com/v1/chat/completions"; //$NON-NLS-1$
	private static final String GPT_API_EMBEDDINGS_URL = "https://api.openai.com/v1/embeddings"; //$NON-NLS-1$

	public static List<Float> queryGPTForEmbeddings(String input) throws IOException
	{
		List<Float> vector = new LinkedList<Float>();
		input = StringEscapeUtils.escapeJava(input);
		JsonObject requestJsonObject = new JsonObject();
		requestJsonObject.addProperty("model", "text-embedding-ada-002");
		requestJsonObject.addProperty("input", input);
		
		URL url = new URL(GPT_API_EMBEDDINGS_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        String jsonInputString = requestJsonObject.toString();
        try (OutputStream os = conn.getOutputStream())
        {
            byte[] inputBytes = jsonInputString.getBytes("utf-8");
            os.write(inputBytes, 0, inputBytes.length);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8")))
        {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null)
            {
                response.append(responseLine.trim());
            }

            String responseBody = response.toString();
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonArray jsonArray = json.get("data").getAsJsonArray().get(0).getAsJsonObject().get("embedding").getAsJsonArray();
            for (JsonElement jsonElement : jsonArray)
            {
            	vector.add(jsonElement.getAsFloat());
            }
            
            return vector;
        }
        catch (IOException e) 
        {
        	InputStream errorStream = conn.getErrorStream();
        	if (errorStream != null) {
        	    try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream))) {
        	      String errorLine;
        	      while ((errorLine = errorReader.readLine()) != null) {
        	        System.err.println(errorLine);
        	      }
        	    }
        	}
        	
        	return null;
		}
	}
	
	public static JsonObject queryGPTForFunctions(String functionName, String functionBody, String location) throws IOException
	{
		functionBody = StringEscapeUtils.escapeJava(functionBody);
		
		String systemContent = "You are given a function name and a function body, you are also given a location of the file where the function "
				+ "was present to help you out a bit more about the context. "
				+ "Describe what the function does and also give what is the intent of the function. The response must be"
				+ " a json object. The context of the code is the esp-idf framework."
				+ "\nYou are to give the response in following json format\nintent: Intent of the function\ncontext: the context for the function"
				+ "\ndescription: a brief description of what the function does\n\n";
		String userContent = "User: Function Name: " + functionName + "\nFunction Body: " + functionBody + "\nLocation: " + location;

		JsonObject requestJsonObject = new JsonObject();
		
		requestJsonObject.addProperty("model", "gpt-3.5-turbo-16k");
		requestJsonObject.addProperty("temperature", 1);
		requestJsonObject.addProperty("max_tokens", 256);
		requestJsonObject.addProperty("top_p", 1);
		requestJsonObject.addProperty("frequency_penalty", 0);
		requestJsonObject.addProperty("presence_penalty", 0);
		
		JsonObject systemRoleObject = new JsonObject();
		systemRoleObject.addProperty("role", "system");
		systemRoleObject.addProperty("content", systemContent);
		
		JsonObject userRoleObject = new JsonObject();
		userRoleObject.addProperty("role", "user");
		userRoleObject.addProperty("content", userContent);
		
		JsonArray jsonArray = new JsonArray();
		jsonArray.add(systemRoleObject);
		jsonArray.add(userRoleObject);
		
		requestJsonObject.add("messages", jsonArray);
		
        URL url = new URL(GPT_API_COMPLETION_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String jsonInputString = requestJsonObject.toString();
        try (OutputStream os = conn.getOutputStream())
        {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8")))
        {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null)
            {
                response.append(responseLine.trim());
            }

            String responseBody = response.toString();
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();

            return json;
        }
        catch (IOException e) 
        {
        	InputStream errorStream = conn.getErrorStream();
        	if (errorStream != null) {
        	    try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream))) {
        	      String errorLine;
        	      while ((errorLine = errorReader.readLine()) != null) {
        	        System.err.println(errorLine);
        	      }
        	    }
        	}
        	
        	return null;
		}

	}

}
