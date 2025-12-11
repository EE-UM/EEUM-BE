package com.eeum.domain.common.spamfilter.service;

import com.eeum.domain.common.ai.dto.response.SpamResultResponse;
import com.eeum.domain.common.constant.DiscordWebhookType;
import com.eeum.domain.common.spamfilter.repository.ForbiddenWordsRepository;
import com.eeum.domain.common.webhook.discord.DiscordWebhookResponse;
import com.eeum.domain.common.webhook.discord.MessageService;
import com.eeum.domain.common.webhook.discord.message.SpamMessageFormatter;
import com.eeum.domain.posts.entity.Posts;
import com.eeum.domain.posts.repository.PostsRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer.KoreanToken;
import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scala.collection.Seq;

@Service
@RequiredArgsConstructor
public class SpamFilterService {

    public static final int DEFAULT_SCORE = 0;
    public static final int SCORE_ADD_UNIT = 1;
    public static final String SYSTEM_MESSAGE = """
                You are a spam classifier.
                If the text contains profanity, hate, harassment, sexual content, violence, spam, or ads → set flag = 1.
                If safe → set flag = 0.
            """;

    private final ChatClient chatClient;

    private final ForbiddenWordsRepository forbiddenWordsRepository;
    private final PostsRepository postsRepository;

    private final MessageService messageService;

    @Transactional
    public void spamPostFilter(Long postId, String content) {
        int score = DEFAULT_SCORE;

        spamWordFilter(postId, content, score);
    }

    private void spamWordFilter(Long postId, String content, int score) {
        List<String> tokens = getParsedTokens(content);

        List<String> findWords = forbiddenWordsRepository.findIn(tokens);
        if (!findWords.isEmpty()) {
            score += SCORE_ADD_UNIT;
        }

        if (spamPostAiFilter(content)) {
            score += SCORE_ADD_UNIT;
        }

        determineByScore(postId, score);
    }

    private void determineByScore(Long postId, int score) {
        Posts posts = postsRepository.findById(postId)
                .orElseThrow();

        if (score == 1) {
            messageService.sendDiscordWebhookMessage(DiscordWebhookResponse.of(SpamMessageFormatter.formatSpamMessageLevel1(
                    String.valueOf(posts.getUserId()),
                    String.valueOf(posts.getId()),
                    String.valueOf(score),
                    posts.getContent()
            )), DiscordWebhookType.SPAM);
        }

        if (score == 2) {
            posts.softDelete();
            messageService.sendDiscordWebhookMessage(DiscordWebhookResponse.of(SpamMessageFormatter.formatSpamMessageLevel2(
                    String.valueOf(posts.getUserId()),
                    String.valueOf(posts.getId()),
                    String.valueOf(score),
                    posts.getContent()
            )), DiscordWebhookType.SPAM);
        }
    }

    private boolean spamPostAiFilter(String content) {
        SpamResultResponse result = chatClient.prompt()
                .advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
                .system(SYSTEM_MESSAGE)
                .user(content)
                .call()
                .entity(SpamResultResponse.class);

        return result.label() == 1;
    }

    private static List<String> getParsedTokens(String content) {
        CharSequence normalize = OpenKoreanTextProcessorJava.normalize(content);
        Seq<KoreanToken> tokenSeq = OpenKoreanTextProcessorJava.tokenize(normalize);
        List<String> tokens = OpenKoreanTextProcessorJava.tokensToJavaStringList(tokenSeq);
        return tokens;
    }
}
