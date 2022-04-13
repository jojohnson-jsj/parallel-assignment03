import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.*;

// Small class used to represent an individual gift
class Present
{
	// The value used for sorting and comparison. Using either the
	// hashcode of the Present object or is overwritten manually.
	public int tag;

	public Present()
	{
		this.tag = hashCode();
	}

	public Present(int tagOverwrite)
	{
		this.tag = tagOverwrite;
	}
}

// Class used for an individual Linked List node. Is used specifically with
// Presents and have two constructors to create new nodes.
class ListNode
{
	AtomicMarkableReference<ListNode> next;
	Present data;

	public ListNode(Present p)
	{
		this.data = p;
	}

	public ListNode(Present p, ListNode next)
	{
		this.next = new AtomicMarkableReference<ListNode>(next, false);
	   	this.data = p;
	}
}


// Window class demonstrated in the textbook for the purpose of a lock-free
// implementation.
class Window
{
	public ListNode pred, curr;

	public Window(ListNode myPred, ListNode myCurr)
	{
		pred = myPred; curr = myCurr;
	}
}

// Linked List class built from ListNodes. A head and tail pointer are maintained,
// although the tail pointer is never explicitly used. Its constructor initializes the
// head of the list to Integer.MIN_VALUE and the head.next (tail) to Integer.MIN_VALUE.
class LinkedList
{
	AtomicMarkableReference<ListNode> head;
	AtomicMarkableReference<ListNode> tail;

	public LinkedList()
	{
		ListNode headNode = new ListNode(new Present(Integer.MIN_VALUE));
		ListNode tailNode = new ListNode(new Present(Integer.MAX_VALUE));

		tail = new AtomicMarkableReference<ListNode>(tailNode, false);
		headNode.next = tail;

		head = new AtomicMarkableReference<ListNode>(headNode, false);
	}

	// Very similar to the implementation demonstrated in the textbook. Used to build the
	// operations supported by a lock-free Linked List.
	public Window find(ListNode head, int key)
	{
		ListNode pred = null, curr = null, succ = null;
		boolean[] marked = {false};
		boolean snip;

		retry: while (true)
		{
			pred = head;
			curr = pred.next.getReference();
			while (true)
			{
				if (curr == null || curr.next == null)
				{
					succ = null;
					marked[0] = false;
				}
				else
				{
					succ = curr.next.get(marked);
				}
				while (marked[0])
				{
					snip = pred.next.compareAndSet(curr, succ, false, false);

					if (!snip)
						continue retry;

					curr = succ;

					if (curr != null && curr.next != null)
						succ = curr.next.get(marked);
					else
						succ = null;
				}

				if (curr != null && curr.data.tag >= key)
				{
					return new Window(pred, curr);
				}

				pred = curr;
				curr = succ;
			}
		}
	}

	// Similar to the implementation from the textbook. Uses a present's "tag" value to
	// determine where in the list it should be inserted into.
	public boolean add(Present present)
	{
		int key = present.tag;

		while (true)
		{
			Window window = find(head.getReference(), key);
			ListNode pred = window.pred, curr = window.curr;

			if (curr.data.tag == key)
			{
				return false;
			}
			else
			{
				ListNode node = new ListNode(present);

				node.next = new AtomicMarkableReference<ListNode>(curr, false);

				if (pred.next.compareAndSet(curr, node, false, false))
				{
					return true;
				}
			}
		}
	}

	// Uses a given "tag" value to search for an remove a present from the list, as per
	// the example shown in the textbook.
	public boolean remove(int tag)
	{
		int key = tag;
		boolean snip;

		while (true)
		{
			Window window = find(head.getReference(), key);
			ListNode pred = window.pred, curr = window.curr;

			if (curr.data.tag != key)
			{
				return false;
			}
			else
			{
				ListNode succ = curr.next.getReference();

				snip = curr.next.compareAndSet(succ, succ, false, true);
				if (!snip)
					continue;

				pred.next.compareAndSet(curr, succ, false, false);
				return true;
			}
		}
	}

