package dev.kwolszczak.peopledb.repository;

import dev.kwolszczak.peopledb.annotation.SQL;
import dev.kwolszczak.peopledb.model.Address;
import dev.kwolszczak.peopledb.model.CrudOperation;
import dev.kwolszczak.peopledb.model.Region;

import java.sql.*;

public class AddressRepository extends CrudRepository<Address> {

    private static final String FIND_ADDRESS_SQL = "SELECT ID, STREET_ADDRESS, ADDRESS2, CITY,STATE, POSTCODE,COUNTY, REGION, COUNTRY FROM ADDRESS WHERE ID = ?";
    public AddressRepository(Connection connection) {
        super(connection);
    }

    @Override
    @SQL(operationType = CrudOperation.FIND_BY_ID, value = FIND_ADDRESS_SQL)
    Address mapForFind(ResultSet rs) throws SQLException {
        Address address = null;
        long id = rs.getLong("ID");
        String streetAddress = rs.getString("STREET_ADDRESS");
        String address2 = rs.getString("ADDRESS2");
        String city = rs.getString("CITY");
        String state = rs.getString("STATE");
        String postcode = rs.getString("POSTCODE");
        String county = rs.getString("COUNTY");
        String region = rs.getString("REGION");
        String country = rs.getString("COUNTRY");

        address = new Address(streetAddress,address2,city,state,postcode,country,county, Region.valueOf(region.toUpperCase()));
        address.setId(id);
        return address;
    }

    @Override
    void mapForDelete(Statement statement, String ids) throws SQLException {

    }

    @Override
    void mapForUpdate(Address entity, PreparedStatement ps) throws SQLException {

    }

    @Override
    @SQL(value = "INSERT INTO ADDRESS (STREET_ADDRESS, ADDRESS2, CITY, STATE, POSTCODE,COUNTY,REGION, COUNTRY) VALUES (?,?,?,?,?,?,?,?);", operationType = CrudOperation.SAVE)
    void mapForSave(Address entity, PreparedStatement ps) throws SQLException {
        ps.setString(1, entity.streetAddress());
        ps.setString(2, entity.address2());
        ps.setString(3, entity.city());
        ps.setString(4, entity.state());
        ps.setString(5, entity.postcode());
        ps.setString(6, entity.county());
        ps.setString(7,entity.region().toString());
        ps.setString(8, entity.country());
    }
}
