import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BooleanSearchEngine implements SearchEngine {
    private Map<String, List<PageEntry>> wordsMap = new HashMap<>();

    public BooleanSearchEngine(File pdfsDir) {
        List<PageEntry> pageEntryList;
        for (File file : pdfsDir.listFiles()) {
            if (file.getName().contains(".pdf")) {
                try (var doc = new PdfDocument(new PdfReader(file))) {
                    for (int i = 1; i < doc.getPageNumber(doc.getLastPage()) - 1; i++) {
                        var text = PdfTextExtractor.getTextFromPage(doc.getPage(i));
                        var words = text.split("\\P{IsAlphabetic}+");
                        Set<String> setWords = Arrays.stream(words)
                                .map(String::toLowerCase)
                                .collect(Collectors.toSet());
                        for (String word : setWords) {
                            int count = Collections.frequency(Arrays.stream(words).collect(Collectors.toList()), word);
                            PageEntry pageEntry = new PageEntry(file.getName(), i, count);
                            if (!wordsMap.containsKey(word)) {
                                pageEntryList = new ArrayList<>();
                                pageEntryList.add(pageEntry);
                                wordsMap.put(word, pageEntryList);
                            } else {
                                wordsMap.get(word).add(pageEntry);
                            }
                            wordsMap.get(word).sort(Collections.reverseOrder());
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Ошибка чтения документа");
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public List<PageEntry> search(String word) {
        return wordsMap.get(word.toLowerCase());
    }
}