	// Takes an integer tag as a parameter and returns true if a present with a tag matching it
	// is in the list. It returns false otherwise.
	public boolean contains(int tag)
	{
		boolean[] marked = { false };
		int key = tag;
		AtomicMarkableReference<ListNode> curr = head;

		while (curr.getReference() != null && curr.getReference().data.tag < key)
		{
			curr = curr.getReference().next;
		}

		if (curr.getReference() == null)
			return false;

		return (curr.getReference().data.tag == key && !marked[0]);
	}
}

// This class is used specifically for determining the thread/servant behavior. It uses fields
// and methods from other classes to support the 3 operations described by Part 1 of the assignment.
class Servant extends Thread
{
	// This list contains 1/4th of all of the gifts.
	ArrayList<Present> servantBag;

	// This list contains 1/4th of all of the gift tags.
	ArrayList<Integer> servantTags;

	// Flags used to know when a servant is done with their portion of the list.
	boolean addingDone = false;
	boolean writingDone = false;

	// Servant number is passed in when instantiating a Servant object and is used to determine
	// which quarter of the 500k gifts correspond to this servant thread.
	int servantNumber;

	// currPres keeps track of the current present to be added from the thread's servantBag.
	int currPres = 0;

	// Takes in a value that corresponds to which of the 4 servants this thread is intended to be.
	public Servant(int value)
	{
		this.servantNumber = value;
	}

	// Executed after a servant thread is told to start. It initializes the servant's lists to be the
	// correct portion of all of the gifts. It then (randomly) simulates the 3 instructions from the
	// assignment and finishes when there are no more gifts that haven't been added to the list and no
	// more gifts left in the list to process.
	public void run()
	{
		if (servantNumber == 1)
		{
			this.servantBag = HW3_1.servantBag1;
			this.servantTags = HW3_1.servantTags1;
		}
		else if (servantNumber == 2)
		{
			this.servantBag = HW3_1.servantBag2;
			this.servantTags = HW3_1.servantTags2;
		}
		else if (servantNumber == 3)
		{
			this.servantBag = HW3_1.servantBag3;
			this.servantTags = HW3_1.servantTags3;
		}
		else if (servantNumber == 4)
		{
			this.servantBag = HW3_1.servantBag4;
			this.servantTags = HW3_1.servantTags4;
		}

		// While there are either more gifts in the servantBag to add or more tags in the servantTags
		// list to process, continue randomly carrying out 1 of the 3 instructions.
		while (!addingDone || !writingDone)
		{
			// Randomly 1/5 of the time the Minotaur will request the servant looks for a specific gift
			// in the list. using the contains method.
			if ((int)( Math.random() * 6) == 5)
			{
				int randomIndex = (int)(Math.random() * (500001));
				int tag = HW3_1.allTags.get(randomIndex);

				boolean found =  HW3_1.list.contains(tag);

				if (found)
					System.out.println("Gift #" + tag + " was found in the list.");
				else
					System.out.println("Gift #" + tag + " was NOT found in the list.");
			}
			// If the Minotaur hasn't made a request to search for a specific gift, do one of the first
			// 2 procedures.
			else
			{
				// Randomly decided whether to add to the list or process a gift in the list.
				boolean add = FlipCoin();

				// If there servantTags left, writing "Thank You" messages is not yet done.
				if (servantTags.size() > 0)
					writingDone = false;

				// Randomly roughly half of the time, add a present to the list.
				if ((add || writingDone) && !addingDone)
				{
					// if currPres >= 125k, the current thread has already processed all of its quarter.
					if (currPres < 125000)
					{
						Present p = this.servantBag.get(currPres);
						boolean success = HW3_1.list.add(p);

						if (success)
						{
							currPres++;
						}
					}
					else
						addingDone = true;
				}
				// Randomly roughly half of the time, process a present in the list.
				else if ((!add || addingDone) && !writingDone)
				{
					if (servantTags.size() > 0)
					{
						int tag = servantTags.get(0);
						boolean success = HW3_1.list.remove(tag);

						if (success)
						{
							servantTags.remove(0);

							// finishedSet keeps track of all of the tags that have been added and removed from the list.
							HW3_1.finishedSet.put(tag, tag);
						}
					}
					else
						writingDone = true;
				}
			}
		}
	}

