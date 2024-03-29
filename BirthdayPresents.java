import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.*;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileNotFoundException;

class Node
{
    int value;
    Node next;

    public Node(int val)
    {
        this.value = val;
        this.next = null;
    }
}

class ConcurrentLinkedList
{
    private Node head;
    private Lock lock;
    private int size;

    public ConcurrentLinkedList()
    {
        this.head = null;
        this.lock = new ReentrantLock();
        this.size = 0;
    }

    public Node peek()
    {
        return head;
    }

    public void add(int value) {
        Node newNode = new Node(value);
        lock.lock();
        try {
            if (head == null) {
                head = newNode;
            } else {
                Node current = head;
                while (current.next != null && current.next.value < value) {
                    current = current.next;
                }
                newNode.next = current.next;
                current.next = newNode;
            }
            size++;
        } finally {
            lock.unlock();
        }
    }

    public void remove(int value)
    {
        lock.lock();

        try
        {
            Node current = head;
            while (current.next != null && current.next.value != value)
            {
                current = current.next;
            }
            if (current.next != null && current.next.value == value)
            {
                current.next = current.next.next;
            }
            size--;
        }
        finally
        {
            lock.unlock();
        }
    }

    public int removeHead() {
        lock.lock();
        try {
            if (head == null)
                return -1;

            int value = head.value;
            head = head.next;
            size--;
            return value;
        } 
        finally 
        {
            lock.unlock();
        }
    }

    public boolean search(int value)
    {
        lock.lock();
        try
        {
            Node current = head;
            while (current != null)
            {
                if (current.value == value)
                    return true;
                current = current.next;
            }
            return false;
        }
        finally
        {
            lock.unlock();
        }
    }

    public boolean empty()
    {   
        synchronized(this)
        {
            return size == 0;
        }
    }

    public int size()
    {
        return size;
    }

    public void print()
    {
        lock.lock();
        try
        {
            Node curr = head;
            while (curr != null)
            {
                System.out.print(curr.value + " -> ");
                curr = curr.next;
            }
            System.out.println();
        }
        finally
        {
            lock.unlock();
        }
    }
}

class Servant extends Thread
{
    private int ID;
    private static Set<Integer> presents;
    private static Set<Integer> cards;
    private static ConcurrentLinkedList list;
    private static Lock lock;
    private static PrintWriter writer;

    // Operation Constants
    final static int TAKE_PRESENT = 0;
    final static int WRITE_CARD = 1;
    final static int CHECK_GIFT = 2;

    final static int NUM_PRESENTS = 500000;

    public Servant (int ID, Set<Integer> presents, Set<Integer> cards, ConcurrentLinkedList list, PrintWriter writer)
    {
        this.ID = ID;
        this.presents = presents;
        this.cards = cards;
        this.list = list;
        this.lock = new ReentrantLock();
        this.writer = writer;
    }

    public void run()
    {
        while (cards.size() < NUM_PRESENTS)
        {
            int task = (int) (Math.random() * 3);

            switch (task)
            {
                case TAKE_PRESENT: {
                    lock.lock();
                    
                    try
                    {
                        if (presents.isEmpty())
                        {
                            continue;
                        }

                        
                        Iterator<Integer> gift = presents.iterator();
                        Integer num = gift.next();
                        gift.remove();
                        list.add(num);
                        
                    }
                    finally
                    {
                        lock.unlock();
                    }

                    break;
                }
                    
                case WRITE_CARD: {
                    
                    if (list.empty())
                    {
                        continue;
                    }

                    int gift = list.removeHead();

                    if (gift == -1)
                    {
                        continue;
                    }

                    //System.out.println("Thank you for the gift guest #" + gift);
                    writer.append("Thank you for the gift guest #" + gift + "\n");

                    lock.lock();
                    try
                    {
                        cards.add(gift);
                    }
                    finally
                    {
                        lock.unlock();
                    }
                    
                    break;
                }

                case CHECK_GIFT: {
                    int randomGuest = (int) (Math.random() * NUM_PRESENTS) + 1;
                    boolean found = list.search(randomGuest);

                    // System.out.println("Minotaur says to search for guest " + randomGuest + "'s gift.");
                    // System.out.println("Guest " + randomGuest + "'s gift was " + (found ? "found" : "not found"));
                    writer.append("Minotaur says to search for guest " + randomGuest + "'s gift.\n");
                    writer.append("Guest " + randomGuest + "'s gift was " + (found ? "found" : "not found\n"));
                    break;
                }
            }
        }
    }
}

public class BirthdayPresents
{
    // Number Constants
    final static int NUM_THREADS = 4;
    final static int NUM_PRESENTS = 500000;

    public static void main(String [] args)
    {
        ConcurrentLinkedList list = new ConcurrentLinkedList();
        Set<Integer> presents = Collections.synchronizedSet(new HashSet<>());
        Set<Integer> cards = Collections.synchronizedSet(new HashSet<>());
        File outFile = new File("out.txt");
        
        try (PrintWriter writer = new PrintWriter(outFile))
        {
            for (int i = 0; i < NUM_PRESENTS; i++)
            {
                presents.add(i + 1);
            }
            Collections.shuffle(new java.util.ArrayList<>(presents));
    
            Servant[] servants = new Servant[NUM_THREADS];

            long startTime = System.currentTimeMillis();
            for (int i = 0; i < NUM_THREADS; i++)
            {
                servants[i] = new Servant(i + 1, presents, cards, list, writer);
                servants[i].start();
            }

            for (Servant servant : servants) 
            {
                try {
                    servant.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            System.out.println("All gift cards have been written in " + duration + " milliseconds");
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        
        //writer.close();
    }
}