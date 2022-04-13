import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

class TemperatureThread extends Thread
{
	// Should be set to be the same constant values as in HW3_2
	static final int MAX_HOURS = 12;
	static final long HOUR = 2000;
	static final long MINUTE = 2000/60;

	// Indicates which of the 8 threads this one is for the sake of knowing
	// which row of the temperatureLog it should be writing to.
	public int threadNum;

	// Used to keep track of the current index in the temperatureLog during this HOUR.
	public int currIndex;

	// Used to keep track of which HOUR the thread is on.
	public int hour;

	// Simple constructor that initializes the thread's values.
	public TemperatureThread(int value)
	{
		this.threadNum = value;
		this.currIndex = 0;
		this.hour = 0;
	}

	// Continues while the hours have not exceeded MAX_HOURS. The thread sleeps for
	// one MINUTE and then records a reading. It writes the recorded reading to its
	// current index in the temperatureLog.
	public void run()
	{
		while (this.hour <= MAX_HOURS)
		{
			try {
				Thread.sleep(MINUTE);
			} catch (InterruptedException e) {}

			RecordReading();
		}
	}

	// Writes the randomly generated temperature reading to the current index in the
	// thread's row in the temperatureLog.
	public void RecordReading()
	{
		// If the index equals or exceeds 60, all the thread's readings for the hour
		// have been performed and it should wait for its index to be reset before
		// trying to record any more readings.
		if (currIndex >= 60)
			return;

		HW3_2.temperatureLog[threadNum][currIndex] = HW3_2.GetTempReading();
		currIndex++;
	}


	// Takes no parameters. Resets the currIndex field of a thread to 0. Done
	// after every "hour" when the report is compiled.
	public void ResetIndex()
	{
		this.currIndex = 0;
	}

	// Takes no parameters. Increments the hour counter. Done after every
	// "hour" when the report is compiled.
	public void IncrementHour()
	{
		this.hour++;
	}
}

// Driver class that starts all of the threads and prints out the HOURly reports.
public class HW3_2
{
	// Should be the same as the constants in TemperatureThread.
	static final int MAX_HOURS = 12;
	static final long HOUR = 2000;

	// The shared 2D array used by all threads to record HOURly readings.
	public static double[][] temperatureLog;

	// The current low/high. Initialized to the appropriate values to be used for
	// determining which HOURly readings should go in the list of highs/lows.
	private static double currLow = Integer.MAX_VALUE;
	private static double currHigh = Integer.MIN_VALUE;

	// Used to store the largest difference found during any 10 MINUTE interval.
	private static double largestInterval = Integer.MIN_VALUE;

	// Used to keep track of the current hour.
	private static int hour;

	// Lists used to keep track of the 5 lowest/highest readings HOURly.
	private static ArrayList<Double> highList;
	private static ArrayList<Double> lowList;

	// 8 Thead declarations.
	public static TemperatureThread thread0;
	public static TemperatureThread thread1;
	public static TemperatureThread thread2;
	public static TemperatureThread thread3;
	public static TemperatureThread thread4;
	public static TemperatureThread thread5;
	public static TemperatureThread thread6;
	public static TemperatureThread thread7;


	public static void main(String args[])
	{
		// This array will be accessed by all 8 threads. However, each thread will only ever
		// access it's specific row, eliminating the possibility of conflict when writing to an index.
		temperatureLog = new double[8][60];
		hour = 0;

		// These two lists keep track of the highest/lowest 5 values at the time of each report.
		highList = new ArrayList<Double>();
		lowList = new ArrayList<Double>();

		// Initializing all the threads
		thread0 = new TemperatureThread(0);
		thread0.start();
		thread1 = new TemperatureThread(1);
		thread1.start();
		thread2 = new TemperatureThread(2);
		thread2.start();
		thread3 = new TemperatureThread(3);
		thread3.start();
		thread4 = new TemperatureThread(4);
		thread4.start();
		thread5 = new TemperatureThread(5);
		thread5.start();
		thread6 = new TemperatureThread(6);
		thread6.start();
		thread7 = new TemperatureThread(7);
		thread7.start();


		Timer timer = new Timer();

		timer.schedule(new TimerTask()
		{
			// Every HOUR (2 seconds), run will be executed by this thread. It will check to see
			// if MAX_HOURS have been reached, in order to stop its execution. Otherwise, it'll
			// increment the hour counter and compile the HOURly report.
			public void run()
			{
				if (hour >= MAX_HOURS)
					timer.cancel();

				CompileReport();
				hour++;
			}
		}, 0, HOUR);


	}

