# Assignment03 - Minotaur Gifts & Atmospheric Temperature Readings

## Problem 1
I create a concurrent Linked List using the non-blocking implementation shown in the textbook. I then create 4 threads to use as servants. I initialize one list with 500k gifts. 
Each gift is an object of type "Present". Each has a tag int that is simply the hashcode for that specific Present instance, unless it was manually overwritten. I then shuffle
the gift bag and split it into 4. I assign each servant one of these 4ths. Each servant then has 125k unsorted gifts. They are then tasked with carrying out the 3 operations
layed out in the assignment description.

## Problem 2
My approach is explained in detail in the HW3_2.java file. It is straightforward, with a random value from -100 to 70 generated every "minute" and a report 
with all of the required information printed to the screen every "hour".


## In order to run these programs:

On a Linux/Unix Environment:
1. In a Terminal window, navigate to the same directory where you downloaded HW3_1.java and HW3_2.java
2. Assuming you have Java installed, Type:
3. `javac HW3_1.java` or `javac HW3_2.java`
4. `java HW3_1` or `java HW3_2`
