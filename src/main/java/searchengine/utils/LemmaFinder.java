package searchengine.utils;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LemmaFinder {
    private static final String REGEX_FOR_PARTICLES = "МЕЖД|СОЮЗ|ПРЕДЛ|ЧАСТ";
    private static final String RUSSIAN_WORDS = "[^а-я\\s]";
    private LuceneMorphology luceneMorphology;

    public static LemmaFinder getInstance() throws IOException {
        RussianLuceneMorphology russianLuceneMorphology = new RussianLuceneMorphology();
        return new LemmaFinder(russianLuceneMorphology);
    }

    private LemmaFinder(LuceneMorphology luceneMorphology) {
        this.luceneMorphology = luceneMorphology;
    }

    private LemmaFinder() {
    }

    public HashMap<String, Integer> getLemmas(String html) {
        String text = Jsoup.parse(html).text();
        String[] words = getWords(text, RUSSIAN_WORDS);

        return Arrays.stream(words)
                .filter((word) -> isNotBelongToParticles(luceneMorphology.getMorphInfo(word)))
                .map(word -> luceneMorphology.getNormalForms(word))
                .filter(list -> !list.isEmpty())
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(
                        Function.identity(),
                        value -> 1,
                        (oldValue, newValue) -> oldValue + 1,
                        HashMap::new
                ));

    }

    private boolean isNotBelongToParticles(List<String> morphInfo) {
        Pattern pattern = Pattern.compile(REGEX_FOR_PARTICLES);
        for (String item : morphInfo) {
            Matcher matcher = pattern.matcher(item);
            if (matcher.find()) {
                return false;
            }
        }
        return true;
    }

    private String[] getWords(String text, String words) {
        return text.toLowerCase()
                .replaceAll(words, " ")
                .trim()
                .split("\\s+");
    }
}
