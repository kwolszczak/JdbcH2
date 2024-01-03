package dev.kwolszczak.peopledb.repository;

import dev.kwolszczak.peopledb.annotation.SQL;
import dev.kwolszczak.peopledb.model.Person;

import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

public class PeopleRepository extends CRUDRepository<Person> {
    private static final String SAVE_PERSON_SQL = "INSERT INTO PEOPLE (FIRST_NAME, LAST_NAME, DOB) VALUES (?, ?, ?)";
    private static final String FIND_PERSON_SQL = "SELECT ID, FIRST_NAME, LAST_NAME, DOB FROM PEOPLE WHERE ID = ?";
    private static final String DELETE_PERSON_SQL = "DELETE FROM PEOPLE WHERE ID = ?";
    private static final String UPDATE_PERSON_SQL = "UPDATE PEOPLE SET FIRST_NAME=?, LAST_NAME=? WHERE ID =?";

    public PeopleRepository(Connection con) {
        super(con);
    }

    @Override
    @SQL(SAVE_PERSON_SQL)
    void mapForSave(Person entity, PreparedStatement ps) throws SQLException {
        ps.setString(1, entity.getFirstName());
        ps.setString(2, entity.getLastName());
        ps.setTimestamp(3, Timestamp.valueOf(entity.getDob().withZoneSameInstant(ZoneId.of("+0")).toLocalDateTime()));
    }

    @Override
    @SQL(UPDATE_PERSON_SQL)
    void mapForUpdate(Person entity, PreparedStatement ps) throws SQLException {
        ps.setString(1, entity.getFirstName());
        ps.setString(2, entity.getLastName());
        ps.setLong(3, entity.getId());
    }

    @Override
    void mapForDelete(Statement statement, String ids) throws SQLException {
        statement.execute(STR."DELETE FROM PEOPLE WHERE ID IN (\{ids});");
    }

    @Override
    @SQL(FIND_PERSON_SQL)
    Person extractEntityFromResultSet(ResultSet rs) throws SQLException {
        long personId = 0;
        personId = rs.getLong("ID");
        String firstName = rs.getString("FIRST_NAME");
        String lastName = rs.getString("LAST_NAME");
        ZonedDateTime dob = ZonedDateTime.of(rs.getTimestamp("DOB").toLocalDateTime(), ZoneId.of("+0"));
        Person entity = new Person(firstName, lastName, dob);
        entity.setId(personId);

        return entity;
    }


    @Override
    protected String getDeleteSql() {
        return DELETE_PERSON_SQL;
    }
}
