package com.edutechnologic.industrialbadger.content.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;

import com.edutechnologic.industrialbadger.base.config.AppStorage;
import com.edutechnologic.industrialbadger.base.nlp.SentenceDetectorHolder;
import com.edutechnologic.industrialbadger.base.nlp.TokenizerHolder;
import com.edutechnologic.industrialbadger.base.service.ThreadService;
import com.edutechnologic.industrialbadger.base.util.NotificationManager;
import com.edutechnologic.industrialbadger.base.util.StringUtil;
import com.edutechnologic.industrialbadger.content.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import ktx.sovereign.database.entity.Content;
import ktx.sovereign.database.repository.KeywordRepository;

import static com.edutechnologic.industrialbadger.base.util.NotificationManager.ARG_CHANNEL_DESCRIPTION;
import static com.edutechnologic.industrialbadger.base.util.NotificationManager.ARG_CHANNEL_ID;
import static com.edutechnologic.industrialbadger.base.util.NotificationManager.ARG_CHANNEL_IMPORTANCE;
import static com.edutechnologic.industrialbadger.base.util.NotificationManager.ARG_CHANNEL_NAME;
import static com.edutechnologic.industrialbadger.base.util.NotificationManager.ARG_CONTENT_TEXT;
import static com.edutechnologic.industrialbadger.base.util.NotificationManager.ARG_CONTENT_TITLE;
import static com.edutechnologic.industrialbadger.base.util.NotificationManager.ARG_NOTIFICATION_ID;
import static com.edutechnologic.industrialbadger.base.util.NotificationManager.ARG_PRIORITY;
import static com.edutechnologic.industrialbadger.base.util.NotificationManager.ARG_PROGRESS_CURRENT;
import static com.edutechnologic.industrialbadger.base.util.NotificationManager.ARG_PROGRESS_MAX;
import static com.edutechnologic.industrialbadger.base.util.NotificationManager.ARG_SMALL_ICON;
import static com.edutechnologic.industrialbadger.base.util.NotificationManager.CHANNEL_TEXT_ANALYSIS;
import static com.edutechnologic.industrialbadger.base.util.NotificationManager.CHANNEL_TEXT_ANALYSIS_DESCRIPTION;
import static com.edutechnologic.industrialbadger.base.util.NotificationManager.CHANNEL_TEXT_ANALYSIS_TITLE;

/**
 * Created by H.D. "Chip" McCullough on 4/3/2019.
 * https://www.programcreek.com/java-api-examples/index.php?source_dir=jatetoolkit-master/src/main/java/uk/ac/shef/dcs/oak/jate/core/algorithm/RAKEAlgorithm.java#
 */
public class TextMiningService extends ThreadService {
    private static final String TAG = TextMiningService.class.getSimpleName();

    public static final String ACTION_RAKE = "com.industrialbadger.service.mining.RAKE";
    public static final String EXTRA_CONTENT = "com.industrialbadger.service.mining#EXTRA_CONTENT";

    private static final String NOTIFICATION_PREPARING_TITLE = "Preparing...";
    private static final String NOTIFICATION_PREPARING = "Preparing %s";
    private static final String NOTIFICATION_ANALYZING = "Analyzing %s";
    private static final String NOTIFICATION_EXTRACTING = "Extracting keywords from %s";
    private static final String NOTIFICATION_FINISHED = "Finished analyzing %s";

    private static final String NOTIFICATION_PROGRESS = "%s of 100";

    private static final String TERM_CLEAN_PATTERN_RAKE = "[^a-zA-Z0-9\\-.'&_]";

    private static final int MIN_CHARACTERS_IN_WORD = 2;
    private static final int METHOD_RAKE = 1;

    private static final Map<String, Integer> mNotificationIdMap = new HashMap<>();
    private NotificationManager mNotificationManager;

    public static void startActionRake(@NonNull Context context, @NonNull Content content) {
        Log.d(TAG, "startActionRake");
        Intent intent = new Intent(context, TextMiningService.class);
        intent.setAction(ACTION_RAKE);
        intent.putExtra(EXTRA_CONTENT, content);
        context.startService(intent);
    }

    public TextMiningService() {
        super(TAG);
    }

    /**
     * Called after {@link #onCreate()} has been called and the initial service thread has been
     * set up.
     */
    @Override
    protected void onPostCreate() {
        super.onPostCreate();
        mNotificationManager = new NotificationManager(TextMiningService.this);
    }

