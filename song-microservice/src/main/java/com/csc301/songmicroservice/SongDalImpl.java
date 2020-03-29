package com.csc301.songmicroservice;

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
		String songName = songToAdd.getSongName();
		String artist = songToAdd.getSongArtistFullName();
		FindIterable<Document> names = db.getCollection("songs").find(new Document("songName", songName));
	    FindIterable<Document> artists = db.getCollection("songs").find(new Document("songArtistFullName", artist));
	    if(names.first() != null || artists.first() != null) {
	    	return new DbQueryStatus("Song or artist already exists", DbQueryExecResult.QUERY_ERROR_GENERIC);
	    }else {
	    	db.insert(songToAdd, "songs");		
			return new DbQueryStatus(songToAdd.toString(), DbQueryExecResult.QUERY_OK);
	    }
	}

	@Override
	public DbQueryStatus findSongById(String songId) {
		// GET
		return null;
	}

	@Override
	public DbQueryStatus getSongTitleById(String songId) {
		// GET
		try {
			Document getter = new Document("_id", new ObjectId(songId));
			BasicDBObject query = new BasicDBObject();
			
			Document myDoc = null;
    		try { //finding post with _id
    			query.put("_id", new ObjectId(songId));
    			myDoc = db.getCollection("songs").find(query).first();
    			String name = myDoc.get("songName").toString();
    			
    			return  new DbQueryStatus(name, DbQueryExecResult.QUERY_OK);
    		} catch (Exception e) { //_id not in database, so post does not exist
    			return new DbQueryStatus("SongId could not be found", DbQueryExecResult.QUERY_ERROR_GENERIC);
    		}
		} catch(Exception e){
			return new DbQueryStatus("SongId provided is invalid object id", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

	@Override
	public DbQueryStatus deleteSongById(String songId) {
		// DELETE
		try {
			Document rem = new Document("_id", new ObjectId(songId));
			if (db.getCollection("songs").findOneAndDelete(rem) == null)
	    	{
	    		// object could not be found in database
				return new DbQueryStatus("SongId could not be found", DbQueryExecResult.QUERY_ERROR_GENERIC);
	    	}
	    	else {
	    		// everything worked correctly
	    		// PUT IT HERE
	    		return new DbQueryStatus("Song removed successfully", DbQueryExecResult.QUERY_OK);
	    	}
		} catch(Exception e){
			return new DbQueryStatus("SongId provided is invalid object id", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

	@Override
	public DbQueryStatus updateSongFavouritesCount(String songId, boolean shouldDecrement) {
		// PUT
		return null;
	}
}