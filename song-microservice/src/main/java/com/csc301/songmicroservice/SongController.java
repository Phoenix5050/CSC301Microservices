package com.csc301.songmicroservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/")
public class SongController {

	@Autowired
	private final SongDal songDal;

	private OkHttpClient client = new OkHttpClient();

	
	public SongController(SongDal songDal) {
		this.songDal = songDal;
	}

	
	@RequestMapping(value = "/getSongById/{songId}", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> getSongById(@PathVariable("songId") String songId,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		
		// call function and process query result as ok or error
		DbQueryStatus dbQueryStatus = songDal.findSongById(songId);
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
		
		// since we need data returned as query result, check first if we returned an error
		if(dbQueryStatus.getdbQueryExecResult().equals(DbQueryExecResult.QUERY_OK)) {
			response.put("data", dbQueryStatus.getMessage());
		}
		return response;
	}

	
	@RequestMapping(value = "/getSongTitleById/{songId}", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> getSongTitleById(@PathVariable("songId") String songId,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		
		// call function and process query result as ok or error
		DbQueryStatus dbQueryStatus = songDal.getSongTitleById(songId);
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
		
		// since we need data returned as query result, check first if we returned an error
		if(dbQueryStatus.getdbQueryExecResult().equals(DbQueryExecResult.QUERY_OK)) {
			response.put("data", dbQueryStatus.getMessage());
		}
		return response;
	}

	
	@RequestMapping(value = "/deleteSongById/{songId}", method = RequestMethod.DELETE)
	public @ResponseBody Map<String, Object> deleteSongById(@PathVariable("songId") String songId,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		
		// call function and process query result as ok or error
		DbQueryStatus dbQueryStatus = songDal.deleteSongById(songId);
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
		
		// no data is returned in result of no errors so send response
		return response;
	}

	
	@RequestMapping(value = "/addSong", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> addSong(@RequestParam Map<String, String> params,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		
		// check that we have all fields necessary to make a song
		if (params.containsKey("songName") && params.containsKey("songArtistFullName") && params.containsKey("songAlbum")) {
			// check that all the information in said fields is in a string format so we can process it
			if (params.get("songName") instanceof String && params.get("songArtistFullName") instanceof String && params.get("songAlbum") instanceof String) {
				Song songToAdd = new Song(params.get("songName"), params.get("songArtistFullName"), params.get("songAlbum"));
				
				// call function and process query result as ok or error
				DbQueryStatus dbQueryStatus = songDal.addSong(songToAdd);
				response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
				
				// since we need data returned as query result, check first if we returned an error
				if(dbQueryStatus.getdbQueryExecResult().equals(DbQueryExecResult.QUERY_OK)) {
					response.put("data", dbQueryStatus.getMessage());
				}
				
				return response;
			}
			else {
				DbQueryStatus dbQueryStatus = new DbQueryStatus("Data provided for information was not string type.", DbQueryExecResult.QUERY_ERROR_GENERIC);
				response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
				return response;
			}
		}
		else {
			DbQueryStatus dbQueryStatus = new DbQueryStatus("Missing information for creating song", DbQueryExecResult.QUERY_ERROR_GENERIC);
			response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
			return response;
		}
	}
	
	@RequestMapping(value = "/updateSongFavouritesCount/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> updateFavouritesCount(@PathVariable("songId") String songId,
			@RequestParam("shouldDecrement") String shouldDecrement, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();		
		
		// since we send the boolean, lets do the check here
		if (shouldDecrement.equals("true")) {
			// call function and process query result as ok or error
			DbQueryStatus dbQueryStatus = songDal.updateSongFavouritesCount(songId, true);
			response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
			
			// no data is returned in result of no errors so send response
			return response;
		} else if(shouldDecrement.equals("false")) {
			// call function and process query result as ok or error
			DbQueryStatus dbQueryStatus = songDal.updateSongFavouritesCount(songId, false);
			response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
			
			// no data is returned in result of no errors so send response
			return response;
		} else {
			// if the string is neither true nor false, we didn't get a boolean
			// don't have to check if instance of string since sent as part of request and not in a body
			DbQueryStatus dbQueryStatus = new DbQueryStatus("Boolean not provided (please ensure it is all lowercase)", DbQueryExecResult.QUERY_ERROR_GENERIC);
			response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
			return response;
		}
	}
}