	// Small method for readability. Returns true roughly half of the time.
	private boolean FlipCoin()
	{
		if((int)( Math.random() * 2 + 1) == 1)
			return true;
		return false;
	}
}


// Driver class. Contains the single Linked List (list), as well as other lists used by individual
//servant threads.
public class HW3_1 extends Thread
{
	public static LinkedList list;
	public static ConcurrentHashMap<Integer, Integer> finishedSet;

	public static ArrayList<Integer> allTags;
	public static ArrayList<Present> servantBag1;
	public static ArrayList<Present> servantBag2;
	public static ArrayList<Present> servantBag3;
	public static ArrayList<Present> servantBag4;
	public static ArrayList<Integer> servantTags1;
	public static ArrayList<Integer> servantTags2;
	public static ArrayList<Integer> servantTags3;
	public static ArrayList<Integer> servantTags4;

	public static void main(String[] args)
	{
		finishedSet = new ConcurrentHashMap<Integer, Integer>();
		allTags = new ArrayList<Integer>();

		// Initializing bags for all the servants to work through.
		servantBag1 = new ArrayList<Present>();
		servantBag2 = new ArrayList<Present>();
		servantBag3 = new ArrayList<Present>();
		servantBag4 = new ArrayList<Present>();
		servantTags1 = new ArrayList<Integer>();
		servantTags2 = new ArrayList<Integer>();
		servantTags3 = new ArrayList<Integer>();
		servantTags4 = new ArrayList<Integer>();

		list = new LinkedList();

		// Add 500k presents to the bag.
		ArrayList<Present> presentBag = new ArrayList<Present>();

		for (int i = 0; i < 500000; i++)
		{
			Present p = new Present();
			allTags.add(p.tag);

			presentBag.add(p);
		}

		// Shuffle the bag to guarantee a random draw.
		Collections.shuffle(presentBag);
		Collections.shuffle(allTags);

		// Split the shuffled bag in 4 and assign the values to each servants bags.
		// Also split all of the gift tags into 4 and assign them to each servant.
		for (int i = 0; i < 125000; i++)
		{
			Present cur = presentBag.get(i);

			servantBag1.add(cur);
			servantTags1.add(cur.tag);
		}
		for (int i = 125000; i < 250000; i++)
		{
			Present cur = presentBag.get(i);

			servantBag2.add(cur);
			servantTags2.add(cur.tag);
		}
		for (int i = 250000; i < 375000; i++)
		{
			Present cur = presentBag.get(i);

			servantBag3.add(cur);
			servantTags3.add(cur.tag);
		}
		for (int i = 375000; i < 500000; i++)
		{
			Present cur = presentBag.get(i);

			servantBag4.add(cur);
			servantTags4.add(cur.tag);
		}


		// Creating threads for each servant.
		Servant servant1 = new Servant(1);
		servant1.start();

		Servant servant2 = new Servant(2);
		servant2.start();

		Servant servant3 = new Servant(3);
		servant3.start();

		Servant servant4 = new Servant(4);
		servant4.start();

		// Once all of the threads have added all of their gifts and processed all of
		// their tags, print a message to indicate that.
		try
		{
			servant1.join();
			servant2.join();
			servant3.join();
			servant4.join();
		}
		catch (Exception e) {}

		System.out.println("All the guests' gifts have been processed!");
	}
}
