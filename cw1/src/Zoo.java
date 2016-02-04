public class Zoo {
//Mihajlo is bored.
  public static void main(String[] args) {
    System.out.println("Whoop! You have opened the zoo for business.");
  }

  //tst comment from Tudor
  public String feed(Animal animal, Food food) {
      return animal.eat(food);
    //return food.eaten(animal);
  }
}
