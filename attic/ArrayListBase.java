package specializ;

import java.lang.reflect.Array;
import java.util.Arrays;

public class ArrayListBase</*any*/ E> {
  private Object data;
  private int size;
  
  public ArrayListBase() {
    data = Array.newInstance(Object.class, 16);
  }
  
  public void add(/*as any*/E e) {
    if (Array.getLength(data) == size) {
      resize();
    }
    Array.set(data, size++, e);
  }
  
  private void resize() {
    data = Arrays.copyOf(/*FIXME*/(Object[])data, size << 1);
  }
  
  @SuppressWarnings("unchecked")
  public /*as any*/E get(int index) {
    return (E)Array.get(data, index);
  }
  
  public void set(int index, /*as any*/E e) {
    Array.set(data, index, e);
  }
  
  /*public boolean contains(E e) {
    for(int i = 0; i < size; i++) {
      if (e.equals(Array.get(data, i))) {
        return true;
      }
    }
    return false;
  }*/
  
  long foo;
  
  public boolean contains(long e) {
    for(int i = 0; i < size; i++) {
      if (e == foo) {
        return true;
      }
    }
    return false;
  }
}
