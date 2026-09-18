package com.ecom.inventory.util.db;

import com.ecom.inventory.entity.enums.CMetaInterface;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import org.springframework.jdbc.datasource.DataSourceUtils;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.sql.Date;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BulkInsertService<T> {

    public void bulkInsert(Class<T> entityClass, List<T> dataList, HikariDataSource hikariDataSource) {
        StringBuilder sqlBuilder = new StringBuilder();

        Field[] allField = entityClass.getDeclaredFields();

        Map<String, Field> columnFieldMap = new LinkedHashMap<>();
        for (Field field : allField) {
            Column column = field.getAnnotation(Column.class);
            if (column != null && !column.name().equalsIgnoreCase("ID")) {
                columnFieldMap.put(column.name(), field);
            }
            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
            if (joinColumn != null) {
                columnFieldMap.put(joinColumn.name(), field);
            }
        }

        sqlBuilder.append("INSERT INTO ");
        sqlBuilder.append(entityClass.getAnnotation(Table.class).name());
        sqlBuilder.append(" ( ");
        List<String> columnNames = new ArrayList<>(columnFieldMap.keySet());
        for (int i = 0; i < columnNames.size(); i++) {
            sqlBuilder.append(columnNames.get(i));
            if (i != columnNames.size() - 1) {
                sqlBuilder.append(", ");
            } else {
                sqlBuilder.append(") ");
            }
        }
        sqlBuilder.append(" values ( ");

        for (int i = 0; i < columnNames.size(); i++) {
            sqlBuilder.append("?");
            if (i != columnNames.size() - 1) {
                sqlBuilder.append(", ");
            } else {
                sqlBuilder.append(") ");
            }
        }

        System.out.println(sqlBuilder);

        Connection connection = DataSourceUtils.getConnection(hikariDataSource);
        try (PreparedStatement statement = connection.prepareStatement(sqlBuilder.toString())) {
            for (T data : dataList) {
                int counter = 1;
                for (String columnName : columnNames) {
                    Field field = columnFieldMap.get(columnName);
                    field.setAccessible(true);
                    Object value = field.get(data);

                    if (value == null) {
                        statement.setNull(counter, Types.NULL);
                    } else {
                        Class<?> dataType = field.getType();
                        if (Integer.class.equals(dataType)) {
                            statement.setInt(counter, (Integer) value);
                        } else if (Boolean.class.equals(dataType) || boolean.class.equals(dataType)) {
                            // Hibernate's @Convert (e.g. NumericBooleanConverter) handles the
                            // Boolean -> Integer mapping at the JDBC layer. Setting a Boolean
                            // here is sufficient and portable.
                            statement.setBoolean(counter, (Boolean) value);
                        } else if (Long.class.equals(dataType)) {
                            statement.setLong(counter, (Long) value);
                        } else if (Double.class.equals(dataType)) {
                            statement.setDouble(counter, (Double) value);
                        } else if (String.class.equals(dataType)) {
                            statement.setString(counter, (String) value);
                        } else if (UUID.class.equals(dataType)) {
                            statement.setObject(counter, value, Types.OTHER);
                        } else if (java.util.Date.class.equals(dataType)) {
                            java.util.Date javaDate = (java.util.Date) value;
                            Date sqlDate = new Date(javaDate.getTime());
                            statement.setDate(counter, sqlDate);
                        } else if (Date.class.equals(dataType)) {
                            statement.setDate(counter, (Date) value);
                        } else if (Time.class.equals(dataType)) {
                            statement.setTime(counter, (Time) value);
                        } else if (Timestamp.class.equals(dataType)) {
                            statement.setTimestamp(counter, (Timestamp) value);
                        } else if (Enum.class.equals(dataType.getSuperclass())) {
                            statement.setString(counter, ((CMetaInterface) value).getValue());
                        } else if (dataType.isAnnotationPresent(Entity.class)) {
                            Field[] dataTypeField = dataType.getDeclaredFields();
                            for (Field field1 : dataTypeField) {
                                if (field1.isAnnotationPresent(Id.class)) {
                                    field1.setAccessible(true);
                                    statement.setLong(counter, (Long) field1.get(value));
                                    field1.setAccessible(false);
                                    break;
                                }
                            }
                        }
                    }
                    field.setAccessible(false);
                    counter++;
                }
                statement.addBatch();
            }

            statement.executeBatch();
            statement.clearBatch();

        } catch (SQLException | IllegalAccessException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException(throwables);
        } finally {
            DataSourceUtils.releaseConnection(connection, hikariDataSource);
        }
    }
}