	// Takes no parameters. Every HOUR (2 seconds), it prints out information as described in Part 2
	// of the assignment. It begins by resetting all the values of each individual thread in order for
	// use in the next HOURly interval. It then searches through the temperatureLog shared 2D array
	// in order to formulate the report statistics.
	public static void CompileReport()
	{
		ResetThreads();

		GetHighsLows();
		GetLargestDifference();

		PrintReport();
	}

	// Takes no parameters. Returns a randomly generated reading between -100F and 70F.
	public static double GetTempReading()
	{
		// Roughly half the time, choose a number from -100 to 0.
		if ((int)( Math.random() * 2 + 1) == 1)
		{
			return -(Math.random() * 100);
		}
		// The other half of the time, choose a number from 0 to 70.
		else
		{
			return Math.random() * 70;
		}
	}

	// Takes no parameters. Iterates through the 2D array HOURly in order to determine
	// the highest/lowest 5 readings.
	public static void GetHighsLows()
	{
		for (int i = 0; i < 8; i++)
		{
			for (int j = 0; j < 60; j++)
			{
				double currData = temperatureLog[i][j];

				// If the list of lowest values isn't full, add any value.
				if (lowList.size() < 5)
				{
					lowList.add(currData);

					currLow = Math.min(currData, currLow);
				}
				// If the current value is lesser than or equal to the lowest temperature
				// in the lowList, update currLow, sort the list in increasing order, remove
				// the last (largest) element, and add the new (smallest).
				else if (currData <= currLow)
				{
					currLow = Math.min(currData, currLow);

					Collections.sort(lowList);

					lowList.remove(lowList.size() - 1);
					lowList.add(currData);
				}

				// If the list of highest values isn't full, add any value.
				if (highList.size() < 5)
				{
					highList.add(currData);

					currHigh = Math.max(currData, currHigh);
				}
				// If the current value is greater than or equal to the highest temperature
				// in the highList, update currHigh, sort the list in increasing order, remove
				// the first (smallest) element, and add the new (largest).
				else if (currData >= currHigh)
				{
					currHigh = Math.max(currData, currHigh);

					Collections.sort(highList);

					highList.remove(0);
					highList.add(highList.size() - 1, currData);
				}
			}
		}
	}

	// Takes no parameters. Iterates through the 2D array HOURly to determine the largest
	// difference in readings within any 10 minute time interval.
	private static void GetLargestDifference()
	{
		largestInterval = Integer.MIN_VALUE;
		double tempLow = Integer.MAX_VALUE;
		double tempHigh = Integer.MIN_VALUE;

		// We loop through every row in isolation since there are 8 sets of readings that
		// were all taken during the same MINUTE by each thread.
		for (int i = 0; i < 8; i++)
		{
			for (int j = 0; j < 60; j++)
			{
				tempLow = Integer.MAX_VALUE;
				tempHigh = Integer.MIN_VALUE;

				// Checking every 10 MINUTE interval of readings too find the greatest
				// difference. Each thread's readings are considered in isolation, and the
				// largest overall interval recorded in any thread is saved.
				for (int k = j; k < j + 10 && k < 60; k++)
				{
					double tempData = temperatureLog[i][k];

					tempLow = Math.min(tempData, tempLow);
					tempHigh = Math.max(tempData, tempHigh);
					largestInterval = Math.max(largestInterval, tempHigh - tempLow);
				}
			}
		}
	}

	// Takes no parameters. Prints the report information and resets the low/high Lists as well
	// as the currLow/High fields.
	private static void PrintReport()
	{
		System.out.println("");
		System.out.println("");
		System.out.println("Report for Hour " + hour);
		System.out.println("-------------------");
		System.out.println("Lowest values were:");
		for (int i = 0; i < 5; i++)
		{
			System.out.println(lowList.get(i));
		}

		System.out.println("");

		System.out.println("Highest values were:");
		for (int i = 0; i < 5; i++)
		{
			System.out.println(highList.get(i));
		}

		System.out.println("The largest difference was: " + largestInterval);

		lowList.clear();
		highList.clear();

		currLow = Integer.MAX_VALUE;
		currHigh = Integer.MIN_VALUE;
	}

	// Resets each thread's currIndex to 0 and increments each thread's hour after every HOURly report.
	public static void ResetThreads()
	{
		thread0.ResetIndex();
		thread0.IncrementHour();

		thread1.ResetIndex();
		thread1.IncrementHour();

		thread2.ResetIndex();
		thread2.IncrementHour();

		thread3.ResetIndex();
		thread3.IncrementHour();

		thread4.ResetIndex();
		thread4.IncrementHour();

		thread5.ResetIndex();
		thread5.IncrementHour();

		thread6.ResetIndex();
		thread6.IncrementHour();

		thread7.ResetIndex();
		thread7.IncrementHour();
	}
}
