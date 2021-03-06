package controllers;
import play.*;
import play.mvc.*;
import play.libs.*;
import views.html.*;
import models.*;
import play.mvc.Http.*;
import play.libs.Json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;

import play.mvc.BodyParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dataServices.CityDataService;


import dataServices.CountryDataService;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.io.UnsupportedEncodingException;
import java.lang.Object;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

class CityNode {
    private int city_id;
    private String city;
    private int country_id;
    private String last_update;
    private List<link> links = new ArrayList<link>();
    
    public void setCity_id (int i) {
    	this.city_id = i;
    }
    
    public void setCountry_id (int i) {
    	this.country_id = i;
    }
    
    public void setCity (String city) {
    	this.city = city;
    }
    
    public void setLast_update (String s) {
    	this.last_update = s;
    }
    
    public void setLink(link l){	
    	this.links.add(l);
    }
}


class City_resultNode {
	private ArrayList<CityNode> City;
	private List<link> links = new ArrayList<link>();
	
	public City_resultNode() {
		this.City = new ArrayList<CityNode>();
		this.links = new ArrayList<link>();
	}
	
	public void add_City (CityNode n) {
    	this.City.add(n);
    }
	
	public void add_City (link l) {
    	this.links.add(l);
    }
}
public class Cities extends Controller{
	// copy from class Cities
	private static final String query_rule = ".*q='((.*=[\\w]+)+)'.*";
	private static final String limit_rule = ".*limit=([\\d]+)&offset=([\\d]+).*";
	private static final String field_rule = ".*field='(([\\w]+,? *)+)'";
	// All the rules implies that if the parameter exists in the url, the value could not be empty
	
