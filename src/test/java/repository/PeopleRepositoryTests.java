package repository;

import dev.kwolszczak.peopledb.model.Person;
import dev.kwolszczak.peopledb.repository.PeopleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class PeopleRepositoryTests {

    private Connection connection;
    private PeopleRepository repo;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:C:/Users/kwolszczak_adm/Desktop/dev/DB/peopleDB");
        connection.setAutoCommit(false);    //it's allows to not commit changes to db. All changes after connection.close() will be rollback
        repo = new PeopleRepository(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    void canSaveOnePerson() throws SQLException {

        Person john = new Person("John", "Smith", ZonedDateTime.of(1980, 11, 15, 15, 15, 0, 0, ZoneId.of("-6")));

        Person savedPerson = repo.save(john);

        assertThat(savedPerson.getId()).isGreaterThan(0);
    }

    @Test
    void canSaveTwoPeople() {
        Person john = new Person("John", "Smith", ZonedDateTime.of(1980, 11, 15, 15, 15, 0, 0, ZoneId.of("-6")));
        Person bobby = new Person("Bobby", "Smith", ZonedDateTime.of(1982, 9, 13, 15, 7, 0, 0, ZoneId.of("-6")));

        Person savedPerson = repo.save(john);
        Person savedPerson2 = repo.save(bobby);

        assertThat(savedPerson.getId()).isPositive();
        assertThat(savedPerson2.getId()).isGreaterThan(savedPerson.getId());
    }

    @Test
    void canFindPersonById() {
        Person savedPerson = repo.save(new Person("test", "jackson", ZonedDateTime.now().withZoneSameInstant(ZoneId.of("+0"))));
        Person foundPerson = repo.findById(savedPerson.getId()).get();

        assertThat(foundPerson.getFirstName()).isEqualTo(savedPerson.getFirstName());
        assertThat(foundPerson.getLastName()).isEqualTo(savedPerson.getLastName());
        assertThat(foundPerson.getId()).isEqualTo(savedPerson.getId());
    }

    @Test
    void cantFindPersonById() {
        Optional<Person> foundPerson = repo.findById(-1L);

        assertThat(foundPerson).isEmpty();
    }

}
