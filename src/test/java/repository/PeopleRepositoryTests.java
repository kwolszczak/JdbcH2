package repository;

import dev.kwolszczak.peopledb.model.Person;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import dev.kwolszczak.peopledb.repository.PeopleRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class PeopleRepositoryTests {

    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:C:/Users/kwolszczak_adm/Desktop/dev/DB/peopleDB");
        connection.setAutoCommit(false);    //it's allows to not commit changes to db. All changes after connection.close() will be rollback
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    void canSaveOnePerson() throws SQLException {

        PeopleRepository repo = new PeopleRepository(connection);
        Person john = new Person("John", "Smith", ZonedDateTime.of(1980, 11, 15, 15, 15, 0,0 , ZoneId.of("-6")));

        Person savedPerson= repo.save(john);

        assertThat(savedPerson.getId()).isGreaterThan(0);
    }

    @Test
    void canSaveTwoPeople() {
        PeopleRepository repo = new PeopleRepository(connection);
        Person john = new Person("John", "Smith", ZonedDateTime.of(1980, 11, 15, 15, 15, 0,0 , ZoneId.of("-6")));
        Person bobby = new Person("Bobby", "Smith", ZonedDateTime.of(1982, 9, 13, 15, 7, 0,0 , ZoneId.of("-6")));

        Person savedPerson= repo.save(john);
        Person savedPerson2 = repo.save(bobby);

        assertThat(savedPerson.getId()).isGreaterThan(0);
        assertThat(savedPerson2.getId()).isGreaterThan(savedPerson.getId());
    }


}
