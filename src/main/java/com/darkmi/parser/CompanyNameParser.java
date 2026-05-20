package com.darkmi.parser;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class CompanyNameParser {
    private static final String SEP = ",";

    private Map<String, String> places = new HashMap<>();
    private Map<String, String> brands = new HashMap<>();
    private Map<String, String> trades = new HashMap<>();
    private Map<String, String> suffixes = new HashMap<>();
    private Set<String> placeSingle = new HashSet<>();
    private Set<String> tradeSingle = new HashSet<>();
    private Set<String> suffixSingle = new HashSet<>();

    private Set<String> symbols = new HashSet<>(Arrays.asList("《", "》", "（", "）", "(", ")"));

    private boolean inited = false;
    private JiebaSegmenter segmenter;

    public CompanyNameParser() {
        segmenter = new JiebaSegmenter();
    }

    public void init() {
        if (inited) return;

        places = loadDict("china_place.txt");
        brands = loadDict("brand.txt");
        trades = loadDict("trade.txt");
        suffixes = loadDict("suffix.txt");
        placeSingle = loadSingleSet("place_single.txt");
        tradeSingle = loadSingleSet("trade_single.txt");
        suffixSingle = loadSingleSet("suffix_single.txt");

        inited = true;
    }

    private Map<String, String> loadDict(InputStream inputStream) {
        if (inputStream == null) {
            throw new RuntimeException("Cannot find resource, input stream is null");
        }
        Map<String, String> map = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\s+");
                if (parts.length == 2) {
                    map.put(parts[0], parts[1]);
                } else if (parts.length == 1) {
                    map.put(parts[0], "1");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    private Map<String, String> loadDict(String filePath) {
        InputStream is = getClass().getClassLoader().getResourceAsStream("data/" + filePath);
        if (is == null) {
            is = getClass().getResourceAsStream("/data/" + filePath);
        }
        return loadDict(is);
    }

    private Set<String> loadSingleSet(InputStream inputStream) {
        if (inputStream == null) {
            throw new RuntimeException("Cannot find resource, input stream is null");
        }
        Set<String> set = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    set.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return set;
    }

    private Set<String> loadSingleSet(String filePath) {
        InputStream is = getClass().getClassLoader().getResourceAsStream("data/" + filePath);
        return loadSingleSet(is);
    }

    private List<Token> jiebaTokenize(String sentence) {
        List<SegToken> segTokens = segmenter.process(sentence, JiebaSegmenter.SegMode.INDEX);
        List<Token> tokens = new ArrayList<>();
        for (SegToken st : segTokens) {
            tokens.add(new Token(st.word, st.startOffset, st.startOffset + st.word.length()));
        }
        return tokens;
    }

    private List<Token> extractTokens(List<Token> tokens, Set<String> dict) {
        List<Token> result = new ArrayList<>();
        for (Token t : tokens) {
            if (dict.contains(t.getWord())) {
                result.add(t);
            }
        }
        return result;
    }

    private List<Token> mergeOverlappingTokens(List<Token> tokens) {
        if (tokens.size() <= 1) return tokens;

        tokens.sort(Comparator.comparingInt(Token::getStart));

        List<Token> result = new ArrayList<>();
        Token current = tokens.get(0);

        for (int i = 1; i < tokens.size(); i++) {
            Token next = tokens.get(i);
            if (next.getStart() < current.getEnd()) {
                if (next.getWord().length() > current.getWord().length()) {
                    current = next;
                }
            } else if (next.getStart() == current.getEnd()) {
                result.add(current);
                current = next;
            } else {
                result.add(current);
                current = next;
            }
        }
        result.add(current);
        return result;
    }

    private List<Token> linkNearWords(List<Token> extracted, List<Token> postprocessTokens) {
        List<Token> allTokens = new ArrayList<>();
        if (extracted != null) allTokens.addAll(extracted);
        if (postprocessTokens != null) allTokens.addAll(postprocessTokens);

        if (allTokens.isEmpty()) return new ArrayList<>();

        allTokens.sort(Comparator.comparingInt(Token::getStart));

        List<Token> result = new ArrayList<>();
        Token current = allTokens.get(0);
        boolean currentFromExtracted = extracted != null && extracted.contains(current);

        for (int i = 1; i < allTokens.size(); i++) {
            Token next = allTokens.get(i);
            boolean nextFromExtracted = extracted != null && extracted.contains(next);

            if (next.getStart() == current.getEnd() && currentFromExtracted == nextFromExtracted) {
                current = new Token(current.getWord() + next.getWord(), current.getStart(), next.getEnd());
            } else {
                result.add(current);
                current = next;
                currentFromExtracted = nextFromExtracted;
            }
        }
        result.add(current);
        return result;
    }

    private List<Token> linkNearWords(List<Token> tokens) {
        List<Token> result = new ArrayList<>();
        if (tokens == null || tokens.isEmpty()) return result;

        Token first = tokens.get(0);
        StringBuilder sb = new StringBuilder(first.getWord());
        int start = first.getStart();
        int end = first.getEnd();

        for (int i = 1; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            if (t.getStart() == end) {
                sb.append(t.getWord());
                end = t.getEnd();
            } else {
                result.add(new Token(sb.toString(), start, end));
                sb = new StringBuilder(t.getWord());
                start = t.getStart();
                end = t.getEnd();
            }
        }
        result.add(new Token(sb.toString(), start, end));
        return result;
    }

    private void postprocess(List<Token> leftWords,
                             List<Token> places,
                             List<Token> brands,
                             List<Token> trades,
                             List<Token> suffixes) {
        List<Token> lefts = new ArrayList<>();

        for (Token t : leftWords) {
            String w = t.getWord();
            if (w.length() == 1) {
                if (tradeSingle.contains(w)) {
                    if (leftWords.size() == 2
                            && leftWords.get(0).getWord().length() == 1
                            && leftWords.get(1).getWord().length() == 1
                            && brands.isEmpty()) {
                        brands.add(t);
                    } else {
                        trades.add(t);
                    }
                } else if (placeSingle.contains(w)) {
                    places.add(t);
                } else if (suffixSingle.contains(w)) {
                    suffixes.add(t);
                } else {
                    lefts.add(t);
                }
            } else {
                String lastChar = w.substring(w.length() - 1);
                if (placeSingle.contains(lastChar)) {
                    places.add(t);
                } else {
                    brands.add(t);
                }
            }
        }

        if (lefts.size() == 1) {
            if (places.size() > 1
                    && places.get(0).getEnd() == lefts.get(0).getStart()
                    && places.get(1).getStart() == lefts.get(0).getEnd()) {
                places.add(lefts.get(0));
            } else if (trades.size() > 1
                    && lefts.get(0).getEnd() == trades.get(0).getStart()
                    && brands.isEmpty()) {
                brands.addAll(lefts);
                brands.add(trades.get(0));
                trades.remove(0);
            } else {
                brands.addAll(lefts);
            }
        } else {
            brands.addAll(lefts);
        }

        if (places.size() > 1) {
            places.sort(Comparator.comparingInt(Token::getStart));
            places = linkNearWords(places);
        }
        if (brands.size() > 1) {
            brands.sort(Comparator.comparingInt(Token::getStart));
            brands = linkNearWords(brands);
        }
    }

    private boolean isChinese(char c) {
        return '\u4e00' <= c && c <= '\u9fa5';
    }

    public CompanyName parse(String name) {
        return parse(name, false, false);
    }

    public CompanyName parse(String name, boolean posSensitive, boolean enableWordSegment) {
        CompanyName result = new CompanyName("", "", "", "", "");

        name = name.trim();
        if (name.isEmpty() || !isChinese(name.charAt(0))) {
            return result;
        }

        init();

        List<Token> tokens = jiebaTokenize(name);

        List<Token> allLeft = new ArrayList<>(tokens);

        List<Token> tSymbols = extractTokens(allLeft, symbols);
        allLeft = getLeftWords(allLeft, symbols);

        List<Token> tPlaces = extractTokens(allLeft, places.keySet());
        tPlaces = mergeOverlappingTokens(tPlaces);
        allLeft = getLeftWords(allLeft, places.keySet());

        List<Token> tSuffixes = extractTokens(allLeft, suffixes.keySet());
        tSuffixes = mergeOverlappingTokens(tSuffixes);
        allLeft = getLeftWords(allLeft, suffixes.keySet());

        List<Token> tTrades;
        if (tSuffixes.size() > 0) {
            int suffixStart = tSuffixes.stream().mapToInt(Token::getStart).min().orElse(0);
            List<Token> tTradesBefore = extractTokens(allLeft, trades.keySet());
            tTradesBefore = mergeOverlappingTokens(tTradesBefore);
            final int fs = suffixStart;
            tTradesBefore = tTradesBefore.stream()
                    .filter(t -> t.getEnd() <= fs)
                    .collect(Collectors.toList());
            allLeft = getLeftWords(allLeft, trades.keySet());
            List<Token> tTradesAfter = extractTokens(allLeft, trades.keySet());
            tTradesAfter = mergeOverlappingTokens(tTradesAfter);
            tTradesBefore.addAll(tTradesAfter);
            tTrades = tTradesBefore;
        } else {
            tTrades = extractTokens(allLeft, trades.keySet());
            tTrades = mergeOverlappingTokens(tTrades);
            allLeft = getLeftWords(allLeft, trades.keySet());
        }

        List<Token> tBrands = extractTokens(allLeft, brands.keySet());
        tBrands = mergeOverlappingTokens(tBrands);
        allLeft = getLeftWords(allLeft, brands.keySet());

        List<Token> extractedBrands = new ArrayList<>(tBrands);
        int extractedBrandCount = tBrands.size();
        postprocess(allLeft, tPlaces, tBrands, tTrades, tSuffixes);

        if (tPlaces.size() > 1) {
            tPlaces.sort(Comparator.comparingInt(Token::getStart));
        }
        if (tBrands.size() > 1) {
            tBrands.sort(Comparator.comparingInt(Token::getStart));
        }
        if (tTrades.size() > 1) {
            tTrades.sort(Comparator.comparingInt(Token::getStart));
        }
        if (tSuffixes.size() > 1) {
            tSuffixes.sort(Comparator.comparingInt(Token::getStart));
        }

        if (!enableWordSegment) {
            tPlaces = linkNearWords(tPlaces);
            tTrades = linkNearWords(tTrades);
            tSuffixes = linkNearWords(tSuffixes);
            if (extractedBrandCount > 0 && tBrands.size() > extractedBrandCount) {
                tBrands = tBrands.subList(0, extractedBrandCount);
            }
            if (tBrands.size() > 1) {
                tBrands = linkNearWords(tBrands);
            }
        }

        if (posSensitive) {
            result.setPlaceTokens(tPlaces);
            result.setBrandTokens(tBrands);
            result.setTradeTokens(tTrades);
            result.setSuffixTokens(tSuffixes);
            result.setSymbolTokens(tSymbols);
        } else {
            result.setPlace(tPlaces.stream().map(Token::getWord).collect(Collectors.joining(SEP)));
            result.setBrand(tBrands.stream().map(Token::getWord).collect(Collectors.joining(SEP)));
            result.setTrade(tTrades.stream().map(Token::getWord).collect(Collectors.joining(SEP)));
            result.setSuffix(tSuffixes.stream().map(Token::getWord).collect(Collectors.joining(SEP)));
            result.setSymbol(tSymbols.stream().map(Token::getWord).collect(Collectors.joining(SEP)));
        }

        return result;
    }

    private List<Token> getLeftWords(List<Token> tokens, Set<String> dict) {
        List<Token> left = new ArrayList<>();
        for (Token t : tokens) {
            if (!dict.contains(t.getWord())) {
                left.add(t);
            }
        }
        return left;
    }

    public static void main(String[] args) {
        CompanyNameParser parser = new CompanyNameParser();

        String[] names = {
                "武汉海明智业电子商务有限公司",
                "泉州益念食品有限公司",
                "常州途畅互联网科技有限公司合肥分公司",
                "昆明享亚教育信息咨询有限公司"
        };

        for (String name : names) {
            CompanyName cn = parser.parse(name);
            System.out.println(name);
            System.out.println("  place: " + cn.getPlace());
            System.out.println("  brand: " + cn.getBrand());
            System.out.println("  trade: " + cn.getTrade());
            System.out.println("  suffix: " + cn.getSuffix());
        }
    }
}