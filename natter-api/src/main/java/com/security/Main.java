package com.security;

import com.security.controller.SpaceController;
import org.h2.jdbcx.JdbcConnectionPool;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

import static spark.Spark.*;

public class Main {

    public static void main(String... args) throws SQLException, IOException, URISyntaxException {
        var datasource = JdbcConnectionPool.create("jdbc:h2:mem:natter", "natter", "password");
        createTables(datasource.getConnection());

        var spaceController = new SpaceController(datasource);
        post("/spaces",spaceController::createSpace);
        after(((request, response) -> {response.type("application/json");}));
        internalServerError(new JSONObject().put("error", "internal server error").toString());
        notFound(new JSONObject().put("error", "not found").toString());
    }

    private static void createTables(Connection connection) throws SQLException, URISyntaxException, IOException {
        try(
                var conn = connection;
                var stmt = conn.createStatement();){
            conn.setAutoCommit(false);
            Path path = Paths.get(Main.class.getResource("/schema.sql").toURI());
            stmt.execute(Files.readString(path));
            conn.commit();
        }
    }

}
