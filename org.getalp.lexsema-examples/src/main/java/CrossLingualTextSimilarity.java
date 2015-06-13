import org.getalp.lexsema.similarity.measures.SimilarityMeasure;
import org.getalp.lexsema.similarity.measures.crosslingual.TranslatorCrossLingualSimilarity;
import org.getalp.lexsema.similarity.measures.tverski.TverskiIndexSimilarityMeasureBuilder;
import org.getalp.lexsema.similarity.signatures.StringSemanticSignature;
import org.getalp.lexsema.similarity.signatures.StringSemanticSignatureImpl;
import org.getalp.lexsema.translation.BaiduAPITranslator;
import org.getalp.lexsema.translation.Translator;
import org.getalp.lexsema.util.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class CrossLingualTextSimilarity {
    private static Logger logger = LoggerFactory.getLogger(CrossLingualTextSimilarity.class);

    public static final String YANDEX_KEY = "trnsl.1.1.20150612T083053Z.116892fa94abf4c3.41fa35d9770a4148842667b6aaa1cba5a78e40fa";
    public static final String BAIDU_KEY = "HmnrXaDYZ8XTn2NnGPU4kYbT";

    public static void main(String[] args) throws IOException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        if(args.length<4){
            usage();
        }
        Language source = Language.fromCode(args[0]);
        Language target = Language.fromCode(args[1]);

        StringSemanticSignature signature1 = new StringSemanticSignatureImpl(args[2]);
        signature1.setLanguage(source);
        StringSemanticSignature signature2 = new StringSemanticSignatureImpl(args[3]);
        signature2.setLanguage(target);

        Translator translator = new BaiduAPITranslator(BAIDU_KEY);
        SimilarityMeasure similarityMeasure = new TverskiIndexSimilarityMeasureBuilder()
                .fuzzyMatching(true).alpha(1d).beta(0d).gamma(0d).normalize(true).regularizeOverlapInput(true).build();
        SimilarityMeasure crossLingualMeasure = new TranslatorCrossLingualSimilarity(similarityMeasure, translator);

        double sim = crossLingualMeasure.compute(signature1, signature2);

        String output = String.format("The similarity between \"%s\" and \"%s\" is %s", signature1.toString(), signature2.toString(), sim);
        logger.info(output);

    }

    private static void usage() {
        logger.error("Usage -- CrossLingualTextSimilarity [sourcelang] [targetlang] \"Source text\" \"Target text\"");
        logger.error("\tExample -- CrossLingualTextSimilarity en fr \"Hello my name is john\" \"Bonjour, je m'appelle John\"");
        System.exit(1);
    }
}
