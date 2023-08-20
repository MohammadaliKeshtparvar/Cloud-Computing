import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class TweetMapper extends Mapper<Object, Text, Text, IntWritable> {

    private final static String[] states = {"York", "Texas", "California", "Florida"};
    private final static IntWritable one = new IntWritable(1);
    
    @Override
    protected void map(Object key, Text value, Mapper<Object, Text, Text, IntWritable>.Context context)
            throws IOException, InterruptedException {
        CSVParser parser = CSVParser.parse(value.toString(), CSVFormat.DEFAULT);
        CSVRecord record = parser.iterator().next();
        String hashtags = record.get(2);
        if (hashtags.equals("tweet"))
            return;

        String stateField = record.get(18);
        if (stateField == null || stateField.isEmpty())
            return;
        stateField = stateField.toLowerCase();

        int createTimeHour = Integer.parseInt(record.get(0).split(" ")[1].split(":")[0]);
        if (!(createTimeHour >= 9 && createTimeHour < 18)) {
            return;
        }

        boolean forBiden = hashtags.contains("#Biden") || hashtags.contains("#JoeBiden");
        boolean forTrump = hashtags.contains("#Trump") || hashtags.contains("#DonaldTrump");

        String outKey;
        if (forBiden && forTrump)
            outKey = "Common";
        else if (forBiden)
            outKey = "Biden";
        else if (forTrump)
            outKey = "Trump";
        else return;

        for (String state : states) {
            if (stateField.contains(state.toLowerCase())) {
                context.write(new Text(state), one);
                context.write(new Text(state + "_" + outKey), one);
            }
        }
    }
}