    /**
     * This method is invoked on the worker thread with a request to process.
     * Only one Intent is processed at a time, but the processing happens on a
     * worker thread that runs independently from other application logic.
     * So, if this code takes a long time, it will hold up other requests to
     * the same IntentService, but it will not hold up anything else.
     * When all requests have been handled, the IntentService stops itself,
     * so you should not call {@link #stopSelf}.
     *
     * @param intent The value passed to {@link
     *               Context#startService(Intent)}.
     *               This may be null if the service is being restarted after
     *               its process has gone away; see
     *               {@link Service#onStartCommand}
     *               for details.
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent");

        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_RAKE:
                    onRapidAutomaticKeywordExtraction((Content)intent.getParcelableExtra(EXTRA_CONTENT));
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
     */
    @Override
    public void onDestroy() {
        mNotificationManager = null;
        SentenceDetectorHolder.free();
        TokenizerHolder.free();
        super.onDestroy();
    }

    private void onRapidAutomaticKeywordExtraction(@NonNull Content content) {
        Log.d(TAG, "onRapidAutomaticKeywordExtraction");
        Bundle args;
        int notificationId = getRelatedNotificationId(content);
        mNotificationManager.showNotification(createNotificationBundle(
                "Preparing", String.format(NOTIFICATION_PREPARING, content.getTitle()), notificationId
        ));

//        KeywordRepository repo = new KeywordRepository(getApplication());
        List<String> candidates = digest(readFile(content), content.getTitle(), notificationId);

        args = createNotificationBundle(
                "Extracting...",
                String.format(NOTIFICATION_EXTRACTING, content.getTitle()),
                notificationId
        );
        args.putInt(ARG_PROGRESS_MAX, 0);
        args.putInt(ARG_PROGRESS_CURRENT, 0);
        mNotificationManager.showNotification(args);

        Keyword[] keywords = rake(candidates, content.getTitle(), notificationId);
        for (Keyword keyword : keywords) {
            if (keyword.getConfidence() > 1.5f) {
//                ExtractedKeyword extracted = new ExtractedKeyword(content.getId(),
//                        keyword.getTerm(), keyword.getConfidence(), METHOD_RAKE);
//                repo.insertExtractedKeyword(extracted);
            }
        }

        args = createNotificationBundle(
                "Finished",
                String.format(NOTIFICATION_FINISHED, content.getTitle()),
                notificationId
        );
        args.putInt(ARG_PROGRESS_MAX, 0);
        args.putInt(ARG_PROGRESS_CURRENT, 0);
        mNotificationManager.showNotification(args);
    }

    @SuppressWarnings("all")
    private int getRelatedNotificationId(@NonNull Content content) {
        Log.d(TAG, "getRelatedNotificationId");
        if (mNotificationIdMap.containsKey(content.getChecksum()))
            return mNotificationIdMap.get(content.getChecksum());

        int id = new Random().nextInt();

        mNotificationIdMap.put(content.getChecksum(), id);
        return id;
    }

    private String readFile(@NonNull Content content) {
        Log.d(TAG, "readFile");
        StringBuilder sb = new StringBuilder();
        File file = AppStorage.GetExternalFile(
                TextMiningService.this, AppStorage.EXTERNAL_CONTENT_DIR, content.getPath()
        );

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        } catch (FileNotFoundException ex) {
            Log.e(TAG, ex.getMessage());
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
        }

