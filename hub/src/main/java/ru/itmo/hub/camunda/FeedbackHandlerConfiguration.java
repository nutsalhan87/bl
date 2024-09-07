package ru.itmo.hub.camunda;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;
import ru.itmo.hub.core.model.comments.comment.CommentRepository;
import ru.itmo.hub.core.model.primary.reaction.Reaction;
import ru.itmo.hub.core.model.primary.reaction.ReactionRepository;
import ru.itmo.hub.service.CommentService;
import ru.itmo.shared.HubExternalTaskHandler;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class FeedbackHandlerConfiguration {
    private final CommentService commentService;
    private final CommentRepository commentRepository;
    private final ReactionRepository reactionRepository;
    private final TransactionTemplate transactionTemplate;

    @Bean
    @ExternalTaskSubscription(value = "create-comment", variableNames = {"videoId", "text", "authorName"})
    public ExternalTaskHandler createCommentHandler() {
        return new HubExternalTaskHandler((externalTask, externalTaskService, helper) -> {
            log.info("Start create-comment topic handler");
            Long videoId = helper.getVarOrThrow("videoId");
            String text = helper.getVarOrThrow("text");
            String authorName = helper.getVarOrThrow("authorName");

            commentService.createComment(videoId, text, authorName);
            externalTaskService.complete(externalTask);
        }, Optional.of(transactionTemplate));
    }

    @Bean
    @ExternalTaskSubscription(value = "get-comment", variableNames = {"commentId"})
    public ExternalTaskHandler getCommentHandler() {
        return new HubExternalTaskHandler((externalTask, externalTaskService, helper) -> {
            log.info("Start get-comment topic handler");
            Long commentId = helper.getVarOrThrow("commentId");
            var comment = commentRepository.getCommentByIdOrThrow(commentId);
            helper.completeWithVar("commentText", comment.getValue());
        }, Optional.of(transactionTemplate));
    }

    @Bean
    @ExternalTaskSubscription(value = "delete-comment", variableNames = {"commentId"})
    public ExternalTaskHandler deleteCommentHandler() {
        return new HubExternalTaskHandler((externalTask, externalTaskService, helper) -> {
            log.info("Start delete-comment topic handler");
            Long commentId = helper.getVarOrThrow("commentId");
            commentRepository.deleteById(commentId);
            externalTaskService.complete(externalTask);
        }, Optional.of(transactionTemplate));
    }

    @Bean
    @ExternalTaskSubscription(value = "get-current-reaction", variableNames = {"videoId", "authorName"})
    public ExternalTaskHandler getCurrentReactionHandler() {
        return new HubExternalTaskHandler((externalTask, externalTaskService, helper) -> {
            log.info("Start get-current-reaction topic handler");
            Long videoId = helper.getVarOrThrow("videoId");
            String authorName = helper.getVarOrThrow("authorName");
            var currentReaction = reactionRepository.findReactionByVideoIdAndAuthorName(videoId, authorName)
                    .map(reaction -> reaction.isPositive() ? "like" : "dislike")
                    .orElse("empty");
            helper.completeWithVar("currentReaction", currentReaction);
        }, Optional.of(transactionTemplate));
    }

    @Bean
    @ExternalTaskSubscription(value = "unreact", variableNames = {"videoId", "authorName"})
    public ExternalTaskHandler unreactHandler() {
        return new HubExternalTaskHandler((externalTask, externalTaskService, helper) -> {
            log.info("Start unreact topic handler");
            Long videoId = helper.getVarOrThrow("videoId");
            String authorName = helper.getVarOrThrow("authorName");
            Long reactionId = reactionRepository.findReactionByVideoIdAndAuthorName(videoId, authorName)
                    .map(Reaction::getId)
                    .orElse(-1L);
            if (reactionId != -1) {
                reactionRepository.deleteById(reactionId);
            }
            helper.completeWithVar("reactionId", reactionId);
        }, Optional.of(transactionTemplate));
    }

    @Bean
    @ExternalTaskSubscription(value = "like", variableNames = {"videoId", "authorName"})
    public ExternalTaskHandler likeHandler() {
        return new HubExternalTaskHandler((externalTask, externalTaskService, helper) -> {
            log.info("Start like topic handler");
            Long videoId = helper.getVarOrThrow("videoId");
            String authorName = helper.getVarOrThrow("authorName");
            var reaction = reactionRepository.findReactionByVideoIdAndAuthorName(videoId, authorName)
                    .map(r -> {
                        r.setPositive(true);
                        return r;
                    })
                    .orElseGet(() -> new Reaction(null, videoId, authorName, true));
            reactionRepository.save(reaction);
            helper.completeWithVar("reactionId", reaction.getId());
        }, Optional.of(transactionTemplate));
    }

    @Bean
    @ExternalTaskSubscription(value = "dislike", variableNames = {"videoId", "authorName"})
    public ExternalTaskHandler dislikeHandler() {
        return new HubExternalTaskHandler((externalTask, externalTaskService, helper) -> {
            log.info("Start dislike topic handler");
            Long videoId = helper.getVarOrThrow("videoId");
            String authorName = helper.getVarOrThrow("authorName");
            var reaction = reactionRepository.findReactionByVideoIdAndAuthorName(videoId, authorName)
                    .map(r -> {
                        r.setPositive(false);
                        return r;
                    })
                    .orElseGet(() -> new Reaction(null, videoId, authorName, false));
            reactionRepository.save(reaction);
            helper.completeWithVar("reactionId", reaction.getId());
        }, Optional.of(transactionTemplate));
    }

    @Bean
    @ExternalTaskSubscription(value = "compensate-reaction", variableNames = {"videoId", "authorName", "reactionId", "currentReaction"})
    public ExternalTaskHandler compensateReactionHandler() {
        return new HubExternalTaskHandler((externalTask, externalTaskService, helper) -> {
            log.info("Start compensate-reaction topic handler");
            Long videoId = helper.getVarOrThrow("videoId");
            String authorName = helper.getVarOrThrow("authorName");
            Long reactionId = helper.getVarOrThrow("reactionId");
            String currentReaction = helper.getVarOrThrow("currentReaction");
            if (Objects.equals(currentReaction, "like") || Objects.equals(currentReaction, "dislike")) {
                var isPositive = Objects.equals(currentReaction, "like");
                var reaction = reactionRepository.findById(reactionId)
                        .map(r -> {
                            r.setPositive(isPositive);
                            return r;
                        })
                        .orElseGet(() -> new Reaction(reactionId, videoId, authorName, isPositive));
                reactionRepository.save(reaction);
            } else {
                reactionRepository.deleteById(reactionId);
            }
            externalTaskService.complete(externalTask);
        }, Optional.of(transactionTemplate));
    }

    @Bean
    @ExternalTaskSubscription(value = "delete-reactions", variableNames = {"videoId"})
    public ExternalTaskHandler deleteReactionHandler() {
        return new HubExternalTaskHandler((externalTask, externalTaskService, helper) -> {
            log.info("Start delete-reactions topic handler");
            Long videoId = helper.getVarOrThrow("videoId");
            reactionRepository.deleteByVideoId(videoId);
            externalTaskService.complete(externalTask);
        }, Optional.of(transactionTemplate));
    }
}
