package FriendRec;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class FriendRecommendation extends Configured implements Tool {
   public static void main(String[] args) throws Exception {
      System.out.println(Arrays.toString(args));
      int res = ToolRunner.run(new Configuration(), new FriendRecommendation(), args);
      
      System.exit(res);
   }
   
   /*
    * The input and output type are not primitive data type in hadoop.
    * Therefore, we implements a new Writable class to output expected results.
    */
   static public class FCWritable implements Writable {
	   // setup public variables
	   public Long User;
	   public Long Friend;
	   
	   /*
	    * Constructor
	    * No input and set two public variable to be negative 
	    */
	   public FCWritable() {
		   this.User = - 1L;
		   this.Friend = - 1L;
	   }
	   
	   /*
	    * Constructor
	    * set two public variable to be certain value
	    */
	   public FCWritable(Long tmpUser, Long tmpFriend) {
		   this.User = tmpUser;
		   this.Friend = tmpFriend;
	   }

	   @Override
	   public void readFields(DataInput arg0) throws IOException {
		   User = arg0.readLong();
		   Friend = arg0.readLong();
	   }

	   @Override
	   public void write(DataOutput arg0) throws IOException {
		   arg0.writeLong(User);
		   arg0.writeLong(Friend);
	   }
   }

   @Override
   public int run(String[] args) throws Exception {
      System.out.println(Arrays.toString(args));
      Job job = new Job(getConf(), "FriendRecommendation");
      job.setJarByClass(FriendRecommendation.class);
      job.setOutputKeyClass(LongWritable.class);
      job.setOutputValueClass(FCWritable.class);

      job.setMapperClass(Map.class);
      job.setReducerClass(Reduce.class);

      job.setInputFormatClass(TextInputFormat.class);
      job.setOutputFormatClass(TextOutputFormat.class);

      FileInputFormat.addInputPath(job, new Path(args[0]));    
      FileOutputFormat.setOutputPath(job, new Path(args[1]));

      job.waitForCompletion(true);
      
      return 0;
   }
   
   public static class Map extends Mapper<LongWritable, Text, LongWritable, FCWritable> {
      private final static IntWritable ONE = new IntWritable(1);
      private Text word = new Text();

      @Override
      public void map(LongWritable key, Text value, Context context)
              throws IOException, InterruptedException {
    	  // variable setup
    	  String input[] = value.toString().split("\\t");
    	  Long User = Long.parseLong(input[0]);
    	  Vector<Long> friendList = new Vector<Long>();
    	  
    	  if(input.length == 2)
    	  {
    		  // read friendList and store to arrayList;
    		  String tmpList[] = input[1].toString().split(",");
    		  for(int index = 0; index < tmpList.length; index++)
    		  {
    			  Long tmpValue = Long.parseLong(tmpList[index]);
    			  friendList.add(tmpValue);
    		  }
    		  
    		  // store friend relationship into map
    		  // set mutual friend to be -1 to present friend relationship between recommendation and user
    		  for(int index = 0; index < friendList.size(); index++)
    		  {
    			  FCWritable tmpFC = new FCWritable(friendList.get(index), -1L);
    			  LongWritable tmpVal = new LongWritable(User);
    			  context.write(tmpVal, tmpFC);
    		  }
    		  
    		  // set recommendation list
        	  for(int index = 0; index < friendList.size(); index++)
        	  {
        		  for(int index2 = 0; index2 < friendList.size(); index2++)
        		  {
        			  if(index == index2)
        			  {
        				  continue;
        			  }
        			  context.write(new LongWritable(friendList.get(index)), new FCWritable(friendList.get(index2), 0L));	  
        		  }
        	  }
    	  }
    	  else if(input.length == 1){
    		  context.write(new LongWritable(User), new FCWritable(-1L,-2L));
    	  }
      }
   }

   public static class Reduce extends Reducer<LongWritable, FCWritable, LongWritable, Text> {
      @Override
      public void reduce(LongWritable key, Iterable<FCWritable> values, Context context)
              throws IOException, InterruptedException {
    	  // setup
    	  // Hashmap: key = RecommendFriend, Value = number of recommendation time
    	  HashMap<Long, Long> record = new HashMap<Long, Long>();
    	  
    	  // store all possible values into table
    	  boolean noFriend = false;
    	  for(FCWritable CurrentVal: values)
    	  {
    		  Long tmpVal = CurrentVal.User; // recommend friend
    		  Long tmpVal2 = CurrentVal.Friend; // mutual friend
    		  boolean check = false;
    		 
    		  //check if no friend at all
    		  if(tmpVal == -1 && tmpVal2 == -2)
    		  {
    			  noFriend = true;
    			  break;
    		  }
    			  
    		  // read recommend list
    		  if(record.containsKey(tmpVal))
    		  {
    			  if(tmpVal2 == -1)
    			  {
    				  record.put(tmpVal, record.get(tmpVal) - record.get(tmpVal) - 1);
    			  }
    			  else 
    			  {
    				  if(record.get(tmpVal) != -1)
    				  {
    					  record.put(tmpVal, record.get(tmpVal)+1);
    				  }
    			  }
    		  }
    		  else
    		  {
    			  if(tmpVal2 == -1)
    			  {
        			  record.put(tmpVal, -1L);
    			  }
    			  else
    			  {
        			  record.put(tmpVal, 1L);
    			  }
    		  }		  
    	  }
    	  
    	  // print no friend list row
    	  if(noFriend == true)
    	  {
    		  String output = "";
    		  context.write(key, new Text(output));
    		  return;
    	  }
    	  
    	  //get recommendation from record      
    	  TreeMap<Long, Long> treeMap = new TreeMap<Long, Long>(record);
    	  Vector<Long[]> table = new Vector<Long[]>();
    	  
    	  for (Entry<Long, Long> entry : treeMap.entrySet()) {
    		  Long tmp[] = new Long[2];
    	      tmp[0] = entry.getKey();
    	      tmp[1] = entry.getValue();
    	      if(tmp[1] == -1)
    	      {
    	    	  continue;
    	      }
    	      table.add(tmp);
    	  }
    	    	  
    	  // sort table2 to generate top 10 results
    	  for(int index = 1; index < table.size(); index++)
    	  {
    		  Long[] tmpVal = new Long[2];
    		  tmpVal[0] = table.get(index)[0];
    		  tmpVal[1] = table.get(index)[1];
    		  
    		  int index2 = index - 1;
    		  while(index2 >= 0 && table.get(index2)[1] < tmpVal[1])
    		  {
    			  table.get(index2 + 1)[0] =  table.get(index2)[0];
    			  table.get(index2 + 1)[1] =  table.get(index2)[1];
    			  index2 = index2 - 1;
    		  }
    		  
    		  table.get(index2 + 1)[0] = tmpVal[0];
    		  table.get(index2 + 1)[1] =  tmpVal[1];  
    	  }
    	  
    	  String output = "";
    	  if(table.size() > 10)
    	  {
    		  for(int index = 0; index < 10; index++)
        	  {
        		  if(index == 9)
        		  {
        			  output = output + table.get(index)[0];
        		  }
        		  else{
            		  output = output + table.get(index)[0] + ",";
        		  }
        	  }
    	  }
    	  else
    	  {
    		  for(int index = 0; index < table.size(); index++)
        	  {
        		  if(index == table.size() - 1)
        		  {
        			  output = output + table.get(index)[0];
        		  }
        		  else{
            		  output = output + table.get(index)[0] + ",";
        		  }
        	  }
    	  }
    	  
    	  context.write(key, new Text(output));
      }
   }
   
}
