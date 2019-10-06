package com.security.controller;

import com.security.Main;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SpaceController {
    private final DataSource dataSource;

    public SpaceController(DataSource dataSource){
        this.dataSource = dataSource;
    }

    public JSONObject createSpace(Request request, Response response) throws SQLException {
        var json = new JSONObject(request.body());
        var spaceName = json.getString("name");
        var owner = json.getString("owner");

        try(
                var conn = dataSource.getConnection();
                var stmt = conn.createStatement();){
            conn.setAutoCommit(false);
            var spaceId = firstLong(stmt.executeQuery("SELECT NEXT VALUE FOR space_id_seq;"));
            stmt.executeUpdate("INSERT INTO spaces(space_id, name, owner) VALUES ('"+ spaceId + "','" + spaceName + "','"+owner+"');");
            conn.commit();

            response.status(201);
            response.header("Location", "spaces/"+spaceId);
            return  new JSONObject().put("name", spaceName).put("uri", "/spaces/"+spaceId);
        }

    }

    private static long firstLong(ResultSet resultSet) throws SQLException {
        if (!resultSet.next()) {
            throw new IllegalArgumentException("no results");
        }

        return resultSet.getLong(1);
    }
}
