package searchengine.utils;

import lombok.experimental.UtilityClass;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class TextHandler {
    private static final String FILTER_PARTS_OF_SPEECH = "МЕЖД|СОЮЗ|ПРЕДЛ";
    private static final String REGEX_FOR_HTML_TAGS = "</?[a-z]+\\s*[\\w\\s='\"]*/?>";

    public static HashMap<String, Integer> getLemmas(String text) {
         return Arrays.stream(text.replaceAll("\\p{Punct}|\\n", " ").split("\\s+"))
                .map(String::toLowerCase)
                .flatMap(TextHandler::convertToLemma)
                .collect(Collectors.toMap(
                        Function.identity(),
                        el -> 1,
                        (existValue, currentValue) -> existValue + 1,
                        HashMap::new
                ));
    }

    private static Stream<String> convertToLemma(String word) {
        Stream<String> stream;
        try {
            LuceneMorphology luceneMorphology = new RussianLuceneMorphology();
            stream = luceneMorphology.getNormalForms(word).stream().filter(lemma -> {
                String partOfSpeech = luceneMorphology.getMorphInfo(lemma).get(0);
                return filterPartOfSpeech(partOfSpeech);
            });
        } catch (IOException e) {
            throw new RuntimeException();
        }
        return stream;
    }

    private static boolean filterPartOfSpeech(String partOfSpeech) {
        Pattern pattern = Pattern.compile(FILTER_PARTS_OF_SPEECH);
        Matcher matcher = pattern.matcher(partOfSpeech);
        return !matcher.find();

    }

    public static String clearTextOfHtmlTags(String text) {
        return text.replaceAll(REGEX_FOR_HTML_TAGS, "");
    }
}
