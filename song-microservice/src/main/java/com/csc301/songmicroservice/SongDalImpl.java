package com.csc301.songmicroservice;

import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

@Repository
public class SongDalImpl implements SongDal {

	private final MongoTemplate db;

	@Autowired
	public SongDalImpl(MongoTemplate mongoTemplate) {
		this.db = mongoTemplate;
	}

	@Override
	public DbQueryStatus addSong(Song songToAdd) {
		// POST	
		// First check to see if the song name or artist exist already as specified in piazza
		String songName = songToAdd.getSongName();
		String artist = songToAdd.getSongArtistFullName();
		FindIterable<Document> names = db.getCollection("songs").find(new Document("songName", songName));
	    FindIterable<Document> artists = db.getCollection("songs").find(new Document("songArtistFullName", artist));
	    if(names.first() != null || artists.first() != null) {
	    	return new DbQueryStatus("Song or artist already exists", DbQueryExecResult.QUERY_ERROR_GENERIC);
	    }else {
	    	// add our song to the database
	    	db.insert(songToAdd, "songs");		
			return new DbQueryStatus(songToAdd.toString(), DbQueryExecResult.QUERY_OK);
	    }
	}

	@Override
	public DbQueryStatus findSongById(String songId) {
		// GET
		try {
			// try block is so if we get an invalid objectId we return error
			ObjectId id = new ObjectId(songId);
			Document getter = new Document("_id", id);
			BasicDBObject query = new BasicDBObject();
			
			Document myDoc = null;
    		try { //finding song with _id
				query.put("_id", new ObjectId(songId));
				myDoc = db.getCollection("songs").find(query).first();
				
				//song found so lets make a new song object for easy display
				
				// get info
				String name = myDoc.get("songName").toString();
				String artist = myDoc.get("songArtistFullName").toString();
				String album = myDoc.get("songAlbum").toString();
				String favsString = myDoc.get("songAmountFavourites").toString();
				long favs = Long.parseLong(favsString);
				
				// make song
				Song song = new Song(name, artist, album);
				song.setId(id);
				song.setSongAmountFavourites(favs);
				
				
				// get display
				Map<String, String> ret = song.getJsonRepresentation();
				
				return  new DbQueryStatus(ret.toString(), DbQueryExecResult.QUERY_OK);
    		} catch (Exception e) { //_id not in database, so post does not exist
    			return new DbQueryStatus("SongId could not be found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
    		}
		} catch(Exception e){
			return new DbQueryStatus("SongId provided is invalid object id", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

	@Override
	public DbQueryStatus getSongTitleById(String songId) {
		// GET
		try {
			// try block is so if we get an invalid objectId we return error
			Document getter = new Document("_id", new ObjectId(songId));
			BasicDBObject query = new BasicDBObject();
			
			Document myDoc = null;
    		try { //finding song with _id
    			query.put("_id", new ObjectId(songId));
    			myDoc = db.getCollection("songs").find(query).first();
    			
    			//song found so we just need to return name
    			String name = myDoc.get("songName").toString();
    			return  new DbQueryStatus(name, DbQueryExecResult.QUERY_OK);
    			
    		} catch (Exception e) { //_id not in database, so post does not exist
    			return new DbQueryStatus("SongId could not be found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
    		}
		} catch(Exception e){
			return new DbQueryStatus("SongId provided is invalid object id", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

	@Override
	public DbQueryStatus deleteSongById(String songId) {
		// DELETE
		try {
			// try block is so if we get an invalid objectId we return error
			Document rem = new Document("_id", new ObjectId(songId));
			if (db.getCollection("songs").findOneAndDelete(rem) == null)
	    	{
	    		// object could not be found in database
				return new DbQueryStatus("SongId could not be found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
	    	}
	    	else {
	    		// everything worked correctly
	    		// at this point the song has been deleted from mongoDB
	    		// ANUSHAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA PUT IT HERE 
	    		return new DbQueryStatus("Song removed successfully", DbQueryExecResult.QUERY_OK);
	    	}
		} catch(Exception e){
			return new DbQueryStatus("SongId provided is invalid object id", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

	@Override
	public DbQueryStatus updateSongFavouritesCount(String songId, boolean shouldDecrement) {
		// PUT
		try {
			// try block is so if we get an invalid objectId we return error
			BasicDBObject searchQuery = new BasicDBObject("_id", new ObjectId(songId));
			
			Document myDoc = null;
    		try { //finding song with _id
    			myDoc = db.getCollection("songs").find(searchQuery).first();
    			
    			// song found so lets find out our current number of favourites in the case its 0 and we need to decrease
    			String favsString = myDoc.get("songAmountFavourites").toString();
				long favs = Long.parseLong(favsString);
				BasicDBObject updateQuery = new BasicDBObject();
				
    			if (shouldDecrement==true) {
    				// can't have a -ve favourites count
    				if (favs>0) {
    					// update favourites number and set our query to update the field
    					long num = favs-1;
    					updateQuery.append("$set", new BasicDBObject().append("songAmountFavourites", num));
    					db.getCollection("songs").updateOne(searchQuery, updateQuery);
    				}
    			} else {
    				// update favourites number and set our query to update the field
    				long num = favs+1;
    				updateQuery.append("$set", new BasicDBObject().append("songAmountFavourites", num));
    				db.getCollection("songs").updateOne(searchQuery, updateQuery);
    			}
    			return new DbQueryStatus("Song updated successfully", DbQueryExecResult.QUERY_OK);
    		} catch (Exception e) { //_id not in database, so post does not exist
    			return new DbQueryStatus("SongId could not be found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
    		}
		} catch(Exception e){
			return new DbQueryStatus("SongId provided is invalid object id", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}
}