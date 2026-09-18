/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecom.inventory.util.db;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.*;

/**
 * @author LENOVO
 */
public class NestedPojoTransformer {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T, C> List<T> transformList(Class<T> parentClass, Class<C> childClass, List<Object[]> resultList, String[] aliases, String parentAlias) throws PojoTransformerException {
        if (resultList == null || resultList.size() == 0) {
            return new ArrayList<T>();
        }
        if (resultList.get(0).length != aliases.length) {
            //throw new PojoTransformerException("The number of values and aliases must be the same.");
        }
        Map<Object, T> parentMap = new HashMap();
        Map<Object, List> childMap = new HashMap<>();
        List<T> genericList = new ArrayList<T>();
        List<List<String>> aliasesList = getAliases(aliases);
        Integer parentAliasIndex = findParentAliasIndex(aliases, parentAlias);
        T nullParentObject = null;
        List nullChildList = new ArrayList();
        String childAlias = findChildFieldName(aliasesList);
        Field childField = null;
        try {
            childField = parentClass.getDeclaredField(childAlias);
            childField.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new PojoTransformerException(ex.getMessage());
        }
        try {
            for (Object[] objList : resultList) {
                Object parentId = objList[parentAliasIndex];    //parent alias should be the identifying field of the parent object
                T t = null;
                C c = null;
                List childList = null;
                if (parentId == null) {
                    if (nullParentObject == null) {
                        nullParentObject = (T) parentClass.newInstance();
                        childField.set(nullParentObject, nullChildList);
                        genericList.add(t);
                    }
                    t = nullParentObject;
                    childList = nullChildList;
                } else {
                    t = parentMap.get(parentId);
                    if (t == null) {
                        t = (T) parentClass.newInstance();
                        parentMap.put(parentId, t);
                        genericList.add(t);
                    }

                    childList = childMap.get(parentId);
                    if (childList == null) {
                        childList = new ArrayList();
                        childField.set(t, childList);
                        childMap.put(parentId, childList);
                    }
                }

                for (int i = 0; i < objList.length; i++) {
                    //assignValues(t, c, aliasesList.get(i), parentAlias, objList[i]);
                    ArrayList<String> aliasesCopy = new ArrayList<String>(aliasesList.get(i));
                    Field f;
                    if (aliasesCopy.size() > 1) {//check if it is the attribute of child, it will have conjugate alias like parentFieldName.childFieldName
                        if (String.class.isAssignableFrom(childClass)
                                || Integer.class.isAssignableFrom(childClass)
                                || Double.class.isAssignableFrom(childClass)
                                || BigInteger.class.isAssignableFrom(childClass)
                                || Long.class.isAssignableFrom(childClass)) {
                            if (objList[i] == null) {
                                childList.add(null);
                            } else {
                                childList.add(objList[i]);
                            }
                        } else if (Boolean.class.isAssignableFrom(childClass)) {
                            if (objList[i] == null) {
                                childList.add(null);
                            } else {
                                if (objList[i].toString().equals("1")) {
                                    childList.add(Boolean.TRUE);
                                } else {
                                    childList.add(Boolean.FALSE);
                                }
                            }
                        } else {
                            if (c == null) {
                                c = (C) childClass.newInstance();
                                childList.add(c);
                            }
                            if (objList[i] != null) {
                                f = childClass.getDeclaredField(aliasesCopy.get(1));
                                f.setAccessible(true);

                                if (Boolean.class.isAssignableFrom(f.getType())) {
                                    if (objList[i].toString().equals("1")) {
                                        f.set(c, Boolean.TRUE);
                                    } else {
                                        f.set(c, Boolean.FALSE);
                                    }
                                } 
                                else if(BigInteger.class.isAssignableFrom(objList[i].getClass())){
                                    if(Long.class.isAssignableFrom(f.getType())){
                                        f.set(c, ((BigInteger)objList[i]).longValue());
                                    }
                                    else if(Integer.class.isAssignableFrom(f.getType())){
                                        f.set(c, ((BigInteger)objList[i]).intValue());
                                    }
                                    else if(String.class.isAssignableFrom(f.getType())){
                                        f.set(c, ((BigInteger)objList[i]).toString());
                                    }
                                    else {
                                        f.set(c, objList[i]);
                                    }
                                }
                                else {
                                    //System.out.println(f.getName()+", "+objList[i].getClass().getSimpleName()+": "+objList[i]);
                                    f.set(c, objList[i]);
                                }
                            }
                        }
                    } else {
                        if (objList[i] != null) {
                            f = parentClass.getDeclaredField(aliasesCopy.get(0));
                            f.setAccessible(true);
                            if (Boolean.class.isAssignableFrom(f.getType())) {
                                if (objList[i].toString().equals("1")) {
                                    f.set(t, Boolean.TRUE);
                                } else {
                                    f.set(t, Boolean.FALSE);
                                }
                            }
                            else if(BigInteger.class.isAssignableFrom(objList[i].getClass())){
                                if(Long.class.isAssignableFrom(f.getType())){
                                    f.set(t, ((BigInteger)objList[i]).longValue());
                                }
                                else if(Integer.class.isAssignableFrom(f.getType())){
                                    f.set(t, ((BigInteger)objList[i]).intValue());
                                }
                                else if(String.class.isAssignableFrom(f.getType())){
                                    f.set(t, ((BigInteger)objList[i]).toString());
                                }
                                else {
                                    f.set(t, objList[i]);
                                }
                            }
                            else {
                                f.set(t, objList[i]);
                            }
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new PojoTransformerException(e);
        }
        return genericList;
    }

    private static Integer findParentAliasIndex(String[] aliases, String parentAlias) throws PojoTransformerException {
        if (parentAlias == null || parentAlias.isEmpty()) {
            throw new PojoTransformerException("parentAlias must be non-empty");
        }
        for (int i = 0; i < aliases.length; i++) {
            if (aliases[i].equals(parentAlias)) {
                return i;
            }
        }
        throw new PojoTransformerException("parentAlias must be present in aliases");
    }

    private static String findChildFieldName(List<List<String>> sortedAliases) throws PojoTransformerException {
        for (List<String> aliases : sortedAliases) {
            if (aliases.size() > 1) {
                return aliases.get(0);
            }
        }
        throw new PojoTransformerException("Nested object must have at least one child aliases defined as parentFieldName.childFieldName");
    }

    private static List<List<String>> getAliases(String[] aliases) {
        List<List<String>> list = new ArrayList<List<String>>();
        List<String> _list = new ArrayList<String>();
        for (String string : aliases) {
            _list = new ArrayList<String>(Arrays.asList(string.split("\\.")));
            list.add(_list);
        }
        return list;
    }

    @SuppressWarnings({"unchecked"})
    private static <T> Object assignValues(Object parentObj, Object childObj, Class childClass, final List<String> aliases, String parentAlias, Object value) throws NoSuchFieldException, SecurityException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        ArrayList<String> aliasesCopy = new ArrayList<String>(aliases);
        Field f;
        if (aliasesCopy.size() > 1) {//check if it is the attribute of child, it will have conjugate alias like parentFieldName.childFieldName

            f = childObj.getClass().getDeclaredField(aliasesCopy.get(1));
            f.setAccessible(true);
            f.set(childObj, value);
        } else {
            f = parentObj.getClass().getDeclaredField(aliasesCopy.get(0));
            f.setAccessible(true);
            f.set(parentObj, value);
        }

        return parentObj;
    }
}
