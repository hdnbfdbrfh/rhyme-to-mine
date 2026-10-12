package dev.bryan.rhymetomine;

import java.util.*;
import java.util.regex.*;

/** First-version spoken-English rhyme lexicon. Every unlock word MUST be part of a rhyme pair. */
public final class Rhymes {
    private Rhymes() {}
    // Rhyming word -> rhyme sound group. This is intentionally conservative, not suffix matching.
    private static final Map<String,String> SOUND = new HashMap<>();
    private static final Map<String,String> BLOCK_WORD = new HashMap<>();
    static {
        add("ohn", "stone", "bone", "bones", "alone", "known", "grown", "throne", "phone", "own");
        add("og", "log", "frog", "dog", "bog", "fog", "hog", "jog");
        add("oor", "ore", "floor", "door", "four", "more", "core", "shore", "store", "before");
        add("and", "sand", "hand", "land", "band", "stand", "brand", "grand");
        add("ood", "wood", "good", "could", "should", "hood", "stood");
        add("irt", "dirt", "shirt", "hurt", "skirt");
        add("ass", "glass", "class", "grass", "pass", "mass");
        add("ice", "ice", "nice", "rice", "twice", "price", "slice", "mice");
        add("ow", "snow", "glow", "flow", "grow", "show", "slow", "throw");
        add("ick", "brick", "stick", "quick", "thick", "pick", "trick");
        add("old", "gold", "cold", "old", "told", "bold", "hold");
        add("eel", "steel", "feel", "real", "wheel", "peel", "deal");
        add("ed", "red", "bed", "head", "said", "bread", "dead");
        add("ight", "light", "night", "bright", "right", "fight", "sight", "white");
        add("ate", "plate", "gate", "late", "eight", "wait", "great", "state");
        add("air", "stair", "chair", "pair", "there", "where", "bear", "care");
        add("ool", "wool", "cool", "pool", "rule", "school", "tool");
        for (String s : List.of("stone","bone","bones","log","ore","door","sand","wood","dirt","glass","grass","ice","snow","brick","gold","red","bed","light","white","gate","stair","wool")) BLOCK_WORD.put(s,s);
    }
    private static void add(String group, String... words) { for (String w:words) SOUND.put(w,group); }

    /** Returns only block terms that participated in a true pronunciation rhyme. */
    public static Set<String> detect(String sentence) {
        List<String> words = new ArrayList<>();
        Matcher m = Pattern.compile("[a-zA-Z']+").matcher(sentence.toLowerCase(Locale.ROOT));
        while (m.find()) words.add(m.group());
        Set<String> used = new HashSet<>();
        for (int i=0;i<words.size();i++) for (int j=i+1;j<words.size();j++) {
            String a=words.get(i), b=words.get(j);
            if (!a.equals(b) && SOUND.containsKey(a) && SOUND.get(a).equals(SOUND.get(b))) { used.add(a); used.add(b); }
        }
        Set<String> result = new LinkedHashSet<>();
        for (String w:used) if (BLOCK_WORD.containsKey(w)) result.add(BLOCK_WORD.get(w));
        // "diamond ore" specifically needed for diamond ores: the 'ore' must be rhymed.
        if (result.contains("ore")) {
            int index=words.indexOf("ore");
            if (index>0 && words.get(index-1).equals("diamond")) result.add("diamond_ore");
            if (index>0 && words.get(index-1).equals("iron")) result.add("iron_ore");
            if (index>0 && words.get(index-1).equals("gold")) result.add("gold_ore");
        }
        return result;
    }
}
