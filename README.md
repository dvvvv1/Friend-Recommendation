## Friend Recommendation, Word Count and MapReduce ##
## Info ##
Author Name: Puzhi Yao

Author Email: dvvvv1@hotmail.com

## Instructions ##
## Prerequisite ##
Make sure all Hadoop components and libraries being installed in your system before running our programs.

## Error Handler 01 ##
If your terminal shows the error message like 'output exists'

please remove the output folder and try it again.

## Error Handler 02 ##
If your terminal shows the error message like 'XXXXXX library missing'

please add external jar library into the classpath:

slf4j-simple-1.7.21.jar

htrace-core4.jar

## Exercise 1 ##
## Standalone Mode for Exercise 1##
1. Open eclipse

2. Import our project from `~/WordCount`. (File->Import->General->Exisiting Project into Workspace)

3. Right click the project and select "Run as" to configure Java application.

4. Create a new launch configuration on Java Application and type "WordCount" in Main Class at right window.

5. Switch to Arguments, and type "100.txt output".

6. Click the button of "Run".

7. Check the output folde in the current path (e.g. "~/WordCount/output") 

8. The file, `part-r-00000`, is the final result for Exercise 1.

---------------------

## Pseudo-distributed Mode for Exercise 1 ##

1. Go into the directory.

$ cd ~/workspace/WordCount

2. Open a terminal and run the following commands.

$ hadoop fs -put 100.txt

$ hadoop fs -rm -r output (Remove the previous output first.)

$ hadoop jar WordCount.jar WordCount.WordCount 100.txt output

3. check the result

$ hadoop fs -cat output/part\* 



## Exercise 2 ##
## Standalone Mode for Exercise 2##

1. Open eclipse

2. Import our project from "~/FriendRecommendation". (File->Import->General->Exisiting Project into Workspace)

3. Right click the project and select "Run as" to configure Java application.

4. Create a new launch configuration on Java Application and type "FriendRecommendation" in Main Class at right window.

5. Switch to Arguments, and type "soc-LiveJournal1Adj.txt output".

6. Click the button of "Run".

7. Check the output folde in the current path (e.g. "~/FriendRecommendation/output") 

8. The file, `part-r-00000`, is the final result for Exercise 2.

---------------------

## Pseudo-distributed Mode for Exercise 2 ##

1. Go into the directory.

$ cd ~/FriendRecommendation

2. Open a terminal and run the following commands.

$ hadoop fs -put soc-LiveJournal1Adj.txt

$ hadoop fs -rm -r output (Remove the previous output first.)

$ hadoop jar FriendRecommendation.jar FriendRec.FriendRecommendation soc-LiveJournal1Adj.txt output

3. check the result

$ hadoop fs -cat output/part\* 

## Instruction END ##
