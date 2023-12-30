package dev.kwolszczak.peopledb.repository;

import dev.kwolszczak.peopledb.model.Person;

import java.sql.*;
import java.time.ZoneId;

public class PeopleRepository {
    private Connection connection;

    public PeopleRepository(Connection con) {
        this.connection = con;

    }

    public Person save(Person person) {
        String sql = "INSERT INTO PEOPLE (FIRST_NAME, LAST_NAME, DOB) VALUES (?,?,?)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, person.getFirstName());
            ps.setString(2, person.getLastName());
            ps.setTimestamp(3, Timestamp.valueOf(person.getDob().withZoneSameInstant(ZoneId.of("+0")).toLocalDateTime()));

            int recordsAffected = ps.executeUpdate();
            System.out.println(STR."Records affected: \{recordsAffected}");

            ResultSet rs = ps.getGeneratedKeys();
            while (rs.next()) {
                long id  = rs.getLong(1);
                person.setId(id);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return person;
    }
}
