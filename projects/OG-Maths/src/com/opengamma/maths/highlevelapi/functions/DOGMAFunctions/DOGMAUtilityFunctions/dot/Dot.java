/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMAUtilityFunctions.dot;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.maths.commonapi.exceptions.MathsExceptionNotImplemented;
import com.opengamma.maths.commonapi.exceptions.MathsExceptionNullPointer;
import com.opengamma.maths.highlevelapi.datatypes.primitive.OGArraySuper;
import com.opengamma.maths.highlevelapi.datatypes.primitive.OGDoubleArray;
import com.opengamma.maths.highlevelapi.datatypes.primitive.OGSparseArray;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class Dot {
  /**
   * hashmapped function pointers
   */
  private static Map<Pair<?, ?>, DotAbstract<?, ?>> s_functionPointers = new HashMap<Pair<?, ?>, DotAbstract<?, ?>>();
  static {
    ObjectsPair<Class<?>, Class<?>> pairOGDoubleArrayOGDoubleArray = new ObjectsPair<Class<?>, Class<?>>(OGDoubleArray.class, OGDoubleArray.class);
    ObjectsPair<Class<?>, Class<?>> pairOGDoubleArrayOGSparseArray = new ObjectsPair<Class<?>, Class<?>>(OGDoubleArray.class, OGSparseArray.class);
    ObjectsPair<Class<?>, Class<?>> pairOGSparseArrayOGDoubleArray = new ObjectsPair<Class<?>, Class<?>>(OGSparseArray.class, OGDoubleArray.class);
    ObjectsPair<Class<?>, Class<?>> pairOGSparseArrayOGSparseArray = new ObjectsPair<Class<?>, Class<?>>(OGSparseArray.class, OGSparseArray.class);    
    s_functionPointers.put(pairOGDoubleArrayOGDoubleArray, DotOGDoubleArrayOGDoubleArray.getInstance());
//    s_functionPointers.put(pairOGDoubleArrayOGSparseArray, DotOGDoubleArrayOGSparseArray.getInstance());
//    s_functionPointers.put(pairOGSparseArrayOGDoubleArray, DotOGSparseArrayOGDoubleArray.getInstance());         
//    s_functionPointers.put(pairOGSparseArrayOGSparseArray, DotOGSparseArrayOGSparseArray.getInstance());        
  }

  @SuppressWarnings("unchecked")
  public <T extends OGArraySuper<Number>, S extends OGArraySuper<Number>> OGArraySuper<Number> dot(T array1, S array2) {
    if (array1 == null) {
      throw new MathsExceptionNullPointer("Null pointer passed in argument 1");
    }
    if (array2 == null) {
      throw new MathsExceptionNullPointer("Null pointer passed in argument 2");
    }    
    ObjectsPair<Class<?>, Class<?>> combo = new ObjectsPair<Class<?>, Class<?>>(array1.getClass(), array2.getClass());
    DotAbstract<T, S> use = (DotAbstract<T, S>) s_functionPointers.get(combo);
    
    if (use == null) {
      throw new MathsExceptionNotImplemented("Dot array class " + array1.getClass().toString() + " and " + array2.getClass().toString() + " is not yet implemented");
    }
    return use.dot(array1, array2);
  }
}