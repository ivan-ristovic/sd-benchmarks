package net.ristovic.benchmarks.nlp;

import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.ie.util.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.*;
import edu.stanford.nlp.trees.*;

import org.graalvm.nativeimage.ObjectSnapshots;
import org.graalvm.nativeimage.ObjectSnapshots.ObjectSnapshot;
import org.graalvm.nativeimage.ObjectSnapshots.ObjectSnapshotProvider;
import org.graalvm.nativeimage.ObjectSnapshots.ObjectSnapshotRegion;
import org.graalvm.nativeimage.ObjectSnapshots.ObjectSnapshotSlot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Main {

    private static Stopwatch t = new Stopwatch();
    private static Stopwatch s = new Stopwatch();

    public static void main(String[] args) throws Exception {
        String text = TextProvider.getText();
        TextProvider.printStats(text);

        // Slow path
        CoreDocument document = annotateSlowPath(text);
        runBasicExamples(document);

        // Store result
        Path snapshotPath = storeResult(document);

        // Fast path
        document = annotateFastPath(snapshotPath, text);
        runBasicExamples(document);
    }

    private static CoreDocument annotateSlowPath(String text) {
        t.start();

            s.start();
                Properties props = new Properties();
                props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse,coref,kbp,quote");
                props.setProperty("coref.algorithm", "neural");
            s.stop();
            s.printElapsed("propSetup");

            s.start();
                StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
            s.stop();
            s.printElapsed("pipeline");

            s.start();
                CoreDocument document = new CoreDocument(text);
            s.stop();
            s.printElapsed("docCreate");

            s.start();
                pipeline.annotate(document);
            s.stop();
            s.printElapsed("docAnnotate");

        t.stop();
        t.printElapsed("slow::total");

        return document;
    }

    private static Path storeResult(CoreDocument document) throws IOException {
        System.gc();

        t.start();
            var provider = ObjectSnapshots.provider();
            var slot = ObjectSnapshots.snapshotRegion().getSlot(0);
            var snapshotPath = Files.createTempFile("ni-oss-", ".snapshot");
            var s = provider.createObjectSnapshot(document, slot);
            provider.store(slot, snapshotPath);
            provider.unload(slot);
        t.stop();
        t.printElapsed("store::total");

        return snapshotPath;
    }

    private static CoreDocument annotateFastPath(Path snapshotPath, String text) {
        t.start();
            var provider = ObjectSnapshots.provider();
            var slot = ObjectSnapshots.snapshotRegion().getSlot(0);
            var s = provider.load(snapshotPath, slot);
        t.stop();
        t.printElapsed("fast::total");

        return (CoreDocument) s.get();
    } 

    private static void runBasicExamples(CoreDocument document) {
        t.start();
        System.out.println();
        System.out.println("=== basic examples ===");
        
        System.out.println("Count sentences");
        System.out.println(document.sentences().size());
        
        Map<Integer, CorefChain> corefChains = document.corefChains();
        System.out.println("Example: coref chains for document");
        System.out.println(corefChains);
        System.out.println();

        System.out.println("===");
        System.out.println();
        t.stop();
        t.printElapsed("examples");
    }

    private static void runAllExamples(CoreDocument document) {
        System.out.println();
        System.out.println("=== examples from CoreNLP tutorial ===");

        // 10th token of the document
        CoreLabel token = document.tokens().get(9);
        System.out.println("Example: token");
        System.out.println(token);
        System.out.println();

        // text of the first sentence
        String sentenceText = document.sentences().get(0).text();
        System.out.println("Example: sentence");
        System.out.println(sentenceText);
        System.out.println();

        // second sentence
        CoreSentence sentence = document.sentences().get(1);

        // list of the part-of-speech tags for the second sentence
        List<String> posTags = sentence.posTags();
        System.out.println("Example: pos tags");
        System.out.println(posTags);
        System.out.println();

        // list of the ner tags for the second sentence
        List<String> nerTags = sentence.nerTags();
        System.out.println("Example: ner tags");
        System.out.println(nerTags);
        System.out.println();

        // constituency parse for the second sentence
        Tree constituencyParse = sentence.constituencyParse();
        System.out.println("Example: constituency parse");
        System.out.println(constituencyParse);
        System.out.println();

        // dependency parse for the second sentence
        SemanticGraph dependencyParse = sentence.dependencyParse();
        System.out.println("Example: dependency parse");
        System.out.println(dependencyParse);
        System.out.println();

        // kbp relations found in fifth sentence
        List<RelationTriple> relations = document.sentences().get(4).relations();
        System.out.println("Example: relation");
        System.out.println(relations.isEmpty() ? "none" : relations.get(0));
        System.out.println();

        // entity mentions in the second sentence
        List<CoreEntityMention> entityMentions = sentence.entityMentions();
        System.out.println("Example: entity mentions");
        System.out.println(entityMentions);
        System.out.println();

        // get document wide coref info
        Map<Integer, CorefChain> corefChains = document.corefChains();
        System.out.println("Example: coref chains for document");
        System.out.println(corefChains);
        System.out.println();

        // get quotes in document
        List<CoreQuote> quotes = document.quotes();
        CoreQuote quote = quotes.get(0);
        System.out.println("Example: quote");
        System.out.println(quote);
        System.out.println();

        // original speaker of quote
        // note that quote.speaker() returns an Optional
        System.out.println("Example: original speaker of quote");
        System.out.println(quote.speaker().get());
        System.out.println();

        // canonical speaker of quote
        System.out.println("Example: canonical speaker of quote");
        System.out.println(quote.canonicalSpeaker().get());
        System.out.println();

        System.out.println("===");
        System.out.println();
    }
}

