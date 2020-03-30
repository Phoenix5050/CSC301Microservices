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
			
			String queryStr = String.format("CREATE (:profile {userName: \"%s\", fullName: \"%s\", password: \"%s\"})", userName, fullName, password);
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
			
			String queryStr = String.format("MATCH (follower:profile),(followee:profile) WHERE (follower.userName=\"%s\" AND followee.userName=\"%s\") CREATE (follower)-[:follows]->(followee)", userName, frndUserName);
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
			
			String queryStr = String.format("MATCH (follower)-[rel:follows]->(followee)  WHERE (follower.userName=\"%s\" AND followee.userName=\"%s\") DELETE rel", userName, frndUserName);
			trans.run(queryStr);
			
			trans.success();

    		return  new DbQueryStatus("Successfully unfollowed profile", DbQueryExecResult.QUERY_OK);
    	} catch (Exception e) {
    		return new DbQueryStatus("Could not unfollow profile", DbQueryExecResult.QUERY_ERROR_GENERIC);
    	}
	}

	@Override
	public DbQueryStatus getAllSongFriendsLike(String userName) {
			
		return null;
	}
}
