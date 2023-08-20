package first;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.FloatWritable;
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

public class TweetDriver extends Configured implements Tool {
    public int run(String[] args) throws Exception {
        @SuppressWarnings("deprecation")
        Job job = new Job(getConf(), "likes , retweets and source counter");

        job.setMapperClass(TweetMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(FloatWritable.class);

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

        System.out.println("----------------------------------------------------------------------------------------------");
        String newLine = reader.readLine();
        while (newLine != null) {

            String like = "", retweet = "", androidSource = "", iPhoneSource = "", webSource = "";
            String[][] line = new String[5][2];
            line[0] = reader.readLine().split("\\s+");
            line[1] = reader.readLine().split("\\s+");
            line[2] = reader.readLine().split("\\s+");
            line[3] = reader.readLine().split("\\s+");
            line[4] = newLine.split("\\s+");

            for (String[] l : line) {
                if (l[0].contains("Android")) {
                    androidSource = l[1];
                } else if (l[0].contains("IPhone")) {
                    iPhoneSource = l[1];
                } else if (l[0].contains("Web")) {
                    webSource = l[1];
                } else if (l[0].contains("Like")) {
                    like = l[1];
                } else {
                    retweet = l[1];
                }
            }

            String key = line[0][0];
            if (key.contains("Biden"))
                key = "Biden";
            else if (key.contains("Trump"))
                key = "Trump";
            else key = "Both candidate";

            System.out.println(fixedLength(14, key) + " : " + fixedLength(14, "like=" + like)
                    + " | " + fixedLength(16, "retweet=" + retweet)
                    + " | " + fixedLength(16, "android=" + androidSource)
                    + " | " + fixedLength(14, "iPhone=" + iPhoneSource)
                    + " | " + fixedLength(15, "web=" + webSource));
            newLine = reader.readLine();
        }
        System.out.println("----------------------------------------------------------------------------------------------");
    }

    private static String fixedLength(int length, String input) {
        return String.format("%-" + length + "." + length + "s", input);
    }

    public static void main(String[] args) throws Exception {
        int jobStatus = ToolRunner.run(new TweetDriver(), args);
        System.out.println(jobStatus);
    }
}
