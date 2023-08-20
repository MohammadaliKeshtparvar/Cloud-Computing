import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class TweetDriver extends Configured implements Tool {

    public int run(String[] args) throws Exception {
        @SuppressWarnings("deprecation")
        Job job = new Job(getConf(), "likes and retweet counter");

        job.setMapperClass(TweetMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setReducerClass(TweetReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        int size = args.length;
        for (int i = 0; i < size - 1; i++)
            FileInputFormat.addInputPath(job, new Path(args[i]));
        FileOutputFormat.setOutputPath(job, new Path(args[size - 1]));

        boolean res = job.waitForCompletion(true);
        if (!res) return 1;

        printResult(args[size - 1]);

        return 0;
    }

    public static void printResult(String path) throws IOException {
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);
        RemoteIterator<LocatedFileStatus> fileStatusRemoteIterator = fs.listFiles(new Path(path), true);
        Path resultFile = null;
        while (fileStatusRemoteIterator.hasNext()) {
            LocatedFileStatus fileStatus = fileStatusRemoteIterator.next();
            Path p = fileStatus.getPath();
            if (p.toString().contains("part-r-")) {
                resultFile = p;
                break;
            }
        }
        FSDataInputStream stream = fs.open(resultFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));


        Map<String, Integer> map = new HashMap<>();
        String line = reader.readLine();
        while (line != null) {
            String[] keyValue = line.split("\\s+");
            String key = keyValue[0];
            int value = Integer.parseInt(keyValue[1]);
            map.put(key, value);
            line = reader.readLine();
        }

        System.out.println("--------------------------------------------------------------------------------");
        String[] states = {"York", "Texas"
                , "California", "Florida"};

        for (String state : states) {
            int all = 0;
            double commonPercent = 0;
            double trumpPercent = 0;
            double bidenPercent = 0;

            Integer contains = map.get(state);
            if (contains != null) {
                all = contains;
                commonPercent = map.get(state + "_" + "Common") / (double) all;
                trumpPercent = map.get(state + "_" + "Trump") / (double) all;
                bidenPercent = map.get(state + "_" + "Biden") / (double) all;
            }
            String outState = state.equals("York") ? "New York" : state;
            System.out.println(outState + "   " + commonPercent + "   " +
                    bidenPercent + "   " + trumpPercent + "    " + all);
        }
        System.out.println("--------------------------------------------------------------------------------");
    }

    public static void main(String[] args) throws Exception {
        int jobStatus = ToolRunner.run(new TweetDriver(), args);
        System.out.println(jobStatus);
    }
}