	private static Pattern query_pattern;
    private static Matcher query_matcher;
    private static Pattern limit_pattern;
    private static Matcher limit_matcher;
    private static Pattern field_pattern;
    private static Matcher field_matcher;
    public static Result get() {
		
		String query = null;
		List<City> contry = null;
		int limit = 20;
		int offset = 0;
		final String url_head = "http://localhost:9000";
		String uri = request().uri();
		String path = request().path(); 
		String field = null;
		String tableName = null;
		
		try {
			uri = java.net.URLDecoder.decode(uri, "UTF-8");
			System.out.println(uri);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		query_pattern = Pattern.compile(query_rule);
		query_matcher = query_pattern.matcher(uri);
		
		limit_pattern = Pattern.compile(limit_rule);
		limit_matcher = limit_pattern.matcher(uri);
		
		field_pattern = Pattern.compile(field_rule);
		field_matcher = field_pattern.matcher(uri);
		
		if(query_matcher.find()){
			query = query_matcher.group(1);
			System.out.println("\nquery is "+ query+ "\n");
		}
		
		if(limit_matcher.find()){
			limit = Integer.parseInt(limit_matcher.group(1));
			offset = Integer.parseInt(limit_matcher.group(2));
			System.out.println("limit is " + limit + "     offset is " + offset + "\n");
		}
		
		if(field_matcher.find()){
			field = field_matcher.group(1);
			System.out.println("field is " + field + "\n");
		}
		
		tableName = path.substring(1);
		if(tableName.equals("City")){
			tableName = "City";
		}
		
		System.out.println("Table Name is " + tableName + "\n");
		
	    
	    contry = CityDataService.get(query, field, tableName);
	    
	    //convert result to json
	    City_resultNode result_node = new City_resultNode();
		
		int pageContent = 0;
		if(contry.size() < limit){
			pageContent = contry.size();
		}
		else{
			pageContent = limit;
		}
		
		for (int i = offset; i < (offset + pageContent); i++) {
			CityNode element1 = new CityNode();
			link link1 = new link("self", url_head + path + "/" + contry.get(i).city_id);
			link link2 = new link("country", url_head + "/country" + "/" + contry.get(i).country_id);
			element1.setLink(link1);
			element1.setLink(link2);
			element1.setCity_id(contry.get(i).city_id);
			element1.setCity(contry.get(i).city);
			element1.setCountry_id(contry.get(i).country_id);
			element1.setLast_update(contry.get(i).last_update);
			result_node.add_City(element1);
		}
		
		int length = contry.size();
		int pre_offset = offset - limit;
		int next_offset = offset + limit;
		String pre_url = null;
		String next_url = null;
		System.out.println(length);
		int las_offset = length-limit;
		if (las_offset < 0){
			las_offset = 0; // In case the total result set has less than the offset
		}
		
		if(pre_offset < 0){
			pre_offset = 0;
		}
		
		if(offset == 0){
			pre_url = "";
		}
		else{
			pre_url = url_head + path + "?limit=" + limit + "&offset=" + pre_offset;
		}
		
		if(next_offset >= length){
			next_url = "";
		}
		else{
			next_url = url_head + path + "?limit=" + limit + "&offset=" + next_offset;
		}
		
		String fir_url = url_head + path + "?limit=" + limit + "&offset=0";
		String las_url = url_head + path + "?limit=" + limit + "&offset=" + (las_offset);
		
		link first = new link("first", fir_url);
		link last = new link("last", las_url);
		link previous = new link("previous", pre_url);
		link next = new link("next", next_url);
		result_node.add_City(first);
		result_node.add_City(last);
		result_node.add_City(previous);
		result_node.add_City(next);
		
		return ok(new Gson().toJson(result_node));
	}

    public static Result getItem(int city_id) {
		// New part for projection and links
		//***************************************************************************
		final String url_head = "http://localhost:9000";
		String tableName = null;
		String field = null;
		
		String path = request().path();
		String uri = request().uri();
		
		// To extract the table name
		tableName = path.substring(path.indexOf("/")+1);
		tableName = tableName.substring(0, tableName.indexOf("/"));
				
		if(tableName.equals("city")){
			tableName = "city";
		}
		
		//System.out.println("Table Name is " + tableName);
		
		try {
			uri = java.net.URLDecoder.decode(uri, "UTF-8");
			//System.out.println(uri);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		field_pattern = Pattern.compile(field_rule);
		field_matcher = field_pattern.matcher(uri);
		
		if(field_matcher.find()){
			field = field_matcher.group(1);
			System.out.println("field is " + field + "\n");
		}
		try{
		City city = CityDataService.getItem(city_id, tableName, field);
		
		City_resultNode result_node = new City_resultNode();
		
		//System.out.println(path);
		
		CityNode element1 = new CityNode();
		link link1 = new link("self", url_head + path);
		link link2;
		if(city.country_id == 0){
			link2 = new link("Country", "");
		}
		else{
			link2 = new link("Country", url_head + "/country/" + city.country_id);
		}		

		
		element1.setLink(link1);
		element1.setLink(link2);
		element1.setCity(city.city);
		element1.setCity_id(city.city_id);
		element1.setCountry_id(city.country_id);
		element1.setLast_update(city.last_update);
		result_node.add_City(element1);
		//***************************************************************************
		
		
		return ok(new Gson().toJson(result_node));
		}
		catch(NullPointerException e)
		{
		//Return response code 404
		return notFound(Json.toJson("City not found"));
		}
	}

	// Create a new City
	// (I THINK Java Play framework should be able to figure out if a JSON object 
	// looks like a customer and automatically create the object and pass it in.)
	@BodyParser.Of(BodyParser.Json.class)
	public static Result create() {
		JsonNode json = request().body().asJson();
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0000");
		//get current date time with Dates()
	    Date date = new Date();
		
		City city = new City(0, json.findPath("city").textValue(), json.findPath("country_id").asInt(), date.toString());
		CityDataService.create(city);
		return created(Json.toJson("http://localhost:9000/city/" + city.city_id));
	}
	// delete a city
	public static Result deleteItem(int city_id) {
		 //Check if the city id exists
	    City c = checkCityId(city_id);
		if(c==null){
			// Return response code 404
	    	return notFound(Json.toJson("City not found"));
	    }
		else{
		CityDataService.delete(city_id);
		//Return response code 204
		return noContent();
		}
	}
	
	
		// return a City given a city ID
	    public static City checkCityId(int city_id){
		// New part for projection and links
		//***************************************************************************
		final String url_head = "http://localhost:9000";
		String tableName = null;
		String field = null;
		
		String path = request().path();
		String uri = request().uri();
		
		// To extract the table name
		tableName = path.substring(path.indexOf("/")+1);
		tableName = tableName.substring(0, tableName.indexOf("/"));
				
		if(tableName.equals("city")){
			tableName = "city";
		}
		
		//System.out.println("Table Name is " + tableName);
		
		try {
			uri = java.net.URLDecoder.decode(uri, "UTF-8");
			//System.out.println(uri);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		field_pattern = Pattern.compile(field_rule);
		field_matcher = field_pattern.matcher(uri);
		
		if(field_matcher.find()){
			field = field_matcher.group(1);
			System.out.println("field is " + field + "\n");
		}
		City city = CityDataService.getItem(city_id, tableName, field);
		return city;
}
	
	
	// Update city info
	@BodyParser.Of(BodyParser.Json.class)
	public static Result updateItem(int city_id) {
		JsonNode json = request().body().asJson();
		
		System.out.println(request().body().asJson());
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0000");
		//get current date time with Dates()
	    Date date = new Date();
   	    //System.out.println(dateFormat.format(date));

	    //Check if the city id exists
	    City c = checkCityId(city_id);
		if(c==null){
			// Return response code 404
	    	return notFound("City not found");
	    }
		else{
			City city = new City(city_id, json.findPath("city").textValue(), json.findPath("country_id").asInt(), null);
			CityDataService.update(city);
			// Update successful Response code 204
			return noContent();
			//return ok(Json.toJson(city));
		}
	}
}
