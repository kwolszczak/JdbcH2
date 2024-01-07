package dev.kwolszczak.peopledb.repository;

import dev.kwolszczak.peopledb.annotation.SQL;
import dev.kwolszczak.peopledb.model.Address;
import dev.kwolszczak.peopledb.model.CrudOperation;

import java.sql.*;

public class AddressRepository extends CrudRepository<Address> {


    public AddressRepository(Connection connection) {
        super(connection);
    }

    @Override
    Address mapForFind(ResultSet rs) throws SQLException {
        return null;
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
