package repository;

import dev.kwolszczak.peopledb.model.Person;
import dev.kwolszczak.peopledb.repository.CRUDRepository;
import dev.kwolszczak.peopledb.repository.PeopleRepository;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
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

    @Test
    void canDelete() {
        Person savedPerson = repo.save(new Person("test", "jackson", ZonedDateTime.now().withZoneSameInstant(ZoneId.of("+0"))));
        repo.delete(savedPerson);
        Optional<Person> personFromDB = repo.findById(savedPerson.getId());
        assertThat(personFromDB).isEmpty();
    }

    @Test
    void canDeleteMultiplePeople() throws SQLException {
        Person person1 = repo.save(new Person("test1", "jackson1", ZonedDateTime.now().withZoneSameInstant(ZoneId.of("+0"))));
        Person person2 = repo.save(new Person("test2", "jackson2", ZonedDateTime.now().withZoneSameInstant(ZoneId.of("+0"))));

        repo.delete(person1, person2);
    }

    @Test
    void canUpdate() {

        Person person = new Person("tom", "johnson", ZonedDateTime.now().withZoneSameInstant(ZoneId.of("+0")));
        Person savedPerson = repo.save(person);
        Person foundPerson = repo.findById(savedPerson.getId()).get();

        person.setFirstName("Bubu");
        person.setLastName("Mimi");
        repo.update(person);

        Person foundPerson2 = repo.findById(savedPerson.getId()).get();
        assertThat(person.getLastName())
//                .usingRecursiveAssertion()
//                .ignoringFields("dob")
                .isEqualTo(foundPerson2.getLastName());
    }

    @Test
    @Disabled
    @DisplayName("Load 5 Millions records to H2DB")
    void loadData() throws IOException, SQLException {
        Files.lines(Path.of("C://Users//kwolszczak_adm//IdeaProjects//Hr5m.csv"))
                .skip(1)
               // .limit(100)
                .map(l -> l.split(","))
                .map(a -> {
                    LocalDate dob = LocalDate.parse(a[10], DateTimeFormatter.ofPattern("M/d/yyyy"));
                    LocalTime tob = LocalTime.parse(a[11], DateTimeFormatter.ofPattern("hh:mm:ss a"));
                    LocalDateTime dtob = LocalDateTime.of(dob, tob);
                    ZonedDateTime zonedDateTime = ZonedDateTime.of(dtob, ZoneId.of("+0"));
                    Person person = new Person(a[2], a[4], zonedDateTime);
                    person.setSalary(new BigDecimal(a[25]));
                    person.setEmail(a[6]);
                    return person;
                })
                .forEach(repo::save);
        connection.commit();
    }

}
