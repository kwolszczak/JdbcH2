package dev.kwolszczak.peopledb.repository;

import dev.kwolszczak.peopledb.exception.UnableToSaveException;
import dev.kwolszczak.peopledb.model.Person;

import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class PeopleRepository {
    public static final String SAVE_PERSON_SQL = "INSERT INTO PEOPLE (FIRST_NAME, LAST_NAME, DOB) VALUES (?, ?, ?)";
    public static final String FIND_PERSON_SQL = "SELECT ID, FIRST_NAME, LAST_NAME, DOB FROM PEOPLE WHERE ID = ?";
    private Connection connection;
    private String DELETE_PERSON_SQL;

    public PeopleRepository(Connection con) {
        this.connection = con;

    }

    public Person save(Person person) {
        try {
            PreparedStatement ps = connection.prepareStatement(SAVE_PERSON_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, person.getFirstName());
            ps.setString(2, person.getLastName());
            ps.setTimestamp(3, Timestamp.valueOf(person.getDob().withZoneSameInstant(ZoneId.of("+0")).toLocalDateTime()));

            int recordsAffected = ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            while (rs.next()) {
                long id = rs.getLong(1);
                person.setId(id);
            }
            System.out.println(person);
            System.out.println(STR."Records affected: \{recordsAffected}");

        } catch (SQLException e) {
            e.printStackTrace();
            throw new UnableToSaveException(STR."Tried to save person:\{person}");
        }

        return person;
    }

    public Optional<Person> findById(Long id) {
        Person person = null;

        try {

            PreparedStatement ps = connection.prepareStatement(FIND_PERSON_SQL);
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                long personId = rs.getLong("ID");
                String firstName = rs.getString("FIRST_NAME");
                String lastName = rs.getString("LAST_NAME");
                ZonedDateTime dob = ZonedDateTime.of(rs.getTimestamp("DOB").toLocalDateTime(), ZoneId.of("+0"));
                person = new Person(firstName, lastName, dob);
                person.setId(personId);
            }
            System.out.println(person);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.ofNullable(person);
    }

    public void delete(Long id) {
        try {
            DELETE_PERSON_SQL = "DELETE FROM PEOPLE WHERE ID = ?";
            PreparedStatement ps = connection.prepareStatement(DELETE_PERSON_SQL);
            ps.setLong(1, id);
            boolean rs = ps.execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public void delete(Person... people) {
        //delete from people where id in ( 10,20,30,40);
        try {
            String ids = Arrays.stream(people)
                    .map(Person::getId)
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            Statement statement = connection.createStatement();
            statement.execute(STR."DELETE FROM PEOPLE WHERE ID IN (\{ids});");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

/*        for (var person : people) {
//            delete(people);

        }*/
    }
}
