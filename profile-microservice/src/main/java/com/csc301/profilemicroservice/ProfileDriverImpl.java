package com.csc301.profilemicroservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import org.springframework.stereotype.Repository;
import org.neo4j.driver.v1.Transaction;

@Repository
public class ProfileDriverImpl implements ProfileDriver {

	Driver driver = ProfileMicroserviceApplication.driver;

	public static void InitProfileDb() {
		String queryStr;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT exists(nProfile.userName)";
				trans.run(queryStr);

				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT exists(nProfile.password)";
				trans.run(queryStr);

				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT nProfile.userName IS UNIQUE";
				trans.run(queryStr);

				trans.success();
			}
			session.close();
		}
	}
	
	@Override
	public DbQueryStatus createUserProfile(String userName, String fullName, String password) {
		//POST
		
		try (Session session = ProfileMicroserviceApplication.driver.session()){
			Transaction trans = session.beginTransaction();
			
			String queryStr = String.format("MATCH (profile:profile) WHERE (profile.userName=\"%s\") RETURN profile", userName);
			StatementResult result = trans.run(queryStr);
			
			if (result.hasNext()) { //profile already exists
				return  new DbQueryStatus("Profile with this username already exists", DbQueryExecResult.QUERY_ERROR_GENERIC);
			}
			
			queryStr = String.format("CREATE (:profile {userName: \"%s\", fullName: \"%s\", password: \"%s\"})", userName, fullName, password);
			trans.run(queryStr);
			
			queryStr = String.format("CREATE (:playlist {plName: \"%s-favorites\"})", userName);
			trans.run(queryStr);
			
			queryStr = String.format("MATCH (profile:profile),(playlist:playlist) WHERE (profile.userName=\"%s\" AND playlist.plName=\"%s-favorites\") CREATE (profile)-[:created]->(playlist)", userName, userName);
			trans.run(queryStr);
			
			
			
			trans.success();

    		return  new DbQueryStatus("Successfully created profile", DbQueryExecResult.QUERY_OK);
    	} catch (Exception e) {
    		return new DbQueryStatus("Profile could not be created", DbQueryExecResult.QUERY_ERROR_GENERIC);
    	}
		
		
	}

	@Override
	public DbQueryStatus followFriend(String userName, String frndUserName) {
		//PUT
		
		try (Session session = ProfileMicroserviceApplication.driver.session()){
			Transaction trans = session.beginTransaction();
			
			String queryStr = String.format("MATCH (p1:profile) WHERE (p1.userName=\"%s\") RETURN p1.userName", frndUserName); 
			StatementResult result = trans.run(queryStr);
			
			if (!result.hasNext()) { //friend profile does not exist
				return  new DbQueryStatus("The profile you are trying to follow does not exist", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			}
			
			queryStr = String.format("MATCH (p1:profile)-[rel:follows]->(p2:profile) WHERE (p1.userName=\"%s\" AND p2.userName=\"%s\") RETURN rel", userName, frndUserName); 
			result = trans.run(queryStr);
			
			if (result.hasNext()) { //relationship exists
				return  new DbQueryStatus("Already following this profile", DbQueryExecResult.QUERY_ERROR_GENERIC);
			} 
			
			queryStr = String.format("MATCH (follower:profile),(followee:profile) WHERE (follower.userName=\"%s\" AND followee.userName=\"%s\") CREATE (follower)-[:follows]->(followee)", userName, frndUserName);
			trans.run(queryStr);
			
			trans.success();

    		return  new DbQueryStatus("Successfully followed profile", DbQueryExecResult.QUERY_OK);
    	} catch (Exception e) {
    		return new DbQueryStatus("Could not follow profile", DbQueryExecResult.QUERY_ERROR_GENERIC);
    	}
	}

	@Override
	public DbQueryStatus unfollowFriend(String userName, String frndUserName) {
		//PUT
		
		try (Session session = ProfileMicroserviceApplication.driver.session()){
			Transaction trans = session.beginTransaction();
			
			
			String queryStr = String.format("MATCH (p1:profile) WHERE (p1.userName=\"%s\") RETURN p1.userName", frndUserName); 
			StatementResult result = trans.run(queryStr);
			
			if (!result.hasNext()) { //friend profile does not exist
				return  new DbQueryStatus("The profile you are trying to unfollow does not exist", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			}
			
			queryStr = String.format("MATCH (p1:profile)-[rel:follows]->(p2:profile) WHERE (p1.userName=\"%s\" AND p2.userName=\"%s\") RETURN rel", userName, frndUserName); 
			result = trans.run(queryStr);
			
			if (!result.hasNext()) { //relationship does not exist
				return  new DbQueryStatus("Not following this profile", DbQueryExecResult.QUERY_ERROR_GENERIC);
			} 
			
			
			
			queryStr = String.format("MATCH (follower)-[rel:follows]->(followee)  WHERE (follower.userName=\"%s\" AND followee.userName=\"%s\") DELETE rel", userName, frndUserName);
			trans.run(queryStr);
			
			trans.success();

    		return  new DbQueryStatus("Successfully unfollowed profile", DbQueryExecResult.QUERY_OK);
    	} catch (Exception e) {
    		return new DbQueryStatus("Could not unfollow profile", DbQueryExecResult.QUERY_ERROR_GENERIC);
    	}
	}

	@Override
	public DbQueryStatus getAllSongFriendsLike(String userName) {
		//GET
			
		try (Session session = ProfileMicroserviceApplication.driver.session()){
			Transaction trans = session.beginTransaction();
			String data = null;
			String followee = null;
			
			String queryStr = String.format("MATCH (follower)-[:follows]->(followee)-[:created]->(playlist)-[]->(song)  WHERE (follower.userName=\"%s\") RETURN followee.userName, song.songId", userName);
			
			StatementResult result = trans.run(queryStr);
			
			while (result.hasNext()) {
				Record next = result.next();
				
				if (followee == null) {
					data += "{\n\t";
					data += next.get( "followee.userName" ).asString();
					data += ": [";
					
				} else if(!(followee.equals(next.get( "followee.userName" ).asString()) ) ) {
					data += "],";
					data += next.get( "followee.userName" ).asString();
					data += ": [";
					
				} else {
					data += ",\n";
				}
				
				followee = next.get( "followee.userName" ).asString();
				
				data += "\n\t" + next.get( "song.songId" ).asString();
				
			}
			
			data += "]\n}";
			
			trans.success();

    		return  new DbQueryStatus(data, DbQueryExecResult.QUERY_OK);
    	} catch (Exception e) {
    		return new DbQueryStatus("Could not show all songs friends like", DbQueryExecResult.QUERY_ERROR_GENERIC);
    	}
	}
}
