import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class TemperatureSensor extends Thread
{
    private final int NUM_THREADS = 8;

    private final int threadID;
    private static List<Integer> readings;
    private static List<Boolean> ready;
    private final int hours;
    private Lock lock;

    public TemperatureSensor(int threadID, List<Integer> readings, List<Boolean> ready, int hours)
    {
        this.threadID = threadID;
        this.readings = readings;
        this.ready = ready;
        this.hours = hours;
        this.lock = new ReentrantLock();
    }

    private boolean allReady()
    {
        for(boolean status : ready)
        {
            if (!status)
                return false;
        }
        return true;
    }

    private void readTemperature()
    {
        for (int i = 0; i < hours; i++)
        {
            for (int j = 0; j < 60; j++)
            {
                int tempReading = (int) ((Math.random() * 172) - 101);
                synchronized (ready)
                {
                    ready.set(threadID - 1, false);
                }
                
                readings.set(j + (threadID * 60 - 60), tempReading);
            
                synchronized (ready)
                {
                    ready.set(threadID - 1, true);
                }

            }

            while (!allReady())
            {
                try
                {
                    Thread.sleep(10);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            
            if (threadID == 1)
            {
                synchronized (this)
                {
                    printReport(i);
                }
            }    
        }
    }

    private void printReport(int hour)
    {
        System.out.println("---- HOUR " + (hour + 1) + " REPORT ----");

        greatestDifference();
        Collections.sort(readings);
        lowestTemps();
        highestTemps();
    }

    private void greatestDifference()
    {
        int interval = 10;
        int start = 0;
        int maxDifference = Integer.MIN_VALUE;

        for (int i = 0; i < NUM_THREADS; i++)
        {
            int offset = i * 60;

            for (int j = offset; j < 60 - interval + 1; j++)
            {
                int max = Collections.max(readings.subList(j, j + interval));
                int min = Collections.min(readings.subList(j, j + interval));
                int diff = max - min;

                if (diff > maxDifference) {
                    maxDifference = diff;
                    start = j;
                }
            }
        }

        System.out.println("<1> Largest Temperature Difference");
        System.out.println("1) Difference: " + maxDifference);
        System.out.println("2) Start: " + start);
        System.out.println("3) End: " + (start + interval) + "\n");
    }

    private void lowestTemps()
    {
        Set<Integer> temps = new TreeSet<>();

        for (Integer reading : readings)
        {
            temps.add(reading);
            if (temps.size() == 5)
            {
                break;
            }
        }

        System.out.println("<2> Lowest Temperatures:");
        int count = 1;
        for (Integer temp : temps)
        {
            System.out.println(count + ") " + temp + " F");
            count++;
        }
        System.out.println();
    }

    private void highestTemps()
    {
        Set<Integer> temps = new TreeSet<>(Collections.reverseOrder());

        for (int i = readings.size() - 1; i >= 0; i--)
        {
            temps.add(readings.get(i));
            if (temps.size() == 5)
            {
                break;
            }
        }

        System.out.println("<3> Highest Temperatures:");
        int count = 1;
        for (Integer temp : temps)
        {
            System.out.println(count + ") " + temp + " F");
            count++;
        }
        System.out.println();
    }

    @Override
    public void run()
    {
        readTemperature();
    }
}

public class AtmosphereReading
{
    final static int NUM_THREADS = 8;
    final static int NUM_READINGS = 60;
    final static int NUM_HOURS = 24;

    public static void main(String[] args)
    {
        List<Integer> readings = Collections.synchronizedList(new ArrayList<>(NUM_THREADS * NUM_READINGS));
        List<Boolean> ready = Collections.synchronizedList(new ArrayList<>(NUM_THREADS));

        for (int i = 0; i < NUM_THREADS * NUM_READINGS; i++)
        {
            readings.add(Integer.MIN_VALUE);
        }

        for (int i = 0; i < NUM_THREADS; i++)
        {
            ready.add(true);
        }

        long startTime = System.currentTimeMillis();
        TemperatureSensor[] sensors = new TemperatureSensor[NUM_THREADS];
        for (int i = 0; i < NUM_THREADS; i++)
        {
            sensors[i] = new TemperatureSensor(i + 1, readings, ready, NUM_HOURS);
            sensors[i].start();
        }

        for (TemperatureSensor sensor : sensors)
        {
            try
            {
                sensor.join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("All sensors have concluded their readings in " + duration);
    }
}