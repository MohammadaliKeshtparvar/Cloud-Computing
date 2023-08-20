package first;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class TweetMapper extends Mapper<Object, Text, Text, FloatWritable> {

    @Override
    protected void map(Object key, Text value, Mapper<Object, Text, Text, FloatWritable>.Context context)
            throws IOException, InterruptedException {
        CSVParser parser = CSVParser.parse(value.toString(), CSVFormat.DEFAULT);
        CSVRecord record = parser.iterator().next();
        String hashtags = record.get(2);
        if (hashtags.equals("tweet")) return;

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

        float numberOfLikes = Float.parseFloat(record.get(3));
        FloatWritable likes = new FloatWritable(numberOfLikes);

        float numberOfRetweets = Float.parseFloat(record.get(4));
        FloatWritable retweets = new FloatWritable(numberOfRetweets);

        String sourceOfTweet = record.get(5);

        String sourceKey = "";
        if (sourceOfTweet.contains("Web"))
            sourceKey = "WebSource";
        else if (sourceOfTweet.contains("iPhone")) {
            sourceKey = "IPhoneSource";
        } else if (sourceOfTweet.contains("Android")) {
            sourceKey = "AndroidSource";
        }

        Text likesRecord = new Text(outKey + "Like");
        Text retweetsRecord = new Text(outKey + "Retweet");

        context.write(likesRecord, likes);
        context.write(retweetsRecord, retweets);

        if (!"".equals(sourceKey)) {
            Text sourceRecord = new Text(outKey + sourceKey);
            context.write(sourceRecord, new FloatWritable(1F));
        }
    }
}