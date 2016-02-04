# Coursework 1, A trip to the zoo

Welcome to Java and the world of object-oriented programming! In this project we will be learning how to compile and run Java programs as well as introducing you to inheritance.

We have provided a skeleton project for you to work on. The project includes a set of `.java` files and a testing suite which we will be using to ensure our project does what it is meant to!

## Compiling

We will begin by compiling the project. Java provides a compiler (`javac`) and an interpreter (`java`) as command line applications. The lab computers already have Java installed and you can check if your own system does by running:

```
java -version
```

If Java is installed the output should be something similar to this:

```
java version “1.8.0_45”
Java(TM) SE Runtime Environment (build 1.8.0_45-b15)
Java HotSpot(TM) 64-Bit Server VM (build 25.45-b02, mixed mode)
```

If you get a message saying something along the lines of `java command not recognized`, follow [this guide](https://www.java.com/en/download/help/download_options.xml) to install the java platform.

Now you have Java installed, we can compile our program. We do this by moving into the `src` directory and running the `javac` command on `Zoo.java`. This will compile `Zoo.java` to a new file, `Zoo.class`, which contains the Java bytecode used by the Java interpreter to run your program. You will notice that it also compiles all dependent files such as `Animal.java` and `Food.java`. More information on the `javac` tool can be found here:

* [windows](http://docs.oracle.com/javase/7/docs/technotes/tools/windows/javac.html).
* [linux](http://docs.oracle.com/javase/7/docs/technotes/tools/solaris/javac.html).

Now we can run the program using the `java` command on `Zoo`. This tells the interpreter to look for the `.class` file which contains the bytecode of `Zoo.java`. More information on the `java` tool can be found here:

* [windows](http://docs.oracle.com/javase/7/docs/technotes/tools/windows/java.html).
* [linux](http://docs.oracle.com/javase/7/docs/technotes/tools/solaris/java.html).
***
**TODO:** Compile `Zoo.java` and run `Zoo.class` by running these commands from inside the directory you just downloaded (You should get a friendly message when you run the program):
```
cd src
javac Zoo.java
java Zoo
```
***
#Testing
Before we start properly, we need to take a segway into a testing framework called JUnit. We use tests when writing our program to highlight mistakes and verify that our code is functioning correctly. This may seem trivial but it can be very helpful, particularly in large projects where changing one thing can have unintended side effects.

JUnit tests are contained in normal classes, the difference is that we use special annotations and methods to let JUnit know what to run. Lets look at `test1` in `test/Tests.java`:
```java
@Test
public void test1() {
  Zoo zoo = new Zoo();
  Animal animal = new Animal();
  Food food = new Food();

  String output = zoo.feed(animal, food);

  assertEquals("Feeding Food to an Animal:", "animal eats food", output);
}
```
The `@Test` annotation before the method lets JUnit know that this method is in fact a test and that it should be run as such. We can of course have helper methods (and classes!) that are not tests and it wouldn’t make sense for JUnit to run them directly.

We set up this test by creating some new objects, a `Zoo`, `Animal` and `Food`. We then call the `feed` method in the `zoo` object which returns a string.

The last line of the method uses a a function called `assertEquals`. This takes three arguments, a message to tell us about the test, the value that we expect and the value that we are actually receiving. If the latter two values we pass in are not equal then JUnit informs us that we are failing the test.

For more information on JUnit's assert methods go [Here](http://junit.org/apidocs/org/junit/Assert.html).

To use the tests you must compile and run `TestRunner.java`. The commands to do this look slightly different to those we have been using but do not fear! The only difference is that we are now compiling files from a number of locations and as such we must let `javac` know where these locations are. The `-cp` flag (which stands for *Classpath*) does exactly this, we provide it with all the folders that the compiler should look in when compiling our code. For our project we include:

* src: Contains all the classes we will be using with `Zoo.java`.
* test: Contains all the tests we will be running.
* lib/junit-4.7.jar: Contains the JUnit library for running the tests.

We also want to recompile the files in src when we run the test so that any changes we have made are applied in the tests. To do this we add `src/*.java` to the list of source files.
***
**TODO:** Move back to the root directory and compile the tests using the commands:

Unix:
```
cd ..
javac -cp src:test:lib/junit-4.7.jar test/TestRunner.java src/*.java
```
Windows:
```
cd ..
javac -cp src;test;lib/junit-4.7.jar test/TestRunner.java src/*.java
```
***
To run `TestRunner` we must again specify our classpath. Notice that when compiling we specify the source files (including their relative location) but when running we just specify the class name.
***
**TODO:** Run the tests using the command:

Unix:
```
java -cp src:test:lib/junit-4.7.jar TestRunner
```
Windows:
```
java -cp src;test;lib/junit-4.7.jar TestRunner
```
***
## The Project

The Zoo class that we were compiling earlier contains a `feed()` method which the provided tests call. There are also a number of other classes in the `src` folder which Zoo makes use of. Some of these classes are incomplete and it will be your task to complete them so that the project passes the tests.

### Part 1

Let's begin by looking at the output that running the tests produces:
```
test1(Tests):
Feeding Food to an Animal: expected:<animal eats food> but was:<null>
test2(Tests):
Feeding Food to a Dog: expected:<dog eats food> but was:<null>
test3(Tests):
Feeding Foods to Animals: expected:<animal eats food> but was:<null>
```
We are currently failing all the tests (not for long!). When an `assertEquals` fails, JUnit gives us the name of the test that failed and then tells us the value it expected and the value it has received. Note that the tests are not run in any particular order so your output may be slightly different.

Test 1 expects the output `animal eats food` but it is currently getting `null`. Let's look at the test again:
```java
Zoo zoo = new Zoo();
Animal animal = new Animal();
Food food = new Food();

String output = zoo.feed(animal, food);

assertEquals("Feeding Food to an Animal:", "animal eats food", output);
```
You can see that this test calls the method `feed` with an instance of `Animal` and `Food`. Take a look at the `Animal` and `Food` classes and modify the definition of `feed` so that your code passes this test. Make sure that you use each of the methods in the `Food` *and* `Animal` classes to do this. *Hint: you will need to implement the feed method in `Zoo`, probably using only one line.*
***
**TODO:** Using the methods in `Animal` and `Food`, modify the `feed` method in `Zoo.java` so that the first test passes (you will need to recompile each time you make a change).
***

### Part 2
```java
Zoo zoo = new Zoo();
Dog scooby = new Dog();
Food food = new Food();

String output = zoo.feed(scooby, food);

assertEquals("Feeding Food to a Dog:", "dog eats food", output);
```
The second test is very similar to the Test 1 but instead of calling feed with a generic `Animal` it instead calls feed with an instance of `Dog`. But hang on, feed takes an `Animal` and a `Food`, how can we call it with an instance of `Dog`?

To understand what’s happening here we need to look at a core idea in java called *inheritance*. If we move out of java land for a moment and think about real world animals, there are certain attributes that all animals have such as a species, and certain things they can do such as make a noise or eat. Specific animals *inherit* these attributes simply by being animals but they also have certain things that they might not share with all animals. A dog, for example, can bark but an eel probably couldn't.

We can represent this in java by having a hierarchy of classes, those lower down inherit all the attributes and methods from the ones above. Our project has two hierarchies, illustrated below:

![Class Hierarchy Image](https://bytebucket.org/bdkmilne/coursework-1-zoo/raw/8bc8bf12cd9e726deb6b7b7b1e685aca25a95d47/Hierachy.png?token=d7326366884afb718262c9dae6fb3757d38f4d1d)

We can see that our `Dog` class inherits from `Animal` and indeed, if we look inside `Dog`, after the class declaration there is a short statement, `extends Animal` which tells the compiler that this relationship exists.
```
public class Dog extends Animal {
  //TODO:
}
```
As well as inheriting all the attributes and methods declared in `Animal`, this means we can treat it as if it were an animal (which of course it is) and pass it into feed no problem!

So we have passed an instance of `Dog` into `feed` but if you look at the result of test 2 it expects the output `dog eats food` and we are currently printing `animal eats food`. This is because the `eat` method is being called in `Animal` (which `Dog` inherits from remember). We can stop this from happening by having a method inside `Dog` which has the exact same signature as the one in `Animal`:
```java
@Override
public String eat(Food food) {
    return food.eaten(this);
}
```
This is known as *overriding*, the program sees that the animal we are calling the method `eat` on is of type `Dog` and that `Dog` does indeed have a method that matches `eat`. The program then makes sure to call the method in `Dog` rather than the one in `Animal`. Notice the `@Override`, this is not required but it tells the compiler that we are intending to *override* a method and that it should give us an error if we do this incorrectly - for example if we don't match the method signature exactly.
***
**TODO:** Override `Animal`'s eat method in `Dog`.
***
Now the eat method is being called in `Dog` but once again, we are calling the `eaten` method in `Food`, producing the output `animal eats food`. We still don’t pass the test. The difference now is that the `Animal` passed into the `eaten` method is of type `Dog`, we can use this fact by implementing another method in `Food`:
```java
public String eaten(Dog dog) {
    return "dog eats food";
}
```
This looks suspiciously like the existing `eaten` method but instead of taking an argument of type `Animal` (or something that inherits from `Animal`), it only takes an argument of type `Dog`. But hold on, `Dog` inherits from `Animal` so which method is called? They both have the same name and they both accept a single argument of type `Dog`. This is a big topic we call *dispatch* but essentially the method that takes `Dog` is called as it is closest in the hierarchy to the argument we are passing in. Having multiple methods named the same that take different arguments is known as *overloading*.
***
**TODO:** Overload the `eaten` method in `Food` with one that takes a `Dog`. Your code should now pass the second test.
***

### Part 3

The final test feeds every type of food to every type of animal. Look at what output the test expects. Using overloading and overriding, make sure that your code passes this test.
***
**TODO:** Make changes to your code so that its passes the final test.
***

We have actually been doing something called *double dispatch* to make sure that the methods are being called in the correct places. This will be covered soon in your lectures so listen out for it!
