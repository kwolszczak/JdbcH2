package dev.kwolszczak.peopledb.repository;

import dev.kwolszczak.peopledb.annotation.SQL;
import dev.kwolszczak.peopledb.model.Address;
import dev.kwolszczak.peopledb.model.CrudOperation;
import dev.kwolszczak.peopledb.model.Person;
import dev.kwolszczak.peopledb.model.Region;

import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PeopleRepository extends CrudRepository<Person> {
    private static final String FIND_PERSON_SQL = "SELECT ID, FIRST_NAME, LAST_NAME, DOB, HOME_ADDRESS FROM PEOPLE WHERE ID = ?";
    private static final String DELETE_PERSON_SQL = "DELETE FROM PEOPLE WHERE ID = ?";
    private static final String UPDATE_PERSON_SQL = "UPDATE PEOPLE SET FIRST_NAME=?, LAST_NAME=? WHERE ID =?";
    private static final String SAVE_PERSON_SQL = """
            INSERT INTO PEOPLE 
            (FIRST_NAME, LAST_NAME, DOB, SALARY, EMAIL, HOME_ADDRESS, BUSINESS_ADDRESS, PARENT_ID) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String FIND_BY_ID_SQL = """
            SELECT
            P.ID AS P_ID, P.FIRST_NAME AS P_FIRST_NAME, P.LAST_NAME AS P_LAST_NAME, P.DOB AS P_DOB, P.SALARY AS P_SALARY, P.EMAIL AS P_EMAIL, P.HOME_ADDRESS AS P_HOME_ADDRESS, P.BUSINESS_ADDRESS AS P_BUSINESS_ADDRESS,
            HOME.ID AS HOME_ID,HOME.STREET_ADDRESS AS HOME_STREET_ADDRESS,HOME.ADDRESS2 AS HOME_ADDRESS2, HOME.CITY AS HOME_CITY, HOME.STATE AS HOME_STATE, HOME.POSTCODE AS HOME_POSTCODE, HOME.COUNTY AS HOME_COUNTY, HOME.REGION AS HOME_REGION, HOME.COUNTRY AS HOME_COUNTRY,
            BIZ.ID AS BIZ_ID,BIZ.STREET_ADDRESS AS BIZ_STREET_ADDRESS,BIZ.ADDRESS2 AS BIZ_ADDRESS2, BIZ.CITY AS BIZ_CITY, BIZ.STATE AS BIZ_STATE, BIZ.POSTCODE AS BIZ_POSTCODE, BIZ.COUNTY AS BIZ_COUNTY, BIZ.REGION AS BIZ_REGION, BIZ.COUNTRY AS BIZ_COUNTRY           
            FROM PEOPLE AS P
            LEFT OUTER JOIN ADDRESS AS HOME ON P.HOME_ADDRESS =HOME.ID
            LEFT OUTER JOIN ADDRESS AS BIZ ON P.BUSINESS_ADDRESS =BIZ.ID
            WHERE P.ID = ? """;

    private AddressRepository addressRepository;
    private Map<String, Integer> aliasColIdxMap = new HashMap<>();

    public PeopleRepository(Connection con) {
        super(con);
        addressRepository = new AddressRepository(con);
    }

    @Override
    @SQL(value = SAVE_PERSON_SQL, operationType = CrudOperation.SAVE)
    void mapForSave(Person entity, PreparedStatement ps) throws SQLException {

        ps.setString(1, entity.getFirstName());
        ps.setString(2, entity.getLastName());
        ps.setTimestamp(3, Timestamp.valueOf(entity.getDob().withZoneSameInstant(ZoneId.of("+0")).toLocalDateTime()));
        ps.setBigDecimal(4, entity.getSalary());
        ps.setString(5, entity.getEmail());

        linkAddressWithPerson(entity.getHomeAddress(), ps, 6);
        linkAddressWithPerson(entity.getBusinessAddress(), ps, 7);
        linkChildWithParent(entity, ps);
    }

    @Override
    protected void postSave(Person entity) {
        entity.getChildren().stream()
                .forEach(this::save);
    }

    private static void linkChildWithParent(Person entity, PreparedStatement ps) throws SQLException {
        Optional<Person> parent = entity.getParent();
        if (parent.isPresent()) {
            ps.setLong(8, parent.get().getId());
        } else {
            ps.setObject(8, null);
        }
    }

    private void linkAddressWithPerson(Optional<Address> address, PreparedStatement ps, int parameterIndex) throws SQLException {
        Address savedAddress;
        if (address.isPresent()) {
            savedAddress = addressRepository.save(address.get());
            ps.setLong(parameterIndex, savedAddress.id());
        } else {
            ps.setObject(parameterIndex, null);
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
    @SQL(operationType = CrudOperation.FIND_BY_ID, value = FIND_BY_ID_SQL)
    Person mapForFind(ResultSet rs) throws SQLException {
        Person person = extractPerson(rs, "P_");

        Long homeAddressId = getValueByAlias(rs, "P_HOME_ADDRESS");
        Long businessAddressId = getValueByAlias(rs, "P_BUSINESS_ADDRESS");

        if (homeAddressId != null) {
            Address homeAddress = extractAddress(rs, "HOME_");
            person.setHomeAddress(homeAddress);
        }
        if (businessAddressId != null) {
            Address businessAddress = extractAddress(rs, "BIZ_");
            person.setBusinessAddress(businessAddress);
        }
        return person;
    }

    private Person extractPerson(ResultSet rs, String aliasPrefix) throws SQLException {
        long personId = 0;
        personId = getValueByAlias(rs, aliasPrefix + "ID");
        String firstName = getValueByAlias(rs, aliasPrefix + "FIRST_NAME");
        String lastName = getValueByAlias(rs, aliasPrefix + "LAST_NAME");
        ZonedDateTime dob = ZonedDateTime.of(rs.getTimestamp(aliasPrefix + "DOB").toLocalDateTime(), ZoneId.of("+0"));
        Person person = new Person(firstName, lastName, dob);
        person.setId(personId);
        return person;
    }

    private Address extractAddress(ResultSet rs, String prefix) throws SQLException {
        Address address = null;
        long id = getValueByAlias(rs, prefix + "ID");
        String streetAddress = getValueByAlias(rs, prefix + "STREET_ADDRESS");
        String address2 = getValueByAlias(rs, prefix + "ADDRESS2");
        String city = getValueByAlias(rs, prefix + "CITY");
        String state = getValueByAlias(rs, prefix + "STATE");
        String postcode = getValueByAlias(rs, prefix + "POSTCODE");
        String county = getValueByAlias(rs, prefix + "COUNTY");
        String region = getValueByAlias(rs, prefix + "REGION");
        String country = getValueByAlias(rs, prefix + "COUNTRY");

        address = new Address(streetAddress, address2, city, state, postcode, country, county, Region.valueOf(region.toUpperCase()));
        address.setId(id);
        return address;
    }

    @Override
    @SQL(value = DELETE_PERSON_SQL, operationType = CrudOperation.DELETE)
    public void delete(Person person) {
        super.delete(person);
    }

    private <T> T getValueByAlias(ResultSet rs, String alias) throws SQLException {
        int columnCount = rs.getMetaData().getColumnCount();
        int foundIdx = getIndexForAlias(rs, alias, columnCount);
        return foundIdx == 0 ? null : (T) rs.getObject(foundIdx);
    }

    private int getIndexForAlias(ResultSet rs, String alias, int columnCount) throws SQLException {
        // use mapping to cache values alias-index , to skip for loop
        int foundIdx = aliasColIdxMap.getOrDefault(alias, 0);
        if (foundIdx == 0) {
            for (int colIdx = 1; colIdx <= columnCount; colIdx++) {
                if (alias.equals(rs.getMetaData().getColumnLabel(colIdx))) {
                    foundIdx = colIdx;
                    aliasColIdxMap.put(alias, foundIdx);
                    break;
                }
            }
        }
        return foundIdx;
    }
}
