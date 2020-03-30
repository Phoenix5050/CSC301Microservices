package com.csc301.profilemicroservice;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.springframework.stereotype.Repository;
import org.neo4j.driver.v1.Transaction;

@Repository
public class PlaylistDriverImpl implements PlaylistDriver {

	Driver driver = ProfileMicroserviceApplication.driver;

	public static void InitPlaylistDb() {
		String queryStr;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				queryStr = "CREATE CONSTRAINT ON (nPlaylist:playlist) ASSERT exists(nPlaylist.plName)";
				trans.run(queryStr);
				trans.success();
			}
			session.close();
		}
	}

	@Override
	public DbQueryStatus likeSong(String userName, String songId) {
		//PUT

		try (Session session = ProfileMicroserviceApplication.driver.session()){
			Transaction trans = session.beginTransaction();
			
			String queryStr = String.format("MATCH (s:song) WHERE (s.songId=\"%s\") RETURN s", songId);
			StatementResult result = trans.run(queryStr);
			
			if (!result.hasNext()) {  //song node does not exist
				queryStr = String.format("CREATE (:song {songId: \"%s\"})", songId);
				trans.run(queryStr);
			}
			
			
			queryStr = String.format("MATCH (playlist)-[rel:includes]->(song) WHERE (playlist.plName=\"%s-favorites\" AND song.songId=\"%s\") RETURN rel", userName, songId);
			result = trans.run(queryStr);
			
			if (!result.hasNext()) {  //song not in playlist
				queryStr = String.format("MATCH (playlist:playlist),(song:song) WHERE (playlist.plName=\"%s-favorites\" AND song.songId=\"%s\") CREATE (playlist)-[:includes]->(song)", userName, songId);
				trans.run(queryStr);
			}
			
			trans.success();

    		return  new DbQueryStatus("Successfully liked song", DbQueryExecResult.QUERY_OK);
    	} catch (Exception e) {
    		return new DbQueryStatus("Could not like song", DbQueryExecResult.QUERY_ERROR_GENERIC);
    	}
	}

	@Override
	public DbQueryStatus unlikeSong(String userName, String songId) {
		//PUT
		
		try (Session session = ProfileMicroserviceApplication.driver.session()){
			Transaction trans = session.beginTransaction();
			
			String queryStr = String.format("MATCH (s:song) WHERE (s.songId=\"%s\") RETURN s", songId);
			StatementResult result = trans.run(queryStr);
			
			if (!result.hasNext()) {  //song node does not exist
				return new DbQueryStatus("Song does not exist", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			}
			
			
			queryStr = String.format("MATCH (playlist)-[rel:includes]->(song) WHERE (playlist.plName=\"%s-favorites\" AND song.songId=\"%s\") RETURN rel", userName, songId);
			result = trans.run(queryStr);
			
			if (!result.hasNext()) {  //song not in playlist
				return new DbQueryStatus("Song not in playlist", DbQueryExecResult.QUERY_ERROR_GENERIC);
			}
			
			
			queryStr = String.format("MATCH (playlist)-[rel:includes]->(song) WHERE (playlist.plName=\"%s-favorites\" AND song.songId=\"%s\") DELETE rel", userName, songId);
			result = trans.run(queryStr);
			
			trans.success();

    		return  new DbQueryStatus("Successfully unliked song", DbQueryExecResult.QUERY_OK);
    	} catch (Exception e) {
    		return new DbQueryStatus("Could not unlike song", DbQueryExecResult.QUERY_ERROR_GENERIC);
    	}
	}

	@Override
	public DbQueryStatus deleteSongFromDb(String songId) {
		
		return null;
	}
}
