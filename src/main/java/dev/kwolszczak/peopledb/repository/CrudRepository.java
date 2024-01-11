package dev.kwolszczak.peopledb.repository;

import dev.kwolszczak.peopledb.annotation.Id;
import dev.kwolszczak.peopledb.annotation.MultiSQL;
import dev.kwolszczak.peopledb.annotation.SQL;
import dev.kwolszczak.peopledb.exception.UnableToSaveException;
import dev.kwolszczak.peopledb.model.CrudOperation;
import dev.kwolszczak.peopledb.model.Entity;

import java.sql.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class CrudRepository<T extends Entity> {

    protected Connection connection;
    private PreparedStatement savePS;
    private PreparedStatement findByIdPS;
    private PreparedStatement updatePS;
    private PreparedStatement deletePS;

    public CrudRepository(Connection connection) {
        try {
            this.connection = connection;
            findByIdPS = connection.prepareStatement(getSQLFromAnnotation(CrudOperation.FIND_BY_ID));
            updatePS = connection.prepareStatement(getSQLFromAnnotation(CrudOperation.UPDATE));
            deletePS = connection.prepareStatement(getSQLFromAnnotation(CrudOperation.DELETE));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public T save(T entity) {
        try {
            savePS = connection.prepareStatement(getSQLFromAnnotation(CrudOperation.SAVE), Statement.RETURN_GENERATED_KEYS);
            mapForSave(entity, savePS);

            int recordsAffected = savePS.executeUpdate();
            ResultSet rs = savePS.getGeneratedKeys();
            while (rs.next()) {
                long id = rs.getLong(1);
                entity.setId(id);
                postSave(entity);
            }
            //  System.out.println(entity);
            // System.out.println(STR."Records affected: \{recordsAffected}");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new UnableToSaveException(STR."Tried to save entity:\{entity}");
        }
        return entity;
    }


    public Optional<T> findById(Long id) {
        T entity = null;
        try {
            findByIdPS.setLong(1, id);
            ResultSet rs = findByIdPS.executeQuery();
            while (rs.next()) {
                entity = mapForFind(rs);
            }
            System.out.println(entity);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.ofNullable(entity);
    }


    public void update(T entity) {
        try {
            mapForUpdate(entity, updatePS);
            updatePS.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Long getIdByAnnotation(T entity) {
        return Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .map(field -> {
                    try {
                        field.setAccessible(true);
                        return field.getLong(entity);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                .findFirst().orElseThrow(() -> new RuntimeException("No ID annotated with @ID"));
    }

    public void delete(T entity) {
        try {
            deletePS.setLong(1, getIdByAnnotation(entity));
            boolean rs = deletePS.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void delete(T... entities) throws SQLException {
        String ids = Arrays.stream(entities)
                .map(T::getId)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        Statement statement = connection.createStatement();
        mapForDelete(statement, ids);
/*        for (var person : entities) {
//            delete(entities);
        }*/
    }

    /**
     * @param rs
     * @return Returns a String that represents the SQL needed to retrieve one entity.
     * The SQL must contain one SQL parameter, i.e. "?", that will bind to the entity's ID
     */
    abstract T mapForFind(ResultSet rs) throws SQLException;

    abstract void mapForDelete(Statement statement, String ids) throws SQLException;

    abstract void mapForUpdate(T entity, PreparedStatement ps) throws SQLException;

    abstract void mapForSave(T entity, PreparedStatement ps) throws SQLException;

    protected void postSave(T entity) {
    }

    private String getSQLFromAnnotation(CrudOperation operationType) {

        //for method with 2 or more Annotations @SQL
        Stream<SQL> multiSqlStream = Arrays.stream(this.getClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(MultiSQL.class))
                .map(method -> method.getAnnotation(MultiSQL.class))
                .flatMap(multiSQL -> Arrays.stream(multiSQL.value()));

        //for method with only one Annotation @SQL
        Stream<SQL> sqlStream = Arrays.stream(this.getClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(SQL.class))
                .map(method -> method.getAnnotation(SQL.class));

        return Stream.concat(sqlStream, multiSqlStream)
                .filter(sql -> sql.operationType().equals(operationType))
                .map(SQL::value)
                .findFirst().orElse("");
    }

}
