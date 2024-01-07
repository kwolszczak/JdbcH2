package loadData;

import dev.kwolszczak.peopledb.model.Person;
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

class LoadData {


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
