package com.espressif.idf.ui.installcomponents.desrializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.espressif.idf.ui.installcomponents.vo.ComponentDetailsDependenciesVO;
import com.espressif.idf.ui.installcomponents.vo.ComponentDetailsVO;
import com.espressif.idf.ui.installcomponents.vo.ComponentVO;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class ComponentsDesrializer implements JsonDeserializer<List<ComponentVO>>
{

	@Override
	public List<ComponentVO> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException
	{
		List<ComponentVO> componentVOs = new ArrayList<>();
		JsonArray jsonArray = json.getAsJsonArray();
		for (int i = 0; i < jsonArray.size(); i++)
		{
			JsonElement jsonElement = jsonArray.get(i);

			JsonObject mainJsonObject = jsonElement.getAsJsonObject();
			ComponentVO componentVO = new ComponentVO();
			String createdAt = mainJsonObject.get("created_at") == null || mainJsonObject.get("created_at").isJsonNull()
					? null
					: mainJsonObject.get("created_at").getAsString();
			boolean featured = mainJsonObject.get("featured") == null || mainJsonObject.get("featured").isJsonNull()
					? false
					: mainJsonObject.get("featured").getAsBoolean();
			String name = mainJsonObject.get("name") == null || mainJsonObject.get("name").isJsonNull() ? null
					: mainJsonObject.get("name").getAsString();
			String namespace = mainJsonObject.get("namespace") == null || mainJsonObject.get("namespace").isJsonNull()
					? null
					: mainJsonObject.get("namespace").getAsString();
			componentVO.setCreatedAt(createdAt);
			componentVO.setFeatured(featured);
			componentVO.setName(name);
			componentVO.setNamespace(namespace);

			if (mainJsonObject.get("latest_version").isJsonNull())
			{
				componentVOs.add(componentVO);
				continue;	
			}
			JsonObject detailsJsonObject = mainJsonObject.getAsJsonObject("latest_version");

			String componentHash = detailsJsonObject.get("component_hash") == null
					|| detailsJsonObject.get("component_hash").isJsonNull() ? null
							: detailsJsonObject.get("component_hash").getAsString();
			String details = detailsJsonObject.get("description") == null
					|| detailsJsonObject.get("description").isJsonNull() ? null
							: detailsJsonObject.get("description").getAsString();
			createdAt = detailsJsonObject.get("created_at") == null || detailsJsonObject.get("created_at").isJsonNull()
					? null
					: detailsJsonObject.get("created_at").getAsString();
			String url = detailsJsonObject.get("url") == null || detailsJsonObject.get("url").isJsonNull() ? null
					: detailsJsonObject.get("url").getAsString();
			String version = detailsJsonObject.get("version") == null || detailsJsonObject.get("version").isJsonNull()
					? null
					: detailsJsonObject.get("version").getAsString();
			ComponentDetailsVO componentDetailsVO = new ComponentDetailsVO();
			componentDetailsVO.setComponentHash(componentHash);
			componentDetailsVO.setCreatedAt(createdAt);
			componentDetailsVO.setDescription(details);
			componentDetailsVO.setUrl(url);
			componentDetailsVO.setVersion(version);
			if (detailsJsonObject.getAsJsonObject("docs") != null
					&& detailsJsonObject.getAsJsonObject("docs").get("readme") != null)
			{
				componentDetailsVO.setReadMe(detailsJsonObject.getAsJsonObject("docs").get("readme").getAsString());
			}

			JsonArray dependenciesArray = detailsJsonObject.getAsJsonArray("dependencies");
			List<ComponentDetailsDependenciesVO> componentDetailsDependenciesVOs = new ArrayList<ComponentDetailsDependenciesVO>();
			for (int x = 0; x < dependenciesArray.size(); x++)
			{
				JsonObject dependencyJsonObject = dependenciesArray.get(x).getAsJsonObject();
				ComponentDetailsDependenciesVO componentDetailsDependenciesVO = new ComponentDetailsDependenciesVO();
				boolean isPublic = dependencyJsonObject.get("is_public") == null
						|| dependencyJsonObject.get("is_public").isJsonNull() ? false
								: dependencyJsonObject.get("is_public").getAsBoolean();
				name = dependencyJsonObject.get("name") == null || dependencyJsonObject.get("name").isJsonNull() ? null
						: dependencyJsonObject.get("name").getAsString();
				namespace = dependencyJsonObject.get("namespace") == null
						|| dependencyJsonObject.get("namespace").isJsonNull() ? null
								: dependencyJsonObject.get("namespace").getAsString();
				String source = dependencyJsonObject.get("source") == null
						|| dependencyJsonObject.get("source").isJsonNull() ? null
								: dependencyJsonObject.get("source").getAsString();
				String spec = dependencyJsonObject.get("spec") == null || dependencyJsonObject.get("spec").isJsonNull()
						? null
						: dependencyJsonObject.get("spec").getAsString();
				componentDetailsDependenciesVO.setPublic(isPublic);
				componentDetailsDependenciesVO.setName(name);
				componentDetailsDependenciesVO.setNamespace(namespace);
				componentDetailsDependenciesVO.setSource(source);
				componentDetailsDependenciesVO.setSpec(spec);

				componentDetailsDependenciesVOs.add(componentDetailsDependenciesVO);
			}

			componentDetailsVO.setDependencies(componentDetailsDependenciesVOs);
			componentVO.setComponentDetails(componentDetailsVO);
			componentVOs.add(componentVO);
		}
		return componentVOs;
	}

}
