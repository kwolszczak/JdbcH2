package dev.kwolszczak.peopledb.repository;

import dev.kwolszczak.peopledb.annotation.SQL;
import dev.kwolszczak.peopledb.model.Address;
import dev.kwolszczak.peopledb.model.CrudOperation;
import dev.kwolszczak.peopledb.model.Person;

import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class PeopleRepository extends CrudRepository<Person> {
    private static final String SAVE_PERSON_SQL = "INSERT INTO PEOPLE (FIRST_NAME, LAST_NAME, DOB) VALUES (?, ?, ?)";
    private static final String FIND_PERSON_SQL = "SELECT ID, FIRST_NAME, LAST_NAME, DOB FROM PEOPLE WHERE ID = ?";
    private static final String DELETE_PERSON_SQL = "DELETE FROM PEOPLE WHERE ID = ?";
    private static final String UPDATE_PERSON_SQL = "UPDATE PEOPLE SET FIRST_NAME=?, LAST_NAME=? WHERE ID =?";

    private AddressRepository addressRepository;

    public PeopleRepository(Connection con) {
        super(con);
        addressRepository= new AddressRepository(con);
    }

    @Override
   // @SQL(value = SAVE_PERSON_SQL , operationType = CrudOperation.SAVE)
    @SQL(value = "INSERT INTO PEOPLE (FIRST_NAME, LAST_NAME, DOB, SALARY, EMAIL, HOME_ADDRESS) VALUES (?, ?, ?, ?, ?, ?)" , operationType = CrudOperation.SAVE)
    void mapForSave(Person entity, PreparedStatement ps) throws SQLException {
        Address savedAddress = null;
        ps.setString(1, entity.getFirstName());
        ps.setString(2, entity.getLastName());
        ps.setTimestamp(3, Timestamp.valueOf(entity.getDob().withZoneSameInstant(ZoneId.of("+0")).toLocalDateTime()));
        ps.setBigDecimal(4, entity.getSalary());
        ps.setString(5, entity.getEmail());
        if (entity.getHomeAddress().isPresent()) {
            savedAddress = addressRepository.save(entity.getHomeAddress().get());
            ps.setLong(6, savedAddress.id());
        } else {
            ps.setObject(6, null);
        }
    }

    @Override
    @SQL(value = UPDATE_PERSON_SQL, operationType = CrudOperation.UPDATE)
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
    @SQL(value = FIND_PERSON_SQL, operationType = CrudOperation.FIND_BY_ID)
    Person mapForFind(ResultSet rs) throws SQLException {
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
    @SQL(value = DELETE_PERSON_SQL, operationType = CrudOperation.DELETE)
    public void delete(Person person) {
        super.delete(person);
    }

}
