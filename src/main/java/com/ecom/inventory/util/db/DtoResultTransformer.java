/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecom.inventory.util.db;

import org.hibernate.transform.ResultTransformer;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class DtoResultTransformer implements ResultTransformer{

    private final Class targetClass;

    public DtoResultTransformer(Class targetClass) {
        this.targetClass = targetClass;
    }
    
    
//    @Override
//    public Object transformTuple(Object[] tuples, String[] aliases) {
//
//        try {
//            Object ret = targetClass.newInstance();
//
//            for(int i=0; i<tuples.length; i++){
//                Boolean found = false;
//                for(Field field: targetClass.getDeclaredFields()){
//                    if(field.getName().equalsIgnoreCase(aliases[i])){
//                        field.setAccessible(true);
//
//                        if(tuples[i] instanceof BigInteger){
//                            if(BigInteger.class.isAssignableFrom(field.getType())){
//                                field.set(ret, ((BigInteger)tuples[i]));
//                            }
//                            else if(Long.class.isAssignableFrom(field.getType())){
//                                field.set(ret, ((BigInteger)tuples[i]).longValue() );
//                            }
//                            else if(Integer.class.isAssignableFrom(field.getType())){
//                                field.set(ret, ((BigInteger)tuples[i]).intValue());
//                            }
//                            else if(String.class.isAssignableFrom(field.getType())){
//                                field.set(ret, ((BigInteger)tuples[i]).toString());
//                            }
//                            else {
//                                System.out.println("Unhandled type in DtoTransformer: BigInteger to "+tuples[i].getClass().getSimpleName()+", for field: "+aliases[i]);
//                            }
//                        }
//                        else if(tuples[i] instanceof BigDecimal){
//                            if(BigDecimal.class.isAssignableFrom(field.getType())){
//                                field.set(ret, ((BigDecimal)tuples[i]));
//                            }
//                            else if(Long.class.isAssignableFrom(field.getType())){
//                                field.set(ret, ((BigDecimal)tuples[i]).longValue() );
//                            }
//                            else if(Double.class.isAssignableFrom(field.getType())){
//                                field.set(ret, ((BigDecimal)tuples[i]).doubleValue() );
//                            }
//                            else if(Integer.class.isAssignableFrom(field.getType())){
//                                field.set(ret, ((BigDecimal)tuples[i]).intValue());
//                            }
//                            else if(String.class.isAssignableFrom(field.getType())){
//                                field.set(ret, ((BigDecimal)tuples[i]).toString());
//                            }
//                            else {
//                                System.out.println("Unhandled type in DtoTransformer: BigDecimal to "+tuples[i].getClass().getSimpleName()+", for field: "+aliases[i]);
//                            }
//                        }
//                        else if(tuples[i] instanceof Byte){
//                              if(tuples[i]==null){
//                                  field.set(ret, new Integer(0));
//                              }  else {
//                                  field.set(ret, Byte.toUnsignedInt((byte) tuples[i]));
//                              }
//
//                        }
//
//                        else if(field.getType().isAssignableFrom(Boolean.class)){
//                            if(tuples[i]!=null){
//                                String strValue = tuples[i].toString();
//                                if(strValue.equals("1") || strValue.equalsIgnoreCase("true")){
//                                    field.set(ret, Boolean.TRUE);
//                                }
//                                else {
//                                    field.set(ret, Boolean.FALSE);
//                                }
//                            }
//                        }
//                        else {
//                            field.set(ret, tuples[i]);
//                        }
//
//                        found = true;
//                        break;
//                    }
//                }
//
//                if(!found){
//                    //error case
//                    Logger.getLogger(DtoResultTransformer.class.getName()).log(Level.SEVERE, "Field not found: "+aliases[i]);
//                }
//            }
//
//            return ret;
//        } catch (InstantiationException ex) {
//            Logger.getLogger(DtoResultTransformer.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            Logger.getLogger(DtoResultTransformer.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        return null;
//    }

    @Override
    public Object transformTuple(Object[] tuples, String[] aliases) {
        try{
            Object ret = targetClass.newInstance();
            for(int i=0; i<tuples.length; i++){
                if(aliases[i] == null || aliases[i].isEmpty()){
                    continue;
                }
                boolean found = false;

                if(aliases[i].contains(".")){
                    String[] parts = aliases[i].split("\\.", 2);
                    String parentFieldName = parts[0];
                    String childFieldName = parts[1];

                    for(Field field: targetClass.getDeclaredFields()){
                        if(!field.getName().equalsIgnoreCase(parentFieldName)){
                            continue;
                        }
                        field.setAccessible(true);
                        Object childObject = field.get(ret);

                        if(childObject == null){
                            childObject = field.getType().newInstance();
                            field.set(ret, childObject);
                        }

                        for(Field childField: field.getType().getDeclaredFields()){
                            if(childField.getName().equalsIgnoreCase(childFieldName)){
                                childField.setAccessible(true);
                                setValue(childObject, childField, tuples[i]);
                                found = true;
                                break;
                            }
                        }
                        break;
                    }
                } else{
                    for(Field field: targetClass.getDeclaredFields()){
                        if(field.getName().equalsIgnoreCase(aliases[i])){
                            field.setAccessible(true);
                            setValue(ret, field, tuples[i]);
                            found = true;
                            break;
                        }
                    }
                }

                if(!found) {
                    Logger.getLogger(DtoResultTransformer.class.getName()).log(Level.SEVERE, "Field not found: " + aliases[i]);
                }
            }
            return ret;
        } catch (InstantiationException | IllegalAccessException e) {
            Logger.getLogger(DtoResultTransformer.class.getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    private void setValue(Object targetObject, Field field, Object value) throws IllegalAccessException{
        if(value instanceof BigInteger){
            if(BigInteger.class.isAssignableFrom(field.getType())){
                field.set(targetObject, ((BigInteger)value));
            } else if(Long.class.isAssignableFrom(field.getType())){
                field.set(targetObject, ((BigInteger)value).longValue() );
            } else if(Integer.class.isAssignableFrom(field.getType())){
                field.set(targetObject, ((BigInteger)value).intValue());
            } else if(String.class.isAssignableFrom(field.getType())){
                field.set(targetObject, ((BigInteger)value).toString());
            } else{
                System.out.println("Unhandled type in DtoTransformer: BigInteger to "+value.getClass().getSimpleName()+", for field: "+field.getName());
            }
        } else if(value instanceof BigDecimal){
            if(BigDecimal.class.isAssignableFrom(field.getType())){
                field.set(targetObject, ((BigDecimal)value));
            } else if(Long.class.isAssignableFrom(field.getType())){
                field.set(targetObject, ((BigDecimal)value).longValue() );
            } else if(Double.class.isAssignableFrom(field.getType())){
                field.set(targetObject, ((BigDecimal)value).doubleValue() );
            } else if(Integer.class.isAssignableFrom(field.getType())){
                field.set(targetObject, ((BigDecimal)value).intValue());
            } else if(String.class.isAssignableFrom(field.getType())){
                field.set(targetObject, ((BigDecimal)value).toString());
            } else{
                System.out.println("Unhandled type in DtoTransformer: BigDecimal to "+value.getClass().getSimpleName()+", for field: "+field.getName());
            }
        } else if(value instanceof Byte){
            field.set(targetObject, Byte.toUnsignedInt((byte) value));
        } else if(field.getType().isAssignableFrom(Boolean.class)){
            if(value!=null){
                String strValue = value.toString();
                if(strValue.equals("1") || strValue.equalsIgnoreCase("true")){
                    field.set(targetObject, Boolean.TRUE);
                } else{
                    field.set(targetObject, Boolean.FALSE);
                }
            }
        }
        else {
            field.set(targetObject, value);
        }
    }

    @Override
    public List transformList(List list) {
        return list;
    }
}