        return Html.fromHtml(sb.toString(), Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH).toString();
    }

    private int calculateProgress(int index, int size) {
        Log.d(TAG, "calculateProgress");
        return (int)(((double)index / (double)size) * 100.0);
    }

    private List<String> digest(@NonNull String text, @NonNull String title, int notificationId) {
        Log.d(TAG, "digest");
        int progress;
        ArrayList<String> res = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        try {
            String[] sentences = SentenceDetectorHolder.getInstance(TextMiningService.this)
                    .detect(text);
            for (int i = 0; i < sentences.length; i++) {
                progress = calculateProgress(i, sentences.length);
                Bundle args = createNotificationBundle(
                        "Analyzing...",
                        String.format(NOTIFICATION_PROGRESS, progress),
                        notificationId);
                args.putInt(ARG_PROGRESS_MAX, 100);
                args.putInt(ARG_PROGRESS_CURRENT, progress);
                mNotificationManager.showNotification(args);
                res.addAll(extract(sentences[i]));
            }
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
        }

        Log.d(TAG, String.format("Digest Runtime: %d", System.currentTimeMillis() - startTime));
        return res;
    }

    private List<String> extract(@NonNull String text) {
        Log.d(TAG, "extract");
        ArrayList<String> res = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        int counter = 0;
        StringBuilder sb = new StringBuilder();

        try {
            StopWords stopWords = new StopWords();
            String[] tokens = TokenizerHolder.getInstance(TextMiningService.this).tokenize(text);
            for (String token : tokens) {


                boolean end = false;
                if (token.matches("^\\.\\[[0-9]*.*"))
                    end = true;
                else if (token.matches("^\\[?[0-9]*\\].*"))
                    continue;

                String candidate = cleanCandidate(token);

                if (candidate.equals(".e")) continue;
                counter++;

                if (sb.length() > 0 && sb.toString().endsWith(".")) {
                    res.add(sb.toString().replace('.', ' ').trim());
                    sb.setLength(0);
                }

                if (candidate.startsWith("'")) {
                    sb.append(candidate);
                    continue;
                }

                if (end || (!StringUtil.containsCharacter(candidate) && !StringUtil.containsDigit(candidate))
                        || candidate.length() < MIN_CHARACTERS_IN_WORD
                        || (stopWords.isStopWord(candidate) || stopWords.isStopWord(token.trim()))) {
                    if (sb.length() != 0) {
                        res.add(sb.toString().trim());
                        sb.setLength(0);
                    }
                } else {
                    sb.append(" ").append(candidate);
                }

                if (counter == tokens.length && sb.length() > 0) {
                    res.add(sb.toString().trim());
                    sb.setLength(0);
                }
            }
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
        }

        Log.d(TAG, String.format("Extraction Runtime: %d", System.currentTimeMillis() - startTime));
        return res;
    }

    private String cleanCandidate(@NonNull String s) {
        return s.replaceAll(TERM_CLEAN_PATTERN_RAKE, " ")
                .replaceAll("\\s+", " ").trim().toLowerCase();
    }

    private Keyword[] rake(List<String> phrases, @NonNull String title, int notificationId) {
        Log.d(TAG, "rake");
        Map<String, Double> scores = calculateWordScores(phrases, title, notificationId);
        return calculateResults(phrases, scores, title, notificationId);
    }

    @SuppressWarnings("all")
    private Map<String, Double> calculateWordScores(List<String> phrases, @NonNull String title, int notificationId) {
        Log.d(TAG, "calculateWordScores");
        Bundle args;
        int progress;
        Map<String, Integer> freqMap = new HashMap<>();
        Map<String, Integer> degMap = new HashMap<>();
        Map<String, Double> score = new HashMap<>();

        for (String phrase : phrases) {
            progress = calculateProgress(phrases.indexOf(phrase) + 1, phrases.size() * 2);
            args = createNotificationBundle(
                    "Extracting...",
                    String.format(NOTIFICATION_PROGRESS, progress),
                    notificationId
            );
            args.putInt(ARG_PROGRESS_MAX, 100);
            args.putInt(ARG_PROGRESS_CURRENT, progress);
            mNotificationManager.showNotification(args);

            String[] words = splitPhrase(phrase);
            int len = words.length;
            int deg = len - 1;
            for (String word : words) {
                Integer freq;

                if (!freqMap.containsKey(word)) {
                    freqMap.put(word, 1);
                } else {
                    freq = freqMap.get(word);
                    freqMap.remove(word);
                    freqMap.put(word, freq == null ? 1 : freq + 1);
                }

                Integer degree;

                if (!degMap.containsKey(word)) {
                    degMap.put(word, deg);
                } else {
                    degree = degMap.get(word);
                    degMap.remove(word);
                    degMap.put(word, degree == null ? deg : degree + deg);
                }
            }
        }

        for (Map.Entry<String, Integer> entry : degMap.entrySet())
            entry.setValue(entry.getValue() + freqMap.get(entry.getKey()));

        for (Map.Entry<String, Integer> entry : freqMap.entrySet())
            score.put(entry.getKey(), (double)degMap.get(entry.getKey()) / (double)freqMap.get(entry.getKey()));

        return score;
    }

    @SuppressWarnings("all")
    private Keyword[] calculateResults(List<String> phrases, Map<String, Double> scores,
                                       @NonNull String title, int notificationId) {
        Log.d(TAG, "calculateResults");
        Bundle args;
        int progress;
        Set<Keyword> results = new HashSet<>();

        for (String candidate : phrases) {
            progress = calculateProgress(phrases.size() + phrases.indexOf(candidate), phrases.size() * 2);
            args = createNotificationBundle(
                    "Extracting...",
                    String.format(NOTIFICATION_PROGRESS, progress),
                    notificationId
            );
            args.putInt(ARG_PROGRESS_MAX, 100);
            args.putInt(ARG_PROGRESS_CURRENT, progress);
            String[] words = splitPhrase(candidate);
            double score = 0.0;
            for (String word : words) {
                if (scores.containsKey(word))
                    score += scores.get(word);
            }
            results.add(new Keyword(candidate, score));
        }

        Keyword[] keywords = results.toArray(new Keyword[results.size()]);
        Arrays.sort(keywords);
        return keywords;
    }

    private String[] splitPhrase(@NonNull String phrase) {
        return phrase.split(" ");
    }

    private Bundle createNotificationBundle(@NonNull String title, @NonNull String text, int notificationId) {
        Bundle args = new Bundle();
        args.putString(ARG_CHANNEL_ID, CHANNEL_TEXT_ANALYSIS);
        args.putString(ARG_CHANNEL_NAME, CHANNEL_TEXT_ANALYSIS_TITLE);
        args.putString(ARG_CHANNEL_DESCRIPTION, CHANNEL_TEXT_ANALYSIS_DESCRIPTION);
        args.putInt(ARG_CHANNEL_IMPORTANCE, NotificationManagerCompat.IMPORTANCE_DEFAULT);
        args.putInt(ARG_SMALL_ICON, R.drawable.ic_content);
        args.putString(ARG_CONTENT_TITLE, title);
        args.putString(ARG_CONTENT_TEXT, text);
        args.putInt(ARG_PRIORITY, NotificationCompat.PRIORITY_DEFAULT);
        args.putInt(ARG_NOTIFICATION_ID, notificationId);
        return args;
    }

    public class Keyword implements Comparable<Keyword> {

        private String mTerm;
        private double mConfidence;

        Keyword(String lemma, double confidence) {
            mTerm = lemma;
            mConfidence = confidence;
        }

        public String getTerm() {
            return mTerm;
        }

        protected void setTerm(String term) {
            mTerm = term;
        }

        public double getConfidence() {
            return mConfidence;
        }

        protected void setConfidence(double confidence) {
            mConfidence = confidence;
        }

        /**
         * Compares this object with the specified object for order.  Returns a
         * negative integer, zero, or a positive integer as this object is less
         * than, equal to, or greater than the specified object.
         *
         * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
         * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
         * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
         * <tt>y.compareTo(x)</tt> throws an exception.)
         *
         * <p>The implementor must also ensure that the relation is transitive:
         * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
         * <tt>x.compareTo(z)&gt;0</tt>.
         *
         * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
         * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
         * all <tt>z</tt>.
         *
         * <p>It is strongly recommended, but <i>not</i> strictly required that
         * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
         * class that implements the <tt>Comparable</tt> interface and violates
         * this condition should clearly indicate this fact.  The recommended
         * language is "Note: this class has a natural ordering that is
         * inconsistent with equals."
         *
         * <p>In the foregoing description, the notation
         * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
         * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
         * <tt>0</tt>, or <tt>1</tt> according to whether the value of
         * <i>expression</i> is negative, zero or positive.
         *
         * @param o the object to be compared.
         * @return a negative integer, zero, or a positive integer as this object
         * is less than, equal to, or greater than the specified object.
         * @throws NullPointerException if the specified object is null
         * @throws ClassCastException   if the specified object's type prevents it
         *                              from being compared to this object.
         */
        @Override
        public int compareTo(@NonNull Keyword o) {
            return Double.compare(o.getConfidence(), mConfidence);
        }

        /**
         * Indicates whether some other object is "equal to" this one.
         * <p>
         * The {@code equals} method implements an equivalence relation
         * on non-null object references:
         * <ul>
         * <li>It is <i>reflexive</i>: for any non-null reference value
         * {@code x}, {@code x.equals(x)} should return
         * {@code true}.
         * <li>It is <i>symmetric</i>: for any non-null reference values
         * {@code x} and {@code y}, {@code x.equals(y)}
         * should return {@code true} if and only if
         * {@code y.equals(x)} returns {@code true}.
         * <li>It is <i>transitive</i>: for any non-null reference values
         * {@code x}, {@code y}, and {@code z}, if
         * {@code x.equals(y)} returns {@code true} and
         * {@code y.equals(z)} returns {@code true}, then
         * {@code x.equals(z)} should return {@code true}.
         * <li>It is <i>consistent</i>: for any non-null reference values
         * {@code x} and {@code y}, multiple invocations of
         * {@code x.equals(y)} consistently return {@code true}
         * or consistently return {@code false}, provided no
         * information used in {@code equals} comparisons on the
         * objects is modified.
         * <li>For any non-null reference value {@code x},
         * {@code x.equals(null)} should return {@code false}.
         * </ul>
         * <p>
         * The {@code equals} method for class {@code Object} implements
         * the most discriminating possible equivalence relation on objects;
         * that is, for any non-null reference values {@code x} and
         * {@code y}, this method returns {@code true} if and only
         * if {@code x} and {@code y} refer to the same object
         * ({@code x == y} has the value {@code true}).
         * <p>
         * Note that it is generally necessary to override the {@code hashCode}
         * method whenever this method is overridden, so as to maintain the
         * general contract for the {@code hashCode} method, which states
         * that equal objects must have equal hash codes.
         *
         * @param obj the reference object with which to compare.
         * @return {@code true} if this object is the same as the obj
         * argument; {@code false} otherwise.
         * @see #hashCode()
         * @see HashMap
         */
        @Override
        public boolean equals(@Nullable Object obj) {
            // True, if self
            if (Keyword.this == obj) return true;

            // True if obj.getTerm() := this.getTerm() && obj.getConfidence() := this.getConfidence()
            if (obj instanceof Keyword) {
                final Keyword keyword = (Keyword)obj;
                return keyword.getTerm().equals(Keyword.this.getTerm()) &&
                        keyword.getConfidence() == Keyword.this.getConfidence();
            }

            // True if typeof(obj) := String && obj := this.getTerm()
            if (obj instanceof String)
                return obj.equals(Keyword.this.getTerm());

            // True if typeof(obj) := Double && obj := this.getConfidence();
            if (obj instanceof Double)
                return obj.equals(Keyword.this.getConfidence());

            return false;
        }

        /**
         * Returns a hash code value for the object. This method is
         * supported for the benefit of hash tables such as those provided by
         * {@link HashMap}.
         * <p>
         * The general contract of {@code hashCode} is:
         * <ul>
         * <li>Whenever it is invoked on the same object more than once during
         * an execution of a Java application, the {@code hashCode} method
         * must consistently return the same integer, provided no information
         * used in {@code equals} comparisons on the object is modified.
         * This integer need not remain consistent from one execution of an
         * application to another execution of the same application.
         * <li>If two objects are equal according to the {@code equals(Object)}
         * method, then calling the {@code hashCode} method on each of
         * the two objects must produce the same integer result.
         * <li>It is <em>not</em> required that if two objects are unequal
         * according to the {@link Object#equals(Object)}
         * method, then calling the {@code hashCode} method on each of the
         * two objects must produce distinct integer results.  However, the
         * programmer should be aware that producing distinct integer results
         * for unequal objects may improve the performance of hash tables.
         * </ul>
         * <p>
         * As much as is reasonably practical, the hashCode method defined by
         * class {@code Object} does return distinct integers for distinct
         * objects. (This is typically implemented by converting the internal
         * address of the object into an integer, but this implementation
         * technique is not required by the
         * Java&trade; programming language.)
         *
         * @return a hash code value for this object.
         * @see Object#equals(Object)
         * @see System#identityHashCode
         */
        @Override
        public int hashCode() {
            return mTerm.hashCode();
        }

        /**
         * Returns a string representation of the object. In general, the
         * {@code toString} method returns a string that
         * "textually represents" this object. The result should
         * be a concise but informative representation that is easy for a
         * person to read.
         * It is recommended that all subclasses override this method.
         * <p>
         * The {@code toString} method for class {@code Object}
         * returns a string consisting of the name of the class of which the
         * object is an instance, the at-sign character `{@code @}', and
         * the unsigned hexadecimal representation of the hash code of the
         * object. In other mWords, this method returns a string equal to the
         * value of:
         * <blockquote>
         * <pre>
         * getClass().getName() + '@' + Integer.toHexString(hashCode())
         * </pre></blockquote>
         *
         * @return a string representation of the object.
         */
        @NonNull
        @Override
        @SuppressWarnings("all")
        public String toString() {
            return new StringBuilder("Keyword")
                    .append("<").append(mTerm).append(",").append(mConfidence).append(">")
                    .toString();
        }
    }

    private class StopWords {
        private final Set<String> mWords = new HashSet<>();

        private StopWords mInstance;

        public StopWords getInstance() throws IOException{
            if (mInstance == null) {
                synchronized (TextMiningService.class) {
                    if (mInstance == null) mInstance = new StopWords();
                }
            }

            return mInstance;
        }

        private StopWords() throws IOException {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("stopwords.txt"))
            )) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.equals("") && !line.startsWith("//"))
                        mWords.add(line.toLowerCase());
                }
            }
        }

        public boolean isStopWord(@Nullable String s) {
            return StringUtil.isNullOrEmpty(s) || mWords.contains(s);
        }
    }
}
