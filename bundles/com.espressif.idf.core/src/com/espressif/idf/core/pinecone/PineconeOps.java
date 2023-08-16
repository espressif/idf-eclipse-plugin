package com.espressif.idf.core.pinecone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import com.espressif.idf.core.logging.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PineconeOps
{
	private static final String URL_UPSERT = "https://idf-index-a675558.svc.us-west4-gcp-free.pinecone.io/vectors/upsert"; //$NON-NLS-1$
	private static final String URL_QUERY = "https://idf-index-a675558.svc.us-west4-gcp-free.pinecone.io/query"; //$NON-NLS-1$
	private static final String API_KEY = "c8e60237-fc0f-43f7-b147-cd4df9ddead3"; //$NON-NLS-1$

	@SuppressWarnings("nls")
	public static JsonObject pineConeQuery(List<Float> queryVector)
	{
		try
		{
			// Create JSON request body
			JsonObject requestBody = new JsonObject();
			requestBody.addProperty("includeValues", false);
			requestBody.addProperty("includeMetadata", true);
			requestBody.addProperty("topK", 5);
			requestBody.add("vector", new Gson().toJsonTree(queryVector, List.class));
			
			return sendRequest(requestBody, URL_QUERY);
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
		
		return null;
	}

	@SuppressWarnings("nls")
	public static void pineConeUpsert(int id, List<Float> vectorEmbedding)
	{
		try
		{
			// Construct JSON using Gson's JsonObject
			JsonObject vector = new JsonObject();
			JsonObject metadata = new JsonObject();
			metadata.addProperty("id", "1");
			vector.add("metadata", metadata);
			JsonArray values = new JsonArray();
			vectorEmbedding.forEach(v -> values.add(v));
			vector.add("values", values);
			vector.addProperty("id", String.valueOf(id));

			JsonArray vectorsArray = new JsonArray();
			vectorsArray.add(vector);

			JsonObject mainObject = new JsonObject();
			mainObject.add("vectors", vectorsArray);

			sendRequest(mainObject, URL_UPSERT);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	@SuppressWarnings("nls")
	private static JsonObject sendRequest(JsonObject requestObject, String urlToUse)
	{
		try
		{
			URL url = new URL(urlToUse);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			// Set the request method
			connection.setRequestMethod("POST");

			// Set request headers
			connection.setRequestProperty("Api-Key", API_KEY);
			connection.setRequestProperty("accept", "application/json");
			connection.setRequestProperty("content-type", "application/json");

			// Enable input/output streams
			connection.setDoOutput(true);

			try (OutputStream os = connection.getOutputStream())
			{
				byte[] input = requestObject.toString().getBytes("utf-8");
				os.write(input, 0, input.length);
			}
			
			int responseCode = connection.getResponseCode();
			try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8")))
			{
				StringBuilder response = new StringBuilder();
				String responseLine;
				while ((responseLine = br.readLine()) != null)
				{
					response.append(responseLine.trim());
				}

				String responseBody = response.toString();
				JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
				Logger.log(json.toString());
				return json;
			}
			catch (IOException e)
			{
				InputStream errorStream = connection.getErrorStream();
				if (errorStream != null)
				{
					try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream)))
					{
						String errorLine;
						while ((errorLine = errorReader.readLine()) != null)
						{
							System.err.println(errorLine);
						}
					}
				}

			}
			System.out.println("HTTP Response Code: " + responseCode);
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
		
		return null;
	}
}
