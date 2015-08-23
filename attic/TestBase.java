package specializ.java.util;

public class TestBase {
  public static void main(String[] args) {
    ArrayListBase<Object> list = new ArrayListBase<>();
    list.add("foo");
    list.add("bar");
    System.out.println(list.get(0));
  }
}
