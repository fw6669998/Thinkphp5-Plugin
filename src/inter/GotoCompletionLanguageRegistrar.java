package inter;

import com.intellij.lang.Language;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public interface GotoCompletionLanguageRegistrar extends GotoCompletionRegistrar {
    boolean support(@NotNull Language language);
}
