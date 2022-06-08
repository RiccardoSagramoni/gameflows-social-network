package it.unipi.dii.inginf.lsdb.gameflows.admin;

import it.unipi.dii.inginf.lsdb.gameflows.persistence.MongoConnection;
import it.unipi.dii.inginf.lsdb.gameflows.persistence.Neo4jConnection;
import javafx.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

/**
 * Interface which offer a set of methods which allows the application
 * to fulfill functionalities requested by an Admin
 */
public interface AdminService {

	boolean login(@NotNull String username, @NotNull String password);
	boolean login (@NotNull MongoConnection connection, @NotNull String username, @NotNull String password);

	boolean createAdminAccount(@NotNull Admin admin);
	boolean createAdminAccount(@NotNull MongoConnection connection, @NotNull Admin admin);

	List<Pair<String, Integer>> viewInfluencerRanking(@NotNull Date fromDate, @NotNull Date toDate, int limit);
	List<Pair<String, Integer>> viewInfluencerRanking(@NotNull Neo4jConnection connection, @NotNull Date fromDate, @NotNull Date toDate, int limit);

	boolean updateInfluencers(@NotNull List<String> influencers);
	boolean updateInfluencers(@NotNull MongoConnection connection, @NotNull List<String> influencers);

	boolean blockUser (@NotNull String username, boolean block);
	boolean blockUser (@NotNull MongoConnection connection, @NotNull String username, boolean block);

	Admin find (@NotNull String username);
	Admin find (@NotNull MongoConnection connection, @NotNull String username);

}
