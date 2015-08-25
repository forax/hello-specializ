public class Test {
  /*
  public static void main(String[] args) {
    ArrayListBase<Object> list = new ArrayListBase<>();
    list.add("foo");
    list.add("bar");
    System.out.println(list.get(0));
  }*/
  
  public static void main(String[] args) {
    ArrayList<Integer> list = new ArrayList<>();
    for(int i = 0; i < 100_000; i++) {
      list.add(i);
    }
    System.out.println(list.size());
  }
}
