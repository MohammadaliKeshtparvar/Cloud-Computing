package third;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class TweetMapper extends Mapper<Object, Text, Text, IntWritable> {

    private final static IntWritable one = new IntWritable(1);

    @Override
    protected void map(Object key, Text value, Mapper<Object, Text, Text, IntWritable>.Context context)
            throws IOException, InterruptedException {
        CSVParser parser = CSVParser.parse(value.toString(), CSVFormat.DEFAULT);
        CSVRecord record = parser.iterator().next();
        String hashtags = record.get(2);
        if (hashtags.equals("tweet"))
            return;

        int createTimeHour = Integer.parseInt(record.get(0).split(" ")[1].split(":")[0]);
        if (!(createTimeHour >= 9 && createTimeHour < 18))
            return;

        String latString = record.get(13);
        String longString = record.get(14);
        if (latString == null || latString.isEmpty() || longString == null || longString.isEmpty())
            return;

        double latitude = Double.parseDouble(latString);
        double longitude = Double.parseDouble(longString);

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

        if (longitude < -71.7517 && longitude > -79.7624 && latitude < 45.0153 && latitude > 40.4772) {
            context.write(new Text("York"), one);
            context.write(new Text("York" + "_" + outKey), one);
        }
        if (longitude < -114.1315 && longitude > -124.6509 && latitude < 42.0126 && latitude > 32.5121) {
            context.write(new Text("California"), one);
            context.write(new Text("California" + "_" + outKey), one);
        }
    }
}