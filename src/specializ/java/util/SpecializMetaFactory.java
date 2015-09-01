package specializ.java.util;

import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;
import static java.util.Objects.requireNonNull;
import static java.util.stream.IntStream.range;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Objects;

import sun.misc.Unsafe;

public class SpecializMetaFactory {
  private static final Unsafe UNSAFE;
  static {
     try {
      Field field = Unsafe.class.getDeclaredField("theUnsafe");
      field.setAccessible(true);
      UNSAFE = (Unsafe)field.get(null);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }
  
  private static final MethodHandle newArrayInstance,
                                    arrayGets[], arraySets[], arrayCopyOfs[], equals[];
  static {
    newArrayInstance = findStatic(Array.class, "newInstance", Object.class, Class.class, int.class);
    
    VariantKind[] kinds = VariantKind.values();
    String[] arrayGetName = { "get", "getInt", "getLong", "getFloat", "getDouble" };
    arrayGets = range(0, kinds.length).mapToObj(i -> 
      findStatic(Array.class, arrayGetName[i], kinds[i].type, Object.class, int.class)).toArray(MethodHandle[]::new);
    String[] arraySetName = { "set", "setInt", "setLong", "setFloat", "setDouble" };
    arraySets = range(0, kinds.length).mapToObj(i -> 
      findStatic(Array.class, arraySetName[i], void.class, Object.class, int.class, kinds[i].type)).toArray(MethodHandle[]::new);
    arrayCopyOfs = range(0, kinds.length).mapToObj(i -> 
      findStatic(Arrays.class, "copyOf", kinds[i].arrayType, kinds[i].arrayType, int.class)).toArray(MethodHandle[]::new);
    equals = range(0, kinds.length).mapToObj(i -> 
      findStatic(SpecializMetaFactory.class, "equals", boolean.class, kinds[i].type, kinds[i].type)).toArray(MethodHandle[]::new);
  }
  
  private static MethodHandle findStatic(Class<?> type, String name, Class<?> returnType, Class<?>... parameterTypes) {
    try {
      return publicLookup().findStatic(type, name, methodType(returnType, parameterTypes));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }
  
  public static boolean equals(Object o1, Object o2) {
    return Objects.equals(o1, o2);
  }
  public static boolean equals(int i1, int i2) {
    return i1 == i2;
  }
  public static boolean equals(long l1, long l2) {
    return l1 == l2;
  }
  public static boolean equals(float f1, float f2) {
    return f1 == f2;
  }
  public static boolean equals(double d1, double d2) {
    return d1 == d2;
  }
  
  public static CallSite bsm(Lookup lookup, String name, MethodType type, String variant) {
    VariantKind kind = VariantKind.valueOf(variant.substring(0, 1));  // FIXME if several arguments !
    MethodHandle mh;
    switch(name) {
    case "newArrayInstance":
      mh = MethodHandles.insertArguments(newArrayInstance, 0, kind.type);
      break;
    case "arrayGet":
      mh = arrayGets[kind.ordinal()];
      break;
    case "arraySet":
      mh = arraySets[kind.ordinal()];
      break;
    case "arrayCopyOf":
      mh = arrayCopyOfs[kind.ordinal()];
      break;
    case "equals":
      mh = equals[kind.ordinal()];
      break;
    default:
      throw new LinkageError("bsm from " + lookup.lookupClass().getName() + " " + name + type + " variant:" + variant);
    }
    
    return new ConstantCallSite(mh.asType(type));
  }
  
  enum VariantKind {
    L(Object.class, Object[].class),
    I(int.class, int[].class),
    J(long.class, long[].class),
    F(float.class, float[].class),
    D(double.class, double[].class);
    
    final Class<?> type;
    final Class<?> arrayType;
    
    private VariantKind(Class<?> type, Class<?> arrayType) {
      this.type = type;
      this.arrayType = arrayType;
    }
    
    String getReplacementDesc() {
      if (this == L) {
        return "Ljava/lang/Object;";
      }
      return name();
    }
  }
  
  static class GenericClass {
    private final byte[] code;
    private final EnumMap<VariantKind, Class<?>> variantMap = new EnumMap<>(VariantKind.class);
    
    GenericClass(byte[] code) {
      this.code = requireNonNull(code);
    }
    
    public Class<?> getVariant(VariantKind kind) {
      return variantMap.computeIfAbsent(kind, this::specialize);
    }
    
    private static final byte UTF8_CONSTANT_POOL_TAG = 1;
    
    private Class<?> specialize(VariantKind kind) {
      // do the translation for the first constant pool entries !
      byte[] code = this.code;
      
      int entryCount = ((code[8] & 0xFF) << 8) | (code[9] & 0xFF);
      Object[] constants = new Object[entryCount];
      
      int index = 10;
      for(int i = 1; i < entryCount; i++) {
        if (code[index] == UTF8_CONSTANT_POOL_TAG) {
          int length = ((code[index + 1] & 0xFF) << 8) | (code[index + 2] & 0xFF); 
          
          //FIXME write a UTF8 decoding method !
          String key;
          try {
            key = new DataInputStream(new ByteArrayInputStream(code, index + 1, code.length - index - 1)).readUTF();
          } catch (IOException e) {
            throw new AssertionError(e);
          }
          
          if (key.charAt(0) == '$') {
            constants[i] = key.substring(1).replace("E", kind.getReplacementDesc());
            index += 1 + 2 + length;
            
            //System.out.println("key " + key + " is substituted by " + constants[i]);
            continue;
          }
        }
        break;
      }
      
      //System.out.println(Arrays.toString(constants));
      
      return UNSAFE.defineAnonymousClass(SpecializMetaFactory.class /* TODO revisit*/, code, constants);
    }
    
    static GenericClass create(Class<?> clazz, String name) {
      byte[] code = new byte[8192];
      int offset = 0;
      try(InputStream input = clazz.getClassLoader().getResourceAsStream(name + ".class")) {
        int read;
        while((read = input.read(code, offset, code.length - offset)) != -1) {
          offset += read;
          if (offset > code.length >>> 1) {
            code = Arrays.copyOf(code, code.length << 1);
          }
        }
      } catch (IOException e) {
        throw (LinkageError)new LinkageError().initCause(e);
      }
      return new GenericClass(Arrays.copyOf(code, offset)); // shrink to the right size
    }
  }
  
  private static final HashMap<String, GenericClass> GENERIC_MAP = new HashMap<>();
  
  public static CallSite indy(Lookup lookup, String name, MethodType type, String generics, String arguments) throws NoSuchMethodException, IllegalAccessException {
    VariantKind kind = VariantKind.valueOf(arguments); // FIXME if several arguments !
    Class<?> variant = GENERIC_MAP.computeIfAbsent(generics, className -> GenericClass.create(lookup.lookupClass(), className)).getVariant(kind);
    MethodHandle mh;
    if ("new".equals(name)) {
      mh = lookup.findConstructor(variant, type.changeReturnType(void.class)); 
    } else {
      mh = lookup.findVirtual(variant, name, type.dropParameterTypes(0, 1));
    }
    return new ConstantCallSite(mh.asType(type));
  }
}
