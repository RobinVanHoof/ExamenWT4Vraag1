package moviespackage;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.BasicDBObject;
import com.mongodb.Cursor;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import moviespackage.JedisConnection;
import jdk.nashorn.internal.parser.JSONParser;
import redis.clients.jedis.Jedis;


@Path("/movies")
public class MovieResource {
	@GET
	@Path("{title}")
	@Produces({"text/html"})
	public String searchMovie(@PathParam("title") String title){
		
		Jedis jedis = JedisConnection.getInstance().getConnection();
		
		
		String returnJSON = "";
		
		Boolean found = false;
		
		
		for(String titlee : jedis.keys("Title:*")){
			String tmpTitle = jedis.get(titlee);
			if(tmpTitle.equalsIgnoreCase(title)){
				found = true;
			}
		}
		


		if (!found){
			
			Response response = ClientBuilder.newClient()
					.target("http://www.omdbapi.com/?t=" + title + "&apikey=plzBanMe")
					.request(MediaType.APPLICATION_JSON)
					.get();
			
			String jsonString = response.readEntity(String.class);
			
			JsonReader jsonReader = Json.createReader(new StringReader(jsonString));
			JsonObject object = jsonReader.readObject();
			jsonReader.close();
			
			Set<String>keys = jedis.keys("Title:*");

			int nextId = 0;
			if (keys.iterator().hasNext()){
				List<String> array = new ArrayList();
				for (String key : keys){
					array.add(key);
				}
				nextId = Integer.parseInt(array.get(array.size()-1).split(":")[1]) + 1;
			}
			else{
				nextId = 1;
			}

			System.out.println(nextId);
			jedis.set("Title:" + nextId, object.getString("Title"));
			jedis.set("Year:" + nextId, object.getString("Year"));
			jedis.set("Actors:" + nextId, object.getString("Actors"));
			
			JsonObjectBuilder builder = Json.createObjectBuilder();
	        builder.add("Year", object.getString("Year"));
	        builder.add("Actors", object.getString("Actors"));
	        JsonObject newJSON = builder.build();
	        
	        System.out.println("toegevoegd aan db");
			
			returnJSON = newJSON.toString();
		}
		else{
			JsonObjectBuilder builder = Json.createObjectBuilder();
			for (String title2 : jedis.keys("Title:*")) {
				String tmpTitle = jedis.get(title2);
				if (tmpTitle.equalsIgnoreCase(title)) {
					int titleId = Integer.parseInt(title2.split(":")[1]);
					
					builder.add("Year", jedis.get("Year:" + titleId));
					builder.add("Actors", jedis.get("Actors:" + titleId));
					
					JsonObject newJSON = builder.build();
					System.out.println("al eerder opgeslagen");
					
					returnJSON = newJSON.toString();
				}
			}
		}
		return returnJSON;
	}
	
}
