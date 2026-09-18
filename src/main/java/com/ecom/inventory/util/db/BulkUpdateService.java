package com.ecom.inventory.util.db;

import com.ecom.inventory.entity.enums.CMetaInterface;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.*;
import java.lang.reflect.Field;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class BulkUpdateService<T> {

    public void bulkUpdate(Class<T> entityClass, List<T> dataList, List<String> fieldsToUpdate, HikariDataSource hikariDataSource) {
        StringBuilder sqlBuilder = new StringBuilder();

        Field[] allField = entityClass.getDeclaredFields();

        Map<String, Field> columnFieldMap = new LinkedHashMap<>();
        Field idField = null;

        for (Field field : allField) {
            Column column = field.getAnnotation(Column.class);
            if (field.isAnnotationPresent(Id.class)) {
                idField = field;
            }
            if (column != null && !column.name().equalsIgnoreCase("ID")) {
                columnFieldMap.put(column.name(), field);
            }
            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
            if (joinColumn != null) {
                columnFieldMap.put(joinColumn.name(), field);
            }
        }

        if (idField == null) {
            throw new IllegalArgumentException("Entity must have an ID field.");
        }

        sqlBuilder.append("UPDATE ");
        sqlBuilder.append(entityClass.getAnnotation(Table.class).name());
        sqlBuilder.append(" SET ");

        List<String> columnNames = new ArrayList<>();
        for (String fieldName : fieldsToUpdate) {
            for (Map.Entry<String, Field> entry : columnFieldMap.entrySet()) {
                if (entry.getValue().getName().equals(fieldName)) {
                    columnNames.add(entry.getKey());
                    break;
                }
            }
        }

        for (int i = 0; i < columnNames.size(); i++) {
            sqlBuilder.append(columnNames.get(i)).append(" = ?");
            if (i != columnNames.size() - 1) {
                sqlBuilder.append(", ");
            }
        }

        sqlBuilder.append(" WHERE ").append(idField.getAnnotation(Column.class).name()).append(" = ?");

        System.out.println(sqlBuilder);

        try (Connection connection = hikariDataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlBuilder.toString())) {

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
                        } else if (Boolean.class.equals(dataType)) {
                            // Hibernate's @Convert will handle the Boolean->Integer mapping.
                            // We can just set the Boolean directly.
                            statement.setBoolean(counter, (Boolean) value);
                        } else if (Long.class.equals(dataType)) {
                            statement.setLong(counter, (Long) value);
                        } else if (Double.class.equals(dataType)) {
                            statement.setDouble(counter, (Double) value);
                        } else if (String.class.equals(dataType)) {
                            statement.setString(counter, (String) value);
                        } else if (UUID.class.equals(dataType)) {
                            statement.setObject(counter, value, Types.OTHER);
                        } else if (Date.class.equals(dataType)) {
                            statement.setDate(counter, (Date) value);
                        } else if (java.util.Date.class.equals(dataType)) {
                            java.util.Date javaDate = (java.util.Date) value;
                            Date sqlDate = new Date(javaDate.getTime());
                            statement.setDate(counter, sqlDate);
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

                idField.setAccessible(true);
                statement.setObject(counter, idField.get(data));
                idField.setAccessible(false);

                statement.addBatch();
            }

            statement.executeBatch();
            statement.clearBatch();

        } catch (SQLException | IllegalAccessException throwables) {
            throwables.printStackTrace();
        }
    }
}