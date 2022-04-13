# Assignment03 - Minotaur Gifts & Atmospheric Temperature Readings

## Problem 1
I create a concurrent Linked List using the non-blocking implementation shown in the textbook. I then create 4 threads to use as servants. I initialize one list with 500k gifts. 
Each gift is an object of type "Present". Each has a tag int that is simply the hashcode for that specific Present instance, unless it was manually overwritten. I then shuffle
the gift bag and split it into 4. I assign each servant one of these 4ths. Each servant then has 125k unsorted gifts. They are then tasked with carrying out the 3 operations
layed out in the assignment description.

## For problem 2
### Approach 1:
Pros:
1. Guests do have the ability to roam around the castle while not attempting to enter the showroom.

Cons:
1. Guests crowding around the door would mean that the first guest there wouldn't necessarily be the first guest in the showroom once it is available again.
2. Not all guests are guaranteed to view the vase, which I'm sure would be a HUGE embarrassment for the Minotaur who is an excellent host.

### Approach 2:
Pros:
1. The sign makes it so that any guests that reads it as "BUSY" can just continue to roam around the castle since they know they have no chance to view the vase at that time.
2. Every guest that wants to view the vase will eventually be able to do so, since the sign will be "AVAILABLE" at some point that they check during the duration of the party.
3. If they are the first person to notice the sign is "AVAILABLE" they are the first person that can go view the vase.

Cons:
1. If two guests happen to view the sign as "AVAILABLE" at the same time, there could be a conflict in deciding who gets to view the vase first.
2. It's possible that a fraction of a second after a guest read the sign as "BUSY" the guest viewing the vase could be done and set the sign as "AVAILABLE". This would result in that previous guest missing out on opportunity to view the vase that they could have had if they had just waited.

### Approach 3
Pros:
1. Every guest will have an opportunity to view the vase.
2. Every guest will enter the showroom in a designated order. Whoever is there first will be the first to view and so on.

Cons:
1. If all guests are queuing, most of the guest will be doing nothing most of the time. This means that the overall experience will be worse for everyone involved since they will spend most of their time standing in line rather than doing stuff.

### Conclusion
I believe that approach number 2 is the best. In approach 1, a bunch of threads trying to access a shared resource with no rhyme or reason could lead to starvation for some threads or for many threads to be waiting on the resource opening up, which isn't happening in any sort of ordered or regulated manner. Although approach number 3 does address these issues, it takes it too far by making it so that every thread except 1 will be doing no work most of the time while it waits for the 1 thread to finish "viewing the vase". At that point, there is a greatly reduced advantage to using parallel programming in the first place. Approach 2 comes in between these by guaranteeing that every thread will eventually have access to the shared resource in an ordered way, and that while a thread is unable to access the resource it isn't just stuck there waiting and can instead do other things while periodically checking back to see if the resource has become available. Approach 2 is the one I implemented in *MinotaurVase.cs*.





## In order to run these programs:

On a Linux/Unix Environment:
1. In a Terminal window, navigate to the same directory where you downloaded HW3_1.java and MinotaurVase.cs
2. Assuming you have Java installed, Type:
3. `javac HW3_1.java`
4. `java HW3_1`
