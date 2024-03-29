# ParallelAssignment3
<h2>Compilation and Execution Instructions</h2>
<p>Run javac BirthdayPresents.java <br>Then run java BirthdayPresents <br>The file "out.txt" will be created after completion and holds the text output of the program. When the program terminates, a console statement noting the runtime will appear to let you know when it is finished.</p>

<h2>Birthday Presents</h2>
<p>To solve this problem I created a concurrent linked list (singularly linked) to act as the "chain" described in the problem. It includes only the functionality required to solve the problem and inserts in numerical order. Items are only removed from the head which is an O(1) operation. To handle the "giftbag" and ensure that all the thank you cards have been written I used synchronized HashSets (shuffled for randomness). The giftbag set was filled with 500000 integers to represent the presents, then one of the three described tasks would occur at random. When a present would be removed from the set it would be added to the linked list, from there it would be removed from the front of the list and added to the card set to symbolize a card being written for that gift. Once the card set reached 500000 all the presents have been accounted for.<br><br>Since the method involves an amount of randomness execution times can vary but, without printing, average run time was 36 seconds on my machine.</p>

<h2>Compilation and Execution Instructions</h2>

<h2>Atmospheric Temperature Reading</h2>
